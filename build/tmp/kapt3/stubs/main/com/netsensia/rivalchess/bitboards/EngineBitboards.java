package com.netsensia.rivalchess.bitboards;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0010\u0016\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0000\u00a2\u0006\u0002\u0010\u0003B\u0005\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0006\u0010\u000b\u001a\u00020\bJ\u000e\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000fJ\u0010\u0010\u0010\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0006\u0010\u0011\u001a\u00020\bJ\u0016\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\nJ\u0016\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u000fJ\u0016\u0010\u001a\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\bJ\u0006\u0010\u001c\u001a\u00020\u0017J\u0016\u0010\u001d\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000f2\u0006\u0010\u001e\u001a\u00020\bJ\u0016\u0010\u001f\u001a\u00020\u00172\u0006\u0010 \u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\bR\u0012\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/netsensia/rivalchess/bitboards/EngineBitboards;", "", "thoseBitboards", "(Lcom/netsensia/rivalchess/bitboards/EngineBitboards;)V", "()V", "pieceBitboards", "", "getBishopMovePiecesBitboard", "", "colour", "Lcom/netsensia/rivalchess/model/Colour;", "getBlackPieces", "getPieceBitboard", "type", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "", "getRookMovePiecesBitboard", "getWhitePieces", "isSquareAttackedBy", "", "attackedSquare", "attacker", "movePiece", "", "piece", "compactMove", "orPieceBitboard", "xorBy", "reset", "setPieceBitboard", "bitboard", "xorPieceBitboard", "i", "rivalchess-engine"})
public final class EngineBitboards {
    @org.jetbrains.annotations.NotNull()
    public long[] pieceBitboards;
    
    public final void reset() {
    }
    
    public final void xorPieceBitboard(int i, long xorBy) {
    }
    
    public final void orPieceBitboard(int type, long xorBy) {
    }
    
    public final void setPieceBitboard(int type, long bitboard) {
    }
    
    public final long getPieceBitboard(int type) {
        return 0L;
    }
    
    public final long getWhitePieces() {
        return 0L;
    }
    
    public final long getBlackPieces() {
        return 0L;
    }
    
    public final long getPieceBitboard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.SquareOccupant type) {
        return 0L;
    }
    
    public final void movePiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.SquareOccupant piece, int compactMove) {
    }
    
    private final long getRookMovePiecesBitboard(com.netsensia.rivalchess.model.Colour colour) {
        return 0L;
    }
    
    private final long getBishopMovePiecesBitboard(com.netsensia.rivalchess.model.Colour colour) {
        return 0L;
    }
    
    public final boolean isSquareAttackedBy(int attackedSquare, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour attacker) {
        return false;
    }
    
    public EngineBitboards() {
        super();
    }
    
    public EngineBitboards(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.bitboards.EngineBitboards thoseBitboards) {
        super();
    }
}