package com.netsensia.rivalchess.epd;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.exception.IllegalEpdItemException;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.EpdItem;
import com.netsensia.rivalchess.util.EpdReader;
import com.netsensia.rivalchess.util.FenUtils;
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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.hasItem;

@SuppressWarnings("squid:S106")
public class EpdTest {

    private static final int MAX_SEARCH_SECONDS = 300;
    private static RivalSearch rivalSearch;

    @BeforeClass
    public static void setup() {
        rivalSearch = new RivalSearch();
        new Thread(rivalSearch).start();
    }

    private final List<String> failingPositions = Collections.unmodifiableList(Arrays.asList(
            "WAC.230","WAC.274",
            "WAC.100",
            "WAC.111","WAC.141","WAC.213","WAC.243",
            "WAC.002","WAC.008","WAC.071","WAC.080","WAC.092","WAC.116","WAC.120",
            "WAC.163","WAC.196","WAC.200","WAC.229","WAC.237","WAC.247","WAC.256",
            "WAC.265","WAC.269","WAC.275","WAC.293","WAC.297",
            "WAC.041","WAC.090","WAC.157","WAC.193","WAC.204","WAC.242","WAC.280","WAC.291"
    ));

    @Test
    public void winAtChess() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd", "WAC.291", true);
    }

    @Test
    public void winAtChessFails() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd", "WAC.001", false);
    }

    private void testPosition(EpdItem epdItem, boolean expectedToPass) throws IllegalFenException, InterruptedException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(epdItem.getFen()));

        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH - 2);
        rivalSearch.setMillisToThink(MAX_SEARCH_SECONDS * 1000);

        rivalSearch.setNodesToSearch(epdItem.getMaxNodesToSearch());
        rivalSearch.setHashSizeMB(32);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(epdItem.getId() + " " + dtf.format(now));

        try {
            await().atMost(MAX_SEARCH_SECONDS, SECONDS).until(() -> !rivalSearch.isSearching());
        } catch (ConditionTimeoutException e) {
            rivalSearch.stopSearch();

            int state;
            do {
                state = rivalSearch.getEngineState();
            }
            while (state != RivalConstants.SEARCHSTATE_READY && state != RivalConstants.SEARCHSTATE_SEARCHCOMPLETE);
        }

        final String move = ChessBoardConversion.getPGNMoveFromCompactMove(rivalSearch.getCurrentMove(), engineChessBoard);

        System.out.println("Looking for " + move + " in " + epdItem.getBestMoves());

        if (expectedToPass) {
            Assert.assertThat(epdItem.getBestMoves(), hasItem(move));
        } else {
            Assert.assertFalse(epdItem.getBestMoves().contains(ChessBoardConversion.getPGNMoveFromCompactMove(
                    rivalSearch.getCurrentMove(), engineChessBoard)));
        }
    }

    public void runEpdSuite(String filename, String startAtId, boolean expectedToPass)
            throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("epd/" + filename)).getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());

        runEpdSuite(startAtId, epdReader, expectedToPass);
    }

    private void runEpdSuite(String startAtId, EpdReader epdReader, boolean expectedToPass) throws IllegalFenException, InterruptedException {
        boolean processTests = false;

        for (EpdItem epdItem : epdReader) {
            processTests = processTests || (epdItem.getId().equals(startAtId));
            if (processTests && expectedToPass != failingPositions.contains(epdItem.getId())) {
                testPosition(epdItem, expectedToPass);
            }
        }
    }

}
