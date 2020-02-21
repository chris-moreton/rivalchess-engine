package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;

public class FastHashCalculator implements BoardHashCalculator {

    public long getHash(EngineChessBoard engineChessBoard) {
        return engineChessBoard.hashCode();
    }

    public long getPawnHash(EngineChessBoard engineChessBoard) {
        return engineChessBoard.pawnHashCode();
    }
}
