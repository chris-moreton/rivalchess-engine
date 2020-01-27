package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class EvaluateTest {

    private void assertRightWayScore(String fen, int expectedScore, Function<EngineChessBoard, Integer> func) throws IllegalFenException {

        EngineChessBoard engineBoard = new EngineChessBoard();

        engineBoard.setBoard(FenUtils.getBoardModel(fen));

        assertEquals(expectedScore, func.apply(engineBoard).intValue());
    }

    @Test
    public void scoreWhiteRightWayPositions() throws IllegalFenException {
        assertRightWayScore("r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN2N2/1P3PPP/3R1RK1 b - -", 30, Evaluate::getWhiteKingRightWayScore);
    }

    @Test
    public void scoreBlackRightWayPositions() throws IllegalFenException {
        assertRightWayScore("r1b2rk1/1p1n1ppp/p1p2q2/4p3/P1B1Pn2/1QN2N2/1P3PPP/3R1RK1 b - -", 30, Evaluate::getBlackKingRightWayScore);
    }
}
