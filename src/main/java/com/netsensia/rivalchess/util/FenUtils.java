package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;

public class FenUtils {

    private FenUtils() {}

    public static Board getBoardModel(String fenStr) throws IllegalFenException {

        final Board board = new Board();
        
        if (fenStr.trim().equals("")) {
            throw new IllegalFenException("Empty FEN");
        }

        int fenIndex = 0;
        int boardArrayIndex = 0;

        for (int i = 0; i < fenStr.length(); i++) {
            final char fenToken = fenStr.charAt(fenIndex++);

            if (fenToken >= '0' && fenToken <= '9') {
                boardArrayIndex = padBoardWithSpaces(board, boardArrayIndex, fenToken);
            } else if (fenToken != '/') {
                boardArrayIndex = setPiece(board, boardArrayIndex, fenToken);
            }

            if (fenToken == ' ') {
                throw new IllegalFenException("Unexpected space character found");
            }

            if (boardArrayIndex == (board.getNumXFiles() * board.getNumYRanks())) {
                break;
            }

            if (boardArrayIndex > (board.getNumXFiles() * board.getNumYRanks())) {
                throw new IllegalFenException("Invalid boardArrayIndex");
            }
        }

        fenIndex++;

        final char fenToken = fenStr.charAt(fenIndex++);

        if (fenToken != 'w' && fenToken != 'b') {
            throw new IllegalFenException("Illegal mover in FEN");
        }

        board.setWhiteToMove(fenToken == 'w');

        fenIndex++;

        board.setWhiteQueenSideCastleAvailable(false);
        board.setWhiteKingSideCastleAvailable(false);
        board.setBlackQueenSideCastleAvailable(false);
        board.setBlackKingSideCastleAvailable(false);

        final String castleFlags = fenStr.substring(fenIndex, fenStr.indexOf(' ', fenIndex));

        board.setWhiteQueenSideCastleAvailable(castleFlags.contains("Q"));
        board.setWhiteKingSideCastleAvailable(castleFlags.contains("K"));
        board.setBlackQueenSideCastleAvailable(castleFlags.contains("q"));
        board.setBlackKingSideCastleAvailable(castleFlags.contains("k"));

        final char enPassantChar = fenStr.charAt(fenIndex + castleFlags.length() + 1);

        board.setEnPassantFile(enPassantChar != '-' ? enPassantChar - 97 : -1);

        return board;
    }

    private static int setPiece(Board board, int boardArrayIndex, char fenToken) {
        final int targetXFile = boardArrayIndex % board.getNumXFiles();
        final int targetYRank = boardArrayIndex / board.getNumXFiles();

        board.setPieceCode(targetXFile, targetYRank, fenToken);
        boardArrayIndex++;
        return boardArrayIndex;
    }

    private static int padBoardWithSpaces(Board board, int boardArrayIndex, char fenToken) {
        for (int n = 1; n <= Character.digit(fenToken, 10); n++) {
            boardArrayIndex = setPiece(board, boardArrayIndex, Board.VACANT_TILE);
        }
        return boardArrayIndex;
    }

    public static String invertFen(String fen) throws IllegalFenException {
        fen = fen.trim();

        fen = fen.replace(" b ", " . ");
        fen = fen.replace(" w ", " ; ");

        fen = fen.replace('Q', 'z');
        fen = fen.replace('K', 'x');
        fen = fen.replace('N', 'c');
        fen = fen.replace('B', 'v');
        fen = fen.replace('R', 'm');
        fen = fen.replace('P', ',');

        fen = fen.replace('q', 'Q');
        fen = fen.replace('k', 'K');
        fen = fen.replace('n', 'N');
        fen = fen.replace('b', 'B');
        fen = fen.replace('r', 'R');
        fen = fen.replace('p', 'P');

        fen = fen.replace('z', 'q');
        fen = fen.replace('x', 'k');
        fen = fen.replace('c', 'n');
        fen = fen.replace('v', 'b');
        fen = fen.replace('m', 'r');
        fen = fen.replace(',', 'p');

        fen = fen.replace(" . ", " w ");
        fen = fen.replace(" ; ", " b ");

        String[] fenParts = fen.split(" ");
        String[] boardParts = fenParts[0].split("/");

        String newFen =
                boardParts[7] + "/" +
                        boardParts[6] + "/" +
                        boardParts[5] + "/" +
                        boardParts[4] + "/" +
                        boardParts[3] + "/" +
                        boardParts[2] + "/" +
                        boardParts[1] + "/" +
                        boardParts[0];

        StringBuilder newFenBuilder = new StringBuilder(newFen);

        for (int i = 1; i < fenParts.length; i++) {
            if (i == 3) {
                newFenBuilder.append(" " + invertSquare(fenParts[i]));
            } else {
                newFenBuilder.append(" " + fenParts[i]);
            }
        }

        return newFenBuilder.toString();
    }

    private static String invertSquare(final String square) throws IllegalFenException {
        if (square.equals("-")) {
            return square;
        }

        if (square.length() != 2) {
            throw new IllegalFenException("Invalid square reference " + square);
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);

        char newFile = (char)('h' - file + 'a');
        char newRank = (char)('8' - rank + '1');

        StringBuilder sb = new StringBuilder();
        return sb.append(newFile).append(newRank).toString();
    }
}
