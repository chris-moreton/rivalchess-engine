package com.netsensia.rivalchess.enums;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\n\b\u0086\u0001\u0018\u0000 \f2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\fB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000b\u00a8\u0006\r"}, d2 = {"Lcom/netsensia/rivalchess/enums/PromotionPieceMask;", "", "value", "", "(Ljava/lang/String;II)V", "getValue", "()I", "PROMOTION_PIECE_TOSQUARE_MASK_QUEEN", "PROMOTION_PIECE_TOSQUARE_MASK_ROOK", "PROMOTION_PIECE_TOSQUARE_MASK_BISHOP", "PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT", "PROMOTION_PIECE_TOSQUARE_MASK_FULL", "Companion", "rivalchess-engine"})
public enum PromotionPieceMask {
    /*public static final*/ PROMOTION_PIECE_TOSQUARE_MASK_QUEEN /* = new PROMOTION_PIECE_TOSQUARE_MASK_QUEEN(0) */,
    /*public static final*/ PROMOTION_PIECE_TOSQUARE_MASK_ROOK /* = new PROMOTION_PIECE_TOSQUARE_MASK_ROOK(0) */,
    /*public static final*/ PROMOTION_PIECE_TOSQUARE_MASK_BISHOP /* = new PROMOTION_PIECE_TOSQUARE_MASK_BISHOP(0) */,
    /*public static final*/ PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT /* = new PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT(0) */,
    /*public static final*/ PROMOTION_PIECE_TOSQUARE_MASK_FULL /* = new PROMOTION_PIECE_TOSQUARE_MASK_FULL(0) */;
    private final int value = 0;
    public static final com.netsensia.rivalchess.enums.PromotionPieceMask.Companion Companion = null;
    
    public final int getValue() {
        return 0;
    }
    
    PromotionPieceMask(int value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.enums.PromotionPieceMask fromValue(int fromValue) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.enums.PromotionPieceMask fromPiece(@org.jetbrains.annotations.Nullable()
    com.netsensia.rivalchess.model.Piece piece) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bH\u0007\u00a8\u0006\t"}, d2 = {"Lcom/netsensia/rivalchess/enums/PromotionPieceMask$Companion;", "", "()V", "fromPiece", "Lcom/netsensia/rivalchess/enums/PromotionPieceMask;", "piece", "Lcom/netsensia/rivalchess/model/Piece;", "fromValue", "", "rivalchess-engine"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.enums.PromotionPieceMask fromValue(int fromValue) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.enums.PromotionPieceMask fromPiece(@org.jetbrains.annotations.Nullable()
        com.netsensia.rivalchess.model.Piece piece) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}