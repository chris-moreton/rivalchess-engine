package com.netsensia.rivalchess.engine.core;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u001b\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001:\u0001$B=\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\u0006\u0010\t\u001a\u00020\u0005\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\u0006\u0010\u000b\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0007H\u00c6\u0003JO\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u001f\u001a\u00020\u00072\b\u0010 \u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010!\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\"\u001a\u00020#H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u0011\u0010\u000b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u000e\u00a8\u0006%"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/MoveSearchResult;", "", "bestPath", "Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "low", "", "scoutSearch", "", "bestMoveForHash", "threatExtend", "hashFlag", "moveIsLegal", "(Lcom/netsensia/rivalchess/engine/core/search/SearchPath;IZIIIZ)V", "getBestMoveForHash", "()I", "getBestPath", "()Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "getHashFlag", "getLow", "getMoveIsLegal", "()Z", "getScoutSearch", "getThreatExtend", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "toString", "", "Builder", "rivalchess-engine"})
public final class MoveSearchResult {
    @org.jetbrains.annotations.NotNull()
    private final com.netsensia.rivalchess.engine.core.search.SearchPath bestPath = null;
    private final int low = 0;
    private final boolean scoutSearch = false;
    private final int bestMoveForHash = 0;
    private final int threatExtend = 0;
    private final int hashFlag = 0;
    private final boolean moveIsLegal = false;
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.search.SearchPath getBestPath() {
        return null;
    }
    
    public final int getLow() {
        return 0;
    }
    
    public final boolean getScoutSearch() {
        return false;
    }
    
    public final int getBestMoveForHash() {
        return 0;
    }
    
    public final int getThreatExtend() {
        return 0;
    }
    
    public final int getHashFlag() {
        return 0;
    }
    
    public final boolean getMoveIsLegal() {
        return false;
    }
    
    public MoveSearchResult(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.search.SearchPath bestPath, int low, boolean scoutSearch, int bestMoveForHash, int threatExtend, int hashFlag, boolean moveIsLegal) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.search.SearchPath component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    public final int component6() {
        return 0;
    }
    
    public final boolean component7() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.MoveSearchResult copy(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.search.SearchPath bestPath, int low, boolean scoutSearch, int bestMoveForHash, int threatExtend, int hashFlag, boolean moveIsLegal) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010!\u001a\u00020\"J\u000e\u0010#\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\u0004J\u000e\u0010%\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\nJ\u000e\u0010&\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\u0004J\u000e\u0010\'\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\u0004J\u000e\u0010(\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\u0016J\u000e\u0010)\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\u0016J\u000e\u0010*\u001a\u00020\u00002\u0006\u0010$\u001a\u00020\u0004R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0006\"\u0004\b\u0011\u0010\bR\u001a\u0010\u0012\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0006\"\u0004\b\u0014\u0010\bR\u001a\u0010\u0015\u001a\u00020\u0016X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u001a\u0010\u001b\u001a\u00020\u0016X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u0018\"\u0004\b\u001d\u0010\u001aR\u001a\u0010\u001e\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u0006\"\u0004\b \u0010\b\u00a8\u0006+"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/MoveSearchResult$Builder;", "", "()V", "bestMoveForHash", "", "getBestMoveForHash", "()I", "setBestMoveForHash", "(I)V", "bestPath", "Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "getBestPath", "()Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "setBestPath", "(Lcom/netsensia/rivalchess/engine/core/search/SearchPath;)V", "hashFlag", "getHashFlag", "setHashFlag", "low", "getLow", "setLow", "moveIsLegal", "", "getMoveIsLegal", "()Z", "setMoveIsLegal", "(Z)V", "scoutSearch", "getScoutSearch", "setScoutSearch", "threatExtend", "getThreatExtend", "setThreatExtend", "build", "Lcom/netsensia/rivalchess/engine/core/MoveSearchResult;", "withBestMoveForHash", "v", "withBestPath", "withHashFlag", "withLow", "withMoveIsLegal", "withScoutSearch", "withThreatExtend", "rivalchess-engine"})
    public static final class Builder {
        @org.jetbrains.annotations.NotNull()
        private com.netsensia.rivalchess.engine.core.search.SearchPath bestPath;
        private int low = -2147483647;
        private boolean scoutSearch = false;
        private int bestMoveForHash = 0;
        private int threatExtend = 0;
        private int hashFlag;
        private boolean moveIsLegal = false;
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.search.SearchPath getBestPath() {
            return null;
        }
        
        public final void setBestPath(@org.jetbrains.annotations.NotNull()
        com.netsensia.rivalchess.engine.core.search.SearchPath p0) {
        }
        
        public final int getLow() {
            return 0;
        }
        
        public final void setLow(int p0) {
        }
        
        public final boolean getScoutSearch() {
            return false;
        }
        
        public final void setScoutSearch(boolean p0) {
        }
        
        public final int getBestMoveForHash() {
            return 0;
        }
        
        public final void setBestMoveForHash(int p0) {
        }
        
        public final int getThreatExtend() {
            return 0;
        }
        
        public final void setThreatExtend(int p0) {
        }
        
        public final int getHashFlag() {
            return 0;
        }
        
        public final void setHashFlag(int p0) {
        }
        
        public final boolean getMoveIsLegal() {
            return false;
        }
        
        public final void setMoveIsLegal(boolean p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withBestPath(@org.jetbrains.annotations.NotNull()
        com.netsensia.rivalchess.engine.core.search.SearchPath v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withLow(int v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withScoutSearch(boolean v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withBestMoveForHash(int v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withThreatExtend(int v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withHashFlag(int v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult.Builder withMoveIsLegal(boolean v) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.netsensia.rivalchess.engine.core.MoveSearchResult build() {
            return null;
        }
        
        public Builder() {
            super();
        }
    }
}