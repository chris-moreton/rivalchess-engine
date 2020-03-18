package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;

public class FenUtils {

    private static final String FEN_START_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private FenUtils() {}

    public static String getStartPos() {
        return FEN_START_POS;
    }

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

        board.setSideToMove(fenToken == 'w' ? Colour.WHITE : Colour.BLACK);

        fenIndex++;

        board.setQueenSideCastleAvailable(Colour.WHITE,false);
        board.setKingSideCastleAvailable(Colour.WHITE,false);
        board.setQueenSideCastleAvailable(Colour.BLACK,false);
        board.setKingSideCastleAvailable(Colour.BLACK,false);

        final String castleFlags = fenStr.substring(fenIndex, fenStr.indexOf(' ', fenIndex));

        board.setQueenSideCastleAvailable(Colour.WHITE,castleFlags.contains("Q"));
        board.setKingSideCastleAvailable(Colour.WHITE,castleFlags.contains("K"));
        board.setQueenSideCastleAvailable(Colour.BLACK,castleFlags.contains("q"));
        board.setKingSideCastleAvailable(Colour.BLACK,castleFlags.contains("k"));

        final char enPassantChar = fenStr.charAt(fenIndex + castleFlags.length() + 1);

        board.setEnPassantFile(enPassantChar != '-' ? enPassantChar - 97 : -1);

        return board;
    }

    private static int setPiece(Board board, int boardArrayIndex, char fenToken) {
        final int targetXFile = boardArrayIndex % board.getNumXFiles();
        final int targetYRank = boardArrayIndex / board.getNumXFiles();

        board.setSquareOccupant(Square.fromCoords(targetXFile, targetYRank), SquareOccupant.fromChar(fenToken));
        boardArrayIndex++;
        return boardArrayIndex;
    }

    private static int padBoardWithSpaces(Board board, int boardArrayIndex, char fenToken) {
        for (int n = 1; n <= Character.digit(fenToken, 10); n++) {
            // Todo use Board.VACANT_TILE
            boardArrayIndex = setPiece(board, boardArrayIndex, '_');
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
                newFenBuilder.append(" ").append(invertSquare(fenParts[i]));
            } else {
                newFenBuilder.append(" ").append(fenParts[i]);
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

        return String.valueOf(newFile) + newRank;
    }
}
