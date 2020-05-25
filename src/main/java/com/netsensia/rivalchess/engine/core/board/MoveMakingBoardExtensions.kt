package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.makeNullMove() {
    boardHashObject.makeNullMove()
    isWhiteToMove = !isWhiteToMove
    val t = engineBitboards.getPieceBitboard(BitboardType.FRIENDLY)
    engineBitboards.setPieceBitboard(BitboardType.FRIENDLY, engineBitboards.getPieceBitboard(BitboardType.ENEMY))
    engineBitboards.setPieceBitboard(BitboardType.ENEMY, t)
    isOnNullMove = true
}

fun EngineBoard.unMakeNullMove() {
    makeNullMove()
    isOnNullMove = false
}

@Throws(InvalidMoveException::class)
fun EngineBoard.unMakeMove() {
    numMovesMade--
    halfMoveCount = moveHistory[numMovesMade].halfMoveCount.toInt()
    isWhiteToMove = !isWhiteToMove
    engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, moveHistory[numMovesMade].enPassantBitboard)
    castlePrivileges = moveHistory[numMovesMade].castlePrivileges.toInt()
    isOnNullMove = moveHistory[numMovesMade].isOnNullMove
    val fromSquare = moveHistory[numMovesMade].move ushr 16 and 63
    val toSquare = moveHistory[numMovesMade].move and 63
    val fromMask = 1L shl fromSquare
    val toMask = 1L shl toSquare
    boardHashObject.unMove(this)
    squareContents[fromSquare] = moveHistory[numMovesMade].movePiece
    squareContents[toSquare] = SquareOccupant.NONE

    // deal with en passants first, they are special moves and capture moves, so just get them out of the way
    if (!unMakeEnPassants(toSquare, fromMask, toMask)) {

        // put capture piece back on toSquare, we don't get here if an en passant has just been unmade
        replaceCapturedPiece(toSquare, toMask)

        // for promotions, remove promotion piece from toSquare
        if (!removePromotionPiece(fromMask, toMask)) {

            // now that promotions are out of the way, we can remove the moving piece from toSquare and put it back on fromSquare
            val movePiece = replaceMovedPiece(fromSquare, fromMask, toMask)

            // for castles, replace the rook
            replaceCastledRook(fromMask, toMask, movePiece)
        }
    }
    calculateSupplementaryBitboards()
}

private fun EngineBoard.unMakeEnPassants(toSquare: Int, fromMask: Long, toMask: Long): Boolean {
    if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
        if (moveHistory[numMovesMade].movePiece == SquareOccupant.WP) {
            engineBitboards.xorPieceBitboard(BitboardType.WP, toMask or fromMask)
            engineBitboards.xorPieceBitboard(BitboardType.BP, toMask ushr 8)
            squareContents[toSquare - 8] = SquareOccupant.BP
            return true
        } else if (moveHistory[numMovesMade].movePiece == SquareOccupant.BP) {
            engineBitboards.xorPieceBitboard(BitboardType.BP, toMask or fromMask)
            engineBitboards.xorPieceBitboard(BitboardType.WP, toMask shl 8)
            squareContents[toSquare + 8] = SquareOccupant.WP
            return true
        }
    }
    return false
}

private fun EngineBoard.replaceCastledRook(fromMask: Long, toMask: Long, movePiece: SquareOccupant) {
    if (movePiece == SquareOccupant.WK) {
        if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H1.bitRef] = SquareOccupant.WR
            squareContents[Square.F1.bitRef] = SquareOccupant.NONE
        } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A1.bitRef] = SquareOccupant.WR
            squareContents[Square.D1.bitRef] = SquareOccupant.NONE
        }
    } else if (movePiece == SquareOccupant.BK) {
        if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H8.bitRef] = SquareOccupant.BR
            squareContents[Square.F8.bitRef] = SquareOccupant.NONE
        } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A8.bitRef] = SquareOccupant.BR
            squareContents[Square.D8.bitRef] = SquareOccupant.NONE
        }
    }
}

private fun EngineBoard.replaceCapturedPiece(toSquare: Int, toMask: Long) {
    val capturePiece = moveHistory[numMovesMade].capturePiece
    if (capturePiece != SquareOccupant.NONE) {
        squareContents[toSquare] = capturePiece
        engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
    }
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.removePromotionPiece(fromMask: Long, toMask: Long): Boolean {
    val promotionPiece = moveHistory[numMovesMade].move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    if (promotionPiece != 0) {
        if (isWhiteToMove) {
            engineBitboards.xorPieceBitboard(BitboardType.WP, fromMask)
            when (PromotionPieceMask.fromValue(promotionPiece)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> engineBitboards.xorPieceBitboard(BitboardType.WQ, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> engineBitboards.xorPieceBitboard(BitboardType.WB, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> engineBitboards.xorPieceBitboard(BitboardType.WN, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> engineBitboards.xorPieceBitboard(BitboardType.WR, toMask)
                else -> throw InvalidMoveException("Illegal promotion piece $promotionPiece")
            }
        } else {
            engineBitboards.xorPieceBitboard(BitboardType.BP, fromMask)
            when (PromotionPieceMask.fromValue(promotionPiece)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> engineBitboards.xorPieceBitboard(BitboardType.BQ, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> engineBitboards.xorPieceBitboard(BitboardType.BB, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> engineBitboards.xorPieceBitboard(BitboardType.BN, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> engineBitboards.xorPieceBitboard(BitboardType.BR, toMask)
                else -> throw InvalidMoveException("Invalid promotionPiece $promotionPiece")
            }
        }
        return true
    }
    return false
}

private fun EngineBoard.replaceMovedPiece(fromSquare: Int, fromMask: Long, toMask: Long): SquareOccupant {
    val movePiece = moveHistory[numMovesMade].movePiece
    engineBitboards.xorPieceBitboard(movePiece.index, toMask or fromMask)
    if (movePiece == SquareOccupant.WK) {
        whiteKingSquare = fromSquare.toByte()
    } else if (movePiece == SquareOccupant.BK) {
        blackKingSquare = fromSquare.toByte()
    }
    return movePiece
}