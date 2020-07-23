package com.netsensia.rivalchess.engine;

import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.util.FenUtils;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class EvaluateTest {

    private static Boolean RECALCULATE = false;

    @Test
    public void testLazyEvaluation() {
        assertEvaluationScore("2kr1r2/B1pq1Npp/1p1p1n2/b2Pp3/4P3/1P5P/NPP2PP1/2KR3R b - - 0 6", 1536, true, 785);
    }

    @Test
    public void testBigPositionalScores() {
        assertEvaluationScore("8/7p/5k2/5p2/pPp2P2/3pP1K1/3r3P/8 b - - 1 3", 3165, true, -Integer.MAX_VALUE);
        assertEvaluationScore("3R1Q2/7k/6pp/8/2P1p3/P3P3/1P2P1K1/8 w - - 1 5", 5679, true, -Integer.MAX_VALUE);
        assertEvaluationScore("1k6/8/1P6/p4pP1/P1P5/2P5/5K1P/8 w - - 1 7", 2860, true, -Integer.MAX_VALUE);
    }

    @Test
    public void testEvaluationScores() throws IllegalFenException {
        assertEvaluationScore("3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - -", -17, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/Bn2Pnp1/4N3/1p2P3/2N2Q2/PPPB1P1P/R3K2r w Qkq - 0 3", -1293, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/1R6/1R6 b - -", -3256, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/2R5/1R6 b - -", -3255, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", -817, true);
        assertEvaluationScore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 0, true);
        assertEvaluationScore("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 4655, true);
        assertEvaluationScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", -23, true);
        assertEvaluationScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 646, true);
        assertEvaluationScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 52, true);
        assertEvaluationScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", -60, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 6, true);
    }

    private void assertEvaluationScore(String fen, int expectedScore, boolean flip) throws IllegalFenException {
            assertEvaluationScore(fen, expectedScore, flip, -Integer.MAX_VALUE);
    }
    private void assertEvaluationScore(String fen, int expectedScore, boolean flip, int minScore) throws IllegalFenException {

        Board board = Board.fromFen(fen);
        EngineBoard engineBoard = new EngineBoard();
        engineBoard.setBoard(board);

        int actualScore = com.netsensia.rivalchess.engine.eval.EvaluateKt.evaluate(engineBoard, minScore);

        if (RECALCULATE) {
            System.out.println("assertEvaluationScore(\"" + fen + "\", " + actualScore + ", true);");
        } else {
            assertEquals(expectedScore, actualScore);

            if (flip) assertEvaluationScore(FenUtils.invertFen(fen), expectedScore, false);
        }
    }


}
