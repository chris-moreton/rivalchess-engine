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
        Long.bitCount(bitboards.whiteKnights) * pieceValue(Piece.KNIGHT) +
        Long.bitCount(bitboards.whiteRooks) * pieceValue(Piece.ROOK) +
        Long.bitCount(bitboards.whiteBishops) * pieceValue(Piece.BISHOP) +
        Long.bitCount(bitboards.whiteQueens) * pieceValue(Piece.QUEEN)

fun blackPieceValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.blackKnights) * pieceValue(Piece.KNIGHT) +
        Long.bitCount(bitboards.blackRooks) * pieceValue(Piece.ROOK) +
        Long.bitCount(bitboards.blackBishops) * pieceValue(Piece.BISHOP) +
        Long.bitCount(bitboards.blackQueens) * pieceValue(Piece.QUEEN)

fun whitePawnValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.whitePawns) * pieceValue(Piece.PAWN)

fun blackPawnValues(bitboards: BitboardData) =
        Long.bitCount(bitboards.blackPawns) * pieceValue(Piece.PAWN)
