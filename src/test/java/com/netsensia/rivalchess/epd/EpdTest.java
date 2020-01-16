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
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EpdTest {

    private static final int MAX_SEARCH_SECONDS = 20;
    private static RivalSearch rivalSearch;

    @BeforeClass
    public static void setup() {
        rivalSearch = new RivalSearch();
        new Thread(rivalSearch).start();
    }

    private final List<String> failingPositions = Collections.unmodifiableList(Arrays.asList(
            "WAC.002","WAC.008","WAC.168","WAC.201","WAC.209",
            "WAC.213","WAC.229","WAC.230","WAC.251","WAC.259",
            "WAC.268","WAC.269","WAC.274","WAC.276","WAC.281",
            "WAC.296"
    ));

    private void testPosition(EpdItem epdItem, boolean expectedToPass) throws IllegalFenException, InterruptedException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(epdItem.getFen()));

        rivalSearch.quit();
        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(10);
        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
        rivalSearch.setHashSizeMB(32);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        System.out.println(epdItem.getId());

        try {
            await().atMost(MAX_SEARCH_SECONDS, SECONDS).until(() -> !rivalSearch.isSearching());
        } catch (ConditionTimeoutException e) {
            assertFalse(expectedToPass);
            return;
        }

        final String move = ChessBoardConversion.getPGNMoveFromCompactMove(rivalSearch.getCurrentMove(), engineChessBoard);
        System.out.println("Looking for " + move + " in " + epdItem.getBestMoves());

        if (expectedToPass) {
            Assert.assertThat(epdItem.getBestMoves(), hasItem(move));
        } else {
            Assert.assertTrue(!epdItem.getBestMoves().contains(ChessBoardConversion.getPGNMoveFromCompactMove(
                    rivalSearch.getCurrentMove(), engineChessBoard)));
        }
    }

    public void runEpdSuite(String filename, String startAtId, boolean expectedToPass)
            throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("epd/" + filename).getFile());

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

    @Test
    public void winAtChess() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd", "WAC.001", true);
    }

    @Test
    public void winAtChessFails() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd", "WAC.001", false);
    }
}
