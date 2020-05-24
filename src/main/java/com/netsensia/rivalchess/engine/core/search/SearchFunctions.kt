package com.netsensia.rivalchess.engine.core.search

import com.netsensia.rivalchess.bitboards.EngineBitboards
import com.netsensia.rivalchess.model.Colour

fun getHighScoreMove(theseMoves: IntArray): Int {
    var bestMove = 0
    var bestIndex = -1
    var bestScore = Int.MAX_VALUE
    var c = 0
    while (theseMoves[c] != 0) {
        if (theseMoves[c] != -1 && theseMoves[c] < bestScore && theseMoves[c] shr 24 != 127) {
            // update best move found so far, but don't consider moves with no score
            bestScore = theseMoves[c]
            bestMove = theseMoves[c]
            bestIndex = c
        }
        c++
    }
    if (bestIndex != -1) theseMoves[bestIndex] = -1
    return bestMove and 0x00FFFFFF
}

fun inCheck(whiteKingSquare: Int, blackKingSquare: Int, mover: Colour) =
        if (mover == Colour.BLACK)
            EngineBitboards.instance.isSquareAttackedBy(blackKingSquare, Colour.WHITE)
        else EngineBitboards.instance.isSquareAttackedBy(whiteKingSquare, Colour.BLACK)
