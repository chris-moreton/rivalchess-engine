package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.enums.MoveOrder
import com.netsensia.rivalchess.model.Colour

fun Search.getHighScoreMove(board: EngineBoard, ply: Int, hashMove: Int): Int {
    if (moveOrderStatus[ply] === MoveOrder.NONE) {
        if (checkForHashMove(hashMove, ply)) return hashMove
        moveOrderStatus[ply] = MoveOrder.CAPTURES
        if (scoreFullWidthCaptures(ply) == 0) {
            // no captures, so move to next stage
            scoreFullWidthMoves(ply)
            moveOrderStatus[ply] = MoveOrder.ALL
        }
    }
    val move = getHighestScoringMoveFromArray(orderedMoves[ply])
    return if (move == 0 && moveOrderStatus[ply] === MoveOrder.CAPTURES) {
        // we move into here if we had some captures but they are now used up
        scoreFullWidthMoves(ply)
        moveOrderStatus[ply] = MoveOrder.ALL
        getHighestScoringMoveFromArray(orderedMoves[ply])
    } else move
}

private fun Search.checkForHashMove(hashMove: Int, ply: Int): Boolean {
    if (hashMove != 0) {
        var c = 0
        while (orderedMoves[ply][c] != 0) {
            if (orderedMoves[ply][c] == hashMove) {
                orderedMoves[ply][c] = -1
                return true
            }
            c++
        }
    }
    return false
}

fun Search.scoreFullWidthCaptures(ply: Int): Int {
    var movesScored = 0
    var i = -1
    while (orderedMoves[ply][++i] != 0) {
        if (orderedMoves[ply][i] != -1 && scoreCaptureMove(ply, i) > 0) movesScored++
    }
    return movesScored
}

fun Search.scoreCaptureMove(ply: Int, i: Int): Int {
    var score = 0
    val toSquare = toSquare(orderedMoves[ply][i])
    val isCapture = engineBoard.getBitboardTypeOfPieceOnSquare(toSquare, engineBoard.mover.opponent()) != BITBOARD_NONE ||
            (1L shl toSquare and engineBoard.getBitboard(BITBOARD_ENPASSANTSQUARE) != 0L &&
                    engineBoard.getBitboardTypeOfPieceOnSquare(fromSquare(orderedMoves[ply][i]), engineBoard.mover) in arrayOf(BITBOARD_WP, BITBOARD_BP))

    orderedMoves[ply][i] = moveNoScore(orderedMoves[ply][i])
    if (orderedMoves[ply][i] == mateKiller[ply]) {
        score = 126
    } else if (isCapture) {
        val see = staticExchangeEvaluator.staticExchangeEvaluation(engineBoard, orderedMoves[ply][i])

        score = if (see > 0) {
            110 + (see / 100)
        } else if (orderedMoves[ply][i] and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
            109
        } else if (see == 0) {
            107
        } else {
            scoreLosingCapturesWithWinningHistory(ply, i, orderedMoves[ply], toSquare)
        }
    } else if (orderedMoves[ply][i] and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
        score = 108
    }
    orderedMoves[ply][i] = orderedMoves[ply][i] or (127 - score shl 24)
    return score
}

fun Search.scoreLosingCapturesWithWinningHistory(ply: Int, i: Int, movesForSorting: IntArray, toSquare: Int): Int {
    val historyScore = historyScore(engineBoard.mover == Colour.WHITE, fromSquare(movesForSorting[i]), toSquare)
    return if (historyScore > 5) historyScore else scoreKillerMoves(ply, i, movesForSorting)
}

fun Search.scoreFullWidthMoves(ply: Int) {
    var i = 0
    while (orderedMoves[ply][i] != 0) {
        if (orderedMoves[ply][i] != -1) {
            val fromSquare = fromSquare(orderedMoves[ply][i])
            val toSquare = toSquare(orderedMoves[ply][i])
            orderedMoves[ply][i] = moveNoScore(orderedMoves[ply][i])

            val killerScore = scoreKillerMoves(ply, i, orderedMoves[ply])
            val historyScore = scoreHistoryHeuristic(engineBoard, killerScore, fromSquare, toSquare)

            val finalScore =
                    if (historyScore == 0)
                        (if (engineBoard.getBitboardTypeOfPieceOnSquare(toSquare, engineBoard.mover.opponent()) != BITBOARD_NONE) // losing capture
                            1 else 50 + scorePieceSquareValues(engineBoard, fromSquare, toSquare) / 2)
                    else historyScore

            orderedMoves[ply][i] = orderedMoves[ply][i] or (127 - finalScore shl 24)
        }
        i++
    }
}

fun Search.scoreHistoryHeuristic(board: EngineBoard, score: Int, fromSquare: Int, toSquare: Int) =
        if (score == 0 && historyMovesSuccess[if (board.mover == Colour.WHITE) 0 else 1][fromSquare][toSquare] > 0) {
            90 + historyScore(board.mover == Colour.WHITE, fromSquare, toSquare)
        } else score

fun Search.scoreKillerMoves(ply: Int, i: Int, movesForSorting: IntArray): Int {
    if (movesForSorting[i] == killerMoves[ply][0]) return 106
    if (movesForSorting[i] == killerMoves[ply][1]) return 105
    return 0
}

fun Search.historyScore(isWhite: Boolean, from: Int, to: Int): Int {
    val colourIndex = if (isWhite) 0 else 1
    val success = historyMovesSuccess[colourIndex][from][to]
    val total = success + historyMovesFail[colourIndex][from][to]
    return if (total > 0) success * 10 / total else 0
}
