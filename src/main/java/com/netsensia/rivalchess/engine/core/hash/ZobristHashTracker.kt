package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.blackMoverHashValue
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.calculateHash
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.whiteMoverHashValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.engine.core.type.MoveDetail
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

    private fun replaceWithAnotherPiece(movedPiece: SquareOccupant, capturedPiece: SquareOccupant, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor
                ZorbristHashCalculator.pieceHashValues[capturedPiece.index][bitRef] xor
                ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRef]
    }

    private fun processPossibleWhitePawnEnPassantCapture(move: Move, capturedPiece: SquareOccupant) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank + 1)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.BP.index][capturedPawnBitRef]
        }
    }

    private fun processPossibleBlackPawnEnPassantCapture(move: Move, capturedPiece: SquareOccupant) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank - 1)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.WP.index][capturedPawnBitRef]
        }
    }

    private fun processCapture(movedPiece: SquareOccupant, capturedPiece: SquareOccupant, bitRefTo: Int) {
        if (capturedPiece == SquareOccupant.NONE)
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRefTo]
        else replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo)
    }

    private fun switchMover() {
        trackedBoardHashValue = trackedBoardHashValue xor switchMoverHashValue
    }

    private fun processCastling(bitRefFrom: Int, movedPiece: SquareOccupant, bitRefTo: Int) {
        if (movedPiece == SquareOccupant.WK && bitRefFrom == 3) {
            if (bitRefTo == 1) trackedBoardHashValue = trackedBoardHashValue xor whiteKingSideCastle
            if (bitRefTo == 5) trackedBoardHashValue = trackedBoardHashValue xor whiteQueenSideCastle
        }
        if (movedPiece == SquareOccupant.BK && bitRefFrom == 59) {
            if (bitRefTo == 57) trackedBoardHashValue = trackedBoardHashValue xor blackKingSideCastle
            if (bitRefTo == 61) trackedBoardHashValue = trackedBoardHashValue xor blackQueenSideCastle
        }
    }

    private fun processSpecialPawnMoves(move: Move, movedPiece: SquareOccupant, bitRefTo: Int, capturedPiece: SquareOccupant) {
        if (movedPiece == SquareOccupant.WP) {
            processPossibleWhitePawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece
            if (promotionPiece != SquareOccupant.NONE) replaceWithAnotherPiece(promotionPiece, SquareOccupant.WP, bitRefTo)
        }
        if (movedPiece == SquareOccupant.BP) {
            processPossibleBlackPawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.BP, bitRefTo)
            }
        }
    }

    private fun unMakeEnPassant(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (1L shl bitRefTo == moveDetail.enPassantBitboard) {
            if (moveDetail.movePiece == SquareOccupant.WP) {
                trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.BP.index][bitRefTo - 8]
                return true
            } else if (moveDetail.movePiece == SquareOccupant.BP) {
                trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[SquareOccupant.WP.index][bitRefTo + 8]
                return true
            }
        }
        return false
    }

    private fun unMakeCapture(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (moveDetail.capturePiece != SquareOccupant.NONE) {
            trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[moveDetail.capturePiece.index][bitRefTo]
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
                    ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRefFrom] xor
                    ZorbristHashCalculator.pieceHashValues[promotedPiece.index][bitRefTo]
            unMakeCapture(bitRefTo, moveDetail)
            return true
        }
        return false
    }

    fun nullMove() = switchMover()

    fun makeMove(engineMove: EngineMove, movedPiece: SquareOccupant, capturedPiece: SquareOccupant) {
        val move = getMoveRefFromEngineMove(engineMove.compact)
        val bitRefFrom = getBitRefFromBoardRef(move.srcBoardRef)
        val bitRefTo = getBitRefFromBoardRef(move.tgtBoardRef)
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRefFrom]
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
                    ZorbristHashCalculator.pieceHashValues[moveDetail.movePiece.index][bitRefFrom] xor
                    ZorbristHashCalculator.pieceHashValues[moveDetail.movePiece.index][bitRefTo]
            if (!unMakeEnPassant(bitRefTo, moveDetail) && !unMakeCapture(bitRefTo, moveDetail)) {
                if (moveDetail.movePiece == SquareOccupant.WK && bitRefFrom == 3) unMakeWhiteCastle(bitRefTo)
                if (moveDetail.movePiece == SquareOccupant.BK && bitRefFrom == 59) unMakeBlackCastle(bitRefTo)
            }
        }
        switchMover()
    }

}