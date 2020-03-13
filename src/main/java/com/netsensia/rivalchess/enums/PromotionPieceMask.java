package com.netsensia.rivalchess.enums;

import com.netsensia.rivalchess.model.Piece;

public enum PromotionPieceMask {
    PROMOTION_PIECE_TOSQUARE_MASK_QUEEN (192),
    PROMOTION_PIECE_TOSQUARE_MASK_ROOK  (64),
    PROMOTION_PIECE_TOSQUARE_MASK_BISHOP (128),
    PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT (256),
    PROMOTION_PIECE_TOSQUARE_MASK_FULL (448)
    ;

    private int value;

    private PromotionPieceMask(int value) {
        this.value = value;
    }

    public static PromotionPieceMask fromValue(int fromValue) {
        for (PromotionPieceMask ppm : values()) {
            if (ppm.getValue() == fromValue) {
                return ppm;
            }
        }
        throw new RuntimeException("Invalid fromValue");
    }

    public static PromotionPieceMask fromPiece(Piece piece) {
        switch (piece) {
            case QUEEN:
                return PROMOTION_PIECE_TOSQUARE_MASK_QUEEN;
            case KNIGHT:
                return PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT;
            case BISHOP:
                return PROMOTION_PIECE_TOSQUARE_MASK_BISHOP;
            case ROOK:
                return PROMOTION_PIECE_TOSQUARE_MASK_ROOK;
            default:
                throw new RuntimeException("Invalid piece for promotion mask");
        }
    }

    public int getValue() {
        return value;
    }
}