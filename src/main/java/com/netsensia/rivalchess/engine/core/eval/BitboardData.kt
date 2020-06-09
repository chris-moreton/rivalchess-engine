package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard

class BitboardData(board: EngineBoard) {
    val whitePawns: Long = board.getBitboard(BITBOARD_WP)
    val whiteBishops: Long = board.getBitboard(BITBOARD_WB)
    val whiteKnights: Long = board.getBitboard(BITBOARD_WN)
    val whiteKing: Long = board.getBitboard(BITBOARD_WK)
    val whiteQueens: Long = board.getBitboard(BITBOARD_WQ)
    val whiteRooks: Long = board.getBitboard(BITBOARD_WR)
    val blackPawns: Long = board.getBitboard(BITBOARD_BP)
    val blackBishops: Long = board.getBitboard(BITBOARD_BB)
    val blackKnights: Long = board.getBitboard(BITBOARD_BN)
    val blackKing: Long = board.getBitboard(BITBOARD_BK)
    val blackQueens: Long = board.getBitboard(BITBOARD_BQ)
    val blackRooks: Long = board.getBitboard(BITBOARD_BR)
    val enemy: Long = board.getBitboard(BITBOARD_ENEMY)
    val friendly: Long = board.getBitboard(BITBOARD_FRIENDLY)
    val all: Long = board.getBitboard(BITBOARD_ALL)
}
