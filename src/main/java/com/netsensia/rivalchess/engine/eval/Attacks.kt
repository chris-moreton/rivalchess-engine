package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.config.THREAT_SCORE_DIVISOR
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour

class Attacks(board: EngineBoard) {
    @JvmField
    val whitePawns = whitePawnAttacks(board.getBitboard(BITBOARD_WP))
    @JvmField
    val blackPawns = blackPawnAttacks(board.getBitboard(BITBOARD_BP))
    @JvmField
    val whiteRooks = attackList(board, board.getBitboard(BITBOARD_WR), ::rookAttacks, Colour.WHITE)
    @JvmField
    val whiteBishops = attackList(board, board.getBitboard(BITBOARD_WB), ::bishopAttacks, Colour.WHITE)
    @JvmField
    val whiteQueens = attackList(board, board.getBitboard(BITBOARD_WQ), ::queenAttacks, Colour.WHITE)
    @JvmField
    val blackRooks = attackList(board, board.getBitboard(BITBOARD_BR), ::rookAttacks, Colour.BLACK)
    @JvmField
    val blackBishops = attackList(board, board.getBitboard(BITBOARD_BB), ::bishopAttacks, Colour.BLACK)
    @JvmField
    val blackQueens = attackList(board, board.getBitboard(BITBOARD_BQ), ::queenAttacks, Colour.BLACK)
    @JvmField
    var blackPieceAttacks = 0L
    @JvmField
    var whitePieceAttacks = 0L

    init {
        knightAttackList(board.getBitboard(BITBOARD_WN), Colour.WHITE)
        knightAttackList(board.getBitboard(BITBOARD_BN), Colour.BLACK)
    }

    private inline fun attackList(board: EngineBoard, squaresBitboard: Long, fn: (EngineBoard, Int) -> Long, colour: Colour): LongArray {
        var count = 0
        val a = longArrayOf(-1L,-1L,-1L,-1L)
        applyToSquares(squaresBitboard) {
            val attacksForSquare = fn(board, it)
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

fun whiteAttackScore(attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(whiteAttacksBitboard(board, attacks)) {
        acc += pieceValue(board.getBitboardTypeOfPieceOnSquare(it, Colour.BLACK))
    }
    return acc
}

fun blackAttackScore(attacks: Attacks, board: EngineBoard): Int {
    var acc = 0
    applyToSquares(blackAttacksBitboard(board, attacks)) {
        acc += pieceValue(board.getBitboardTypeOfPieceOnSquare(it, Colour.WHITE))
    }
    return acc
}

fun whiteAttacksBitboard(board: EngineBoard, attacks: Attacks) =
        (attacks.whitePieceAttacks or attacks.whitePawns) and blackPieceBitboard(board)

fun blackAttacksBitboard(board: EngineBoard, attacks: Attacks) =
        (attacks.blackPieceAttacks or attacks.blackPawns) and whitePieceBitboard(board)

fun threatEval(attacks: Attacks, board: EngineBoard) =
        (adjustedAttackScore(whiteAttackScore(attacks, board)) -
                adjustedAttackScore(blackAttackScore(attacks, board))) / THREAT_SCORE_DIVISOR

fun adjustedAttackScore(attackScore: Int) = attackScore + attackScore * (attackScore / VALUE_QUEEN)

fun rookAttacks(board: EngineBoard, sq: Int) : Long =
        MagicBitboards.magicMovesRook[sq][
                ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq] ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun bishopAttacks(board: EngineBoard, sq: Int) =
        MagicBitboards.magicMovesBishop[sq][
                ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq] ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun queenAttacks(board: EngineBoard, sq: Int) = rookAttacks(board, sq) or bishopAttacks(board, sq)
