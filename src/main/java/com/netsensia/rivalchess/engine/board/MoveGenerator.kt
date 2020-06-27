package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.config.MAX_LEGAL_MOVES
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.model.Colour

class MoveGenerator(
        private val engineBitboards: EngineBitboards,
        private val mover: Colour,
        private val whiteKingSquare: Int,
        private val blackKingSquare: Int,
        private val castlePrivileges: Int
) {

    var moves = IntArray(MAX_LEGAL_MOVES)
    val bitboards = engineBitboards.pieceBitboards
    var moveCount = 0
    private val allSquaresExceptFriendlyPieces = bitboards[BITBOARD_FRIENDLY].inv()
    private val knightBitboardForMover = if (mover == Colour.WHITE) bitboards[BITBOARD_WN] else bitboards[BITBOARD_BN]

    fun generateLegalMoves(): MoveGenerator {
        moveCount = 0

        generateKnightMoves()
        generateKingMoves()
        generatePawnMoves(
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture
        )
        generateSliderMoves(BITBOARD_WR, BITBOARD_BR, MagicBitboards.rookVars)
        generateSliderMoves(BITBOARD_WB, BITBOARD_BB, MagicBitboards.bishopVars)

        moves[moveCount] = 0

        return this
    }

    private fun generateKnightMoves() {
        applyToSquares(knightBitboardForMover) { from ->
            val fromShifted = from shl 16
            applyToSquares(knightMoves[from] and allSquaresExceptFriendlyPieces) { to ->
                moves[moveCount++] = (fromShifted or to)
            }
        }
    }

    private fun addMoves(fromSquareMask: Int, bitboard: Long) {
        applyToSquares(bitboard) {
            moves[moveCount++] = (fromSquareMask or it)
        }
    }

    private fun generateKingMoves() {
        if (mover == Colour.WHITE)
            generateCastleMoves(
                    3, 4, Colour.BLACK,
                    CASTLEPRIV_WK, CASTLEPRIV_WQ,
                    WHITEKINGSIDECASTLESQUARES, WHITEQUEENSIDECASTLESQUARES) else
            generateCastleMoves(
                    59, 60, Colour.WHITE,
                    CASTLEPRIV_BK, CASTLEPRIV_BQ,
                    BLACKKINGSIDECASTLESQUARES, BLACKQUEENSIDECASTLESQUARES)

        val kingSquare = if (mover == Colour.WHITE) whiteKingSquare else blackKingSquare
        addMoves(kingSquare shl 16, kingMoves[kingSquare] and allSquaresExceptFriendlyPieces)
    }

    private fun generateCastleMoves(
            kingStartSquare: Int = 3,
            queenStartSquare: Int = 4,
            opponent: Colour = Colour.BLACK,
            kingPrivileges: Int,
            queenPrivileges: Int,
            kingCastleSquares: Long,
            queenCastleSquares: Long
    ) {
        if ((castlePrivileges and kingPrivileges).toLong() != 0L && bitboards[BITBOARD_ALL] and kingCastleSquares == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare - 1, opponent)) {
            moves[moveCount++] = (kingStartSquare shl 16 or kingStartSquare - 2)
        }
        if ((castlePrivileges and queenPrivileges).toLong() != 0L && bitboards[BITBOARD_ALL] and queenCastleSquares == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(queenStartSquare, opponent)) {
            moves[moveCount++] = (kingStartSquare shl 16 or queenStartSquare + 1)
        }
    }

    private fun generateSliderMoves(whitePiece: Int, blackPiece: Int, magicVars: MagicVars) {
        val bitboard: Long = if (mover == Colour.WHITE)
            bitboards[whitePiece] or bitboards[BITBOARD_WQ] else
            bitboards[blackPiece] or bitboards[BITBOARD_BQ]

        applyToSquares(bitboard) { from ->
            val toSquares = magicVars.moves[from][((bitboards[BITBOARD_ALL] and magicVars.mask[from]) *
                    magicVars.number[from] ushr magicVars.shift[from]).toInt()] and
                    allSquaresExceptFriendlyPieces
            val fromShifted = from shl 16
            applyToSquares(toSquares) { to ->
                moves[moveCount++] = (fromShifted or to)
            }
        }
    }

    fun generateLegalQuiesceMoves(generateChecks: Boolean): MoveGenerator {
        moves = IntArray(MAX_LEGAL_MOVES)
        moveCount = 0
        
        val kingSquare: Int = (if (mover == Colour.WHITE) whiteKingSquare else blackKingSquare).toInt()
        val enemyKingSquare:Int = (if (mover == Colour.WHITE) blackKingSquare else whiteKingSquare).toInt()

        generateQuiesceKnightMoves(generateChecks, enemyKingSquare, knightBitboardForMover)
        addMoves(kingSquare shl 16, kingMoves[kingSquare] and bitboards[BITBOARD_ENEMY])
        generateQuiescePawnMoves(generateChecks,
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture,
                enemyKingSquare,
                if (mover == Colour.WHITE) bitboards[BITBOARD_WP] else bitboards[BITBOARD_BP]
        )

        generateQuiesceSliderMoves(generateChecks, enemyKingSquare, MagicBitboards.rookVars, BITBOARD_WR, BITBOARD_BR)
        generateQuiesceSliderMoves(generateChecks, enemyKingSquare, MagicBitboards.bishopVars, BITBOARD_WB, BITBOARD_BB)

        moves[moveCount] = 0
        return this
    }

    private fun generateQuiesceKnightMoves(generateChecks: Boolean, enemyKingSquare: Int, knightBitboard: Long) {
        val potentialToSquares = if (generateChecks)
            bitboards[BITBOARD_ENEMY] or (knightMoves[enemyKingSquare] and allSquaresExceptFriendlyPieces)
            else bitboards[BITBOARD_ENEMY]

        applyToSquares(knightBitboard) { from ->
            val fromShifted = from shl 16
            applyToSquares(knightMoves[from] and potentialToSquares) { to ->
                moves[moveCount++] = (fromShifted or to)
            }
        }
    }

    private fun generateQuiescePawnMoves(generateChecks: Boolean,
                                         bitboardMaskForwardPawnMoves: LongArray,
                                         bitboardMaskCapturePawnMoves: LongArray,
                                         enemyKingSquare: Int,
                                         pawnBitboard: Long) {
        val pawnMovesCapture = if (mover == Colour.WHITE) blackPawnMovesCapture[enemyKingSquare] else whitePawnMovesCapture[enemyKingSquare]
        val emptySquaresBitboard = bitboards[BITBOARD_ALL].inv()
        val promotionRank = RANK_1 or RANK_8
        var bitboardPawnMoves: Long

        applyToSquares(pawnBitboard) {
            bitboardPawnMoves = 0
            if (generateChecks) bitboardPawnMoves =
                        pawnForwardMovesBitboard(bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard) and pawnMovesCapture

            bitboardPawnMoves = bitboardPawnMoves or (bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard and promotionRank)
            bitboardPawnMoves = pawnForwardAndCaptureMovesBitboard(it, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
            addPawnMoves(it shl 16, bitboardPawnMoves, true)
        }
    }

    private fun generateQuiesceSliderMoves(generateChecks: Boolean, enemyKingSquare: Int, magicVars: MagicVars, whiteSliderConstant: Int, blackSliderConstant: Int) {
        val checkSquares = magicVars.moves[enemyKingSquare][((bitboards[BITBOARD_ALL] and magicVars.mask[enemyKingSquare]) *
                magicVars.number[enemyKingSquare] ushr magicVars.shift[enemyKingSquare]).toInt()]
        val pieceBitboard = if (mover == Colour.WHITE) engineBitboards.pieceBitboards[whiteSliderConstant] or bitboards[BITBOARD_WQ]
                                  else engineBitboards.pieceBitboards[blackSliderConstant] or bitboards[BITBOARD_BQ]

        val enemyBitboard = if (generateChecks) checkSquares or bitboards[BITBOARD_ENEMY] else bitboards[BITBOARD_ENEMY]

        applyToSquares(pieceBitboard) {
            val pieceMoves = magicVars.moves[it][((bitboards[BITBOARD_ALL] and magicVars.mask[it]) *
                    magicVars.number[it] ushr magicVars.shift[it]).toInt()] and allSquaresExceptFriendlyPieces
            addMoves(it shl 16, pieceMoves and enemyBitboard)
        }
    }

    private fun addPawnMoves(fromSquareMoveMask: Int, bitboard: Long, queenCapturesOnly: Boolean) {
        applyToSquares(bitboard) {
            if (it >= 56 || it <= 7) {
                moves[moveCount++] = (fromSquareMoveMask or it or PROMOTION_PIECE_TOSQUARE_MASK_QUEEN)
                if (!queenCapturesOnly) {
                    moves[moveCount++] = (fromSquareMoveMask or it or PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT)
                    moves[moveCount++] = (fromSquareMoveMask or it or PROMOTION_PIECE_TOSQUARE_MASK_ROOK)
                    moves[moveCount++] = (fromSquareMoveMask or it or PROMOTION_PIECE_TOSQUARE_MASK_BISHOP)
                }
            } else {
                moves[moveCount++] = (fromSquareMoveMask or it)
            }
        }
    }

    private fun generatePawnMoves(
            bitboardMaskForwardPawnMoves: LongArray,
            bitboardMaskCapturePawnMoves: LongArray
    ) {
        val emptySquaresBitboard = bitboards[BITBOARD_ALL].inv()

        applyToSquares(if (mover == Colour.WHITE) bitboards[BITBOARD_WP] else bitboards[BITBOARD_BP]) {
            addPawnMoves(it shl 16, pawnForwardAndCaptureMovesBitboard(
                    it,
                    bitboardMaskCapturePawnMoves,
                    pawnForwardMovesBitboard(bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard)
            ), false)
        }
    }

    private fun pawnForwardMovesBitboard(bitboardPawnMoves: Long) =
            bitboardPawnMoves or (potentialPawnJumpMoves(bitboardPawnMoves) and bitboards[BITBOARD_ALL].inv())

    private fun potentialPawnJumpMoves(bitboardPawnMoves: Long) =
            if (mover == Colour.WHITE) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

    private fun enPassantCaptureRank(mover: Colour) = if (mover == Colour.WHITE) RANK_6 else RANK_3

    private fun pawnForwardAndCaptureMovesBitboard(bitRef: Int, bitboardMaskCapturePawnMoves: LongArray, bitboardPawnMoves: Long) =
            bitboardPawnMoves or if (bitboards[BITBOARD_ENPASSANTSQUARE] and enPassantCaptureRank(mover) != 0L)
                pawnCapturesPlusEnPassantSquare(bitboardMaskCapturePawnMoves, bitRef) else
                pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BITBOARD_ENEMY)

    private fun pawnCapturesPlusEnPassantSquare(bitboardMaskCapturePawnMoves: LongArray, bitRef: Int) =
            pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BITBOARD_ENEMY) or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BITBOARD_ENPASSANTSQUARE)

    private fun pawnCaptures(bitboardMaskCapturePawnMoves: LongArray, bitRef: Int, bitboardType: Int) =
            (bitboardMaskCapturePawnMoves[bitRef] and bitboards[bitboardType])

}