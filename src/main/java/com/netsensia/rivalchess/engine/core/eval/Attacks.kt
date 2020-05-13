package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.FILE_A
import com.netsensia.rivalchess.bitboards.FILE_H
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

class Attacks(bitboardData: BitboardData, pieceSquareLists: PieceSquareLists) {
    val whitePawns = whitePawnAttacks(bitboardData.whitePawns)
    val blackPawns = blackPawnAttacks(bitboardData.blackPawns)
    val whiteRookPair = attackList(bitboardData, pieceSquareLists.whiteRooks, ::rookAttacks)
    val whiteBishopPair = attackList(bitboardData, pieceSquareLists.whiteBishops, ::bishopAttacks)
    val whiteQueenPair = attackList(bitboardData, pieceSquareLists.whiteQueens, ::queenAttacks)
    val whiteKnightPair = knightAttackList(pieceSquareLists.whiteKnights)
    val blackRookPair = attackList(bitboardData, pieceSquareLists.blackRooks, ::rookAttacks)
    val blackBishopPair = attackList(bitboardData, pieceSquareLists.blackBishops, ::bishopAttacks)
    val blackQueenPair = attackList(bitboardData, pieceSquareLists.blackQueens, ::queenAttacks)
    val blackKnightPair = knightAttackList(pieceSquareLists.blackKnights)
}

fun whitePawnAttacks(whitePawns: Long) =
        whitePawns and FILE_A.inv() shl 9 or (whitePawns and FILE_H.inv() shl 7)

fun blackPawnAttacks(blackPawns: Long) =
        blackPawns and FILE_A.inv() ushr 7 or (blackPawns and FILE_H.inv() ushr 9)

fun attackList(bitboards: BitboardData, squares: List<Int>, fn: (BitboardData, Int) -> Long): Pair<List<Long>, Long> {
    var list = mutableListOf<Long>()
    var orred = 0L

    for (square in squares) {
        val attacks = fn(bitboards, square)
        list.add(attacks)
        orred = orred or attacks
    }

    return Pair(list, orred)
}

fun knightAttackList(squares: List<Int>): Pair<List<Long>, Long> {
    var list = mutableListOf<Long>()
    var orred = 0L

    for (square in squares) {
        val attacks = Bitboards.knightMoves[square]
        list.add(attacks)
        orred = orred or attacks
    }

    return Pair(list, orred)
}

fun whiteAttackScore(bitboards: BitboardData, attacks: Attacks, squareOccupants: List<SquareOccupant>): Int {
    return squareList(whiteAttacksBitboard(bitboards, attacks))
            .asSequence()
            .map { pieceValue(squareOccupants[it].piece) }
            .fold(0) { acc, i -> acc + i }
}

fun blackAttackScore(bitboards: BitboardData, attacks: Attacks, squareOccupants: List<SquareOccupant>): Int {
    return squareList(blackAttacksBitboard(bitboards, attacks))
            .asSequence()
            .map { pieceValue(squareOccupants[it].piece) }
            .fold(0) { acc, i -> acc + i }
}

fun whitePieceAttacks(attacks: Attacks) =
        attacks.whiteRookPair.second or
        attacks.whiteQueenPair.second or
        attacks.whiteBishopPair.second or
        attacks.whiteKnightPair.second

fun blackPieceAttacks(attacks: Attacks) =
        attacks.blackRookPair.second or
        attacks.blackQueenPair.second or
        attacks.blackBishopPair.second or
        attacks.blackKnightPair.second

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
        attackScore + attackScore * (attackScore / pieceValue(Piece.QUEEN))

fun rookAttacks(bitboards: BitboardData, sq: Int) : Long =
        Bitboards.magicBitboards.magicMovesRook[sq][
                ((bitboards.all and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq] ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun bishopAttacks(bitboards: BitboardData, sq: Int) =
        Bitboards.magicBitboards.magicMovesBishop[sq][
                ((bitboards.all and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq]
                        ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun queenAttacks(bitboards: BitboardData, sq: Int) = rookAttacks(bitboards, sq) or bishopAttacks(bitboards, sq)
