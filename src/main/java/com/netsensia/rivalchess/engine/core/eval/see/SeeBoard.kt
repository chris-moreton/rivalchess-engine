package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.bitboards.blackPawnMovesCapture
import com.netsensia.rivalchess.bitboards.kingMoves
import com.netsensia.rivalchess.bitboards.knightMoves
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.bitboards.whitePawnMovesCapture
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.board.getFen
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.*
import com.netsensia.rivalchess.model.exception.EnumConversionException
import java.lang.Long.bitCount

class SeeBoard(board: EngineBoard) {
    val bitboardMap: MutableMap<Int, Long> = mutableMapOf()
    private val bitboardMapHistory: MutableMap<Int, MutableMap<Int, Long>> = mutableMapOf()
    private val whiteList = listOf(BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR)
    private val blackList = listOf(BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)

    var mover: Colour
    val board: EngineBoard = board
    var movePointer = 0

    init {
        whiteList.forEach { bitboardMap[it] = board.engineBitboards.getPieceBitboard(it) }
        blackList.forEach { bitboardMap[it] = board.engineBitboards.getPieceBitboard(it) }
        bitboardMap[BITBOARD_ENPASSANTSQUARE] = board.engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE)
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

        bitboardMap[BITBOARD_ENPASSANTSQUARE] = 0

        if (movedPieceBitboardType == BITBOARD_WP) {
            if (move.to() - move.from() == 16) bitboardMap[BITBOARD_ENPASSANTSQUARE] = toBit shr 8
            if (move.to() >= 56) {
                togglePiece(1L shl move.to(), BITBOARD_WP)
                togglePiece(1L shl move.to(), BITBOARD_WQ)
            }
        } else if (movedPieceBitboardType == BITBOARD_BP) {
            if (move.from() - move.to() == 16) bitboardMap[BITBOARD_ENPASSANTSQUARE] = toBit shl 8
            if (move.to() >= 56) {
                togglePiece(1L shl move.to(), BITBOARD_BP)
                togglePiece(1L shl move.to(), BITBOARD_BQ)
            }
        }

        if (capturedPieceBitboardType == BITBOARD_NONE) {
            if (movedPieceBitboardType == BITBOARD_WP) {
                if ((move.to() - move.from()) % 2 != 0) togglePiece(1L shl (move.to() - 8), BITBOARD_BP)
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
        get() = if (bitboardMap[BITBOARD_WK]!! != 0L) pieceValue(Piece.KING) else 0 +
                bitCount(bitboardMap[BITBOARD_WN]!!) * pieceValue(Piece.KNIGHT) +
                bitCount(bitboardMap[BITBOARD_WR]!!) * pieceValue(Piece.ROOK) +
                bitCount(bitboardMap[BITBOARD_WB]!!) * pieceValue(Piece.BISHOP) +
                bitCount(bitboardMap[BITBOARD_WQ]!!) * pieceValue(Piece.QUEEN) +
                bitCount(bitboardMap[BITBOARD_WP]!!) * pieceValue(Piece.PAWN)

    val blackPieceValues: Int
        get() = if (bitboardMap[BITBOARD_BK]!! != 0L) pieceValue(Piece.KING) else 0 +
                bitCount(bitboardMap[BITBOARD_BN]!!) * pieceValue(Piece.KNIGHT) +
                bitCount(bitboardMap[BITBOARD_BR]!!) * pieceValue(Piece.ROOK) +
                bitCount(bitboardMap[BITBOARD_BB]!!) * pieceValue(Piece.BISHOP) +
                bitCount(bitboardMap[BITBOARD_BQ]!!) * pieceValue(Piece.QUEEN) +
                bitCount(bitboardMap[BITBOARD_BP]!!) * pieceValue(Piece.PAWN)

    fun generateCaptureMovesOnSquare(square: Int): List<EngineMove> {

        val moves = mutableListOf<EngineMove>()
        
        knightCaptures(square, moves)
        kingCaptures(square, moves)
        pawnCaptures(square, moves)

        val allLocations = allLocationsBitboard()

        rookCaptures(square, allLocations, moves)
        bishopCaptures(square, allLocations, moves)

        return moves
    }

    private fun allLocationsBitboard() =
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

    private fun pawnCaptures(square: Int, moves: MutableList<EngineMove>) {
        val pawnLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WP else BITBOARD_BP]!!
        val pawnCaptureMoves = if (mover == Colour.WHITE) blackPawnMovesCapture[square] else whitePawnMovesCapture[square]
        squareList(pawnCaptureMoves and pawnLocations).forEach {
            if (square >= 56 || square <= 7)
                moves.add(EngineMove(((it shl 16) or square) or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value))
            else
                moves.add(EngineMove((it shl 16) or square))
        }
    }

    private fun kingCaptures(square: Int, moves: MutableList<EngineMove>) {
        val kingLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WK else BITBOARD_BK]!!
        squareList(kingMoves[square] and kingLocations).forEach { moves.add(EngineMove((it shl 16) or square)) }
    }

    private fun knightCaptures(square: Int, moves: MutableList<EngineMove>) {
        val knightLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WN else BITBOARD_BN]!!
        squareList(knightMoves[square] and knightLocations).forEach { moves.add(EngineMove((it shl 16) or square)) }
    }

    private fun bishopCaptures(square: Int, allLocations: Long, moves: MutableList<EngineMove>) {
        val bishopLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WB else BITBOARD_BB]!! or
                bitboardMap[if (mover == Colour.WHITE) BITBOARD_WQ else BITBOARD_BQ]!!

        if (bishopLocations != 0L) {
            var numBishopsLeft = bitCount(bishopLocations)

            listOf(Pair(1, 1), Pair(-1, 1), Pair(1, -1), Pair(-1, -1)).forEach {
                if (numBishopsLeft > 0) {
                    var sq = Square.fromBitRef(square)
                    var done: Boolean
                    do {
                        val newX = sq.xFile + it.first
                        val newY = sq.yRank + it.second
                        done = newX < 0 || newX > 7 || newY < 0 || newY > 7
                        if (!done) {
                            sq = Square.fromCoords(newX, newY)
                            if ((bishopLocations and (1L shl sq.bitRef)) != 0L) {
                                done = true
                                moves.add(EngineMove(Move(sq, Square.fromBitRef(square))))
                                numBishopsLeft--
                            } else if (allLocations and (1L shl sq.bitRef) != 0L) {
                                done = true
                            }
                        }
                    } while (!done)
                }
            }
        }
    }

    private fun rookCaptures(square: Int, allLocations: Long, moves: MutableList<EngineMove>) {
        val rookLocations = bitboardMap[if (mover == Colour.WHITE) BITBOARD_WR else BITBOARD_BR]!! or
                bitboardMap[if (mover == Colour.WHITE) BITBOARD_WQ else BITBOARD_BQ]!!

        if (rookLocations != 0L) {
            var numRooksLeft = bitCount(rookLocations)

            listOf(Pair(1, 0), Pair(0, 1), Pair(0, -1), Pair(-1, 0)).forEach {
                if (numRooksLeft > 0) {
                    var done: Boolean
                    var sq = Square.fromBitRef(square)
                    do {
                        val newX = sq.xFile + it.first
                        val newY = sq.yRank + it.second
                        done = newX < 0 || newX > 7 || newY < 0 || newY > 7
                        if (!done) {
                            sq = Square.fromCoords(newX, newY)
                            if ((rookLocations and (1L shl sq.bitRef)) != 0L) {
                                done = true
                                moves.add(EngineMove(Move(sq, Square.fromBitRef(square))))
                                numRooksLeft--
                            } else if (allLocations and (1L shl sq.bitRef) != 0L) {
                                done = true
                            }
                        }
                    } while (!done)
                }
            }
        }
    }
}