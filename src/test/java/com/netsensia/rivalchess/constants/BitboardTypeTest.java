package com.netsensia.rivalchess.constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitboardTypeTest {

    @Test
    public void getIndex() {
        assertEquals(8, BitboardType.BB.getIndex());
    }

    @Test
    public void fromIndex() {
        assertEquals(BitboardType.BB, BitboardType.fromIndex(8));
    }
}
