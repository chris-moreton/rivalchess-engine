package com.netsensia.rivalchess.engine;

import com.netsensia.rivalchess.consts.BitboardsKt;
import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.util.FenUtils;
import org.junit.Assert;
import org.junit.Test;
import com.netsensia.rivalchess.openings.OpeningLibrary;

import java.util.ArrayList;
import java.util.List;

import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_BK;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_BQ;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_NONE;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WB;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WK;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WN;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WQ;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WR;
import static com.netsensia.rivalchess.consts.GameKt.FEN_START_POS;
import static com.netsensia.rivalchess.engine.board.MoveMakingBoardExtensionsKt.makeMove;
import static com.netsensia.rivalchess.engine.eval.PieceValueKt.pieceValue;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EngineBoardTest {

    @Test
    public void canRecognisePreviousPositions() throws IllegalFenException {
        final EngineBoard board = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        final List<String> moves = new ArrayList<>();

        moves.add("e2e4");
        moves.add("e7e5");

        board.setBoard(FenUtils.getBoardModel(FEN_START_POS));

        for (String move : moves) {
            makeMove(board, getEngineMoveFromSimpleAlgebraic(move).compact, false, true);
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(1, board.previousOccurrencesOfThisPosition());

        moveKnightsBackAndForth(board, moves);

        assertEquals(2, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(FEN_START_POS));

        moves.add("b1c3");
        moves.add("b8a6");

        for (String move : moves) {
            makeMove(board, getEngineMoveFromSimpleAlgebraic(move).compact, false, true);
        }

        assertEquals(0, board.previousOccurrencesOfThisPosition());

        board.setBoard(FenUtils.getBoardModel(FEN_START_POS));

        moves.add("c3b1");
        moves.add("a6b8");

        for (String move : moves) {
            makeMove(board, getEngineMoveFromSimpleAlgebraic(move).compact, false, true);
        }

        assertEquals(3, board.previousOccurrencesOfThisPosition());

    }

    private void moveKnightsBackAndForth(EngineBoard board, List<String> moves) throws IllegalFenException {
        moves.add("b1c3");
        moves.add("b8c6");
        moves.add("c3b1");
        moves.add("c6b8");

        board.setBoard(FenUtils.getBoardModel(FEN_START_POS));

        for (String move : moves) {
            makeMove(board, getEngineMoveFromSimpleAlgebraic(move).compact, false, true);
        }
    }

    @Test
    public void isGameOver() throws IllegalFenException {

        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String STALEMATE = "k7/6b1/8/8/8/n7/PP6/K7 w - - 0 4";
        final String NOT_STALEMATE = "8/6k1/8/8/8/n7/PP6/K7 b - - 0 4";

        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));
        
        assertFalse(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isGameOver(engineBoard));

        engineBoard.setBoard(FenUtils.getBoardModel(SCHOLARS_MATE));
        assertTrue(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isGameOver(engineBoard));

        engineBoard.setBoard(FenUtils.getBoardModel(STALEMATE));
        assertTrue(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isGameOver(engineBoard));

        engineBoard.setBoard(FenUtils.getBoardModel(NOT_STALEMATE));
        assertFalse(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isGameOver(engineBoard));
    }

    @Test
    public void getSquareOccupant() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        assertEquals(BITBOARD_WK, engineBoard.getBitboardTypeOfPieceOnSquare(3));
        assertEquals(BITBOARD_WQ, engineBoard.getBitboardTypeOfPieceOnSquare(4));
        assertEquals(BITBOARD_BQ, engineBoard.getBitboardTypeOfPieceOnSquare(60));
        assertEquals(BITBOARD_BK, engineBoard.getBitboardTypeOfPieceOnSquare(59));

        assertEquals(BITBOARD_NONE, engineBoard.getBitboardTypeOfPieceOnSquare(32));

    }

    @Test
    public void getPiece() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        Assert.assertEquals(Piece.KING, com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getPiece(engineBoard, 3));
        Assert.assertEquals(Piece.QUEEN, com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getPiece(engineBoard, 4));
        Assert.assertEquals(Piece.KING, com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getPiece(engineBoard, 59));
        Assert.assertEquals(Piece.QUEEN, com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getPiece(engineBoard, 60));

        Assert.assertEquals(Piece.NONE, com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getPiece(engineBoard, 32));
    }

    @Test
    public void isCapture() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(OpeningLibrary.E2E4_D7D5));
        assertTrue(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCapture(engineBoard, getEngineMoveFromSimpleAlgebraic("e4d5").compact));
        assertFalse(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCapture(engineBoard, getEngineMoveFromSimpleAlgebraic("e4e5").compact));
        engineBoard.setBoard(FenUtils.getBoardModel("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6"));
        assertTrue(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCapture(engineBoard, getEngineMoveFromSimpleAlgebraic("e5d6").compact));
        assertFalse(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCapture(engineBoard, getEngineMoveFromSimpleAlgebraic("e5e6").compact));
    }

    @Test
    public void isCheck() throws IllegalFenException {
        final String SCHOLARS_MATE = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4";
        final String SILLY_CHECK = "rnbqkbnr/pppp1ppp/8/8/8/8/PPPP1PPP/RNBKQBNR b KQkq - 0 4";
        final String STALEMATE = "8/6b1/8/8/8/n7/PP6/K7 w - - 0 4";

        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        assertFalse(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCheck(engineBoard, engineBoard.mover));

        engineBoard.setBoard(FenUtils.getBoardModel(SILLY_CHECK));
        assertTrue(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCheck(engineBoard, engineBoard.mover));

        engineBoard.setBoard(FenUtils.getBoardModel(SCHOLARS_MATE));
        assertTrue(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCheck(engineBoard, engineBoard.mover));

        engineBoard.setBoard(FenUtils.getBoardModel(STALEMATE));
        assertFalse(com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isCheck(engineBoard, engineBoard.mover));
    }

    @Test
    public void getWhitePieceValues() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        Assert.assertEquals(
                pieceValue(BITBOARD_WQ),
                + pieceValue(BITBOARD_WN) * 2
                + pieceValue(BITBOARD_WB) * 2
                + pieceValue(BITBOARD_WR) * 2,
                engineBoard.getWhitePieceValues());

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e2e4").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("d7d6").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("g1f3").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e7e5").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("f3e5").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("d6e5").compact, false, true);

        Assert.assertEquals(
                pieceValue(BITBOARD_WQ)
                        + pieceValue(BITBOARD_WN)
                        + pieceValue(BITBOARD_WB) * 2
                        + pieceValue(BITBOARD_WR) * 2,
                engineBoard.getWhitePieceValues());

    }

    @Test
    public void getBlackPieceValues() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        Assert.assertEquals(pieceValue(BITBOARD_WQ) + pieceValue(BITBOARD_WN) * 2 + pieceValue(BITBOARD_WB) * 2 + pieceValue(BITBOARD_WR) * 2, engineBoard.getBlackPieceValues());

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e2e4").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("d7d6").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("g1f3").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e7e5").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("f3e5").compact, false, true);
        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("d6e5").compact, false, true);

        Assert.assertEquals(pieceValue(BITBOARD_WQ) + pieceValue(BITBOARD_WN) * 2 + pieceValue(BITBOARD_WB) * 2 + pieceValue(BITBOARD_WR) * 2, engineBoard.getBlackPieceValues());
    }

    @Test
    public void getFen() throws IllegalFenException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        Assert.assertEquals(FEN_START_POS, com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e2e4").compact, false, true);
        Assert.assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("d7d6").compact, false, true);
        Assert.assertEquals("rnbqkbnr/ppp1pppp/3p4/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("g1f3").compact, false, true);
        Assert.assertEquals("rnbqkbnr/ppp1pppp/3p4/8/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e7e5").compact, false, true);
        Assert.assertEquals("rnbqkbnr/ppp2ppp/3p4/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq e6 0 3", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("f3e5").compact, false, true);
        Assert.assertEquals("rnbqkbnr/ppp2ppp/3p4/4N3/4P3/8/PPPP1PPP/RNBQKB1R b KQkq - 0 3", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("d6e5").compact, false, true);
        Assert.assertEquals("rnbqkbnr/ppp2ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKB1R w KQkq - 0 4", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("c2c4").compact, false, true);
        Assert.assertEquals("rnbqkbnr/ppp2ppp/8/4p3/2P1P3/8/PP1P1PPP/RNBQKB1R b KQkq c3 0 4", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("b7b5").compact, false, true);
        Assert.assertEquals("rnbqkbnr/p1p2ppp/8/1p2p3/2P1P3/8/PP1P1PPP/RNBQKB1R w KQkq b6 0 5", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("f1e2").compact, false, true);
        Assert.assertEquals("rnbqkbnr/p1p2ppp/8/1p2p3/2P1P3/8/PP1PBPPP/RNBQK2R b KQkq - 1 5", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("b5b4").compact, false, true);
        Assert.assertEquals("rnbqkbnr/p1p2ppp/8/4p3/1pP1P3/8/PP1PBPPP/RNBQK2R w KQkq - 0 6", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));

        makeMove(engineBoard, getEngineMoveFromSimpleAlgebraic("e1f1").compact, false, true);
        Assert.assertEquals("rnbqkbnr/p1p2ppp/8/4p3/1pP1P3/8/PP1PBPPP/RNBQ1K1R b kq - 1 6", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(engineBoard));
    }

}
