package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.EngineBitboards
import com.netsensia.rivalchess.bitboards.util.popCount
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.eval.pieceValue
import com.netsensia.rivalchess.engine.hash.BoardHash
import com.netsensia.rivalchess.engine.type.MoveDetail
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import java.lang.Long
import kotlin.system.exitProcess

class EngineBoard @JvmOverloads constructor(board: Board = getBoardModel(FEN_START_POS)) {
    @JvmField
    val engineBitboards = EngineBitboards()

    @JvmField
    val boardHashObject = BoardHash()

    @JvmField
    var moveHistory = arrayOfNulls<MoveDetail>(MAX_HALFMOVES_IN_GAME)

    @JvmField
    var numMovesMade = 0

    @JvmField
    var halfMoveCount = 0

    @JvmField
    var castlePrivileges = 0

    @JvmField
    var isOnNullMove = false

    @JvmField
    var mover = Colour.WHITE

    val whitePieceValues: Int
        get() = popCount(getBitboard(BITBOARD_WN)) * pieceValue(BITBOARD_WN) +
                popCount(getBitboard(BITBOARD_WB)) * pieceValue(BITBOARD_WB) +
                popCount(getBitboard(BITBOARD_WR)) * pieceValue(BITBOARD_WR) +
                popCount(getBitboard(BITBOARD_WQ)) * pieceValue(BITBOARD_WQ)

    val blackPieceValues: Int
        get() = popCount(getBitboard(BITBOARD_BN)) * pieceValue(BITBOARD_WN) +
                popCount(getBitboard(BITBOARD_BB)) * pieceValue(BITBOARD_WB) +
                popCount(getBitboard(BITBOARD_BR)) * pieceValue(BITBOARD_WR) +
                popCount(getBitboard(BITBOARD_BQ)) * pieceValue(BITBOARD_WQ)

    val lastMoveMade: MoveDetail?
        get() = moveHistory[numMovesMade]

    fun getWhiteKingSquareCalculated(): Int {
        val retVal = Long.numberOfTrailingZeros(getBitboard(BITBOARD_WK))
        if (retVal != whiteKingSquareTracked) {
            println(this)
            exitProcess(1)
        }
        return retVal
    }

    fun getBlackKingSquareCalculated(): Int {
        val retVal = Long.numberOfTrailingZeros(getBitboard(BITBOARD_BK))
        if (retVal != blackKingSquareTracked) {
            println(this)
            exitProcess(1)
        }
        return retVal
    }

    @JvmField
    var whiteKingSquareTracked = 0

    @JvmField
    var blackKingSquareTracked = 0

    init {
        setBoard(board)
    }

    fun setBoard(board: Board) {
        numMovesMade = 0
        halfMoveCount = board.halfMoveCount
        setEngineBoardVars(board)
        boardHashObject.hashTableVersion = 0
        boardHashObject.setHashSizeMB(DEFAULT_HASHTABLE_SIZE_MB)
        boardHashObject.initialiseHashCode(this)
    }

    fun getBitboardTypeOfPieceOnSquare(bitRef: Int, colour: Colour): Int {
        for (type in if (colour == Colour.WHITE) whiteBitboardTypes else blackBitboardTypes)
            if (engineBitboards.pieceBitboards[type] and (1L shl bitRef) != 0L) return type
        return BITBOARD_NONE
    }

    fun getBitboardTypeOfPieceOnSquare(bitRef: Int): Int {
        val bitMask = 1L shl bitRef
        for (type in allBitboardTypes)
            if (engineBitboards.pieceBitboards[type] and bitMask != 0L) return type
        return BITBOARD_NONE
    }

    fun moveGenerator() = MoveGenerator(engineBitboards, mover, getWhiteKingSquareCalculated(), getBlackKingSquareCalculated(), castlePrivileges)

    private fun setEngineBoardVars(board: Board) {
        mover = board.sideToMove
        engineBitboards.reset()
        setSquareContents(board)
        setEnPassantBitboard(board)
        setCastlePrivileges(board)
        calculateSupplementaryBitboards()
    }

    private fun setSquareContents(board: Board) {
        for (y in 0..7) {
            for (x in 0..7) {
                val bitNum = (63 - 8 * y - x)
                val squareOccupant = board.getSquareOccupant(Square.fromCoords(x, y))
                if (squareOccupant != SquareOccupant.NONE) {
                    engineBitboards.orPieceBitboard(squareOccupant.index, 1L shl bitNum)
                    if (squareOccupant == SquareOccupant.WK) whiteKingSquareTracked = bitNum
                    if (squareOccupant == SquareOccupant.BK) blackKingSquareTracked = bitNum
                }
            }
        }
    }

    private fun setEnPassantBitboard(board: Board) {
        val ep = board.enPassantFile
        if (ep == -1) engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 0) else
            if (board.sideToMove == Colour.WHITE)
                engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 1L shl 40 + (7 - ep))
            else
                engineBitboards.setPieceBitboard(BITBOARD_ENPASSANTSQUARE, 1L shl 16 + (7 - ep))
    }

    private fun setCastlePrivileges(board: Board) {
        castlePrivileges = (if (board.isKingSideCastleAvailable(Colour.WHITE)) CASTLEPRIV_WK else 0) or
                (if (board.isQueenSideCastleAvailable(Colour.WHITE)) CASTLEPRIV_WQ else 0) or
                (if (board.isKingSideCastleAvailable(Colour.BLACK)) CASTLEPRIV_BK else 0) or
                (if (board.isQueenSideCastleAvailable(Colour.BLACK)) CASTLEPRIV_BQ else 0)
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

    fun getBitboard(bitboardType: Int) = engineBitboards.pieceBitboards[bitboardType]

    fun previousOccurrencesOfThisPosition(): Int {
        val boardHashCode = boardHashObject.trackedHashValue
        var occurrences = 0
        var i = numMovesMade - 2
        while (i >= 0 && i >= numMovesMade - halfMoveCount) {
            if (moveHistory[i]!!.hashValue == boardHashCode) occurrences++
            i -= 2
        }
        return occurrences
    }

    fun boardHashCode() = boardHashObject.trackedHashValue

    override fun toString() = this.getFen()
}
