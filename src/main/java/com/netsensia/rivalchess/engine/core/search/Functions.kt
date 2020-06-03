package com.netsensia.rivalchess.engine.core.search

fun moveNoScore(move: Int) = move and 0x00FFFFFF

fun moveSequence(moves: IntArray) = sequence {
    var i = -1;
    while (moveNoScore(moves[++i]) != 0) {
        yield (moves[i])
    }
}

fun moveCount(moves: IntArray) = moves.indexOfFirst { it == 0 }

fun swapElements(a: IntArray, i1: Int, i2: Int) {
    val tempScore: Int = a[i1]
    a[i1] = a[i2]
    a[i2] = tempScore
}