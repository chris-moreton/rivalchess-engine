package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.consts.BITBOARD_BK
import com.netsensia.rivalchess.consts.BITBOARD_WK
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.makeMove
import com.netsensia.rivalchess.engine.board.unMakeMove
import com.netsensia.rivalchess.engine.eval.ALLOW_PIECE_VALUE_MODIFICATIONS
import com.netsensia.rivalchess.engine.eval.DEFAULT_VALUE_KING
import com.netsensia.rivalchess.engine.eval.pieceValue
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

        var bestScore = materialBalance
        seeBoard.getLvaCaptureMove(captureSquare).also { captureMove ->
            if (captureMove != 0) {
               val materialGain = seeBoard.makeMove(captureMove)

               val opposingKingBitboard = if (seeBoard.mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK
               if (seeBoard.capturedPieceBitboardType == opposingKingBitboard) {
                   return bestScore + if (ALLOW_PIECE_VALUE_MODIFICATIONS) pieceValue(BITBOARD_WK) else DEFAULT_VALUE_KING
               }

               val seeScore = -seeSearch(seeBoard, captureSquare, -(materialBalance + materialGain))
               bestScore = seeScore.coerceAtLeast(bestScore)
           }
        }

        return bestScore
    }

    private fun materialBalanceFromMoverPerspective(board: EngineBoard) =
        if (board.mover == Colour.WHITE)
            (board.whitePieceValues + board.whitePawnValues - board.blackPieceValues - board.blackPawnValues) else
            (board.blackPieceValues + board.blackPawnValues - board.whitePieceValues - board.whitePawnValues)

}