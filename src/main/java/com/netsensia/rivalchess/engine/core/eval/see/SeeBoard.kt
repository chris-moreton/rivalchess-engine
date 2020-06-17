package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.eval.*
import com.netsensia.rivalchess.engine.core.search.fromSquare
import com.netsensia.rivalchess.engine.core.search.toSquare
import com.netsensia.rivalchess.enums.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN
import com.netsensia.rivalchess.model.*
import java.lang.Long.bitCount
import java.lang.Long.numberOfTrailingZeros

const val VALUE_PAWN_PROMOTION_TO_QUEEN = VALUE_QUEEN - VALUE_PAWN

class SeeBoard(board: EngineBoard) {
    @JvmField
    val bitboards = EngineBitboards(board.engineBitboards)

    private val whiteBitboardIndexes = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    private val blackBitboardIndexes = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)

    private val enPassantHistory: MutableList<Long> = ArrayList()
    private val moveHistory: MutableList<List<Pair<Int, Long>>> = ArrayList()

    var mover: Colour

    var capturedPieceBitboardType: Int = BITBOARD_NONE

    init {
        mover = board.mover
    }

    fun makeMove(move: Int): Int {
        val deltas: MutableList<Pair<Int, Long>> = mutableListOf()

        enPassantHistory.add(bitboards.pieceBitboards[BITBOARD_ENPASSANTSQUARE])

        val moveFrom = fromSquare(move)
        val moveTo = toSquare(move)
        val fromBit = 1L shl moveFrom
        val toBit = 1L shl moveTo

        val movedPieceBitboardType = removeFromRelevantBitboard(fromBit, if (mover == Colour.BLACK) blackBitboardIndexes else whiteBitboardIndexes, deltas)
        capturedPieceBitboardType = removeFromRelevantBitboard(toBit, if (mover == Colour.BLACK) whiteBitboardIndexes else blackBitboardIndexes, deltas)

        var materialGain = if (capturedPieceBitboardType == BITBOARD_NONE) {
            if ((moveTo - moveFrom) % 2 != 0) {
                if (movedPieceBitboardType == BITBOARD_WP) togglePiece(1L shl (moveTo - 8), BITBOARD_BP, deltas)
                else if (movedPieceBitboardType == BITBOARD_BP) togglePiece(1L shl (moveTo + 8), BITBOARD_WP, deltas)
            }
            VALUE_PAWN
        } else pieceValue(capturedPieceBitboardType)

        togglePiece(toBit, movedPieceBitboardType, deltas)

        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)

        if (movedPieceBitboardType == BITBOARD_WP) {
            if (moveTo - moveFrom == 16) bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shr 8)
            else if (moveTo >= 56) {
                togglePiece(1L shl moveTo, BITBOARD_WP, deltas)
                togglePiece(1L shl moveTo, BITBOARD_WQ, deltas)
                materialGain += VALUE_PAWN_PROMOTION_TO_QUEEN
            }
        } else if (movedPieceBitboardType == BITBOARD_BP) {
            if (moveFrom - moveTo == 16) bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shl 8)
            else if (moveTo <= 7) {
                togglePiece(1L shl moveTo, BITBOARD_BP, deltas)
                togglePiece(1L shl moveTo, BITBOARD_BQ, deltas)
                materialGain += VALUE_PAWN_PROMOTION_TO_QUEEN
            }
        }

        mover = mover.opponent()
        moveHistory.add(deltas)
        return materialGain
    }

    fun unMakeMove() {
        val lastIndex = moveHistory.size-1
        moveHistory[lastIndex].forEach { bitboards.xorPieceBitboard(it.first, it.second) }
        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, enPassantHistory[enPassantHistory.size-1])
        mover = mover.opponent()
        moveHistory.removeAt(lastIndex)
        enPassantHistory.removeAt(lastIndex)
    }

    private fun removeFromRelevantBitboard(squareBit: Long, bitboardList: List<Int>, deltas: MutableList<Pair<Int, Long>>): Int {
        bitboardList.forEach { if (bitboards.pieceBitboards[it] and squareBit == squareBit) {
            togglePiece(squareBit, it, deltas)
            return it
        } }
        return BITBOARD_NONE
    }

    private fun togglePiece(squareBit: Long, bitboardType: Int, deltas: MutableList<Pair<Int, Long>>) {
        bitboards.xorPieceBitboard(bitboardType, squareBit)
        deltas.add(Pair(bitboardType, squareBit))
    }

    val whitePieceValues: Int
        get() = bitCount(bitboards.pieceBitboards[BITBOARD_WN]) * VALUE_KNIGHT +
                bitCount(bitboards.pieceBitboards[BITBOARD_WR]) * VALUE_ROOK +
                bitCount(bitboards.pieceBitboards[BITBOARD_WB]) * VALUE_BISHOP +
                bitCount(bitboards.pieceBitboards[BITBOARD_WQ]) * VALUE_QUEEN +
                bitCount(bitboards.pieceBitboards[BITBOARD_WP]) * VALUE_PAWN

    val blackPieceValues: Int
        get() = bitCount(bitboards.pieceBitboards[BITBOARD_BN]) * VALUE_KNIGHT +
                bitCount(bitboards.pieceBitboards[BITBOARD_BR]) * VALUE_ROOK +
                bitCount(bitboards.pieceBitboards[BITBOARD_BB]) * VALUE_BISHOP +
                bitCount(bitboards.pieceBitboards[BITBOARD_BQ]) * VALUE_QUEEN +
                bitCount(bitboards.pieceBitboards[BITBOARD_BP]) * VALUE_PAWN

    fun generateCaptureMovesOnSquare(square: Int): List<Int> {

        val moves = mutableListOf<Int>()

        knightCaptures(square, moves)
        kingCaptures(square, moves)
        pawnCaptures(square, moves)

        val whiteBitboard = bitboards.getWhitePieces()
        val blackBitboard = bitboards.getBlackPieces()

        rookCaptures(square, moves, whiteBitboard or blackBitboard, if (mover == Colour.WHITE) whiteBitboard else blackBitboard)
        bishopCaptures(square, moves, whiteBitboard or blackBitboard, if (mover == Colour.WHITE) whiteBitboard else blackBitboard)

        return moves
    }

    private fun pawnCaptures(square: Int, moves: MutableList<Int>) {
        val pawnLocations = bitboards.pieceBitboards[if (mover == Colour.WHITE) BITBOARD_WP else BITBOARD_BP]
        val pawnCaptureMoves = if (mover == Colour.WHITE) blackPawnMovesCapture[square] else whitePawnMovesCapture[square]

        applyToSquares(pawnCaptureMoves and pawnLocations) {
            if (square >= 56 || square <= 7)
                moves.add((((it shl 16) or square) or PROMOTION_PIECE_TOSQUARE_MASK_QUEEN))
            else
                moves.add(((it shl 16) or square))
        }
    }

    private fun kingCaptures(square: Int, moves: MutableList<Int>) {
        val kingLocation = bitboards.pieceBitboards[if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK]
        if (kingMoves[square] and kingLocation != 0L) {
            moves.add(((numberOfTrailingZeros(kingLocation) shl 16) or square))
        }
    }

    private fun knightCaptures(square: Int, moves: MutableList<Int>) {
        val knightLocations = bitboards.pieceBitboards[if (mover == Colour.WHITE) BITBOARD_WN else BITBOARD_BN]
        applyToSquares(knightMoves[square] and knightLocations) {
            moves.add(((it shl 16) or square))
        }
    }

    private fun bishopCaptures(square: Int, moves: MutableList<Int>, allBitboard: Long, friendlyBitboard: Long) {
        generateSliderMoves(
                if (mover == Colour.WHITE)
                    bitboards.pieceBitboards[SquareOccupant.WB.index] or bitboards.pieceBitboards[BITBOARD_WQ] else
                    bitboards.pieceBitboards[SquareOccupant.BB.index] or bitboards.pieceBitboards[BITBOARD_BQ],
                MagicBitboards.bishopVars,
                allBitboard,
                friendlyBitboard,
                square,
                moves
        )
    }

    private fun rookCaptures(square: Int, moves: MutableList<Int>, allBitboard: Long, friendlyBitboard: Long) {
        generateSliderMoves(
                if (mover == Colour.WHITE)
                    bitboards.pieceBitboards[SquareOccupant.WR.index] or bitboards.pieceBitboards[BITBOARD_WQ] else
                    bitboards.pieceBitboards[SquareOccupant.BR.index] or bitboards.pieceBitboards[BITBOARD_BQ],
                MagicBitboards.rookVars,
                allBitboard,
                friendlyBitboard,
                square,
                moves
        )
    }

    private fun generateSliderMoves(
            bitboard: Long,
            magicVars: MagicVars,
            allBitboard: Long,
            friendlyBitboard: Long,
            toSquare: Int,
            moves: MutableList<Int>
    ) {
        val friendlyBitboardInverted = friendlyBitboard.inv()

        applyToSquares(bitboard) {
            val moveToBitboard = magicVars.moves[it][((allBitboard and magicVars.mask[it]) *
                            magicVars.number[it] ushr magicVars.shift[it]).toInt()] and friendlyBitboardInverted

            if (moveToBitboard and (1L shl toSquare) != 0L) moves.add(((it shl 16) or toSquare))
        }
    }
}