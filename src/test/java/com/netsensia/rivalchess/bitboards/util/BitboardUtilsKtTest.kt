package com.netsensia.rivalchess.bitboards.util

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Ignore

import kotlin.random.Random

internal class BitboardUtilsKtTest {

    @Test
    @ExperimentalStdlibApi
    fun squareListNew() {
        for (i in 1..10000) {
            val l = Random.nextLong()
            assertEquals(squareList(l), squareListNew(l))
        }
    }

    @Test
    @ExperimentalStdlibApi
    @Ignore
    fun speed1() {
        for (i in 1..500000000) {
            var r = 0L
            for (j in 1..Random.nextInt(0,3)) {
                r = r and (1L shl Random.nextInt(0, 63))
            }
            val l = squareList(r)
        }
    }

    @Test
    @ExperimentalStdlibApi
    @Ignore
    fun speed2() {
        for (i in 1..500000000) {
            var r = 0L
            for (j in 1..Random.nextInt(0,3)) {
                r = r and (1L shl Random.nextInt(0, 63))
            }
            val l = squareListNew(r)
        }
    }
}