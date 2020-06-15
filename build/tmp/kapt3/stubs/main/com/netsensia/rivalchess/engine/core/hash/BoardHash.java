package com.netsensia.rivalchess.engine.core.hash;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0015\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010\u0005\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0014\u001a\u00020\u0015J\u0010\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0017\u001a\u00020\u0007H\u0002J\u000e\u0010\u0018\u001a\u00020\u00072\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u0010\u0018\u001a\u00020\u00072\u0006\u0010\u001b\u001a\u00020\u0011J\u000e\u0010\u001c\u001a\u00020\u00072\u0006\u0010\u001d\u001a\u00020\u0007J\u0006\u0010\u001e\u001a\u00020\u0015J\u000e\u0010\u001f\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001aJ\u0006\u0010 \u001a\u00020\u0015J\u0016\u0010!\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010!\u001a\u00020\u0007J\u000e\u0010\"\u001a\u00020\u00152\u0006\u0010#\u001a\u00020\u0007J\u0006\u0010$\u001a\u00020\u0015J\u0016\u0010%\u001a\u00020\u00152\u0006\u0010\u001d\u001a\u00020\u00072\u0006\u0010&\u001a\u00020\u0007J\u0010\u0010\'\u001a\u00020\u00152\u0006\u0010\u000f\u001a\u00020\u0007H\u0002J.\u0010(\u001a\u00020\u00152\u0006\u0010!\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\u001a2\u0006\u0010*\u001a\u00020\u00072\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u0007J@\u0010.\u001a\u00020\u00152\u0006\u0010!\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\u001a2\u0006\u0010*\u001a\u00020\u00072\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u00072\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0005\u001a\u00020\u0004H\u0002J\u0018\u0010/\u001a\u00020\u00152\u0006\u0010)\u001a\u00020\u001a2\u0006\u0010\u0017\u001a\u00020\u0007H\u0002J\u0018\u00100\u001a\u00020\u00152\u0006\u0010)\u001a\u00020\u001a2\u0006\u0010\u0017\u001a\u00020\u0007H\u0002J\u000e\u00101\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u00102\u001a\u00020\u00072\u0006\u0010\u001d\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0010\u001a\u00020\u00118F\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013\u00a8\u00063"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/hash/BoardHash;", "", "()V", "hashTableIgnoreHeight", "", "hashTableUseHeight", "hashTableVersion", "", "getHashTableVersion", "()I", "setHashTableVersion", "(I)V", "hashTracker", "Lcom/netsensia/rivalchess/engine/core/hash/ZobristHashTracker;", "lastHashSizeCreated", "maxHashEntries", "trackedHashValue", "", "getTrackedHashValue", "()J", "clearHash", "", "copyEntryFromUseHeightToIgnoreHeightTable", "hashIndex", "getHashIndex", "engineBoard", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "hashValue", "ignoreHeight", "index", "incVersion", "initialiseHashCode", "makeNullMove", "move", "setHashSizeMB", "hashSizeMB", "setHashTable", "setHashTableUseHeightVersion", "value", "setMaxHashEntries", "storeHashMove", "board", "score", "flag", "", "height", "storeMoveInHashTable", "storeSuperVerifyAlwaysReplaceInformation", "storeSuperVerifyUseHeightInformation", "unMove", "useHeight", "rivalchess-engine"})
public final class BoardHash {
    private final com.netsensia.rivalchess.engine.core.hash.ZobristHashTracker hashTracker = null;
    private int hashTableVersion = 0;
    private int[] hashTableUseHeight;
    private int[] hashTableIgnoreHeight;
    private int maxHashEntries = 0;
    private int lastHashSizeCreated = 0;
    
    public final int getHashTableVersion() {
        return 0;
    }
    
    public final void setHashTableVersion(int p0) {
    }
    
    public final int useHeight(int index) {
        return 0;
    }
    
    public final int ignoreHeight(int index) {
        return 0;
    }
    
    public final void setHashTableUseHeightVersion(int index, int value) {
    }
    
    public final void clearHash() {
    }
    
    public final void setHashTable() {
    }
    
    public final void storeHashMove(int move, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int score, byte flag, int height) {
    }
    
    private final void copyEntryFromUseHeightToIgnoreHeightTable(int hashIndex) {
    }
    
    private final void storeMoveInHashTable(int move, com.netsensia.rivalchess.engine.core.board.EngineBoard board, int score, byte flag, int height, int hashIndex, int[] hashTableUseHeight) {
    }
    
    private final void storeSuperVerifyAlwaysReplaceInformation(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int hashIndex) {
    }
    
    private final void storeSuperVerifyUseHeightInformation(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int hashIndex) {
    }
    
    public final void setHashSizeMB(int hashSizeMB) {
    }
    
    public final void initialiseHashCode(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard) {
    }
    
    public final int getHashIndex(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard) {
        return 0;
    }
    
    public final int getHashIndex(long hashValue) {
        return 0;
    }
    
    public final void move(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard, int move) {
    }
    
    public final void unMove(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard) {
    }
    
    public final void makeNullMove() {
    }
    
    public final long getTrackedHashValue() {
        return 0L;
    }
    
    private final void setMaxHashEntries(int maxHashEntries) {
    }
    
    public final void incVersion() {
    }
    
    public BoardHash() {
        super();
    }
}