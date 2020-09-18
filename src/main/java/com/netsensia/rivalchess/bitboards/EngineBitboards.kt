package com.netsensia.rivalchess.bitboards

import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.model.Colour
import java.util.*
import kotlin.collections.copyOf

class EngineBitboards() {
    @JvmField
    var pieceBitboards = LongArray(BITBOARD_COUNT)

    init { reset() }

    constructor(thoseBitboards: EngineBitboards) : this() {
        pieceBitboards = thoseBitboards.pieceBitboards.copyOf()
    }

    fun reset() {
        pieceBitboards = longArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
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

    fun getWhitePieces() =
            pieceBitboards[BITBOARD_WK] or
            pieceBitboards[BITBOARD_WN] or
            pieceBitboards[BITBOARD_WQ] or
            pieceBitboards[BITBOARD_WB] or
            pieceBitboards[BITBOARD_WR] or
            pieceBitboards[BITBOARD_WP]

    fun getBlackPieces() =
            pieceBitboards[BITBOARD_BK] or
            pieceBitboards[BITBOARD_BN] or
            pieceBitboards[BITBOARD_BQ] or
            pieceBitboards[BITBOARD_BB] or
            pieceBitboards[BITBOARD_BR] or
            pieceBitboards[BITBOARD_BP]

    fun movePiece(piece: Int, compactMove: Int) {
        val fromMask = (1L shl (compactMove ushr 16))
        val toMask = (1L shl (compactMove and 63))
        xorPieceBitboard(piece, (fromMask or toMask))
    }

    private fun getRookMovePiecesBitboard(colour: Colour) =
        if (colour == Colour.WHITE) pieceBitboards[BITBOARD_WR] or pieceBitboards[BITBOARD_WQ] else
            pieceBitboards[BITBOARD_BR] or pieceBitboards[BITBOARD_BQ]

    private fun getBishopMovePiecesBitboard(colour: Colour) =
        if (colour == Colour.WHITE) pieceBitboards[BITBOARD_WB] or pieceBitboards[BITBOARD_WQ] else
            pieceBitboards[BITBOARD_BB] or pieceBitboards[BITBOARD_BQ]

    fun isSquareAttackedBy(attackedSquare: Int, attacker: Colour): Boolean {

        if (pieceBitboards[if (attacker == Colour.WHITE) BITBOARD_WN else BITBOARD_BN] and knightMoves[attackedSquare] != 0L ||
                pieceBitboards[if (attacker == Colour.WHITE) BITBOARD_WK else BITBOARD_BK] and kingMoves[attackedSquare] != 0L ||
                (pieceBitboards[if (attacker == Colour.WHITE) BITBOARD_WP else BITBOARD_BP] and
                        getPawnMovesCaptureOfColour(attacker.opponent())[attackedSquare]) != 0L) return true

        applyToSquares(getBishopMovePiecesBitboard(attacker)) {
            if (isBishopAttackingSquare(attackedSquare, it, pieceBitboards[BITBOARD_ALL])) return true
        }

        applyToSquares(getRookMovePiecesBitboard(attacker)) {
            if (isRookAttackingSquare(attackedSquare, it, pieceBitboards[BITBOARD_ALL])) return true
        }

        return false
    }
}
