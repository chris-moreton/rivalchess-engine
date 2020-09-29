package com.netsensia.rivalchess.bitboards

import com.netsensia.rivalchess.bitboards.MagicBitboards.magicMovesBishop
import com.netsensia.rivalchess.bitboards.MagicBitboards.magicMovesRook
import org.junit.Assert
import org.junit.Test

class MagicBitboardsTest {

    @Test
    fun magicMovesRookSampleTest() {
        Assert.assertEquals(16544, magicMovesRook[6][2])
    }

    @Test
    fun magicMovesBishopSampleTest() {
        Assert.assertEquals(1089536, magicMovesBishop[6][2])
    }
}