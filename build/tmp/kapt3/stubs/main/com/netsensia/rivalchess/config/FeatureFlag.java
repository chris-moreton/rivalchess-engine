package com.netsensia.rivalchess.config;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000b\n\u0002\b\f\b\u0086\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/netsensia/rivalchess/config/FeatureFlag;", "", "isActive", "", "(Ljava/lang/String;IZ)V", "()Z", "USE_HEIGHT_REPLACE_HASH", "USE_ALWAYS_REPLACE_HASH", "USE_NULL_MOVE_PRUNING", "USE_HISTORY_HEURISTIC", "USE_MATE_HISTORY_KILLERS", "USE_INTERNAL_OPENING_BOOK", "USE_PV_SEARCH", "USE_INTERNAL_ITERATIVE_DEEPENING", "USE_SUPER_VERIFY_ON_HASH", "rivalchess-engine"})
public enum FeatureFlag {
    /*public static final*/ USE_HEIGHT_REPLACE_HASH /* = new USE_HEIGHT_REPLACE_HASH(false) */,
    /*public static final*/ USE_ALWAYS_REPLACE_HASH /* = new USE_ALWAYS_REPLACE_HASH(false) */,
    /*public static final*/ USE_NULL_MOVE_PRUNING /* = new USE_NULL_MOVE_PRUNING(false) */,
    /*public static final*/ USE_HISTORY_HEURISTIC /* = new USE_HISTORY_HEURISTIC(false) */,
    /*public static final*/ USE_MATE_HISTORY_KILLERS /* = new USE_MATE_HISTORY_KILLERS(false) */,
    /*public static final*/ USE_INTERNAL_OPENING_BOOK /* = new USE_INTERNAL_OPENING_BOOK(false) */,
    /*public static final*/ USE_PV_SEARCH /* = new USE_PV_SEARCH(false) */,
    /*public static final*/ USE_INTERNAL_ITERATIVE_DEEPENING /* = new USE_INTERNAL_ITERATIVE_DEEPENING(false) */,
    /*public static final*/ USE_SUPER_VERIFY_ON_HASH /* = new USE_SUPER_VERIFY_ON_HASH(false) */;
    private final boolean isActive = false;
    
    public final boolean isActive() {
        return false;
    }
    
    FeatureFlag(boolean isActive) {
    }
}