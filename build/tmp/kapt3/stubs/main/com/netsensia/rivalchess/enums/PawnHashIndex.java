package com.netsensia.rivalchess.enums;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\b\u0086\u0001\u0018\u0000 \r2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\rB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\f\u00a8\u0006\u000e"}, d2 = {"Lcom/netsensia/rivalchess/enums/PawnHashIndex;", "", "index", "", "(Ljava/lang/String;II)V", "getIndex", "()I", "PAWNHASHENTRY_MAIN_SCORE", "PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE", "PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE", "PAWNHASHENTRY_WHITE_PASSEDPAWNS", "PAWNHASHENTRY_BLACK_PASSEDPAWNS", "PAWNHASHENTRY_LOCK", "Companion", "rivalchess-engine"})
public enum PawnHashIndex {
    /*public static final*/ PAWNHASHENTRY_MAIN_SCORE /* = new PAWNHASHENTRY_MAIN_SCORE(0) */,
    /*public static final*/ PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE /* = new PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE(0) */,
    /*public static final*/ PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE /* = new PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE(0) */,
    /*public static final*/ PAWNHASHENTRY_WHITE_PASSEDPAWNS /* = new PAWNHASHENTRY_WHITE_PASSEDPAWNS(0) */,
    /*public static final*/ PAWNHASHENTRY_BLACK_PASSEDPAWNS /* = new PAWNHASHENTRY_BLACK_PASSEDPAWNS(0) */,
    /*public static final*/ PAWNHASHENTRY_LOCK /* = new PAWNHASHENTRY_LOCK(0) */;
    private final int index = 0;
    public static final int numHashFields = 6;
    public static final com.netsensia.rivalchess.enums.PawnHashIndex.Companion Companion = null;
    
    public final int getIndex() {
        return 0;
    }
    
    PawnHashIndex(int index) {
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/netsensia/rivalchess/enums/PawnHashIndex$Companion;", "", "()V", "hashPositionSizeBytes", "", "getHashPositionSizeBytes", "()I", "numHashFields", "rivalchess-engine"})
    public static final class Companion {
        
        public final int getHashPositionSizeBytes() {
            return 0;
        }
        
        private Companion() {
            super();
        }
    }
}