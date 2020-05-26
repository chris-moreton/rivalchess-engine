package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
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
    switchMover()
    val t = this.engineBitboards.getPieceBitboard(BitboardType.FRIENDLY)
    this.engineBitboards.setPieceBitboard(BitboardType.FRIENDLY, this.engineBitboards.getPieceBitboard(BitboardType.ENEMY))
    this.engineBitboards.setPieceBitboard(BitboardType.ENEMY, t)
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
    switchMover()
    this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, moveHistory[numMovesMade].enPassantBitboard)
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
            this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask or fromMask)
            this.engineBitboards.xorPieceBitboard(BitboardType.BP, toMask ushr 8)
            squareContents[toSquare - 8] = SquareOccupant.BP
            return true
        } else if (moveHistory[numMovesMade].movePiece == SquareOccupant.BP) {
            this.engineBitboards.xorPieceBitboard(BitboardType.BP, toMask or fromMask)
            this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask shl 8)
            squareContents[toSquare + 8] = SquareOccupant.WP
            return true
        }
    }
    return false
}

private fun EngineBoard.replaceCastledRook(fromMask: Long, toMask: Long, movePiece: SquareOccupant) {
    if (movePiece == SquareOccupant.WK) {
        if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H1.bitRef] = SquareOccupant.WR
            squareContents[Square.F1.bitRef] = SquareOccupant.NONE
        } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A1.bitRef] = SquareOccupant.WR
            squareContents[Square.D1.bitRef] = SquareOccupant.NONE
        }
    } else if (movePiece == SquareOccupant.BK) {
        if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H8.bitRef] = SquareOccupant.BR
            squareContents[Square.F8.bitRef] = SquareOccupant.NONE
        } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
            this.engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKQUEENSIDECASTLEROOKMOVE)
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
            this.engineBitboards.xorPieceBitboard(BitboardType.WP, fromMask)
            when (PromotionPieceMask.fromValue(promotionPiece)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> this.engineBitboards.xorPieceBitboard(BitboardType.WQ, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> this.engineBitboards.xorPieceBitboard(BitboardType.WB, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> this.engineBitboards.xorPieceBitboard(BitboardType.WN, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> this.engineBitboards.xorPieceBitboard(BitboardType.WR, toMask)
                else -> throw InvalidMoveException("Illegal promotion piece $promotionPiece")
            }
        } else {
            this.engineBitboards.xorPieceBitboard(BitboardType.BP, fromMask)
            when (PromotionPieceMask.fromValue(promotionPiece)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> this.engineBitboards.xorPieceBitboard(BitboardType.BQ, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> this.engineBitboards.xorPieceBitboard(BitboardType.BB, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> this.engineBitboards.xorPieceBitboard(BitboardType.BN, toMask)
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> this.engineBitboards.xorPieceBitboard(BitboardType.BR, toMask)
                else -> throw InvalidMoveException("Invalid promotionPiece $promotionPiece")
            }
        }
        return true
    }
    return false
}

private fun EngineBoard.replaceMovedPiece(fromSquare: Int, fromMask: Long, toMask: Long): SquareOccupant {
    val movePiece = moveHistory[numMovesMade].movePiece
    this.engineBitboards.xorPieceBitboard(movePiece.index, toMask or fromMask)
    if (movePiece == SquareOccupant.WK) {
        whiteKingSquare = fromSquare.toByte()
    } else if (movePiece == SquareOccupant.BK) {
        blackKingSquare = fromSquare.toByte()
    }
    return movePiece
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeNonTrivialMoveTypeAdjustments(compactMove: Int, capturePiece: SquareOccupant, movePiece: SquareOccupant?) {
    val moveFrom = (compactMove ushr 16).toByte()
    val moveTo = (compactMove and 63).toByte()
    val toMask = 1L shl moveTo.toInt()
    if (mover == Colour.WHITE) {
        if (movePiece == SquareOccupant.WP) {
            makeSpecialWhitePawnMoveAdjustments(compactMove)
        } else if (movePiece == SquareOccupant.WR) {
            adjustCastlePrivilegesForWhiteRookMove(moveFrom)
        } else if (movePiece == SquareOccupant.WK) {
            adjustKingVariablesForWhiteKingMove(compactMove)
        }
        if (capturePiece != SquareOccupant.NONE) {
            makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece, toMask)
        }
    } else {
        if (movePiece == SquareOccupant.BP) {
            makeSpecialBlackPawnMoveAdjustments(compactMove)
        } else if (movePiece == SquareOccupant.BR) {
            adjustCastlePrivilegesForBlackRookMove(moveFrom)
        } else if (movePiece == SquareOccupant.BK) {
            adjustKingVariablesForBlackKingMove(compactMove)
        }
        if (capturePiece != SquareOccupant.NONE) {
            makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece, toMask)
        }
    }
}

@Throws(InvalidMoveException::class)
fun EngineBoard.makeMove(engineMove: EngineMove): Boolean {
    val compactMove = engineMove.compact
    val moveFrom = (compactMove ushr 16).toByte()
    val moveTo = (compactMove and 63).toByte()
    val capturePiece = squareContents[moveTo.toInt()]
    val movePiece = squareContents[moveFrom.toInt()]

    val moveDetail = MoveDetail()
    moveDetail.capturePiece = SquareOccupant.NONE
    moveDetail.move = compactMove
    moveDetail.hashValue = boardHashObject.trackedHashValue
    moveDetail.isOnNullMove = isOnNullMove
    moveDetail.pawnHashValue = boardHashObject.trackedPawnHashValue
    moveDetail.halfMoveCount = halfMoveCount.toByte()
    moveDetail.enPassantBitboard = this.engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE)
    moveDetail.castlePrivileges = castlePrivileges.toByte()
    moveDetail.movePiece = movePiece

    if (moveHistory.size <= numMovesMade) {
        moveHistory.add(moveDetail)
    } else {
        moveHistory[numMovesMade] = moveDetail
    }

    boardHashObject.move(this, engineMove)
    isOnNullMove = false
    halfMoveCount++
    this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 0)
    this.engineBitboards.movePiece(movePiece, compactMove)
    squareContents[moveFrom.toInt()] = SquareOccupant.NONE
    squareContents[moveTo.toInt()] = movePiece
    makeNonTrivialMoveTypeAdjustments(compactMove, capturePiece, movePiece)
    switchMover()

    numMovesMade++

    calculateSupplementaryBitboards()
    if (this.isCheck(mover.opponent())) {
        unMakeMove()
        return false
    }
    return true
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece: SquareOccupant, toMask: Long) {
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
    val moveFrom = (compactMove ushr 16).toByte()
    val moveTo = (compactMove and 63).toByte()
    val fromMask = 1L shl moveFrom.toInt()
    val toMask = 1L shl moveTo.toInt()
    castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BNONE.value
    blackKingSquare = moveTo
    if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKKINGSIDECASTLEROOKMOVE)
        squareContents[Square.H8.bitRef] = SquareOccupant.NONE
        squareContents[Square.F8.bitRef] = SquareOccupant.BR
    } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKQUEENSIDECASTLEROOKMOVE)
        squareContents[Square.A8.bitRef] = SquareOccupant.NONE
        squareContents[Square.D8.bitRef] = SquareOccupant.BR
    }
}

private fun EngineBoard.adjustCastlePrivilegesForBlackRookMove(moveFrom: Byte) {
    if (moveFrom.toInt() == Square.A8.bitRef) castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value.inv() else if (moveFrom.toInt() == Square.H8.bitRef) castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value.inv()
}

@Throws(InvalidMoveException::class)
private fun EngineBoard.makeSpecialBlackPawnMoveAdjustments(compactMove: Int) {
    val moveFrom = (compactMove ushr 16).toByte()
    val moveTo = (compactMove and 63).toByte()
    val fromMask = 1L shl moveFrom.toInt()
    val toMask = 1L shl moveTo.toInt()
    halfMoveCount = 0
    if (toMask and RANK_5 != 0L && fromMask and RANK_7 != 0L) {
        this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, toMask shl 8)
    } else if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
        this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask shl 8)
        moveHistory[numMovesMade].capturePiece = SquareOccupant.WP
        squareContents[moveTo + 8] = SquareOccupant.NONE
    } else if (compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value != 0) {
        val promotionPieceMask = compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
        when (PromotionPieceMask.fromValue(promotionPieceMask)) {
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                this.engineBitboards.orPieceBitboard(BitboardType.BQ, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.BQ
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                this.engineBitboards.orPieceBitboard(BitboardType.BR, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.BR
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                this.engineBitboards.orPieceBitboard(BitboardType.BN, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.BN
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                this.engineBitboards.orPieceBitboard(BitboardType.BB, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.BB
            }
            else -> throw InvalidMoveException(
                    "compactMove $compactMove produced invalid promotion piece")
        }
        this.engineBitboards.xorPieceBitboard(BitboardType.BP, toMask)
    }
}

private fun EngineBoard.makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece: SquareOccupant?, toMask: Long) {
    moveHistory[numMovesMade].capturePiece = capturePiece!!
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
    val moveFrom = (compactMove ushr 16).toByte()
    val moveTo = (compactMove and 63).toByte()
    val fromMask = 1L shl moveFrom.toInt()
    val toMask = 1L shl moveTo.toInt()
    whiteKingSquare = moveTo
    castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WNONE.value
    if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEKINGSIDECASTLEROOKMOVE)
        squareContents[Square.H1.bitRef] = SquareOccupant.NONE
        squareContents[Square.F1.bitRef] = SquareOccupant.WR
    } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
        this.engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEQUEENSIDECASTLEROOKMOVE)
        squareContents[Square.A1.bitRef] = SquareOccupant.NONE
        squareContents[Square.D1.bitRef] = SquareOccupant.WR
    }
}

private fun EngineBoard.adjustCastlePrivilegesForWhiteRookMove(moveFrom: Byte) {
    if (moveFrom.toInt() == Square.A1.bitRef) {
        castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value.inv()
    } else if (moveFrom.toInt() == Square.H1.bitRef) {
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
        this.engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, fromMask shl 8)
    } else if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
        this.engineBitboards.xorPieceBitboard(SquareOccupant.BP.index, toMask ushr 8)
        moveHistory[numMovesMade].capturePiece = SquareOccupant.BP
        squareContents[moveTo - 8] = SquareOccupant.NONE
    } else if (compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value != 0) {
        val promotionPieceMask = PromotionPieceMask.fromValue(compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value)
        when (promotionPieceMask) {
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                this.engineBitboards.orPieceBitboard(BitboardType.WQ, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WQ
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                this.engineBitboards.orPieceBitboard(BitboardType.WR, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WR
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                this.engineBitboards.orPieceBitboard(BitboardType.WN, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WN
            }
            PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                this.engineBitboards.orPieceBitboard(BitboardType.WB, toMask)
                squareContents[moveTo.toInt()] = SquareOccupant.WB
            }
            else -> throw InvalidMoveException(
                    "compactMove $compactMove produced invalid promotion piece")
        }
        this.engineBitboards.xorPieceBitboard(BitboardType.WP, toMask)
    }
}
