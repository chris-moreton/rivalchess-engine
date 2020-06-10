package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.board.*
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour

class StaticExchangeEvaluatorPremium : StaticExchangeEvaluator {

    @Throws(InvalidMoveException::class)
    override fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int {
        val captureSquare = move.compact and 63

        //println(board.getFen())
        if (board.makeMove(move)) {
            //println("seeSearchTop: " + move.from() + "-" + move.to())
            val seeBoard = SeeBoard(board)
            val materialBalance = materialBalanceFromMoverPerspective(seeBoard)
            val seeValue = -seeSearch(seeBoard, captureSquare) - materialBalance
            board.unMakeMove()
            return seeValue
        }
        return -Int.MAX_VALUE
    }

    @Throws(InvalidMoveException::class)
    fun seeSearch(seeBoard: SeeBoard, captureSquare: Int): Int {

        var bestScore = materialBalanceFromMoverPerspective(seeBoard)

        for (move in seeBoard.generateCaptureMovesOnSquare(captureSquare)) {
            seeBoard.makeMove(move)
            //println("seeSearch: " + move.from() + "-" + move.to())
            val seeScore = -seeSearch(seeBoard, captureSquare)
            seeBoard.unMakeMove()
            bestScore = seeScore.coerceAtLeast(bestScore)
        }

        return bestScore
    }

    private fun materialBalanceFromMoverPerspective(seeBoard: SeeBoard) =
        if (seeBoard.mover == Colour.WHITE)
            (seeBoard.whitePawnValues + seeBoard.whitePieceValues - (seeBoard.blackPawnValues + seeBoard.blackPieceValues)) else
            (seeBoard.blackPawnValues + seeBoard.blackPieceValues - (seeBoard.whitePawnValues + seeBoard.whitePieceValues))

}