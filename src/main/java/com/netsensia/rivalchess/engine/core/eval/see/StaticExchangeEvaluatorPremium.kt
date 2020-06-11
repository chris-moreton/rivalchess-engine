package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.BITBOARD_BK
import com.netsensia.rivalchess.engine.core.BITBOARD_WK
import com.netsensia.rivalchess.engine.core.board.*
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece

class StaticExchangeEvaluatorPremium : StaticExchangeEvaluator {

    @Throws(InvalidMoveException::class)
    override fun staticExchangeEvaluation(board: EngineBoard, move: EngineMove): Int {
        val captureSquare = move.compact and 63
        val seeBoard = SeeBoard(board)
        val materialBalance = materialBalanceFromMoverPerspective(seeBoard)

        //println(board.getFen())
        if (board.makeMove(move, true)) {
            seeBoard.makeMove(move)
            //println("seeSearchTop: " + move.from() + "-" + move.to())
            val seeValue = -seeSearch(seeBoard, captureSquare, board) - materialBalance
            board.unMakeMove()
            seeBoard.unMakeMove()
            return seeValue
        }
        return -Int.MAX_VALUE
    }

    @Throws(InvalidMoveException::class)
    fun seeSearch(seeBoard: SeeBoard, captureSquare: Int, board: EngineBoard): Int {

        var board = board
        var bestScore = materialBalanceFromMoverPerspective(seeBoard)
        val oldMoves = board.moveGenerator()
                .generateLegalQuiesceMoves(false).getMoveArray()
                .filter {it and 63 == captureSquare}.map { EngineMove(it) }
                .sortedByDescending { it.compact }
                .toList()
        val newMoves = seeBoard.generateCaptureMovesOnSquare(captureSquare).sortedByDescending { it.compact }.toList()

        for (move in seeBoard.generateCaptureMovesOnSquare(captureSquare)) {
            seeBoard.makeMove(move)
            board.makeMove(move, true)
            require(oldMoves.equals(newMoves))

            val kingBitboard = if (seeBoard.mover == Colour.WHITE) BITBOARD_BK else BITBOARD_WK
            if (seeBoard.bitboardMap[kingBitboard] == 0L) {
                seeBoard.unMakeMove()
                board.unMakeMove()
                return pieceValue(Piece.KING)
            }
            //println("seeSearch: " + move.from() + "-" + move.to())
            val seeScore = -seeSearch(seeBoard, captureSquare, board)
            seeBoard.unMakeMove()
            board.unMakeMove()
            bestScore = seeScore.coerceAtLeast(bestScore)
        }

        return bestScore
    }

    private fun materialBalanceFromMoverPerspective(board: EngineBoard) =
            if (board.mover == Colour.WHITE)
                (board.whitePawnValues + board.whitePieceValues - (board.blackPawnValues + board.blackPieceValues)) else
                (board.blackPawnValues + board.blackPieceValues - (board.whitePawnValues + board.whitePieceValues))

    private fun materialBalanceFromMoverPerspective(seeBoard: SeeBoard) =
        if (seeBoard.mover == Colour.WHITE)
            (seeBoard.whitePawnValues + seeBoard.whitePieceValues - (seeBoard.blackPawnValues + seeBoard.blackPieceValues)) else
            (seeBoard.blackPawnValues + seeBoard.blackPieceValues - (seeBoard.whitePawnValues + seeBoard.whitePieceValues))

}