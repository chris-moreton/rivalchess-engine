package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.consts.BITBOARD_BK
import com.netsensia.rivalchess.consts.BITBOARD_WK
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.makeMove
import com.netsensia.rivalchess.engine.board.unMakeMove
import com.netsensia.rivalchess.engine.eval.VALUE_KING
import com.netsensia.rivalchess.engine.search.toSquare
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour

class StaticExchangeEvaluatorSeeBoard : StaticExchangeEvaluator {

    @Throws(InvalidMoveException::class)
    override fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int {
        val seeBoard = SeeBoard(board)

        if (board.makeMove(move, false, false)) {
            val materialBalance = materialBalanceFromMoverPerspective(seeBoard)
            val captureSquare = toSquare(move.compact)
            val materialGain = seeBoard.makeMove(move.compact)
            val seeValue = -seeSearch(seeBoard, captureSquare, -(materialBalance + materialGain)) - materialBalance
            board.unMakeMove(false)
            return seeValue
        }
        return -Int.MAX_VALUE
    }

    @Throws(InvalidMoveException::class)
    fun seeSearch(seeBoard: SeeBoard, captureSquare: Int, materialBalance: Int): Int {

        var bestScore = materialBalance

        for (move in seeBoard.generateCaptureMovesOnSquare(captureSquare)) {
            val materialGain = seeBoard.makeMove(move)

            if (seeBoard.capturedPieceBitboardType == if (seeBoard.mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK) {
                seeBoard.unMakeMove()
                return bestScore + VALUE_KING
            }

            val seeScore = -seeSearch(seeBoard, captureSquare, -(materialBalance + materialGain))
            seeBoard.unMakeMove()
            bestScore = seeScore.coerceAtLeast(bestScore)
        }

        return bestScore
    }

    private fun materialBalanceFromMoverPerspective(seeBoard: SeeBoard) =
        if (seeBoard.mover == Colour.WHITE)
            (seeBoard.whitePieceValues - seeBoard.blackPieceValues) else
            (seeBoard.blackPieceValues - seeBoard.whitePieceValues)

}