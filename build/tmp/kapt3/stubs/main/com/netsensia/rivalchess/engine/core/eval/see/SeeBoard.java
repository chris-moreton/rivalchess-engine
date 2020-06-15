package com.netsensia.rivalchess.engine.core.eval.see;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J.\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u00122\u0006\u0010$\u001a\u00020\u00142\u0006\u0010%\u001a\u00020\u0014H\u0002J\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\"\u001a\u00020\tJF\u0010\'\u001a\u00020!2\u0006\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020)2\u0006\u0010+\u001a\u00020,2\u0006\u0010$\u001a\u00020\u00142\u0006\u0010%\u001a\u00020\u00142\u0006\u0010-\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u0012H\u0002J\u001e\u0010.\u001a\u00020!2\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u0012H\u0002J\u001e\u0010/\u001a\u00020!2\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u0012H\u0002J\u000e\u00100\u001a\u00020\t2\u0006\u00101\u001a\u00020\tJ\u001e\u00102\u001a\u00020!2\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u0012H\u0002J\u001e\u00103\u001a\u00020\t2\u0006\u00104\u001a\u00020\u00142\f\u00105\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J\u0018\u00106\u001a\u0002072\u0006\u00104\u001a\u00020\u00142\u0006\u00108\u001a\u00020\tH\u0002J.\u00109\u001a\u00020!2\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u00122\u0006\u0010$\u001a\u00020\u00142\u0006\u0010%\u001a\u00020\u0014H\u0002J\u0019\u0010:\u001a\u00020!2\u0006\u00104\u001a\u00020\u00142\u0006\u00108\u001a\u00020\tH\u0082\bJ\b\u0010;\u001a\u00020!H\u0007R\u0010\u0010\u0005\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\n\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\fR\u001a\u0010\r\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\f\"\u0004\b\u000f\u0010\u0010R \u0010\u0011\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00140\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00140\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\u0016\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00140\u00130\b0\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u00020\u0018X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u001e\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010\f\u00a8\u0006<"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/eval/see/SeeBoard;", "", "board", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "(Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;)V", "bitboards", "Lcom/netsensia/rivalchess/bitboards/EngineBitboards;", "blackBitboardIndexes", "", "", "blackPieceValues", "getBlackPieceValues", "()I", "capturedPieceBitboardType", "getCapturedPieceBitboardType", "setCapturedPieceBitboardType", "(I)V", "deltas", "", "Lkotlin/Pair;", "", "enPassantHistory", "moveHistory", "mover", "Lcom/netsensia/rivalchess/model/Colour;", "getMover", "()Lcom/netsensia/rivalchess/model/Colour;", "setMover", "(Lcom/netsensia/rivalchess/model/Colour;)V", "whiteBitboardIndexes", "whitePieceValues", "getWhitePieceValues", "bishopCaptures", "", "square", "moves", "allBitboard", "friendlyBitboard", "generateCaptureMovesOnSquare", "generateSliderMoves", "whitePiece", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "blackPiece", "magicVars", "Lcom/netsensia/rivalchess/bitboards/MagicVars;", "toSquare", "kingCaptures", "knightCaptures", "makeMove", "move", "pawnCaptures", "removeFromRelevantBitboard", "squareBit", "bitboardList", "removePieceIfExistsInBitboard", "", "bitboardType", "rookCaptures", "togglePiece", "unMakeMove", "rivalchess-engine"})
public final class SeeBoard {
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.bitboards.EngineBitboards bitboards = null;
    private final java.util.List<java.lang.Integer> whiteBitboardIndexes = null;
    private final java.util.List<java.lang.Integer> blackBitboardIndexes = null;
    private final java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Long>> deltas = null;
    private final java.util.List<java.lang.Long> enPassantHistory = null;
    private final java.util.List<java.util.List<kotlin.Pair<java.lang.Integer, java.lang.Long>>> moveHistory = null;
    @org.jetbrains.annotations.NotNull()
    private com.netsensia.rivalchess.model.Colour mover;
    private int capturedPieceBitboardType = -1;
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.model.Colour getMover() {
        return null;
    }
    
    public final void setMover(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour p0) {
    }
    
    public final int getCapturedPieceBitboardType() {
        return 0;
    }
    
    public final void setCapturedPieceBitboardType(int p0) {
    }
    
    public final int makeMove(int move) {
        return 0;
    }
    
    @kotlin.ExperimentalStdlibApi()
    public final void unMakeMove() {
    }
    
    private final boolean removePieceIfExistsInBitboard(long squareBit, int bitboardType) {
        return false;
    }
    
    private final int removeFromRelevantBitboard(long squareBit, java.util.List<java.lang.Integer> bitboardList) {
        return 0;
    }
    
    private final void togglePiece(long squareBit, int bitboardType) {
    }
    
    public final int getWhitePieceValues() {
        return 0;
    }
    
    public final int getBlackPieceValues() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> generateCaptureMovesOnSquare(int square) {
        return null;
    }
    
    private final void pawnCaptures(int square, java.util.List<java.lang.Integer> moves) {
    }
    
    private final void kingCaptures(int square, java.util.List<java.lang.Integer> moves) {
    }
    
    private final void knightCaptures(int square, java.util.List<java.lang.Integer> moves) {
    }
    
    private final void bishopCaptures(int square, java.util.List<java.lang.Integer> moves, long allBitboard, long friendlyBitboard) {
    }
    
    private final void rookCaptures(int square, java.util.List<java.lang.Integer> moves, long allBitboard, long friendlyBitboard) {
    }
    
    private final void generateSliderMoves(com.netsensia.rivalchess.model.SquareOccupant whitePiece, com.netsensia.rivalchess.model.SquareOccupant blackPiece, com.netsensia.rivalchess.bitboards.MagicVars magicVars, long allBitboard, long friendlyBitboard, int toSquare, java.util.List<java.lang.Integer> moves) {
    }
    
    public SeeBoard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board) {
        super();
    }
}