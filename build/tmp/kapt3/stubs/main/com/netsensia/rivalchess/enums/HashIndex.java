package com.netsensia.rivalchess.enums;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\r\b\u0086\u0001\u0018\u0000 \u000f2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u000fB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000e\u00a8\u0006\u0010"}, d2 = {"Lcom/netsensia/rivalchess/enums/HashIndex;", "", "index", "", "(Ljava/lang/String;II)V", "getIndex", "()I", "MOVE", "SCORE", "HASHENTRY_HEIGHT", "FLAG", "VERSION", "HASHENTRY_64BIT1", "HASHENTRY_64BIT2", "HASHENTRY_LOCK1", "Companion", "rivalchess-engine"})
public enum HashIndex {
    /*public static final*/ MOVE /* = new MOVE(0) */,
    /*public static final*/ SCORE /* = new SCORE(0) */,
    /*public static final*/ HASHENTRY_HEIGHT /* = new HASHENTRY_HEIGHT(0) */,
    /*public static final*/ FLAG /* = new FLAG(0) */,
    /*public static final*/ VERSION /* = new VERSION(0) */,
    /*public static final*/ HASHENTRY_64BIT1 /* = new HASHENTRY_64BIT1(0) */,
    /*public static final*/ HASHENTRY_64BIT2 /* = new HASHENTRY_64BIT2(0) */,
    /*public static final*/ HASHENTRY_LOCK1 /* = new HASHENTRY_LOCK1(0) */;
    private final int index = 0;
    public static final int numIndexes = 6;
    public static final com.netsensia.rivalchess.enums.HashIndex.Companion Companion = null;
    
    public final int getIndex() {
        return 0;
    }
    
    HashIndex(int index) {
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0011\u0010\u0007\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\u0006R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/netsensia/rivalchess/enums/HashIndex$Companion;", "", "()V", "hashPositionSizeBytes", "", "getHashPositionSizeBytes", "()I", "numHashFields", "getNumHashFields", "numIndexes", "rivalchess-engine"})
    public static final class Companion {
        
        public final int getNumHashFields() {
            return 0;
        }
        
        public final int getHashPositionSizeBytes() {
            return 0;
        }
        
        private Companion() {
            super();
        }
    }
}