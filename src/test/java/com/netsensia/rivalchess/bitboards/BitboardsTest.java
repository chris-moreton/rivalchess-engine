package com.netsensia.rivalchess.bitboards;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BitboardsTest {

    @Test
    public void getSetBits() {
        assertEquals(
                new ArrayList<>(Arrays.asList(0, 1, 6, 9, 11, 13, 16, 18, 19, 20, 21, 23)),
                Bitboards.getSetBits(12397123L));

        assertEquals(
                new ArrayList<>(Arrays.asList(0, 1, 6, 8, 10, 12, 14, 24, 33, 38, 44, 45, 54, 55, 57, 59, 60, 63)),
                Bitboards.getSetBits(Long.parseUnsignedLong("11150965737412121923")));
    }

    @Test
    public void southFill() {
    }

    @Test
    public void northFill() {
    }
}
