package com.netsensia.rivalchess.enums;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\r\b\u0086\u0001\u0018\u0000 \u000f2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u000fB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000e\u00a8\u0006\u0010"}, d2 = {"Lcom/netsensia/rivalchess/enums/BoardDirection;", "", "index", "", "(Ljava/lang/String;II)V", "getIndex", "()I", "E", "SE", "S", "SW", "W", "NW", "N", "NE", "Companion", "rivalchess-engine"})
public enum BoardDirection {
    /*public static final*/ E /* = new E(0) */,
    /*public static final*/ SE /* = new SE(0) */,
    /*public static final*/ S /* = new S(0) */,
    /*public static final*/ SW /* = new SW(0) */,
    /*public static final*/ W /* = new W(0) */,
    /*public static final*/ NW /* = new NW(0) */,
    /*public static final*/ N /* = new N(0) */,
    /*public static final*/ NE /* = new NE(0) */;
    private final int index = 0;
    public static final com.netsensia.rivalchess.enums.BoardDirection.Companion Companion = null;
    
    public final int getIndex() {
        return 0;
    }
    
    BoardDirection(int index) {
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/netsensia/rivalchess/enums/BoardDirection$Companion;", "", "()V", "fromIndex", "Lcom/netsensia/rivalchess/enums/BoardDirection;", "index", "", "rivalchess-engine"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.enums.BoardDirection fromIndex(int index) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}