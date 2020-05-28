package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant
import java.lang.Long.numberOfTrailingZeros

class MoveGenerator(
        private val engineBitboards: EngineBitboards,
        private val mover: Colour,
        private val whiteKingSquare: Int,
        private val blackKingSquare: Int,
        private val castlePrivileges: Int
) {

    var moves = mutableListOf<Int>()

    fun getNumLegalMoves() = moves.toList().size

    fun getMoveArray() = moves.toIntArray() + 0

    fun generateLegalMoves(): MoveGenerator {

        moves.clear()

        generateKnightMoves(knightBitboardForMover())
        generateKingMoves(kingSquareForMover())
        generatePawnMoves(pawnBitboardForMover(),
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture)
        generateSliderMoves(
                SquareOccupant.WR,
                SquareOccupant.BR,
                MagicBitboards.magicMovesRook,
                MagicBitboards.occupancyMaskRook,
                MagicBitboards.magicNumberRook,
                MagicBitboards.magicNumberShiftsRook
        )
        generateSliderMoves(
                SquareOccupant.WB,
                SquareOccupant.BB,
                MagicBitboards.magicMovesBishop,
                MagicBitboards.occupancyMaskBishop,
                MagicBitboards.magicNumberBishop,
                MagicBitboards.magicNumberShiftsBishop
        )

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
        var bitboardCopy = bitboard
        while (bitboardCopy != 0L) {
            val square = numberOfTrailingZeros(bitboardCopy)
            moves.add(fromSquareMask or square)
            bitboardCopy = bitboardCopy xor (1L shl square)
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
            moves.add(kingStartSquare shl 16 or kingStartSquare - 2)
        }
        if ((castlePrivileges and privileges.second).toLong() != 0L && engineBitboards.getPieceBitboard(BitboardType.ALL) and castleSquares.second == 0L &&
                !engineBitboards.isSquareAttackedBy(kingStartSquare, opponent) &&
                !engineBitboards.isSquareAttackedBy(queenStartSquare, opponent)) {
            moves.add(kingStartSquare shl 16 or queenStartSquare + 1)
        }
    }

    private fun emptySquaresBitboard() = engineBitboards.getPieceBitboard(BitboardType.ALL).inv()

    private fun generateSliderMoves(
            whitePiece: SquareOccupant,
            blackPiece: SquareOccupant,
            magicMovesArray: Array<LongArray>,
            occupancyMask: LongArray,
            magicNumber: LongArray,
            magicNumberShifts: IntArray
    ) {

        val bitboard: Long = if (mover == Colour.WHITE)
            engineBitboards.getPieceBitboard(whitePiece) or engineBitboards.getPieceBitboard(BitboardType.WQ) else
            engineBitboards.getPieceBitboard(blackPiece) or engineBitboards.getPieceBitboard(BitboardType.BQ)

        squareList(bitboard).forEach {
            addMoves(
                    it shl 16,
                    magicMovesArray[it][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMask[it]) *
                            magicNumber[it] ushr magicNumberShifts[it]).toInt()] and
                            engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
        }
    }

    fun generateLegalQuiesceMoves(includeChecks: Boolean): MoveGenerator {
        moves.clear()
        val possibleDestinations = engineBitboards.getPieceBitboard(BitboardType.ENEMY)
        val kingSquare: Int = (if (mover == Colour.WHITE) whiteKingSquare else blackKingSquare).toInt()
        val enemyKingSquare:Int = (if (mover == Colour.WHITE) blackKingSquare else whiteKingSquare).toInt()
        generateQuiesceKnightMoves(includeChecks,
                enemyKingSquare,
                knightBitboardForMover())
        addMoves(kingSquare shl 16, kingMoves[kingSquare] and possibleDestinations)
        generateQuiescePawnMoves(includeChecks,
                if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
                if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture,
                enemyKingSquare,
                pawnBitboardForMover())
        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.ROOK, SquareOccupant.WR.index, SquareOccupant.BR.index)
        generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.BISHOP, SquareOccupant.WB.index, SquareOccupant.BB.index)

        return this
    }

    private fun generateQuiesceKnightMoves(includeChecks: Boolean, enemyKingSquare: Int, knightBitboard: Long) {
        var possibleDestinations: Long
        squareList(knightBitboard).forEach {
            possibleDestinations = if (includeChecks) {
                engineBitboards.getPieceBitboard(BitboardType.ENEMY) or (knightMoves[enemyKingSquare] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
            } else {
                engineBitboards.getPieceBitboard(BitboardType.ENEMY)
            }
            addMoves(it shl 16, knightMoves[it] and possibleDestinations)
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

    private fun generateQuiesceSliderMoves(includeChecks: Boolean, enemyKingSquare: Int, piece: Piece, whiteSliderConstant: Int, blackSliderConstant: Int) {
        val magicMovesRook = if (piece == Piece.ROOK) MagicBitboards.magicMovesRook else MagicBitboards.magicMovesBishop
        val occupancyMaskRook = if (piece == Piece.ROOK) MagicBitboards.occupancyMaskRook else MagicBitboards.occupancyMaskBishop
        val magicNumberRook = if (piece == Piece.ROOK) MagicBitboards.magicNumberRook else MagicBitboards.magicNumberBishop
        val magicNumberShiftsRook = if (piece == Piece.ROOK) MagicBitboards.magicNumberShiftsRook else MagicBitboards.magicNumberShiftsBishop
        val rookCheckSquares = magicMovesRook[enemyKingSquare][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare] ushr magicNumberShiftsRook[enemyKingSquare]).toInt()]
        var pieceBitboard = if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(
                BitboardType.fromIndex(whiteSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.WQ) else engineBitboards.getPieceBitboard(
                BitboardType.fromIndex(blackSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.BQ)
        while (pieceBitboard != 0L) {
            val bitRef = numberOfTrailingZeros(pieceBitboard)
            pieceBitboard = pieceBitboard xor (1L shl bitRef)
            val pieceMoves = magicMovesRook[bitRef][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[bitRef]) * magicNumberRook[bitRef] ushr magicNumberShiftsRook[bitRef]).toInt()] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()
            if (includeChecks) {
                addMoves(bitRef shl 16, pieceMoves and (rookCheckSquares or engineBitboards.getPieceBitboard(BitboardType.ENEMY)))
            } else {
                addMoves(bitRef shl 16, pieceMoves and engineBitboards.getPieceBitboard(BitboardType.ENEMY))
            }
        }
    }

    private fun addPawnMoves(fromSquareMoveMask: Int, bitboard: Long, queenCapturesOnly: Boolean) {
        squareList(bitboard).forEach {
            if (it >= 56 || it <= 7) {
                moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value)
                if (!queenCapturesOnly) {
                    moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT.value)
                    moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_ROOK.value)
                    moves.add(fromSquareMoveMask or it or PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP.value)
                }
            } else {
                moves.add(fromSquareMoveMask or it)
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

    fun potentialPawnJumpMoves(bitboardPawnMoves: Long) =
            if (mover == Colour.WHITE) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

    fun pawnForwardAndCaptureMovesBitboard(bitRef: Int, bitboardMaskCapturePawnMoves: List<Long>, bitboardPawnMoves: Long) =
            if (engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) and enPassantCaptureRank(mover) != 0L)
                bitboardPawnMoves or
                        pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY) or
                        pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENPASSANTSQUARE)
            else bitboardPawnMoves or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY)

    fun pawnCaptures(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int, bitboardType: BitboardType) =
            (bitboardMaskCapturePawnMoves[bitRef] and engineBitboards.getPieceBitboard(bitboardType))

    fun pawnBitboardForMover() =
            if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(BitboardType.WP) else engineBitboards.getPieceBitboard(BitboardType.BP)

}