package com.netsensia.rivalchess.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SquareOccupantTest {

    @Test
    public void getIndex() {
        assertEquals(8, SquareOccupant.BB.getIndex());
    }

    @Test
    public void fromIndex() {
        assertEquals(SquareOccupant.BB, SquareOccupant.fromIndex(8));
    }

    @Test
    public void ofColour() {
        assertEquals(SquareOccupant.WB.getIndex(), SquareOccupant.BB.ofColour(Colour.WHITE));
        assertEquals(SquareOccupant.BB.getIndex(), SquareOccupant.BB.ofColour(Colour.BLACK));
    }
}
