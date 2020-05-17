package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.EngineBitboards
import com.netsensia.rivalchess.engine.core.eval.onlyOneBitSet
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.onlyKingsRemain() =
    onlyOneBitSet(EngineBitboards.getInstance().getPieceBitboard(BitboardType.ENEMY)) &&
            onlyOneBitSet(EngineBitboards.getInstance().getPieceBitboard(BitboardType.FRIENDLY))

fun EngineBoard.isSquareEmpty(bitRef: Int) = squareContents.get(bitRef) == SquareOccupant.NONE

fun EngineBoard.isCapture(move: Int): Boolean {
    val toSquare = move and 63
    var isCapture: Boolean = !isSquareEmpty(toSquare)
    if (!isCapture && 1L shl toSquare and EngineBitboards.getInstance().getPieceBitboard(BitboardType.ENPASSANTSQUARE) != 0L &&
            squareContents.get(move ushr 16 and 63).piece == Piece.PAWN) {
        isCapture = true
    }
    return isCapture
}

fun inCheck(whiteKingSquare: Int, blackKingSquare: Int, mover: Colour) =
    if (mover == Colour.BLACK)
        EngineBitboards.getInstance().isSquareAttackedBy(blackKingSquare.toInt(), Colour.WHITE)
    else EngineBitboards.getInstance().isSquareAttackedBy(whiteKingSquare.toInt(), Colour.BLACK)

fun EngineBoard.getPiece(bitRef: Int) = when (squareContents.get(bitRef)) {
        SquareOccupant.WP, SquareOccupant.BP -> Piece.PAWN
        SquareOccupant.WB, SquareOccupant.BB -> Piece.BISHOP
        SquareOccupant.WN, SquareOccupant.BN -> Piece.KNIGHT
        SquareOccupant.WR, SquareOccupant.BR -> Piece.ROOK
        SquareOccupant.WQ, SquareOccupant.BQ -> Piece.QUEEN
        SquareOccupant.WK, SquareOccupant.BK -> Piece.KING
        else -> Piece.NONE
    }
