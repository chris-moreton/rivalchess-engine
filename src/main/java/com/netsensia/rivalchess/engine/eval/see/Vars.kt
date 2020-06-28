package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.consts.*

val enPassantHistory = LongArray(20)
val moveHistory = arrayOfNulls<Array<LongArray>>(20)
val whiteBitboardIndexes = intArrayOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
val blackBitboardIndexes = intArrayOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)