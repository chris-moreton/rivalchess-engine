package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.config.MATE_SCORE_START
import com.netsensia.rivalchess.config.VALUE_MATE
import com.netsensia.rivalchess.engine.board.*

@kotlin.ExperimentalUnsignedTypes
fun Search.solveForMate(board: EngineBoard, maxPly: Int): SearchPath? {
    abortingSearch = false
    for (i in 1..maxPly) {
        if (!abortingSearch) {
            val path = solveForMateSearcher(board, 0, i)
            if (path.score > MATE_SCORE_START) return path
        }
    }
    return null
}

@kotlin.ExperimentalUnsignedTypes
fun Search.solveForMateSearcher(board: EngineBoard, ply: Int, maxPly: Int): SearchPath {
    nodes ++
    var numMoves = 0
    var numLegalMoves = 0
    val bestPath = SearchPath().withScore(-Int.MAX_VALUE)

    if (ply == maxPly) return SearchPath().withScore(if (board.isGameOver() && board.isCheck(board.mover)) -VALUE_MATE else 0)

    val moves = board.moveGenerator().generateLegalMoves().moves

    abortIfTimeIsUp()

    moveSequence(moves).forEach {
        val move = moveNoScore(it)

        if (abortingSearch) return SearchPath()

        if (board.makeMove(move)) {
            numLegalMoves ++

            val newPath = solveForMateSearcher(board, ply + 1, maxPly).also { path ->
                path.score = adjustedMateScore(-path.score)
            }

            if (abortingSearch) return SearchPath()

            if (newPath.score > bestPath.score) {
                bestPath.setPath(move, newPath)

                if (bestPath.score >= MATE_SCORE_START) {
                    board.unMakeMove()
                    return bestPath
                }
            }

            board.unMakeMove()
        }
        numMoves++
    }

    if (numLegalMoves == 0) {
        return SearchPath().withScore(if (board.isCheck(board.mover)) -VALUE_MATE else 0)
    }

    if (abortingSearch) return SearchPath()

    return bestPath
}