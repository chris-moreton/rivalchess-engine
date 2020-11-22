package com.netsensia.rivalchess.engine.eval.see

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToFirstSquare
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.eval.pieceValue
import com.netsensia.rivalchess.engine.search.fromSquare
import com.netsensia.rivalchess.engine.search.toSquare
import com.netsensia.rivalchess.model.Colour
import java.lang.Long.numberOfTrailingZeros

val VALUE_PAWN_PROMOTION_TO_QUEEN = pieceValue(BITBOARD_WQ) - pieceValue(BITBOARD_WP)

@kotlin.ExperimentalUnsignedTypes
class SeeBoard(board: EngineBoard) {
    val bitboards = EngineBitboards(board.engineBitboards)
    @JvmField
    var mover = board.mover
    
    var capturedPieceBitboardType: Int = BITBOARD_NONE

    fun makeMove(move: Int): Int {

        val moveFrom = fromSquare(move)
        val moveTo = toSquare(move)
        val fromBit = 1L shl moveFrom
        val toBit = 1L shl moveTo

        capturedPieceBitboardType = removeFromRelevantBitboard(toBit, if (mover == Colour.BLACK) whiteBitboardIndexes else blackBitboardIndexes)
        if (capturedPieceBitboardType == BITBOARD_WK || capturedPieceBitboardType == BITBOARD_BK) return Int.MAX_VALUE

        val movedPieceBitboardType = removeFromRelevantBitboard(fromBit, if (mover == Colour.BLACK) blackBitboardIndexes else whiteBitboardIndexes)

        var materialGain = if (capturedPieceBitboardType == BITBOARD_NONE) {
            if ((moveTo - moveFrom) % 2 != 0) {
                if (movedPieceBitboardType == BITBOARD_WP)
                    bitboards.xorPieceBitboard(BITBOARD_BP, 1L shl (moveTo - 8))
                else if (movedPieceBitboardType == BITBOARD_BP) 
                    bitboards.xorPieceBitboard(BITBOARD_WP, 1L shl (moveTo + 8))
            }
            pieceValue(BITBOARD_WP)
        } else {
            pieceValue(capturedPieceBitboardType)
        }

        bitboards.xorPieceBitboard(movedPieceBitboardType, toBit)
        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)

        if (movedPieceBitboardType == BITBOARD_WP) {
            if (moveTo - moveFrom == 16)
                bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shr 8)
            else if (moveTo >= 56) {
                val moveToBit = 1L shl moveTo
                bitboards.xorPieceBitboard(BITBOARD_WP, moveToBit)
                bitboards.xorPieceBitboard(BITBOARD_WQ, moveToBit)
                materialGain += VALUE_PAWN_PROMOTION_TO_QUEEN
            }
        } else if (movedPieceBitboardType == BITBOARD_BP) {
            if (moveFrom - moveTo == 16)
                bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shl 8)
            else if (moveTo <= 7) {
                val moveToBit = 1L shl moveTo
                bitboards.xorPieceBitboard(BITBOARD_BP, moveToBit)
                bitboards.xorPieceBitboard(BITBOARD_BQ, moveToBit)
                materialGain += VALUE_PAWN_PROMOTION_TO_QUEEN
            }
        }

        mover = mover.opponent()
        return materialGain
    }

    private fun removeFromRelevantBitboard(squareBit: Long, bitboardList: IntArray): Int {
        for (bitboard in bitboardList) {
            if (bitboards.pieceBitboards[bitboard] and squareBit == squareBit) {
                bitboards.xorPieceBitboard(bitboard, squareBit)
                return bitboard
            }
        }
        return BITBOARD_NONE
    }
    
    fun getLvaCaptureMove(square: Int): Int {

        pawnCaptures(square).also { if (it != 0) return it }
        knightCaptures(square).also { if (it != 0) return it }

        val whiteBitboard = bitboards.getWhitePieces()
        val blackBitboard = bitboards.getBlackPieces()
        val allBitboard = whiteBitboard or blackBitboard
        val moverBitboard = if (mover == Colour.WHITE) whiteBitboard else blackBitboard

        val friendlyBitboardInverted = moverBitboard.inv()
        bishopCaptures(square, allBitboard, friendlyBitboardInverted).also { if (it != 0) return it }
        rookCaptures(square, allBitboard, friendlyBitboardInverted).also { if (it != 0) return it }
        queenCaptures(square, allBitboard, friendlyBitboardInverted).also { if (it != 0) return it }
        kingCaptures(square).also { if (it != 0) return it }

        return 0
    }

    private fun pawnCaptures(square: Int): Int {
        val pawnLocations = bitboards.pieceBitboards[if (mover == Colour.WHITE) BITBOARD_WP else BITBOARD_BP]
        val pawnCaptureMoves = if (mover == Colour.WHITE) blackPawnMovesCapture[square] else whitePawnMovesCapture[square]
        if (square >= 56 || square <= 7)
            applyToFirstSquare(pawnCaptureMoves and pawnLocations) {
                return ((it shl 16) or square) or PROMOTION_PIECE_TOSQUARE_MASK_QUEEN
            }
        else
            applyToFirstSquare(pawnCaptureMoves and pawnLocations) {
                return ((it shl 16) or square)
            }
        return 0
    }

    private fun kingCaptures(square: Int): Int {
        val kingLocation = bitboards.pieceBitboards[if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK]
        if (kingMoves[square] and kingLocation != 0L) return ((numberOfTrailingZeros(kingLocation) shl 16) or square)
        return 0
    }

    private fun knightCaptures(square: Int): Int {
        val knightLocations = bitboards.pieceBitboards[if (mover == Colour.WHITE) BITBOARD_WN else BITBOARD_BN]
        applyToFirstSquare(knightMoves[square] and knightLocations) { capturingKnightSquare ->
            return ((capturingKnightSquare shl 16) or square)
        }
        return 0
    }

    private fun bishopCaptures(square: Int, allBitboard: Long, friendlyBitboardInverted: Long) =
        generateSliderMoves(
                if (mover == Colour.WHITE) bitboards.pieceBitboards[BITBOARD_WB] else bitboards.pieceBitboards[BITBOARD_BB],
                MagicBitboards.bishopVars,
                allBitboard,
                friendlyBitboardInverted,
                square
        )

    private fun rookCaptures(square: Int, allBitboard: Long, friendlyBitboardInverted: Long) =
        generateSliderMoves(
                if (mover == Colour.WHITE) bitboards.pieceBitboards[BITBOARD_WR] else bitboards.pieceBitboards[BITBOARD_BR],
                MagicBitboards.rookVars,
                allBitboard,
                friendlyBitboardInverted,
                square
        )

    private fun queenCaptures(square: Int, allBitboard: Long, friendlyBitboardInverted: Long): Int {
        val queenLocations = if (mover == Colour.WHITE) bitboards.pieceBitboards[BITBOARD_WQ] else bitboards.pieceBitboards[BITBOARD_BQ]
        generateSliderMoves(
                queenLocations,
                MagicBitboards.rookVars,
                allBitboard,
                friendlyBitboardInverted,
                square
        ).also { if (it != 0) return it }

        generateSliderMoves(
                queenLocations,
                MagicBitboards.bishopVars,
                allBitboard,
                friendlyBitboardInverted,
                square
        ).also { if (it != 0) return it }

        return 0
    }

    private fun generateSliderMoves(
            bitboard: Long,
            magicVars: MagicVars,
            allBitboard: Long,
            friendlyBitboardInverted: Long,
            toSquare: Int
    ): Int {
        applyToSquares(bitboard) {
            val moveToBitboard = magicVars.moves[it][((allBitboard and magicVars.mask[it]) *
                            magicVars.number[it] ushr magicVars.shift[it]).toInt()] and friendlyBitboardInverted

            if (moveToBitboard and (1L shl toSquare) != 0L) return ((it shl 16) or toSquare)
        }
        return 0
    }
}