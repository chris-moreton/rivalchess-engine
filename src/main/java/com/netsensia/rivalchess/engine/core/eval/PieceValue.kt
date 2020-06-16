package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.model.Piece

const val VALUE_PAWN = 100
const val VALUE_KNIGHT = 390
const val VALUE_BISHOP = 390
const val VALUE_ROOK = 595
const val VALUE_KING = 30000
const val VALUE_QUEEN = 1175

fun pieceValue(piece: Piece): Int {
    return when (piece) {
        Piece.PAWN -> VALUE_PAWN
        Piece.KNIGHT -> VALUE_KNIGHT
        Piece.BISHOP -> VALUE_BISHOP
        Piece.ROOK -> VALUE_ROOK
        Piece.KING -> VALUE_KING
        Piece.QUEEN -> VALUE_QUEEN
        Piece.NONE -> 0
    }
}

fun pieceValue(bitboardType: Int): Int {
    return when (bitboardType) {
        BITBOARD_WP, BITBOARD_BP -> 100
        BITBOARD_WN, BITBOARD_BN -> 390
        BITBOARD_WB, BITBOARD_BB -> 390
        BITBOARD_WR, BITBOARD_BR -> 595
        BITBOARD_WK, BITBOARD_BK -> 30000
        BITBOARD_WQ, BITBOARD_BQ -> 1175
        else -> 0
    }
}
