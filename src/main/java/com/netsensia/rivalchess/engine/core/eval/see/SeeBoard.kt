package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.bitboards.blackPawnMovesCapture
import com.netsensia.rivalchess.bitboards.kingMoves
import com.netsensia.rivalchess.bitboards.knightMoves
import com.netsensia.rivalchess.bitboards.util.squareSequence
import com.netsensia.rivalchess.bitboards.whitePawnMovesCapture
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.board.getFen
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.*
import com.netsensia.rivalchess.model.exception.EnumConversionException

class SeeBoard(board: EngineBoard) {
    val bitboardMap: MutableMap<Int, Long> = mutableMapOf()
    private val bitboardMapHistory: MutableMap<Int, MutableMap<Int, Long>> = mutableMapOf()
    private val whiteList = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    private val blackList = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)

    var mover: Colour
    val board: EngineBoard = board
    var movePointer = 0

    init {
        whiteList.forEach { bitboardMap.put(it, board.engineBitboards.getPieceBitboard(it)) }
        blackList.forEach { bitboardMap.put(it, board.engineBitboards.getPieceBitboard(it)) }
        bitboardMap.put(BITBOARD_ENPASSANTSQUARE, board.engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE))
        mover = board.mover
    }

    fun makeMove(move: EngineMove): Boolean {
        bitboardMapHistory.put(movePointer, HashMap(bitboardMap))
        movePointer ++

        val fromBit = 1L shl move.from()
        val toBit = 1L shl move.to()

        val movedPieceBitboardType = removeFromRelevantBitboard(fromBit, if (mover == Colour.BLACK) blackList else whiteList)
        val capturedPieceBitboardType = removeFromRelevantBitboard(toBit, if (mover == Colour.WHITE) blackList else whiteList)
        togglePiece(toBit, movedPieceBitboardType)

        if (mover == Colour.WHITE) {
            if (movedPieceBitboardType == BITBOARD_WP && move.to() - move.from() == 16) {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = toBit shr 8
            } else {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = 0
            }
            if (movedPieceBitboardType == BITBOARD_WP && move.to() >= 56) {
                togglePiece(1L shl move.to(), BITBOARD_WP)
                togglePiece(1L shl move.to(), BITBOARD_WQ)
            }
        } else {
            if (movedPieceBitboardType == BITBOARD_BP && move.from() - move.to() == 16) {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = toBit shl 8
            } else {
                bitboardMap[BITBOARD_ENPASSANTSQUARE] = 0
            }
            if (movedPieceBitboardType == BITBOARD_BP && move.to() >= 56) {
                togglePiece(1L shl move.to(), BITBOARD_BP)
                togglePiece(1L shl move.to(), BITBOARD_BQ)
            }
        }

        if (capturedPieceBitboardType == BITBOARD_NONE) {
            if (movedPieceBitboardType == BITBOARD_WP) {
                if ((move.to() - move.from()) % 2 != 0) {
                    togglePiece(1L shl (move.to() - 8), BITBOARD_BP)
                }
            } else if (movedPieceBitboardType == BITBOARD_BP) {
                if ((move.to() - move.from()) % 2 != 0) {
                    togglePiece(1L shl (move.to() - 8), BITBOARD_WP)
                }
            }
        }

        mover = mover.opponent()

        return true
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
        movePointer--
        bitboardMap.clear()
        bitboardMap.putAll(bitboardMapHistory[movePointer]!!)
        mover = mover.opponent()
    }

    val whitePieceValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_WK]!!) * pieceValue(Piece.KING) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WN]!!) * pieceValue(Piece.KNIGHT) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WR]!!) * pieceValue(Piece.ROOK) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WB]!!) * pieceValue(Piece.BISHOP) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_WQ]!!) * pieceValue(Piece.QUEEN)

    val blackPieceValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_BK]!!) * pieceValue(Piece.KING) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BN]!!) * pieceValue(Piece.KNIGHT) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BR]!!) * pieceValue(Piece.ROOK) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BB]!!) * pieceValue(Piece.BISHOP) +
                java.lang.Long.bitCount(bitboardMap[BITBOARD_BQ]!!) * pieceValue(Piece.QUEEN)

    val whitePawnValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_WP]!!) * pieceValue(Piece.PAWN)

    val blackPawnValues: Int
        get() = java.lang.Long.bitCount(bitboardMap[BITBOARD_BP]!!) * pieceValue(Piece.PAWN)

    fun generateCaptureMovesOnSquare(square: Int) = sequence {
        val knightLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WN else BITBOARD_BN]!!
        squareSequence(knightMoves[square] and knightLocations).forEach { yield(EngineMove((it shl 16) or square)) }

        val kingLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK]!!
        squareSequence(kingMoves[square] and kingLocations).forEach { yield(EngineMove((it shl 16) or square)) }

        val pawnLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WP else BITBOARD_BP]!!
        val pawnCaptureMoves = if (mover == Colour.WHITE) blackPawnMovesCapture[square] else whitePawnMovesCapture[square]
        squareSequence(pawnCaptureMoves and pawnLocations).forEach {
            if (square >= 56 || square <= 7) {
                yield(EngineMove(((it shl 16) or square) or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value))
            } else {
                yield(EngineMove((it shl 16) or square))
            }
        }

        val bishopLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WB else BITBOARD_BB]!! or
                bitboardMap[if (mover == Colour.WHITE) BITBOARD_WQ else BITBOARD_BQ]!!
        val rookLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WR else BITBOARD_BR]!! or
                bitboardMap[if (mover == Colour.WHITE) BITBOARD_WQ else BITBOARD_BQ]!!
        val allLocations =
                bitboardMap[BITBOARD_WP]!! or
                bitboardMap[BITBOARD_BP]!! or
                bitboardMap[BITBOARD_WN]!! or
                bitboardMap[BITBOARD_BN]!! or
                bitboardMap[BITBOARD_WB]!! or
                bitboardMap[BITBOARD_BB]!! or
                bitboardMap[BITBOARD_WR]!! or
                bitboardMap[BITBOARD_BR]!! or
                bitboardMap[BITBOARD_WQ]!! or
                bitboardMap[BITBOARD_BQ]!! or
                bitboardMap[BITBOARD_WK]!! or
                bitboardMap[BITBOARD_BK]!!

        listOf(Pair(1,0), Pair(0,1), Pair(0,-1), Pair(-1,0)).forEach {
            var sq = Square.fromBitRef(square)
            var done = false
            do {
                val newX = sq.xFile + it.first
                val newY = sq.yRank + it.second
                done = newX < 0 || newX > 7 || newY < 0 || newY > 7
                if (!done) {
                    sq = Square.fromCoords(newX, newY)
                    if ((rookLocations and (1L shl sq.bitRef)) != 0L) {
                        done = true
                        yield(EngineMove(Move(sq, Square.fromBitRef(square))))
                    } else if (allLocations and (1L shl sq.bitRef) != 0L) {
                        done = true
                    }
                }

            } while (!done)
        }

        listOf(Pair(1,1), Pair(-1,1), Pair(1,-1), Pair(-1,-1)).forEach {
            var sq = Square.fromBitRef(square)
            var done = false
            do {
                val newX = sq.xFile + it.first
                val newY = sq.yRank + it.second
                done = newX < 0 || newX > 7 || newY < 0 || newY > 7
                if (!done) {
                    sq = Square.fromCoords(newX, newY)
                    if ((bishopLocations and (1L shl sq.bitRef)) != 0L) {
                        done = true
                        yield(EngineMove(Move(sq, Square.fromBitRef(square))))
                    } else if (allLocations and (1L shl sq.bitRef) != 0L) {
                        done = true
                    }
                }
            } while (!done)
        }

    }
}