package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.consts.BITBOARD_BK
import com.netsensia.rivalchess.consts.BITBOARD_WK
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.makeMove
import com.netsensia.rivalchess.engine.board.unMakeMove
import com.netsensia.rivalchess.engine.eval.VALUE_KING
import com.netsensia.rivalchess.engine.search.toSquare
import com.netsensia.rivalchess.model.Colour

class StaticExchangeEvaluator {

    fun staticExchangeEvaluation(board: EngineBoard, compactMove: Int): Int {
        val seeBoard = SeeBoard(board)

        if (board.makeMove(compactMove, false, updateHash = false)) {
            val materialBalance = materialBalanceFromMoverPerspective(board)
            board.unMakeMove(false)

            return -seeSearch(
                    seeBoard,
                    toSquare(compactMove),
                    -(materialBalance + seeBoard.makeMove(compactMove))
            ) - materialBalance
        }
        return -Int.MAX_VALUE
    }

    fun seeSearch(seeBoard: SeeBoard, captureSquare: Int, materialBalance: Int): Int {

        var bestScore = materialBalance
        val moves = seeBoard.generateCaptureMovesOnSquare(captureSquare)

        for (move in moves) {
            if (move == 0) break
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

    private fun materialBalanceFromMoverPerspective(board: EngineBoard) =
        if (board.mover == Colour.WHITE)
            (board.whitePieceValues + board.whitePawnValues - board.blackPieceValues - board.blackPawnValues) else
            (board.blackPieceValues + board.blackPawnValues - board.whitePieceValues - board.whitePawnValues)

}