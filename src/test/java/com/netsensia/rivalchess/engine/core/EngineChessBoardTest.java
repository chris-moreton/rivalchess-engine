package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
}
