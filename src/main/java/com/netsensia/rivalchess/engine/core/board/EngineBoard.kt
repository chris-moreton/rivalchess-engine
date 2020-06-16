package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.config.DEFAULT_HASHTABLE_SIZE_MB
import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.eval.*
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
    val boardHashObject = BoardHash()

    var moveHistory: MutableList<MoveDetail> = ArrayList()

    var numMovesMade = 0
    var halfMoveCount = 0

    var castlePrivileges = 0
    lateinit var mover: Colour
    var whiteKingSquare = 0
    var blackKingSquare = 0

    var isOnNullMove = false

    val lastMoveMade: MoveDetail
        get() = moveHistory[numMovesMade]

    val whitePieceValues: Int
        get() = bitCount(engineBitboards.pieceBitboards[BITBOARD_WN]) * VALUE_KNIGHT +
                bitCount(engineBitboards.pieceBitboards[BITBOARD_WR]) * VALUE_ROOK +
                bitCount(engineBitboards.pieceBitboards[BITBOARD_WB]) * VALUE_BISHOP +
                bitCount(engineBitboards.pieceBitboards[BITBOARD_WQ]) * VALUE_QUEEN

    val blackPieceValues: Int
        get() = bitCount(engineBitboards.pieceBitboards[BITBOARD_BN]) * VALUE_KNIGHT +
                bitCount(engineBitboards.pieceBitboards[BITBOARD_BR]) * VALUE_ROOK +
                bitCount(engineBitboards.pieceBitboards[BITBOARD_BB]) * VALUE_BISHOP +
                bitCount(engineBitboards.pieceBitboards[BITBOARD_BQ]) * VALUE_QUEEN

    val whitePawnValues: Int
        get() = bitCount(engineBitboards.pieceBitboards[BITBOARD_WP]) * VALUE_PAWN

    val blackPawnValues: Int
        get() = bitCount(engineBitboards.pieceBitboards[BITBOARD_BP]) * VALUE_PAWN

    init {
        setBoard(board)
    }

    fun setBoard(board: Board) {
        numMovesMade = 0
        moveHistory.clear()
        halfMoveCount = board.halfMoveCount
        setEngineBoardVars(board)
        boardHashObject.hashTableVersion = 0
        boardHashObject.setHashSizeMB(DEFAULT_HASHTABLE_SIZE_MB)
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
                val bitNum = (63 - 8 * y - x)
                val squareOccupant = board.getSquareOccupant(Square.fromCoords(x, y))
                squareContents[bitNum.toInt()] = squareOccupant
                if (squareOccupant != SquareOccupant.NONE) {
                    engineBitboards.orPieceBitboard(squareOccupant.index, 1L shl bitNum.toInt())
                    if (squareOccupant == SquareOccupant.WK) whiteKingSquare = bitNum
                    if (squareOccupant == SquareOccupant.BK) blackKingSquare = bitNum
                }
            }
        }
    }

    private fun setEnPassantBitboard(board: Board) {
        val ep = board.enPassantFile
        if (ep == -1) {
            engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0)
        } else {
            if (board.sideToMove == Colour.WHITE) {
                engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 1L shl 40 + (7 - ep))
            } else {
                engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 1L shl 16 + (7 - ep))
            }
        }
    }

    private fun setCastlePrivileges(board: Board) {
        castlePrivileges = (if (board.isKingSideCastleAvailable(Colour.WHITE)) CastleBitMask.CASTLEPRIV_WK.value else 0) or
                (if (board.isQueenSideCastleAvailable(Colour.WHITE)) CastleBitMask.CASTLEPRIV_WQ.value else 0) or
                (if (board.isKingSideCastleAvailable(Colour.BLACK)) CastleBitMask.CASTLEPRIV_BK.value else 0) or
                                if (board.isQueenSideCastleAvailable(Colour.BLACK)) CastleBitMask.CASTLEPRIV_BQ.value else 0
    }

    fun calculateSupplementaryBitboards() {
        val white = engineBitboards.pieceBitboards[BITBOARD_WP] or engineBitboards.pieceBitboards[BITBOARD_WN] or
                engineBitboards.pieceBitboards[BITBOARD_WB] or engineBitboards.pieceBitboards[BITBOARD_WQ] or
                engineBitboards.pieceBitboards[BITBOARD_WK] or engineBitboards.pieceBitboards[BITBOARD_WR]

        val black = engineBitboards.pieceBitboards[BITBOARD_BP] or engineBitboards.pieceBitboards[BITBOARD_BN] or
                engineBitboards.pieceBitboards[BITBOARD_BB] or engineBitboards.pieceBitboards[BITBOARD_BQ] or
                engineBitboards.pieceBitboards[BITBOARD_BK] or engineBitboards.pieceBitboards[BITBOARD_BR]

        if (mover == Colour.WHITE) {
            engineBitboards.setPieceBitboard(BITBOARD_FRIENDLY, white)
            engineBitboards.setPieceBitboard(BITBOARD_ENEMY, black)
        } else {
            engineBitboards.setPieceBitboard(BITBOARD_FRIENDLY, black)
            engineBitboards.setPieceBitboard(BITBOARD_ENEMY, white)
        }
        engineBitboards.setPieceBitboard(BITBOARD_ALL, white or black)
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
                    engineBitboards.pieceBitboards[BITBOARD_BP]) == 0
        } else {
            if (toSquare <= 23) return bitCount(blackPassedPawnMask[toSquare] and
                    engineBitboards.pieceBitboards[BITBOARD_WP]) == 0
        }
        return false
    }

    val allPiecesBitboard: Long
        get() = engineBitboards.pieceBitboards[BITBOARD_ALL]

    fun getBitboard(bitboardType: Int) = engineBitboards.pieceBitboards[bitboardType]

    fun previousOccurrencesOfThisPosition(): Int {
        val boardHashCode = boardHashObject.trackedHashValue
        var occurrences = 0
        var i = numMovesMade - 2
        while (i >= 0 && i >= numMovesMade - halfMoveCount) {
            if (moveHistory[i].hashValue == boardHashCode) occurrences++
            i -= 2
        }
        return occurrences
    }

    fun trackedBoardHashCode(): Long {
        return boardHashObject.trackedHashValue
    }

    override fun toString(): String {
        return this.getFen()
    }

}