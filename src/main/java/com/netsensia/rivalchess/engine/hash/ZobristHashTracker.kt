package com.netsensia.rivalchess.engine.hash

import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.hash.ZorbristHashCalculator.blackMoverHashValue
import com.netsensia.rivalchess.engine.hash.ZorbristHashCalculator.calculateHash
import com.netsensia.rivalchess.engine.hash.ZorbristHashCalculator.whiteMoverHashValue
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.engine.type.MoveDetail
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.util.getBitRefFromBoardRef
import com.netsensia.rivalchess.util.getMoveRefFromEngineMove

class ZobristHashTracker {
    @JvmField
    var trackedBoardHashValue: Long = 0

    private val whiteKingSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][0] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][2]
    private val whiteQueenSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][7] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][4]
    private val blackKingSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][56] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][58]
    private val blackQueenSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][63] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][60]

    private val switchMoverHashValue = whiteMoverHashValue xor blackMoverHashValue

    fun initHash(engineBoard: EngineBoard) {
        trackedBoardHashValue = calculateHash(engineBoard)
    }

    private fun replaceWithAnotherPiece(movedPiece: Int, capturedPiece: Int, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor
                ZorbristHashCalculator.pieceHashValues[capturedPiece][bitRef] xor
                ZorbristHashCalculator.pieceHashValues[movedPiece][bitRef]
    }

    private fun processPossibleWhitePawnEnPassantCapture(move: Move, capturedPiece: Int) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == BITBOARD_NONE) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank + 1)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.BP.index][capturedPawnBitRef]
        }
    }

    private fun processPossibleBlackPawnEnPassantCapture(move: Move, capturedPiece: Int) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == BITBOARD_NONE) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank - 1)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.WP.index][capturedPawnBitRef]
        }
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

    private fun processSpecialPawnMoves(move: Move, movedPiece: Int, bitRefTo: Int, capturedPiece: Int) {
        if (movedPiece == BITBOARD_WP) {
            processPossibleWhitePawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece
            if (promotionPiece != SquareOccupant.NONE) replaceWithAnotherPiece(promotionPiece.index, BITBOARD_WP, bitRefTo)
        }
        if (movedPiece == BITBOARD_BP) {
            processPossibleBlackPawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece.index, BITBOARD_BP, bitRefTo)
            }
        }
    }

    private fun unMakeEnPassant(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (1L shl bitRefTo == moveDetail.enPassantBitboard) {
            if (moveDetail.movePiece == BITBOARD_WP) {
                trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.BP.index][bitRefTo - 8]
                return true
            } else if (moveDetail.movePiece == BITBOARD_BP) {
                trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.WP.index][bitRefTo + 8]
                return true
            }
        }
        return false
    }

    private fun unMakeCapture(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (moveDetail.capturePiece != BITBOARD_NONE) {
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[moveDetail.capturePiece][bitRefTo]
            return true
        }
        return false
    }

    private fun unMakeWhiteCastle(bitRefTo: Int): Boolean {
        return when (bitRefTo) {
            1 -> {
                trackedBoardHashValue = trackedBoardHashValue xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][2] xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][0]
                true
            }
            5 -> {
                trackedBoardHashValue = trackedBoardHashValue xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][4] xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][7]
                true
            }
            else -> false
        }
    }

    private fun unMakeBlackCastle(bitRefTo: Int): Boolean {
        return when (bitRefTo) {
            61 -> {
                trackedBoardHashValue = trackedBoardHashValue xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][60] xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][63]
                true
            }
            57 -> {
                trackedBoardHashValue = trackedBoardHashValue xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][58] xor
                        ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][56]
                true
            }
            else -> false
        }
    }

    private fun unMakePromotion(bitRefFrom: Int, bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        val move = getMoveRefFromEngineMove(moveDetail.move)
        val movedPiece = moveDetail.movePiece
        val promotedPiece = move.promotedPiece
        if (promotedPiece != SquareOccupant.NONE) {
            trackedBoardHashValue = trackedBoardHashValue xor
                    ZorbristHashCalculator.pieceHashValues[movedPiece][bitRefFrom] xor
                    ZorbristHashCalculator.pieceHashValues[promotedPiece.index][bitRefTo]
            unMakeCapture(bitRefTo, moveDetail)
            return true
        }
        return false
    }

    fun nullMove() = switchMover()

    fun makeMove(engineMove: EngineMove, movedPiece: Int, capturedPiece: Int) {
        val move = getMoveRefFromEngineMove(engineMove.compact)
        val bitRefFrom = getBitRefFromBoardRef(move.srcBoardRef)
        val bitRefTo = getBitRefFromBoardRef(move.tgtBoardRef)
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece][bitRefFrom]
        processCapture(movedPiece, capturedPiece, bitRefTo)
        processSpecialPawnMoves(move, movedPiece, bitRefTo, capturedPiece)
        processCastling(bitRefFrom, movedPiece, bitRefTo)
        switchMover()
    }

    fun unMakeMove(moveDetail: MoveDetail) {
        val move = getMoveRefFromEngineMove(moveDetail.move)
        val bitRefFrom = getBitRefFromBoardRef(move.srcBoardRef)
        val bitRefTo = getBitRefFromBoardRef(move.tgtBoardRef)
        if (!unMakePromotion(bitRefFrom, bitRefTo, moveDetail)) {
            trackedBoardHashValue = trackedBoardHashValue xor
                    ZorbristHashCalculator.pieceHashValues[moveDetail.movePiece][bitRefFrom] xor
                    ZorbristHashCalculator.pieceHashValues[moveDetail.movePiece][bitRefTo]
            if (!unMakeEnPassant(bitRefTo, moveDetail) && !unMakeCapture(bitRefTo, moveDetail)) {
                if (moveDetail.movePiece == BITBOARD_WK && bitRefFrom == 3) unMakeWhiteCastle(bitRefTo)
                if (moveDetail.movePiece == BITBOARD_BK && bitRefFrom == 59) unMakeBlackCastle(bitRefTo)
            }
        }
        switchMover()
    }

}