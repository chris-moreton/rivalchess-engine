package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.RANK_4
import com.netsensia.rivalchess.bitboards.RANK_5
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.model.Colour

fun EngineBoard.generatePawnMoves(
        pawnBitboard: Long,
        bitboardMaskForwardPawnMoves: List<Long>,
        bitboardMaskCapturePawnMoves: List<Long>,
        moves: MutableList<Int>
) {

    squareList(pawnBitboard).forEach {
        addPawnMoves(it shl 16, pawnForwardAndCaptureMovesBitboard(
                it,
                bitboardMaskCapturePawnMoves,
                pawnForwardMovesBitboard(bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard())
        ), false, moves)
    }
}

fun EngineBoard.pawnForwardMovesBitboard(bitboardPawnMoves: Long) =
        bitboardPawnMoves or (potentialPawnJumpMoves(bitboardPawnMoves) and emptySquaresBitboard())

fun EngineBoard.potentialPawnJumpMoves(bitboardPawnMoves: Long) =
        if (mover == Colour.WHITE) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

fun EngineBoard.pawnForwardAndCaptureMovesBitboard(bitRef: Int, bitboardMaskCapturePawnMoves: List<Long>, bitboardPawnMoves: Long) =
        if (engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) and enPassantCaptureRank(mover) != 0L)
            bitboardPawnMoves or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY) or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENPASSANTSQUARE)
        else bitboardPawnMoves or
                pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY)

fun EngineBoard.pawnCaptures(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int, bitboardType: BitboardType) =
        (bitboardMaskCapturePawnMoves[bitRef] and engineBitboards.getPieceBitboard(bitboardType))

fun EngineBoard.pawnBitboardForMover() =
        if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(BitboardType.WP) else engineBitboards.getPieceBitboard(BitboardType.BP)
