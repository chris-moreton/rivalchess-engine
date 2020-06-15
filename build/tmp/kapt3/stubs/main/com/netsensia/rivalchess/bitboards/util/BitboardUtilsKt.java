package com.netsensia.rivalchess.bitboards.util;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\u001a%\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u0005H\u0086\b\u001a\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u0003\u001a\u0016\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0003\u001a\u0016\u0010\r\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0003\u001a\u000e\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0003\u001a\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00030\u00112\u0006\u0010\u0012\u001a\u00020\u0013\u001a\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\u0003\u001a\u001e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0003\u001a\u001e\u0010\u0018\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0003\u001a\u001b\u0010\u0019\u001a\u00020\u00032\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u0006H\u0086\u0010\u001a\u001b\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u0006H\u0086\u0010\u001a\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00060\u00112\u0006\u0010\u0002\u001a\u00020\u0003\u00a8\u0006\u001d"}, d2 = {"applyToSquares", "", "bitboard", "", "fn", "Lkotlin/Function1;", "", "getBlackPassedPawns", "whitePawns", "blackPawns", "getMagicIndexForBishop", "pieceSquare", "allPieceBitboard", "getMagicIndexForRook", "getPawnFiles", "pawns", "getPawnMovesCaptureOfColour", "", "colour", "Lcom/netsensia/rivalchess/model/Colour;", "getWhitePassedPawns", "isBishopAttackingSquare", "", "attackedSquare", "isRookAttackingSquare", "northFill", "shiftBy", "southFill", "squareList", "rivalchess-engine"})
public final class BitboardUtilsKt {
    
    public static final long southFill(long bitboard, int shiftBy) {
        return 0L;
    }
    
    public static final long northFill(long bitboard, int shiftBy) {
        return 0L;
    }
    
    public static final long getBlackPassedPawns(long whitePawns, long blackPawns) {
        return 0L;
    }
    
    public static final long getPawnFiles(long pawns) {
        return 0L;
    }
    
    public static final long getWhitePassedPawns(long whitePawns, long blackPawns) {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.util.List<java.lang.Long> getPawnMovesCaptureOfColour(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour colour) {
        return null;
    }
    
    public static final boolean isBishopAttackingSquare(int attackedSquare, int pieceSquare, long allPieceBitboard) {
        return false;
    }
    
    public static final int getMagicIndexForBishop(int pieceSquare, long allPieceBitboard) {
        return 0;
    }
    
    public static final boolean isRookAttackingSquare(int attackedSquare, int pieceSquare, long allPieceBitboard) {
        return false;
    }
    
    public static final int getMagicIndexForRook(int pieceSquare, long allPieceBitboard) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.util.List<java.lang.Integer> squareList(long bitboard) {
        return null;
    }
    
    public static final void applyToSquares(long bitboard, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> fn) {
    }
}