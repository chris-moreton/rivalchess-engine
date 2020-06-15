package com.netsensia.rivalchess.engine.core.hash;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u0016\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\u0007J \u0010\u000f\u001a\u00020\u00072\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\rH\u0002J \u0010\u0014\u001a\u00020\u00072\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\rH\u0002J\u0010\u0010\u0016\u001a\u00020\u00072\u0006\u0010\u0013\u001a\u00020\rH\u0002J\u0018\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0012\u001a\u00020\u0011H\u0002J\u0010\u0010\u001a\u001a\u00020\u00072\u0006\u0010\u0013\u001a\u00020\rH\u0002J\u0011\u0010\u001b\u001a\u00020\u00072\u0006\u0010\u0013\u001a\u00020\rH\u0082\bJ\u0018\u0010\u001c\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0012\u001a\u00020\u0011H\u0002J\u0011\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u0013\u001a\u00020\rH\u0082\bJ(\u0010\u001e\u001a\u00020\u00072\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u0011H\u0002J\u0019\u0010\u001f\u001a\u00020\u00072\u0006\u0010 \u001a\u00020\u00112\u0006\u0010!\u001a\u00020\rH\u0082\bJ!\u0010\"\u001a\u00020\u00072\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010!\u001a\u00020\rH\u0082\bJ\t\u0010#\u001a\u00020\u0007H\u0082\bJ\u0010\u0010$\u001a\u00020%2\u0006\u0010\u0013\u001a\u00020\rH\u0002J\u0018\u0010&\u001a\u00020%2\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\'\u001a\u00020(H\u0002J\u0018\u0010)\u001a\u00020%2\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\'\u001a\u00020(H\u0002J\u000e\u0010*\u001a\u00020\u00072\u0006\u0010\'\u001a\u00020(J \u0010+\u001a\u00020%2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\'\u001a\u00020(H\u0002J\u0010\u0010,\u001a\u00020%2\u0006\u0010\u0013\u001a\u00020\rH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u0005\u001a\u00020\u00048\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/hash/ZobristHashTracker;", "", "()V", "switchMoverHashValue", "", "trackedBoardHashValue", "initHash", "", "engineBoard", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "makeMove", "board", "engineMove", "", "nullMove", "processCapture", "movedPiece", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "capturedPiece", "bitRefTo", "processCastling", "bitRefFrom", "processPossibleBlackKingSideCastle", "processPossibleBlackPawnEnPassantCapture", "move", "Lcom/netsensia/rivalchess/model/Move;", "processPossibleBlackQueenSideCastle", "processPossibleWhiteKingSideCastle", "processPossibleWhitePawnEnPassantCapture", "processPossibleWhiteQueenSideCastle", "processSpecialPawnMoves", "removeOrPlacePieceOnEmptySquare", "squareOccupant", "bitRef", "replaceWithAnotherPiece", "switchMover", "unMakeBlackCastle", "", "unMakeCapture", "moveDetail", "Lcom/netsensia/rivalchess/engine/core/type/MoveDetail;", "unMakeEnPassant", "unMakeMove", "unMakePromotion", "unMakeWhiteCastle", "rivalchess-engine"})
public final class ZobristHashTracker {
    public long trackedBoardHashValue = 0L;
    private final long switchMoverHashValue = 0L;
    
    public final void initHash(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard) {
    }
    
    private final void removeOrPlacePieceOnEmptySquare(com.netsensia.rivalchess.model.SquareOccupant squareOccupant, int bitRef) {
    }
    
    private final void replaceWithAnotherPiece(com.netsensia.rivalchess.model.SquareOccupant movedPiece, com.netsensia.rivalchess.model.SquareOccupant capturedPiece, int bitRef) {
    }
    
    private final void processPossibleWhiteKingSideCastle(int bitRefTo) {
    }
    
    private final void processPossibleWhiteQueenSideCastle(int bitRefTo) {
    }
    
    private final void processPossibleBlackQueenSideCastle(int bitRefTo) {
    }
    
    private final void processPossibleBlackKingSideCastle(int bitRefTo) {
    }
    
    private final void processPossibleWhitePawnEnPassantCapture(com.netsensia.rivalchess.model.Move move, com.netsensia.rivalchess.model.SquareOccupant capturedPiece) {
    }
    
    private final void processPossibleBlackPawnEnPassantCapture(com.netsensia.rivalchess.model.Move move, com.netsensia.rivalchess.model.SquareOccupant capturedPiece) {
    }
    
    private final void processCapture(com.netsensia.rivalchess.model.SquareOccupant movedPiece, com.netsensia.rivalchess.model.SquareOccupant capturedPiece, int bitRefTo) {
    }
    
    private final void switchMover() {
    }
    
    private final void processCastling(int bitRefFrom, com.netsensia.rivalchess.model.SquareOccupant movedPiece, int bitRefTo) {
    }
    
    private final void processSpecialPawnMoves(com.netsensia.rivalchess.model.Move move, com.netsensia.rivalchess.model.SquareOccupant movedPiece, int bitRefTo, com.netsensia.rivalchess.model.SquareOccupant capturedPiece) {
    }
    
    private final boolean unMakeEnPassant(int bitRefTo, com.netsensia.rivalchess.engine.core.type.MoveDetail moveDetail) {
        return false;
    }
    
    private final boolean unMakeCapture(int bitRefTo, com.netsensia.rivalchess.engine.core.type.MoveDetail moveDetail) {
        return false;
    }
    
    private final boolean unMakeWhiteCastle(int bitRefTo) {
        return false;
    }
    
    private final boolean unMakeBlackCastle(int bitRefTo) {
        return false;
    }
    
    private final boolean unMakePromotion(int bitRefFrom, int bitRefTo, com.netsensia.rivalchess.engine.core.type.MoveDetail moveDetail) {
        return false;
    }
    
    public final void nullMove() {
    }
    
    public final void makeMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int engineMove) {
    }
    
    public final void unMakeMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.type.MoveDetail moveDetail) {
    }
    
    public ZobristHashTracker() {
        super();
    }
}