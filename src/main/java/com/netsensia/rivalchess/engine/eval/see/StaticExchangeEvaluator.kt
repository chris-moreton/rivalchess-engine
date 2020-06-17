package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException

interface StaticExchangeEvaluator {
    @Throws(InvalidMoveException::class)
    fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int
}