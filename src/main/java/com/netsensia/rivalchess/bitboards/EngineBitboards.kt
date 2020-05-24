package com.netsensia.rivalchess.bitboards

import com.netsensia.rivalchess.bitboards.util.getFirstOccupiedSquare
import com.netsensia.rivalchess.bitboards.util.getPawnMovesCaptureOfColour
import com.netsensia.rivalchess.bitboards.util.isBishopAttackingSquare
import com.netsensia.rivalchess.bitboards.util.isRookAttackingSquare
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.SquareOccupant
import java.util.*

class EngineBitboards private constructor() {
    private lateinit var pieceBitboards: LongArray

    val allPieceBitboard: Long
        get() = getPieceBitboard(BitboardType.ALL)

    fun xorPieceBitboard(i: Int, xorBy: Long) {
        pieceBitboards[i] = pieceBitboards[i] xor xorBy
    }

    fun xorPieceBitboard(type: BitboardType, xorBy: Long) {
        pieceBitboards[type.index] = pieceBitboards[type.index] xor xorBy
    }

    fun orPieceBitboard(type: BitboardType, xorBy: Long) {
        pieceBitboards[type.index] = pieceBitboards[type.index] or xorBy
    }

    fun reset() {
        pieceBitboards = LongArray(BitboardType.getNumBitboardTypes())
        Arrays.fill(pieceBitboards, 0)
    }

    fun setPieceBitboard(type: BitboardType, bitboard: Long) {
        pieceBitboards[type.index] = bitboard
    }

    fun getPieceBitboard(type: BitboardType): Long {
        return pieceBitboards[type.index]
    }

    fun movePiece(piece: SquareOccupant, compactMove: Int) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val fromMask = 1L shl moveFrom.toInt()
        val toMask = 1L shl moveTo.toInt()
        pieceBitboards[piece.index] = pieceBitboards[piece.index] xor (fromMask or toMask)
    }

    fun getRookMovePiecesBitboard(colour: Colour): Long {
        return if (colour == Colour.WHITE) getPieceBitboard(BitboardType.WR) or getPieceBitboard(BitboardType.WQ) else getPieceBitboard(BitboardType.BR) or getPieceBitboard(BitboardType.BQ)
    }

    fun getBishopMovePiecesBitboard(colour: Colour): Long {
        return if (colour == Colour.WHITE) getPieceBitboard(BitboardType.WB) or getPieceBitboard(BitboardType.WQ) else getPieceBitboard(BitboardType.BB) or getPieceBitboard(BitboardType.BQ)
    }

    fun isSquareAttackedBy(attackedSquare: Int, attacker: Colour): Boolean {
        if (pieceBitboards[SquareOccupant.WN.ofColour(attacker).index] and knightMoves[attackedSquare] != 0L || pieceBitboards[SquareOccupant.WK.ofColour(attacker).index] and kingMoves[attackedSquare] != 0L || (pieceBitboards[SquareOccupant.WP.ofColour(attacker).index]
                        and getPawnMovesCaptureOfColour(attacker.opponent())[attackedSquare]) != 0L) return true
        var bitboardBishop = getBishopMovePiecesBitboard(attacker)
        while (bitboardBishop != 0L) {
            val pieceSquare = getFirstOccupiedSquare(bitboardBishop)
            bitboardBishop = bitboardBishop xor (1L shl pieceSquare)
            if (isBishopAttackingSquare(attackedSquare, pieceSquare, allPieceBitboard)) {
                return true
            }
        }
        var bitboardRook = getRookMovePiecesBitboard(attacker)
        while (bitboardRook != 0L) {
            val pieceSquare = java.lang.Long.numberOfTrailingZeros(bitboardRook)
            bitboardRook = bitboardRook xor (1L shl pieceSquare)
            if (isRookAttackingSquare(attackedSquare, pieceSquare, allPieceBitboard)) {
                return true
            }
        }
        return false
    }

    companion object {
        private var INSTANCE: EngineBitboards? = null
        @JvmStatic
        val instance: EngineBitboards
            get() {
                if (INSTANCE == null) {
                    INSTANCE = EngineBitboards()
                }
                return INSTANCE!!
            }
    }

    init {
        reset()
    }
}