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
    var trackedBoardHashValue: Long = 0
        private set
    private val switchMoverHashValue = whiteMoverHashValue xor blackMoverHashValue

    fun initHash(engineBoard: EngineBoard) {
        trackedBoardHashValue = calculateHash(engineBoard)
    }

    private inline fun removeOrPlacePieceOnEmptySquare(squareOccupant: SquareOccupant, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[squareOccupant.index][bitRef]
    }

    private inline fun replaceWithAnotherPiece(movedPiece: SquareOccupant, capturedPiece: SquareOccupant, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor
                ZorbristHashCalculator.pieceHashValues[capturedPiece.index][bitRef] xor
                ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRef]
    }

    private fun processPossibleWhiteKingSideCastle(bitRefTo: Int) {
        if (bitRefTo == 1) {
            removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 0)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 2)
        }
    }

    private fun processPossibleWhiteQueenSideCastle(bitRefTo: Int) {
        if (bitRefTo == 5) {
            removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 7)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 4)
        }
    }

    private fun processPossibleBlackQueenSideCastle(bitRefTo: Int) {
        if (bitRefTo == 61) {
            removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 63)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 60)
        }
    }

    private fun processPossibleBlackKingSideCastle(bitRefTo: Int) {
        if (bitRefTo == 57) {
            removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 56)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 58)
        }
    }

    private fun processPossibleWhitePawnEnPassantCapture(move: Move, capturedPiece: SquareOccupant) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank + 1)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.BP, capturedPawnBitRef)
        }
    }

    private fun processPossibleBlackPawnEnPassantCapture(move: Move, capturedPiece: SquareOccupant) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE) {
            val capturedPawnBitRef = getBitRefFromBoardRef(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank - 1)
            removeOrPlacePieceOnEmptySquare(SquareOccupant.WP, capturedPawnBitRef)
        }
    }

    private fun processCapture(movedPiece: SquareOccupant, capturedPiece: SquareOccupant, bitRefTo: Int) {
        if (capturedPiece == SquareOccupant.NONE) {
            removeOrPlacePieceOnEmptySquare(movedPiece, bitRefTo)
        } else {
            replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo)
        }
    }

    private inline fun switchMover() {
        trackedBoardHashValue = trackedBoardHashValue xor switchMoverHashValue
    }

    private fun processCastling(bitRefFrom: Int, movedPiece: SquareOccupant, bitRefTo: Int) {
        if (movedPiece == SquareOccupant.WK && bitRefFrom == 3) {
            processPossibleWhiteKingSideCastle(bitRefTo)
            processPossibleWhiteQueenSideCastle(bitRefTo)
        }
        if (movedPiece == SquareOccupant.BK && bitRefFrom == 59) {
            processPossibleBlackKingSideCastle(bitRefTo)
            processPossibleBlackQueenSideCastle(bitRefTo)
        }
    }

    private fun processSpecialPawnMoves(move: Move, movedPiece: SquareOccupant, bitRefTo: Int, capturedPiece: SquareOccupant) {
        if (movedPiece == SquareOccupant.WP) {
            processPossibleWhitePawnEnPassantCapture(move, capturedPiece)
            val promotionPiece = move.promotedPiece
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.WP, bitRefTo)
            }
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
                removeOrPlacePieceOnEmptySquare(SquareOccupant.BP, bitRefTo - 8)
                return true
            } else if (moveDetail.movePiece == SquareOccupant.BP) {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.WP, bitRefTo + 8)
                return true
            }
        }
        return false
    }

    private fun unMakeCapture(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (moveDetail.capturePiece != SquareOccupant.NONE) {
            removeOrPlacePieceOnEmptySquare(moveDetail.capturePiece, bitRefTo)
            return true
        }
        return false
    }

    private fun unMakeWhiteCastle(bitRefTo: Int): Boolean {
        return when (bitRefTo) {
            1 -> {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 2)
                removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 0)
                true
            }
            5 -> {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 4)
                removeOrPlacePieceOnEmptySquare(SquareOccupant.WR, 7)
                true
            }
            else -> false
        }
    }

    private fun unMakeBlackCastle(bitRefTo: Int): Boolean {
        return when (bitRefTo) {
            61 -> {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 60)
                removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 63)
                true
            }
            57 -> {
                removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 58)
                removeOrPlacePieceOnEmptySquare(SquareOccupant.BR, 56)
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
            removeOrPlacePieceOnEmptySquare(movedPiece, bitRefFrom)
            removeOrPlacePieceOnEmptySquare(promotedPiece, bitRefTo)
            unMakeCapture(bitRefTo, moveDetail)
            return true
        }
        return false
    }

    fun nullMove() = switchMover()

    fun makeMove(board: EngineBoard, engineMove: EngineMove) {
        val move = getMoveRefFromEngineMove(engineMove.compact)
        val bitRefFrom = getBitRefFromBoardRef(move.srcBoardRef)
        val movedPiece = board.getSquareOccupant(bitRefFrom)
        val bitRefTo = getBitRefFromBoardRef(move.tgtBoardRef)
        val capturedPiece = board.getSquareOccupant(bitRefTo)
        removeOrPlacePieceOnEmptySquare(movedPiece, bitRefFrom)
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
            removeOrPlacePieceOnEmptySquare(moveDetail.movePiece, bitRefFrom)
            removeOrPlacePieceOnEmptySquare(moveDetail.movePiece, bitRefTo)
            if (!unMakeEnPassant(bitRefTo, moveDetail) && !unMakeCapture(bitRefTo, moveDetail)) {
                if (moveDetail.movePiece == SquareOccupant.WK && bitRefFrom == 3) unMakeWhiteCastle(bitRefTo)
                if (moveDetail.movePiece == SquareOccupant.BK && bitRefFrom == 59) unMakeBlackCastle(bitRefTo)
            }
        }
        switchMover()
    }

}