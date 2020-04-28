package com.netsensia.rivalchess.bitboards.util

import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.model.Colour
import java.lang.Long.numberOfTrailingZeros
import java.util.*

tailrec fun southFill(bitboard: Long, shiftBy: Int = 8): Long {
    val shiftedAndOrred = bitboard or (bitboard ushr shiftBy)
    return if (shiftBy == 32) shiftedAndOrred else southFill(shiftedAndOrred, shiftBy * 2)
}

tailrec fun northFill(bitboard: Long, shiftBy: Int = 8): Long {
    val shiftedAndOrred = bitboard or (bitboard shl shiftBy)
    return if (shiftBy == 32) shiftedAndOrred else northFill(shiftedAndOrred, shiftBy * 2)
}

fun getBlackPassedPawns(whitePawns: Long, blackPawns: Long): Long {
    return blackPawns and
            northFill(whitePawns or getWhitePawnAttacks(whitePawns) or (blackPawns shl 8)).inv()
}

fun getPawnFiles(pawns: Long): Long {
    return southFill(pawns) and Bitboards.RANK_1
}

fun getWhitePassedPawns(whitePawns: Long, blackPawns: Long): Long {
    return whitePawns and
            southFill(blackPawns or getBlackPawnAttacks(blackPawns) or (whitePawns ushr 8)).inv()
}

fun getBlackPawnAttacks(blackPawns: Long): Long {
    return blackPawns and Bitboards.FILE_A.inv() ushr 7 or (blackPawns and Bitboards.FILE_H.inv() ushr 9)
}

fun getWhitePawnAttacks(whitePawns: Long): Long {
    return whitePawns and Bitboards.FILE_A.inv() shl 9 or (whitePawns and Bitboards.FILE_H.inv() shl 7)
}

tailrec fun getSetBits(bitboard: Long, setBits: MutableList<Int> = ArrayList()): List<Int> {
    return when (bitboard) {
        0L -> setBits
        else -> {
            val trailingZeroes = numberOfTrailingZeros(bitboard)
            setBits.add(trailingZeroes)
            return getSetBits(bitboard xor (1L shl trailingZeroes), setBits)
        }
    }
}

fun getPawnMovesCaptureOfColour(colour: Colour): List<Long> {
    return if (colour == Colour.WHITE) Bitboards.whitePawnMovesCapture else Bitboards.blackPawnMovesCapture
}

fun getFirstOccupiedSquare(bitboard: Long): Int {
    return java.lang.Long.numberOfTrailingZeros(bitboard)
}

fun isBishopAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long): Boolean {
    return (Bitboards.magicBitboards.magicMovesBishop[pieceSquare][getMagicIndexForBishop(pieceSquare, allPieceBitboard)]
            and (1L shl attackedSquare)) != 0L
}

fun getMagicIndexForBishop(pieceSquare: Int, allPieceBitboard: Long): Int {
    return ((allPieceBitboard and MagicBitboards.occupancyMaskBishop[pieceSquare]) * MagicBitboards.magicNumberBishop[pieceSquare]
            ushr MagicBitboards.magicNumberShiftsBishop[pieceSquare]).toInt()
}

fun isRookAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long): Boolean {
    return (Bitboards.magicBitboards.magicMovesRook[pieceSquare][getMagicIndexForRook(pieceSquare, allPieceBitboard)]
            and (1L shl attackedSquare)) != 0L
}

fun getMagicIndexForRook(pieceSquare: Int, allPieceBitboard: Long): Int {
    return ((allPieceBitboard and MagicBitboards.occupancyMaskRook[pieceSquare]) * MagicBitboards.magicNumberRook[pieceSquare]
            ushr MagicBitboards.magicNumberShiftsRook[pieceSquare]).toInt()
}

fun unsetBit(bitboard: Long, bit: Int) = bitboard xor (1L shl bit)

fun squareList(bitboard: Long) : List<Int> {
    val retList = mutableListOf<Int>()
    var mutableBitboard = bitboard

    while (mutableBitboard != 0L) {
        val square = numberOfTrailingZeros(mutableBitboard)
        retList.add(square)
        mutableBitboard = unsetBit(mutableBitboard, square)
    }
    return retList
}