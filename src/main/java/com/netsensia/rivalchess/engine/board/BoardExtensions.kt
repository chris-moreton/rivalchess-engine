package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.util.popCount
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.eval.exactlyOneBitSet
import com.netsensia.rivalchess.engine.eval.pieceValue
import com.netsensia.rivalchess.engine.eval.see.StaticExchangeEvaluator
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.onlyKingsRemain() =
    exactlyOneBitSet(this.engineBitboards.pieceBitboards[BITBOARD_ENEMY]) &&
            exactlyOneBitSet(this.engineBitboards.pieceBitboards[BITBOARD_FRIENDLY])

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.isSquareEmpty(bitRef: Int) = engineBitboards.pieceBitboards[BITBOARD_ALL] and (1L shl bitRef) == 0L

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.isCapture(move: Int): Boolean {
    val toSquare = move and 63
    return !isSquareEmpty(toSquare) ||
            (1L shl toSquare and this.engineBitboards.pieceBitboards[BITBOARD_ENPASSANTSQUARE] != 0L &&
            getBitboardTypeOfPieceOnSquare(move ushr 16 and 63, mover) in intArrayOf(BITBOARD_WP, BITBOARD_BP))
}

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.getPiece(bitRef: Int) = when (getBitboardTypeOfPieceOnSquare(bitRef)) {
        BITBOARD_WP, BITBOARD_BP -> Piece.PAWN
        BITBOARD_WB, BITBOARD_BB -> Piece.BISHOP
        BITBOARD_WN, BITBOARD_BN -> Piece.KNIGHT
        BITBOARD_WR, BITBOARD_BR -> Piece.ROOK
        BITBOARD_WQ, BITBOARD_BQ -> Piece.QUEEN
        BITBOARD_WK, BITBOARD_BK -> Piece.KING
        else -> Piece.NONE
    }

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.isCheck(colour: Colour) =
    if (colour == Colour.WHITE)
        this.engineBitboards.isSquareAttackedBy(whiteKingSquareCalculated, Colour.BLACK) else
        this.engineBitboards.isSquareAttackedBy(blackKingSquareCalculated, Colour.WHITE)

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.getScore(move: Int, isCapture: Boolean, staticExchangeEvaluator: StaticExchangeEvaluator): Int {
    var score = 0
    val promotionMask = move and PROMOTION_PIECE_TOSQUARE_MASK_FULL
    if (isCapture) {
        val see = staticExchangeEvaluator.staticExchangeEvaluation(this, move)
        if (see > 0) score = 100 + (see.toDouble() / pieceValue(BITBOARD_WQ) * 10).toInt()
        if (promotionMask == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) score += 9
    } else if (promotionMask == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
        score = 116
    }
    return score
}

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.pawnValues(pawnBitboardType: Int) = popCount(getBitboard(pawnBitboardType)) * pieceValue(BITBOARD_WP)

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.kingSquare(colour: Colour) = getBitboard(if (colour == Colour.WHITE) BITBOARD_WK else BITBOARD_BK).countTrailingZeroBits()

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.moveDoesNotLeaveMoverInCheck(moveToVerify: Int) =
    makeMove(moveToVerify and 0x00FFFFFF, false, updateHash = false).also {
        if (it) unMakeMove(false)
    }

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.getCharBoard(): CharArray {
        val board = CharArray(64){'0'}
        val pieces = charArrayOf('P', 'N', 'B', 'Q', 'K', 'R', 'p', 'n', 'b', 'q', 'k', 'r')
        for (i in BITBOARD_WP..BITBOARD_BR) {
            val bitsSet = squareList(this.engineBitboards.pieceBitboards[i])
            for (bitSet in bitsSet) board[bitSet] = pieces[i]
        }
        return board
    }

@kotlin.ExperimentalUnsignedTypes
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

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.getFen(): String {
        val fen = getFenBoard()
        fen.append(' ')
        fen.append(if (mover == Colour.WHITE) 'w' else 'b')
        fen.append(' ')
        var noPrivs = true
        if (castlePrivileges and CASTLEPRIV_WK != 0) {
            fen.append('K')
            noPrivs = false
        }
        if (castlePrivileges and CASTLEPRIV_WQ != 0) {
            fen.append('Q')
            noPrivs = false
        }
        if (castlePrivileges and CASTLEPRIV_BK != 0) {
            fen.append('k')
            noPrivs = false
        }
        if (castlePrivileges and CASTLEPRIV_BQ != 0) {
            fen.append('q')
            noPrivs = false
        }
        if (noPrivs) fen.append('-')
        fen.append(' ')
        val bitboard = this.engineBitboards.pieceBitboards[BITBOARD_ENPASSANTSQUARE]
        if (bitboard > 0L) {
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

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.anyLegalMoves(moves: IntArray, moveIndex: Int): Boolean =
    when {
        moves[moveIndex] == 0 -> false
        moveDoesNotLeaveMoverInCheck(moves[moveIndex]) -> true
        else -> anyLegalMoves(moves, moveIndex+1)
    }

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.isGameOver() = !anyLegalMoves(moveGenerator().generateLegalMoves().moves, 0)
