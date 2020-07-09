package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import kotlin.math.pow

fun moveNoScore(move: Int) = move and 0x00FFFFFF

fun fromSquare(move: Int) = (move ushr 16) and 63

fun toSquare(move: Int) = move and 63

fun promotionPiece(move: Int): Int {
    val to = move and 63
    return when (move and PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
        PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> if (to >= 56) BITBOARD_WQ else BITBOARD_BQ
        PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> if (to >= 56) BITBOARD_WR else BITBOARD_BR
        PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> if (to >= 56) BITBOARD_WN else BITBOARD_BN
        PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> if (to >= 56) BITBOARD_WB else BITBOARD_BB
        else -> BITBOARD_NONE
    }
}

fun moveSequence(moves: IntArray) = sequence {
    var i = -1;
    while (moveNoScore(moves[++i]) != 0) yield (moves[i])
}

fun applyToMoves(moves: IntArray, fn: (Int) -> SearchPath) {
    var i = -1;
    while (moveNoScore(moves[++i]) != 0) fn (moves[i])
}

fun moveCount(moves: IntArray) = moves.indexOfFirst { it == 0 }

fun swapElements(a: IntArray, i1: Int, i2: Int) {
    a[i1] = a[i2].also{ a[i2] = a[i1] }
}

fun nullMoveReduceDepth(depthRemaining: Int) = NULLMOVE_REDUCE_DEPTH + if (depthRemaining > NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE) 1 else 0

fun useScoutSearch(depth: Int, newExtensions: Int) = depth + (newExtensions / FRACTIONAL_EXTENSION_FULL) >= PV_MINIMUM_DISTANCE_FROM_LEAF

fun widenAspiration(attempt: Int) = (ASPIRATION_RADIUS * 2.0.pow(attempt.toDouble())).toInt()

fun adjustedMateScore(score: Int) = if (score > MATE_SCORE_START) score-1 else (if (score < -MATE_SCORE_START) score+1 else score)
