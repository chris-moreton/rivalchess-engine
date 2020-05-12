package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.util.blackPawnAttacks
import com.netsensia.rivalchess.bitboards.util.whitePawnAttacks

class Attacks(private val bitboardData: BitboardData, private val pieceSquareLists: PieceSquareLists) {
    val whitePawns by lazy { whitePawnAttacks(bitboardData.whitePawns) }
    val blackPawns by lazy { blackPawnAttacks(bitboardData.blackPawns) }
    val whiteRooks by lazy { rookAttackList(bitboardData, pieceSquareLists.whiteRooks) }
    val whiteBishops by lazy { bishopAttackList(bitboardData, pieceSquareLists.whiteBishops) }
    val whiteQueens by lazy { queenAttackList(bitboardData, pieceSquareLists.whiteQueens) }
    val whiteKnights by lazy { knightAttackList(pieceSquareLists.whiteKnights) }
    val blackRooks by lazy { rookAttackList(bitboardData, pieceSquareLists.blackRooks) }
    val blackBishops by lazy { bishopAttackList(bitboardData, pieceSquareLists.blackBishops) }
    val blackQueens by lazy { queenAttackList(bitboardData, pieceSquareLists.blackQueens) }
    val blackKnights by lazy { knightAttackList(pieceSquareLists.blackKnights) }

}

