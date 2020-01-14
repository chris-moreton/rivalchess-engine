package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.board.BoardModel;
import com.netsensia.rivalchess.model.board.FenChess;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class EngineChessBoardTest {

    @Test
    public void canRecognisePreviousPositions() throws IllegalFenException {
        final EngineChessBoard board = new EngineChessBoard();
        final List<String> moves = new ArrayList<>();
        final BoardModel boardModel = new BoardModel();
        final FenChess fenChess = new FenChess(boardModel);

        moves.add("e2e4");
        moves.add("e7e5");

        fenChess.setFromStr(EngineChessBoard.START_POS);
        board.setBoard(boardModel);

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertTrue(board.previousOccurrencesOfThisPosition() == 0);

        moves.add("b1c3");
        moves.add("b8c6");
        moves.add("c3b1");
        moves.add("c6b8");

        fenChess.setFromStr(EngineChessBoard.START_POS);
        board.setBoard(boardModel);

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertTrue(board.previousOccurrencesOfThisPosition() == 1);

        moves.add("b1c3");
        moves.add("b8c6");
        moves.add("c3b1");
        moves.add("c6b8");

        fenChess.setFromStr(EngineChessBoard.START_POS);
        board.setBoard(boardModel);

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertTrue(board.previousOccurrencesOfThisPosition() == 2);

        fenChess.setFromStr(EngineChessBoard.START_POS);
        board.setBoard(boardModel);

        moves.add("b1c3");
        moves.add("b8a6");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertTrue(board.previousOccurrencesOfThisPosition() == 0);

        fenChess.setFromStr(EngineChessBoard.START_POS);
        board.setBoard(boardModel);

        moves.add("c3b1");
        moves.add("a6b8");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(move));
        }

        assertTrue(board.previousOccurrencesOfThisPosition() == 3);

    }
}
