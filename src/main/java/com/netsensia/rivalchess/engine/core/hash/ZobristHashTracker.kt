package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.blackMoverHashValue
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.calculateHash
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.whiteMoverHashValue
import com.netsensia.rivalchess.engine.core.type.MoveDetail
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.util.getBitRefFromBoardRef
import com.netsensia.rivalchess.util.getMoveRefFromEngineMove

class ZobristHashTracker {
    @JvmField
    var trackedBoardHashValue: Long = 0

    val whiteKingSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][0] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][2]
    val whiteQueenSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][7] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.WR.index][4]
    val blackKingSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][56] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][58]
    val blackQueenSideCastle = ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][63] xor
            ZorbristHashCalculator.pieceHashValues[SquareOccupant.BR.index][60]

    private val switchMoverHashValue = whiteMoverHashValue xor blackMoverHashValue

    fun initHash(engineBoard: EngineBoard) {
        trackedBoardHashValue = calculateHash(engineBoard)
    }

    fun switchMover() {
        trackedBoardHashValue = trackedBoardHashValue xor switchMoverHashValue
    }

    private fun removeOrPlacePieceOnEmptySquare(squareOccupant: Int, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[squareOccupant][bitRef]
    }

    private fun replaceWithAnotherPiece(movedPiece: Int, capturedPiece: Int, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor
                ZorbristHashCalculator.pieceHashValues[capturedPiece][bitRef] xor
                ZorbristHashCalculator.pieceHashValues[movedPiece][bitRef]
    }

    private fun processPossibleWhitePawnEnPassantCapture(move: Move, capturedPiece: Int) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE.index) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank + 1)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.BP.index, capturedPawnBitRef)
        }
    }

    private fun processPossibleBlackPawnEnPassantCapture(move: Move, capturedPiece: Int) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE.index) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank - 1)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.WP.index, capturedPawnBitRef)
        }
    }

    private fun processCapture(movedPiece: Int, capturedPiece: Int, bitRefTo: Int) {
        if (capturedPiece == SquareOccupant.NONE.index)
            removeOrPlacePieceOnEmptySquare(movedPiece, bitRefTo)
        else
            replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo)
    }

    private fun processCastling(bitRefFrom: Int, movedPiece: Int, bitRefTo: Int) {
        if (movedPiece == SquareOccupant.WK.index && bitRefFrom == 3) {
            if (bitRefTo == 1) trackedBoardHashValue = trackedBoardHashValue xor whiteKingSideCastle
            if (bitRefTo == 5) trackedBoardHashValue = trackedBoardHashValue xor whiteQueenSideCastle
        }
        if (movedPiece == SquareOccupant.BK.index && bitRefFrom == 59) {
            if (bitRefTo == 57) trackedBoardHashValue = trackedBoardHashValue xor blackKingSideCastle
            if (bitRefTo == 61) trackedBoardHashValue = trackedBoardHashValue xor blackQueenSideCastle
        }
    }

    private fun processSpecialPawnMoves(move: Move, movedPiece: Int, bitRefTo: Int, capturedPiece: Int) {
        if (movedPiece == SquareOccupant.WP.index) {
            processPossibleWhitePawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece.index
            if (promotionPiece != SquareOccupant.NONE.index) replaceWithAnotherPiece(promotionPiece, SquareOccupant.WP.index, bitRefTo)
        }
        if (movedPiece == SquareOccupant.BP.index) {
            processPossibleBlackPawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece.index
            if (promotionPiece != SquareOccupant.NONE.index) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.BP.index, bitRefTo)
            }
        }
    }

    private fun unMakeEnPassant(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (1L shl bitRefTo == moveDetail.enPassantBitboard) {
            if (moveDetail.movePiece == SquareOccupant.WP) {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.BP.index, bitRefTo - 8)
                return true
            } else if (moveDetail.movePiece == SquareOccupant.BP) {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.WP.index, bitRefTo + 8)
                return true
            }
        }
        return false
    }

    private fun unMakeCapture(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (moveDetail.capturePiece != SquareOccupant.NONE) {
            removeOrPlacePieceOnEmptySquare(moveDetail.capturePiece.index, bitRefTo)
            return true
        }
        return false
    }

    private fun unMakeWhiteCastle(bitRefTo: Int) =
            when (bitRefTo) {
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

    private fun unMakeBlackCastle(bitRefTo: Int) =
            when (bitRefTo) {
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

    private fun unMakePromotion(bitRefFrom: Int, bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        val move = getMoveRefFromEngineMove(moveDetail.move)
        val movedPiece = moveDetail.movePiece.index
        val promotedPiece = move.promotedPiece.index
        if (promotedPiece != SquareOccupant.NONE.index) {
            removeOrPlacePieceOnEmptySquare(movedPiece, bitRefFrom)
            removeOrPlacePieceOnEmptySquare(promotedPiece, bitRefTo)
            unMakeCapture(bitRefTo, moveDetail)
            return true
        }
        return false
    }

    fun makeMove(board: EngineBoard, engineMove: Int) {
        val move = getMoveRefFromEngineMove(engineMove)
        val bitRefFrom = getBitRefFromBoardRef(move.srcBoardRef)
        val movedPiece = board.squareContents[bitRefFrom].index
        val bitRefTo = getBitRefFromBoardRef(move.tgtBoardRef)
        val capturedPiece = board.squareContents[bitRefTo].index
        removeOrPlacePieceOnEmptySquare(movedPiece, bitRefFrom)
        processCapture(movedPiece, capturedPiece, bitRefTo)
        processSpecialPawnMoves(move, movedPiece, bitRefTo, capturedPiece)
        processCastling(bitRefFrom, movedPiece, bitRefTo)
        trackedBoardHashValue = trackedBoardHashValue xor switchMoverHashValue
    }

    fun unMakeMove(moveDetail: MoveDetail) {
        val move = getMoveRefFromEngineMove(moveDetail.move)
        val bitRefFrom = getBitRefFromBoardRef(move.srcBoardRef)
        val bitRefTo = getBitRefFromBoardRef(move.tgtBoardRef)
        if (!unMakePromotion(bitRefFrom, bitRefTo, moveDetail)) {
            removeOrPlacePieceOnEmptySquare(moveDetail.movePiece.index, bitRefFrom)
            removeOrPlacePieceOnEmptySquare(moveDetail.movePiece.index, bitRefTo)
            if (!unMakeEnPassant(bitRefTo, moveDetail) && !unMakeCapture(bitRefTo, moveDetail)) {
                if (moveDetail.movePiece == SquareOccupant.WK && bitRefFrom == 3) unMakeWhiteCastle(bitRefTo)
                if (moveDetail.movePiece == SquareOccupant.BK && bitRefFrom == 59) unMakeBlackCastle(bitRefTo)
            }
        }
        trackedBoardHashValue = trackedBoardHashValue xor switchMoverHashValue
    }

}