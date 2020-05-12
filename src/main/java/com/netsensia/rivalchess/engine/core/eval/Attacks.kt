package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.util.orList
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

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

fun whiteAttackScore(bitboards: BitboardData, attacks: Attacks, squareOccupants: List<SquareOccupant>): Int {
    return squareList(whiteAttacksBitboard(bitboards, attacks))
            .asSequence()
            .map { PieceValue.getValue(squareOccupants[it].piece) }
            .fold(0) { acc, i -> acc + i }
}

fun blackAttackScore(bitboards: BitboardData, attacks: Attacks, squareOccupants: List<SquareOccupant>): Int {
    return squareList(blackAttacksBitboard(bitboards, attacks))
            .asSequence()
            .map { PieceValue.getValue(squareOccupants[it].piece) }
            .fold(0) { acc, i -> acc + i }
}

fun whitePieceAttacks(attacks: Attacks) =
        orList(attacks.whiteRooks) or
                orList(attacks.whiteQueens) or
                orList(attacks.whiteBishops) or
                orList(attacks.whiteKnights)

fun blackPieceAttacks(attacks: Attacks) =
        orList(attacks.blackRooks) or
                orList(attacks.blackQueens) or
                orList(attacks.blackBishops) or
                orList(attacks.blackKnights)

fun whiteAttacksBitboard(bitboards: BitboardData, attacks: Attacks) =
        (whitePieceAttacks(attacks) or attacks.whitePawns) and
                blackPieceBitboard(bitboards)

fun blackAttacksBitboard(bitboards: BitboardData, attacks: Attacks) =
        (blackPieceAttacks(attacks) or attacks.blackPawns) and
                whitePieceBitboard(bitboards)

fun threatEval(bitboards: BitboardData, attacks: Attacks, squareOccupants: List<SquareOccupant>): Int {
    return (adjustedAttackScore(whiteAttackScore(bitboards, attacks, squareOccupants)) -
            adjustedAttackScore(blackAttackScore(bitboards, attacks, squareOccupants))) /
            Evaluation.THREAT_SCORE_DIVISOR.value
}

fun adjustedAttackScore(attackScore: Int) =
        attackScore + attackScore * (attackScore / PieceValue.getValue(Piece.QUEEN))
