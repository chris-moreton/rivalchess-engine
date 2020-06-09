package com.netsensia.rivalchess.bitboards

import com.netsensia.rivalchess.bitboards.util.getPawnMovesCaptureOfColour
import com.netsensia.rivalchess.bitboards.util.isBishopAttackingSquare
import com.netsensia.rivalchess.bitboards.util.isRookAttackingSquare
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.SquareOccupant
import java.lang.Long.numberOfTrailingZeros
import java.util.*

class EngineBitboards {
    lateinit var pieceBitboards: LongArray

    init {
        reset()
    }

    fun reset() {
        pieceBitboards = LongArray(BITBOARD_COUNT)
        Arrays.fill(pieceBitboards, 0)
    }

    fun xorPieceBitboard(i: Int, xorBy: Long) {
        pieceBitboards[i] = pieceBitboards[i] xor xorBy
    }

    fun orPieceBitboard(type: Int, xorBy: Long) {
        pieceBitboards[type] = pieceBitboards[type] or xorBy
    }

    fun setPieceBitboard(type: Int, bitboard: Long) {
        pieceBitboards[type] = bitboard
    }

    fun getPieceBitboard(type: Int) = pieceBitboards[type]
    
    fun getPieceBitboard(type: SquareOccupant) = getPieceBitboard(type.index)

    fun movePiece(piece: SquareOccupant, compactMove: Int) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val fromMask = 1L shl moveFrom.toInt()
        val toMask = 1L shl moveTo.toInt()
        pieceBitboards[piece.index] = pieceBitboards[piece.index] xor (fromMask or toMask)
    }

    private fun getRookMovePiecesBitboard(colour: Colour): Long {
        return if (colour == Colour.WHITE) getPieceBitboard(BITBOARD_WR) or getPieceBitboard(BITBOARD_WQ) else getPieceBitboard(BITBOARD_BR) or getPieceBitboard(BITBOARD_BQ)
    }

    private fun getBishopMovePiecesBitboard(colour: Colour): Long {
        return if (colour == Colour.WHITE) getPieceBitboard(BITBOARD_WB) or getPieceBitboard(BITBOARD_WQ) else getPieceBitboard(BITBOARD_BB) or getPieceBitboard(BITBOARD_BQ)
    }

    fun isSquareAttackedBy(attackedSquare: Int, attacker: Colour): Boolean {
        if (pieceBitboards[SquareOccupant.WN.ofColour(attacker).index] and knightMoves[attackedSquare] != 0L ||
                pieceBitboards[SquareOccupant.WK.ofColour(attacker).index] and kingMoves[attackedSquare] != 0L ||
                (pieceBitboards[SquareOccupant.WP.ofColour(attacker).index]
                        and getPawnMovesCaptureOfColour(attacker.opponent())[attackedSquare]) != 0L)
            return true

        var bitboardBishop = getBishopMovePiecesBitboard(attacker)

        while (bitboardBishop != 0L) {
            val pieceSquare = numberOfTrailingZeros(bitboardBishop)
            bitboardBishop = bitboardBishop xor (1L shl pieceSquare)
            if (isBishopAttackingSquare(attackedSquare, pieceSquare, pieceBitboards[BITBOARD_ALL])) {
                return true
            }
        }
        var bitboardRook = getRookMovePiecesBitboard(attacker)
        while (bitboardRook != 0L) {
            val pieceSquare = numberOfTrailingZeros(bitboardRook)
            bitboardRook = bitboardRook xor (1L shl pieceSquare)
            if (isRookAttackingSquare(attackedSquare, pieceSquare, pieceBitboards[BITBOARD_ALL])) {
                return true
            }
        }
        return false
    }


}