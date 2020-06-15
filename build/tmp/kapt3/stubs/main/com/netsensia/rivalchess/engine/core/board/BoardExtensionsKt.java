package com.netsensia.rivalchess.engine.core.board;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0019\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a\u001c\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u001a\n\u0010\u0007\u001a\u00020\b*\u00020\u0002\u001a\n\u0010\t\u001a\u00020\n*\u00020\u0002\u001a\u000e\u0010\u000b\u001a\u00060\fj\u0002`\r*\u00020\u0002\u001a\u0012\u0010\u000e\u001a\u00020\u000f*\u00020\u00022\u0006\u0010\u0010\u001a\u00020\u0006\u001a*\u0010\u0011\u001a\u00020\u0006*\u00020\u00022\u0006\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\u0016\u001a\u0012\u0010\u0014\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0012\u001a\u00020\u0006\u001a\u0012\u0010\u0017\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0018\u001a\u00020\u0019\u001a\n\u0010\u001a\u001a\u00020\u0001*\u00020\u0002\u001a\u0012\u0010\u001b\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0010\u001a\u00020\u0006\u001a\u0012\u0010\u001c\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u001d\u001a\u00020\u0006\u001a\n\u0010\u001e\u001a\u00020\u0001*\u00020\u0002\u00a8\u0006\u001f"}, d2 = {"anyLegalMoves", "", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "moves", "", "moveIndex", "", "getCharBoard", "", "getFen", "", "getFenBoard", "Ljava/lang/StringBuilder;", "Lkotlin/text/StringBuilder;", "getPiece", "Lcom/netsensia/rivalchess/model/Piece;", "bitRef", "getScore", "move", "includeChecks", "isCapture", "staticExchangeEvaluator", "Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluator;", "isCheck", "colour", "Lcom/netsensia/rivalchess/model/Colour;", "isGameOver", "isSquareEmpty", "moveDoesNotLeaveMoverInCheck", "moveToVerify", "onlyKingsRemain", "rivalchess-engine"})
public final class BoardExtensionsKt {
    
    public static final boolean onlyKingsRemain(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$onlyKingsRemain) {
        return false;
    }
    
    public static final boolean isSquareEmpty(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$isSquareEmpty, int bitRef) {
        return false;
    }
    
    public static final boolean isCapture(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$isCapture, int move) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final com.netsensia.rivalchess.model.Piece getPiece(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$getPiece, int bitRef) {
        return null;
    }
    
    public static final boolean isCheck(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$isCheck, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour colour) {
        return false;
    }
    
    public static final int getScore(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$getScore, int move, boolean includeChecks, boolean isCapture, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.see.StaticExchangeEvaluator staticExchangeEvaluator) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    public static final boolean moveDoesNotLeaveMoverInCheck(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$moveDoesNotLeaveMoverInCheck, int moveToVerify) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final char[] getCharBoard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$getCharBoard) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.StringBuilder getFenBoard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$getFenBoard) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getFen(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$getFen) {
        return null;
    }
    
    private static final boolean anyLegalMoves(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$anyLegalMoves, int[] moves, int moveIndex) {
        return false;
    }
    
    public static final boolean isGameOver(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard $this$isGameOver) {
        return false;
    }
}