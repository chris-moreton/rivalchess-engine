package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.model.Piece

fun pieceValue(piece: Piece): Int {
    return when (piece) {
        Piece.PAWN -> 100
        Piece.KNIGHT -> 390
        Piece.BISHOP -> 390
        Piece.ROOK -> 595
        Piece.KING -> 30000
        Piece.QUEEN -> 1175
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
