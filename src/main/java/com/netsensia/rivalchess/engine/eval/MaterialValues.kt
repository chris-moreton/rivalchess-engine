package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.util.popCount

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
        popCount(bitboards.whiteKnights) * VALUE_KNIGHT +
        popCount(bitboards.whiteRooks) * VALUE_ROOK +
        popCount(bitboards.whiteBishops) * VALUE_BISHOP +
        popCount(bitboards.whiteQueens) * VALUE_QUEEN

fun blackPieceValues(bitboards: BitboardData) =
        popCount(bitboards.blackKnights) * VALUE_KNIGHT +
        popCount(bitboards.blackRooks) * VALUE_ROOK +
        popCount(bitboards.blackBishops) * VALUE_BISHOP +
        popCount(bitboards.blackQueens) * VALUE_QUEEN

fun whitePawnValues(bitboards: BitboardData) = popCount(bitboards.whitePawns) * VALUE_PAWN

fun blackPawnValues(bitboards: BitboardData) = popCount(bitboards.blackPawns) * VALUE_PAWN
