package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.board.makeMove
import com.netsensia.rivalchess.engine.core.board.unMakeMove
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour

class StaticExchangeEvaluatorPremium : StaticExchangeEvaluator {

    @Throws(InvalidMoveException::class)
    override fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int {
        val captureSquare = move.compact and 63
        val materialBalance = materialBalanceFromMoverPerspective(board)
        if (board.makeMove(move)) {
            val seeValue = -seeSearch(board, captureSquare) - materialBalance
            board.unMakeMove()
            return seeValue
        }
        return -Int.MAX_VALUE
    }

    @Throws(InvalidMoveException::class)
    fun seeSearch(board: EngineBoard, captureSquare: Int): Int {

        var bestScore = materialBalanceFromMoverPerspective(board)

        for (move in getCaptureMovesOnSquare(board, captureSquare)) {
            if (board.makeMove(move)) {
                val seeScore = -seeSearch(board, captureSquare)
                board.unMakeMove()
                bestScore = seeScore.coerceAtLeast(bestScore)
            }
        }

        return bestScore
    }

    private fun materialBalanceFromMoverPerspective(board: EngineBoard) =
            if (board.mover == Colour.WHITE)
                (board.whitePawnValues + board.whitePieceValues - (board.blackPawnValues + board.blackPieceValues)) else
                (board.blackPawnValues + board.blackPieceValues - (board.whitePawnValues + board.whitePieceValues))

    private fun getCaptureMovesOnSquare(board: EngineBoard, captureSquare: Int) = sequence {
        for (move in board.moveGenerator().generateLegalQuiesceMoves(false).getMoveArray()) {
            if (move == 0) break
            if (move and 63 == captureSquare) yield(EngineMove(move))
        }
    }
}