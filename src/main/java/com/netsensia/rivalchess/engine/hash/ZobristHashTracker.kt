package com.netsensia.rivalchess.engine.hash

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.hash.ZorbristHashCalculator.blackMoverHashValue
import com.netsensia.rivalchess.engine.hash.ZorbristHashCalculator.calculateHash
import com.netsensia.rivalchess.engine.hash.ZorbristHashCalculator.whiteMoverHashValue
import com.netsensia.rivalchess.engine.search.*
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.engine.type.MoveDetail

class ZobristHashTracker {
    @JvmField
    var trackedBoardHashValue: Long = 0

    private val whiteKingSideCastle = ZorbristHashCalculator.pieceHashValues[BITBOARD_WR][0] xor
            ZorbristHashCalculator.pieceHashValues[BITBOARD_WR][2]
    private val whiteQueenSideCastle = ZorbristHashCalculator.pieceHashValues[BITBOARD_WR][7] xor
            ZorbristHashCalculator.pieceHashValues[BITBOARD_WR][4]
    private val blackKingSideCastle = ZorbristHashCalculator.pieceHashValues[BITBOARD_BR][56] xor
            ZorbristHashCalculator.pieceHashValues[BITBOARD_BR][58]
    private val blackQueenSideCastle = ZorbristHashCalculator.pieceHashValues[BITBOARD_BR][63] xor
            ZorbristHashCalculator.pieceHashValues[BITBOARD_BR][60]

    private val switchMoverHashValue = whiteMoverHashValue xor blackMoverHashValue

    fun initHash(engineBoard: EngineBoard) {
        trackedBoardHashValue = calculateHash(engineBoard)
    }

    private fun replaceWithAnotherPiece(movedPiece: Int, capturedPiece: Int, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor
            ZorbristHashCalculator.pieceHashValues[capturedPiece][bitRef] xor
            ZorbristHashCalculator.pieceHashValues[movedPiece][bitRef]
    }

    private fun differentFiles(sq1: Int, sq2: Int) = sq1 % 8 != sq2 % 8

    private fun processPossibleWhitePawnEnPassantCapture(compactMove: Int, capturedPiece: Int) {
        val to = toSquare(compactMove)
        if (differentFiles(fromSquare(compactMove), to) && capturedPiece == BITBOARD_NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[BITBOARD_BP][to - 8]
    }

    private fun processPossibleBlackPawnEnPassantCapture(compactMove: Int, capturedPiece: Int) {
        val to = toSquare(compactMove)
        if (differentFiles(fromSquare(compactMove), to) && capturedPiece == BITBOARD_NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[BITBOARD_WP][to + 8]
    }

    private fun processCapture(movedPiece: Int, capturedPiece: Int, bitRefTo: Int) {
        if (capturedPiece == BITBOARD_NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece][bitRefTo]
        else replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo)
    }

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

    fun nullMove() = switchMover()

    fun makeMove(compactMove: Int, movedPiece: Int, capturedPiece: Int) {
        val bitRefFrom = fromSquare(compactMove)
        val bitRefTo = toSquare(compactMove)
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece][bitRefFrom]
        processCapture(movedPiece, capturedPiece, bitRefTo)
        processSpecialPawnMoves(compactMove, movedPiece, bitRefTo, capturedPiece)
        processCastling(bitRefFrom, movedPiece, bitRefTo)
        switchMover()
    }

    fun unMakeMove(moveDetail: MoveDetail) {
        trackedBoardHashValue = moveDetail.hashValue
    }

}