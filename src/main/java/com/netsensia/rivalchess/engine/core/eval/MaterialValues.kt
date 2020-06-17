package com.netsensia.rivalchess.engine.core.eval

import java.lang.Long

data class MaterialValues(val bitboardData: BitboardData) {
        @JvmField
        val whitePieces = whitePieceValues(bitboardData)
        @JvmField
        val blackPieces = blackPieceValues(bitboardData)
        @JvmField
        val whitePawns = whitePawnValues(bitboardData)
        @JvmField
        val blackPawns = blackPawnValues(bitboardData)
}

fun whitePieceValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.whiteKnights) * VALUE_KNIGHT +
        Long.bitCount(bitboards.whiteRooks) * VALUE_ROOK +
        Long.bitCount(bitboards.whiteBishops) * VALUE_BISHOP +
        Long.bitCount(bitboards.whiteQueens) * VALUE_QUEEN

fun blackPieceValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.blackKnights) * VALUE_KNIGHT +
        Long.bitCount(bitboards.blackRooks) * VALUE_ROOK +
        Long.bitCount(bitboards.blackBishops) * VALUE_BISHOP +
        Long.bitCount(bitboards.blackQueens) * VALUE_QUEEN

fun whitePawnValues(bitboards: BitboardData) = Long.bitCount(bitboards.whitePawns) * VALUE_PAWN

fun blackPawnValues(bitboards: BitboardData) = Long.bitCount(bitboards.blackPawns) * VALUE_PAWN
