package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException

interface StaticExchangeEvaluator {
    @Throws(InvalidMoveException::class)
    fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int
}