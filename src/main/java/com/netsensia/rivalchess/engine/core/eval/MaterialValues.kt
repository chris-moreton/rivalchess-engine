package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.model.Piece
import java.lang.Long

data class MaterialValues(val bitboardData: BitboardData) {
    val whitePieces = whitePieceValues(bitboardData)
    val blackPieces = blackPieceValues(bitboardData)
    val whitePawns = whitePawnValues(bitboardData)
    val blackPawns = blackPawnValues(bitboardData)
}

fun whitePieceValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.whiteKnights) * PieceValue.getValue(Piece.KNIGHT) +
                Long.bitCount(bitboards.whiteRooks) * PieceValue.getValue(Piece.ROOK) +
                Long.bitCount(bitboards.whiteBishops) * PieceValue.getValue(Piece.BISHOP) +
                Long.bitCount(bitboards.whiteQueens) * PieceValue.getValue(Piece.QUEEN)

fun blackPieceValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.blackKnights) * PieceValue.getValue(Piece.KNIGHT) +
                Long.bitCount(bitboards.blackRooks) * PieceValue.getValue(Piece.ROOK) +
                Long.bitCount(bitboards.blackBishops) * PieceValue.getValue(Piece.BISHOP) +
                Long.bitCount(bitboards.blackQueens) * PieceValue.getValue(Piece.QUEEN)

fun whitePawnValues(bitboards: BitboardData): Int {
    return Long.bitCount(bitboards.whitePawns) * PieceValue.getValue(Piece.PAWN)
}

fun blackPawnValues(bitboards: BitboardData): Int {
    return Long.bitCount(bitboards.blackPawns) * PieceValue.getValue(Piece.PAWN)
}