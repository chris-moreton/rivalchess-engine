package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.enums.MoveOrder
import com.netsensia.rivalchess.model.Colour

fun Search.getHighScoreMove(board: EngineBoard, ply: Int, hashMove: Int): Int {
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
        if (scoreFullWidthCaptures(board, ply) == 0) {
            // no captures, so move to next stage
            scoreFullWidthMoves(board, ply)
            moveOrderStatus[ply] = MoveOrder.ALL
        }
    }
    val move = getHighestScoringMoveFromArray(orderedMoves[ply])
    return if (move == 0 && moveOrderStatus[ply] === MoveOrder.CAPTURES) {
        // we move into here if we had some captures but they are now used up
        scoreFullWidthMoves(board, ply)
        moveOrderStatus[ply] = MoveOrder.ALL
        getHighestScoringMoveFromArray(orderedMoves[ply])
    } else move
}

fun Search.scoreFullWidthCaptures(board: EngineBoard, ply: Int): Int {
    var movesScored = 0
    var i = -1
    while (orderedMoves[ply][++i] != 0) {
        if (orderedMoves[ply][i] != -1 && scoreCaptureMove(ply, i, board) > 0) movesScored++
    }
    return movesScored
}

fun Search.scoreCaptureMove(ply: Int, i: Int, board: EngineBoard): Int {
    var score = 0
    val toSquare = toSquare(orderedMoves[ply][i])
    val isCapture = board.getBitboardTypeOfPieceOnSquare(toSquare, board.mover.opponent()) != BITBOARD_NONE ||
            (1L shl toSquare and board.getBitboard(BITBOARD_ENPASSANTSQUARE) != 0L &&
                    board.getBitboardTypeOfPieceOnSquare(fromSquare(orderedMoves[ply][i]), board.mover) in arrayOf(BITBOARD_WP, BITBOARD_BP))

    val move = moveNoScore(orderedMoves[ply][i])
    if (move == mateKiller[ply]) score = 126
    else if (isCapture) staticExchangeEvaluator.staticExchangeEvaluation(board, move).also {
            score = if (it > 0) (110 + (it / 100)) else if (it == 0) 107 else scoreLosingCapturesWithWinningHistory(board, ply, move, toSquare)
        }
    else if (orderedMoves[ply][i] and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) score = 108

    orderedMoves[ply][i] = move or (127 - score shl 24)
    return score
}

fun Search.moveGivesCheck(move: Int): Boolean {
    val toSquare = toSquare(move)
    val fromSquare = fromSquare(move)
    val capturePiece = engineBoard.getBitboardTypeOfPieceOnSquare(toSquare)
    val movePiece =  engineBoard.getBitboardTypeOfPieceOnSquare(fromSquare)
    val toMask = 1L shl toSquare
    val fromMask = 1L shl fromSquare
    if (capturePiece != BITBOARD_NONE) engineBoard.engineBitboards.xorPieceBitboard(capturePiece, toMask)
    engineBoard.engineBitboards.xorPieceBitboard(movePiece, toMask)
    engineBoard.engineBitboards.xorPieceBitboard(movePiece, fromMask)
    val enemyKing = if (engineBoard.mover == Colour.WHITE) engineBoard.blackKingSquare else engineBoard.whiteKingSquare
    val moveGivesCheck = (engineBoard.engineBitboards.isSquareAttackedBy(enemyKing, engineBoard.mover))
    if (capturePiece != BITBOARD_NONE) engineBoard.engineBitboards.xorPieceBitboard(capturePiece, toMask)
    engineBoard.engineBitboards.xorPieceBitboard(movePiece, toMask)
    engineBoard.engineBitboards.xorPieceBitboard(movePiece, fromMask)
    return moveGivesCheck
}

fun Search.scoreLosingCapturesWithWinningHistory(board: EngineBoard, ply: Int, move: Int, toSquare: Int): Int {
    val historyScore = historyScore(board.mover == Colour.WHITE, fromSquare(move), toSquare)
    return if (historyScore > 5) historyScore else scoreKillerMoves(ply, move)
}

fun Search.scoreFullWidthMoves(board: EngineBoard, ply: Int) {
    var i = 0
    while (orderedMoves[ply][i] != 0) {
        if (orderedMoves[ply][i] != -1) {
            val move = moveNoScore(orderedMoves[ply][i])

            val fromSquare = fromSquare(move)
            val toSquare = toSquare(move)

            val killerScore = scoreKillerMoves(ply, move)
            val historyScore = scoreHistoryHeuristic(board, killerScore, fromSquare, toSquare)

            val finalScore =
                    (if (historyScore == 0)
                        (if (board.getBitboardTypeOfPieceOnSquare(toSquare, board.mover.opponent()) != BITBOARD_NONE) // losing capture
                            1 else 50 + scorePieceSquareValues(board, fromSquare, toSquare) / 2)
                    else historyScore)

            orderedMoves[ply][i] = move or (127 - finalScore shl 24)
        }
        i++
    }
}

fun Search.scoreHistoryHeuristic(board: EngineBoard, score: Int, fromSquare: Int, toSquare: Int) =
        if (score == 0 && historyMovesSuccess[if (board.mover == Colour.WHITE) 0 else 1][fromSquare][toSquare] > 0) {
            90 + historyScore(board.mover == Colour.WHITE, fromSquare, toSquare)
        } else score

fun Search.scoreKillerMoves(ply: Int, move: Int): Int {
    if (move == killerMoves[ply][0]) return 106
    if (move == killerMoves[ply][1]) return 105
    return 0
}

fun Search.historyScore(isWhite: Boolean, from: Int, to: Int): Int {
    val colourIndex = if (isWhite) 0 else 1
    val success = historyMovesSuccess[colourIndex][from][to]
    val total = success + historyMovesFail[colourIndex][from][to]

    return if (total > 0) success * 10 / total else 0
}
