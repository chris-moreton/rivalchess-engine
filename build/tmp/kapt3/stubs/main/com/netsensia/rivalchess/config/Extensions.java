package com.netsensia.rivalchess.config;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\f\b\u0086\u0001\u0018\u0000 \u000e2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u000eB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\r\u00a8\u0006\u000f"}, d2 = {"Lcom/netsensia/rivalchess/config/Extensions;", "", "value", "", "(Ljava/lang/String;II)V", "getValue", "()I", "FRACTIONAL_EXTENSION_FULL", "FRACTIONAL_EXTENSION_THREAT", "FRACTIONAL_EXTENSION_CHECK", "FRACTIONAL_EXTENSION_RECAPTURE", "FRACTIONAL_EXTENSION_PAWN", "RECAPTURE_EXTENSION_MARGIN", "LAST_EXTENSION_LAYER", "Companion", "rivalchess-engine"})
public enum Extensions {
    /*public static final*/ FRACTIONAL_EXTENSION_FULL /* = new FRACTIONAL_EXTENSION_FULL(0) */,
    /*public static final*/ FRACTIONAL_EXTENSION_THREAT /* = new FRACTIONAL_EXTENSION_THREAT(0) */,
    /*public static final*/ FRACTIONAL_EXTENSION_CHECK /* = new FRACTIONAL_EXTENSION_CHECK(0) */,
    /*public static final*/ FRACTIONAL_EXTENSION_RECAPTURE /* = new FRACTIONAL_EXTENSION_RECAPTURE(0) */,
    /*public static final*/ FRACTIONAL_EXTENSION_PAWN /* = new FRACTIONAL_EXTENSION_PAWN(0) */,
    /*public static final*/ RECAPTURE_EXTENSION_MARGIN /* = new RECAPTURE_EXTENSION_MARGIN(0) */,
    /*public static final*/ LAST_EXTENSION_LAYER /* = new LAST_EXTENSION_LAYER(0) */;
    private final int value = 0;
    public static final com.netsensia.rivalchess.config.Extensions.Companion Companion = null;
    
    public final int getValue() {
        return 0;
    }
    
    Extensions(int value) {
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/netsensia/rivalchess/config/Extensions$Companion;", "", "()V", "maxNewExtensionsTreePart", "", "", "getMaxNewExtensionsTreePart", "()Ljava/util/List;", "rivalchess-engine"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.Integer> getMaxNewExtensionsTreePart() {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}