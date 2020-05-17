package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.engine.core.board.BoardExtensionsKt;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;
import com.netsensia.rivalchess.openings.OpeningLibrary;

import java.util.ArrayList;
import java.util.List;

import static com.netsensia.rivalchess.engine.core.eval.PieceValueKt.pieceValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EngineBoardTest {

    @Test
    public void canRecognisePreviousPositions() throws IllegalFenException, InvalidMoveException {
        final EngineBoard board = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        final List<String> moves = new ArrayList<>();

        moves.add("e2e4");
        moves.add("e7e5");

        board.setBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move));
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(1, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(2, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        moves.add("b1c3");
        moves.add("b8a6");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move));
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        moves.add("c3b1");
        moves.add("a6b8");

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move));
        }

        assertEquals(3, board.previousOccurrencesOfThisPosition());

    }

    private void moveKnightsBackAndForth(EngineBoard board, List<String> moves) throws IllegalFenException, InvalidMoveException {
        moves.add("b1c3");
        moves.add("b8c6");
        moves.add("c3b1");
        moves.add("c6b8");

        board.setBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        for (String move : moves) {
            board.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move));
        }
    }

    @Test
    public void isGameOver() throws IllegalFenException, InvalidMoveException {

        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 w - - 0 4";
        final String NOT_STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 b - - 0 4";

        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));
        
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertFalse(engineBoard.isGameOver());

        engineBoard.setBoard(FenUtils.getBoardModel(SCHOLARS_MATE));
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertTrue(engineBoard.isGameOver());

        engineBoard.setBoard(FenUtils.getBoardModel(STALEMATE));
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertTrue(engineBoard.isGameOver());

        engineBoard.setBoard(FenUtils.getBoardModel(NOT_STALEMATE));
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertFalse(engineBoard.isGameOver());
    }

    @Test
    public void getSquareOccupant() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        assertEquals(SquareOccupant.WK, engineBoard.getSquareOccupant(3));
        assertEquals(SquareOccupant.WQ, engineBoard.getSquareOccupant(4));
        assertEquals(SquareOccupant.BQ, engineBoard.getSquareOccupant(60));
        assertEquals(SquareOccupant.BK, engineBoard.getSquareOccupant(59));

        assertEquals(SquareOccupant.NONE, engineBoard.getSquareOccupant(32));

    }

    @Test
    public void getPiece() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        assertEquals(Piece.KING, BoardExtensionsKt.getPiece(engineBoard, 3));
        assertEquals(Piece.QUEEN, BoardExtensionsKt.getPiece(engineBoard, 4));
        assertEquals(Piece.KING, BoardExtensionsKt.getPiece(engineBoard, 59));
        assertEquals(Piece.QUEEN, BoardExtensionsKt.getPiece(engineBoard, 60));

        assertEquals(Piece.NONE, BoardExtensionsKt.getPiece(engineBoard, 32));
    }

    @Test
    public void isCapture() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(OpeningLibrary.E2E4_D7D5));
        assertTrue(BoardExtensionsKt.isCapture(engineBoard, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e4d5").compact));
        assertFalse(BoardExtensionsKt.isCapture(engineBoard, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e4e5").compact));
        engineBoard.setBoard(FenUtils.getBoardModel("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6"));
        assertTrue(BoardExtensionsKt.isCapture(engineBoard, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e5d6").compact));
        assertFalse(BoardExtensionsKt.isCapture(engineBoard, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e5e6").compact));
    }

    @Test
    public void isCheck() throws IllegalFenException {
        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String SILLY_CHECK = "rnbqkbnr/pppp1ppp/8/8/8/8/PPPP1PPP/RNBKQBNR b KQkq - 0 4";
        final String STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 w - - 0 4";

        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertFalse(BoardExtensionsKt.isCheck(engineBoard));

        engineBoard.setBoard(FenUtils.getBoardModel(SILLY_CHECK));
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertTrue(BoardExtensionsKt.isCheck(engineBoard));

        engineBoard.setBoard(FenUtils.getBoardModel(SCHOLARS_MATE));
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertTrue(BoardExtensionsKt.isCheck(engineBoard));

        engineBoard.setBoard(FenUtils.getBoardModel(STALEMATE));
        engineBoard.setLegalMoves(new int[Limit.MAX_LEGAL_MOVES.getValue()]);
        assertFalse(BoardExtensionsKt.isCheck(engineBoard));
    }

    @Test
    public void getWhitePieceValues() throws IllegalFenException, InvalidMoveException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        assertEquals(
                pieceValue(Piece.QUEEN)
                + pieceValue(Piece.KNIGHT) * 2
                + pieceValue(Piece.BISHOP) * 2
                + pieceValue(Piece.ROOK) * 2,
                engineBoard.getWhitePieceValues());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e2e4"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d7d6"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e7e5"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("f3e5"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d6e5"));

        assertEquals(
                pieceValue(Piece.QUEEN)
                        + pieceValue(Piece.KNIGHT)
                        + pieceValue(Piece.BISHOP) * 2
                        + pieceValue(Piece.ROOK) * 2,
                engineBoard.getWhitePieceValues());

    }

    @Test
    public void getBlackPieceValues() throws IllegalFenException, InvalidMoveException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        assertEquals(
                pieceValue(Piece.QUEEN)
                        + pieceValue(Piece.KNIGHT) * 2
                        + pieceValue(Piece.BISHOP) * 2
                        + pieceValue(Piece.ROOK) * 2,
                engineBoard.getBlackPieceValues());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e2e4"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d7d6"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e7e5"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("f3e5"));
        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d6e5"));

        assertEquals(
                pieceValue(Piece.QUEEN)
                        + pieceValue(Piece.KNIGHT) * 2
                        + pieceValue(Piece.BISHOP) * 2
                        + pieceValue(Piece.ROOK) * 2,
                engineBoard.getBlackPieceValues());

    }

    @Test
    public void getFen() throws IllegalFenException, InvalidMoveException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        assertEquals(ConstantsKt.FEN_START_POS, engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e2e4"));
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d7d6"));
        assertEquals("rnbqkbnr/ppp1pppp/3p4/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3"));
        assertEquals("rnbqkbnr/ppp1pppp/3p4/8/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e7e5"));
        assertEquals("rnbqkbnr/ppp2ppp/3p4/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq e6 0 3", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("f3e5"));
        assertEquals("rnbqkbnr/ppp2ppp/3p4/4N3/4P3/8/PPPP1PPP/RNBQKB1R b KQkq - 0 3", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d6e5"));
        assertEquals("rnbqkbnr/ppp2ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKB1R w KQkq - 0 4", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("c2c4"));
        assertEquals("rnbqkbnr/ppp2ppp/8/4p3/2P1P3/8/PP1P1PPP/RNBQKB1R b KQkq c3 0 4", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b7b5"));
        assertEquals("rnbqkbnr/p1p2ppp/8/1p2p3/2P1P3/8/PP1P1PPP/RNBQKB1R w KQkq b6 0 5", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("f1e2"));
        assertEquals("rnbqkbnr/p1p2ppp/8/1p2p3/2P1P3/8/PP1PBPPP/RNBQK2R b KQkq - 1 5", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b5b4"));
        assertEquals("rnbqkbnr/p1p2ppp/8/4p3/1pP1P3/8/PP1PBPPP/RNBQK2R w KQkq - 0 6", engineBoard.getFen());

        engineBoard.makeMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e1f1"));
        assertEquals("rnbqkbnr/p1p2ppp/8/4p3/1pP1P3/8/PP1PBPPP/RNBQ1K1R b kq - 1 6", engineBoard.getFen());
    }

}
