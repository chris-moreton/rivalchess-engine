package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.engine.core.board.EngineBoard

class BitboardData(board: EngineBoard) {
    val whitePawns: Long = board.getBitboard(BitboardType.WP)
    val whiteBishops: Long = board.getBitboard(BitboardType.WB)
    val whiteKnights: Long = board.getBitboard(BitboardType.WN)
    val whiteKing: Long = board.getBitboard(BitboardType.WK)
    val whiteQueens: Long = board.getBitboard(BitboardType.WQ)
    val whiteRooks: Long = board.getBitboard(BitboardType.WR)
    val blackPawns: Long = board.getBitboard(BitboardType.BP)
    val blackBishops: Long = board.getBitboard(BitboardType.BB)
    val blackKnights: Long = board.getBitboard(BitboardType.BN)
    val blackKing: Long = board.getBitboard(BitboardType.BK)
    val blackQueens: Long = board.getBitboard(BitboardType.BQ)
    val blackRooks: Long = board.getBitboard(BitboardType.BR)
    val enemy: Long = board.getBitboard(BitboardType.ENEMY)
    val friendly: Long = board.getBitboard(BitboardType.FRIENDLY)
    val all: Long = board.getBitboard(BitboardType.ALL)
}
