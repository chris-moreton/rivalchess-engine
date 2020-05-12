package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.util.getBlackPassedPawns
import com.netsensia.rivalchess.bitboards.util.getWhitePassedPawns
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class EvaluateKtTest {

    @Test
    fun getWhitePassedPawns() {
        val whitePawns: Long = BigInteger(
                "00000000" +
                        "00100000" +
                        "00100000" +
                        "00000000" +
                        "01000001" +
                        "11001010" +
                        "00000000" +
                        "00000000", 2).toLong()
        val blackPawns: Long = BigInteger(
                "00000000" +
                        "00000000" +
                        "00000000" +
                        "10000010" +
                        "10100100" +
                        "00000000" +
                        "00000000" +
                        "00000000", 2).toLong()
        val expected: Long = BigInteger(
                "00000000" +
                        "00100000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000", 2).toLong()

        assertEquals(expected, getWhitePassedPawns(whitePawns, blackPawns))
    }

    @Test
    fun testGetBlackPassedPawns() {
        val whitePawns: Long = BigInteger(
                "00000000" +
                        "00100000" +
                        "00100000" +
                        "00000000" +
                        "01000001" +
                        "11001010" +
                        "00000000" +
                        "00000000", 2).toLong()
        val blackPawns: Long = BigInteger(
                "00000000" +
                        "00000000" +
                        "00000000" +
                        "10000010" +
                        "10100100" +
                        "00100100" +
                        "00000000" +
                        "00000000", 2).toLong()
        val expected: Long = BigInteger(
                "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00100100" +
                        "00000000" +
                        "00000000", 2).toLong()

        assertEquals(expected, getBlackPassedPawns(whitePawns, blackPawns))
    }

}