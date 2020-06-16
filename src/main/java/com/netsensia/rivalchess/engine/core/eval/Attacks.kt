package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.config.THREAT_SCORE_DIVISOR
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

class Attacks(bitboardData: BitboardData) {
    val whitePawns = whitePawnAttacks(bitboardData.whitePawns)
    val blackPawns = blackPawnAttacks(bitboardData.blackPawns)
    val whiteRookPair = attackList(bitboardData, bitboardData.whiteRooks, ::rookAttacks)
    val whiteBishopPair = attackList(bitboardData, bitboardData.whiteBishops, ::bishopAttacks)
    val whiteQueenPair = attackList(bitboardData, bitboardData.whiteQueens, ::queenAttacks)
    val whiteKnightPair = knightAttackList(bitboardData.whiteKnights)
    val blackRookPair = attackList(bitboardData, bitboardData.blackRooks, ::rookAttacks)
    val blackBishopPair = attackList(bitboardData, bitboardData.blackBishops, ::bishopAttacks)
    val blackQueenPair = attackList(bitboardData, bitboardData.blackQueens, ::queenAttacks)
    val blackKnightPair = knightAttackList(bitboardData.blackKnights)
}

fun whitePawnAttacks(whitePawns: Long) = whitePawns and FILE_A.inv() shl 9 or (whitePawns and FILE_H.inv() shl 7)

fun blackPawnAttacks(blackPawns: Long) = blackPawns and FILE_A.inv() ushr 7 or (blackPawns and FILE_H.inv() ushr 9)

inline fun attackList(bitboards: BitboardData, squaresBitboard: Long, fn: (BitboardData, Int) -> Long): Pair<List<Long>, Long> {
    var orred = 0L
    val list = mutableListOf<Long>()
    applyToSquares(squaresBitboard) {
        val attacksForSquare = fn(bitboards, it)
        list.add(attacksForSquare)
        orred = orred or attacksForSquare
    }
    return Pair(list, orred)
}

fun knightAttackList(squaresBitboard: Long): Pair<List<Long>, Long> {
    var orred = 0L
    val list = mutableListOf<Long>()
    applyToSquares(squaresBitboard) {
        val attacksForSquare = knightMoves[it]
        list.add(attacksForSquare)
        orred = orred or attacksForSquare
    }
    return Pair(list, orred)
}

fun whiteAttackScore(bitboards: BitboardData, attacks: Attacks, squareOccupants: Array<SquareOccupant>): Int {
    var acc = 0
    applyToSquares(whiteAttacksBitboard(bitboards, attacks)) {
        acc += pieceValue(squareOccupants[it].piece)
    }
    return acc
}

fun blackAttackScore(bitboards: BitboardData, attacks: Attacks, squareOccupants: Array<SquareOccupant>): Int {
    var acc = 0
    applyToSquares(blackAttacksBitboard(bitboards, attacks)) {
        acc += pieceValue(squareOccupants[it].piece)
    }
    return acc
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
        (whitePieceAttacks(attacks) or attacks.whitePawns) and blackPieceBitboard(bitboards)

fun blackAttacksBitboard(bitboards: BitboardData, attacks: Attacks) =
        (blackPieceAttacks(attacks) or attacks.blackPawns) and whitePieceBitboard(bitboards)

fun threatEval(bitboards: BitboardData, attacks: Attacks, squareOccupants: Array<SquareOccupant>) =
    (adjustedAttackScore(whiteAttackScore(bitboards, attacks, squareOccupants)) -
            adjustedAttackScore(blackAttackScore(bitboards, attacks, squareOccupants))) /
            THREAT_SCORE_DIVISOR

fun adjustedAttackScore(attackScore: Int) = attackScore + attackScore * (attackScore / pieceValue(Piece.QUEEN))

fun rookAttacks(bitboards: BitboardData, sq: Int) : Long =
        MagicBitboards.magicMovesRook[sq][
                ((bitboards.all and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq] ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun bishopAttacks(bitboards: BitboardData, sq: Int) =
        MagicBitboards.magicMovesBishop[sq][
                ((bitboards.all and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq]
                        ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun queenAttacks(bitboards: BitboardData, sq: Int) = rookAttacks(bitboards, sq) or bishopAttacks(bitboards, sq)
