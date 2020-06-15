package com.netsensia.rivalchess.engine.core.eval.see;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0005H\u0002J\u0010\u0010\t\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0002J\u0016\u0010\n\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0005J\u0018\u0010\u000b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\u0005H\u0016\u00a8\u0006\r"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluatorPremium;", "Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluator;", "()V", "getCaptureMovesOnSquare", "Lkotlin/sequences/Sequence;", "", "board", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "captureSquare", "materialBalanceFromMoverPerspective", "seeSearch", "staticExchangeEvaluation", "move", "rivalchess-engine"})
public final class StaticExchangeEvaluatorPremium implements com.netsensia.rivalchess.engine.core.eval.see.StaticExchangeEvaluator {
    
    @java.lang.Override()
    public int staticExchangeEvaluation(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int move) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    public final int seeSearch(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int captureSquare) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    private final int materialBalanceFromMoverPerspective(com.netsensia.rivalchess.engine.core.board.EngineBoard board) {
        return 0;
    }
    
    private final kotlin.sequences.Sequence<java.lang.Integer> getCaptureMovesOnSquare(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int captureSquare) {
        return null;
    }
    
    public StaticExchangeEvaluatorPremium() {
        super();
    }
}