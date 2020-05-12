package com.netsensia.rivalchess.eve;

import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.engine.core.ConstantsKt;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.Search;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.util.FenUtils;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class EngineVEngineTest {

    private EngineBoard engineBoard = new EngineBoard();

    public EngineVEngineTest() throws IllegalFenException {
    }

    private void makeMove() throws IllegalFenException, InterruptedException {

        Search search = new Search();

        new Thread(search).start();

        search.setBoard(engineBoard);
        search.setSearchDepth(6);
        search.setNodesToSearch(10000);
        search.setMillisToThink(Limit.MAX_SEARCH_MILLIS.getValue());
        search.startSearch();

        MILLISECONDS.sleep(50);

        await().atMost(5, SECONDS).until(() -> !search.isSearching());
    }

    @Test
    public void runGame() throws IllegalFenException {
        engineBoard.setBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
    }
}
