package com.netsensia.rivalchess.engine.core.search

import com.netsensia.rivalchess.config.Extensions
import com.netsensia.rivalchess.config.FeatureFlag
import com.netsensia.rivalchess.config.SearchConfig

fun moveNoScore(move: Int) = move and 0x00FFFFFF

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
        if (depthRemaining > SearchConfig.NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE.value) SearchConfig.NULLMOVE_REDUCE_DEPTH.value + 1
        else SearchConfig.NULLMOVE_REDUCE_DEPTH.value

fun useScoutSearch(depth: Int, newExtensions: Int) =
        FeatureFlag.USE_PV_SEARCH.isActive && depth + (newExtensions / Extensions.FRACTIONAL_EXTENSION_FULL.value) >=
                SearchConfig.PV_MINIMUM_DISTANCE_FROM_LEAF.value