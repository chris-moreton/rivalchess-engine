package com.netsensia.rivalchess.engine.eval

import java.lang.Long.numberOfTrailingZeros

class KingSquares(bitboardData: BitboardData) {
    @JvmField
    val white = numberOfTrailingZeros(bitboardData.whiteKing)
    @JvmField
    val black = numberOfTrailingZeros(bitboardData.blackKing)
}
