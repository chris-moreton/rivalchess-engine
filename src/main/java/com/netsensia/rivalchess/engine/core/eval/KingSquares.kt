package com.netsensia.rivalchess.engine.core.eval

import java.lang.Long

class KingSquares(private val bitboardData: BitboardData) {
    val white = whiteKingSquare(bitboardData)
    val black = blackKingSquare(bitboardData)
}

fun whiteKingSquare(bitboards: BitboardData) = Long.numberOfTrailingZeros(bitboards.whiteKing)

fun blackKingSquare(bitboards: BitboardData) = Long.numberOfTrailingZeros(bitboards.blackKing)
