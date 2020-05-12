package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.engine.core.eval.onlyOneBitSet

fun EngineBoard.onlyKingsRemain(): Boolean {
    return onlyOneBitSet(engineBitboards.getPieceBitboard(BitboardType.ENEMY)) &&
            onlyOneBitSet(engineBitboards.getPieceBitboard(BitboardType.FRIENDLY))
}