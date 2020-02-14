package com.netsensia.rivalchess.eve;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class EngineVEngineTest {

    private EngineChessBoard engineChessBoard = new EngineChessBoard();

    public EngineVEngineTest() throws IllegalFenException {
    }

    private void makeMove() throws IllegalFenException, InterruptedException {

        RivalSearch rivalSearch = new RivalSearch();

        new Thread(rivalSearch).start();

        rivalSearch.setSearchDepth(6);
        rivalSearch.setNodesToSearch(10000);
        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
        rivalSearch.startSearch();

        MILLISECONDS.sleep(50);

        await().atMost(5, SECONDS).until(() -> !rivalSearch.isSearching());
    }

    @Test
    public void runGame() throws IllegalFenException {
        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
    }
}
