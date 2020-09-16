package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.makeMove
import com.netsensia.rivalchess.engine.board.unMakeMove
import com.netsensia.rivalchess.engine.search.toSquare
import com.netsensia.rivalchess.model.Colour

class StaticExchangeEvaluator {

    fun staticExchangeEvaluation(board: EngineBoard, compactMove: Int): Int {

        if (board.makeMove(compactMove, false, updateHash = false)) {
            val materialBalance = materialBalanceFromMoverPerspective(board)
            board.unMakeMove(false)
            val seeBoard = SeeBoard(board)
            return -seeSearch(seeBoard, toSquare(compactMove), -(materialBalance + seeBoard.makeMove(compactMove))) - materialBalance
        }
        return -Int.MAX_VALUE
    }

    private fun seeSearch(seeBoard: SeeBoard, captureSquare: Int, materialBalance: Int): Int {
        seeBoard.getLvaCaptureMove(captureSquare).also { captureMove ->
            if (captureMove == 0) return materialBalance
            seeBoard.makeMove(captureMove).also { materialGain ->
                if (materialGain == Int.MAX_VALUE) return Int.MAX_VALUE
                return (-seeSearch(seeBoard, captureSquare, -(materialBalance + materialGain))).coerceAtLeast(materialBalance)
            }
        }
    }

    private fun materialBalanceFromMoverPerspective(board: EngineBoard) =
            if (board.mover == Colour.WHITE)
                (board.whitePieceValues + board.whitePawnValues - board.blackPieceValues - board.blackPawnValues) else
                (board.blackPieceValues + board.blackPawnValues - board.whitePieceValues - board.whitePawnValues)

}