package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.util.squareList

class PieceSquareLists(private val bitboardData: BitboardData) {
    val whitePawns by lazy { squareList(bitboardData.whitePawns) }
    val whiteRooks by lazy { squareList(bitboardData.whiteRooks) }
    val whiteBishops by lazy { squareList(bitboardData.whiteBishops) }
    val whiteKnights by lazy { squareList(bitboardData.whiteKnights) }
    val whiteQueens by lazy { squareList(bitboardData.whiteQueens) }
    val blackPawns by lazy { squareList(bitboardData.blackPawns) }
    val blackRooks by lazy { squareList(bitboardData.blackRooks) }
    val blackBishops by lazy { squareList(bitboardData.blackBishops) }
    val blackKnights by lazy { squareList(bitboardData.blackKnights) }
    val blackQueens  by lazy { squareList(bitboardData.blackQueens) }
}