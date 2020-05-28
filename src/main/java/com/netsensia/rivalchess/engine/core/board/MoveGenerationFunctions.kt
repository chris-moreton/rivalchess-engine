package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.RANK_3
import com.netsensia.rivalchess.bitboards.RANK_6
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.Colour

fun enPassantCaptureRank(mover: Colour) = if (mover == Colour.WHITE) RANK_6 else RANK_3

fun addPawnMoves(
        fromSquareMoveMask: Int,
        bitboard: Long,
        queenCapturesOnly: Boolean,
        moves: MutableList<Int>) {

    squareList(bitboard).forEach {
        if (it >= 56 || it <= 7) {
            moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value)
            if (!queenCapturesOnly) {
                moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.value)
                moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.value)
                moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.value)
            }
        } else {
            moves.add(fromSquareMoveMask or it)
        }
    }
}
