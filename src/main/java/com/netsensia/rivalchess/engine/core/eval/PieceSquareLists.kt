package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.util.unsetBit

class PieceSquareLists(private val bitboardData: BitboardData) {
    val whitePawns = squareList(bitboardData.whitePawns)
    val whiteRooks = squareList(bitboardData.whiteRooks)
    val whiteBishops = squareList(bitboardData.whiteBishops)
    val whiteKnights = squareList(bitboardData.whiteKnights)
    val whiteQueens = squareList(bitboardData.whiteQueens)
    val blackPawns = squareList(bitboardData.blackPawns)
    val blackRooks = squareList(bitboardData.blackRooks)
    val blackBishops = squareList(bitboardData.blackBishops)
    val blackKnights = squareList(bitboardData.blackKnights)
    val blackQueens = squareList(bitboardData.blackQueens)
}

tailrec fun squareList(bitboard: Long, squareList: List<Int> = emptyList()) : List<Int> =
        when (bitboard) {
            0L -> squareList
            else -> {
                val square = java.lang.Long.numberOfTrailingZeros(bitboard)
                squareList(unsetBit(bitboard, square), squareList + square)
            }
        }