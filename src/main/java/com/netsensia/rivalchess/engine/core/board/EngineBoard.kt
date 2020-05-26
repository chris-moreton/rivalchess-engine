package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.config.Hash
import com.netsensia.rivalchess.engine.core.FEN_START_POS
import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.engine.core.hash.BoardHash
import com.netsensia.rivalchess.engine.core.type.MoveDetail
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.model.*
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import java.lang.Long.bitCount
import java.util.*
import kotlin.collections.ArrayList

class EngineBoard @JvmOverloads constructor(board: Board = getBoardModel(FEN_START_POS)) {
    val squareContents = Array(64){SquareOccupant.NONE}
    val squareOccupants: List<SquareOccupant> get() = squareContents.toList()
    val boardHashObject = BoardHash()

    var moveHistory: MutableList<MoveDetail> = ArrayList()

    var numMovesMade = 0
    var halfMoveCount = 0

    var castlePrivileges = 0
    lateinit var mover: Colour
    var whiteKingSquare: Byte = 0
    var blackKingSquare: Byte = 0

    var isOnNullMove = false

    val lastMoveMade: MoveDetail
        get() = moveHistory[numMovesMade]

    val whitePieceValues: Int
        get() = bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.WN)) * pieceValue(Piece.KNIGHT) +
                bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.WR)) * pieceValue(Piece.ROOK) +
                bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.WB)) * pieceValue(Piece.BISHOP) +
                bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.WQ)) * pieceValue(Piece.QUEEN)

    val blackPieceValues: Int
        get() = bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.BN)) * pieceValue(Piece.KNIGHT) +
                bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.BR)) * pieceValue(Piece.ROOK) +
                bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.BB)) * pieceValue(Piece.BISHOP) +
                bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.BQ)) * pieceValue(Piece.QUEEN)

    val whitePawnValues: Int
        get() = bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.WP)) * pieceValue(Piece.PAWN)

    val blackPawnValues: Int
        get() = bitCount(EngineBitboards.instance.getPieceBitboard(BitboardType.BP)) * pieceValue(Piece.PAWN)

    init {
        setBoard(board)
    }

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

    fun setEngineBoardVars(board: Board) {
        mover = board.sideToMove
        EngineBitboards.instance.reset()
        setSquareContents(board)
        setEnPassantBitboard(board)
        setCastlePrivileges(board)
        calculateSupplementaryBitboards()
    }

    private fun setSquareContents(board: Board) {
        Arrays.fill(squareContents, SquareOccupant.NONE)
        for (y in 0..7) {
            for (x in 0..7) {
                val bitNum = (63 - 8 * y - x).toByte()
                val squareOccupant = board.getSquareOccupant(Square.fromCoords(x, y))
                squareContents[bitNum.toInt()] = squareOccupant
                if (squareOccupant != SquareOccupant.NONE) {
                    EngineBitboards.instance.orPieceBitboard(
                            BitboardType.fromIndex(squareOccupant.index), 1L shl bitNum.toInt())
                    if (squareOccupant == SquareOccupant.WK) {
                        whiteKingSquare = bitNum
                    }
                    if (squareOccupant == SquareOccupant.BK) {
                        blackKingSquare = bitNum
                    }
                }
            }
        }
    }

    private fun setEnPassantBitboard(board: Board) {
        val ep = board.enPassantFile
        if (ep == -1) {
            EngineBitboards.instance.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 0)
        } else {
            if (board.sideToMove == Colour.WHITE) {
                EngineBitboards.instance.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 1L shl 40 + (7 - ep))
            } else {
                EngineBitboards.instance.setPieceBitboard(BitboardType.ENPASSANTSQUARE, 1L shl 16 + (7 - ep))
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
        if (mover == Colour.WHITE) {
            EngineBitboards.instance.setPieceBitboard(BitboardType.FRIENDLY,
                    EngineBitboards.instance.getPieceBitboard(BitboardType.WP) or EngineBitboards.instance.getPieceBitboard(BitboardType.WN) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.WB) or EngineBitboards.instance.getPieceBitboard(BitboardType.WQ) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.WK) or EngineBitboards.instance.getPieceBitboard(BitboardType.WR))
            EngineBitboards.instance.setPieceBitboard(BitboardType.ENEMY,
                    EngineBitboards.instance.getPieceBitboard(BitboardType.BP) or EngineBitboards.instance.getPieceBitboard(BitboardType.BN) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.BB) or EngineBitboards.instance.getPieceBitboard(BitboardType.BQ) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.BK) or EngineBitboards.instance.getPieceBitboard(BitboardType.BR))
        } else {
            EngineBitboards.instance.setPieceBitboard(BitboardType.ENEMY,
                    EngineBitboards.instance.getPieceBitboard(BitboardType.WP) or EngineBitboards.instance.getPieceBitboard(BitboardType.WN) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.WB) or EngineBitboards.instance.getPieceBitboard(BitboardType.WQ) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.WK) or EngineBitboards.instance.getPieceBitboard(BitboardType.WR))
            EngineBitboards.instance.setPieceBitboard(BitboardType.FRIENDLY,
                    EngineBitboards.instance.getPieceBitboard(BitboardType.BP) or EngineBitboards.instance.getPieceBitboard(BitboardType.BN) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.BB) or EngineBitboards.instance.getPieceBitboard(BitboardType.BQ) or
                            EngineBitboards.instance.getPieceBitboard(BitboardType.BK) or EngineBitboards.instance.getPieceBitboard(BitboardType.BR))
        }
        EngineBitboards.instance.setPieceBitboard(BitboardType.ALL, EngineBitboards.instance.getPieceBitboard(BitboardType.FRIENDLY) or EngineBitboards.instance.getPieceBitboard(BitboardType.ENEMY))
    }

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
        if (mover == Colour.BLACK) // white made the last move
        {
            if (toSquare >= 40) return bitCount(whitePassedPawnMask[toSquare] and
                    EngineBitboards.instance.getPieceBitboard(BitboardType.BP)) == 0
        } else {
            if (toSquare <= 23) return bitCount(blackPassedPawnMask[toSquare] and
                    EngineBitboards.instance.getPieceBitboard(BitboardType.WP)) == 0
        }
        return false
    }

    fun switchMover() {
        mover = if (mover == Colour.WHITE) Colour.BLACK else Colour.WHITE
    }

    val allPiecesBitboard: Long
        get() = EngineBitboards.instance.getPieceBitboard(BitboardType.ALL)

    fun getWhiteKingSquare() = whiteKingSquare.toInt()

    fun getBlackKingSquare() = blackKingSquare.toInt()

    @Deprecated("")
    fun getBitboardByIndex(index: Int): Long {
        return EngineBitboards.instance.getPieceBitboard(BitboardType.fromIndex(index))
    }

    fun getBitboard(bitboardType: BitboardType): Long {
        return EngineBitboards.instance.getPieceBitboard(bitboardType)
    }

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

    fun trackedBoardHashCode(): Long {
        return boardHashObject.trackedHashValue
    }

}