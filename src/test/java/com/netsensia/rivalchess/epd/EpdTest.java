package com.netsensia.rivalchess.epd;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.EpdReader;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

public class EpdTest {

    private static final int MAX_SEARCH_SECONDS = 3;

    private final List<String> noFailIfNoSolve = Collections.unmodifiableList(Arrays.asList(
        "WAC.001"
    ));

    private void testPosition(String fen, String expectedMove) throws IllegalFenException, InterruptedException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(fen));
        RivalSearch rivalSearch = new RivalSearch();

        new Thread(rivalSearch).start();

        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(4);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        await().atMost(MAX_SEARCH_SECONDS, SECONDS).until(() -> !rivalSearch.isSearching());

        assertEquals(expectedMove, ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(rivalSearch.getCurrentMove()));
    }

    public void runEpdSuite(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("epd/" + filename).getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());
    }

    @Test
    public void winAtChess() throws IOException {
        runEpdSuite("winAtChess.epd");
    }
}
