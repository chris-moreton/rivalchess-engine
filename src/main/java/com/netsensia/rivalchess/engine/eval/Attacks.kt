package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.config.THREAT_SCORE_DIVISOR
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece

class Attacks(bitboardData: BitboardData) {
    @JvmField
    val whitePawns = whitePawnAttacks(bitboardData.whitePawns)
    @JvmField
    val blackPawns = blackPawnAttacks(bitboardData.blackPawns)
    @JvmField
    val whiteRookPair = attackList(bitboardData, bitboardData.whiteRooks, ::rookAttacks)
    @JvmField
    val whiteBishopPair = attackList(bitboardData, bitboardData.whiteBishops, ::bishopAttacks)
    @JvmField
    val whiteQueenPair = attackList(bitboardData, bitboardData.whiteQueens, ::queenAttacks)
    @JvmField
    val whiteKnightPair = knightAttackList(bitboardData.whiteKnights)
    @JvmField
    val blackRookPair = attackList(bitboardData, bitboardData.blackRooks, ::rookAttacks)
    @JvmField
    val blackBishopPair = attackList(bitboardData, bitboardData.blackBishops, ::bishopAttacks)
    @JvmField
    val blackQueenPair = attackList(bitboardData, bitboardData.blackQueens, ::queenAttacks)
    @JvmField
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

fun whiteAttackScore(bitboards: BitboardData, attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(whiteAttacksBitboard(bitboards, attacks)) {
        acc += pieceValue(board.getSquareOccupant(it, Colour.BLACK).piece)
    }
    return acc
}

fun blackAttackScore(bitboards: BitboardData, attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(blackAttacksBitboard(bitboards, attacks)) {
        acc += pieceValue(board.getSquareOccupant(it, Colour.WHITE).piece)
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

fun threatEval(bitboards: BitboardData, attacks: Attacks, board: EngineBoard) =
    (adjustedAttackScore(whiteAttackScore(bitboards, attacks, board)) -
            adjustedAttackScore(blackAttackScore(bitboards, attacks, board))) /
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