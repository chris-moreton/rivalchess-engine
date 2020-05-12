package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.engine.core.EngineChessBoard

class BitboardData(board: EngineChessBoard) {
    val whitePawns: Long by lazy { board.getBitboard(BitboardType.WP) }
    val whiteBishops: Long by lazy { board.getBitboard(BitboardType.WB) }
    val whiteKnights: Long by lazy { board.getBitboard(BitboardType.WN) }
    val whiteKing: Long by lazy { board.getBitboard(BitboardType.WK) }
    val whiteQueens: Long by lazy { board.getBitboard(BitboardType.WQ) }
    val whiteRooks: Long by lazy { board.getBitboard(BitboardType.WR) }
    val blackPawns: Long by lazy { board.getBitboard(BitboardType.BP) }
    val blackBishops: Long by lazy { board.getBitboard(BitboardType.BB) }
    val blackKnights: Long by lazy { board.getBitboard(BitboardType.BN) }
    val blackKing: Long by lazy { board.getBitboard(BitboardType.BK) }
    val blackQueens: Long by lazy { board.getBitboard(BitboardType.BQ) }
    val blackRooks: Long by lazy { board.getBitboard(BitboardType.BR) }
    val enemy: Long by lazy { board.getBitboard(BitboardType.ENEMY) }
    val friendly: Long by lazy { board.getBitboard(BitboardType.FRIENDLY) }
    val all: Long by lazy { board.getBitboard(BitboardType.ALL) }
    val enPassantSquare: Long by lazy { board.getBitboard(BitboardType.ENPASSANTSQUARE) }
}
