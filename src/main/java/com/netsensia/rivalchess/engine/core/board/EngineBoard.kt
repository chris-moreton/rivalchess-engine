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
    val engineBitboards = EngineBitboards()
    
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
        get() = bitCount(engineBitboards.getPieceBitboard(BitboardType.WN)) * pieceValue(Piece.KNIGHT) +
                bitCount(engineBitboards.getPieceBitboard(BitboardType.WR)) * pieceValue(Piece.ROOK) +
                bitCount(engineBitboards.getPieceBitboard(BitboardType.WB)) * pieceValue(Piece.BISHOP) +
                bitCount(engineBitboards.getPieceBitboard(BitboardType.WQ)) * pieceValue(Piece.QUEEN)

    val blackPieceValues: Int
        get() = bitCount(engineBitboards.getPieceBitboard(BitboardType.BN)) * pieceValue(Piece.KNIGHT) +
                bitCount(engineBitboards.getPieceBitboard(BitboardType.BR)) * pieceValue(Piece.ROOK) +
                bitCount(engineBitboards.getPieceBitboard(BitboardType.BB)) * pieceValue(Piece.BISHOP) +
                bitCount(engineBitboards.getPieceBitboard(BitboardType.BQ)) * pieceValue(Piece.QUEEN)

    val whitePawnValues: Int
        get() = bitCount(engineBitboards.getPieceBitboard(BitboardType.WP)) * pieceValue(Piece.PAWN)

    val blackPawnValues: Int
        get() = bitCount(engineBitboards.getPieceBitboard(BitboardType.BP)) * pieceValue(Piece.PAWN)

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

    fun getSquareOccupant(bitRef: Int) = squareContents[bitRef]

    fun moveGenerator() =
        MoveGenerator(
                engineBitboards,
                mover,
                whiteKingSquare.toInt(),
                blackKingSquare.toInt(),
                castlePrivileges)

    fun setEngineBoardVars(board: Board) {
        mover = board.sideToMove
        engineBitboards.reset()
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
                    engineBitboards.orPieceBitboard(
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
        castlePrivileges = if (board.isKingSideCastleAvailable(Colour.WHITE)) CastleBitMask.CASTLEPRIV_WK.value else 0
        castlePrivileges = castlePrivileges or if (board.isQueenSideCastleAvailable(Colour.WHITE)) CastleBitMask.CASTLEPRIV_WQ.value else 0
        castlePrivileges = castlePrivileges or if (board.isKingSideCastleAvailable(Colour.BLACK)) CastleBitMask.CASTLEPRIV_BK.value else 0
        castlePrivileges = castlePrivileges or if (board.isQueenSideCastleAvailable(Colour.BLACK)) CastleBitMask.CASTLEPRIV_BQ.value else 0
    }

    fun calculateSupplementaryBitboards() {
        if (mover == Colour.WHITE) {
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
                    engineBitboards.getPieceBitboard(BitboardType.BP)) == 0
        } else {
            if (toSquare <= 23) return bitCount(blackPassedPawnMask[toSquare] and
                    engineBitboards.getPieceBitboard(BitboardType.WP)) == 0
        }
        return false
    }

    fun switchMover() {
        mover = if (mover == Colour.WHITE) Colour.BLACK else Colour.WHITE
    }

    val allPiecesBitboard: Long
        get() = engineBitboards.getPieceBitboard(BitboardType.ALL)

    fun getWhiteKingSquare() = whiteKingSquare.toInt()

    fun getBlackKingSquare() = blackKingSquare.toInt()

    fun getBitboardType(index: Int): Long {
        return engineBitboards.getPieceBitboard(BitboardType.fromIndex(index))
    }

    fun getBitboard(bitboardType: BitboardType): Long {
        return engineBitboards.getPieceBitboard(bitboardType)
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