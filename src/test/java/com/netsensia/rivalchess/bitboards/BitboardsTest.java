package com.netsensia.rivalchess.bitboards;

import com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt;
import com.netsensia.rivalchess.engine.eval.PawnEvalKt;
import com.netsensia.rivalchess.model.Colour;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static com.netsensia.rivalchess.engine.eval.AttacksKt.blackPawnAttacks;
import static com.netsensia.rivalchess.engine.eval.AttacksKt.whitePawnAttacks;
import static org.junit.Assert.assertEquals;

public class BitboardsTest {

    @Test
    public void getSetBits() {
        assertEquals(
                new ArrayList<>(Arrays.asList(0, 1, 6, 9, 11, 13, 16, 18, 19, 20, 21, 23)),
                BitboardUtilsKt.squareList(12397123L));

        assertEquals(
                new ArrayList<>(Arrays.asList(0, 1, 6, 8, 10, 12, 14, 24, 33, 38, 44, 45, 54, 55, 57, 59, 60, 63)),
                BitboardUtilsKt.squareList(Long.parseUnsignedLong("11150965737412121923")));
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

        assertEquals(expected, BitboardUtilsKt.southFill(bitboard));
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

        assertEquals(expected, BitboardUtilsKt.northFill(bitboard));

    }

    @Test
    // Starting with the most advanced bit on each file, fill all the bits above it on the same file.
    public void getPawnFiles() {

        final long bitboard = new BigInteger(
                "01000000" +
                "00000000" +
                "11100000" +
                "00000000" +
                "01000001" +
                "11000010" +
                "00000000" +
                "10101010", 2).longValue();

        final long expected = new BigInteger(
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "11101011", 2).longValue();

        assertEquals(expected, PawnEvalKt.getPawnFiles(bitboard));
    }

    @Test
    public void getBlackPawnAttacks() {
        final long bitboard = new BigInteger(
                "00000000" +
                "00000000" +
                "11100000" +
                "00000000" +
                "01000001" +
                "11000010" +
                "00000000" +
                "00000000", 2).longValue();

        final long expected = new BigInteger(
                "00000000" +
                "00000000" +
                "00000000" +
                "11110000" +
                "00000000" +
                "10100010" +
                "11100101" +
                "00000000", 2).longValue();

        assertEquals(expected, blackPawnAttacks(bitboard));
    }

    @Test
    public void getWhitePawnAttacks() {
        final long bitboard = new BigInteger(
                "00000000" +
                "00000000" +
                "11100000" +
                "00000000" +
                "01000001" +
                "11000010" +
                "00000000" +
                "00000000", 2).longValue();

        final long expected = new BigInteger(
                 "00000000" +
                    "11110000" +
                    "00000000" +
                    "10100010" +
                    "11100101" +
                    "00000000" +
                    "00000000" +
                    "00000000", 2).longValue();

        assertEquals(expected, whitePawnAttacks(bitboard));
    }

    @Test
    public void getWhitePassedPawns() {
        final long whitePawns = new BigInteger(
                     "00000000" +
                        "00100000" +
                        "00100000" +
                        "00000000" +
                        "01000001" +
                        "11001010" +
                        "00000000" +
                        "00000000", 2).longValue();

        final long blackPawns = new BigInteger(
                     "00000000" +
                        "00000000" +
                        "00000000" +
                        "10000010" +
                        "10100100" +
                        "00000000" +
                        "00000000" +
                        "00000000", 2).longValue();

        final long expected = new BigInteger(
                     "00000000" +
                        "00100000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000", 2).longValue();

        assertEquals(expected, PawnEvalKt.getWhitePassedPawns(whitePawns, blackPawns));
    }

    @Test
    public void getBlackPassedPawns() {
        final long whitePawns = new BigInteger(
                     "00000000" +
                        "00100000" +
                        "00100000" +
                        "00000000" +
                        "01000001" +
                        "11001010" +
                        "00000000" +
                        "00000000", 2).longValue();

        final long blackPawns = new BigInteger(
                     "00000000" +
                        "00000000" +
                        "00000000" +
                        "10000010" +
                        "10100100" +
                        "00100100" +
                        "00000000" +
                        "00000000", 2).longValue();

        final long expected = new BigInteger(
                     "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00100100" +
                        "00000000" +
                        "00000000", 2).longValue();

        assertEquals(expected, PawnEvalKt.getBlackPassedPawns(whitePawns, blackPawns));
    }

    @Test
    public void getPawnMovesOfCaptureColour() {
        assertEquals(BitboardConstantsKt.getWhitePawnMovesCapture(), BitboardUtilsKt.getPawnMovesCaptureOfColour(Colour.WHITE));
        assertEquals(BitboardConstantsKt.getBlackPawnMovesCapture(), BitboardUtilsKt.getPawnMovesCaptureOfColour(Colour.BLACK));
    }
}
