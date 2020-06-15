package com.netsensia.rivalchess.engine.core.eval.see;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J \u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0018\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0004H\u0017\u00a8\u0006\u000e"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluatorSeeBoard;", "Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluator;", "()V", "materialBalanceFromMoverPerspective", "", "seeBoard", "Lcom/netsensia/rivalchess/engine/core/eval/see/SeeBoard;", "seeSearch", "captureSquare", "materialBalance", "staticExchangeEvaluation", "board", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "move", "rivalchess-engine"})
public final class StaticExchangeEvaluatorSeeBoard implements com.netsensia.rivalchess.engine.core.eval.see.StaticExchangeEvaluator {
    
    @kotlin.ExperimentalStdlibApi()
    @java.lang.Override()
    public int staticExchangeEvaluation(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int move) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    @kotlin.ExperimentalStdlibApi()
    public final int seeSearch(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.see.SeeBoard seeBoard, int captureSquare, int materialBalance) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    private final int materialBalanceFromMoverPerspective(com.netsensia.rivalchess.engine.core.eval.see.SeeBoard seeBoard) {
        return 0;
    }
    
    public StaticExchangeEvaluatorSeeBoard() {
        super();
    }
}