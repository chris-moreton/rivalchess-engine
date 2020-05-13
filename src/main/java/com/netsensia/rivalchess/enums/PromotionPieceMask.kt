package com.netsensia.rivalchess.enums

import com.netsensia.rivalchess.model.Piece

enum class PromotionPieceMask(val value: Int) {
    PROMOTION_PIECE_TOSQUARE_MASK_QUEEN(192), PROMOTION_PIECE_TOSQUARE_MASK_ROOK(64), PROMOTION_PIECE_TOSQUARE_MASK_BISHOP(128), PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT(256), PROMOTION_PIECE_TOSQUARE_MASK_FULL(448);

    companion object {
        @JvmStatic
        fun fromValue(fromValue: Int): PromotionPieceMask {
            for (ppm in values()) {
                if (ppm.value == fromValue) {
                    return ppm
                }
            }
            throw RuntimeException("Invalid fromValue")
        }

        @JvmStatic
        fun fromPiece(piece: Piece?): PromotionPieceMask {
            return when (piece) {
                Piece.QUEEN -> PROMOTION_PIECE_TOSQUARE_MASK_QUEEN
                Piece.KNIGHT -> PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT
                Piece.BISHOP -> PROMOTION_PIECE_TOSQUARE_MASK_BISHOP
                Piece.ROOK -> PROMOTION_PIECE_TOSQUARE_MASK_ROOK
                else -> throw RuntimeException("Invalid piece for promotion mask")
            }
        }
    }

}