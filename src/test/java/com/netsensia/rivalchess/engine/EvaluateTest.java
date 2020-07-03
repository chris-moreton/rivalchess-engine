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

    private void assertEvaluationScore(String fen, int expectedScore, boolean flip) throws IllegalFenException {

        Board board = Board.fromFen(fen);
        EngineBoard engineBoard = new EngineBoard();
        engineBoard.setBoard(board);

        int actualScore = com.netsensia.rivalchess.engine.eval.EvaluateKt.evaluate(engineBoard);

        if (RECALCULATE) {
            System.out.println("assertEvaluationScore(\"" + fen + "\", " + actualScore + ", true);");
        } else {
            assertEquals(expectedScore, actualScore);

            if (flip) assertEvaluationScore(FenUtils.invertFen(fen), expectedScore, false);
        }
    }

    @Test
    public void testEvaluationScores() throws IllegalFenException {
        assertEvaluationScore("3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - -", 233, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/Bn2Pnp1/4N3/1p2P3/2N2Q2/PPPB1P1P/R3K2r w Qkq - 0 3", -680, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/1R6/1R6 b - -", -1946, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/2R5/1R6 b - -", -1945, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", -478, true);
        assertEvaluationScore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 0, true);
        assertEvaluationScore("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 2994, true);
        assertEvaluationScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", 70, true);
        assertEvaluationScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 375, true);
        assertEvaluationScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 52, true);
        assertEvaluationScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 60, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 6, true);
    }

}
