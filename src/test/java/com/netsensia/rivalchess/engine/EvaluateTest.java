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

    private void assertRightWayScore(String fen, int expectedScore) throws IllegalFenException {

        EngineBoard engineBoard = new EngineBoard();
        Function<EngineBoard, Integer> whiteFunc = com.netsensia.rivalchess.engine.eval.RightWayKt::getWhiteKingRightWayScore;
        Function<EngineBoard, Integer> blackFunc = com.netsensia.rivalchess.engine.eval.RightWayKt::getBlackKingRightWayScore;

        engineBoard.setBoard(FenUtils.getBoardModel(fen));
        assertEquals(expectedScore, whiteFunc.apply(engineBoard).intValue());

        engineBoard.setBoard(FenUtils.getBoardModel(FenUtils.invertFen(fen)));
        assertEquals(expectedScore, blackFunc.apply(engineBoard).intValue());
    }

    private void assertEvaluationScore(String fen, int expectedScore, boolean flip) throws IllegalFenException {

        Board board = Board.fromFen(fen);
        EngineBoard engineBoard = new EngineBoard();
        engineBoard.setBoard(board);

        int actualScore = com.netsensia.rivalchess.engine.eval.EvaluateKt.evaluate(engineBoard);

        if (RECALCULATE) {
            System.out.println("assertEvaluationScore(\"" + fen + "\", " + actualScore + ");");
        } else {
            assertEquals(expectedScore, actualScore);

            if (flip) assertEvaluationScore(FenUtils.invertFen(fen), expectedScore, false);
        }
    }

    @Test
    public void testEvaluationScores() throws IllegalFenException {
        assertEvaluationScore("3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - -", 213, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/Bn2Pnp1/4N3/1p2P3/2N2Q2/PPPB1P1P/R3K2r w Qkq - 0 3", -680, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/1R6/1R6 b - -", -2036, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/2R5/1R6 b - -", -2035, true);
        assertEvaluationScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", -568, true);
        assertEvaluationScore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 0, true);
        assertEvaluationScore("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 3069, true);
        assertEvaluationScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", -20, true);
        assertEvaluationScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 465, true);
        assertEvaluationScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 52, true);
        assertEvaluationScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", -60, true);
        assertEvaluationScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 6, true);
    }

    @Test
    public void scoreWhiteRightWayPositions() throws IllegalFenException {
        // Strong (A)
        assertRightWayScore("r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN5/1P3PPP/3R1RK1 b - -", 30);
        // Strong (A) + Knight
        assertRightWayScore("r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN2N2/1P3PPP/3R1RK1 b - -", 30);

        // Strong (B)
        assertRightWayScore("r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN3P1/1P3PBP/3R1RK1 b - -", 25);
        // Strong (B) + Opponent no same colour bishop
        assertRightWayScore("r4rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN3P1/1P3PBP/3R1RK1 b - -", 25);

        // Quite Strong (C)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN3PP/1P3PBK/3R1R2 b - -", 17);

        // Quite Strong (D) + Knight on F3 + No opponent same colour bishop as h3
        assertRightWayScore("r4rk1/1p1n1ppp/p1p2q2/4p3/P3Pn2/1QN2N1P/1P3PP1/3R1RK1 b - -", 17);

        // Quite Strong (D) + Knight on F3 but opponent same colour bishop as h3
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3Pn2/1QN2N1P/1P3PP1/3R1RK1 b - -", 17);

        // Strong (E)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3PP2/1QN5/1P4PP/3R1RK1 b - -", 20);

        // Strong (E) + Knight on F3
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3PP2/1QN2N2/1P4PP/3R1RK1 b - -", 30);

        // Weak (F)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3P3/1QN2P2/1P4PP/3R1RK1 b - -", -2);

        // Very Weak (G)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3P3/1QN3P1/1P3P1P/3R1RK1 b - -", 0);

        // Very Weak (H)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3P3/1QN2P1P/1P4P1/3R1RK1 b - -", -7);

        // Weak (I)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3PP2/1QN3P1/1P5P/3R1RK1 b - -", 0);
    }

}
