package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.eval.*
import com.netsensia.rivalchess.engine.type.MoveDetail
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square

fun EngineBoard.makeNullMove() {
    boardHashObject.makeNullMove()
    mover = mover.opponent()
    val t = engineBitboards.pieceBitboards[BITBOARD_FRIENDLY]
    engineBitboards.setPieceBitboard(BITBOARD_FRIENDLY, engineBitboards.pieceBitboards[BITBOARD_ENEMY])
    engineBitboards.setPieceBitboard(BITBOARD_ENEMY, t)
    isOnNullMove = true
}

fun EngineBoard.unMakeNullMove() {
    makeNullMove()
    isOnNullMove = false
}

@Throws(InvalidMoveException::class)
fun EngineBoard.makeMove(compactMove: Int, ignoreCheck: Boolean = false, updateHash: Boolean = true): Boolean {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val capturePiece = getBitboardTypeOfPieceOnSquare(moveTo, mover.opponent())
    val movePiece = getBitboardTypeOfPieceOnSquare(moveFrom, mover)

    val moveDetail = MoveDetail()
    moveDetail.capturePiece = BITBOARD_NONE
    moveDetail.move = compactMove
    moveDetail.hashValue = boardHashObject.trackedHashValue
    moveDetail.isOnNullMove = isOnNullMove
    moveDetail.halfMoveCount = halfMoveCount
    moveDetail.enPassantBitboard = engineBitboards.pieceBitboards[BITBOARD_ENPASSANTSQUARE]
    moveDetail.castlePrivileges = castlePrivileges
    moveDetail.movePiece = movePiece

    moveHistory[numMovesMade] = moveDetail

    isOnNullMove = false
    halfMoveCount++
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)
    engineBitboards.movePiece(movePiece, compactMove)

    updateMaterialAfterPieceCapture(capturePiece)

    makeNonTrivialMoveTypeAdjustments(moveFrom, moveTo, compactMove, capturePiece, movePiece)

    numMovesMade++
    mover = mover.opponent()

    calculateSupplementaryBitboards()

    if (!ignoreCheck && isCheck(mover.opponent())) {
        unMakeMove(false)
        return false
    }

    if (updateHash) boardHashObject.move(compactMove, movePiece, capturePiece)

    return true
}

private fun EngineBoard.updateMaterialAfterPieceCapture(capturePiece: Int) {
    when (capturePiece) {
        BITBOARD_WP -> whitePawnValues -= VALUE_PAWN
        BITBOARD_WN -> whitePieceValues -= VALUE_KNIGHT
        BITBOARD_WB -> whitePieceValues -= VALUE_BISHOP
        BITBOARD_WR -> whitePieceValues -= VALUE_ROOK
        BITBOARD_WQ -> whitePieceValues -= VALUE_QUEEN
        BITBOARD_BP -> blackPawnValues -= VALUE_PAWN
        BITBOARD_BN -> blackPieceValues -= VALUE_KNIGHT
        BITBOARD_BB -> blackPieceValues -= VALUE_BISHOP
        BITBOARD_BR -> blackPieceValues -= VALUE_ROOK
        BITBOARD_BQ -> blackPieceValues -= VALUE_QUEEN
    }
}

@Throws(InvalidMoveException::class)
fun EngineBoard.unMakeMove(updateHash: Boolean = true) {
    numMovesMade--
    halfMoveCount = moveHistory[numMovesMade]!!.halfMoveCount
    mover = mover.opponent()
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, moveHistory[numMovesMade]!!.enPassantBitboard)
    castlePrivileges = moveHistory[numMovesMade]!!.castlePrivileges
    isOnNullMove = moveHistory[numMovesMade]!!.isOnNullMove
    val fromSquare = moveHistory[numMovesMade]!!.move ushr 16 and 63
    val toSquare = moveHistory[numMovesMade]!!.move and 63
    val fromMask = 1L shl fromSquare
    val toMask = 1L shl toSquare
    if (updateHash) boardHashObject.unMove(this)

    // deal with en passants first, they are special moves as well as capture moves, so just get them out of the way
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
            engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask or fromMask)
            engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
            blackPawnValues += VALUE_PAWN
            return true
        } else if (moveHistory[numMovesMade]!!.movePiece == BITBOARD_BP) {
            engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask or fromMask)
            engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
            whitePawnValues += VALUE_PAWN
            return true
        }
    }
    return false
}

private fun EngineBoard.replaceCastledRook(fromMask: Long, toMask: Long, movePiece: Int) {
    if (movePiece == BITBOARD_WK) {
        if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK)
            engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
        else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK)
            engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
    } else if (movePiece == BITBOARD_BK) {
        if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK)
            engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
        else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK)
            engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
    }
}

private fun EngineBoard.replaceCapturedPiece(toMask: Long) {
    val capturePiece = moveHistory[numMovesMade]!!.capturePiece
    if (capturePiece != BITBOARD_NONE) {
        engineBitboards.xorPieceBitboard(capturePiece, toMask)
        when (capturePiece) {
            BITBOARD_WP -> whitePawnValues += VALUE_PAWN
            BITBOARD_WN -> whitePieceValues += VALUE_KNIGHT
            BITBOARD_WB -> whitePieceValues += VALUE_BISHOP
            BITBOARD_WR -> whitePieceValues += VALUE_ROOK
            BITBOARD_WQ -> whitePieceValues += VALUE_QUEEN
            BITBOARD_BP -> blackPawnValues += VALUE_PAWN
            BITBOARD_BN -> blackPieceValues += VALUE_KNIGHT
            BITBOARD_BB -> blackPieceValues += VALUE_BISHOP
            BITBOARD_BR -> blackPieceValues += VALUE_ROOK
            BITBOARD_BQ -> blackPieceValues += VALUE_QUEEN
        }
    }
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.removePromotionPiece(fromMask: Long, toMask: Long): Boolean {
    val promotionPiece = moveHistory[numMovesMade]!!.move and PROMOTION_PIECE_TOSQUARE_MASK_FULL
    if (promotionPiece != 0) {
        if (mover == Colour.WHITE) {
            engineBitboards.xorPieceBitboard(BITBOARD_WP, fromMask)
            whitePawnValues += VALUE_PAWN
            when (promotionPiece) {
                PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_WQ, toMask)
                    whitePieceValues -= VALUE_QUEEN
                }
                PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_WB, toMask)
                    whitePieceValues -= VALUE_BISHOP
                }
                PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_WN, toMask)
                    whitePieceValues -= VALUE_KNIGHT
                }
                PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_WR, toMask)
                    whitePieceValues -= VALUE_ROOK
                }
                else -> throw InvalidMoveException("Illegal promotion piece $promotionPiece")
            }
        } else {
            engineBitboards.xorPieceBitboard(BITBOARD_BP, fromMask)
            blackPawnValues += VALUE_PAWN
            when (promotionPiece) {
                PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_BQ, toMask)
                    blackPieceValues -= VALUE_QUEEN
                }
                PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_BB, toMask)
                    blackPieceValues -= VALUE_BISHOP
                }
                PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_BN, toMask)
                    blackPieceValues -= VALUE_KNIGHT
                }
                PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                    engineBitboards.xorPieceBitboard(BITBOARD_BR, toMask)
                    blackPieceValues -= VALUE_ROOK
                }
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
    engineBitboards.xorPieceBitboard(capturePiece, toMask)
    if (capturePiece == BITBOARD_WR) {
        if (toMask == WHITEKINGSIDEROOKMASK)
            castlePrivileges = castlePrivileges and CASTLEPRIV_WK.inv()
        else if (toMask == WHITEQUEENSIDEROOKMASK)
            castlePrivileges = castlePrivileges and CASTLEPRIV_WQ.inv()
    }
}

private fun EngineBoard.adjustKingVariablesForBlackKingMove(compactMove: Int) {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val fromMask = 1L shl moveFrom
    val toMask = 1L shl moveTo
    castlePrivileges = castlePrivileges and CASTLEPRIV_BNONE
    blackKingSquare = moveTo
    if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK)
        engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
    else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK)
        engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
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
        engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, toMask shl 8)
    } else if (toMask == moveHistory[numMovesMade]!!.enPassantBitboard) {
        engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
        moveHistory[numMovesMade]!!.capturePiece = BITBOARD_WP
        whitePawnValues -= VALUE_PAWN
    } else if (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL != 0) {
        val promotionPieceMask = compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL
        blackPawnValues -= VALUE_PAWN
        when (promotionPieceMask) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                engineBitboards.orPieceBitboard(BITBOARD_BQ, toMask)
                blackPieceValues += VALUE_QUEEN
            }
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                engineBitboards.orPieceBitboard(BITBOARD_BR, toMask)
                blackPieceValues += VALUE_ROOK
            }
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                engineBitboards.orPieceBitboard(BITBOARD_BN, toMask)
                blackPieceValues += VALUE_KNIGHT
            }
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                engineBitboards.orPieceBitboard(BITBOARD_BB, toMask)
                blackPieceValues += VALUE_BISHOP
            }
        }
        engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask)
    }
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece: Int, moveTo: Int) {
    val toMask = 1L shl moveTo
    moveHistory[numMovesMade]!!.capturePiece = capturePiece
    halfMoveCount = 0
    engineBitboards.xorPieceBitboard(capturePiece, toMask)
    if (capturePiece == BITBOARD_BR) {
        if (toMask == BLACKKINGSIDEROOKMASK)
            castlePrivileges = castlePrivileges and CASTLEPRIV_BK.inv()
        else if (toMask == BLACKQUEENSIDEROOKMASK)
            castlePrivileges = castlePrivileges and CASTLEPRIV_BQ.inv()
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
        engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
    } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
        engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
    }
}

private fun EngineBoard.adjustCastlePrivilegesForWhiteRookMove(moveFrom: Int) {
    if (moveFrom == Square.A1.bitRef)
        castlePrivileges = castlePrivileges and CASTLEPRIV_WQ.inv()
    else if (moveFrom == Square.H1.bitRef)
        castlePrivileges = castlePrivileges and CASTLEPRIV_WK.inv()
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeSpecialWhitePawnMoveAdjustments(compactMove: Int) {
    val fromMask = 1L shl (compactMove ushr 16)
    val toMask = 1L shl (compactMove and 63)
    halfMoveCount = 0
    if (toMask and RANK_4 != 0L && fromMask and RANK_2 != 0L) {
        engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, fromMask shl 8)
    } else if (toMask == moveHistory[numMovesMade]!!.enPassantBitboard) {
        engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
        moveHistory[numMovesMade]!!.capturePiece = BITBOARD_BP
        blackPawnValues -= VALUE_PAWN
    } else if (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL != 0) {
        whitePawnValues -= VALUE_PAWN
        when (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                engineBitboards.orPieceBitboard(BITBOARD_WQ, toMask)
                whitePieceValues += VALUE_QUEEN
            }
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                engineBitboards.orPieceBitboard(BITBOARD_WR, toMask)
                whitePieceValues += VALUE_ROOK
            }
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                engineBitboards.orPieceBitboard(BITBOARD_WN, toMask)
                whitePieceValues += VALUE_KNIGHT
            }
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                engineBitboards.orPieceBitboard(BITBOARD_WB, toMask)
                whitePieceValues += VALUE_BISHOP
            }
            else -> throw InvalidMoveException("compactMove $compactMove produced invalid promotion piece")
        }
        engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask)
    }
}
