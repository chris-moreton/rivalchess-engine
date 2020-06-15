package com.netsensia.rivalchess.engine.core.board;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0016\n\u0002\b\b\n\u0002\u0010\u0015\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\u0006\u0010\t\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u00072\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J \u0010\u001f\u001a\u00020\u001b2\u0006\u0010 \u001a\u00020\u00072\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010!\u001a\u00020\"H\u0002J\b\u0010#\u001a\u00020\u001eH\u0002J\u0010\u0010$\u001a\u00020\u001e2\u0006\u0010\u0004\u001a\u00020\u0005H\u0002JN\u0010%\u001a\u00020\u001b2\b\b\u0002\u0010&\u001a\u00020\u00072\b\b\u0002\u0010\'\u001a\u00020\u00072\b\b\u0002\u0010(\u001a\u00020\u00052\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00070*2\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\u001e\u0012\u0004\u0012\u00020\u001e0*H\u0002J\u0010\u0010,\u001a\u00020\u001b2\u0006\u0010-\u001a\u00020\u0007H\u0002J\u0010\u0010.\u001a\u00020\u001b2\u0006\u0010/\u001a\u00020\u001eH\u0002J\u0006\u00100\u001a\u00020\u0000J\u000e\u00101\u001a\u00020\u00002\u0006\u00102\u001a\u00020\"J,\u00103\u001a\u00020\u001b2\u0006\u00104\u001a\u00020\u001e2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u001e062\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u001e06H\u0002J \u00108\u001a\u00020\u001b2\u0006\u00102\u001a\u00020\"2\u0006\u00109\u001a\u00020\u00072\u0006\u0010/\u001a\u00020\u001eH\u0002J<\u0010:\u001a\u00020\u001b2\u0006\u00102\u001a\u00020\"2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u001e062\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u001e062\u0006\u00109\u001a\u00020\u00072\u0006\u00104\u001a\u00020\u001eH\u0002J0\u0010;\u001a\u00020\u001b2\u0006\u00102\u001a\u00020\"2\u0006\u00109\u001a\u00020\u00072\u0006\u0010<\u001a\u00020=2\u0006\u0010>\u001a\u00020\u00072\u0006\u0010?\u001a\u00020\u0007H\u0002J \u0010@\u001a\u00020\u001b2\u0006\u0010A\u001a\u00020B2\u0006\u0010C\u001a\u00020B2\u0006\u0010<\u001a\u00020=H\u0002J\b\u0010D\u001a\u00020\u0007H\u0002J\b\u0010E\u001a\u00020\u001eH\u0002J\b\u0010F\u001a\u00020\u001eH\u0002J&\u0010G\u001a\u00020\u001e2\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u001e062\u0006\u0010H\u001a\u00020\u00072\u0006\u0010I\u001a\u00020\u0007H\u0002J\u001e\u0010J\u001a\u00020\u001e2\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u001e062\u0006\u0010H\u001a\u00020\u0007H\u0002J&\u0010K\u001a\u00020\u001e2\u0006\u0010H\u001a\u00020\u00072\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u001e062\u0006\u0010L\u001a\u00020\u001eH\u0002J\u0010\u0010M\u001a\u00020\u001e2\u0006\u0010L\u001a\u00020\u001eH\u0002J\u0010\u0010N\u001a\u00020\u001e2\u0006\u0010L\u001a\u00020\u001eH\u0002R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\b\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u00020\u0015X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006O"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/board/MoveGenerator;", "", "engineBitboards", "Lcom/netsensia/rivalchess/bitboards/EngineBitboards;", "mover", "Lcom/netsensia/rivalchess/model/Colour;", "whiteKingSquare", "", "blackKingSquare", "castlePrivileges", "(Lcom/netsensia/rivalchess/bitboards/EngineBitboards;Lcom/netsensia/rivalchess/model/Colour;III)V", "bitboards", "", "getBitboards", "()[J", "moveCount", "getMoveCount", "()I", "setMoveCount", "(I)V", "moves", "", "getMoves", "()[I", "setMoves", "([I)V", "addMoves", "", "fromSquareMask", "bitboard", "", "addPawnMoves", "fromSquareMoveMask", "queenCapturesOnly", "", "emptySquaresBitboard", "enPassantCaptureRank", "generateCastleMoves", "kingStartSquare", "queenStartSquare", "opponent", "privileges", "Lkotlin/Pair;", "castleSquares", "generateKingMoves", "kingSquare", "generateKnightMoves", "knightBitboard", "generateLegalMoves", "generateLegalQuiesceMoves", "generateChecks", "generatePawnMoves", "pawnBitboard", "bitboardMaskForwardPawnMoves", "", "bitboardMaskCapturePawnMoves", "generateQuiesceKnightMoves", "enemyKingSquare", "generateQuiescePawnMoves", "generateQuiesceSliderMoves", "magicVars", "Lcom/netsensia/rivalchess/bitboards/MagicVars;", "whiteSliderConstant", "blackSliderConstant", "generateSliderMoves", "whitePiece", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "blackPiece", "kingSquareForMover", "knightBitboardForMover", "pawnBitboardForMover", "pawnCaptures", "bitRef", "bitboardType", "pawnCapturesPlusEnPassantSquare", "pawnForwardAndCaptureMovesBitboard", "bitboardPawnMoves", "pawnForwardMovesBitboard", "potentialPawnJumpMoves", "rivalchess-engine"})
public final class MoveGenerator {
    @org.jetbrains.annotations.NotNull()
    private int[] moves;
    @org.jetbrains.annotations.NotNull()
    private final long[] bitboards = null;
    private int moveCount = 0;
    private final com.netsensia.rivalchess.bitboards.EngineBitboards engineBitboards = null;
    private final com.netsensia.rivalchess.model.Colour mover = null;
    private final int whiteKingSquare = 0;
    private final int blackKingSquare = 0;
    private final int castlePrivileges = 0;
    
    @org.jetbrains.annotations.NotNull()
    public final int[] getMoves() {
        return null;
    }
    
    public final void setMoves(@org.jetbrains.annotations.NotNull()
    int[] p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final long[] getBitboards() {
        return null;
    }
    
    public final int getMoveCount() {
        return 0;
    }
    
    public final void setMoveCount(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.board.MoveGenerator generateLegalMoves() {
        return null;
    }
    
    private final int kingSquareForMover() {
        return 0;
    }
    
    private final long knightBitboardForMover() {
        return 0L;
    }
    
    private final void generateKnightMoves(long knightBitboard) {
    }
    
    private final void addMoves(int fromSquareMask, long bitboard) {
    }
    
    private final void generateKingMoves(int kingSquare) {
    }
    
    private final void generateCastleMoves(int kingStartSquare, int queenStartSquare, com.netsensia.rivalchess.model.Colour opponent, kotlin.Pair<java.lang.Integer, java.lang.Integer> privileges, kotlin.Pair<java.lang.Long, java.lang.Long> castleSquares) {
    }
    
    private final long emptySquaresBitboard() {
        return 0L;
    }
    
    private final void generateSliderMoves(com.netsensia.rivalchess.model.SquareOccupant whitePiece, com.netsensia.rivalchess.model.SquareOccupant blackPiece, com.netsensia.rivalchess.bitboards.MagicVars magicVars) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.board.MoveGenerator generateLegalQuiesceMoves(boolean generateChecks) {
        return null;
    }
    
    private final void generateQuiesceKnightMoves(boolean generateChecks, int enemyKingSquare, long knightBitboard) {
    }
    
    private final void generateQuiescePawnMoves(boolean generateChecks, java.util.List<java.lang.Long> bitboardMaskForwardPawnMoves, java.util.List<java.lang.Long> bitboardMaskCapturePawnMoves, int enemyKingSquare, long pawnBitboard) {
    }
    
    private final void generateQuiesceSliderMoves(boolean generateChecks, int enemyKingSquare, com.netsensia.rivalchess.bitboards.MagicVars magicVars, int whiteSliderConstant, int blackSliderConstant) {
    }
    
    private final void addPawnMoves(int fromSquareMoveMask, long bitboard, boolean queenCapturesOnly) {
    }
    
    private final long enPassantCaptureRank(com.netsensia.rivalchess.model.Colour mover) {
        return 0L;
    }
    
    private final void generatePawnMoves(long pawnBitboard, java.util.List<java.lang.Long> bitboardMaskForwardPawnMoves, java.util.List<java.lang.Long> bitboardMaskCapturePawnMoves) {
    }
    
    private final long pawnForwardMovesBitboard(long bitboardPawnMoves) {
        return 0L;
    }
    
    private final long potentialPawnJumpMoves(long bitboardPawnMoves) {
        return 0L;
    }
    
    private final long pawnForwardAndCaptureMovesBitboard(int bitRef, java.util.List<java.lang.Long> bitboardMaskCapturePawnMoves, long bitboardPawnMoves) {
        return 0L;
    }
    
    private final long pawnCapturesPlusEnPassantSquare(java.util.List<java.lang.Long> bitboardMaskCapturePawnMoves, int bitRef) {
        return 0L;
    }
    
    private final long pawnCaptures(java.util.List<java.lang.Long> bitboardMaskCapturePawnMoves, int bitRef, int bitboardType) {
        return 0L;
    }
    
    private final long pawnBitboardForMover() {
        return 0L;
    }
    
    public MoveGenerator(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.bitboards.EngineBitboards engineBitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour mover, int whiteKingSquare, int blackKingSquare, int castlePrivileges) {
        super();
    }
}