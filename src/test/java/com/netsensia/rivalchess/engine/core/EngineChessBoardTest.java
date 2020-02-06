package com.netsensia.rivalchess.engine.core;

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

        board.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(1, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(2, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));

        moves.add("b1c3");
        moves.add("b8a6");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));

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

        board.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));

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

                assertEquals(expectedWhiteAttacks[y][x], engineChessBoard.isSquareAttacked(
                        ChessBoardConversion.getBitRefFromBoardRef(new Square(x, y)),
                        true));

                assertEquals(expectedBlackAttacks[y][x], engineChessBoard.isSquareAttacked(
                        ChessBoardConversion.getBitRefFromBoardRef(new Square(x, y)),
                        false));
            }
        }
    }

    @Test
    public void isGameOver() throws IllegalFenException {

        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 w - - 0 4";
        final String NOT_STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 b - - 0 4";

        EngineChessBoard engineChessBoard = new EngineChessBoard();
        
        engineChessBoard.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));
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
        engineChessBoard.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));

        assertEquals(SquareOccupant.WK, engineChessBoard.getSquareOccupant(3));
        assertEquals(SquareOccupant.WQ, engineChessBoard.getSquareOccupant(4));
        assertEquals(SquareOccupant.BQ, engineChessBoard.getSquareOccupant(60));
        assertEquals(SquareOccupant.BK, engineChessBoard.getSquareOccupant(59));

        assertEquals(SquareOccupant.NONE, engineChessBoard.getSquareOccupant(32));

    }

    @Test
    public void getPiece() throws IllegalFenException {
        EngineChessBoard engineChessBoard = new EngineChessBoard();
        engineChessBoard.setBoard(FenUtils.getBoardModel(EngineChessBoard.START_POS));

        assertEquals(Piece.KING, engineChessBoard.getPiece(3));
        assertEquals(Piece.QUEEN, engineChessBoard.getPiece(4));
        assertEquals(Piece.KING, engineChessBoard.getPiece(59));
        assertEquals(Piece.QUEEN, engineChessBoard.getPiece(60));

        assertEquals(Piece.NONE, engineChessBoard.getPiece(32));

    }
}
