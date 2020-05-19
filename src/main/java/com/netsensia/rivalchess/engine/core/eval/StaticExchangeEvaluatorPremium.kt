package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.config.Limit
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import java.util.*

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

        val moves = getCaptureMovesOnSquare(board, captureSquare)

        var bestScore = materialBalanceFromMoverPerspective(board)

        for (move in moves) {
            if (board.makeMove(move)) {
                val seeScore = -seeSearch(board, captureSquare)
                board.unMakeMove()
                bestScore = seeScore.coerceAtLeast(bestScore)
            }
        }
        return bestScore
    }

    private fun materialBalanceFromMoverPerspective(board: EngineBoard): Int {
        val whiteMaterial = board.whitePawnValues + board.whitePieceValues
        val blackMaterial = board.blackPawnValues + board.blackPieceValues
        return if (board.mover == Colour.WHITE) {
            whiteMaterial - blackMaterial
        } else blackMaterial - whiteMaterial
    }

    private fun getCaptureMovesOnSquare(board: EngineBoard, captureSquare: Int): List<EngineMove> {
        val moves = board.getQuiesceMoveArray(false)
        val moveList: MutableList<EngineMove> = ArrayList()
        var moveNum = 0
        var move = moves[moveNum]
        while (move != 0) {
            if (move and 63 == captureSquare) {
                moveList.add(EngineMove(move))
            }
            move = moves[++moveNum]
        }
        return moveList
    }
}