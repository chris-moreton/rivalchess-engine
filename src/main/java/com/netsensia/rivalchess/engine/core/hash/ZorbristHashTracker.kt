package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.blackMoverHashValue
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.calculateHash
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.calculatePawnHash
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator.whiteMoverHashValue
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.engine.core.type.MoveDetail
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.util.ChessBoardConversion

class ZorbristHashTracker {
    var trackedBoardHashValue: Long = 0
        private set
    var trackedPawnHashValue: Long = 0
        private set
    val switchMoverHashValue = whiteMoverHashValue xor blackMoverHashValue
    fun initHash(engineBoard: EngineBoard?) {
        trackedBoardHashValue = calculateHash(engineBoard!!)
        trackedPawnHashValue = calculatePawnHash(engineBoard)
    }

    private fun replaceWithEmptySquare(squareOccupant: SquareOccupant, bitRef: Int) {
        val squareOccupantIndex = squareOccupant.index
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef]
        if (squareOccupant == SquareOccupant.WP || squareOccupant == SquareOccupant.BP) {
            trackedPawnHashValue = trackedPawnHashValue xor ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef]
        }
    }

    private fun placePieceOnEmptySquare(squareOccupant: SquareOccupant, bitRef: Int) {
        val squareOccupantIndex = squareOccupant.index
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef]
        if (squareOccupant == SquareOccupant.WP || squareOccupant == SquareOccupant.BP) {
            trackedPawnHashValue = trackedPawnHashValue xor ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef]
        }
    }

    private fun replaceWithAnotherPiece(movedPiece: SquareOccupant, capturedPiece: SquareOccupant, bitRef: Int) {
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[capturedPiece.index][bitRef]
        trackedBoardHashValue = trackedBoardHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRef]
        if (capturedPiece == SquareOccupant.WP || capturedPiece == SquareOccupant.BP) {
            trackedPawnHashValue = trackedPawnHashValue xor ZorbristHashCalculator.pieceHashValues[capturedPiece.index][bitRef]
        }
        if (movedPiece == SquareOccupant.WP || movedPiece == SquareOccupant.BP) {
            trackedPawnHashValue = trackedPawnHashValue xor ZorbristHashCalculator.pieceHashValues[movedPiece.index][bitRef]
        }
    }

    private fun processPossibleWhiteKingSideCastle(bitRefTo: Int) {
        if (bitRefTo == 1) {
            replaceWithEmptySquare(SquareOccupant.WR, 0)
            placePieceOnEmptySquare(SquareOccupant.WR, 2)
        }
    }

    private fun processPossibleWhiteQueenSideCastle(bitRefTo: Int) {
        if (bitRefTo == 5) {
            replaceWithEmptySquare(SquareOccupant.WR, 7)
            placePieceOnEmptySquare(SquareOccupant.WR, 4)
        }
    }

    private fun processPossibleBlackQueenSideCastle(bitRefTo: Int) {
        if (bitRefTo == 61) {
            replaceWithEmptySquare(SquareOccupant.BR, 63)
            placePieceOnEmptySquare(SquareOccupant.BR, 60)
        }
    }

    private fun processPossibleBlackKingSideCastle(bitRefTo: Int) {
        if (bitRefTo == 57) {
            replaceWithEmptySquare(SquareOccupant.BR, 56)
            placePieceOnEmptySquare(SquareOccupant.BR, 58)
        }
    }

    private fun processPossibleWhitePawnEnPassantCapture(move: Move, capturedPiece: SquareOccupant) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE) {
            val capturedPawnBitRef = ChessBoardConversion.getBitRefFromBoardRef(
                    Square.fromCoords(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank + 1
                    ))
            replaceWithEmptySquare(SquareOccupant.BP, capturedPawnBitRef)
        }
    }

    private fun processPossibleBlackPawnEnPassantCapture(move: Move, capturedPiece: SquareOccupant) {
        if (move.srcBoardRef.xFile != move.tgtBoardRef.xFile && capturedPiece == SquareOccupant.NONE) {
            val capturedPawnBitRef = ChessBoardConversion.getBitRefFromBoardRef(
                    Square.fromCoords(move.tgtBoardRef.xFile, move.tgtBoardRef.yRank - 1
                    ))
            replaceWithEmptySquare(SquareOccupant.WP, capturedPawnBitRef)
        }
    }

    private fun processCapture(movedPiece: SquareOccupant, capturedPiece: SquareOccupant, bitRefTo: Int) {
        if (capturedPiece == SquareOccupant.NONE) {
            placePieceOnEmptySquare(movedPiece, bitRefTo)
        } else {
            replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo)
        }
    }

    private fun switchMover() {
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

    private fun getSquareOccupantFromString(s: String): SquareOccupant {
        return if (s.trim { it <= ' ' } == "") {
            SquareOccupant.NONE
        } else SquareOccupant.fromChar(s.toCharArray()[0])
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
                placePieceOnEmptySquare(SquareOccupant.BP, bitRefTo - 8)
                return true
            } else if (moveDetail.movePiece == SquareOccupant.BP) {
                placePieceOnEmptySquare(SquareOccupant.WP, bitRefTo + 8)
                return true
            }
        }
        return false
    }

    fun unMakeCapture(bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        if (moveDetail.capturePiece != SquareOccupant.NONE) {
            placePieceOnEmptySquare(moveDetail.capturePiece, bitRefTo)
            return true
        }
        return false
    }

    fun unMakeWhiteCastle(bitRefTo: Int): Boolean {
        return when (bitRefTo) {
            1 -> {
                replaceWithEmptySquare(SquareOccupant.WR, 2)
                placePieceOnEmptySquare(SquareOccupant.WR, 0)
                true
            }
            5 -> {
                replaceWithEmptySquare(SquareOccupant.WR, 4)
                placePieceOnEmptySquare(SquareOccupant.WR, 7)
                true
            }
            else -> false
        }
    }

    fun unMakeBlackCastle(bitRefTo: Int): Boolean {
        return when (bitRefTo) {
            61 -> {
                replaceWithEmptySquare(SquareOccupant.BR, 60)
                placePieceOnEmptySquare(SquareOccupant.BR, 63)
                true
            }
            57 -> {
                replaceWithEmptySquare(SquareOccupant.BR, 58)
                placePieceOnEmptySquare(SquareOccupant.BR, 56)
                true
            }
            else -> false
        }
    }

    fun unMakePromotion(bitRefFrom: Int, bitRefTo: Int, moveDetail: MoveDetail): Boolean {
        val move = ChessBoardConversion.getMoveRefFromEngineMove(moveDetail.move)
        val movedPiece = moveDetail.movePiece
        val promotedPiece = move.promotedPiece
        if (promotedPiece != SquareOccupant.NONE) {
            placePieceOnEmptySquare(movedPiece, bitRefFrom)
            replaceWithEmptySquare(promotedPiece, bitRefTo)
            unMakeCapture(bitRefTo, moveDetail)
            return true
        }
        return false
    }

    fun nullMove() {
        switchMover()
    }

    fun makeMove(board: EngineBoard, engineMove: EngineMove) {
        val move = ChessBoardConversion.getMoveRefFromEngineMove(engineMove.compact)
        val bitRefFrom = ChessBoardConversion.getBitRefFromBoardRef(move.srcBoardRef)
        val movedPiece = board.getSquareOccupant(bitRefFrom)
        val bitRefTo = ChessBoardConversion.getBitRefFromBoardRef(move.tgtBoardRef)
        val capturedPiece = board.getSquareOccupant(bitRefTo)
        replaceWithEmptySquare(movedPiece, bitRefFrom)
        processCapture(movedPiece, capturedPiece, bitRefTo)
        processSpecialPawnMoves(move, movedPiece, bitRefTo, capturedPiece)
        processCastling(bitRefFrom, movedPiece, bitRefTo)
        switchMover()
    }

    fun unMakeMove(moveDetail: MoveDetail) {
        val move = ChessBoardConversion.getMoveRefFromEngineMove(moveDetail.move)
        val bitRefFrom = ChessBoardConversion.getBitRefFromBoardRef(move.srcBoardRef)
        val bitRefTo = ChessBoardConversion.getBitRefFromBoardRef(move.tgtBoardRef)
        if (!unMakePromotion(bitRefFrom, bitRefTo, moveDetail)) {
            placePieceOnEmptySquare(moveDetail.movePiece, bitRefFrom)
            replaceWithEmptySquare(moveDetail.movePiece, bitRefTo)
            if (!unMakeEnPassant(bitRefTo, moveDetail)) {
                if (!unMakeCapture(bitRefTo, moveDetail)) {
                    if (moveDetail.movePiece == SquareOccupant.WK && bitRefFrom == 3) {
                        unMakeWhiteCastle(bitRefTo)
                    }
                    if (moveDetail.movePiece == SquareOccupant.BK && bitRefFrom == 59) {
                        unMakeBlackCastle(bitRefTo)
                    }
                }
            }
        }
        switchMover()
    }

}