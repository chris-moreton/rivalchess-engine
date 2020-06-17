package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.config.*

fun moveNoScore(move: Int) = move and 0x00FFFFFF

fun fromSquare(move: Int) = (move ushr 16) and 63

fun toSquare(move: Int) = move and 63

fun moveSequence(moves: IntArray) = sequence {
    var i = -1;
    while (moveNoScore(moves[++i]) != 0) {
        yield (moves[i])
    }
}

fun applyToMoves(moves: IntArray, fn: (Int) -> SearchPath) {
    var i = -1;
    while (moveNoScore(moves[++i]) != 0) {
        fn (moves[i])
    }
}

fun moveCount(moves: IntArray) = moves.indexOfFirst { it == 0 }

fun swapElements(a: IntArray, i1: Int, i2: Int) {
    a[i1] = a[i2].also{ a[i2] = a[i1] }
}

fun nullMoveReduceDepth(depthRemaining: Int) =
        if (depthRemaining > NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE) NULLMOVE_REDUCE_DEPTH + 1 else NULLMOVE_REDUCE_DEPTH

fun useScoutSearch(depth: Int, newExtensions: Int) =
        USE_PV_SEARCH && depth + (newExtensions / FRACTIONAL_EXTENSION_FULL) >= PV_MINIMUM_DISTANCE_FROM_LEAF