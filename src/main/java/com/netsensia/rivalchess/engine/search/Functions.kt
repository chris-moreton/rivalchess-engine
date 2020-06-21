package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*

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
    while (moveNoScore(moves[++i]) != 0) {
        yield (moves[i])
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
