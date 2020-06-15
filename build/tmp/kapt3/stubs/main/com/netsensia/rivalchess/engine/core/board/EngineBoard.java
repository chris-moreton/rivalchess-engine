package com.netsensia.rivalchess.engine.core.board;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010F\u001a\u00020GJ\u000e\u0010H\u001a\u00020\u00062\u0006\u0010I\u001a\u00020\nJ\u000e\u0010J\u001a\u00020;2\u0006\u0010K\u001a\u00020\nJ\u0006\u0010L\u001a\u00020MJ\u0006\u0010N\u001a\u00020\nJ\u000e\u0010O\u001a\u00020G2\u0006\u0010\u0002\u001a\u00020\u0003J\u0010\u0010\u0019\u001a\u00020G2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\u0010\u0010P\u001a\u00020G2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\u000e\u0010Q\u001a\u00020G2\u0006\u0010\u0002\u001a\u00020\u0003J\u0010\u0010R\u001a\u00020G2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\b\u0010S\u001a\u00020TH\u0016J\u0006\u0010U\u001a\u00020\u0006J\u0006\u0010V\u001a\u00020\"J\u0006\u0010W\u001a\u00020\"R\u0011\u0010\u0005\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u0011\u0010\u000f\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\fR\u0011\u0010\u0011\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\fR\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0017\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\f\"\u0004\b\u0019\u0010\u000eR\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u001a\u0010\u001e\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\f\"\u0004\b \u0010\u000eR\u001a\u0010!\u001a\u00020\"X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010#\"\u0004\b$\u0010%R\u0011\u0010&\u001a\u00020\'8F\u00a2\u0006\u0006\u001a\u0004\b(\u0010)R \u0010*\u001a\b\u0012\u0004\u0012\u00020\'0+X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b,\u0010-\"\u0004\b.\u0010/R\u001a\u00100\u001a\u000201X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b2\u00103\"\u0004\b4\u00105R\u001a\u00106\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b7\u0010\f\"\u0004\b8\u0010\u000eR\u0019\u00109\u001a\b\u0012\u0004\u0012\u00020;0:\u00a2\u0006\n\n\u0002\u0010>\u001a\u0004\b<\u0010=R\u001a\u0010?\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b@\u0010\f\"\u0004\bA\u0010\u000eR\u0011\u0010B\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\bC\u0010\fR\u0011\u0010D\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\bE\u0010\f\u00a8\u0006X"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "", "board", "Lcom/netsensia/rivalchess/model/Board;", "(Lcom/netsensia/rivalchess/model/Board;)V", "allPiecesBitboard", "", "getAllPiecesBitboard", "()J", "blackKingSquare", "", "getBlackKingSquare", "()I", "setBlackKingSquare", "(I)V", "blackPawnValues", "getBlackPawnValues", "blackPieceValues", "getBlackPieceValues", "boardHashObject", "Lcom/netsensia/rivalchess/engine/core/hash/BoardHash;", "getBoardHashObject", "()Lcom/netsensia/rivalchess/engine/core/hash/BoardHash;", "castlePrivileges", "getCastlePrivileges", "setCastlePrivileges", "engineBitboards", "Lcom/netsensia/rivalchess/bitboards/EngineBitboards;", "getEngineBitboards", "()Lcom/netsensia/rivalchess/bitboards/EngineBitboards;", "halfMoveCount", "getHalfMoveCount", "setHalfMoveCount", "isOnNullMove", "", "()Z", "setOnNullMove", "(Z)V", "lastMoveMade", "Lcom/netsensia/rivalchess/engine/core/type/MoveDetail;", "getLastMoveMade", "()Lcom/netsensia/rivalchess/engine/core/type/MoveDetail;", "moveHistory", "", "getMoveHistory", "()Ljava/util/List;", "setMoveHistory", "(Ljava/util/List;)V", "mover", "Lcom/netsensia/rivalchess/model/Colour;", "getMover", "()Lcom/netsensia/rivalchess/model/Colour;", "setMover", "(Lcom/netsensia/rivalchess/model/Colour;)V", "numMovesMade", "getNumMovesMade", "setNumMovesMade", "squareContents", "", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "getSquareContents", "()[Lcom/netsensia/rivalchess/model/SquareOccupant;", "[Lcom/netsensia/rivalchess/model/SquareOccupant;", "whiteKingSquare", "getWhiteKingSquare", "setWhiteKingSquare", "whitePawnValues", "getWhitePawnValues", "whitePieceValues", "getWhitePieceValues", "calculateSupplementaryBitboards", "", "getBitboard", "bitboardType", "getSquareOccupant", "bitRef", "moveGenerator", "Lcom/netsensia/rivalchess/engine/core/board/MoveGenerator;", "previousOccurrencesOfThisPosition", "setBoard", "setEnPassantBitboard", "setEngineBoardVars", "setSquareContents", "toString", "", "trackedBoardHashCode", "wasCapture", "wasPawnPush", "rivalchess-engine"})
public final class EngineBoard {
    @org.jetbrains.annotations.NotNull()
    private final com.netsensia.rivalchess.bitboards.EngineBitboards engineBitboards = null;
    @org.jetbrains.annotations.NotNull()
    private final com.netsensia.rivalchess.model.SquareOccupant[] squareContents = null;
    @org.jetbrains.annotations.NotNull()
    private final com.netsensia.rivalchess.engine.core.hash.BoardHash boardHashObject = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.netsensia.rivalchess.engine.core.type.MoveDetail> moveHistory;
    private int numMovesMade = 0;
    private int halfMoveCount = 0;
    private int castlePrivileges = 0;
    @org.jetbrains.annotations.NotNull()
    public com.netsensia.rivalchess.model.Colour mover;
    private int whiteKingSquare = 0;
    private int blackKingSquare = 0;
    private boolean isOnNullMove = false;
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.bitboards.EngineBitboards getEngineBitboards() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.model.SquareOccupant[] getSquareContents() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.hash.BoardHash getBoardHashObject() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.netsensia.rivalchess.engine.core.type.MoveDetail> getMoveHistory() {
        return null;
    }
    
    public final void setMoveHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.netsensia.rivalchess.engine.core.type.MoveDetail> p0) {
    }
    
    public final int getNumMovesMade() {
        return 0;
    }
    
    public final void setNumMovesMade(int p0) {
    }
    
    public final int getHalfMoveCount() {
        return 0;
    }
    
    public final void setHalfMoveCount(int p0) {
    }
    
    public final int getCastlePrivileges() {
        return 0;
    }
    
    public final void setCastlePrivileges(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.model.Colour getMover() {
        return null;
    }
    
    public final void setMover(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour p0) {
    }
    
    public final int getWhiteKingSquare() {
        return 0;
    }
    
    public final void setWhiteKingSquare(int p0) {
    }
    
    public final int getBlackKingSquare() {
        return 0;
    }
    
    public final void setBlackKingSquare(int p0) {
    }
    
    public final boolean isOnNullMove() {
        return false;
    }
    
    public final void setOnNullMove(boolean p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.type.MoveDetail getLastMoveMade() {
        return null;
    }
    
    public final int getWhitePieceValues() {
        return 0;
    }
    
    public final int getBlackPieceValues() {
        return 0;
    }
    
    public final int getWhitePawnValues() {
        return 0;
    }
    
    public final int getBlackPawnValues() {
        return 0;
    }
    
    public final void setBoard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Board board) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.model.SquareOccupant getSquareOccupant(int bitRef) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.board.MoveGenerator moveGenerator() {
        return null;
    }
    
    public final void setEngineBoardVars(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Board board) {
    }
    
    private final void setSquareContents(com.netsensia.rivalchess.model.Board board) {
    }
    
    private final void setEnPassantBitboard(com.netsensia.rivalchess.model.Board board) {
    }
    
    private final void setCastlePrivileges(com.netsensia.rivalchess.model.Board board) {
    }
    
    public final void calculateSupplementaryBitboards() {
    }
    
    public final boolean wasCapture() {
        return false;
    }
    
    public final boolean wasPawnPush() {
        return false;
    }
    
    public final long getAllPiecesBitboard() {
        return 0L;
    }
    
    public final long getBitboard(int bitboardType) {
        return 0L;
    }
    
    public final int previousOccurrencesOfThisPosition() {
        return 0;
    }
    
    public final long trackedBoardHashCode() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public EngineBoard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Board board) {
        super();
    }
    
    public EngineBoard() {
        super();
    }
}