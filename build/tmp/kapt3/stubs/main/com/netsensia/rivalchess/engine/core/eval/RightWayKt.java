package com.netsensia.rivalchess.engine.core.eval;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000,\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\u001a.\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t\u001a\u001e\u0010\n\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007\u001a\u0016\u0010\u000b\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007\u001a.\u0010\f\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t\u001a\u001e\u0010\r\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007\u001a\u000e\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u0010\u001a\u000e\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u0010\u001a&\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00102\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\u0016"}, d2 = {"checkForPositionD", "", "friendlyPawns", "", "friendlyKnights", "friendlyBishops", "rightWaySquares", "Lcom/netsensia/rivalchess/engine/core/eval/RightWaySquares;", "cornerColour", "Lcom/netsensia/rivalchess/model/Colour;", "checkForPositionE", "checkForPositionFOrH", "checkForPositionsAOrD", "checkForPositionsBOrC", "getBlackKingRightWayScore", "engineBoard", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "getWhiteKingRightWayScore", "scoreRightWayPositions", "board", "isWhite", "", "rivalchess-engine"})
public final class RightWayKt {
    
    public static final int scoreRightWayPositions(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.RightWaySquares rightWaySquares, boolean isWhite, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour cornerColour) {
        return 0;
    }
    
    public static final int checkForPositionFOrH(long friendlyPawns, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.RightWaySquares rightWaySquares) {
        return 0;
    }
    
    public static final int checkForPositionE(long friendlyPawns, long friendlyKnights, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.RightWaySquares rightWaySquares) {
        return 0;
    }
    
    public static final int checkForPositionsBOrC(long friendlyPawns, long friendlyBishops, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.RightWaySquares rightWaySquares) {
        return 0;
    }
    
    public static final int checkForPositionsAOrD(long friendlyPawns, long friendlyKnights, long friendlyBishops, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.RightWaySquares rightWaySquares, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour cornerColour) {
        return 0;
    }
    
    public static final int checkForPositionD(long friendlyPawns, long friendlyKnights, long friendlyBishops, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.RightWaySquares rightWaySquares, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour cornerColour) {
        return 0;
    }
    
    public static final int getWhiteKingRightWayScore(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard) {
        return 0;
    }
    
    public static final int getBlackKingRightWayScore(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard) {
        return 0;
    }
}