package com.netsensia.rivalchess.bitboards.util

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.engine.core.eval.blackPawnAttacks
import com.netsensia.rivalchess.engine.core.eval.whitePawnAttacks
import com.netsensia.rivalchess.model.Colour
import java.lang.Long.numberOfTrailingZeros

tailrec fun southFill(bitboard: Long, shiftBy: Int = 8): Long =
    if (shiftBy == 32) bitboard or (bitboard ushr shiftBy)
    else southFill(bitboard or (bitboard ushr shiftBy), shiftBy + shiftBy)

tailrec fun northFill(bitboard: Long, shiftBy: Int = 8): Long =
    if (shiftBy == 32) bitboard or (bitboard shl shiftBy)
    else northFill(bitboard or (bitboard shl shiftBy), shiftBy + shiftBy)

fun getBlackPassedPawns(whitePawns: Long, blackPawns: Long) =
    blackPawns and northFill(whitePawns or whitePawnAttacks(whitePawns) or (blackPawns shl 8)).inv()

fun getPawnFiles(pawns: Long) = southFill(pawns) and RANK_1

fun getWhitePassedPawns(whitePawns: Long, blackPawns: Long) =
    whitePawns and southFill(blackPawns or blackPawnAttacks(blackPawns) or (whitePawns ushr 8)).inv()

fun getPawnMovesCaptureOfColour(colour: Colour): List<Long> =
    if (colour == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture

fun isBishopAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long) =
    (MagicBitboards.magicMovesBishop[pieceSquare][getMagicIndexForBishop(pieceSquare, allPieceBitboard)] and (1L shl attackedSquare)) != 0L

fun getMagicIndexForBishop(pieceSquare: Int, allPieceBitboard: Long): Int {
    return ((allPieceBitboard and MagicBitboards.occupancyMaskBishop[pieceSquare]) *
            MagicBitboards.magicNumberBishop[pieceSquare] ushr
            MagicBitboards.magicNumberShiftsBishop[pieceSquare]).toInt()
}

fun isRookAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long) =
    (MagicBitboards.magicMovesRook[pieceSquare][getMagicIndexForRook(pieceSquare, allPieceBitboard)] and (1L shl attackedSquare)) != 0L

fun getMagicIndexForRook(pieceSquare: Int, allPieceBitboard: Long) =
    ((allPieceBitboard and MagicBitboards.occupancyMaskRook[pieceSquare]) *
            MagicBitboards.magicNumberRook[pieceSquare] ushr
            MagicBitboards.magicNumberShiftsRook[pieceSquare]).toInt()

fun squareList(bitboard: Long): List<Int> {
    val squares = mutableListOf<Int>()
    var bitboardCopy = bitboard
    while (bitboardCopy != 0L) {
        val square = numberOfTrailingZeros(bitboardCopy)
        squares.add(square)
        bitboardCopy = bitboardCopy xor (1L shl square)
    }
    return squares
}

inline fun applyToSquares(bitboard: Long, fn: (Int) -> Unit) {
    var bitboardCopy = bitboard
    while (bitboardCopy != 0L) {
        val square = numberOfTrailingZeros(bitboardCopy)
        fn(square)
        bitboardCopy = bitboardCopy xor (1L shl square)
    }
}
