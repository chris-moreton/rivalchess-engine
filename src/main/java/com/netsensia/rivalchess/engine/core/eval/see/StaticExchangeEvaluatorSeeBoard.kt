package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.BITBOARD_BK
import com.netsensia.rivalchess.engine.core.BITBOARD_WK
import com.netsensia.rivalchess.engine.core.board.*
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece

class StaticExchangeEvaluatorSeeBoard : StaticExchangeEvaluator {

    @Throws(InvalidMoveException::class)
    override fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int {
        val captureSquare = move.compact and 63
        val seeBoard = SeeBoard(board)
        val materialBalance = materialBalanceFromMoverPerspective(seeBoard)

        if (board.makeMove(move, true)) {
            seeBoard.makeMove(move)
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

            val kingBitboard = if (seeBoard.mover == Colour.WHITE) BITBOARD_BK else BITBOARD_WK
            if (seeBoard.bitboardMap[kingBitboard] == 0L) {
                seeBoard.unMakeMove()
                return bestScore + pieceValue(Piece.KING)
            }

            val seeScore = -seeSearch(seeBoard, captureSquare)
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