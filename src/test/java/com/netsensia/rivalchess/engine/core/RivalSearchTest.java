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

    private void assertBestMove(String fen, String expectedMove, int expectedScore) throws IllegalFenException, InterruptedException {

        RivalSearch rivalSearch = new RivalSearch();

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(fen));

        new Thread(rivalSearch).start();

        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(4);
        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        await().atMost(30, SECONDS).until(() -> !rivalSearch.isSearching());

        assertEquals(expectedMove, ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(rivalSearch.getCurrentMove()));
        assertEquals(expectedScore, rivalSearch.getCurrentScore());

    }

    private void assertNodeCount(String fen, long expectedNodes) throws IllegalFenException, InterruptedException {

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(fen));

        RivalSearch rivalSearch = new RivalSearch();

        new Thread(rivalSearch).start();

        rivalSearch.setBoard(engineChessBoard);
        rivalSearch.setSearchDepth(6);
        rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
        rivalSearch.startSearch();

        SECONDS.sleep(1);

        await().atMost(30, SECONDS).until(() -> !rivalSearch.isSearching());

        assertEquals(expectedNodes, rivalSearch.getNodes());
    }

    @Test
    public void testBestMoves() throws IllegalFenException, InterruptedException {
        assertBestMove("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", "g4g6", -1176);
        assertBestMove("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", "f7e6", -86);
        assertBestMove("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", "g5g7", 9996);
        assertBestMove("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2h1q", 1238);
        assertBestMove("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "e5d4", 179);
        assertBestMove("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "b1c3", 55);
        assertBestMove("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4", 83);
        assertBestMove("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "d5e6", 25);
    }

    @Test
    public void testNodeCount() throws IllegalFenException, InterruptedException {
        assertNodeCount("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -", 24599);
        assertNodeCount("8/k1b5/P4p2/1Pp2p1p/K1P2P1P/8/3B4/8 w - -", 4107);
        assertNodeCount("3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - -", 113324);
        assertNodeCount("4r3/1Q1qk2p/p4pp1/3Pb3/P7/6PP/5P2/4R1K1 w - - bm", 76013);
        assertNodeCount("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", 22368);
        assertNodeCount("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", 3011);
        assertNodeCount("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 1247);
        assertNodeCount("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", 395365);
        assertNodeCount("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 9199);
        assertNodeCount("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 68199);
        assertNodeCount("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 33295);
        assertNodeCount("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 153223);
    }
}
