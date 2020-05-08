package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.InvalidMoveException;

public interface StaticExchangeEvaluator {

    int staticExchangeEvaluation(EngineChessBoard board, EngineMove move) throws InvalidMoveException;
}
