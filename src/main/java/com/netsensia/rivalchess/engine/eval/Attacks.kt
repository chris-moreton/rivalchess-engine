package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.config.THREAT_SCORE_DIVISOR
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour

class Attacks(bitboardData: BitboardData) {
    @JvmField
    val whitePawns = whitePawnAttacks(bitboardData.whitePawns)
    @JvmField
    val blackPawns = blackPawnAttacks(bitboardData.blackPawns)
    @JvmField
    val whiteRooks = attackList(bitboardData, bitboardData.whiteRooks, ::rookAttacks, Colour.WHITE)
    @JvmField
    val whiteBishops = attackList(bitboardData, bitboardData.whiteBishops, ::bishopAttacks, Colour.WHITE)
    @JvmField
    val whiteQueens = attackList(bitboardData, bitboardData.whiteQueens, ::queenAttacks, Colour.WHITE)
    @JvmField
    val blackRooks = attackList(bitboardData, bitboardData.blackRooks, ::rookAttacks, Colour.BLACK)
    @JvmField
    val blackBishops = attackList(bitboardData, bitboardData.blackBishops, ::bishopAttacks, Colour.BLACK)
    @JvmField
    val blackQueens = attackList(bitboardData, bitboardData.blackQueens, ::queenAttacks, Colour.BLACK)
    @JvmField
    var blackPieceAttacks = 0L
    @JvmField
    var whitePieceAttacks = 0L

    init {
        knightAttackList(bitboardData.whiteKnights, Colour.WHITE)
        knightAttackList(bitboardData.blackKnights, Colour.BLACK)
    }

    private inline fun attackList(bitboards: BitboardData, squaresBitboard: Long, fn: (BitboardData, Int) -> Long, colour: Colour): LongArray {
        var count = 0
        val a = longArrayOf(-1L,-1L,-1L,-1L)
        applyToSquares(squaresBitboard) {
            val attacksForSquare = fn(bitboards, it)
            a[count++] = attacksForSquare
            if (colour == Colour.WHITE) whitePieceAttacks = whitePieceAttacks or attacksForSquare else
                blackPieceAttacks = blackPieceAttacks or attacksForSquare
            if (count == 4) return@applyToSquares
        }
        return a
    }

    private fun knightAttackList(squaresBitboard: Long, colour: Colour) {
        applyToSquares(squaresBitboard) {
            if (colour == Colour.WHITE) whitePieceAttacks = whitePieceAttacks or knightMoves[it] else
                blackPieceAttacks = blackPieceAttacks or knightMoves[it]
        }
    }
}

fun whitePawnAttacks(whitePawns: Long) = whitePawns and FILE_A.inv() shl 9 or (whitePawns and FILE_H.inv() shl 7)

fun blackPawnAttacks(blackPawns: Long) = blackPawns and FILE_A.inv() ushr 7 or (blackPawns and FILE_H.inv() ushr 9)

fun whiteAttackScore(bitboards: BitboardData, attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(whiteAttacksBitboard(bitboards, attacks)) {
        acc += pieceValue(board.getBitboardTypeOfPieceOnSquare(it, Colour.BLACK))
    }
    return acc
}

fun blackAttackScore(bitboards: BitboardData, attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(blackAttacksBitboard(bitboards, attacks)) {
        acc += pieceValue(board.getBitboardTypeOfPieceOnSquare(it, Colour.WHITE))
    }
    return acc
}

fun whiteAttacksBitboard(bitboards: BitboardData, attacks: Attacks) =
        (attacks.whitePieceAttacks or attacks.whitePawns) and blackPieceBitboard(bitboards)

fun blackAttacksBitboard(bitboards: BitboardData, attacks: Attacks) =
        (attacks.blackPieceAttacks or attacks.blackPawns) and whitePieceBitboard(bitboards)

fun threatEval(bitboards: BitboardData, attacks: Attacks, board: EngineBoard) =
        (adjustedAttackScore(whiteAttackScore(bitboards, attacks, board)) -
                adjustedAttackScore(blackAttackScore(bitboards, attacks, board))) /
                THREAT_SCORE_DIVISOR

fun adjustedAttackScore(attackScore: Int) = attackScore + attackScore * (attackScore / VALUE_QUEEN)

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
