package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard

class BitboardData(board: EngineBoard) {
    @JvmField
    val whitePawns: Long = board.getBitboard(BITBOARD_WP)
    @JvmField
    val whiteBishops: Long = board.getBitboard(BITBOARD_WB)
    @JvmField
    val whiteKnights: Long = board.getBitboard(BITBOARD_WN)
    @JvmField
    val whiteKing: Long = board.getBitboard(BITBOARD_WK)
    @JvmField
    val whiteQueens: Long = board.getBitboard(BITBOARD_WQ)
    @JvmField
    val whiteRooks: Long = board.getBitboard(BITBOARD_WR)
    @JvmField
    val blackPawns: Long = board.getBitboard(BITBOARD_BP)
    @JvmField
    val blackBishops: Long = board.getBitboard(BITBOARD_BB)
    @JvmField
    val blackKnights: Long = board.getBitboard(BITBOARD_BN)
    @JvmField
    val blackKing: Long = board.getBitboard(BITBOARD_BK)
    @JvmField
    val blackQueens: Long = board.getBitboard(BITBOARD_BQ)
    @JvmField
    val blackRooks: Long = board.getBitboard(BITBOARD_BR)
    @JvmField
    val enemy: Long = board.getBitboard(BITBOARD_ENEMY)
    @JvmField
    val friendly: Long = board.getBitboard(BITBOARD_FRIENDLY)
    @JvmField
    val all: Long = board.getBitboard(BITBOARD_ALL)
}
