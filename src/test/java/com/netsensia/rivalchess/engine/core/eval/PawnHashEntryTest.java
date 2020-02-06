package com.netsensia.rivalchess.engine.core.eval;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PawnHashEntryTest {

    @Test
    public void addBlackPassedPawnScore() {
        PawnHashEntry pawnHashEntry = new PawnHashEntry();
        pawnHashEntry.setBlackPassedPawnScore(10);
        pawnHashEntry.addBlackPassedPawnScore(5);
        assertEquals(15, pawnHashEntry.getBlackPassedPawnScore());
    }

    @Test
    public void addWhitePassedPawnScore() {
        PawnHashEntry pawnHashEntry = new PawnHashEntry();
        pawnHashEntry.setWhitePassedPawnScore(10);
        pawnHashEntry.addWhitePassedPawnScore(5);
        assertEquals(15, pawnHashEntry.getWhitePassedPawnScore());
    }

    @Test
    public void copy() {
        PawnHashEntry pawnHashEntry = new PawnHashEntry();
        pawnHashEntry.setWhitePassedPawnScore(10);
        pawnHashEntry.setBlackPassedPawnScore(5);
        pawnHashEntry.setWhitePassedPawnsBitboard(100);
        pawnHashEntry.setBlackPassedPawnsBitboard(928);

        PawnHashEntry copy = new PawnHashEntry(pawnHashEntry);

        assertEquals(10, copy.getWhitePassedPawnScore());
        assertEquals(5, copy.getBlackPassedPawnScore());
        assertEquals(100, copy.getWhitePassedPawnsBitboard());
        assertEquals(928, copy.getBlackPassedPawnsBitboard());

    }
}
