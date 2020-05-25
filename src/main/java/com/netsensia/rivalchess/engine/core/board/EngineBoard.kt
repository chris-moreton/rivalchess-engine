package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.getSetBits
import com.netsensia.rivalchess.config.Hash
import com.netsensia.rivalchess.engine.core.FEN_START_POS
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.hash.BoardHash
import com.netsensia.rivalchess.engine.core.search.inCheck
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.engine.core.type.MoveDetail
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.enums.PromotionPieceMask.Companion.fromValue
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.*
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import java.util.*
import kotlin.collections.ArrayList

class EngineBoard @JvmOverloads constructor(board: Board = getBoardModel(FEN_START_POS)) {
    @JvmField
    val engineBitboards = EngineBitboards.instance
    val squareContents = Array(64){SquareOccupant.NONE}
    var moveHistory: MutableList<MoveDetail> = ArrayList()

    val boardHashObject = BoardHash()

    var castlePrivileges = 0
    var isWhiteToMove = false
    var whiteKingSquare: Byte = 0
    var blackKingSquare: Byte = 0
    var numMovesMade = 0
    var isOnNullMove = false
    var halfMoveCount = 0

    fun setBoard(board: Board) {
        numMovesMade = 0
        moveHistory.clear()
        halfMoveCount = board.halfMoveCount
        setEngineBoardVars(board)
        boardHashObject.hashTableVersion = 0
        boardHashObject.setHashSizeMB(Hash.DEFAULT_HASHTABLE_SIZE_MB.value)
        boardHashObject.initialiseHashCode(this)
    }

    fun getSquareOccupant(bitRef: Int): SquareOccupant {
        return squareContents[bitRef]
    }

    val squareOccupants: List<SquareOccupant>
        get() = squareContents.toList()


    fun isGameOver(): Boolean {
            return generateLegalMoves().toList().stream()
                    .filter { m: Int -> isMoveLegal(m) }
                    .count() == 0L
        }

    fun setEngineBoardVars(board: Board) {
        isWhiteToMove = board.sideToMove == Colour.WHITE
        engineBitboards.reset()
        setSquareContents(board)
        setEnPassantBitboard(board)
        setCastlePrivileges(board)
        calculateSupplementaryBitboards()
    }

    private fun setSquareContents(board: Board) {
        var bitNum: Byte
        var bitSet: Long
        var pieceIndex: Int
        Arrays.fill(squareContents, SquareOccupant.NONE)
        for (y in 0..7) {
            for (x in 0..7) {
                bitNum = (63 - 8 * y - x).toByte()
                bitSet = 1L shl bitNum.toInt()
                val squareOccupant = board.getSquareOccupant(Square.fromCoords(x, y))
                squareContents[bitNum.toInt()] = squareOccupant
                pieceIndex = squareOccupant.index
                if (pieceIndex != -1) {
                    engineBitboards.orPieceBitboard(BitboardType.fromIndex(pieceIndex), bitSet)
                }
                if (squareOccupant == SquareOccupant.WK) {
                    whiteKingSquare = bitNum
                }
                if (squareOccupant == SquareOccupant.BK) {
                    blackKingSquare = bitNum
                }
            }
        }
    }

    private fun setEnPassantBitboard(board: Board) {
        val ep = board.enPassantFile
        if (ep == -1) {
            engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 0)
        } else {
            if (board.sideToMove == Colour.WHITE) {
                engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 1L shl 40 + (7 - ep))
            } else {
                engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 1L shl 16 + (7 - ep))
            }
        }
    }

    private fun setCastlePrivileges(board: Board) {
        castlePrivileges = 0
        castlePrivileges = castlePrivileges or if (board.isKingSideCastleAvailable(Colour.WHITE)) CastleBitMask.CASTLEPRIV_WK.value else 0
        castlePrivileges = castlePrivileges or if (board.isQueenSideCastleAvailable(Colour.WHITE)) CastleBitMask.CASTLEPRIV_WQ.value else 0
        castlePrivileges = castlePrivileges or if (board.isKingSideCastleAvailable(Colour.BLACK)) CastleBitMask.CASTLEPRIV_BK.value else 0
        castlePrivileges = castlePrivileges or if (board.isQueenSideCastleAvailable(Colour.BLACK)) CastleBitMask.CASTLEPRIV_BQ.value else 0
    }

    fun calculateSupplementaryBitboards() {
        if (isWhiteToMove) {
            engineBitboards.setPieceBitboard(BitboardType.FRIENDLY,
                    engineBitboards.getPieceBitboard(BitboardType.WP) or engineBitboards.getPieceBitboard(BitboardType.WN) or
                            engineBitboards.getPieceBitboard(BitboardType.WB) or engineBitboards.getPieceBitboard(BitboardType.WQ) or
                            engineBitboards.getPieceBitboard(BitboardType.WK) or engineBitboards.getPieceBitboard(BitboardType.WR))
            engineBitboards.setPieceBitboard(BitboardType.ENEMY,
                    engineBitboards.getPieceBitboard(BitboardType.BP) or engineBitboards.getPieceBitboard(BitboardType.BN) or
                            engineBitboards.getPieceBitboard(BitboardType.BB) or engineBitboards.getPieceBitboard(BitboardType.BQ) or
                            engineBitboards.getPieceBitboard(BitboardType.BK) or engineBitboards.getPieceBitboard(BitboardType.BR))
        } else {
            engineBitboards.setPieceBitboard(BitboardType.ENEMY,
                    engineBitboards.getPieceBitboard(BitboardType.WP) or engineBitboards.getPieceBitboard(BitboardType.WN) or
                            engineBitboards.getPieceBitboard(BitboardType.WB) or engineBitboards.getPieceBitboard(BitboardType.WQ) or
                            engineBitboards.getPieceBitboard(BitboardType.WK) or engineBitboards.getPieceBitboard(BitboardType.WR))
            engineBitboards.setPieceBitboard(BitboardType.FRIENDLY,
                    engineBitboards.getPieceBitboard(BitboardType.BP) or engineBitboards.getPieceBitboard(BitboardType.BN) or
                            engineBitboards.getPieceBitboard(BitboardType.BB) or engineBitboards.getPieceBitboard(BitboardType.BQ) or
                            engineBitboards.getPieceBitboard(BitboardType.BK) or engineBitboards.getPieceBitboard(BitboardType.BR))
        }
        engineBitboards.setPieceBitboard(BitboardType.ALL, engineBitboards.getPieceBitboard(BitboardType.FRIENDLY) or engineBitboards.getPieceBitboard(BitboardType.ENEMY))
    }

    val isNotOnNullMove: Boolean
        get() = !isOnNullMove



    @Throws(InvalidMoveException::class)
    fun makeMove(engineMove: EngineMove): Boolean {
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
        moveDetail.enPassantBitboard = engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE)
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
        engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 0)
        engineBitboards.movePiece(movePiece, compactMove)
        squareContents[moveFrom.toInt()] = SquareOccupant.NONE
        squareContents[moveTo.toInt()] = movePiece
        makeNonTrivialMoveTypeAdjustments(compactMove, capturePiece, movePiece)
        switchMover()

        numMovesMade++

        calculateSupplementaryBitboards()
        if (inCheck(whiteKingSquare.toInt(), blackKingSquare.toInt(), mover.opponent())) {
            unMakeMove()
            return false
        }
        return true
    }

    @Throws(InvalidMoveException::class)
    private fun makeNonTrivialMoveTypeAdjustments(compactMove: Int, capturePiece: SquareOccupant, movePiece: SquareOccupant?) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val toMask = 1L shl moveTo.toInt()
        if (isWhiteToMove) {
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

    private fun makeAdjustmentsFollowingCaptureOfWhitePiece(capturePiece: SquareOccupant, toMask: Long) {
        moveHistory[numMovesMade].capturePiece = capturePiece
        halfMoveCount = 0
        engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
        if (capturePiece == SquareOccupant.WR) {
            if (toMask == WHITEKINGSIDEROOKMASK) {
                castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value.inv()
            } else if (toMask == WHITEQUEENSIDEROOKMASK) {
                castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value.inv()
            }
        }
    }

    private fun adjustKingVariablesForBlackKingMove(compactMove: Int) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val fromMask = 1L shl moveFrom.toInt()
        val toMask = 1L shl moveTo.toInt()
        castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BNONE.value
        blackKingSquare = moveTo
        if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H8.bitRef] = SquareOccupant.NONE
            squareContents[Square.F8.bitRef] = SquareOccupant.BR
        } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A8.bitRef] = SquareOccupant.NONE
            squareContents[Square.D8.bitRef] = SquareOccupant.BR
        }
    }

    private fun adjustCastlePrivilegesForBlackRookMove(moveFrom: Byte) {
        if (moveFrom.toInt() == Square.A8.bitRef) castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value.inv() else if (moveFrom.toInt() == Square.H8.bitRef) castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value.inv()
    }

    @Throws(InvalidMoveException::class)
    private fun makeSpecialBlackPawnMoveAdjustments(compactMove: Int) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val fromMask = 1L shl moveFrom.toInt()
        val toMask = 1L shl moveTo.toInt()
        halfMoveCount = 0
        if (toMask and RANK_5 != 0L && fromMask and RANK_7 != 0L) {
            engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, toMask shl 8)
        } else if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
            engineBitboards.xorPieceBitboard(BitboardType.WP, toMask shl 8)
            moveHistory[numMovesMade].capturePiece = SquareOccupant.WP
            squareContents[moveTo + 8] = SquareOccupant.NONE
        } else if (compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value != 0) {
            val promotionPieceMask = compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
            when (fromValue(promotionPieceMask)) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                    engineBitboards.orPieceBitboard(BitboardType.BQ, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.BQ
                }
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                    engineBitboards.orPieceBitboard(BitboardType.BR, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.BR
                }
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                    engineBitboards.orPieceBitboard(BitboardType.BN, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.BN
                }
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                    engineBitboards.orPieceBitboard(BitboardType.BB, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.BB
                }
                else -> throw InvalidMoveException(
                        "compactMove $compactMove produced invalid promotion piece")
            }
            engineBitboards.xorPieceBitboard(BitboardType.BP, toMask)
        }
    }

    private fun makeAdjustmentsFollowingCaptureOfBlackPiece(capturePiece: SquareOccupant?, toMask: Long) {
        moveHistory[numMovesMade].capturePiece = capturePiece!!
        halfMoveCount = 0
        engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
        if (capturePiece == SquareOccupant.BR) {
            if (toMask == BLACKKINGSIDEROOKMASK) {
                castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value.inv()
            } else if (toMask == BLACKQUEENSIDEROOKMASK) {
                castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value.inv()
            }
        }
    }

    private fun adjustKingVariablesForWhiteKingMove(compactMove: Int) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val fromMask = 1L shl moveFrom.toInt()
        val toMask = 1L shl moveTo.toInt()
        whiteKingSquare = moveTo
        castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WNONE.value
        if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEKINGSIDECASTLEROOKMOVE)
            squareContents[Square.H1.bitRef] = SquareOccupant.NONE
            squareContents[Square.F1.bitRef] = SquareOccupant.WR
        } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
            engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEQUEENSIDECASTLEROOKMOVE)
            squareContents[Square.A1.bitRef] = SquareOccupant.NONE
            squareContents[Square.D1.bitRef] = SquareOccupant.WR
        }
    }

    private fun adjustCastlePrivilegesForWhiteRookMove(moveFrom: Byte) {
        if (moveFrom.toInt() == Square.A1.bitRef) {
            castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value.inv()
        } else if (moveFrom.toInt() == Square.H1.bitRef) {
            castlePrivileges = castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value.inv()
        }
    }

    @Throws(InvalidMoveException::class)
    private fun makeSpecialWhitePawnMoveAdjustments(compactMove: Int) {
        val moveFrom = (compactMove ushr 16).toByte()
        val moveTo = (compactMove and 63).toByte()
        val fromMask = 1L shl moveFrom.toInt()
        val toMask = 1L shl moveTo.toInt()
        halfMoveCount = 0
        if (toMask and RANK_4 != 0L && fromMask and RANK_2 != 0L) {
            engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, fromMask shl 8)
        } else if (toMask == moveHistory[numMovesMade].enPassantBitboard) {
            engineBitboards.xorPieceBitboard(SquareOccupant.BP.index, toMask ushr 8)
            moveHistory[numMovesMade].capturePiece = SquareOccupant.BP
            squareContents[moveTo - 8] = SquareOccupant.NONE
        } else if (compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value != 0) {
            val promotionPieceMask = fromValue(compactMove and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value)
            when (promotionPieceMask) {
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> {
                    engineBitboards.orPieceBitboard(BitboardType.WQ, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.WQ
                }
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> {
                    engineBitboards.orPieceBitboard(BitboardType.WR, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.WR
                }
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> {
                    engineBitboards.orPieceBitboard(BitboardType.WN, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.WN
                }
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> {
                    engineBitboards.orPieceBitboard(BitboardType.WB, toMask)
                    squareContents[moveTo.toInt()] = SquareOccupant.WB
                }
                else -> throw InvalidMoveException(
                        "compactMove $compactMove produced invalid promotion piece")
            }
            engineBitboards.xorPieceBitboard(BitboardType.WP, toMask)
        }
    }

    val lastMoveMade: MoveDetail
        get() = moveHistory[numMovesMade]







    val whitePieceValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WN)) * pieceValue(Piece.KNIGHT) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WR)) * pieceValue(Piece.ROOK) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WB)) * pieceValue(Piece.BISHOP) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WQ)) * pieceValue(Piece.QUEEN)

    val blackPieceValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BN)) * pieceValue(Piece.KNIGHT) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BR)) * pieceValue(Piece.ROOK) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BB)) * pieceValue(Piece.BISHOP) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BQ)) * pieceValue(Piece.QUEEN)

    val whitePawnValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WP)) * pieceValue(Piece.PAWN)

    val blackPawnValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BP)) * pieceValue(Piece.PAWN)





    fun lastCapturePiece(): SquareOccupant {
        return moveHistory[numMovesMade - 1].capturePiece
    }

    fun wasCapture(): Boolean {
        return moveHistory[numMovesMade - 1].capturePiece == SquareOccupant.NONE
    }

    fun wasPawnPush(): Boolean {
        val toSquare = moveHistory[numMovesMade - 1].move and 63
        val movePiece = moveHistory[numMovesMade - 1].movePiece
        if (movePiece.piece != Piece.PAWN) {
            return false
        }
        if (toSquare >= 48 || toSquare <= 15) {
            return true
        }
        if (!isWhiteToMove) // white made the last move
        {
            if (toSquare >= 40) return java.lang.Long.bitCount(whitePassedPawnMask[toSquare] and
                    engineBitboards.getPieceBitboard(BitboardType.BP)) == 0
        } else {
            if (toSquare <= 23) return java.lang.Long.bitCount(blackPassedPawnMask[toSquare] and
                    engineBitboards.getPieceBitboard(BitboardType.WP)) == 0
        }
        return false
    }

    private fun switchMover() {
        isWhiteToMove = !isWhiteToMove
    }

    val allPiecesBitboard: Long
        get() = engineBitboards.getPieceBitboard(BitboardType.ALL)

    fun getWhiteKingSquare() = whiteKingSquare.toInt()

    fun getBlackKingSquare() = blackKingSquare.toInt()

    @Deprecated("")
    fun getBitboardByIndex(index: Int): Long {
        return engineBitboards.getPieceBitboard(BitboardType.fromIndex(index))
    }

    fun getBitboard(bitboardType: BitboardType): Long {
        return engineBitboards.getPieceBitboard(bitboardType)
    }

    val mover: Colour
        get() = if (isWhiteToMove) Colour.WHITE else Colour.BLACK

    fun previousOccurrencesOfThisPosition(): Int {
        val boardHashCode = boardHashObject.trackedHashValue
        var occurrences = 0
        var i = numMovesMade - 2
        while (i >= 0 && i >= numMovesMade - halfMoveCount) {
            if (moveHistory[i].hashValue == boardHashCode) {
                occurrences++
            }
            i -= 2
        }
        return occurrences
    }

    val fen: String
        get() {
            val fen = fenBoard
            fen.append(' ')
            fen.append(if (isWhiteToMove) 'w' else 'b')
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
            val bitboard = engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE)
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

    private val fenBoard: StringBuilder
        get() {
            val board = charBoard
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

    private val charBoard: CharArray
        get() {
            val board = CharArray(64){'0'}
            val pieces = charArrayOf('P', 'N', 'B', 'Q', 'K', 'R', 'p', 'n', 'b', 'q', 'k', 'r')
            for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
                val bitsSet = getSetBits(engineBitboards.getPieceBitboard(BitboardType.fromIndex(i)), ArrayList())
                for (bitSet in bitsSet) {
                    board[bitSet] = pieces[i]
                }
            }
            return board
        }

    fun isMoveLegal(moveToVerify: Int): Boolean {
        val moves: List<Int> = generateLegalMoves().toList()
        try {
            for (move in moves) {
                val engineMove = EngineMove(move and 0x00FFFFFF)
                if (makeMove(engineMove)) {
                    unMakeMove()
                    if (engineMove.compact == moveToVerify and 0x00FFFFFF)
                        return true
                }
            }
        } catch (e: InvalidMoveException) {
            return false
        }
        return false
    }

    fun trackedBoardHashCode(): Long {
        return boardHashObject.trackedHashValue
    }

    init {
        setBoard(board)
    }
}