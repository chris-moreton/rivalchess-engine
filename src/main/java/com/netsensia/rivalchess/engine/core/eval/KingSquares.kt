package com.netsensia.rivalchess.engine.core.eval

class KingSquares(private val bitboardData: BitboardData) {
    val white by lazy { whiteKingSquare(bitboardData) }
    val black by lazy { blackKingSquare(bitboardData) }
}