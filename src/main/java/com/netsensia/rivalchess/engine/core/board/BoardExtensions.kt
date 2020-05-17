package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.EngineBitboards
import com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluator
import com.netsensia.rivalchess.engine.core.eval.onlyOneBitSet
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.onlyKingsRemain() =
    onlyOneBitSet(engineBitboards.getPieceBitboard(BitboardType.ENEMY)) &&
            onlyOneBitSet(engineBitboards.getPieceBitboard(BitboardType.FRIENDLY))

fun EngineBoard.isSquareEmpty(bitRef: Int) = squareContents.get(bitRef) == SquareOccupant.NONE

fun EngineBoard.isCapture(move: Int): Boolean {
    val toSquare = move and 63
    var isCapture: Boolean = !isSquareEmpty(toSquare)
    if (!isCapture && 1L shl toSquare and EngineBitboards.getInstance().getPieceBitboard(BitboardType.ENPASSANTSQUARE) != 0L &&
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

fun EngineBoard.isCheck() =
    if (isWhiteToMove)
        engineBitboards.isSquareAttackedBy(whiteKingSquare, Colour.BLACK)
    else
        engineBitboards.isSquareAttackedBy(blackKingSquare, Colour.WHITE)

@Throws(InvalidMoveException::class)
fun EngineBoard.getScore(move: Int, includeChecks: Boolean, isCapture: Boolean, staticExchangeEvaluator: StaticExchangeEvaluator): Int {
    var score = 0
    val promotionMask = move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    if (isCapture) {
        val see: Int = staticExchangeEvaluator.staticExchangeEvaluation(this, EngineMove(move))
        if (see > 0) {
            score = 100 + (see.toDouble() / pieceValue(Piece.QUEEN) * 10).toInt()
        }
        if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
            score += 9
        }
    } else if (promotionMask == PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
        score = 116
    } else if (includeChecks) {
        score = 100
    }
    return score
}
