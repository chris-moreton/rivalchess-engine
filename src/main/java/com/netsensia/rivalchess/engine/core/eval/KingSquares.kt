package com.netsensia.rivalchess.engine.core.eval

import java.lang.Long.numberOfTrailingZeros

class KingSquares(bitboardData: BitboardData) {
    val white = whiteKingSquare(bitboardData)
    val black = blackKingSquare(bitboardData)
}

fun whiteKingSquare(bitboards: BitboardData) = numberOfTrailingZeros(bitboards.whiteKing)

fun blackKingSquare(bitboards: BitboardData) = numberOfTrailingZeros(bitboards.blackKing)
