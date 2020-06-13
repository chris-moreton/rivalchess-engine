package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.squareSequence
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.*
import java.lang.Long.bitCount

class SeeBoard(board: EngineBoard) {
    val bitboards = EngineBitboards(board.engineBitboards)
    private val whiteList = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    private val blackList = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)

    var deltas: MutableList<Pair<Int, Long>> = mutableListOf()
    var enpassantHistory: MutableList<Long> = ArrayList()
    var moveHistory: MutableList<MutableList<Pair<Int, Long>>> = ArrayList()

    var mover: Colour

    init {
        mover = board.mover
    }

    fun makeMove(move: EngineMove): Int {
        deltas.clear()
        enpassantHistory.add(bitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE))


        val fromBit = 1L shl move.from()
        val toBit = 1L shl move.to()

        val movedPieceBitboardType = removeFromRelevantBitboard(fromBit, if (mover == Colour.BLACK) blackList else whiteList)
        val capturedPieceBitboardType = removeFromRelevantBitboard(toBit, if (mover == Colour.BLACK) whiteList else blackList)
        var materialGain = if (capturedPieceBitboardType == BITBOARD_NONE) pieceValue(Piece.PAWN) else pieceValue(capturedPieceBitboardType)

        togglePiece(toBit, movedPieceBitboardType)

        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)

        if (movedPieceBitboardType == BITBOARD_WP) {
            if (move.to() - move.from() == 16) bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shr 8)
            if (move.to() >= 56) {
                togglePiece(1L shl move.to(), BITBOARD_WP)
                togglePiece(1L shl move.to(), BITBOARD_WQ)
                materialGain += pieceValue(Piece.QUEEN) - pieceValue(Piece.PAWN)
            }
        } else if (movedPieceBitboardType == BITBOARD_BP) {
            if (move.from() - move.to() == 16) bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toBit shl 8)
            if (move.to() <= 7) {
                togglePiece(1L shl move.to(), BITBOARD_BP)
                togglePiece(1L shl move.to(), BITBOARD_BQ)
                materialGain += pieceValue(Piece.QUEEN) - pieceValue(Piece.PAWN)
            }
        }

        if (capturedPieceBitboardType == BITBOARD_NONE) {
            if (movedPieceBitboardType == BITBOARD_WP && (move.to() - move.from()) % 2 != 0) {
                togglePiece(1L shl (move.to() - 8), BITBOARD_BP)
            } else if (movedPieceBitboardType == BITBOARD_BP && (move.to() - move.from()) % 2 != 0) {
                togglePiece(1L shl (move.to() + 8), BITBOARD_WP)
            }
        }

        mover = mover.opponent()

        moveHistory.add(deltas.toMutableList())

        return materialGain
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

    private fun togglePiece(squareBit: Long, bitboardType: Int) {
        bitboards.xorPieceBitboard(bitboardType, squareBit)
        deltas.add(Pair(bitboardType, squareBit))
    }

    @ExperimentalStdlibApi
    fun unMakeMove() {
        moveHistory[moveHistory.size-1].forEach {bitboards.xorPieceBitboard(it.first, it.second)}
        bitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, enpassantHistory[enpassantHistory.size-1])
        mover = mover.opponent()
        moveHistory.removeLast()
        enpassantHistory.removeLast()
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

    fun generateCaptureMovesOnSquare(square: Int) = sequence {
        yieldAll(pawnCaptures(square))
        yieldAll(knightCaptures(square))
        yieldAll(kingCaptures(square))

        val whiteBitboard = bitboards.getPieceBitboard(BITBOARD_WK) or
                bitboards.getPieceBitboard(BITBOARD_WN) or
                bitboards.getPieceBitboard(BITBOARD_WQ) or
                bitboards.getPieceBitboard(BITBOARD_WB) or
                bitboards.getPieceBitboard(BITBOARD_WR) or
                bitboards.getPieceBitboard(BITBOARD_WP)

        val blackBitboard = bitboards.getPieceBitboard(BITBOARD_BK) or
                bitboards.getPieceBitboard(BITBOARD_BN) or
                bitboards.getPieceBitboard(BITBOARD_BQ) or
                bitboards.getPieceBitboard(BITBOARD_BB) or
                bitboards.getPieceBitboard(BITBOARD_BR) or
                bitboards.getPieceBitboard(BITBOARD_BP)

        val moverBitboard = if (mover == Colour.WHITE) whiteBitboard else blackBitboard

        yieldAll(rookCaptures(square, whiteBitboard or blackBitboard, moverBitboard))
        yieldAll(bishopCaptures(square, whiteBitboard or blackBitboard, moverBitboard))
    }

    private fun pawnCaptures(square: Int) = sequence {
        val pawnLocations = bitboards.getPieceBitboard(if (mover == Colour.WHITE) BITBOARD_WP else BITBOARD_BP)
        val pawnCaptureMoves = if (mover == Colour.WHITE) blackPawnMovesCapture[square] else whitePawnMovesCapture[square]
        squareSequence(pawnCaptureMoves and pawnLocations).forEach {
            if (square >= 56 || square <= 7)
                yield(EngineMove(((it shl 16) or square) or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value))
            else
                yield(EngineMove((it shl 16) or square))
        }
    }

    private fun kingCaptures(square: Int) = sequence {
        val kingLocations = bitboards.getPieceBitboard(if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK)
        squareSequence(kingMoves[square] and kingLocations).forEach { yield(EngineMove((it shl 16) or square)) }
    }

    private fun knightCaptures(square: Int) = sequence {
        val knightLocations = bitboards.getPieceBitboard(if (mover == Colour.WHITE) BITBOARD_WN else BITBOARD_BN)
        squareSequence(knightMoves[square] and knightLocations).forEach { yield(EngineMove((it shl 16) or square)) }
    }

    private fun bishopCaptures(square: Int, allBitboard: Long, friendlyBitboard: Long) = sequence {
        yieldAll(generateSliderMoves(
                SquareOccupant.WB,
                SquareOccupant.BB,
                MagicBitboards.bishopVars,
                allBitboard,
                friendlyBitboard,
                square)
        )
    }

    private fun rookCaptures(square: Int, allBitboard: Long, friendlyBitboard: Long) = sequence {
        yieldAll(generateSliderMoves(
                SquareOccupant.WR,
                SquareOccupant.BR,
                MagicBitboards.rookVars,
                allBitboard,
                friendlyBitboard,
                square
        ))
    }

    private fun generateSliderMoves(
            whitePiece: SquareOccupant,
            blackPiece: SquareOccupant,
            magicVars: MagicVars,
            allBitboard: Long,
            friendlyBitboard: Long,
            toSquare: Int
    ) = sequence {

        val bitboard: Long = if (mover == Colour.WHITE)
            bitboards.getPieceBitboard(whitePiece.index) or bitboards.getPieceBitboard(BITBOARD_WQ) else
            bitboards.getPieceBitboard(blackPiece.index) or bitboards.getPieceBitboard(BITBOARD_BQ)

        squareSequence(bitboard).forEach {
            val moveToBitboard =
                    magicVars.moves[it][((allBitboard and magicVars.mask[it]) *
                            magicVars.number[it] ushr magicVars.shift[it]).toInt()] and
                            friendlyBitboard.inv()

            if (moveToBitboard and (1L shl toSquare) != 0L) {
                yield(EngineMove((it shl 16) or toSquare))
            }
        }
    }
}