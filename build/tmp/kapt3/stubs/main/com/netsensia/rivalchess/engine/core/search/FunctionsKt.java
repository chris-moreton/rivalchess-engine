package com.netsensia.rivalchess.engine.core.search;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0003\u001a\"\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u001a\u000e\u0010\b\u001a\u00020\u00062\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006\u001a\u000e\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0006\u001a\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u000e2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\u000f\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006\u001a\u000e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0006\u001a\u001e\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0006\u001a\u0016\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00062\u0006\u0010\u0019\u001a\u00020\u0006\u00a8\u0006\u001a"}, d2 = {"applyToMoves", "", "moves", "", "fn", "Lkotlin/Function1;", "", "Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "moveCount", "moveFrom", "compact", "moveNoScore", "move", "moveSequence", "Lkotlin/sequences/Sequence;", "moveTo", "nullMoveReduceDepth", "depthRemaining", "swapElements", "a", "i1", "i2", "useScoutSearch", "", "depth", "newExtensions", "rivalchess-engine"})
public final class FunctionsKt {
    
    public static final int moveFrom(int compact) {
        return 0;
    }
    
    public static final int moveTo(int compact) {
        return 0;
    }
    
    public static final int moveNoScore(int move) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final kotlin.sequences.Sequence<java.lang.Integer> moveSequence(@org.jetbrains.annotations.NotNull()
    int[] moves) {
        return null;
    }
    
    public static final void applyToMoves(@org.jetbrains.annotations.NotNull()
    int[] moves, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, com.netsensia.rivalchess.engine.core.search.SearchPath> fn) {
    }
    
    public static final int moveCount(@org.jetbrains.annotations.NotNull()
    int[] moves) {
        return 0;
    }
    
    public static final void swapElements(@org.jetbrains.annotations.NotNull()
    int[] a, int i1, int i2) {
    }
    
    public static final int nullMoveReduceDepth(int depthRemaining) {
        return 0;
    }
    
    public static final boolean useScoutSearch(int depth, int newExtensions) {
        return false;
    }
}