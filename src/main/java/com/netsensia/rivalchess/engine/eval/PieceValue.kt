package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.consts.*

const val VALUE_PAWN = 100
const val VALUE_KNIGHT = 550
const val VALUE_BISHOP = 600
const val VALUE_ROOK = 1000
const val VALUE_KING = 30000
const val VALUE_QUEEN = 2000

fun pieceValue(bitboardType: Int): Int {
    return when (bitboardType) {
        BITBOARD_WP, BITBOARD_BP -> VALUE_PAWN
        BITBOARD_WN, BITBOARD_BN -> VALUE_KNIGHT
        BITBOARD_WB, BITBOARD_BB -> VALUE_BISHOP
        BITBOARD_WR, BITBOARD_BR -> VALUE_ROOK
        BITBOARD_WK, BITBOARD_BK -> VALUE_KING
        BITBOARD_WQ, BITBOARD_BQ -> VALUE_QUEEN
        else -> 0
    }
}
