package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.engine.core.type.MoveDetail;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.util.ChessBoardConversion;

public class ZorbristHashTracker {

    private long trackedBoardHash;
    private long trackedPawnHash;

    final long switchMoverHashValue =
            ZorbristHashCalculator.getWhiteMoverHashValue() ^ ZorbristHashCalculator.getBlackMoverHashValue();

    public void initHash(final EngineChessBoard engineChessBoard) {
        trackedBoardHash = ZorbristHashCalculator.calculateHash(engineChessBoard);
        trackedPawnHash = ZorbristHashCalculator.calculatePawnHash(engineChessBoard);
    }

    private void replaceWithEmptySquare(final SquareOccupant squareOccupant, final int bitRef) {
        final int squareOccupantIndex = squareOccupant.getIndex();

        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef];
        if (squareOccupant == SquareOccupant.WP || squareOccupant == SquareOccupant.BP) {
            trackedPawnHash ^= ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef];
        }
    }

    private void placePieceOnEmptySquare(final SquareOccupant squareOccupant, final int bitRef) {
        final int squareOccupantIndex = squareOccupant.getIndex();

        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef];
        if (squareOccupant == SquareOccupant.WP || squareOccupant == SquareOccupant.BP) {
            trackedPawnHash ^= ZorbristHashCalculator.pieceHashValues[squareOccupantIndex][bitRef];
        }
    }

    private void replaceWithAnotherPiece(final SquareOccupant movedPiece, final SquareOccupant capturedPiece, final int bitRef) {
        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[capturedPiece.getIndex()][bitRef];
        trackedBoardHash ^= ZorbristHashCalculator.pieceHashValues[movedPiece.getIndex()][bitRef];
        if (capturedPiece == SquareOccupant.WP || capturedPiece == SquareOccupant.BP) {
            trackedPawnHash ^= ZorbristHashCalculator.pieceHashValues[capturedPiece.getIndex()][bitRef];
        }
        if (movedPiece == SquareOccupant.WP || movedPiece == SquareOccupant.BP) {
            trackedPawnHash ^= ZorbristHashCalculator.pieceHashValues[movedPiece.getIndex()][bitRef];
        }
    }

    private void processPossibleWhiteKingSideCastle(final int bitRefTo) {
        if (bitRefTo == 1) {
            replaceWithEmptySquare(SquareOccupant.WR, 0);
            placePieceOnEmptySquare(SquareOccupant.WR, 2);
        }
    }

    private void processPossibleWhiteQueenSideCastle(final int bitRefTo) {
        if (bitRefTo == 5) {
            replaceWithEmptySquare(SquareOccupant.WR, 7);
            placePieceOnEmptySquare(SquareOccupant.WR, 4);
        }
    }

    private void processPossibleBlackQueenSideCastle(final int bitRefTo) {
        if (bitRefTo == 61) {
            replaceWithEmptySquare(SquareOccupant.BR, 63);
            placePieceOnEmptySquare(SquareOccupant.BR, 60);
        }
    }

    private void processPossibleBlackKingSideCastle(final int bitRefTo) {
        if (bitRefTo == 57) {
            replaceWithEmptySquare(SquareOccupant.BR, 56);
            placePieceOnEmptySquare(SquareOccupant.BR, 58);
        }
    }

    private void processPossibleWhitePawnEnPassantCapture(final Move move, final SquareOccupant capturedPiece) {
        if (move.getSrcBoardRef().getXFile() != move.getTgtBoardRef().getXFile() && capturedPiece == SquareOccupant.NONE) {

            final int capturedPawnBitRef = ChessBoardConversion.getBitRefFromBoardRef(
                        Square.fromCoords(move.getTgtBoardRef().getXFile(), move.getTgtBoardRef().getYRank() + 1
                    ));

            replaceWithEmptySquare(SquareOccupant.BP, capturedPawnBitRef);
        }
    }

    private void processPossibleBlackPawnEnPassantCapture(final Move move, final SquareOccupant capturedPiece) {
        if (move.getSrcBoardRef().getXFile() != move.getTgtBoardRef().getXFile() && capturedPiece == SquareOccupant.NONE) {

            final int capturedPawnBitRef = ChessBoardConversion.getBitRefFromBoardRef(
                    Square.fromCoords(move.getTgtBoardRef().getXFile(), move.getTgtBoardRef().getYRank() - 1
                    ));

            replaceWithEmptySquare(SquareOccupant.WP, capturedPawnBitRef);
        }
    }

    private void processCapture(final SquareOccupant movedPiece, final SquareOccupant capturedPiece, int bitRefTo) {
        if (capturedPiece == SquareOccupant.NONE) {
            placePieceOnEmptySquare(movedPiece, bitRefTo);
        } else {
            replaceWithAnotherPiece(movedPiece, capturedPiece, bitRefTo);
        }
    }

    private void switchMover() {
        trackedBoardHash ^= switchMoverHashValue;
    }

    private void processCastling(final int bitRefFrom, final SquareOccupant movedPiece, final int bitRefTo) {
        if (movedPiece == SquareOccupant.WK && bitRefFrom == 3) {
            processPossibleWhiteKingSideCastle(bitRefTo);
            processPossibleWhiteQueenSideCastle(bitRefTo);
        }

        if (movedPiece == SquareOccupant.BK && bitRefFrom == 59) {
            processPossibleBlackKingSideCastle(bitRefTo);
            processPossibleBlackQueenSideCastle(bitRefTo);
        }
    }

    private SquareOccupant getSquareOccupantFromString(String s) {
        if (s.trim().equals("")) {
            return SquareOccupant.NONE;
        }
        return SquareOccupant.fromChar(s.toCharArray()[0]);
    }

    private void processSpecialPawnMoves(final Move move, final SquareOccupant movedPiece, final int bitRefTo, final SquareOccupant capturedPiece) {
        if (movedPiece == SquareOccupant.WP) {
            processPossibleWhitePawnEnPassantCapture(move, capturedPiece);
            final SquareOccupant promotionPiece = move.getPromotedPiece();
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.WP, bitRefTo);
            }
        }

        if (movedPiece == SquareOccupant.BP) {
            processPossibleBlackPawnEnPassantCapture(move, capturedPiece);
            final SquareOccupant promotionPiece = move.getPromotedPiece();
            if (promotionPiece != SquareOccupant.NONE) {
                replaceWithAnotherPiece(promotionPiece, SquareOccupant.BP, bitRefTo);
            }
        }
    }

    private boolean unMakeEnPassant(final int bitRefTo, final MoveDetail moveDetail) {
        if ((1L << bitRefTo) == moveDetail.enPassantBitboard) {
            if (moveDetail.movePiece == SquareOccupant.WP) {
                placePieceOnEmptySquare(SquareOccupant.BP, bitRefTo-8);
                return true;
            } else if (moveDetail.movePiece == SquareOccupant.BP) {
                placePieceOnEmptySquare(SquareOccupant.WP, bitRefTo+8);
                return true;

            }
        }
        return false;
    }

    public boolean unMakeCapture(final int bitRefTo, final MoveDetail moveDetail) {
        if (moveDetail.capturePiece != SquareOccupant.NONE) {
            placePieceOnEmptySquare(moveDetail.capturePiece, bitRefTo);
            return true;
        }
        return false;
    }

    public boolean unMakeWhiteCastle(final int bitRefTo) {

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

    public boolean unMakeBlackCastle(final int bitRefTo) {
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

    public boolean unMakePromotion(final int bitRefFrom, final int bitRefTo, final MoveDetail moveDetail) {
        final Move move = ChessBoardConversion.getMoveRefFromEngineMove(moveDetail.move);
        final SquareOccupant movedPiece = moveDetail.movePiece;
        final SquareOccupant promotedPiece = move.getPromotedPiece();
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

    public void makeMove(final EngineChessBoard board, final EngineMove engineMove) {

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

    public void unMakeMove(final MoveDetail moveDetail) {

        final Move move = ChessBoardConversion.getMoveRefFromEngineMove(moveDetail.move);
        final int bitRefFrom = ChessBoardConversion.getBitRefFromBoardRef(move.getSrcBoardRef());
        final int bitRefTo = ChessBoardConversion.getBitRefFromBoardRef(move.getTgtBoardRef());

        if (!unMakePromotion(bitRefFrom, bitRefTo, moveDetail)) {
            placePieceOnEmptySquare(moveDetail.movePiece, bitRefFrom);
            replaceWithEmptySquare(moveDetail.movePiece, bitRefTo);
            if (!unMakeEnPassant(bitRefTo, moveDetail)) {
                if (!unMakeCapture(bitRefTo, moveDetail)) {
                    if (moveDetail.movePiece == SquareOccupant.WK && bitRefFrom == 3) {
                        unMakeWhiteCastle(bitRefTo);
                    }
                    if (moveDetail.movePiece == SquareOccupant.BK && bitRefFrom == 59) {
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

    public long getTrackedPawnHashValue() {
        return trackedPawnHash;
    }

}
