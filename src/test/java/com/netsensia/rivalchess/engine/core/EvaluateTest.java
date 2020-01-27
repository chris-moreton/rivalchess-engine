package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EvaluateTest {

    private void assertRightWayScore(String fen, int expectedScore) throws IllegalFenException {

        final int h1 = 0, h2 = 8, h3 = 16, g2 = 9, g3 = 17, f1 = 2, f2 = 10, f3 = 18, f4 = 26;

        EngineChessBoard engineBoard = new EngineChessBoard();

        engineBoard.setBoard(FenUtils.getBoardModel(fen));

        assertEquals(expectedScore, Evaluate.scoreRightWayPositions(engineBoard, h1, h2, h3, g2, g3, f1, f2, f3, f4, 0, RivalConstants.WHITE));
    }

    @Test
    public void scoreRightWayPositions() throws IllegalFenException {
        assertRightWayScore("r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN2N2/1P3PPP/3R1RK1 b - -", 30);
    }
}
