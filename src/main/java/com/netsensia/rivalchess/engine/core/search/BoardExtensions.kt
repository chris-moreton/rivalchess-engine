package com.netsensia.rivalchess.engine.core.search

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluator
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Piece

@Throws(InvalidMoveException::class)
fun EngineBoard.getScore(move: Int, includeChecks: Boolean, isCapture: Boolean, staticExchangeEvaluator: StaticExchangeEvaluator): Int {
    var score = 0
    val promotionMask = move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    if (isCapture) {
        val see: Int = staticExchangeEvaluator.staticExchangeEvaluation(this, EngineMove(move))
        if (see > 0) {
            score = 100 + (see.toDouble() / pieceValue(Piece.QUEEN) * 10).toInt()
        }
        if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
            score += 9
        }
    } else if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
        score = 116
    } else if (includeChecks) {
        score = 100
    }
    return score
}

