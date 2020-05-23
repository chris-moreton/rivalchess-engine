package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.getSetBits
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.bitboards.util.squareListSequence
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
    val engineBitboards = EngineBitboards.getInstance()
    val boardHashObject = BoardHash()
    var castlePrivileges = 0
        private set
    var isWhiteToMove = false
        private set
    private var whiteKingSquare: Byte = 0
    private var blackKingSquare: Byte = 0
    private var numMovesMade = 0

    val squareContents = Array(64){SquareOccupant.NONE}

    private var isOnNullMove = false
    private var moveList: MutableList<MoveDetail> = ArrayList()
    var halfMoveCount = 0
        private set

    fun setBoard(board: Board) {
        numMovesMade = 0
        moveList.clear()
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

    val numLegalMoves: Int
        get() = generateLegalMoves().size

    val moveArray: IntArray
        get() {
            val legalMoves = generateLegalMoves()
            var intArray = legalMoves.stream().mapToInt(Int::toInt).toArray()
            intArray += 0
            return intArray
        }

    fun getQuiesceMoveArray(includeChecks: Boolean): IntArray {
        val legalMoves = generateLegalQuiesceMoves(includeChecks)
        legalMoves.add(0)
        return legalMoves.stream().mapToInt(Int::toInt).toArray()
    }

    fun isGameOver(): Boolean {
            val legalMoves: List<Int> = generateLegalMoves()
            return legalMoves.stream()
                    .filter { m: Int -> isMoveLegal(m) }
                    .count() == 0L
        }

    private fun addPossiblePromotionMoves(fromSquareMoveMask: Int, bitboard: Long, queenCapturesOnly: Boolean): List<Int> {
        val moves: MutableList<Int> = ArrayList()

        val squares = squareListSequence(bitboard)

        for (toSquare in squares) {
            if (toSquare >= 56 || toSquare <= 7) {
                moves.add(fromSquareMoveMask or toSquare or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value)
                if (!queenCapturesOnly) {
                    moves.add(fromSquareMoveMask or toSquare or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.value)
                    moves.add(fromSquareMoveMask or toSquare or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.value)
                    moves.add(fromSquareMoveMask or toSquare or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.value)
                }
            } else {
                moves.add(fromSquareMoveMask or toSquare)
            }
        }
        return moves
    }

    private fun addMoves(fromSquareMask: Int, bitboard: Long): List<Int> {
        val moves: MutableList<Int> = ArrayList()
        val squares = squareListSequence(bitboard)
        for (toSquare in squares) {
            moves.add(fromSquareMask or toSquare)
        }
        return moves
    }

    fun generateLegalMoves() : List<Int> =
        generateKnightMoves(if (isWhiteToMove) engineBitboards.getPieceBitboard(BitboardType.WN) else engineBitboards.getPieceBitboard(BitboardType.BN)) +
        generateKingMoves(if (isWhiteToMove) whiteKingSquare.toInt() else blackKingSquare.toInt()) +
        generatePawnMoves(if (isWhiteToMove) engineBitboards.getPieceBitboard(BitboardType.WP) else engineBitboards.getPieceBitboard(BitboardType.BP),
                if (isWhiteToMove) whitePawnMovesForward else blackPawnMovesForward,
                if (isWhiteToMove) whitePawnMovesCapture else blackPawnMovesCapture) +
        generateSliderMoves(SquareOccupant.WR.index, SquareOccupant.BR.index, MagicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook) +
        generateSliderMoves(SquareOccupant.WB.index, SquareOccupant.BB.index, MagicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop)

    private fun generateSliderMoves(whitePieceConstant: Int, blackPieceConstant: Int, magicMovesRook: Array<LongArray>, occupancyMaskRook: LongArray, magicNumberRook: LongArray, magicNumberShiftsRook: IntArray): List<Int> {
        val rookBitboard: Long
        val moves: MutableList<Int> = ArrayList()
        rookBitboard = if (isWhiteToMove) engineBitboards.getPieceBitboard(BitboardType.fromIndex(whitePieceConstant)) or
                engineBitboards.getPieceBitboard(BitboardType.WQ) else engineBitboards.getPieceBitboard(BitboardType.fromIndex(blackPieceConstant)) or
                engineBitboards.getPieceBitboard(BitboardType.BQ)

        val squares = squareListSequence(rookBitboard)
        for (bitRef in squares) {
            moves.addAll(addMoves(
                    bitRef shl 16,
                    magicMovesRook[bitRef][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[bitRef]) * magicNumberRook[bitRef] ushr magicNumberShiftsRook[bitRef]).toInt()] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()))
        }
        return moves
    }

    private fun generatePawnMoves(pawnBitboard: Long, bitboardMaskForwardPawnMoves: List<Long>, bitboardMaskCapturePawnMoves: List<Long>): List<Int> {
        var bitboardPawnMoves: Long
        val moves: MutableList<Int> = ArrayList()

        val squares = squareListSequence(pawnBitboard)
        for (bitRef in squares) {
            bitboardPawnMoves = bitboardMaskForwardPawnMoves[bitRef] and emptySquaresBitboard()
            bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves)
            bitboardPawnMoves = addBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
            moves.addAll(addPossiblePromotionMoves(bitRef shl 16, bitboardPawnMoves, false))
        }
        return moves
    }

    private fun generateKingMoves(kingSquare: Int): List<Int> {
        val moves: MutableList<Int> = ArrayList()
        if (isWhiteToMove) {
            moves.addAll(generateWhiteKingMoves())
        } else {
            moves.addAll(generateBlackKingMoves())
        }
        moves.addAll(addMoves(kingSquare shl 16, kingMoves[kingSquare] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()))
        return moves
    }

    private fun generateWhiteKingMoves(): List<Int> {
        val moves: MutableList<Int> = ArrayList()
        val whiteKingStartSquare = 3
        val whiteQueenStartSquare = 4
        val opponent = Colour.BLACK
        if ((castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and WHITEKINGSIDECASTLESQUARES == 0L &&
                !engineBitboards.isSquareAttackedBy(whiteKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(whiteKingStartSquare - 1, opponent)) {
            moves.add(whiteKingStartSquare shl 16 or whiteKingStartSquare - 2)
        }
        if ((castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and WHITEQUEENSIDECASTLESQUARES == 0L &&
                !engineBitboards.isSquareAttackedBy(whiteKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(whiteQueenStartSquare, opponent)) {
            moves.add(whiteKingStartSquare shl 16 or whiteQueenStartSquare + 1)
        }
        return moves
    }

    private fun generateBlackKingMoves(): List<Int> {
        val moves: MutableList<Int> = ArrayList()
        val blackKingStartSquare = 59
        val opponent = Colour.WHITE
        val blackQueenStartSquare = 60
        if ((castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and BLACKKINGSIDECASTLESQUARES == 0L &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare - 1, opponent)) {
            moves.add(blackKingStartSquare shl 16 or blackKingStartSquare - 2)
        }
        if ((castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and BLACKQUEENSIDECASTLESQUARES == 0L &&
                !engineBitboards.isSquareAttackedBy(blackKingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(blackQueenStartSquare, opponent)) {
            moves.add(blackKingStartSquare shl 16 or blackQueenStartSquare + 1)
        }
        return moves
    }

    private fun generateKnightMoves(knightBitboard: Long): List<Int> {
        val moves: MutableList<Int> = ArrayList()

        for (bitRef in squareListSequence(knightBitboard)) {
            moves.addAll(addMoves(bitRef shl 16, knightMoves[bitRef] and
                            engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()))
        }
        return moves
    }

    private fun enPassantCaptureRank() = if (isWhiteToMove) RANK_6 else RANK_3

    private fun addBitboardPawnCaptureMoves(bitRef: Int, bitboardMaskCapturePawnMoves: List<Long>, bitboardPawnMoves: Long) =
        if (engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) and enPassantCaptureRank() != 0L)
            bitboardPawnMoves or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY) or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENPASSANTSQUARE)
        else bitboardPawnMoves or
                pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY)

    private fun pawnCaptures(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int, bitboardType: BitboardType) =
            (bitboardMaskCapturePawnMoves[bitRef] and engineBitboards.getPieceBitboard(bitboardType))

    private fun getBitboardPawnJumpMoves(bitboardPawnMoves: Long) =
        bitboardPawnMoves or (potentialPawnJumpMoves(bitboardPawnMoves) and emptySquaresBitboard())

    private fun potentialPawnJumpMoves(bitboardPawnMoves: Long) =
        if (isWhiteToMove) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

    fun generateLegalQuiesceMoves(includeChecks: Boolean): MutableList<Int> {
        val moves: MutableList<Int> = ArrayList()
        val possibleDestinations = engineBitboards.getPieceBitboard(BitboardType.ENEMY)
        val kingSquare: Int = (if (isWhiteToMove) whiteKingSquare else blackKingSquare).toInt()
        val enemyKingSquare:Int = (if (isWhiteToMove) blackKingSquare else whiteKingSquare).toInt()
        moves.addAll(generateQuiesceKnightMoves(includeChecks,
                enemyKingSquare,
                if (isWhiteToMove) engineBitboards.getPieceBitboard(BitboardType.WN) else engineBitboards.getPieceBitboard(BitboardType.BN)))
        moves.addAll(addMoves(kingSquare shl 16, kingMoves[kingSquare] and possibleDestinations))
        moves.addAll(generateQuiescePawnMoves(includeChecks,
                if (isWhiteToMove) whitePawnMovesForward else blackPawnMovesForward,
                if (isWhiteToMove) whitePawnMovesCapture else blackPawnMovesCapture,
                enemyKingSquare,
                if (isWhiteToMove) engineBitboards.getPieceBitboard(BitboardType.WP) else engineBitboards.getPieceBitboard(BitboardType.BP)))
        moves.addAll(generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.ROOK, SquareOccupant.WR.index, SquareOccupant.BR.index))
        moves.addAll(generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.BISHOP, SquareOccupant.WB.index, SquareOccupant.BB.index))
        return moves
    }

    private fun generateQuiesceSliderMoves(includeChecks: Boolean, enemyKingSquare: Int, piece: Piece, whiteSliderConstant: Int, blackSliderConstant: Int): List<Int> {
        val moves: MutableList<Int> = ArrayList()
        val magicMovesRook = if (piece == Piece.ROOK) MagicBitboards.magicMovesRook else MagicBitboards.magicMovesBishop
        val occupancyMaskRook = if (piece == Piece.ROOK) MagicBitboards.occupancyMaskRook else MagicBitboards.occupancyMaskBishop
        val magicNumberRook = if (piece == Piece.ROOK) MagicBitboards.magicNumberRook else MagicBitboards.magicNumberBishop
        val magicNumberShiftsRook = if (piece == Piece.ROOK) MagicBitboards.magicNumberShiftsRook else MagicBitboards.magicNumberShiftsBishop
        val rookCheckSquares = magicMovesRook[enemyKingSquare][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare] ushr magicNumberShiftsRook[enemyKingSquare]).toInt()]
        var pieceBitboard = if (isWhiteToMove) engineBitboards.getPieceBitboard(
                BitboardType.fromIndex(whiteSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.WQ) else engineBitboards.getPieceBitboard(
                BitboardType.fromIndex(blackSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.BQ)
        while (pieceBitboard != 0L) {
            val bitRef = java.lang.Long.numberOfTrailingZeros(pieceBitboard)
            pieceBitboard = pieceBitboard xor (1L shl bitRef)
            val pieceMoves = magicMovesRook[bitRef][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[bitRef]) * magicNumberRook[bitRef] ushr magicNumberShiftsRook[bitRef]).toInt()] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()
            if (includeChecks) {
                moves.addAll(addMoves(bitRef shl 16, pieceMoves and (rookCheckSquares or engineBitboards.getPieceBitboard(BitboardType.ENEMY))))
            } else {
                moves.addAll(addMoves(bitRef shl 16, pieceMoves and engineBitboards.getPieceBitboard(BitboardType.ENEMY)))
            }
        }
        return moves
    }

    private fun generateQuiescePawnMoves(includeChecks: Boolean, bitboardMaskForwardPawnMoves: List<Long>, bitboardMaskCapturePawnMoves: List<Long>, enemyKingSquare: Int, pawnBitboard: Long): List<Int> {
        var bitboardPawnMoves: Long
        val moves: MutableList<Int> = ArrayList()
        val squares = squareListSequence(pawnBitboard)
        for (bitRef in squares) {
            bitboardPawnMoves = 0
            if (includeChecks) {
                bitboardPawnMoves = getBitboardPawnJumpMoves(
                        bitboardMaskForwardPawnMoves[bitRef] and emptySquaresBitboard())
                bitboardPawnMoves = if (isWhiteToMove) {
                    bitboardPawnMoves and blackPawnMovesCapture[enemyKingSquare]
                } else {
                    bitboardPawnMoves and whitePawnMovesCapture[enemyKingSquare]
                }
            }

            // promotions
            bitboardPawnMoves = bitboardPawnMoves or (bitboardMaskForwardPawnMoves[bitRef] and emptySquaresBitboard() and (RANK_1 or RANK_8))
            bitboardPawnMoves = addBitboardPawnCaptureMoves(bitRef, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
            moves.addAll(addPossiblePromotionMoves(bitRef shl 16, bitboardPawnMoves, true))
        }
        return moves
    }

    private fun emptySquaresBitboard() = engineBitboards.getPieceBitboard(BitboardType.ALL).inv()

    private fun generateQuiesceKnightMoves(includeChecks: Boolean, enemyKingSquare: Int, knightBitboard: Long): List<Int> {
        val moves: MutableList<Int> = ArrayList()
        var possibleDestinations: Long
        val squares = squareListSequence(knightBitboard)
        for (bitRef in squares) {
            possibleDestinations = if (includeChecks) {
                engineBitboards.getPieceBitboard(BitboardType.ENEMY) or (knightMoves[enemyKingSquare] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
            } else {
                engineBitboards.getPieceBitboard(BitboardType.ENEMY)
            }
            moves.addAll(addMoves(bitRef shl 16, knightMoves[bitRef] and possibleDestinations))
        }
        return moves
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

    fun makeNullMove() {
        boardHashObject.makeNullMove()
        isWhiteToMove = !isWhiteToMove
        val t = engineBitboards.getPieceBitboard(BitboardType.FRIENDLY)
        engineBitboards.setPieceBitboard(BitboardType.FRIENDLY, engineBitboards.getPieceBitboard(BitboardType.ENEMY))
        engineBitboards.setPieceBitboard(BitboardType.ENEMY, t)
        isOnNullMove = true
    }

    fun unMakeNullMove() {
        makeNullMove()
        isOnNullMove = false
    }

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

        if (moveList.size <= numMovesMade) {
            moveList.add(moveDetail)
        } else {
            moveList[numMovesMade] = moveDetail
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
        moveList[numMovesMade].capturePiece = capturePiece
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
        } else if (toMask == moveList[numMovesMade].enPassantBitboard) {
            engineBitboards.xorPieceBitboard(BitboardType.WP, toMask shl 8)
            moveList[numMovesMade].capturePiece = SquareOccupant.WP
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
        moveList[numMovesMade].capturePiece = capturePiece!!
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
        } else if (toMask == moveList[numMovesMade].enPassantBitboard) {
            engineBitboards.xorPieceBitboard(SquareOccupant.BP.index, toMask ushr 8)
            moveList[numMovesMade].capturePiece = SquareOccupant.BP
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
        get() = moveList[numMovesMade]

    @Throws(InvalidMoveException::class)
    fun unMakeMove() {
        numMovesMade--
        halfMoveCount = moveList[numMovesMade].halfMoveCount.toInt()
        isWhiteToMove = !isWhiteToMove
        engineBitboards.setPieceBitboard(BitboardType.ENPASSANTSQUARE, moveList[numMovesMade].enPassantBitboard)
        castlePrivileges = moveList[numMovesMade].castlePrivileges.toInt()
        isOnNullMove = moveList[numMovesMade].isOnNullMove
        val fromSquare = moveList[numMovesMade].move ushr 16 and 63
        val toSquare = moveList[numMovesMade].move and 63
        val fromMask = 1L shl fromSquare
        val toMask = 1L shl toSquare
        boardHashObject.unMove(this)
        squareContents[fromSquare] = moveList[numMovesMade].movePiece
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

    private fun replaceMovedPiece(fromSquare: Int, fromMask: Long, toMask: Long): SquareOccupant {
        val movePiece = moveList[numMovesMade].movePiece
        engineBitboards.xorPieceBitboard(movePiece.index, toMask or fromMask)
        if (movePiece == SquareOccupant.WK) {
            whiteKingSquare = fromSquare.toByte()
        } else if (movePiece == SquareOccupant.BK) {
            blackKingSquare = fromSquare.toByte()
        }
        return movePiece
    }

    private fun replaceCastledRook(fromMask: Long, toMask: Long, movePiece: SquareOccupant) {
        if (movePiece == SquareOccupant.WK) {
            if (toMask or fromMask == WHITEKINGSIDECASTLEMOVEMASK) {
                engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEKINGSIDECASTLEROOKMOVE)
                squareContents[Square.H1.bitRef] = SquareOccupant.WR
                squareContents[Square.F1.bitRef] = SquareOccupant.NONE
            } else if (toMask or fromMask == WHITEQUEENSIDECASTLEMOVEMASK) {
                engineBitboards.xorPieceBitboard(BitboardType.WR, WHITEQUEENSIDECASTLEROOKMOVE)
                squareContents[Square.A1.bitRef] = SquareOccupant.WR
                squareContents[Square.D1.bitRef] = SquareOccupant.NONE
            }
        } else if (movePiece == SquareOccupant.BK) {
            if (toMask or fromMask == BLACKKINGSIDECASTLEMOVEMASK) {
                engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKKINGSIDECASTLEROOKMOVE)
                squareContents[Square.H8.bitRef] = SquareOccupant.BR
                squareContents[Square.F8.bitRef] = SquareOccupant.NONE
            } else if (toMask or fromMask == BLACKQUEENSIDECASTLEMOVEMASK) {
                engineBitboards.xorPieceBitboard(BitboardType.BR, BLACKQUEENSIDECASTLEROOKMOVE)
                squareContents[Square.A8.bitRef] = SquareOccupant.BR
                squareContents[Square.D8.bitRef] = SquareOccupant.NONE
            }
        }
    }

    @Throws(InvalidMoveException::class)
    private fun removePromotionPiece(fromMask: Long, toMask: Long): Boolean {
        val promotionPiece = moveList[numMovesMade].move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value
        if (promotionPiece != 0) {
            if (isWhiteToMove) {
                engineBitboards.xorPieceBitboard(BitboardType.WP, fromMask)
                when (fromValue(promotionPiece)) {
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> engineBitboards.xorPieceBitboard(BitboardType.WQ, toMask)
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> engineBitboards.xorPieceBitboard(BitboardType.WB, toMask)
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> engineBitboards.xorPieceBitboard(BitboardType.WN, toMask)
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> engineBitboards.xorPieceBitboard(BitboardType.WR, toMask)
                    else -> throw InvalidMoveException("Illegal promotion piece $promotionPiece")
                }
            } else {
                engineBitboards.xorPieceBitboard(BitboardType.BP, fromMask)
                when (fromValue(promotionPiece)) {
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN -> engineBitboards.xorPieceBitboard(BitboardType.BQ, toMask)
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP -> engineBitboards.xorPieceBitboard(BitboardType.BB, toMask)
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT -> engineBitboards.xorPieceBitboard(BitboardType.BN, toMask)
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK -> engineBitboards.xorPieceBitboard(BitboardType.BR, toMask)
                    else -> throw InvalidMoveException("Invalid promotionPiece $promotionPiece")
                }
            }
            return true
        }
        return false
    }

    val whitePieceValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WN)) * pieceValue(Piece.KNIGHT) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WR)) * pieceValue(Piece.ROOK) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WB)) * pieceValue(Piece.BISHOP) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WQ)) * pieceValue(Piece.QUEEN)

    val blackPieceValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BN)) * pieceValue(Piece.KNIGHT) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BR)) * pieceValue(Piece.ROOK) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BB)) * pieceValue(Piece.BISHOP) + java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BQ)) * pieceValue(Piece.QUEEN)

    val whitePawnValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.WP)) * pieceValue(Piece.PAWN)

    val blackPawnValues: Int
        get() = java.lang.Long.bitCount(engineBitboards.getPieceBitboard(BitboardType.BP)) * pieceValue(Piece.PAWN)

    private fun replaceCapturedPiece(toSquare: Int, toMask: Long) {
        val capturePiece = moveList[numMovesMade].capturePiece
        if (capturePiece != SquareOccupant.NONE) {
            squareContents[toSquare] = capturePiece
            engineBitboards.xorPieceBitboard(capturePiece.index, toMask)
        }
    }

    private fun unMakeEnPassants(toSquare: Int, fromMask: Long, toMask: Long): Boolean {
        if (toMask == moveList[numMovesMade].enPassantBitboard) {
            if (moveList[numMovesMade].movePiece == SquareOccupant.WP) {
                engineBitboards.xorPieceBitboard(BitboardType.WP, toMask or fromMask)
                engineBitboards.xorPieceBitboard(BitboardType.BP, toMask ushr 8)
                squareContents[toSquare - 8] = SquareOccupant.BP
                return true
            } else if (moveList[numMovesMade].movePiece == SquareOccupant.BP) {
                engineBitboards.xorPieceBitboard(BitboardType.BP, toMask or fromMask)
                engineBitboards.xorPieceBitboard(BitboardType.WP, toMask shl 8)
                squareContents[toSquare + 8] = SquareOccupant.WP
                return true
            }
        }
        return false
    }

    fun lastCapturePiece(): SquareOccupant {
        return moveList[numMovesMade - 1].capturePiece
    }

    fun wasCapture(): Boolean {
        return moveList[numMovesMade - 1].capturePiece == SquareOccupant.NONE
    }

    fun wasPawnPush(): Boolean {
        val toSquare = moveList[numMovesMade - 1].move and 63
        val movePiece = moveList[numMovesMade - 1].movePiece
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
            if (moveList[i].hashValue == boardHashCode) {
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
        val moves: List<Int> = generateLegalMoves()
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