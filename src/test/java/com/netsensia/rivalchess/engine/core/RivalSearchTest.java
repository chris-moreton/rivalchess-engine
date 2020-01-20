package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RivalSearchTest {

    private void assertEvaluationScore(String fen, int expectedScore, boolean flip) throws IllegalFenException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(fen));
        RivalSearch rivalSearch = new RivalSearch();
        int actualScore = rivalSearch.evaluate(engineChessBoard);

        assertEquals(expectedScore, actualScore);

        if (flip) {
            assertEvaluationScore(FenUtils.invertFen(fen), expectedScore, false);
        }

    }

    @Test
    public void testEvaluationScores() throws IllegalFenException {
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", -568, true);
        assertEvaluationScore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 0, true);
        assertEvaluationScore("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 4101, true);
        assertEvaluationScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", 0, true);
        assertEvaluationScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 461, true);
        assertEvaluationScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 52, true);
        assertEvaluationScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", -60, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 6, true);
    }

    private void assertBestMove(String fen, String expectedMove) throws IllegalFenException, InterruptedException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(fen));
        RivalSearch rivalSearch = new RivalSearch();

        new Thread(rivalSearch).start();

        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(4);
        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        await().atMost(30, SECONDS).until(() -> !rivalSearch.isSearching());

        assertEquals(expectedMove, ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(rivalSearch.getCurrentMove()));
    }

    @Test
    public void testBestMoves() throws IllegalFenException, InterruptedException {
        assertBestMove("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", "g4g6");
        assertBestMove("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", "f7e6");
        assertBestMove("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", "g5g7");
        assertBestMove("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2h1q");
        assertBestMove("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "e5d4");
        assertBestMove("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "b1c3");
        assertBestMove("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4");
        assertBestMove("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "d5e6");
    }
}
