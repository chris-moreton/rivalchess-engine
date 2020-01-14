package com.netsensia.rivalchess.model.board;

import com.netsensia.rivalchess.exception.IllegalFenException;

public class FenChess {
    protected BoardModel board;

    public FenChess(BoardModel board) {
        this.board = board;
    }

    public BoardModel getBoard() {
        return this.board;
    }

    public void setFromStr(String fenStr) throws IllegalFenException {

        if (fenStr.trim().equals("")) {
            throw new IllegalFenException("Empty FEN");
        }

        int fenIndex = 0;
        int boardArrayIndex = 0;

        for (int i = 0; i < fenStr.length(); i++) {
            final char fenToken = fenStr.charAt(fenIndex++);

            // If this is an integer, pad board with spaces
            if (fenToken >= '0' && fenToken <= '9') {
                int numPadded = Character.digit(fenToken, 10);

                for (int n = 1; n <= numPadded; n++) {
                    final int targetXFile = boardArrayIndex % this.board.getNumXFiles();
                    final int targetYRank = boardArrayIndex / this.board.getNumXFiles();

                    this.board.setPieceCode(targetXFile, targetYRank, BoardModel.VACANT_TILE);
                    boardArrayIndex++;
                }
            } else // set the right piece unless divider
            {
                if (fenToken != '/') {
                    final int targetXFile = boardArrayIndex % this.board.getNumXFiles();
                    final int targetYRank = boardArrayIndex / this.board.getNumXFiles();

                    this.board.setPieceCode(targetXFile, targetYRank, fenToken);
                    boardArrayIndex++;
                }
            }

            // No space should exist until finished.
            if (fenToken == ' ') {
                throw new IllegalFenException("Unexpected space character found");
            }

            if (boardArrayIndex == (this.board.getNumXFiles() * this.board.getNumYRanks())) {
                // We have the whole board
                break;
            }

            if (boardArrayIndex > (this.board.getNumXFiles() * this.board.getNumYRanks())) {
                throw new IllegalFenException("Invalid boardArrayIndex");
            }
        }

        fenIndex++; // Scoot past space

        final char fenToken = fenStr.charAt(fenIndex++);

        if (fenToken != 'w' && fenToken != 'b') {
            throw new IllegalFenException("Illegal mover in FEN");
        }

        this.board.setWhiteToMove(fenToken == 'w');

        fenIndex++;

        this.board.setWhiteQueenSideCastleAvailable(false);
        this.board.setWhiteKingSideCastleAvailable(false);
        this.board.setBlackQueenSideCastleAvailable(false);
        this.board.setBlackKingSideCastleAvailable(false);

        while (fenStr.charAt(fenIndex) != ' ') {
            switch (fenStr.charAt(fenIndex)) {
                case 'Q':
                    this.board.setWhiteQueenSideCastleAvailable(true);
                    break;
                case 'K':
                    this.board.setWhiteKingSideCastleAvailable(true);
                    break;
                case 'q':
                    this.board.setBlackQueenSideCastleAvailable(true);
                    break;
                case 'k':
                    this.board.setBlackKingSideCastleAvailable(true);
                    break;
                case '-':
                    break;
                default:
                    throw new IllegalFenException("Illegal character found in castle section " + fenStr.charAt(fenIndex));
            }
            // castle privileges
            fenIndex++;
        }

        final char enPassantChar = fenStr.charAt(fenIndex+1);

        this.board.setEnPassantFile(enPassantChar != '-' ? enPassantChar - 97 : -1);

    }

}
