package com.netsensia.rivalchess.util;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000$\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\u001a\u0011\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0086\b\u001a\u0019\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0086\b\u001a\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t\u001a\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0001\u001a\u0018\u0010\r\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\u00012\b\u0010\u000e\u001a\u0004\u0018\u00010\t\u001a\u000e\u0010\u000f\u001a\u00020\t2\u0006\u0010\u0010\u001a\u00020\u0001\u001a\u000e\u0010\u0011\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\u0001\u00a8\u0006\u0013"}, d2 = {"getBitRefFromBoardRef", "", "boardRef", "Lcom/netsensia/rivalchess/model/Square;", "xFile", "yRank", "getEngineMoveFromSimpleAlgebraic", "Lcom/netsensia/rivalchess/engine/core/type/EngineMove;", "s", "", "getMoveRefFromEngineMove", "Lcom/netsensia/rivalchess/model/Move;", "move", "getPgnMoveFromCompactMove", "fen", "getSimpleAlgebraicFromBitRef", "bitRef", "getSimpleAlgebraicMoveFromCompactMove", "compactMove", "rivalchess-engine"})
public final class ChessBoardConversionKt {
    
    public static final int getBitRefFromBoardRef(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Square boardRef) {
        return 0;
    }
    
    public static final int getBitRefFromBoardRef(int xFile, int yRank) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.model.Move getMoveRefFromEngineMove(int move) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getSimpleAlgebraicMoveFromCompactMove(int compactMove) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getSimpleAlgebraicFromBitRef(int bitRef) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getPgnMoveFromCompactMove(int move, @org.jetbrains.annotations.Nullable()
    java.lang.String fen) throws com.netsensia.rivalchess.exception.IllegalFenException, com.netsensia.rivalchess.exception.InvalidMoveException {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.engine.core.type.EngineMove getEngineMoveFromSimpleAlgebraic(@org.jetbrains.annotations.NotNull()
    java.lang.String s) {
        return null;
    }
}