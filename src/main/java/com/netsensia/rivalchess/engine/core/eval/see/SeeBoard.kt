package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.search.moveFrom
import com.netsensia.rivalchess.engine.core.search.moveTo
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.*
import java.lang.Long.bitCount
import java.lang.Long.numberOfTrailingZeros

class SeeBoard(board: EngineBoard) {
    @JvmField
    val bitboards = EngineBitboards(board.engineBitboards)

    private val whiteBitboardIndexes = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    private val blackBitboardIndexes = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)

    private val deltas: MutableList<Pair<Int, Long>> = mutableListOf()
    private val enPassantHistory: MutableList<Long> = ArrayList()
    private val moveHistory: MutableList<List<Pair<Int, Long>>> = ArrayList()

    var mover: Colour

    var capturedPieceBitboardType: Int = BITBOARD_NONE

    init {
        mover = board.mover
    }

    fun makeMove(move: Int): Int {
        deltas.clear()
        enPassantHistory.add(bitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE))

        val moveFrom = moveFrom(move)
        val moveTo = moveTo(move)
        val fromBit = 1L shl moveFrom
        val toBit = 1L shl moveTo

        val movedPieceBitboardType = removeFromRelevantBitboard(fromBit, if (mover == Colour.BLACK) blackBitboardIndexes else whiteBitboardIndexes)
        capturedPieceBitboardType = removeFromRelevantBitboard(toBit, if (mover == Colour.BLACK) whiteBitboardIndexes else blackBitboardIndexes)

        var materialGain = if (capturedPieceBitboardType == BITBOARD_NONE) {
            if ((moveTo - moveFrom) % 2 != 0) {
                if (movedPieceBitboardType == BITBOARD_WP) togglePiece(1L shl (moveTo - 8), BITBOARD_BP)
                else if (movedPieceBitboardType == BITBOARD_BP) togglePiece(1L shl (moveTo + 8), BITBOARD_WP)
            }
            pieceValue(Piece.PAWN)
        } else pieceValue(capturedPieceBitboardType)

        togglePiece(toBit, movedPieceBitboardType)

        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)

        if (movedPieceBitboardType == BITBOARD_WP) {
            if (moveTo - moveFrom == 16) bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shr 8)
            if (moveTo >= 56) {
                togglePiece(1L shl moveTo, BITBOARD_WP)
                togglePiece(1L shl moveTo, BITBOARD_WQ)
                materialGain += pieceValue(Piece.QUEEN) - pieceValue(Piece.PAWN)
            }
        } else if (movedPieceBitboardType == BITBOARD_BP) {
            if (moveFrom - moveTo == 16) bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shl 8)
            if (moveTo <= 7) {
                togglePiece(1L shl moveTo, BITBOARD_BP)
                togglePiece(1L shl moveTo, BITBOARD_BQ)
                materialGain += pieceValue(Piece.QUEEN) - pieceValue(Piece.PAWN)
            }
        }

        mover = mover.opponent()

        moveHistory.add(deltas.toList())

        return materialGain
    }

    @ExperimentalStdlibApi
    fun unMakeMove() {
        val lastIndex = moveHistory.size-1
        moveHistory[lastIndex].forEach { bitboards.xorPieceBitboard(it.first, it.second) }
        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, enPassantHistory[enPassantHistory.size-1])
        mover = mover.opponent()
        moveHistory.removeAt(lastIndex)
        enPassantHistory.removeAt(lastIndex)
    }

    private fun removePieceIfExistsInBitboard(squareBit: Long, bitboardType: Int): Boolean {
        val pieceBitboard = bitboards.getPieceBitboard(bitboardType)
        if (pieceBitboard or squareBit == pieceBitboard) {
            togglePiece(squareBit, bitboardType)
            return true
        }
        return false
    }

    private fun removeFromRelevantBitboard(squareBit: Long, bitboardList: List<Int>): Int {
        bitboardList.forEach { if (removePieceIfExistsInBitboard(squareBit, it)) return it }
        return BITBOARD_NONE
    }

    private inline fun togglePiece(squareBit: Long, bitboardType: Int) {
        bitboards.xorPieceBitboard(bitboardType, squareBit)
        deltas.add(Pair(bitboardType, squareBit))
    }

    val whitePieceValues: Int
        get() = bitCount(bitboards.getPieceBitboard(BITBOARD_WN)) * pieceValue(Piece.KNIGHT) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_WR)) * pieceValue(Piece.ROOK) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_WB)) * pieceValue(Piece.BISHOP) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_WQ)) * pieceValue(Piece.QUEEN) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_WP)) * pieceValue(Piece.PAWN)

    val blackPieceValues: Int
        get() = bitCount(bitboards.getPieceBitboard(BITBOARD_BN)) * pieceValue(Piece.KNIGHT) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_BR)) * pieceValue(Piece.ROOK) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_BB)) * pieceValue(Piece.BISHOP) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_BQ)) * pieceValue(Piece.QUEEN) +
                bitCount(bitboards.getPieceBitboard(BITBOARD_BP)) * pieceValue(Piece.PAWN)

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
        val pawnLocations = bitboards.getPieceBitboard(if (mover == Colour.WHITE) BITBOARD_WP else BITBOARD_BP)
        val pawnCaptureMoves = if (mover == Colour.WHITE) blackPawnMovesCapture[square] else whitePawnMovesCapture[square]

        applyToSquares(pawnCaptureMoves and pawnLocations) {
            if (square >= 56 || square <= 7)
                moves.add((((it shl 16) or square) or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value))
            else
                moves.add(((it shl 16) or square))
        }
    }

    private fun kingCaptures(square: Int, moves: MutableList<Int>) {
        val kingLocation = bitboards.getPieceBitboard(if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK)
        if (kingMoves[square] and kingLocation != 0L) {
            moves.add(((numberOfTrailingZeros(kingLocation) shl 16) or square))
        }
    }

    private fun knightCaptures(square: Int, moves: MutableList<Int>) {
        val knightLocations = bitboards.getPieceBitboard(if (mover == Colour.WHITE) BITBOARD_WN else BITBOARD_BN)
        applyToSquares(knightMoves[square] and knightLocations) { moves.add(((it shl 16) or square)) }
    }

    private fun bishopCaptures(square: Int, moves: MutableList<Int>, allBitboard: Long, friendlyBitboard: Long) {
        generateSliderMoves(
                SquareOccupant.WB,
                SquareOccupant.BB,
                MagicBitboards.bishopVars,
                allBitboard,
                friendlyBitboard,
                square,
                moves
        )
    }

    private fun rookCaptures(square: Int, moves: MutableList<Int>, allBitboard: Long, friendlyBitboard: Long) {
        generateSliderMoves(
                SquareOccupant.WR,
                SquareOccupant.BR,
                MagicBitboards.rookVars,
                allBitboard,
                friendlyBitboard,
                square,
                moves
        )
    }

    private fun generateSliderMoves(
            whitePiece: SquareOccupant,
            blackPiece: SquareOccupant,
            magicVars: MagicVars,
            allBitboard: Long,
            friendlyBitboard: Long,
            toSquare: Int,
            moves: MutableList<Int>
    ) {

        val bitboard: Long = if (mover == Colour.WHITE)
            bitboards.getPieceBitboard(whitePiece.index) or bitboards.getPieceBitboard(BITBOARD_WQ) else
            bitboards.getPieceBitboard(blackPiece.index) or bitboards.getPieceBitboard(BITBOARD_BQ)

        applyToSquares(bitboard) {
            val moveToBitboard = magicVars.moves[it][((allBitboard and magicVars.mask[it]) *
                            magicVars.number[it] ushr magicVars.shift[it]).toInt()] and
                            friendlyBitboard.inv()

            if (moveToBitboard and (1L shl toSquare) != 0L) moves.add(((it shl 16) or toSquare))
        }
    }
}