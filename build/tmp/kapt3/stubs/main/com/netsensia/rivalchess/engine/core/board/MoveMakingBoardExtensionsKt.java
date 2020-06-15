package com.netsensia.rivalchess.engine.core.board;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000,\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\n\u001a\u0014\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0002\u001a\u0014\u0010\u0005\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0002\u001a\u0014\u0010\u0006\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0004H\u0002\u001a\u0014\u0010\b\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0004H\u0002\u001a\u001e\u0010\t\u001a\u00020\u0001*\u00020\u00022\b\u0010\n\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\f\u001a\u00020\u0004H\u0002\u001a\u001c\u0010\r\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0004H\u0002\u001a\u0012\u0010\u000e\u001a\u00020\u000f*\u00020\u00022\u0006\u0010\u0010\u001a\u00020\u0004\u001a\u001c\u0010\u000e\u001a\u00020\u000f*\u00020\u00022\u0006\u0010\u0010\u001a\u00020\u00042\b\b\u0002\u0010\u0011\u001a\u00020\u000f\u001a6\u0010\u0012\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\u0013\u001a\u0004\u0018\u00010\u000bH\u0002\u001a\n\u0010\u0014\u001a\u00020\u0001*\u00020\u0002\u001a\u0014\u0010\u0015\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0004H\u0002\u001a\u0014\u0010\u0016\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0004H\u0002\u001a\u001c\u0010\u0017\u001a\u00020\u000f*\u00020\u00022\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002\u001a\u001c\u0010\u001b\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u0019H\u0002\u001a$\u0010\u001d\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u0013\u001a\u00020\u000bH\u0002\u001a$\u0010\u001e\u001a\u00020\u000b*\u00020\u00022\u0006\u0010\u001f\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002\u001a$\u0010 \u001a\u00020\u000f*\u00020\u00022\u0006\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002\u001a\n\u0010!\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\"\u001a\u00020\u0001*\u00020\u0002\u00a8\u0006#"}, d2 = {"adjustCastlePrivilegesForBlackRookMove", "", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "moveFrom", "", "adjustCastlePrivilegesForWhiteRookMove", "adjustKingVariablesForBlackKingMove", "compactMove", "adjustKingVariablesForWhiteKingMove", "makeAdjustmentsFollowingCaptureOfBlackPiece", "capturePiece", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "moveTo", "makeAdjustmentsFollowingCaptureOfWhitePiece", "makeMove", "", "engineMove", "ignoreCheck", "makeNonTrivialMoveTypeAdjustments", "movePiece", "makeNullMove", "makeSpecialBlackPawnMoveAdjustments", "makeSpecialWhitePawnMoveAdjustments", "removePromotionPiece", "fromMask", "", "toMask", "replaceCapturedPiece", "toSquare", "replaceCastledRook", "replaceMovedPiece", "fromSquare", "unMakeEnPassants", "unMakeMove", "unMakeNullMove", "rivalchess-engine"})
public final class MoveMakingBoardExtensionsKt {
    
    public static final void makeNullMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeNullMove) {
    }
    
    public static final void unMakeNullMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$unMakeNullMove) {
    }
    
    public static final boolean makeMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeMove, int engineMove) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return false;
    }
    
    public static final boolean makeMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeMove, int engineMove, boolean ignoreCheck) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return false;
    }
    
    public static final void unMakeMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$unMakeMove) throws com.netsensia.rivalchess.exception.InvalidMoveException {
    }
    
    private static final boolean unMakeEnPassants(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$unMakeEnPassants, int toSquare, long fromMask, long toMask) {
        return false;
    }
    
    private static final void replaceCastledRook(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$replaceCastledRook, long fromMask, long toMask, com.netsensia.rivalchess.model.SquareOccupant movePiece) {
    }
    
    private static final void replaceCapturedPiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$replaceCapturedPiece, int toSquare, long toMask) {
    }
    
    private static final boolean removePromotionPiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$removePromotionPiece, long fromMask, long toMask) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return false;
    }
    
    private static final com.netsensia.rivalchess.model.SquareOccupant replaceMovedPiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$replaceMovedPiece, int fromSquare, long fromMask, long toMask) {
        return null;
    }
    
    private static final void makeNonTrivialMoveTypeAdjustments(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeNonTrivialMoveTypeAdjustments, int moveFrom, int moveTo, int compactMove, com.netsensia.rivalchess.model.SquareOccupant capturePiece, com.netsensia.rivalchess.model.SquareOccupant movePiece) throws com.netsensia.rivalchess.exception.InvalidMoveException {
    }
    
    private static final void makeAdjustmentsFollowingCaptureOfWhitePiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeAdjustmentsFollowingCaptureOfWhitePiece, com.netsensia.rivalchess.model.SquareOccupant capturePiece, int moveTo) {
    }
    
    private static final void adjustKingVariablesForBlackKingMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$adjustKingVariablesForBlackKingMove, int compactMove) {
    }
    
    private static final void adjustCastlePrivilegesForBlackRookMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$adjustCastlePrivilegesForBlackRookMove, int moveFrom) {
    }
    
    private static final void makeSpecialBlackPawnMoveAdjustments(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeSpecialBlackPawnMoveAdjustments, int compactMove) throws com.netsensia.rivalchess.exception.InvalidMoveException {
    }
    
    private static final void makeAdjustmentsFollowingCaptureOfBlackPiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeAdjustmentsFollowingCaptureOfBlackPiece, com.netsensia.rivalchess.model.SquareOccupant capturePiece, int moveTo) {
    }
    
    private static final void adjustKingVariablesForWhiteKingMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$adjustKingVariablesForWhiteKingMove, int compactMove) {
    }
    
    private static final void adjustCastlePrivilegesForWhiteRookMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$adjustCastlePrivilegesForWhiteRookMove, int moveFrom) {
    }
    
    private static final void makeSpecialWhitePawnMoveAdjustments(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$makeSpecialWhitePawnMoveAdjustments, int compactMove) throws com.netsensia.rivalchess.exception.InvalidMoveException {
    }
}