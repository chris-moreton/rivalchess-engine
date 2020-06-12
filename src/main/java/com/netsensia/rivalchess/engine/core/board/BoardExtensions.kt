package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.engine.core.BITBOARD_ENEMY
import com.netsensia.rivalchess.engine.core.BITBOARD_ENPASSANTSQUARE
import com.netsensia.rivalchess.engine.core.BITBOARD_FRIENDLY
import com.netsensia.rivalchess.engine.core.eval.see.StaticExchangeEvaluator
import com.netsensia.rivalchess.engine.core.eval.onlyOneBitSet
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.onlyKingsRemain() =
    onlyOneBitSet(this.engineBitboards.getPieceBitboard(BITBOARD_ENEMY)) &&
            onlyOneBitSet(this.engineBitboards.getPieceBitboard(BITBOARD_FRIENDLY))

fun EngineBoard.isSquareEmpty(bitRef: Int) = squareContents.get(bitRef) == SquareOccupant.NONE

fun EngineBoard.isCapture(move: Int): Boolean {
    val toSquare = move and 63
    var isCapture: Boolean = !isSquareEmpty(toSquare)
    if (!isCapture && 1L shl toSquare and this.engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE) != 0L &&
            squareContents.get(move ushr 16 and 63).piece == Piece.PAWN) {
        isCapture = true
    }
    return isCapture
}

fun EngineBoard.getPiece(bitRef: Int) = when (squareContents.get(bitRef)) {
        SquareOccupant.WP, SquareOccupant.BP -> Piece.PAWN
        SquareOccupant.WB, SquareOccupant.BB -> Piece.BISHOP
        SquareOccupant.WN, SquareOccupant.BN -> Piece.KNIGHT
        SquareOccupant.WR, SquareOccupant.BR -> Piece.ROOK
        SquareOccupant.WQ, SquareOccupant.BQ -> Piece.QUEEN
        SquareOccupant.WK, SquareOccupant.BK -> Piece.KING
        else -> Piece.NONE
    }

fun EngineBoard.isCheck(colour: Colour) =
    if (colour == Colour.WHITE)
        this.engineBitboards.isSquareAttackedBy(getWhiteKingSquare(), Colour.BLACK)
    else
        this.engineBitboards.isSquareAttackedBy(getBlackKingSquare(), Colour.WHITE)

@Throws(InvalidMoveException::class)
fun EngineBoard.getScore(move: Int, includeChecks: Boolean, isCapture: Boolean, staticExchangeEvaluator: StaticExchangeEvaluator): Int {
    var score = 0
    val promotionMask = move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    if (isCapture) {
        val see = staticExchangeEvaluator.staticExchangeEvaluation(this, EngineMove(move))
        if (see > 0) score = 100 + (see.toDouble() / pieceValue(Piece.QUEEN) * 10).toInt()
        if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) score += 9
    } else if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
        score = 116
    } else if (includeChecks) {
        score = 100
    }
    return score
}

fun EngineBoard.moveDoesNotLeaveMoverInCheck(moveToVerify: Int): Boolean {

    try {
        val engineMove = EngineMove(moveToVerify and 0x00FFFFFF)
        if (makeMove(engineMove)) {
            unMakeMove()
            return true
        }
    } catch (e: InvalidMoveException) {
        return false
    }
    return false
}

fun EngineBoard.getCharBoard(): CharArray {
        val board = CharArray(64){'0'}
        val pieces = charArrayOf('P', 'N', 'B', 'Q', 'K', 'R', 'p', 'n', 'b', 'q', 'k', 'r')
        for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
            val bitsSet = squareList(this.engineBitboards.getPieceBitboard(i))
            for (bitSet in bitsSet) {
                board[bitSet] = pieces[i]
            }
        }
        return board
    }

fun EngineBoard.getFenBoard(): StringBuilder {
        val board = getCharBoard()
        val fen = StringBuilder()
        var spaces = '0'
        for (i in 63 downTo 0) {
            if (board[i] == '0') {
                spaces++
            } else {
                if (spaces > '0') {
                    fen.append(spaces)
                    spaces = '0'
                }
                fen.append(board[i])
            }
            if (i % 8 == 0) {
                if (spaces > '0') {
                    fen.append(spaces)
                    spaces = '0'
                }
                if (i > 0) {
                    fen.append('/')
                }
            }
        }
        return fen
    }

fun EngineBoard.getFen(): String {
        val fen = getFenBoard()
        fen.append(' ')
        fen.append(if (mover == Colour.WHITE) 'w' else 'b')
        fen.append(' ')
        var noPrivs = true
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value != 0) {
            fen.append('K')
            noPrivs = false
        }
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value != 0) {
            fen.append('Q')
            noPrivs = false
        }
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value != 0) {
            fen.append('k')
            noPrivs = false
        }
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value != 0) {
            fen.append('q')
            noPrivs = false
        }
        if (noPrivs) fen.append('-')
        fen.append(' ')
        val bitboard = this.engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE)
        if (java.lang.Long.bitCount(bitboard) > 0) {
            val epSquare = java.lang.Long.numberOfTrailingZeros(bitboard)
            val file = (7 - epSquare % 8).toChar()
            val rank = (if (epSquare <= 23) 2 else 5).toChar()
            fen.append((file + 'a'.toInt()))
            fen.append((rank + '1'.toInt()))
        } else {
            fen.append('-')
        }
        fen.append(' ')
        fen.append(halfMoveCount)
        fen.append(' ')
        fen.append(numMovesMade / 2 + 1)
        return fen.toString()
    }

private fun EngineBoard.anyLegalMoves(moves: IntArray, moveIndex: Int): Boolean =
    if (moves[moveIndex] == 0) false
    else if (moveDoesNotLeaveMoverInCheck(moves[moveIndex])) true
    else anyLegalMoves(moves, moveIndex+1)

fun EngineBoard.isGameOver() = !anyLegalMoves(moveGenerator().generateLegalMoves().moves, 0)
