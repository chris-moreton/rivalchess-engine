package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.Bitboards

class Attacks(bitboardData: BitboardData, pieceSquareLists: PieceSquareLists) {
    val whitePawns = whitePawnAttacks(bitboardData.whitePawns)
    val blackPawns = blackPawnAttacks(bitboardData.blackPawns)
    val whiteRooks = rookAttackList(bitboardData, pieceSquareLists.whiteRooks)
    val whiteBishops = bishopAttackList(bitboardData, pieceSquareLists.whiteBishops)
    val whiteQueens = queenAttackList(bitboardData, pieceSquareLists.whiteQueens)
    val whiteKnights = knightAttackList(pieceSquareLists.whiteKnights)
    val blackRooks = rookAttackList(bitboardData, pieceSquareLists.blackRooks)
    val blackBishops = bishopAttackList(bitboardData, pieceSquareLists.blackBishops)
    val blackQueens = queenAttackList(bitboardData, pieceSquareLists.blackQueens)
    val blackKnights = knightAttackList(pieceSquareLists.blackKnights)
}

fun whitePawnAttacks(whitePawns: Long) =
    whitePawns and Bitboards.FILE_A.inv() shl 9 or (whitePawns and Bitboards.FILE_H.inv() shl 7)

fun blackPawnAttacks(blackPawns: Long) =
    blackPawns and Bitboards.FILE_A.inv() ushr 7 or (blackPawns and Bitboards.FILE_H.inv() ushr 9)

fun rookAttackList(bitboards: BitboardData, rookSquares: List<Int>) =
        rookSquares.asSequence().map { rookAttacks(bitboards, it) }.toList()

fun bishopAttackList(bitboards: BitboardData, whiteBishopSquares: List<Int>) =
        whiteBishopSquares.asSequence().map { s -> bishopAttacks(bitboards, s)}.toList()

fun queenAttackList(bitboards: BitboardData, whiteQueenSquares: List<Int>) =
        whiteQueenSquares.asSequence().map { s -> queenAttacks(bitboards, s)}.toList()
