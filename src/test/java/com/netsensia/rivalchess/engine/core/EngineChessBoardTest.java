package com.netsensia.rivalchess.engine.core;

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

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(1,2)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(3,2)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(2,0)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(6,4)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(6,5)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(5,6)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(4,4)),
                true));

        assertTrue(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(1,2)),
                true));

        assertFalse(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(3,0)),
                true));

        assertFalse(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(4,0)),
                true));

        assertFalse(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(7,0)),
                true));

        assertFalse(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(0,7)),
                true));

        assertFalse(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(1,5)),
                true));

        assertFalse(engineChessBoard.isSquareAttacked(
                ChessBoardConversion.getBitRefFromBoardRef(new Square(7,2)),
                true));

    }
}
