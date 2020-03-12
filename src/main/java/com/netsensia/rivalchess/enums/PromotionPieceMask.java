package com.netsensia.rivalchess.enums;

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

    public int getValue() {
        return value;
    }
}