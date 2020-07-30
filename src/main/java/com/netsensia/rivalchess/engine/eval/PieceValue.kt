package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.consts.*

private const val INDEX_PAWN = 0
private const val INDEX_KNIGHT = 1
private const val INDEX_BISHOP = 2
private const val INDEX_ROOK = 3
private const val INDEX_QUEEN = 4
private const val INDEX_KING = 5

var pieceValues = intArrayOf(100, 390, 400, 595, 1175, 30000)

fun pieceValue(bitboardType: Int): Int {
    return when (bitboardType) {
        BITBOARD_WP, BITBOARD_BP -> pieceValues[INDEX_PAWN]
        BITBOARD_WN, BITBOARD_BN -> pieceValues[INDEX_KNIGHT]
        BITBOARD_WB, BITBOARD_BB -> pieceValues[INDEX_BISHOP]
        BITBOARD_WR, BITBOARD_BR -> pieceValues[INDEX_ROOK]
        BITBOARD_WQ, BITBOARD_BQ -> pieceValues[INDEX_QUEEN]
        BITBOARD_WK, BITBOARD_BK -> pieceValues[INDEX_KING]
        else -> 0
    }
}
