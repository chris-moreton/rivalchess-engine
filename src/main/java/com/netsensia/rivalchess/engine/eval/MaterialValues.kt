package com.netsensia.rivalchess.engine.eval

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
        java.lang.Long.bitCount(bitboards.whiteKnights) * VALUE_KNIGHT +
        java.lang.Long.bitCount(bitboards.whiteRooks) * VALUE_ROOK +
        java.lang.Long.bitCount(bitboards.whiteBishops) * VALUE_BISHOP +
        java.lang.Long.bitCount(bitboards.whiteQueens) * VALUE_QUEEN

fun blackPieceValues(bitboards: BitboardData) =
        java.lang.Long.bitCount(bitboards.blackKnights) * VALUE_KNIGHT +
        java.lang.Long.bitCount(bitboards.blackRooks) * VALUE_ROOK +
        java.lang.Long.bitCount(bitboards.blackBishops) * VALUE_BISHOP +
        java.lang.Long.bitCount(bitboards.blackQueens) * VALUE_QUEEN

fun whitePawnValues(bitboards: BitboardData) = java.lang.Long.bitCount(bitboards.whitePawns) * VALUE_PAWN

fun blackPawnValues(bitboards: BitboardData) = java.lang.Long.bitCount(bitboards.blackPawns) * VALUE_PAWN
