package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.getScore
import com.netsensia.rivalchess.engine.board.isCapture
import com.netsensia.rivalchess.enums.MoveOrder
import com.netsensia.rivalchess.model.Colour

@kotlin.ExperimentalUnsignedTypes
fun Search.getHighScoreMove(ply: Int, hashMove: Int): Int {
    if (moveOrderStatus[ply] === MoveOrder.NONE) {
        if (hashMove != 0) {
            var c = 0
            while (orderedMoves[ply][c] != 0) {
                if (orderedMoves[ply][c] == hashMove) {
                    orderedMoves[ply][c] = -1
                    return hashMove
                }
                c++
            }
        }
        moveOrderStatus[ply] = MoveOrder.CAPTURES
        if (scoreFullWidthCaptures(ply) == 0) {
            // no captures, so move to next stage
            scoreFullWidthMoves(ply)
            moveOrderStatus[ply] = MoveOrder.ALL
        }
    }
    val move = getHighestScoringMoveFromArray(orderedMoves[ply])
    return if (move == 0 && moveOrderStatus[ply] === MoveOrder.CAPTURES) {
        // if we had some captures but they are now used up...
        scoreFullWidthMoves(ply)
        moveOrderStatus[ply] = MoveOrder.ALL
        getHighestScoringMoveFromArray(orderedMoves[ply])
    } else move
}

@kotlin.ExperimentalUnsignedTypes
fun Search.scoreFullWidthCaptures(ply: Int): Int {
    var movesScored = 0
    var i = -1
    while (orderedMoves[ply][++i] != 0) {
        if (orderedMoves[ply][i] != -1 && scoreCaptureMove(ply, i) > 0) movesScored++
    }
    return movesScored
}

@kotlin.ExperimentalUnsignedTypes
fun Search.scoreCaptureMove(ply: Int, i: Int): Int {
    var score = 0
    val toSquare = toSquare(orderedMoves[ply][i])
    val move = moveNoScore(orderedMoves[ply][i])

    if (move == mateKiller[ply]) score = 126
    else if (isCapture(engineBoard, move)) staticExchangeEvaluator.staticExchangeEvaluation(engineBoard, move).also {
        score = if (it > 0) (110 + (it / 100)) else if (it == 0) 107 else scoreLosingCapturesWithWinningHistory(engineBoard, ply, move, toSquare)
    }
    else if (orderedMoves[ply][i] and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) score = 108

    orderedMoves[ply][i] = move or ((127 - score).coerceAtLeast(0) shl 24)
    return score
}

@kotlin.ExperimentalUnsignedTypes
private fun isCapture(board: EngineBoard, move: Int): Boolean {
    val toSquare = toSquare(move)
    return board.getBitboardTypeOfPieceOnSquare(toSquare, board.mover.opponent()) != BITBOARD_NONE ||
            (1L shl toSquare and board.getBitboard(BITBOARD_ENPASSANTSQUARE) != 0L &&
                    board.getBitboardTypeOfPieceOnSquare(fromSquare(move), board.mover) in arrayOf(BITBOARD_WP, BITBOARD_BP))
}

@kotlin.ExperimentalUnsignedTypes
fun Search.scoreLosingCapturesWithWinningHistory(board: EngineBoard, ply: Int, move: Int, toSquare: Int): Int {
    val historyScore = historyScore(board.mover, fromSquare(move), toSquare)
    return if (historyScore > 5) historyScore else scoreKillerMoves(ply, move)
}

@kotlin.ExperimentalUnsignedTypes
fun Search.scoreFullWidthMoves(ply: Int) {
    var i = 0
    while (orderedMoves[ply][i] != 0) {
        if (orderedMoves[ply][i] != -1) {
            val move = moveNoScore(orderedMoves[ply][i])

            val fromSquare = fromSquare(move)
            val toSquare = toSquare(move)

            val killerScore = scoreKillerMoves(ply, move)
            val finalScore =
                if (killerScore > 0) {
                    killerScore
                } else {
                    val historyScore = historyScore(engineBoard.mover, fromSquare, toSquare)
                    if (historyScore > 0) {
                        90 + historyScore
                    } else {
                        50 + scorePieceSquareValues(fromSquare, toSquare) / 2
                    }
                }

            orderedMoves[ply][i] = move or ((127 - finalScore).coerceAtLeast(0) shl 24)
        }
        i++
    }
}

@kotlin.ExperimentalUnsignedTypes
private fun Search.hasHistorySuccess(fromSquare: Int, toSquare: Int): Boolean {
    return historyMovesSuccess[if (engineBoard.mover == Colour.WHITE) 0 else 1][fromSquare][toSquare] > 0
}

@kotlin.ExperimentalUnsignedTypes
fun Search.scoreKillerMoves(ply: Int, move: Int): Int {
    if (move == killerMoves[ply][0]) return 106
    if (move == killerMoves[ply][1]) return 105
    return 0
}

@kotlin.ExperimentalUnsignedTypes
fun Search.historyScore(mover: Colour, from: Int, to: Int): Int {
    val colourIndex = if (mover == Colour.WHITE) 0 else 1
    val success = historyMovesSuccess[colourIndex][from][to]
    val total = success + historyMovesFail[colourIndex][from][to]

    return if (total > 0) ((success.toDouble() / total.toDouble()) * 10).toInt() else 0
}

@kotlin.ExperimentalUnsignedTypes
fun Search.scoreQuiesceMoves(ply: Int) {
    var moveCount = 0
    var i = 0
    while (orderedMoves[ply][i] != 0) {
        var move = orderedMoves[ply][i]
        val isCapture = engineBoard.isCapture(move)
        move = moveNoScore(move)
        val score = engineBoard.getScore(move, isCapture, staticExchangeEvaluator)
        if (score > 0) orderedMoves[ply][moveCount++] = move or (127 - score shl 24)
        i++
    }
    orderedMoves[ply][moveCount] = 0
}