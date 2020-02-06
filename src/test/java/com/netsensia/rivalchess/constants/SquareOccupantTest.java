package com.netsensia.rivalchess.constants;

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
}
