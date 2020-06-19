package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.engine.type.MoveDetail
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square

fun EngineBoard.makeNullMove() {
    boardHashObject.makeNullMove()
    mover = mover.opponent()
    val t = this.engineBitboards.pieceBitboards[BITBOARD_FRIENDLY]
    this.engineBitboards.setPieceBitboard(BITBOARD_FRIENDLY, this.engineBitboards.pieceBitboards[BITBOARD_ENEMY])
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
    val capturePiece = getBitboardTypeOfPieceOnSquare(moveTo, mover.opponent())
    val movePiece = getBitboardTypeOfPieceOnSquare(moveFrom, mover)

    val moveDetail = MoveDetail()
    moveDetail.capturePiece = BITBOARD_NONE
    moveDetail.move = compactMove
    moveDetail.hashValue = boardHashObject.trackedHashValue
    moveDetail.isOnNullMove = isOnNullMove
    moveDetail.halfMoveCount = halfMoveCount.toByte()
    moveDetail.enPassantBitboard = engineBitboards.pieceBitboards[BITBOARD_ENPASSANTSQUARE]
    moveDetail.castlePrivileges = castlePrivileges.toByte()
    moveDetail.movePiece = movePiece

    moveHistory[numMovesMade] = moveDetail

    isOnNullMove = false
    halfMoveCount++
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)
    engineBitboards.movePiece(movePiece, compactMove)

    makeNonTrivialMoveTypeAdjustments(moveFrom, moveTo, compactMove, capturePiece, movePiece)

    numMovesMade++
    mover = mover.opponent()

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
    halfMoveCount = moveHistory[numMovesMade]!!.halfMoveCount.toInt()
    mover = mover.opponent()
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, moveHistory[numMovesMade]!!.enPassantBitboard)
    castlePrivileges = moveHistory[numMovesMade]!!.castlePrivileges.toInt()
    isOnNullMove = moveHistory[numMovesMade]!!.isOnNullMove
    val fromSquare = moveHistory[numMovesMade]!!.move ushr 16 and 63
    val toSquare = moveHistory[numMovesMade]!!.move and 63
    val fromMask = 1L shl fromSquare
    val toMask = 1L shl toSquare
    if (updateHash) boardHashObject.unMove(this)

    // deal with en passants first, they are special moves and capture moves, so just get them out of the way
    if (!unMakeEnPassants(fromMask, toMask)) {

        // put capture piece back on toSquare, we don't get here if an en passant has just been unmade
        replaceCapturedPiece(toMask)

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

private fun EngineBoard.unMakeEnPassants(fromMask: Long, toMask: Long): Boolean {
    if (toMask == moveHistory[numMovesMade]!!.enPassantBitboard) {
        if (moveHistory[numMovesMade]!!.movePiece == BITBOARD_WP) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask or fromMask)
            this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
            return true
        } else if (moveHistory[numMovesMade]!!.movePiece == BITBOARD_BP) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask or fromMask)
            this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
            return true
        }
    }
    return false
}

private fun EngineBoard.replaceCastledRook(fromMask: Long, toMask: Long, movePiece: Int) {
    if (movePiece == BITBOARD_WK) {
        if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
        } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
        }
    } else if (movePiece == BITBOARD_BK) {
        if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
        } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
        }
    }
}

private fun EngineBoard.replaceCapturedPiece(toMask: Long) {
    val capturePiece = moveHistory[numMovesMade]!!.capturePiece
    if (capturePiece != BITBOARD_NONE) {
        this.engineBitboards.xorPieceBitboard(capturePiece, toMask)
    }
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.removePromotionPiece(fromMask: Long, toMask: Long): Boolean {
    val promotionPiece = moveHistory[numMovesMade]!!.move and PROMOTION_PIECE_TOSQUARE_MASK_FULL
    if (promotionPiece != 0) {
        if (mover == Colour.WHITE) {
            this.engineBitboards.xorPieceBitboard(BITBOARD_WP, fromMask)
            when (promotionPiece) {
                PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> this.engineBitboards.xorPieceBitboard(BITBOARD_WQ, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> this.engineBitboards.xorPieceBitboard(BITBOARD_WB, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> this.engineBitboards.xorPieceBitboard(BITBOARD_WN, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> this.engineBitboards.xorPieceBitboard(BITBOARD_WR, toMask)
                else -> throw InvalidMoveException("Illegal promotion piece $promotionPiece")
            }
        } else {
            this.engineBitboards.xorPieceBitboard(BITBOARD_BP, fromMask)
            when (promotionPiece) {
                PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> this.engineBitboards.xorPieceBitboard(BITBOARD_BQ, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> this.engineBitboards.xorPieceBitboard(BITBOARD_BB, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> this.engineBitboards.xorPieceBitboard(BITBOARD_BN, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> this.engineBitboards.xorPieceBitboard(BITBOARD_BR, toMask)
                else -> throw InvalidMoveException("Invalid promotionPiece $promotionPiece")
            }
        }
        return true
    }
    return false
}

private fun EngineBoard.replaceMovedPiece(fromSquare: Int, fromMask: Long, toMask: Long): Int {
    val movePiece = moveHistory[numMovesMade]!!.movePiece
    engineBitboards.xorPieceBitboard(movePiece, toMask or fromMask)
    if (movePiece == BITBOARD_WK) whiteKingSquare = fromSquare else
        if (movePiece == BITBOARD_BK) blackKingSquare = fromSquare
    return movePiece
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeNonTrivialMoveTypeAdjustments(moveFrom: Int, moveTo: Int, compactMove: Int, capturePiece: Int, movePiece: Int) {
    if (mover == Colour.WHITE) {
        if (movePiece == BITBOARD_WP) makeSpecialWhitePawnMoveAdjustments(compactMove)
        else if (movePiece == BITBOARD_WR) adjustCastlePrivilegesForWhiteRookMove(moveFrom)
        else if (movePiece == BITBOARD_WK) adjustKingVariablesForWhiteKingMove(compactMove)
        if (capturePiece != BITBOARD_NONE) makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece, moveTo)
    } else {
        if (movePiece == BITBOARD_BP) makeSpecialBlackPawnMoveAdjustments(compactMove)
        else if (movePiece == BITBOARD_BR) adjustCastlePrivilegesForBlackRookMove(moveFrom)
        else if (movePiece == BITBOARD_BK) adjustKingVariablesForBlackKingMove(compactMove)
        if (capturePiece != BITBOARD_NONE) makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece, moveTo)
    }
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece: Int, moveTo: Int) {
    val toMask = 1L shl moveTo

    moveHistory[numMovesMade]!!.capturePiece = capturePiece
    halfMoveCount = 0
    this.engineBitboards.xorPieceBitboard(capturePiece, toMask)
    if (capturePiece == BITBOARD_WR) {
        if (toMask == WHITEKINGSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CASTLEPRIV_WK.inv()
        } else if (toMask == WHITEQUEENSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CASTLEPRIV_WQ.inv()
        }
    }
}

private fun EngineBoard.adjustKingVariablesForBlackKingMove(compactMove: Int) {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val fromMask = 1L shl moveFrom
    val toMask = 1L shl moveTo
    castlePrivileges = castlePrivileges and CASTLEPRIV_BNONE
    blackKingSquare = moveTo
    if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
    } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
    }
}

private fun EngineBoard.adjustCastlePrivilegesForBlackRookMove(moveFrom: Int) {
    if (moveFrom == Square.A8.bitRef) castlePrivileges = castlePrivileges and CASTLEPRIV_BQ.inv() else
        if (moveFrom == Square.H8.bitRef) castlePrivileges = castlePrivileges and CASTLEPRIV_BK.inv()
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
    } else if (toMask == moveHistory[numMovesMade]!!.enPassantBitboard) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
        moveHistory[numMovesMade]!!.capturePiece = BITBOARD_WP
    } else if (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL != 0) {
        val promotionPieceMask = compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL
        when (promotionPieceMask) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BQ, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BR, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BN, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_BB, toMask)
            }
            else -> throw InvalidMoveException("compactMove $compactMove produced invalid promotion piece")
        }
        this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask)
    }
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece: Int, moveTo: Int) {
    val toMask = 1L shl moveTo
    moveHistory[numMovesMade]!!.capturePiece = capturePiece
    halfMoveCount = 0
    this.engineBitboards.xorPieceBitboard(capturePiece, toMask)
    if (capturePiece == BITBOARD_BR) {
        if (toMask == BLACKKINGSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CASTLEPRIV_BK.inv()
        } else if (toMask == BLACKQUEENSIDEROOKMASK) {
            castlePrivileges = castlePrivileges and CASTLEPRIV_BQ.inv()
        }
    }
}

private fun EngineBoard.adjustKingVariablesForWhiteKingMove(compactMove: Int) {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val fromMask = 1L shl moveFrom
    val toMask = 1L shl moveTo
    whiteKingSquare = moveTo
    castlePrivileges = castlePrivileges and CASTLEPRIV_WNONE
    if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
    } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
    }
}

private fun EngineBoard.adjustCastlePrivilegesForWhiteRookMove(moveFrom: Int) {
    if (moveFrom == Square.A1.bitRef) {
        castlePrivileges = castlePrivileges and CASTLEPRIV_WQ.inv()
    } else if (moveFrom == Square.H1.bitRef) {
        castlePrivileges = castlePrivileges and CASTLEPRIV_WK.inv()
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
    } else if (toMask == moveHistory[numMovesMade]!!.enPassantBitboard) {
        this.engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
        moveHistory[numMovesMade]!!.capturePiece = BITBOARD_BP
    } else if (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL != 0) {
        when (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WQ, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WR, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WN, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                this.engineBitboards.orPieceBitboard(BITBOARD_WB, toMask)
            }
            else -> throw InvalidMoveException("compactMove $compactMove produced invalid promotion piece")
        }
        this.engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask)
    }
}
