package com.netsensia.rivalchess.consts

import com.netsensia.rivalchess.model.Piece

const val PROMOTION_PIECE_TOSQUARE_MASK_QUEEN = 192
const val PROMOTION_PIECE_TOSQUARE_MASK_ROOK = 64
const val PROMOTION_PIECE_TOSQUARE_MASK_BISHOP = 128
const val PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT = 256
const val PROMOTION_PIECE_TOSQUARE_MASK_FULL = 448

fun promotionMask(piece: Piece): Int {
    return when (piece) {
        Piece.QUEEN -> PROMOTION_PIECE_TOSQUARE_MASK_QUEEN
        Piece.KNIGHT -> PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT
        Piece.BISHOP -> PROMOTION_PIECE_TOSQUARE_MASK_BISHOP
        Piece.ROOK -> PROMOTION_PIECE_TOSQUARE_MASK_ROOK
        else -> throw RuntimeException("Invalid piece for promotion mask")
    }
}
