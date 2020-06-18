package com.netsensia.rivalchess.consts

import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant

const val FEN_START_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

const val BITBOARD_NONE = -1
const val BITBOARD_WP = 0
const val BITBOARD_WN = 1
const val BITBOARD_WB = 2
const val BITBOARD_WQ = 3
const val BITBOARD_WK = 4
const val BITBOARD_WR = 5
const val BITBOARD_BP = 6
const val BITBOARD_BN = 7
const val BITBOARD_BB = 8
const val BITBOARD_BQ = 9
const val BITBOARD_BK = 10
const val BITBOARD_BR = 11
const val BITBOARD_ALL = 12
const val BITBOARD_FRIENDLY = 13
const val BITBOARD_ENEMY = 14
const val BITBOARD_ENPASSANTSQUARE = 15
const val BITBOARD_COUNT = 16

val whiteBitboardTypes = intArrayOf(BITBOARD_WP, BITBOARD_WN, BITBOARD_WB, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WR)
val blackBitboardTypes = intArrayOf(BITBOARD_BP, BITBOARD_BN, BITBOARD_BB, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BR)

fun squareOccupantFromBitboardType(bitboardType: Int): SquareOccupant {
    when (bitboardType) {
        BITBOARD_WP -> return SquareOccupant.WP
        BITBOARD_WB -> return SquareOccupant.WB
        BITBOARD_WN -> return SquareOccupant.WN
        BITBOARD_WR -> return SquareOccupant.WR
        BITBOARD_WQ -> return SquareOccupant.WQ
        BITBOARD_WK -> return SquareOccupant.WK
        BITBOARD_BP -> return SquareOccupant.BP
        BITBOARD_BB -> return SquareOccupant.BB
        BITBOARD_BN -> return SquareOccupant.BN
        BITBOARD_BR -> return SquareOccupant.BR
        BITBOARD_BQ -> return SquareOccupant.BQ
        BITBOARD_BK -> return SquareOccupant.BK
        else -> return SquareOccupant.NONE
    }
}
