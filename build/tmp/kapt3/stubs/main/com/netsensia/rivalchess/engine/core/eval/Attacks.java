package com.netsensia.rivalchess.engine.core.eval;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\t\n\u0002\b\u0016\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R#\u0010\u0005\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR#\u0010\u000b\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\nR\u0011\u0010\r\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR#\u0010\u0010\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\nR#\u0010\u0012\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\nR#\u0010\u0014\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\nR#\u0010\u0016\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\nR\u0011\u0010\u0018\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u000fR#\u0010\u001a\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\nR#\u0010\u001c\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\n\u00a8\u0006\u001e"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/eval/Attacks;", "", "bitboardData", "Lcom/netsensia/rivalchess/engine/core/eval/BitboardData;", "(Lcom/netsensia/rivalchess/engine/core/eval/BitboardData;)V", "blackBishopPair", "Lkotlin/Pair;", "", "", "getBlackBishopPair", "()Lkotlin/Pair;", "blackKnightPair", "getBlackKnightPair", "blackPawns", "getBlackPawns", "()J", "blackQueenPair", "getBlackQueenPair", "blackRookPair", "getBlackRookPair", "whiteBishopPair", "getWhiteBishopPair", "whiteKnightPair", "getWhiteKnightPair", "whitePawns", "getWhitePawns", "whiteQueenPair", "getWhiteQueenPair", "whiteRookPair", "getWhiteRookPair", "rivalchess-engine"})
public final class Attacks {
    private final long whitePawns = 0L;
    private final long blackPawns = 0L;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> whiteRookPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> whiteBishopPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> whiteQueenPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> whiteKnightPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> blackRookPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> blackBishopPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> blackQueenPair = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> blackKnightPair = null;
    
    public final long getWhitePawns() {
        return 0L;
    }
    
    public final long getBlackPawns() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getWhiteRookPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getWhiteBishopPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getWhiteQueenPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getWhiteKnightPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getBlackRookPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getBlackBishopPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getBlackQueenPair() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> getBlackKnightPair() {
        return null;
    }
    
    public Attacks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboardData) {
        super();
    }
}