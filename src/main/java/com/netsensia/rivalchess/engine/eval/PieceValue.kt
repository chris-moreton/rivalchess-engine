package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*

private const val INDEX_PAWN = 0
private const val INDEX_KNIGHT = 1
private const val INDEX_BISHOP = 2
private const val INDEX_ROOK = 3
private const val INDEX_QUEEN = 4
private const val INDEX_KING = 5

const val DEFAULT_VALUE_PAWN = VALUE_PAWN
const val DEFAULT_VALUE_KNIGHT = VALUE_KNIGHT
const val DEFAULT_VALUE_BISHOP = VALUE_BISHOP
const val DEFAULT_VALUE_ROOK = VALUE_ROOK
const val DEFAULT_VALUE_QUEEN = VALUE_QUEEN
const val DEFAULT_VALUE_KING = 30000

var pieceValues = intArrayOf(
        DEFAULT_VALUE_PAWN,
        DEFAULT_VALUE_KNIGHT,
        DEFAULT_VALUE_BISHOP,
        DEFAULT_VALUE_ROOK,
        DEFAULT_VALUE_QUEEN,
        DEFAULT_VALUE_KING)

const val ALLOW_PIECE_VALUE_MODIFICATIONS = false

fun pieceValue(bitboardType: Int): Int {
    return if (ALLOW_PIECE_VALUE_MODIFICATIONS) when (bitboardType) {
        BITBOARD_WP, BITBOARD_BP -> pieceValues[INDEX_PAWN]
        BITBOARD_WN, BITBOARD_BN -> pieceValues[INDEX_KNIGHT]
        BITBOARD_WB, BITBOARD_BB -> pieceValues[INDEX_BISHOP]
        BITBOARD_WR, BITBOARD_BR -> pieceValues[INDEX_ROOK]
        BITBOARD_WQ, BITBOARD_BQ -> pieceValues[INDEX_QUEEN]
        BITBOARD_WK, BITBOARD_BK -> pieceValues[INDEX_KING]
        else -> 0
    } else when (bitboardType) {
        BITBOARD_WP, BITBOARD_BP -> DEFAULT_VALUE_PAWN
        BITBOARD_WN, BITBOARD_BN -> DEFAULT_VALUE_KNIGHT
        BITBOARD_WB, BITBOARD_BB -> DEFAULT_VALUE_BISHOP
        BITBOARD_WR, BITBOARD_BR -> DEFAULT_VALUE_ROOK
        BITBOARD_WQ, BITBOARD_BQ -> DEFAULT_VALUE_QUEEN
        BITBOARD_WK, BITBOARD_BK -> DEFAULT_VALUE_KING
        else -> 0
    }
}
