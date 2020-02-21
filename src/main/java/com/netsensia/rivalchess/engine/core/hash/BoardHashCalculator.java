package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;

public interface BoardHashCalculator {

    public long getHash(EngineChessBoard engineChessBoard);
    public long getPawnHash(EngineChessBoard engineChessBoard);

}
