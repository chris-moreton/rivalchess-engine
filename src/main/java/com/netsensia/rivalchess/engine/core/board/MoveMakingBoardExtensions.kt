package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.engine.core.type.MoveDetail
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.makeNullMove() {
    boardHashObject.makeNullMove()
    mover = mover.opponent()
    val t = this.engineBitboards.getPieceBitboard(BITBOARD_FRIENDLY)
    this.engineBitboards.setPieceBitboard(BITBOARD_FRIENDLY, this.engineBitboards.getPieceBitboard(BITBOARD_ENEMY))
    this.engineBitboards.setPieceBitboard(BITBOARD_ENEMY, t)
    isOnNullMove = true
}

fun EngineBoard.unMakeNullMove() {
    makeNullMove()
    isOnNullMove = false
}

@Throws(InvalidMoveException::class)
fun EngineBoard.makeMove(engineMove: EngineMove, ignoreCheck: Boolean = false, updateHash: Boolean = true): Boolean {
    val compactMove = engineMove.compact
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val capturePiece = squareContents[moveTo]
    val movePiece = squareContents[moveFrom]

    val moveDetail = MoveDetail()
    moveDetail.capturePiece = SquareOccupant.NONE
    moveDetail.move = compactMove
    moveDetail.hashValue = boardHashObject.trackedHashValue
    moveDetail.isOnNullMove = isOnNullMove
    moveDetail.halfMoveCount = halfMoveCount.toByte()
    moveDetail.enPassantBitboard = engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE)
    moveDetail.castlePrivileges = castlePrivileges.toByte()
    moveDetail.movePiece = movePiece

    if (moveHistory.size <= numMovesMade) moveHistory.add(moveDetail) else moveHistory[numMovesMade] = moveDetail

    isOnNullMove = false
    halfMoveCount++
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)
    engineBitboards.movePiece(movePiece, compactMove)
    squareContents[moveFrom] = SquareOccupant.NONE
    squareContents[moveTo] = movePiece

    makeNonTrivialMoveTypeAdjustments(moveFrom, moveTo, compactMove, capturePiece, movePiece)

    mover = mover.opponent()

    numMovesMade++

    calculateSupplementaryBitboards()

    if (!ignoreCheck && isCheck(mover.opponent())) {
        unMakeMove(false)
        return false
    }
    if (updateHash) boardHashObject.move(engineMove, movePiece, capturePiece)

    return true
}

@Throws(InvalidMoveException::class)
fun EngineBoard.unMakeMove(updateHash: Boolean = true) {
    numMovesMade--
    halfMoveCount = moveHistory[numMovesMade].halfMoveCount.toInt()
    mover = mover.opponent()
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, moveHistory[numMovesMade].enPassantBitboard)
    castlePrivileges = moveHistory[numMovesMade].castlePrivileges.toInt()
    isOnNullMove = moveHistory[numMovesMade].isOnNullMove
    val fromSquare = moveHistory[numMovesMade].move ushr 16 and 63
    val toSquare = moveHistory[numMovesMade].move and 63
    val fromMask = 1L shl fromSquare
    val toMask = 1L shl toSquare
    if (updateHash) boardHashObject.unMove(this)
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
            this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask or fromMask)
            this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
            squareContents[toSquare - 8] = SquareOccupant.BP
            return true
        } else if (moveHistory[numMovesMade].movePiece == SquareOccupant.BP) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask or fromMask)
            this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
            squareContents[toSquare + 8] = SquareOccupant.WP
            return true
        }
    }
    return false
}

private fun EngineBoard.replaceCastledRook(fromMask: Long, toMask: Long, movePiece: SquareOccupant) {
    if (movePiece == SquareOccupant.WK) {
        if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H1.bitRef] = SquareOccupant.WR
            squareContents[Square.F1.bitRef] = SquareOccupant.NONE
        } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A1.bitRef] = SquareOccupant.WR
            squareContents[Square.D1.bitRef] = SquareOccupant.NONE
        }
    } else if (movePiece == SquareOccupant.BK) {
        if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H8.bitRef] = SquareOccupant.BR
            squareContents[Square.F8.bitRef] = SquareOccupant.NONE
        } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A8.bitRef] = SquareOccupant.BR
            squareContents[Square.D8.bitRef] = SquareOccupant.NONE
        }
    }
}

private fun EngineBoard.replaceCapturedPiece(toSquare: Int, toMask: Long) {
    val capturePiece = moveHistory[numMovesMade].capturePiece
    if (capturePiece != SquareOccupant.NONE) {
        squareContents[toSquare] = capturePiece
        this.engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
    }
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.removePromotionPiece(fromMask: Long, toMask: Long): Boolean {
    val promotionPiece = moveHistory[numMovesMade].move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
    if (promotionPiece != 0) {
        if (mover == Colour.WHITE) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WP, fromMask)
            when (PromotionPieceMask.fromValue(promotionPiece)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> this.engineBitboards.xorPieceBitboard(BITBOARD_WQ, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> this.engineBitboards.xorPieceBitboard(BITBOARD_WB, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> this.engineBitboards.xorPieceBitboard(BITBOARD_WN, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> this.engineBitboards.xorPieceBitboard(BITBOARD_WR, toMask)
                else -> throw InvalidMoveException("Illegal promotion piece $promotionPiece")
            }
        } else {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BP, fromMask)
            when (PromotionPieceMask.fromValue(promotionPiece)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> this.engineBitboards.xorPieceBitboard(BITBOARD_BQ, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> this.engineBitboards.xorPieceBitboard(BITBOARD_BB, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> this.engineBitboards.xorPieceBitboard(BITBOARD_BN, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> this.engineBitboards.xorPieceBitboard(BITBOARD_BR, toMask)
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
    if (movePiece == SquareOccupant.WK) whiteKingSquare = fromSquare else
        if (movePiece == SquareOccupant.BK) blackKingSquare = fromSquare
    return movePiece
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeNonTrivialMoveTypeAdjustments(moveFrom: Int, moveTo: Int, compactMove: Int, capturePiece: SquareOccupant, movePiece: SquareOccupant?) {
    if (mover == Colour.WHITE) {
        if (movePiece == SquareOccupant.WP) makeSpecialWhitePawnMoveAdjustments(compactMove)
        else if (movePiece == SquareOccupant.WR) adjustCastlePrivilegesForWhiteRookMove(moveFrom)
        else if (movePiece == SquareOccupant.WK) adjustKingVariablesForWhiteKingMove(compactMove)
        if (capturePiece != SquareOccupant.NONE) makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece, moveTo)
    } else {
        if (movePiece == SquareOccupant.BP) makeSpecialBlackPawnMoveAdjustments(compactMove)
        else if (movePiece == SquareOccupant.BR) adjustCastlePrivilegesForBlackRookMove(moveFrom)
        else if (movePiece == SquareOccupant.BK) adjustKingVariablesForBlackKingMove(compactMove)
        if (capturePiece != SquareOccupant.NONE) makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece, moveTo)
    }
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece: SquareOccupant, moveTo: Int) {
    val toMask = 1L shl moveTo

    moveHistory[numMovesMade].capturePiece = capturePiece
    halfMoveCount = 0
    this.engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
    if (capturePiece == SquareOccupant.WR) {
        if (toMask == WHITEKINGSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value.inv()
        } else if (toMask == WHITEQUEENSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value.inv()
        }
    }
}

private fun EngineBoard.adjustKingVariablesForBlackKingMove(compactMove: Int) {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val fromMask = 1L shl moveFrom
    val toMask = 1L shl moveTo
    castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BNONE.value
    blackKingSquare = moveTo
    if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
        squareContents[Square.H8.bitRef] = SquareOccupant.NONE
        squareContents[Square.F8.bitRef] = SquareOccupant.BR
    } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
        squareContents[Square.A8.bitRef] = SquareOccupant.NONE
        squareContents[Square.D8.bitRef] = SquareOccupant.BR
    }
}

private fun EngineBoard.adjustCastlePrivilegesForBlackRookMove(moveFrom: Int) {
    if (moveFrom == Square.A8.bitRef) castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value.inv() else
        if (moveFrom == Square.H8.bitRef) castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value.inv()
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeSpecialBlackPawnMoveAdjustments(compactMove: Int) {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val fromMask = 1L shl moveFrom
    val toMask = 1L shl moveTo
    halfMoveCount = 0
    if (toMask and RANK_5 != 0L && fromMask and RANK_7 != 0L) {
        this.engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toMask shl 8)
    } else if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
        moveHistory[numMovesMade].capturePiece = SquareOccupant.WP
        squareContents[moveTo + 8] = SquareOccupant.NONE
    } else if (compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value != 0) {
        val promotionPieceMask = compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
        when (PromotionPieceMask.fromValue(promotionPieceMask)) {
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BQ, toMask)
                squareContents[moveTo] = SquareOccupant.BQ
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BR, toMask)
                squareContents[moveTo] = SquareOccupant.BR
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BN, toMask)
                squareContents[moveTo] = SquareOccupant.BN
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BB, toMask)
                squareContents[moveTo] = SquareOccupant.BB
            }
            else -> throw InvalidMoveException("compactMove $compactMove produced invalid promotion piece")
        }
        this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask)
    }
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece: SquareOccupant, moveTo: Int) {
    val toMask = 1L shl moveTo
    moveHistory[numMovesMade].capturePiece = capturePiece
    halfMoveCount = 0
    this.engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
    if (capturePiece == SquareOccupant.BR) {
        if (toMask == BLACKKINGSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value.inv()
        } else if (toMask == BLACKQUEENSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value.inv()
        }
    }
}

private fun EngineBoard.adjustKingVariablesForWhiteKingMove(compactMove: Int) {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val fromMask = 1L shl moveFrom
    val toMask = 1L shl moveTo
    whiteKingSquare = moveTo
    castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WNONE.value
    if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
        squareContents[Square.H1.bitRef] = SquareOccupant.NONE
        squareContents[Square.F1.bitRef] = SquareOccupant.WR
    } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
        squareContents[Square.A1.bitRef] = SquareOccupant.NONE
        squareContents[Square.D1.bitRef] = SquareOccupant.WR
    }
}

private fun EngineBoard.adjustCastlePrivilegesForWhiteRookMove(moveFrom: Int) {
    if (moveFrom == Square.A1.bitRef) {
        castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value.inv()
    } else if (moveFrom == Square.H1.bitRef) {
        castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value.inv()
    }
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeSpecialWhitePawnMoveAdjustments(compactMove: Int) {
    val moveFrom = (compactMove ushr 16).toByte()
    val moveTo = (compactMove and 63).toByte()
    val fromMask = 1L shl moveFrom.toInt()
    val toMask = 1L shl moveTo.toInt()
    halfMoveCount = 0
    if (toMask and RANK_4 != 0L && fromMask and RANK_2 != 0L) {
        this.engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, fromMask shl 8)
    } else if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
        this.engineBitboards.xorPieceBitboard(SquareOccupant.BP.index, toMask ushr 8)
        moveHistory[numMovesMade].capturePiece = SquareOccupant.BP
        squareContents[moveTo - 8] = SquareOccupant.NONE
    } else if (compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value != 0) {
        when (PromotionPieceMask.fromValue(compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value)) {
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WQ, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WQ
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WR, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WR
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WN, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WN
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WB, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WB
            }
            else -> throw InvalidMoveException(
                    "compactMove $compactMove produced invalid promotion piece")
        }
        this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask)
    }
}
