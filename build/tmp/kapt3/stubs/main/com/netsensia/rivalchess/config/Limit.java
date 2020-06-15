package com.netsensia.rivalchess.config;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\b\u0086\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\r\u00a8\u0006\u000e"}, d2 = {"Lcom/netsensia/rivalchess/config/Limit;", "", "value", "", "(Ljava/lang/String;II)V", "getValue", "()I", "MAX_LEGAL_MOVES", "MAX_SEARCH_MILLIS", "MIN_SEARCH_MILLIS", "MAX_SEARCH_DEPTH", "MAX_QUIESCE_DEPTH", "MAX_EXTENSION_DEPTH", "MAX_TREE_DEPTH", "rivalchess-engine"})
public enum Limit {
    /*public static final*/ MAX_LEGAL_MOVES /* = new MAX_LEGAL_MOVES(0) */,
    /*public static final*/ MAX_SEARCH_MILLIS /* = new MAX_SEARCH_MILLIS(0) */,
    /*public static final*/ MIN_SEARCH_MILLIS /* = new MIN_SEARCH_MILLIS(0) */,
    /*public static final*/ MAX_SEARCH_DEPTH /* = new MAX_SEARCH_DEPTH(0) */,
    /*public static final*/ MAX_QUIESCE_DEPTH /* = new MAX_QUIESCE_DEPTH(0) */,
    /*public static final*/ MAX_EXTENSION_DEPTH /* = new MAX_EXTENSION_DEPTH(0) */,
    /*public static final*/ MAX_TREE_DEPTH /* = new MAX_TREE_DEPTH(0) */;
    private final int value = 0;
    
    public final int getValue() {
        return 0;
    }
    
    Limit(int value) {
    }
}