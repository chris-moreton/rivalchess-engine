package com.netsensia.rivalchess.engine.core.search

fun moveNoScore(move: Int) = move and 0x00FFFFFF

fun moveSequence(moves: IntArray) = sequence {
    var i = -1;
    while (moveNoScore(moves[++i]) != 0) {
        yield (moves[i])
    }
}

fun moveCount(moves: IntArray) = moves.indexOfFirst { it == 0 }
