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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EpdTest {

    private static final int MAX_SEARCH_SECONDS = 100;

    private final List<String> excludeTests = Collections.unmodifiableList(Arrays.asList(
            "WAC.008"
    ));

    private void testPosition(EpdItem epdItem) throws IllegalFenException, InterruptedException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(epdItem.getFen()));
        RivalSearch rivalSearch = new RivalSearch();

        new Thread(rivalSearch).start();

        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(12);
        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
        rivalSearch.setHashSizeMB(32);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        await().atMost(MAX_SEARCH_SECONDS, SECONDS).until(() -> !rivalSearch.isSearching());

        System.out.println(epdItem.getId());

        Assert.assertThat(epdItem.getBestMoves(),
                hasItem(ChessBoardConversion.getPGNMoveFromCompactMove(rivalSearch.getCurrentMove(), engineChessBoard)));
    }

    public void runEpdSuite(String filename) throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("epd/" + filename).getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());

        for (EpdItem epdItem : epdReader) {
            if (!excludeTests.contains(epdItem.getId())) {
                testPosition(epdItem);
            }
        }
    }

    @Test
    public void winAtChess() throws IOException, IllegalEpdItemException, IllegalFenException, InterruptedException {
        runEpdSuite("winAtChess.epd");
    }
}
