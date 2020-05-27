package com.netsensia.rivalchess.engine.core.board

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant

fun EngineBoard.generateLegalMoves(): List<Int> {
    val moves = mutableListOf<Int>()

    generateKnightMoves(knightBitboardForMover(), moves)
    generateKingMoves(kingSquareForMover(), moves)
    generatePawnMoves(pawnBitboardForMover(),
            if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
            if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture, moves)
    generateSliderMoves(SquareOccupant.WR, SquareOccupant.BR, MagicBitboards.magicMovesRook, MagicBitboards.occupancyMaskRook, MagicBitboards.magicNumberRook, MagicBitboards.magicNumberShiftsRook, moves)
    generateSliderMoves(SquareOccupant.WB, SquareOccupant.BB, MagicBitboards.magicMovesBishop, MagicBitboards.occupancyMaskBishop, MagicBitboards.magicNumberBishop, MagicBitboards.magicNumberShiftsBishop, moves)

    return moves
}

private fun EngineBoard.kingSquareForMover() = if (mover == Colour.WHITE) getWhiteKingSquare() else getBlackKingSquare()

private fun EngineBoard.knightBitboardForMover() =
        if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(BitboardType.WN) else engineBitboards.getPieceBitboard(BitboardType.BN)

private fun EngineBoard.generateKnightMoves(knightBitboard: Long, moves: MutableList<Int>) {
    squareList(knightBitboard).forEach {
        addMoves(it shl 16, knightMoves[it] and
                engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv(), moves) }
}

private fun addMoves(fromSquareMask: Int, bitboard: Long, moves: MutableList<Int>) {
    squareList(bitboard).forEach { moves.add(fromSquareMask or it) }
}

private fun EngineBoard.generateKingMoves(kingSquare: Int, moves: MutableList<Int>) {
    if (mover == Colour.WHITE)
        generateCastleMoves(
                3, 4, Colour.BLACK,
                Pair(CastleBitMask.CASTLEPRIV_WK.value, CastleBitMask.CASTLEPRIV_WQ.value),
                Pair(WHITEKINGSIDECASTLESQUARES, WHITEQUEENSIDECASTLESQUARES), moves
        ) else
        generateCastleMoves(
                59, 60, Colour.WHITE,
                Pair(CastleBitMask.CASTLEPRIV_BK.value, CastleBitMask.CASTLEPRIV_BQ.value),
                Pair(BLACKKINGSIDECASTLESQUARES, BLACKQUEENSIDECASTLESQUARES), moves
        )

    addMoves(kingSquare shl 16, kingMoves[kingSquare] and
            engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv(), moves)
}

private fun EngineBoard.generateCastleMoves(
        kingStartSquare: Int = 3,
        queenStartSquare: Int = 4,
        opponent: Colour = Colour.BLACK,
        privileges: Pair<Int,Int>,
        castleSquares: Pair<Long,Long>,
        moves: MutableList<Int>
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

private fun EngineBoard.generatePawnMoves(
        pawnBitboard: Long,
        bitboardMaskForwardPawnMoves: List<Long>,
        bitboardMaskCapturePawnMoves: List<Long>,
        moves: MutableList<Int>
    ) {

    var bitboardPawnMoves: Long

    squareList(pawnBitboard).forEach {
        bitboardPawnMoves = bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard()
        bitboardPawnMoves = getBitboardPawnJumpMoves(bitboardPawnMoves)
        bitboardPawnMoves = addBitboardPawnCaptureMoves(it, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
        addPossiblePromotionMoves(it shl 16, bitboardPawnMoves, false, moves)
    }
}

private fun EngineBoard.emptySquaresBitboard() = engineBitboards.getPieceBitboard(BitboardType.ALL).inv()

private fun EngineBoard.getBitboardPawnJumpMoves(bitboardPawnMoves: Long) =
        bitboardPawnMoves or (potentialPawnJumpMoves(bitboardPawnMoves) and emptySquaresBitboard())

private fun EngineBoard.potentialPawnJumpMoves(bitboardPawnMoves: Long) =
        if (mover == Colour.WHITE) (bitboardPawnMoves shl 8) and RANK_4 else (bitboardPawnMoves shr 8) and RANK_5

private fun EngineBoard.addBitboardPawnCaptureMoves(bitRef: Int, bitboardMaskCapturePawnMoves: List<Long>, bitboardPawnMoves: Long) =
        if (engineBitboards.getPieceBitboard(BitboardType.ENPASSANTSQUARE) and enPassantCaptureRank() != 0L)
            bitboardPawnMoves or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY) or
                    pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENPASSANTSQUARE)
        else bitboardPawnMoves or
                pawnCaptures(bitboardMaskCapturePawnMoves, bitRef, BitboardType.ENEMY)

private fun EngineBoard.pawnCaptures(bitboardMaskCapturePawnMoves: List<Long>, bitRef: Int, bitboardType: BitboardType) =
        (bitboardMaskCapturePawnMoves[bitRef] and engineBitboards.getPieceBitboard(bitboardType))

private fun addPossiblePromotionMoves(
        fromSquareMoveMask: Int,
        bitboard: Long,
        queenCapturesOnly: Boolean,
        moves: MutableList<Int>) {

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

private fun EngineBoard.enPassantCaptureRank() = if (mover == Colour.WHITE) RANK_6 else RANK_3

private fun EngineBoard.pawnBitboardForMover() =
        if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(BitboardType.WP) else engineBitboards.getPieceBitboard(BitboardType.BP)

private fun EngineBoard.generateSliderMoves(
        whitePiece: SquareOccupant,
        blackPiece: SquareOccupant,
        magicMovesRook: Array<LongArray>,
        occupancyMaskRook: LongArray,
        magicNumberRook: LongArray,
        magicNumberShiftsRook: IntArray,
        moves: MutableList<Int>
    ) {

    val rookBitboard: Long = if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(whitePiece) or
            engineBitboards.getPieceBitboard(BitboardType.WQ)
    else engineBitboards.getPieceBitboard(blackPiece) or
            engineBitboards.getPieceBitboard(BitboardType.BQ)

    squareList(rookBitboard).forEach {
        addMoves(
                it shl 16,
                magicMovesRook[it][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[it]) *
                        magicNumberRook[it] ushr magicNumberShiftsRook[it]).toInt()] and
                        engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv(), moves)
    }
}

fun EngineBoard.generateLegalQuiesceMoves(includeChecks: Boolean): MutableList<Int> {
    val moves: MutableList<Int> = ArrayList()
    val possibleDestinations = engineBitboards.getPieceBitboard(BitboardType.ENEMY)
    val kingSquare: Int = (if (mover == Colour.WHITE) getWhiteKingSquare() else getBlackKingSquare()).toInt()
    val enemyKingSquare:Int = (if (mover == Colour.WHITE) getBlackKingSquare() else getWhiteKingSquare()).toInt()
    moves.addAll(generateQuiesceKnightMoves(includeChecks,
            enemyKingSquare,
            knightBitboardForMover()))
    addMoves(kingSquare shl 16, kingMoves[kingSquare] and possibleDestinations, moves)
    moves.addAll(generateQuiescePawnMoves(includeChecks,
            if (mover == Colour.WHITE) whitePawnMovesForward else blackPawnMovesForward,
            if (mover == Colour.WHITE) whitePawnMovesCapture else blackPawnMovesCapture,
            enemyKingSquare,
            pawnBitboardForMover()))
    moves.addAll(generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.ROOK, SquareOccupant.WR.index, SquareOccupant.BR.index))
    moves.addAll(generateQuiesceSliderMoves(includeChecks, enemyKingSquare, Piece.BISHOP, SquareOccupant.WB.index, SquareOccupant.BB.index))
    return moves
}

private fun EngineBoard.generateQuiesceKnightMoves(includeChecks: Boolean, enemyKingSquare: Int, knightBitboard: Long): List<Int> {
    val moves: MutableList<Int> = ArrayList()
    var possibleDestinations: Long
    squareList(knightBitboard).forEach {
        possibleDestinations = if (includeChecks) {
            engineBitboards.getPieceBitboard(BitboardType.ENEMY) or (knightMoves[enemyKingSquare] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv())
        } else {
            engineBitboards.getPieceBitboard(BitboardType.ENEMY)
        }
        addMoves(it shl 16, knightMoves[it] and possibleDestinations, moves)
    }
    return moves
}

private fun EngineBoard.generateQuiescePawnMoves(includeChecks: Boolean, bitboardMaskForwardPawnMoves: List<Long>, bitboardMaskCapturePawnMoves: List<Long>, enemyKingSquare: Int, pawnBitboard: Long): List<Int> {
    var bitboardPawnMoves: Long
    val moves: MutableList<Int> = ArrayList()
    squareList(pawnBitboard).forEach {
        bitboardPawnMoves = 0
        if (includeChecks) {
            bitboardPawnMoves = getBitboardPawnJumpMoves(
                    bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard())
            bitboardPawnMoves = if (mover == Colour.WHITE) {
                bitboardPawnMoves and blackPawnMovesCapture[enemyKingSquare]
            } else {
                bitboardPawnMoves and whitePawnMovesCapture[enemyKingSquare]
            }
        }

        // promotions
        bitboardPawnMoves = bitboardPawnMoves or (bitboardMaskForwardPawnMoves[it] and emptySquaresBitboard() and (RANK_1 or RANK_8))
        bitboardPawnMoves = addBitboardPawnCaptureMoves(it, bitboardMaskCapturePawnMoves, bitboardPawnMoves)
        addPossiblePromotionMoves(it shl 16, bitboardPawnMoves, true, moves)
    }
    return moves
}

private fun EngineBoard.generateQuiesceSliderMoves(includeChecks: Boolean, enemyKingSquare: Int, piece: Piece, whiteSliderConstant: Int, blackSliderConstant: Int): List<Int> {
    val moves: MutableList<Int> = ArrayList()
    val magicMovesRook = if (piece == Piece.ROOK) MagicBitboards.magicMovesRook else MagicBitboards.magicMovesBishop
    val occupancyMaskRook = if (piece == Piece.ROOK) MagicBitboards.occupancyMaskRook else MagicBitboards.occupancyMaskBishop
    val magicNumberRook = if (piece == Piece.ROOK) MagicBitboards.magicNumberRook else MagicBitboards.magicNumberBishop
    val magicNumberShiftsRook = if (piece == Piece.ROOK) MagicBitboards.magicNumberShiftsRook else MagicBitboards.magicNumberShiftsBishop
    val rookCheckSquares = magicMovesRook[enemyKingSquare][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[enemyKingSquare]) * magicNumberRook[enemyKingSquare] ushr magicNumberShiftsRook[enemyKingSquare]).toInt()]
    var pieceBitboard = if (mover == Colour.WHITE) engineBitboards.getPieceBitboard(
            BitboardType.fromIndex(whiteSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.WQ) else engineBitboards.getPieceBitboard(
            BitboardType.fromIndex(blackSliderConstant)) or engineBitboards.getPieceBitboard(BitboardType.BQ)
    while (pieceBitboard != 0L) {
        val bitRef = java.lang.Long.numberOfTrailingZeros(pieceBitboard)
        pieceBitboard = pieceBitboard xor (1L shl bitRef)
        val pieceMoves = magicMovesRook[bitRef][((engineBitboards.getPieceBitboard(BitboardType.ALL) and occupancyMaskRook[bitRef]) * magicNumberRook[bitRef] ushr magicNumberShiftsRook[bitRef]).toInt()] and engineBitboards.getPieceBitboard(BitboardType.FRIENDLY).inv()
        if (includeChecks) {
            addMoves(bitRef shl 16, pieceMoves and (rookCheckSquares or engineBitboards.getPieceBitboard(BitboardType.ENEMY)), moves)
        } else {
            addMoves(bitRef shl 16, pieceMoves and engineBitboards.getPieceBitboard(BitboardType.ENEMY), moves)
        }
    }
    return moves
}

fun EngineBoard.numLegalMoves() = generateLegalMoves().toList().size

fun EngineBoard.getQuiesceMoveArray(includeChecks: Boolean) =
    generateLegalQuiesceMoves(includeChecks).stream().mapToInt(Int::toInt).toArray() + 0


fun EngineBoard.getMovesAsArray() = generateLegalMoves().toIntArray() + 0
