package com.netsensia.rivalchess.engine.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.config.MAX_LEGAL_MOVES
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.SquareOccupant

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

    fun generateLegalMoves(): MoveGenerator {

        moveCount = 0

        generateKnightMoves(knightBitboardForMover())
        generateKingMoves(kingSquareForMover())
        generatePawnMoves(pawnBitboardForMover(),
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture)
        generateSliderMoves(
                SquareOccupant.WR,
                SquareOccupant.BR,
                MagicBitboards.rookVars
        )
        generateSliderMoves(
                SquareOccupant.WB,
                SquareOccupant.BB,
                MagicBitboards.bishopVars
        )

        moves[moveCount] = 0

        return this
    }

    private fun kingSquareForMover() = if (mover == Colour.WHITE) whiteKingSquare else blackKingSquare

    private fun knightBitboardForMover() = if (mover == Colour.WHITE) bitboards[BITBOARD_WN] else bitboards[BITBOARD_BN]

    private fun generateKnightMoves(knightBitboard: Long) {
        applyToSquares(knightBitboard) {
            addMoves(it shl 16, knightMoves[it] and bitboards[BITBOARD_FRIENDLY].inv())
        }
    }

    private fun addMoves(fromSquareMask: Int, bitboard: Long) {
        applyToSquares(bitboard) {
            moves[moveCount++] = (fromSquareMask or it)
        }
    }

    private fun generateKingMoves(kingSquare: Int) {
        if (mover == Colour.WHITE)
            generateCastleMoves(
                    3, 4, Colour.BLACK,
                    Pair(CASTLEPRIV_WK, CASTLEPRIV_WQ),
                    Pair(WHITEKINGSIDECASTLESQUARES, WHITEQUEENSIDECASTLESQUARES)) else
            generateCastleMoves(
                    59, 60, Colour.WHITE,
                    Pair(CASTLEPRIV_BK, CASTLEPRIV_BQ),
                    Pair(BLACKKINGSIDECASTLESQUARES, BLACKQUEENSIDECASTLESQUARES))

        addMoves(kingSquare shl 16, kingMoves[kingSquare] and
                bitboards[BITBOARD_FRIENDLY].inv())
    }

    private fun generateCastleMoves(
            kingStartSquare: Int = 3,
            queenStartSquare: Int = 4,
            opponent: Colour = Colour.BLACK,
            privileges: Pair<Int,Int>,
            castleSquares: Pair<Long,Long>
    ) {
        if ((castlePrivileges and privileges.first).toLong() != 0L && bitboards[BITBOARD_ALL] and castleSquares.first == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare - 1, opponent)) {
            moves[moveCount++] = (kingStartSquare shl 16 or kingStartSquare - 2)
        }
        if ((castlePrivileges and privileges.second).toLong() != 0L && bitboards[BITBOARD_ALL] and castleSquares.second == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(queenStartSquare, opponent)) {
            moves[moveCount++] = (kingStartSquare shl 16 or queenStartSquare + 1)
        }
    }

    private fun emptySquaresBitboard() = bitboards[BITBOARD_ALL].inv()

    private fun generateSliderMoves(
            whitePiece: SquareOccupant,
            blackPiece: SquareOccupant,
            magicVars: MagicVars
    ) {

        val bitboard: Long = if (mover == Colour.WHITE)
            bitboards[whitePiece.index] or bitboards[BITBOARD_WQ] else
            bitboards[blackPiece.index] or bitboards[BITBOARD_BQ]

        applyToSquares(bitboard) {
            addMoves(
                    it shl 16,
                    magicVars.moves[it][((bitboards[BITBOARD_ALL] and magicVars.mask[it]) *
                            magicVars.number[it] ushr magicVars.shift[it]).toInt()] and
                            bitboards[BITBOARD_FRIENDLY].inv())
        }
    }

    fun generateLegalQuiesceMoves(generateChecks: Boolean): MoveGenerator {
        moves = IntArray(MAX_LEGAL_MOVES)
        moveCount = 0
        
        val kingSquare: Int = (if (mover == Colour.WHITE) whiteKingSquare else blackKingSquare).toInt()
        val enemyKingSquare:Int = (if (mover == Colour.WHITE) blackKingSquare else whiteKingSquare).toInt()
        generateQuiesceKnightMoves(generateChecks,
                enemyKingSquare,
                knightBitboardForMover()
        )
        addMoves(kingSquare shl 16, kingMoves[kingSquare] and bitboards[BITBOARD_ENEMY])
        generateQuiescePawnMoves(generateChecks,
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture,
                enemyKingSquare,
                pawnBitboardForMover()
        )

        generateQuiesceSliderMoves(generateChecks, enemyKingSquare, MagicBitboards.rookVars, SquareOccupant.WR.index, SquareOccupant.BR.index)
        generateQuiesceSliderMoves(generateChecks, enemyKingSquare, MagicBitboards.bishopVars, SquareOccupant.WB.index, SquareOccupant.BB.index)

        moves[moveCount] = 0
        return this
    }

    private fun generateQuiesceKnightMoves(generateChecks: Boolean, enemyKingSquare: Int, knightBitboard: Long) {
        applyToSquares(knightBitboard) {
            addMoves(it shl 16, knightMoves[it] and if (generateChecks)
                bitboards[BITBOARD_ENEMY] or
                        (knightMoves[enemyKingSquare] and bitboards[BITBOARD_FRIENDLY].inv())
                    else bitboards[BITBOARD_ENEMY]
            )
        }
    }

    private fun generateQuiescePawnMoves(generateChecks: Boolean, bitboardMaskForwardPawnMoves: List<Long>, bitboardMaskCapturePawnMoves: List<Long>, enemyKingSquare: Int, pawnBitboard: Long) {
        var bitboardPawnMoves: Long
        applyToSquares(pawnBitboard) {
            bitboardPawnMoves = 0
            if (generateChecks) {
                bitboardPawnMoves = pawnForwardMovesBitboard(bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard())
                bitboardPawnMoves = bitboardPawnMoves and if (mover == Colour.WHITE) blackPawnMovesCapture[enemyKingSquare] else whitePawnMovesCapture[enemyKingSquare]
            }

            bitboardPawnMoves = bitboardPawnMoves or (bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard() and (RANK_1 or RANK_8))
            bitboardPawnMoves = pawnForwardAndCaptureMovesBitboard(it, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
            addPawnMoves(it shl 16, bitboardPawnMoves, true)
        }
    }

    private fun generateQuiesceSliderMoves(generateChecks: Boolean, enemyKingSquare: Int, magicVars: MagicVars, whiteSliderConstant: Int, blackSliderConstant: Int) {
        val checkSquares = magicVars.moves[enemyKingSquare][((bitboards[BITBOARD_ALL] and magicVars.mask[enemyKingSquare]) *
                magicVars.number[enemyKingSquare] ushr magicVars.shift[enemyKingSquare]).toInt()]
        val pieceBitboard = if (mover == Colour.WHITE) engineBitboards.pieceBitboards[whiteSliderConstant] or
                bitboards[BITBOARD_WQ] else engineBitboards.pieceBitboards[blackSliderConstant] or bitboards[BITBOARD_BQ]

        applyToSquares(pieceBitboard) {
            val pieceMoves = magicVars.moves[it][((bitboards[BITBOARD_ALL] and magicVars.mask[it]) *
                    magicVars.number[it] ushr magicVars.shift[it]).toInt()] and bitboards[BITBOARD_FRIENDLY].inv()
            addMoves(it shl 16, pieceMoves and
                    if (generateChecks) checkSquares or bitboards[BITBOARD_ENEMY]
                    else bitboards[BITBOARD_ENEMY])
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

    private fun enPassantCaptureRank(mover: Colour) = if (mover == Colour.WHITE) RANK_6 else RANK_3

    private fun generatePawnMoves(
            pawnBitboard: Long,
            bitboardMaskForwardPawnMoves: List<Long>,
            bitboardMaskCapturePawnMoves: List<Long>
    ) {
        applyToSquares(pawnBitboard) {
            addPawnMoves(it shl 16, pawnForwardAndCaptureMovesBitboard(
                    it,
                    bitboardMaskCapturePawnMoves,
                    pawnForwardMovesBitboard(bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard())
            ), false)
        }
    }

    private fun pawnForwardMovesBitboard(bitboardPawnMoves: Long) =
            bitboardPawnMoves or (potentialPawnJumpMoves(bitboardPawnMoves) and emptySquaresBitboard())

    private fun potentialPawnJumpMoves(bitboardPawnMoves: Long) =
            if (mover == Colour.WHITE) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

    private fun pawnForwardAndCaptureMovesBitboard(bitRef: Int, bitboardMaskCapturePawnMoves: List<Long>, bitboardPawnMoves: Long) =
            bitboardPawnMoves or if (bitboards[BITBOARD_ENPASSANTSQUARE] and enPassantCaptureRank(mover) != 0L)
                pawnCapturesPlusEnPassantSquare(bitboardMaskCapturePawnMoves, bitRef)
            else
                pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BITBOARD_ENEMY)

    private fun pawnCapturesPlusEnPassantSquare(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int) =
            pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BITBOARD_ENEMY) or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BITBOARD_ENPASSANTSQUARE)

    private fun pawnCaptures(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int, bitboardType: Int) =
            (bitboardMaskCapturePawnMoves[bitRef] and bitboards[bitboardType])

    private fun pawnBitboardForMover() = if (mover == Colour.WHITE) bitboards[BITBOARD_WP] else bitboards[BITBOARD_BP]

}