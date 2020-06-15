package com.netsensia.rivalchess.engine.core.eval.see;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0003H&\u00a8\u0006\u0007"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluator;", "", "staticExchangeEvaluation", "", "board", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "move", "rivalchess-engine"})
public abstract interface StaticExchangeEvaluator {
    
    public abstract int staticExchangeEvaluation(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int move) throws com.netsensia.rivalchess.exception.InvalidMoveException;
}