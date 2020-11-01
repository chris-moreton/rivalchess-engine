package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.type.MoveDetail
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.nullMoveCore() {
    boardHashObject.makeNullMove()
    mover = mover.opponent()
    val friendlyHolder = engineBitboards.pieceBitboards[BITBOARD_FRIENDLY]
    engineBitboards.setPieceBitboard(BITBOARD_FRIENDLY, engineBitboards.pieceBitboards[BITBOARD_ENEMY])
    engineBitboards.setPieceBitboard(BITBOARD_ENEMY, friendlyHolder)
}

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.makeNullMove(ply: Int) {
    nullMoveCore()
    nullMovesMade ++
    isOnNullMove[ply] = true
}

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.unMakeNullMove(ply: Int) {
    nullMoveCore()
    nullMovesMade --
    isOnNullMove[ply] = false
}

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.makeMove(compactMove: Int, ignoreCheck: Boolean = false, updateHash: Boolean = true): Boolean {
    val moveFrom = (compactMove ushr 16)
    val moveTo = (compactMove and 63)
    val movePiece = getBitboardTypeOfPieceOnSquare(moveFrom, mover)
    val targetSquarePiece = getBitboardTypeOfPieceOnSquare(moveTo, mover.opponent())

    val moveDetail = MoveDetail()
    moveDetail.capturePiece = targetSquarePiece
    moveDetail.move = compactMove
    moveDetail.hashValue = boardHashObject.trackedHashValue
    moveDetail.halfMoveCount = halfMoveCount
    moveDetail.enPassantBitboard = engineBitboards.pieceBitboards[BITBOARD_ENPASSANTSQUARE]
    moveDetail.castlePrivileges = castlePrivileges
    moveDetail.movePiece = movePiece

    moveHistory[numMovesMade] = moveDetail

    halfMoveCount++
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)

    engineBitboards.movePiece(movePiece, compactMove)

    // moveDetail.capturePiece gets updated here
    makeNonTrivialMoveTypeAdjustments(moveFrom, moveTo, compactMove, targetSquarePiece, movePiece)

    numMovesMade++
    mover = mover.opponent()

    calculateSupplementaryBitboards()

    if (!ignoreCheck && isCheck(mover.opponent())) {
        unMakeMove(false)
        return false
    }

    if (updateHash) boardHashObject.move(compactMove, movePiece, targetSquarePiece)

    return true
}

@kotlin.ExperimentalUnsignedTypes
fun EngineBoard.unMakeMove(updateHash: Boolean = true) {
    val moveMade = moveHistory[--numMovesMade]!!
    halfMoveCount = moveMade.halfMoveCount
    mover = mover.opponent()
    engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, moveMade.enPassantBitboard)
    castlePrivileges = moveMade.castlePrivileges
    val fromSquare = moveMade.move ushr 16 and 63
    val toSquare = moveMade.move and 63
    val fromMask = 1L shl fromSquare
    val toMask = 1L shl toSquare
    if (updateHash) boardHashObject.unMove(this)

    // deal with en passants first, they are special moves as well as capture moves, so just get them out of the way
    if (!unMakeEnPassants(moveMade.enPassantBitboard, moveMade.movePiece, fromMask, toMask)) {

        // put capture piece back on toSquare, we don't get here if an en passant has just been unmade
        replaceCapturedPiece(moveMade.capturePiece, toMask)

        // for promotions, remove promotion piece from toSquare
        if (!removePromotionPiece(moveMade.move and PROMOTION_PIECE_TOSQUARE_MASK_FULL, fromMask, toMask)) {

            // now that promotions are out of the way, we can remove the moving piece from toSquare and put it back on fromSquare
            val movePiece = replaceMovedPiece(moveMade.movePiece, fromMask, toMask)

            // for castles, replace the rook
            replaceCastledRook(fromMask, toMask, movePiece)
        }
    }
    calculateSupplementaryBitboards()
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.unMakeEnPassants(enPassantBitboard: Long, movePiece: Int, fromMask: Long, toMask: Long): Boolean {
    if (toMask == enPassantBitboard) {
        if (movePiece == BITBOARD_WP) {
            engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask or fromMask)
            engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
            return true
        } else if (movePiece == BITBOARD_BP) {
            engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask or fromMask)
            engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask shl 8)
            return true
        }
    }
    return false
}

@kotlin.ExperimentalUnsignedTypes
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

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.replaceCapturedPiece(capturePiece: Int, toMask: Long) {
    if (capturePiece != BITBOARD_NONE) engineBitboards.xorPieceBitboard(capturePiece, toMask)
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.removePromotionPiece(promotionPiece: Int, fromMask: Long, toMask: Long): Boolean {
    if (promotionPiece != 0) {
        if (mover == Colour.WHITE) {
            engineBitboards.xorPieceBitboard(BITBOARD_WP, fromMask)
            when (promotionPiece) {
                PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> engineBitboards.xorPieceBitboard(BITBOARD_WQ, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> engineBitboards.xorPieceBitboard(BITBOARD_WB, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> engineBitboards.xorPieceBitboard(BITBOARD_WN, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> engineBitboards.xorPieceBitboard(BITBOARD_WR, toMask)
            }
        } else {
            engineBitboards.xorPieceBitboard(BITBOARD_BP, fromMask)
            when (promotionPiece) {
                PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> engineBitboards.xorPieceBitboard(BITBOARD_BQ, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> engineBitboards.xorPieceBitboard(BITBOARD_BB, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> engineBitboards.xorPieceBitboard(BITBOARD_BN, toMask)
                PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> engineBitboards.xorPieceBitboard(BITBOARD_BR, toMask)
            }
        }
        return true
    }
    return false
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.replaceMovedPiece(movePiece: Int, fromMask: Long, toMask: Long): Int {
    engineBitboards.xorPieceBitboard(movePiece, toMask or fromMask)
    return movePiece
}

@kotlin.ExperimentalUnsignedTypes
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

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece: Int, moveTo: Int) {

    moveHistory[numMovesMade]!!.capturePiece = capturePiece
    halfMoveCount = 0

    val toMask = 1L shl moveTo

    engineBitboards.xorPieceBitboard(capturePiece, toMask)
    if (capturePiece == BITBOARD_WR) {
        if (toMask == WHITEKINGSIDEROOKMASK) castlePrivileges = castlePrivileges and CASTLEPRIV_WK.inv()
        else if (toMask == WHITEQUEENSIDEROOKMASK) castlePrivileges = castlePrivileges and CASTLEPRIV_WQ.inv()
    }
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.adjustKingVariablesForBlackKingMove(compactMove: Int) {
    val fromMask = 1L shl (compactMove ushr 16)
    val toMask = 1L shl (compactMove and 63)
    castlePrivileges = castlePrivileges and CASTLEPRIV_BNONE
    if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKKINGSIDECASTLEROOKMOVE)
    else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) engineBitboards.xorPieceBitboard(BITBOARD_BR, BLACKQUEENSIDECASTLEROOKMOVE)
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.adjustCastlePrivilegesForBlackRookMove(moveFrom: Int) {
    if (moveFrom == Square.A8.bitRef) castlePrivileges = castlePrivileges and CASTLEPRIV_BQ.inv() else
        if (moveFrom == Square.H8.bitRef) castlePrivileges = castlePrivileges and CASTLEPRIV_BK.inv()
}

@kotlin.ExperimentalUnsignedTypes
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
    } else if (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL != 0) {
        val promotionPieceMask = compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL
        when (promotionPieceMask) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                engineBitboards.orPieceBitboard(BITBOARD_BQ, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                engineBitboards.orPieceBitboard(BITBOARD_BR, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                engineBitboards.orPieceBitboard(BITBOARD_BN, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                engineBitboards.orPieceBitboard(BITBOARD_BB, toMask)
            }
        }
        engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask)
    }
}

@kotlin.ExperimentalUnsignedTypes
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

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.adjustKingVariablesForWhiteKingMove(compactMove: Int) {
    val fromMask = 1L shl (compactMove ushr 16)
    val toMask = 1L shl (compactMove and 63)
    castlePrivileges = castlePrivileges and CASTLEPRIV_WNONE
    if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
        engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEKINGSIDECASTLEROOKMOVE)
    } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
        engineBitboards.xorPieceBitboard(BITBOARD_WR, WHITEQUEENSIDECASTLEROOKMOVE)
    }
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.adjustCastlePrivilegesForWhiteRookMove(moveFrom: Int) {
    if (moveFrom == Square.A1.bitRef)
        castlePrivileges = castlePrivileges and CASTLEPRIV_WQ.inv()
    else if (moveFrom == Square.H1.bitRef)
        castlePrivileges = castlePrivileges and CASTLEPRIV_WK.inv()
}

@kotlin.ExperimentalUnsignedTypes
private fun EngineBoard.makeSpecialWhitePawnMoveAdjustments(compactMove: Int) {
    val fromMask = 1L shl (compactMove ushr 16)
    val toMask = 1L shl (compactMove and 63)
    halfMoveCount = 0
    if (toMask and RANK_4 != 0L && fromMask and RANK_2 != 0L) {
        engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, fromMask shl 8)
    } else if (toMask == moveHistory[numMovesMade]!!.enPassantBitboard) {
        engineBitboards.xorPieceBitboard(BITBOARD_BP, toMask ushr 8)
        moveHistory[numMovesMade]!!.capturePiece = BITBOARD_BP
    } else if (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL != 0) {
        when (compactMove and PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
            PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                engineBitboards.orPieceBitboard(BITBOARD_WQ, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                engineBitboards.orPieceBitboard(BITBOARD_WR, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                engineBitboards.orPieceBitboard(BITBOARD_WN, toMask)
            }
            PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                engineBitboards.orPieceBitboard(BITBOARD_WB, toMask)
            }
        }
        engineBitboards.xorPieceBitboard(BITBOARD_WP, toMask)
    }
}
