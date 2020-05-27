package com.netsensia.rivalchess.bitboards.util

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.engine.core.eval.blackPawnAttacks
import com.netsensia.rivalchess.engine.core.eval.whitePawnAttacks
import com.netsensia.rivalchess.model.Colour
import kotlinx.coroutines.yield
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
            northFill(whitePawns or whitePawnAttacks(whitePawns) or (blackPawns shl 8)).inv()
}

fun getPawnFiles(pawns: Long): Long {
    return southFill(pawns) and RANK_1
}

fun getWhitePassedPawns(whitePawns: Long, blackPawns: Long): Long {
    return whitePawns and
            southFill(blackPawns or blackPawnAttacks(blackPawns) or (whitePawns ushr 8)).inv()
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
    return if (colour == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture
}

fun getFirstOccupiedSquare(bitboard: Long): Int {
    return numberOfTrailingZeros(bitboard)
}

fun isBishopAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long): Boolean {
    return (MagicBitboards.magicMovesBishop[pieceSquare][getMagicIndexForBishop(pieceSquare, allPieceBitboard)]
            and (1L shl attackedSquare)) != 0L
}

fun getMagicIndexForBishop(pieceSquare: Int, allPieceBitboard: Long): Int {
    return ((allPieceBitboard and MagicBitboards.occupancyMaskBishop[pieceSquare]) * MagicBitboards.magicNumberBishop[pieceSquare]
            ushr MagicBitboards.magicNumberShiftsBishop[pieceSquare]).toInt()
}

fun isRookAttackingSquare(attackedSquare: Int, pieceSquare: Int, allPieceBitboard: Long): Boolean {
    return (MagicBitboards.magicMovesRook[pieceSquare][getMagicIndexForRook(pieceSquare, allPieceBitboard)]
            and (1L shl attackedSquare)) != 0L
}

fun getMagicIndexForRook(pieceSquare: Int, allPieceBitboard: Long): Int {
    return ((allPieceBitboard and MagicBitboards.occupancyMaskRook[pieceSquare]) *
            MagicBitboards.magicNumberRook[pieceSquare]
            ushr MagicBitboards.magicNumberShiftsRook[pieceSquare]).toInt()
}

fun unsetBit(bitboard: Long, bit: Int) = bitboard xor (1L shl bit)

fun orList(list: List<Long>) : Long = list.asSequence().fold(0L) { acc, i -> acc or i }

tailrec fun squareList(bitboard: Long, squareList: List<Int> = emptyList()) : List<Int> =
        when (bitboard) {
            0L -> squareList
            else -> {
                val square = numberOfTrailingZeros(bitboard)
                squareList(unsetBit(bitboard, square), squareList + square)
            }
        }

fun squareListSequence(bitboard: Long) = sequence {
    var bitboardCopy = bitboard
    while (bitboardCopy != 0L) {
        val square = numberOfTrailingZeros(bitboardCopy)
        yield (square)
        bitboardCopy = unsetBit(bitboardCopy, square)
    }
}