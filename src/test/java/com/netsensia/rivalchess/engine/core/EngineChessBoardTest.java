package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.constants.SquareOccupant;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EngineChessBoardTest {

    @Test
    public void canRecognisePreviousPositions() throws IllegalFenException {
        final EngineChessBoard board = new EngineChessBoard();
        final List<String> moves = new ArrayList<>();

        moves.add("e2e4");
        moves.add("e7e5");

        board.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(1, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(2, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        moves.add("b1c3");
        moves.add("b8a6");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        moves.add("c3b1");
        moves.add("a6b8");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertEquals(3, board.previousOccurrencesOfThisPosition());

    }

    private void moveKnightsBackAndForth(EngineChessBoard board, List<String> moves) throws IllegalFenException {
        moves.add("b1c3");
        moves.add("b8c6");
        moves.add("c3b1");
        moves.add("c6b8");

        board.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }
    }

    @Test
    public void isSquareAttacked() throws IllegalFenException {
        final EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -"));

        boolean[][] expectedWhiteAttacks = {
                {false,false,true,false,false,false,false,false},
                {false,false,false,true,false,false,false,true},
                {false,true,false,true,true,true,true,false},
                {false,false,true,true,true,false,true,true},
                {false,false,true,false,true,true,true,false},
                {false,false,true,false,true,true,true,true},
                {false,false,false,true,true,true,true,false},
                {false,false,false,false,false,false,false,false}
        };

        boolean[][] expectedBlackAttacks = {
                {false,false,false,false,false,true,false,true},
                {false,false,false,false,false,true,true,true},
                {false,true,false,false,false,true,true,true},
                {false,false,false,false,true,false,true,false},
                {false,false,false,true,false,true,true,false},
                {false,false,true,false,true,false,true,false},
                {false,false,false,true,false,true,true,true},
                {true,true,true,true,true,true,true,true}
        };

        for (int y=0; y<8; y++) {
            for (int x=0; x<8; x++) {

                assertEquals(expectedWhiteAttacks[y][x], engineChessBoard.isSquareAttackedBy(
                        ChessBoardConversion.getBitRefFromBoardRef(new Square(x, y)),
                        Colour.WHITE));

                assertEquals(expectedBlackAttacks[y][x], engineChessBoard.isSquareAttackedBy(
                        ChessBoardConversion.getBitRefFromBoardRef(new Square(x, y)),
                        Colour.BLACK));
            }
        }
    }

    @Test
    public void isGameOver() throws IllegalFenException {

        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 w - - 0 4";
        final String NOT_STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 b - - 0 4";

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        
        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertFalse(engineChessBoard.isGameOver());

        engineChessBoard.setBoard(FenUtils.getBoardModel(SCHOLARS_MATE));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertTrue(engineChessBoard.isGameOver());

        engineChessBoard.setBoard(FenUtils.getBoardModel(STALEMATE));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertTrue(engineChessBoard.isGameOver());

        engineChessBoard.setBoard(FenUtils.getBoardModel(NOT_STALEMATE));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertFalse(engineChessBoard.isGameOver());
    }

    @Test
    public void getSquareOccupant() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        assertEquals(SquareOccupant.WK, engineChessBoard.getSquareOccupant(3));
        assertEquals(SquareOccupant.WQ, engineChessBoard.getSquareOccupant(4));
        assertEquals(SquareOccupant.BQ, engineChessBoard.getSquareOccupant(60));
        assertEquals(SquareOccupant.BK, engineChessBoard.getSquareOccupant(59));

        assertEquals(SquareOccupant.NONE, engineChessBoard.getSquareOccupant(32));

    }

    @Test
    public void getPiece() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        assertEquals(Piece.KING, engineChessBoard.getPiece(3));
        assertEquals(Piece.QUEEN, engineChessBoard.getPiece(4));
        assertEquals(Piece.KING, engineChessBoard.getPiece(59));
        assertEquals(Piece.QUEEN, engineChessBoard.getPiece(60));

        assertEquals(Piece.NONE, engineChessBoard.getPiece(32));
    }

    @Test
    public void isCapture() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(OpeningLibrary.E2E4_D7D5));
        assertTrue(engineChessBoard.isCapture(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e4d5")));
        assertFalse(engineChessBoard.isCapture(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e4e5")));
        engineChessBoard.setBoard(FenUtils.getBoardModel("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6"));
        assertTrue(engineChessBoard.isCapture(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e5d6")));
        assertFalse(engineChessBoard.isCapture(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e5e6")));
    }

    @Test
    public void isCheck() throws IllegalFenException {
        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String SILLY_CHECK = "rnbqkbnr/pppp1ppp/8/8/8/8/PPPP1PPP/RNBKQBNR b KQkq - 0 4";
        final String STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 w - - 0 4";

        EngineChessBoard engineChessBoard = new EngineChessBoard();

        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertFalse(engineChessBoard.isCheck());

        engineChessBoard.setBoard(FenUtils.getBoardModel(SILLY_CHECK));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertTrue(engineChessBoard.isCheck());

        engineChessBoard.setBoard(FenUtils.getBoardModel(SCHOLARS_MATE));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertTrue(engineChessBoard.isCheck());

        engineChessBoard.setBoard(FenUtils.getBoardModel(STALEMATE));
        engineChessBoard.setLegalMoves(new int[RivalConstants.MAX_LEGAL_MOVES]);
        assertFalse(engineChessBoard.isCheck());
    }

    @Test
    public void getWhitePieceValues() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();

        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        assertEquals(
                Piece.QUEEN.getValue()
                + Piece.KNIGHT.getValue() * 2
                + Piece.BISHOP.getValue() * 2
                + Piece.ROOK.getValue() * 2,
                engineChessBoard.getWhitePieceValues());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e2e4"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("d7d6"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("g1f3"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e7e5"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("f3e5"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("d6e5"));

        assertEquals(
                Piece.QUEEN.getValue()
                        + Piece.KNIGHT.getValue()
                        + Piece.BISHOP.getValue() * 2
                        + Piece.ROOK.getValue() * 2,
                engineChessBoard.getWhitePieceValues());

    }

    @Test
    public void getBlackPieceValues() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();

        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));

        assertEquals(
                Piece.QUEEN.getValue()
                        + Piece.KNIGHT.getValue() * 2
                        + Piece.BISHOP.getValue() * 2
                        + Piece.ROOK.getValue() * 2,
                engineChessBoard.getBlackPieceValues());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e2e4"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("d7d6"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("g1f3"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e7e5"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("f3e5"));
        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("d6e5"));

        assertEquals(
                Piece.QUEEN.getValue()
                        + Piece.KNIGHT.getValue() * 2,
                        + Piece.BISHOP.getValue() * 2
                        + Piece.ROOK.getValue() * 2,
                engineChessBoard.getWhitePieceValues());

    }

    @Test
    public void getFen() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();

        engineChessBoard.setBoard(FenUtils.getBoardModel(RivalConstants.FEN_START_POS));
        assertEquals(RivalConstants.FEN_START_POS, engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e2e4"));
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("d7d6"));
        assertEquals("rnbqkbnr/ppp1pppp/3p4/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("g1f3"));
        assertEquals("rnbqkbnr/ppp1pppp/3p4/8/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e7e5"));
        assertEquals("rnbqkbnr/ppp2ppp/3p4/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq e6 0 3", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("f3e5"));
        assertEquals("rnbqkbnr/ppp2ppp/3p4/4N3/4P3/8/PPPP1PPP/RNBQKB1R b KQkq - 0 3", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("d6e5"));
        assertEquals("rnbqkbnr/ppp2ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKB1R w KQkq - 0 4", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("c2c4"));
        assertEquals("rnbqkbnr/ppp2ppp/8/4p3/2P1P3/8/PP1P1PPP/RNBQKB1R b KQkq c3 0 4", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("b7b5"));
        assertEquals("rnbqkbnr/p1p2ppp/8/1p2p3/2P1P3/8/PP1P1PPP/RNBQKB1R w KQkq b6 0 5", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("f1e2"));
        assertEquals("rnbqkbnr/p1p2ppp/8/1p2p3/2P1P3/8/PP1PBPPP/RNBQK2R b KQkq - 1 5", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("b5b4"));
        assertEquals("rnbqkbnr/p1p2ppp/8/4p3/1pP1P3/8/PP1PBPPP/RNBQK2R w KQkq - 0 6", engineChessBoard.getFen());

        engineChessBoard.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic("e1f1"));
        assertEquals("rnbqkbnr/p1p2ppp/8/4p3/1pP1P3/8/PP1PBPPP/RNBQ1K1R b kq - 1 6", engineChessBoard.getFen());
    }

}
