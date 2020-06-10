package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.model.Colour

class SeeBoard(board: EngineBoard) {
    val bitboardMap: MutableMap<Int, Long> = mutableMapOf()
    val lastBitboardMap: MutableMap<Int, Long> = mutableMapOf()
    val whiteList = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    val blackList = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)
    var capturedPieceBitboardType: Int = BITBOARD_NONE
    var movedPieceBitboardType: Int = BITBOARD_NONE
    var lastFromBit: Long = 0
    var lastToBit: Long = 0
    var mover: Colour
    var lastEnPassantBitboard: Long = 0

    init {
        whiteList.forEach { bitboardMap.put(it, board.engineBitboards.getPieceBitboard(it)) }
        blackList.forEach { bitboardMap.put(it, board.engineBitboards.getPieceBitboard(it)) }
        bitboardMap.put(BITBOARD_ENPASSANTSQUARE, board.engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE))
        mover = board.mover
    }

    fun makeMove(move: EngineMove) {
        lastBitboardMap.clear()
        lastBitboardMap.putAll(bitboardMap)

        lastFromBit = 1L shl move.from()
        lastToBit = 1L shl move.to()
        lastEnPassantBitboard = bitboardMap[BITBOARD_ENPASSANTSQUARE]!!

        capturedPieceBitboardType = removeFromRelevantBitboard(lastToBit, if (mover == Colour.WHITE) blackList else whiteList)
        togglePiece(lastToBit, removeFromRelevantBitboard(lastFromBit, if (mover == Colour.BLACK) blackList else whiteList).also {
            movedPieceBitboardType = it }
        )

        if (mover == Colour.WHITE) {
            if (movedPieceBitboardType == BITBOARD_WP && move.to() - move.from() == 16) {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = lastToBit shr 8
            } else {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = 0
            }
        } else {
            if (movedPieceBitboardType == BITBOARD_BP && move.from() - move.to() == 16) {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = lastToBit shl 8
            } else {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = 0
            }
        }

        if (capturedPieceBitboardType == BITBOARD_NONE) {
            if (movedPieceBitboardType == BITBOARD_WP) {
                if ((move.to() - move.from()) % 2 != 0) {
                    togglePiece(1L shl (move.to() - 8), BITBOARD_BP)
                }
            } else {
                if ((move.to() - move.from()) % 2 != 0) {
                    togglePiece(1L shl (move.to() - 8), BITBOARD_WP)
                }
            }
        }

        mover = mover.opponent()
    }

    private fun removePieceIfExistsInBitboard(squareBit: Long, bitboardType: Int): Boolean {
        val pieceBitboard = bitboardMap[bitboardType]!!
        if (pieceBitboard or squareBit == pieceBitboard) {
            bitboardMap[bitboardType] = pieceBitboard xor squareBit
            return true
        }
        return false
    }

    private fun removeFromRelevantBitboard(squareBit: Long, bitboardList: List<Int>): Int {
        bitboardList.forEach { if (removePieceIfExistsInBitboard(squareBit, it)) return it }
        return BITBOARD_NONE
    }

    private fun togglePiece(squareBit: Long, bitboardType: Int) {
        bitboardMap[bitboardType] = bitboardMap[bitboardType]!! xor squareBit
    }

    fun unMakeMove() {
        bitboardMap.clear()
        bitboardMap.putAll(lastBitboardMap)
        mover = mover.opponent()
    }
}