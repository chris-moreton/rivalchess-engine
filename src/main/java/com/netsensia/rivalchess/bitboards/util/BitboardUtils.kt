package com.netsensia.rivalchess.bitboards.util

import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.bitboards.blackPawnMovesCapture
import com.netsensia.rivalchess.bitboards.whitePawnMovesCapture
import com.netsensia.rivalchess.model.Colour

fun southFill(bitboard: Long): Long {
    val a = bitboard or (bitboard ushr 8)
    val b = a or (a ushr 16)
    return b or (b ushr 32)
}

fun northFill(bitboard: Long): Long {
    val a = bitboard or (bitboard shl 8)
    val b = a or (a shl 16)
    return b or (b shl 32)
}

fun getPawnMovesCaptureOfColour(colour: Colour): LongArray =
        if (colour == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture

fun isBishopAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long) =
    (MagicBitboards.magicMovesBishop[pieceSquare][getMagicIndexForBishop(pieceSquare, allPieceBitboard)] and (1L shl attackedSquare)) != 0L

fun getMagicIndexForBishop(pieceSquare: Int, allPieceBitboard: Long) =
    ((allPieceBitboard and MagicBitboards.occupancyMaskBishop[pieceSquare]) *
            MagicBitboards.magicNumberBishop[pieceSquare] ushr
            MagicBitboards.magicNumberShiftsBishop[pieceSquare]).toInt()

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
        val square = bitboardCopy.countTrailingZeroBits()
        squares.add(square)
        bitboardCopy = bitboardCopy xor (1L shl square)
    }
    return squares
}

inline fun applyToSquares(bitboard: Long, fn: (Int) -> Unit) {
    var bitboardCopy = bitboard
    while (bitboardCopy != 0L) {
        val square = bitboardCopy.countTrailingZeroBits()
        fn(square)
        bitboardCopy = bitboardCopy xor (1L shl square)
    }
}

inline fun applyToFirstSquare(bitboard: Long, fn: (Int) -> Unit) {
    if (bitboard != 0L) fn(bitboard.countTrailingZeroBits())
}

fun popCount(x: Long) = x.countOneBits()
