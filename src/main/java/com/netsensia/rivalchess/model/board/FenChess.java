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

            if (fenToken >= '0' && fenToken <= '9') {
                boardArrayIndex = padBoardWithSpaces(boardArrayIndex, fenToken);
            } else if (fenToken != '/') {
                boardArrayIndex = setPiece(boardArrayIndex, fenToken);
            }

            if (fenToken == ' ') {
                throw new IllegalFenException("Unexpected space character found");
            }

            if (boardArrayIndex == (this.board.getNumXFiles() * this.board.getNumYRanks())) {
                break;
            }

            if (boardArrayIndex > (this.board.getNumXFiles() * this.board.getNumYRanks())) {
                throw new IllegalFenException("Invalid boardArrayIndex");
            }
        }

        fenIndex++;

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

        final String castleFlags = fenStr.substring(fenIndex, fenStr.indexOf(' ', fenIndex));

        this.board.setWhiteQueenSideCastleAvailable(castleFlags.contains("Q"));
        this.board.setWhiteKingSideCastleAvailable(castleFlags.contains("K"));
        this.board.setBlackQueenSideCastleAvailable(castleFlags.contains("q"));
        this.board.setBlackKingSideCastleAvailable(castleFlags.contains("k"));

        final char enPassantChar = fenStr.charAt(fenIndex + castleFlags.length() + 1);

        this.board.setEnPassantFile(enPassantChar != '-' ? enPassantChar - 97 : -1);
    }

    private int setPiece(int boardArrayIndex, char fenToken) {
        final int targetXFile = boardArrayIndex % this.board.getNumXFiles();
        final int targetYRank = boardArrayIndex / this.board.getNumXFiles();

        this.board.setPieceCode(targetXFile, targetYRank, fenToken);
        boardArrayIndex++;
        return boardArrayIndex;
    }

    private int padBoardWithSpaces(int boardArrayIndex, char fenToken) {
        for (int n = 1; n <= Character.digit(fenToken, 10); n++) {
            boardArrayIndex = setPiece(boardArrayIndex, BoardModel.VACANT_TILE);
        }
        return boardArrayIndex;
    }

}
