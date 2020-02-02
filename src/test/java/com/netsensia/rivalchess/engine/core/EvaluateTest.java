package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class EvaluateTest {

    private void assertRightWayScore(String fen, int expectedScore) throws IllegalFenException {

        EngineChessBoard engineBoard = new EngineChessBoard();
        Function<EngineChessBoard, Integer> whiteFunc = Evaluate::getWhiteKingRightWayScore;
        Function<EngineChessBoard, Integer> blackFunc = Evaluate::getBlackKingRightWayScore;

        engineBoard.setBoard(FenUtils.getBoardModel(fen));
        assertEquals(expectedScore, whiteFunc.apply(engineBoard).intValue());

        engineBoard.setBoard(FenUtils.getBoardModel(FenUtils.invertFen(fen)));
        assertEquals(expectedScore, blackFunc.apply(engineBoard).intValue());
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
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3PP2/1QN5/1P4PP/3R1RK1 b - -", 12);

        // Strong (E) + Knight on F3
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3PP2/1QN2N2/1P4PP/3R1RK1 b - -", 22);

        // Weak (F)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3P3/1QN2P2/1P4PP/3R1RK1 b - -", 6);

        // Very Weak (G)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3P3/1QN3P1/1P3P1P/3R1RK1 b - -", 0);

        // Very Weak (H)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3P3/1QN2P1P/1P4P1/3R1RK1 b - -", -7);

        // Weak (I)
        assertRightWayScore("r4rk1/1p1n1ppp/p1p1bq2/4p3/P3PP2/1QN3P1/1P5P/3R1RK1 b - -", 0);
    }

}
