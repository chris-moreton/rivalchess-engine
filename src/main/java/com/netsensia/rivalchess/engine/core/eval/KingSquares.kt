package com.netsensia.rivalchess.engine.core.eval

import java.lang.Long.numberOfTrailingZeros

class KingSquares(bitboardData: BitboardData) {
    val white = numberOfTrailingZeros(bitboardData.whiteKing)
    val black = numberOfTrailingZeros(bitboardData.blackKing)
}
