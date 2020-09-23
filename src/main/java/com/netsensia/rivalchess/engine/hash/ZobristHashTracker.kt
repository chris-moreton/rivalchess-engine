package com.netsensia.rivalchess.engine.hash

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.eval.yCoordOfSquare
import com.netsensia.rivalchess.engine.hash.ZobristHashCalculator.blackMoverHashValue
import com.netsensia.rivalchess.engine.hash.ZobristHashCalculator.calculateHash
import com.netsensia.rivalchess.engine.hash.ZobristHashCalculator.whiteMoverHashValue
import com.netsensia.rivalchess.engine.search.*
import com.netsensia.rivalchess.engine.type.MoveDetail

@kotlin.ExperimentalUnsignedTypes
class ZobristHashTracker {
    @JvmField
    var trackedBoardHashValue: Long = 0

    private val whiteKingSideCastle = ZobristHashCalculator.pieceHashValues[BITBOARD_WR][0] xor
            ZobristHashCalculator.pieceHashValues[BITBOARD_WR][2]
    private val whiteQueenSideCastle = ZobristHashCalculator.pieceHashValues[BITBOARD_WR][7] xor
            ZobristHashCalculator.pieceHashValues[BITBOARD_WR][4]
    private val blackKingSideCastle = ZobristHashCalculator.pieceHashValues[BITBOARD_BR][56] xor
            ZobristHashCalculator.pieceHashValues[BITBOARD_BR][58]
    private val blackQueenSideCastle = ZobristHashCalculator.pieceHashValues[BITBOARD_BR][63] xor
            ZobristHashCalculator.pieceHashValues[BITBOARD_BR][60]

    private val switchMoverHashValue = whiteMoverHashValue xor blackMoverHashValue

    @kotlin.ExperimentalUnsignedTypes
    fun initHash(engineBoard: EngineBoard) {
        trackedBoardHashValue = calculateHash(engineBoard)
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun replaceWithAnotherPiece(movedPiece: Int, capturedPiece: Int, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor
            ZobristHashCalculator.pieceHashValues[capturedPiece][bitRef] xor
            ZobristHashCalculator.pieceHashValues[movedPiece][bitRef]
    }

    private fun differentFiles(sq1: Int, sq2: Int) = sq1 % 8 != sq2 % 8

    private fun processPossibleWhitePawnEnPassantCapture(compactMove: Int, capturedPiece: Int) {
        val to = toSquare(compactMove)
        if (differentFiles(fromSquare(compactMove), to) && capturedPiece == BITBOARD_NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZobristHashCalculator.pieceHashValues[BITBOARD_BP][to - 8]
    }

    private fun processPossibleBlackPawnEnPassantCapture(compactMove: Int, capturedPiece: Int) {
        val to = toSquare(compactMove)
        if (differentFiles(fromSquare(compactMove), to) && capturedPiece == BITBOARD_NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZobristHashCalculator.pieceHashValues[BITBOARD_WP][to + 8]
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun processCapture(movedPiece: Int, capturedPiece: Int, bitRefTo: Int) {
        if (capturedPiece == BITBOARD_NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZobristHashCalculator.pieceHashValues[movedPiece][bitRefTo]
        else replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo)
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun switchMover() {
        trackedBoardHashValue = trackedBoardHashValue xor switchMoverHashValue
    }

    private fun processCastling(bitRefFrom: Int, movedPiece: Int, bitRefTo: Int) {
        if (movedPiece == BITBOARD_WK && bitRefFrom == 3) {
            if (bitRefTo == 1) trackedBoardHashValue = trackedBoardHashValue xor whiteKingSideCastle
            else if (bitRefTo == 5) trackedBoardHashValue = trackedBoardHashValue xor whiteQueenSideCastle
        }
        if (movedPiece == BITBOARD_BK && bitRefFrom == 59) {
            if (bitRefTo == 57) trackedBoardHashValue = trackedBoardHashValue xor blackKingSideCastle
            else if (bitRefTo == 61) trackedBoardHashValue = trackedBoardHashValue xor blackQueenSideCastle
        }
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun processSpecialPawnMoves(compactMove: Int, movedPiece: Int, bitRefTo: Int, capturedPiece: Int) {
        if (movedPiece == BITBOARD_WP) {
            processPossibleWhitePawnEnPassantCapture(compactMove, capturedPiece)
            val promotionPiece = promotionPiece(compactMove)
            if (promotionPiece != BITBOARD_NONE) replaceWithAnotherPiece(promotionPiece, BITBOARD_WP, bitRefTo)
        }
        if (movedPiece == BITBOARD_BP) {
            processPossibleBlackPawnEnPassantCapture(compactMove, capturedPiece)
            val promotionPiece = promotionPiece(compactMove)
            if (promotionPiece != BITBOARD_NONE) replaceWithAnotherPiece(promotionPiece, BITBOARD_BP, bitRefTo)
        }
    }

    @kotlin.ExperimentalUnsignedTypes
    fun nullMove() = switchMover()

    fun makeMove(compactMove: Int, movedPiece: Int, capturedPiece: Int) {
        val bitRefFrom = fromSquare(compactMove)
        val bitRefTo = toSquare(compactMove)
        trackedBoardHashValue = trackedBoardHashValue xor ZobristHashCalculator.pieceHashValues[movedPiece][bitRefFrom]
        processCapture(movedPiece, capturedPiece, bitRefTo)
        processSpecialPawnMoves(compactMove, movedPiece, bitRefTo, capturedPiece)
        processCastling(bitRefFrom, movedPiece, bitRefTo)
        switchMover()
    }

    fun unMakeMove(moveDetail: MoveDetail) {
        trackedBoardHashValue = moveDetail.hashValue
    }

}