package com.netsensia.rivalchess.engine.core.type;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0005\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002R\u0012\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\t\u001a\u00020\u00068\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\n\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\r\u001a\u00020\u000e8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u000f\u001a\u00020\u00048\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u0010\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/type/MoveDetail;", "", "()V", "capturePiece", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "castlePrivileges", "", "enPassantBitboard", "", "halfMoveCount", "hashValue", "isOnNullMove", "", "move", "", "movePiece", "pawnHashValue", "rivalchess-engine"})
public final class MoveDetail {
    @org.jetbrains.annotations.NotNull()
    public com.netsensia.rivalchess.model.SquareOccupant movePiece = com.netsensia.rivalchess.model.SquareOccupant.NONE;
    @org.jetbrains.annotations.NotNull()
    public com.netsensia.rivalchess.model.SquareOccupant capturePiece = com.netsensia.rivalchess.model.SquareOccupant.NONE;
    public int move = 0;
    public byte halfMoveCount = (byte)0;
    public byte castlePrivileges = (byte)0;
    public long enPassantBitboard = 0L;
    public long hashValue = 0L;
    public boolean isOnNullMove = false;
    public long pawnHashValue = 0L;
    
    public MoveDetail() {
        super();
    }
}