package com.netsensia.rivalchess.epd;

import com.netsensia.rivalchess.enums.SearchState;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.Search;
import com.netsensia.rivalchess.exception.IllegalEpdItemException;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.EpdItem;
import com.netsensia.rivalchess.util.EpdReader;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.hasItem;

@SuppressWarnings("squid:S106")
public class EpdTest {

    private static final int MAX_SEARCH_SECONDS = 1000;
    private static Search search;

    @BeforeClass
    public static void setup() {
        search = new Search();
        new Thread(search).start();
    }

    private final List<String> failingPositions = Collections.unmodifiableList(Arrays.asList(
            "WAC.230","WAC.274",
            "WAC.100",
            "WAC.141","WAC.213",
            "WAC.002","WAC.008","WAC.071","WAC.080","WAC.092","WAC.116","WAC.120",
            "WAC.163","WAC.196","WAC.200","WAC.229","WAC.237","WAC.247","WAC.256",
            "WAC.265","WAC.275","WAC.293","WAC.297",
            "WAC.041","WAC.157","WAC.204","WAC.242","WAC.280","WAC.291",
            "WAC.090","WAC.287"
    ));

    @Test
    public void winAtChess() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException, InvalidMoveException {
        runEpdSuite("winAtChess.epd", "WAC.001", true);
    }

    @Test
    @Ignore
    public void winAtChessFails() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException, InvalidMoveException {
        runEpdSuite("winAtChess.epd", "WAC.001", false);
    }

    private void testPosition(EpdItem epdItem, boolean expectedToPass) throws IllegalFenException, InterruptedException, InvalidMoveException {

        if (new Random().nextInt(1) > 0) {
            return;
        }

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(epdItem.getFen()));

        search.quit();

        search = new Search();
        new Thread(search).start();

        search.setBoard(engineChessBoard);
        search.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
        search.setMillisToThink(MAX_SEARCH_SECONDS * 1000);

        search.setNodesToSearch(epdItem.getMaxNodesToSearch());
        search.setHashSizeMB(32);
        search.clearHash();
        search.startSearch();

        MILLISECONDS.sleep(100);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(epdItem.getId() + " " + dtf.format(now));

        try {
            await().atMost(MAX_SEARCH_SECONDS, SECONDS).until(() -> !search.isSearching());
        } catch (ConditionTimeoutException e) {
            search.stopSearch();

            SearchState state;
            do {
                state = search.getEngineState();
            }
            while (state != SearchState.READY && state != SearchState.COMPLETE);
        }

        final String move = ChessBoardConversion.getPgnMoveFromCompactMove(
                search.getCurrentMove(), engineChessBoard.getFen());

        System.out.println("Looking for " + move + " in " + epdItem.getBestMoves());

        if (expectedToPass) {
            Assert.assertThat(epdItem.getBestMoves(), hasItem(move));
        } else {
            Assert.assertFalse(epdItem.getBestMoves().contains(
                    ChessBoardConversion.getPgnMoveFromCompactMove(
                        search.getCurrentMove(), engineChessBoard.getFen())));
        }
    }

    public void runEpdSuite(String filename, String startAtId, boolean expectedToPass)
            throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException, InvalidMoveException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("epd/" + filename)).getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());

        runEpdSuite(startAtId, epdReader, expectedToPass);
    }

    private void runEpdSuite(String startAtId, EpdReader epdReader, boolean expectedToPass) throws IllegalFenException, InterruptedException, InvalidMoveException {
        boolean processTests = false;

        for (EpdItem epdItem : epdReader) {
            processTests = processTests || (epdItem.getId().equals(startAtId));
            if (processTests && expectedToPass != failingPositions.contains(epdItem.getId())) {
                testPosition(epdItem, expectedToPass);
            }
        }
    }

}
