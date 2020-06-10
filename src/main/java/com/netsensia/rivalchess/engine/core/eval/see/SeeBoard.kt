package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.bitboards.kingMoves
import com.netsensia.rivalchess.bitboards.knightMoves
import com.netsensia.rivalchess.bitboards.util.getPawnMovesCaptureOfColour
import com.netsensia.rivalchess.bitboards.util.squareSequence
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import java.lang.Long.numberOfTrailingZeros
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

class SeeBoard(board: EngineBoard) {
    val bitboardMap: MutableMap<Int, Long> = mutableMapOf()
    private val lastBitboardMap: MutableMap<Int, Long> = mutableMapOf()
    private val whiteList = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    private val blackList = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)
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

    fun makeMove(move: EngineMove): Boolean {
        lastBitboardMap.clear()
        lastBitboardMap.putAll(bitboardMap)

        lastFromBit = 1L shl move.from()
        lastToBit = 1L shl move.to()
        lastEnPassantBitboard = bitboardMap[BITBOARD_ENPASSANTSQUARE]!!

        val movedPieceBitboardType = removeFromRelevantBitboard(lastFromBit, if (mover == Colour.BLACK) blackList else whiteList)
        val capturedPieceBitboardType = removeFromRelevantBitboard(lastToBit, if (mover == Colour.WHITE) blackList else whiteList)
        togglePiece(lastToBit, movedPieceBitboardType)

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

        if (isCheck(mover.opponent())) {
            unMakeMove()
            return false
        }

        return true
    }

    private fun isCheck(colour: Colour) =
            if (colour == Colour.WHITE) isSquareAttackedBy(numberOfTrailingZeros(bitboardMap[BITBOARD_WK]!!), Colour.BLACK)
            else isSquareAttackedBy(numberOfTrailingZeros(bitboardMap[BITBOARD_BK]!!), Colour.WHITE)

    private fun isSquareAttackedBy(attackedSquare: Int, attacker: Colour) =
            bitboardMap[SquareOccupant.WN.ofColour(attacker).index]!! and knightMoves[attackedSquare] != 0L ||
                    bitboardMap[SquareOccupant.WK.ofColour(attacker).index]!! and kingMoves[attackedSquare] != 0L ||
                    (bitboardMap[SquareOccupant.WP.ofColour(attacker).index]!! and getPawnMovesCaptureOfColour(attacker.opponent())[attackedSquare]) != 0L

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

    val whitePieceValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_WN]!!) * pieceValue(Piece.KNIGHT) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WR]!!) * pieceValue(Piece.ROOK) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WB]!!) * pieceValue(Piece.BISHOP) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WQ]!!) * pieceValue(Piece.QUEEN)

    val blackPieceValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_BN]!!) * pieceValue(Piece.KNIGHT) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BR]!!) * pieceValue(Piece.ROOK) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BB]!!) * pieceValue(Piece.BISHOP) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BQ]!!) * pieceValue(Piece.QUEEN)

    val whitePawnValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_WP]!!) * pieceValue(Piece.PAWN)

    val blackPawnValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_BP]!!) * pieceValue(Piece.PAWN)

    fun generateCaptureMovesOnSquare(square: Int) = sequence {
        val knightBitboard = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK]!!
        val knightMoves = knightMoves[square]
        squareSequence(knightMoves and knightBitboard).forEach {
            yield(EngineMove((it shl 16) or square))
        }
        yield(EngineMove(12))
    }
}