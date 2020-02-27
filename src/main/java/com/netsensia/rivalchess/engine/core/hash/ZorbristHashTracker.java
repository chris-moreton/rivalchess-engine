package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.engine.core.type.MoveDetail;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.util.ChessBoardConversion;

public class ZorbristHashTracker {

    private long trackedBoardHash;
    
    public void initHash(EngineChessBoard engineChessBoard) {
        trackedBoardHash = ZorbristHashCalculator.calculateHash(engineChessBoard);
    }

    private void replaceWithEmptySquare(SquareOccupant piece, int bitRef) {
        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[piece.getIndex()][bitRef];
    }

    private void placePieceOnEmptySquare(SquareOccupant piece, int bitRef) {
        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[piece.getIndex()][bitRef];
    }

    private void replaceWithAnotherPiece(SquareOccupant movedPiece, SquareOccupant capturedPiece, int bitRef) {
        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[capturedPiece.getIndex()][bitRef];
        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[movedPiece.getIndex()][bitRef];
    }

    private void processPossibleWhiteKingSideCastle(int bitRefTo) {
        if (bitRefTo == 1) {
            replaceWithEmptySquare(SquareOccupant.WR, 0);
            placePieceOnEmptySquare(SquareOccupant.WR, 2);
        }
    }

    private void processPossibleWhiteQueenSideCastle(int bitRefTo) {
        if (bitRefTo == 5) {
            replaceWithEmptySquare(SquareOccupant.WR, 7);
            placePieceOnEmptySquare(SquareOccupant.WR, 4);
        }
    }

    private void processPossibleBlackQueenSideCastle(int bitRefTo) {
        if (bitRefTo == 61) {
            replaceWithEmptySquare(SquareOccupant.BR, 63);
            placePieceOnEmptySquare(SquareOccupant.BR, 60);
        }
    }

    private void processPossibleBlackKingSideCastle(int bitRefTo) {
        if (bitRefTo == 57) {
            replaceWithEmptySquare(SquareOccupant.BR, 56);
            placePieceOnEmptySquare(SquareOccupant.BR, 58);
        }
    }

    private void processPossibleWhitePawnEnPassantCapture(Move move, SquareOccupant capturedPiece) {
        if (move.getSrcXFile() != move.getTgtXFile() && capturedPiece == SquareOccupant.NONE) {

            final int capturedPawnBitRef = ChessBoardConversion.getBitRefFromBoardRef(
                        new Square(move.getTgtXFile(), move.getTgtYRank() + 1
                    ));

            replaceWithEmptySquare(SquareOccupant.BP, capturedPawnBitRef);
        }
    }

    private void processPossibleBlackPawnEnPassantCapture(Move move, SquareOccupant capturedPiece) {
        if (move.getSrcXFile() != move.getTgtXFile() && capturedPiece == SquareOccupant.NONE) {

            final int capturedPawnBitRef = ChessBoardConversion.getBitRefFromBoardRef(
                    new Square(move.getTgtXFile(), move.getTgtYRank() - 1
                    ));

            replaceWithEmptySquare(SquareOccupant.WP, capturedPawnBitRef);
        }
    }

    private void processCapture(SquareOccupant movedPiece, SquareOccupant capturedPiece, int bitRefTo) {
        if (capturedPiece == SquareOccupant.NONE) {
            placePieceOnEmptySquare(movedPiece, bitRefTo);
        } else {
            replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo);
        }
    }

    private void switchMover() {
        trackedBoardHash ^= ZorbristHashCalculator.moverHashValues[Colour.WHITE.getValue()];
        trackedBoardHash ^= ZorbristHashCalculator.moverHashValues[Colour.BLACK.getValue()];
    }

    private void processCastling(int bitRefFrom, SquareOccupant movedPiece, int bitRefTo) {
        if (movedPiece == SquareOccupant.WK && bitRefFrom == 3) {
            processPossibleWhiteKingSideCastle(bitRefTo);
            processPossibleWhiteQueenSideCastle(bitRefTo);
        }

        if (movedPiece == SquareOccupant.BK && bitRefFrom == 59) {
            processPossibleBlackKingSideCastle(bitRefTo);
            processPossibleBlackQueenSideCastle(bitRefTo);
        }
    }

    private void processSpecialPawnMoves(Move move, SquareOccupant movedPiece, int bitRefTo, SquareOccupant capturedPiece) {
        if (movedPiece == SquareOccupant.WP) {
            processPossibleWhitePawnEnPassantCapture(move, capturedPiece);
            final SquareOccupant promotionPiece = SquareOccupant.fromString(move.getPromotedPieceCode());
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.WP, bitRefTo);
            }
        }

        if (movedPiece == SquareOccupant.BP) {
            processPossibleBlackPawnEnPassantCapture(move, capturedPiece);
            final SquareOccupant promotionPiece = SquareOccupant.fromString(move.getPromotedPieceCode());
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.BP, bitRefTo);
            }
        }
    }

    private boolean unMakeEnPassant(int bitRefTo, MoveDetail moveDetail) {
        if ((1L << bitRefTo) == moveDetail.enPassantBitboard) {
            if (moveDetail.movePiece == RivalConstants.WP) {
                placePieceOnEmptySquare(SquareOccupant.BP, bitRefTo-8);
                return true;
            } else if (moveDetail.movePiece == RivalConstants.BP) {
                placePieceOnEmptySquare(SquareOccupant.WP, bitRefTo+8);
                return true;

            }
        }
        return false;
    }

    public boolean unMakeCapture(int bitRefTo, MoveDetail moveDetail) {
        if (moveDetail.capturePiece != -1) {
            placePieceOnEmptySquare(SquareOccupant.fromIndex(moveDetail.capturePiece), bitRefTo);
            return true;
        }
        return false;
    }

    public boolean unMakeWhiteCastle(int bitRefTo) {

        switch (bitRefTo) {
            case 1:
                replaceWithEmptySquare(SquareOccupant.WR, 2);
                placePieceOnEmptySquare(SquareOccupant.WR, 0);
                return true;
            case 5:
                replaceWithEmptySquare(SquareOccupant.WR, 4);
                placePieceOnEmptySquare(SquareOccupant.WR, 7);
                return true;
            default:
                return false;
        }
    }

    public boolean unMakeBlackCastle(int bitRefTo) {
        switch (bitRefTo) {
            case 61:
                replaceWithEmptySquare(SquareOccupant.BR, 60);
                placePieceOnEmptySquare(SquareOccupant.BR, 63);
                return true;
            case 57:
                replaceWithEmptySquare(SquareOccupant.BR, 58);
                placePieceOnEmptySquare(SquareOccupant.BR, 56);
                return true;
            default:
                return false;
        }
    }

    public boolean unMakePromotion(int bitRefFrom, int bitRefTo, MoveDetail moveDetail) {
        final Move move = ChessBoardConversion.getMoveRefFromEngineMove(moveDetail.move);
        final SquareOccupant movedPiece = SquareOccupant.fromIndex(moveDetail.movePiece);
        SquareOccupant promotedPiece = SquareOccupant.fromString(move.getPromotedPieceCode());
        if (promotedPiece != SquareOccupant.NONE) {
            placePieceOnEmptySquare(movedPiece, bitRefFrom);
            replaceWithEmptySquare(promotedPiece, bitRefTo);
            unMakeCapture(bitRefTo, moveDetail);
            return true;
        }
        return false;
    }

    public void nullMove() {
        switchMover();
    }

    public void makeMove(EngineChessBoard board, EngineMove engineMove) {

        final Move move = ChessBoardConversion.getMoveRefFromEngineMove(engineMove.compact);
        final int bitRefFrom = ChessBoardConversion.getBitRefFromBoardRef(move.getSrcBoardRef());
        final SquareOccupant movedPiece = board.getSquareOccupant(bitRefFrom);
        final int bitRefTo = ChessBoardConversion.getBitRefFromBoardRef(move.getTgtBoardRef());
        final SquareOccupant capturedPiece = board.getSquareOccupant(bitRefTo);

        replaceWithEmptySquare(movedPiece, bitRefFrom);

        processCapture(movedPiece, capturedPiece, bitRefTo);
        processSpecialPawnMoves(move, movedPiece, bitRefTo, capturedPiece);
        processCastling(bitRefFrom, movedPiece, bitRefTo);

        switchMover();

    }

    public void unMakeMove(MoveDetail moveDetail) {

        final Move move = ChessBoardConversion.getMoveRefFromEngineMove(moveDetail.move);
        final int bitRefFrom = ChessBoardConversion.getBitRefFromBoardRef(move.getSrcBoardRef());
        final int bitRefTo = ChessBoardConversion.getBitRefFromBoardRef(move.getTgtBoardRef());

        if (!unMakePromotion(bitRefFrom, bitRefTo, moveDetail)) {
            placePieceOnEmptySquare(SquareOccupant.fromIndex(moveDetail.movePiece), bitRefFrom);
            replaceWithEmptySquare(SquareOccupant.fromIndex(moveDetail.movePiece), bitRefTo);
            if (!unMakeEnPassant(bitRefTo, moveDetail)) {
                if (!unMakeCapture(bitRefTo, moveDetail)) {
                    if (SquareOccupant.fromIndex(moveDetail.movePiece) == SquareOccupant.WK && bitRefFrom == 3) {
                        unMakeWhiteCastle(bitRefTo);
                    }
                    if (SquareOccupant.fromIndex(moveDetail.movePiece) == SquareOccupant.BK && bitRefFrom == 59) {
                        unMakeBlackCastle(bitRefTo);
                    }
                }
            }
        }

        switchMover();

    }

    public long getTrackedBoardHashValue() {
        return trackedBoardHash;
    }

}
