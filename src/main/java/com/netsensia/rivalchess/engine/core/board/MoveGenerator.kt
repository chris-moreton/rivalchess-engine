package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.config.Limit
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.SquareOccupant

class MoveGenerator(
        private val engineBitboards: EngineBitboards,
        private val mover: Colour,
        private val whiteKingSquare: Int,
        private val blackKingSquare: Int,
        private val castlePrivileges: Int
) {

    lateinit var moves: IntArray
    var moveCount = 0

    fun getNumLegalMoves() = moveCount

    fun getMoveArray() = moves

    fun generateLegalMoves(): MoveGenerator {

        moves = IntArray(Limit.MAX_LEGAL_MOVES.value)
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

    private fun knightBitboardForMover() =
            if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(BitboardType.WN)
            else engineBitboards.getPieceBitboard(BitboardType.BN)

    private fun generateKnightMoves(knightBitboard: Long) {
        squareList(knightBitboard).forEach {
            addMoves(it shl 16, knightMoves[it] and
                    engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()) }
    }

    private fun addMoves(fromSquareMask: Int, bitboard: Long) {
        addMoves(fromSquareMask, squareList(bitboard))
    }

    private fun addMoves(fromSquareMask: Int, squares: List<Int>) {
        squares.forEach {
            moves[moveCount++] = (fromSquareMask or it)
        }
    }

    private fun generateKingMoves(kingSquare: Int) {
        if (mover == Colour.WHITE)
            generateCastleMoves(
                    3, 4, Colour.BLACK,
                    Pair(CastleBitMask.CASTLEPRIV_WK.value, CastleBitMask.CASTLEPRIV_WQ.value),
                    Pair(WHITEKINGSIDECASTLESQUARES, WHITEQUEENSIDECASTLESQUARES)) else
            generateCastleMoves(
                    59, 60, Colour.WHITE,
                    Pair(CastleBitMask.CASTLEPRIV_BK.value, CastleBitMask.CASTLEPRIV_BQ.value),
                    Pair(BLACKKINGSIDECASTLESQUARES, BLACKQUEENSIDECASTLESQUARES))

        addMoves(kingSquare shl 16, kingMoves[kingSquare] and
                engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
    }

    private fun generateCastleMoves(
            kingStartSquare: Int = 3,
            queenStartSquare: Int = 4,
            opponent: Colour = Colour.BLACK,
            privileges: Pair<Int,Int>,
            castleSquares: Pair<Long,Long>
    ) {
        if ((castlePrivileges and privileges.first).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and castleSquares.first == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare - 1, opponent)) {
            moves[moveCount++] = (kingStartSquare shl 16 or kingStartSquare - 2)
        }
        if ((castlePrivileges and privileges.second).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and castleSquares.second == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(queenStartSquare, opponent)) {
            moves[moveCount++] = (kingStartSquare shl 16 or queenStartSquare + 1)
        }
    }

    private fun emptySquaresBitboard() = engineBitboards.getPieceBitboard(BitboardType.ALL).inv()

    private fun generateSliderMoves(
            whitePiece: SquareOccupant,
            blackPiece: SquareOccupant,
            magicVars: MagicVars
    ) {

        val bitboard: Long = if (mover == Colour.WHITE)
            engineBitboards.getPieceBitboard(whitePiece) or engineBitboards.getPieceBitboard(BitboardType.WQ) else
            engineBitboards.getPieceBitboard(blackPiece) or engineBitboards.getPieceBitboard(BitboardType.BQ)

        squareList(bitboard).forEach {
            addMoves(
                    it shl 16,
                    magicVars.moves[it][((engineBitboards.getPieceBitboard(BitboardType.ALL) and magicVars.mask[it]) *
                            magicVars.number[it] ushr magicVars.shift[it]).toInt()] and
                            engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
        }
    }

    fun generateLegalQuiesceMoves(includeChecks: Boolean): MoveGenerator {
        moves = IntArray(Limit.MAX_LEGAL_MOVES.value)
        moveCount = 0
        
        val kingSquare: Int = (if (mover == Colour.WHITE) whiteKingSquare else blackKingSquare).toInt()
        val enemyKingSquare:Int = (if (mover == Colour.WHITE) blackKingSquare else whiteKingSquare).toInt()
        generateQuiesceKnightMoves(includeChecks,
                enemyKingSquare,
                knightBitboardForMover())
        addMoves(kingSquare shl 16, kingMoves[kingSquare] and engineBitboards.getPieceBitboard(BitboardType.ENEMY))
        generateQuiescePawnMoves(includeChecks,
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture,
                enemyKingSquare,
                pawnBitboardForMover())

        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, MagicBitboards.rookVars, SquareOccupant.WR.index, SquareOccupant.BR.index)
        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, MagicBitboards.bishopVars, SquareOccupant.WB.index, SquareOccupant.BB.index)

        moves[moveCount] = 0
        return this
    }

    private fun generateQuiesceKnightMoves(includeChecks: Boolean, enemyKingSquare: Int, knightBitboard: Long) {
        squareList(knightBitboard).forEach {
            addMoves(it shl 16, knightMoves[it] and if (includeChecks)
                engineBitboards.getPieceBitboard(BitboardType.ENEMY) or
                        (knightMoves[enemyKingSquare] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
                    else engineBitboards.getPieceBitboard(BitboardType.ENEMY)
            )
        }
    }

    private fun generateQuiescePawnMoves(includeChecks: Boolean, bitboardMaskForwardPawnMoves: List<Long>, bitboardMaskCapturePawnMoves: List<Long>, enemyKingSquare: Int, pawnBitboard: Long) {
        var bitboardPawnMoves: Long
        squareList(pawnBitboard).forEach {
            bitboardPawnMoves = 0
            if (includeChecks) {
                bitboardPawnMoves = pawnForwardMovesBitboard(
                        bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard())
                bitboardPawnMoves = if (mover == Colour.WHITE) {
                    bitboardPawnMoves and blackPawnMovesCapture[enemyKingSquare]
                } else {
                    bitboardPawnMoves and whitePawnMovesCapture[enemyKingSquare]
                }
            }

            // promotions
            bitboardPawnMoves = bitboardPawnMoves or (bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard() and (RANK_1 or RANK_8))
            bitboardPawnMoves = pawnForwardAndCaptureMovesBitboard(it, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
            addPawnMoves(it shl 16, bitboardPawnMoves, true)
        }
    }

    private fun generateQuiesceSliderMoves(includeChecks: Boolean, enemyKingSquare: Int, magicVars: MagicVars, whiteSliderConstant: Int, blackSliderConstant: Int) {
        val checkSquares = magicVars.moves[enemyKingSquare][((engineBitboards.getPieceBitboard(BitboardType.ALL) and magicVars.mask[enemyKingSquare]) *
                magicVars.number[enemyKingSquare] ushr magicVars.shift[enemyKingSquare]).toInt()]
        val pieceBitboard = if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(
                BitboardType.fromIndex(whiteSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.WQ) else engineBitboards.getPieceBitboard(
                BitboardType.fromIndex(blackSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.BQ)

        squareList(pieceBitboard).forEach {
            val pieceMoves = magicVars.moves[it][((engineBitboards.getPieceBitboard(BitboardType.ALL) and magicVars.mask[it]) *
                    magicVars.number[it] ushr magicVars.shift[it]).toInt()] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()
            if (includeChecks) {
                addMoves(it shl 16, pieceMoves and (checkSquares or engineBitboards.getPieceBitboard(BitboardType.ENEMY)))
            } else {
                addMoves(it shl 16, pieceMoves and engineBitboards.getPieceBitboard(BitboardType.ENEMY))
            }
        }
    }

    private fun addPawnMoves(fromSquareMoveMask: Int, bitboard: Long, queenCapturesOnly: Boolean) {
        squareList(bitboard).forEach {
            if (it >= 56 || it <= 7) {
                moves[moveCount++] = (fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value)
                if (!queenCapturesOnly) {
                    moves[moveCount++] = (fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.value)
                    moves[moveCount++] = (fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.value)
                    moves[moveCount++] = (fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.value)
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

        squareList(pawnBitboard).forEach {
            addPawnMoves(it shl 16, pawnForwardAndCaptureMovesBitboard(
                    it,
                    bitboardMaskCapturePawnMoves,
                    pawnForwardMovesBitboard(bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard())
            ), false)
        }
    }

    fun pawnForwardMovesBitboard(bitboardPawnMoves: Long) =
            bitboardPawnMoves or (potentialPawnJumpMoves(bitboardPawnMoves) and emptySquaresBitboard())

    private fun potentialPawnJumpMoves(bitboardPawnMoves: Long) =
            if (mover == Colour.WHITE) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

    private fun pawnForwardAndCaptureMovesBitboard(bitRef: Int, bitboardMaskCapturePawnMoves: List<Long>, bitboardPawnMoves: Long) =
            if (engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) and enPassantCaptureRank(mover) != 0L)
                bitboardPawnMoves or
                        pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY) or
                        pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENPASSANTSQUARE)
            else bitboardPawnMoves or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY)

    private fun pawnCaptures(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int, bitboardType: BitboardType) =
            (bitboardMaskCapturePawnMoves[bitRef] and engineBitboards.getPieceBitboard(bitboardType))

    private fun pawnBitboardForMover() =
            if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(BitboardType.WP) else engineBitboards.getPieceBitboard(BitboardType.BP)

}