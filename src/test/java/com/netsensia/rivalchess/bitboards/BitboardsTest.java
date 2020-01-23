package com.netsensia.rivalchess.bitboards;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static java.time.LocalDate.now;
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
    // Starting with the most advanced bit on each file, fill all the bits below it on the same file.
    public void southFill() {
        String bitboardString =
                "01000000" +
                "00000000" +
                "11100100" +
                "00000000" +
                "01010101" +
                "11000011" +
                "00000000" +
                "10101010";

        String expectedString =
                "01000000" +
                "01000000" +
                "11100100" +
                "11100100" +
                "11110101" +
                "11110111" +
                "11110111" +
                "11111111";

        long bitboard = new BigInteger(bitboardString, 2).longValue();
        long expected = new BigInteger(expectedString, 2).longValue();

        assertEquals(expected, Bitboards.southFill(bitboard));
    }

    @Test
    // Starting with the most advanced bit on each file, fill all the bits above it on the same file.
    public void northFill() {

        String bitboardString =
                "01000000" +
                "00000000" +
                "11100100" +
                "00000000" +
                "01010101" +
                "11000011" +
                "00000000" +
                "10101010";

        String expectedString =
                "11111111" +
                "11111111" +
                "11111111" +
                "11111111" +
                "11111111" +
                "11101011" +
                "10101010" +
                "10101010";

        long bitboard = new BigInteger(bitboardString, 2).longValue();
        long expected = new BigInteger(expectedString, 2).longValue();

        assertEquals(expected, Bitboards.northFill(bitboard));
    }

    @Test
    // Starting with the most advanced bit on each file, fill all the bits above it on the same file.
    public void getPawnFiles() {

        String bitboardString =
                "01000000" +
                "00000000" +
                "11100000" +
                "00000000" +
                "01000001" +
                "11000010" +
                "00000000" +
                "10101010";

        String expectedString =
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "11101011";

        long bitboard = new BigInteger(bitboardString, 2).longValue();
        long expected = new BigInteger(expectedString, 2).longValue();

        assertEquals(expected, Bitboards.getPawnFiles(bitboard));
    }
}
