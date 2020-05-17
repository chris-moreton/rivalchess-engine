package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.EngineBitboards
import com.netsensia.rivalchess.engine.core.eval.onlyOneBitSet
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.onlyKingsRemain() =
    onlyOneBitSet(engineBitboards.getPieceBitboard(BitboardType.ENEMY)) &&
            onlyOneBitSet(engineBitboards.getPieceBitboard(BitboardType.FRIENDLY))

fun EngineBoard.isSquareEmpty(bitRef: Int) = squareContents.get(bitRef) == SquareOccupant.NONE

fun EngineBoard.isCapture(move: Int): Boolean {
    val toSquare = move and 63
    var isCapture: Boolean = !isSquareEmpty(toSquare)
    if (!isCapture && 1L shl toSquare and engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) != 0L &&
            squareContents.get(move ushr 16 and 63).piece == Piece.PAWN) {
        isCapture = true
    }
    return isCapture
}

fun EngineBoard.isNonMoverInCheck(whiteKingSquare: Int, blackKingSquare: Int, mover: Colour) =
    if (mover == Colour.WHITE)
        EngineBitboards.getInstance().isSquareAttackedBy(blackKingSquare.toInt(), Colour.WHITE)
    else EngineBitboards.getInstance().isSquareAttackedBy(whiteKingSquare.toInt(), Colour.BLACK)
