package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.FILE_A
import com.netsensia.rivalchess.bitboards.FILE_H
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.bitboards.knightMoves
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.config.THREAT_SCORE_DIVISOR
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour

@kotlin.ExperimentalUnsignedTypes
class Attacks(board: EngineBoard) {
    val whitePawns = whitePawnAttacks(board.getBitboard(BITBOARD_WP))
    val blackPawns = blackPawnAttacks(board.getBitboard(BITBOARD_BP))
    val allBitboard = board.getBitboard(BITBOARD_ALL)
    val whiteRooks = attackList(allBitboard, board.getBitboard(BITBOARD_WR), ::rookAttacks, Colour.WHITE)
    val whiteBishops = attackList(allBitboard, board.getBitboard(BITBOARD_WB), ::bishopAttacks, Colour.WHITE)
    val whiteQueens = attackList(allBitboard, board.getBitboard(BITBOARD_WQ), ::queenAttacks, Colour.WHITE)
    val blackRooks = attackList(allBitboard, board.getBitboard(BITBOARD_BR), ::rookAttacks, Colour.BLACK)
    val blackBishops = attackList(allBitboard, board.getBitboard(BITBOARD_BB), ::bishopAttacks, Colour.BLACK)
    val blackQueens = attackList(allBitboard, board.getBitboard(BITBOARD_BQ), ::queenAttacks, Colour.BLACK)

    @JvmField
    var blackPieceAttacks = 0L
    @JvmField
    var whitePieceAttacks = 0L

    init {
        knightAttackList(board.getBitboard(BITBOARD_WN), Colour.WHITE)
        knightAttackList(board.getBitboard(BITBOARD_BN), Colour.BLACK)
    }

    private inline fun attackList(allBitboard: Long, squaresBitboard: Long, pieceAttacksFn: (Long, Int) -> Long, colour: Colour): LongArray {
        var count = 0
        val squareAttacks = longArrayOf(-1L,-1L,-1L,-1L)
        applyToSquares(squaresBitboard) {
            val attacksForSquare = pieceAttacksFn(allBitboard, it)
            squareAttacks[count++] = attacksForSquare
            if (colour == Colour.WHITE)
                whitePieceAttacks = whitePieceAttacks or attacksForSquare else
                blackPieceAttacks = blackPieceAttacks or attacksForSquare
            if (count == 4) return@applyToSquares
        }
        return squareAttacks
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

@kotlin.ExperimentalUnsignedTypes
fun whiteAttackScore(attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(whiteAttacksBitboard(board, attacks)) {
        acc += pieceValue(board.getBitboardTypeOfPieceOnSquare(it, Colour.BLACK))
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun blackAttackScore(attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(blackAttacksBitboard(board, attacks)) {
        acc += pieceValue(board.getBitboardTypeOfPieceOnSquare(it, Colour.WHITE))
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun whiteAttacksBitboard(board: EngineBoard, attacks: Attacks) = (attacks.whitePieceAttacks or attacks.whitePawns) and blackPieceBitboard(board)

@kotlin.ExperimentalUnsignedTypes
fun blackAttacksBitboard(board: EngineBoard, attacks: Attacks) = (attacks.blackPieceAttacks or attacks.blackPawns) and whitePieceBitboard(board)

@kotlin.ExperimentalUnsignedTypes
fun threatEval(attacks: Attacks, board: EngineBoard) =
        (adjustedAttackScore(whiteAttackScore(attacks, board)) -
                adjustedAttackScore(blackAttackScore(attacks, board))) / THREAT_SCORE_DIVISOR

fun adjustedAttackScore(attackScore: Int) = attackScore + attackScore * (attackScore / pieceValue(BITBOARD_WQ))

fun rookAttacks(allBitboard: Long, sq: Int) : Long =
        MagicBitboards.magicMovesRook[sq][
                ((allBitboard and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq] ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun bishopAttacks(allBitboard: Long, sq: Int) =
        MagicBitboards.magicMovesBishop[sq][
                ((allBitboard and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq] ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun queenAttacks(allBitboard: Long, sq: Int) = rookAttacks(allBitboard, sq) or bishopAttacks(allBitboard, sq)
