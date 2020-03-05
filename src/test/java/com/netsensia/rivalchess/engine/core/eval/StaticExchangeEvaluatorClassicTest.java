package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.constants.Piece;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluationHelper.getIndexOfNextDirectionAttackerAfterIndex;
import static com.netsensia.rivalchess.engine.core.eval.StaticExchangeEvaluationHelper.getScoreFromCaptureList;

public class StaticExchangeEvaluatorClassicTest extends TestCase {

    private StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluatorClassic();

    public void assertSeeScore (final String fen, final String move, final int expectedScore) throws InvalidMoveException {
        EngineChessBoard engineChessBoard = new EngineChessBoard(FenUtils.getBoardModel(fen));
        assertEquals(expectedScore, staticExchangeEvaluator.staticExchangeEvaluation(
                engineChessBoard,
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move)));
    }

    @Test
    public void testStaticExchangeEvaluation() throws IllegalFenException, InvalidMoveException {
        assertSeeScore("4k3/p1pprpb1/bnr1p3/3QN1n1/1p1NP1p1/7p/PPPBBPPP/R3K2R w KQ - 0 1", "d5e6", Piece.PAWN.getValue() - Piece.QUEEN.getValue());

        assertSeeScore("4k2r/p1ppqpb1/bnr1p3/3PN1n1/1p2P1p1/2N2Q1p/PPPBBPPP/R3K2R w KQk - 0 1", "d5e6", 0);

        assertSeeScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -",  "f7e6", Piece.BISHOP.getValue());
        assertSeeScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2f1", Piece.KNIGHT.getValue() - Piece.PAWN.getValue());

        // leaves king in check
        assertSeeScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "h6c1", -RivalConstants.INFINITY);

        assertSeeScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "e5f6", 0);
        assertSeeScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4", Piece.PAWN.getValue());
        assertSeeScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "e5d7", Piece.PAWN.getValue() - Piece.KNIGHT.getValue());
        assertSeeScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "f3f6", Piece.KNIGHT.getValue() - Piece.QUEEN.getValue());
    }

    private void confirmNoAttackersInDirection(EngineChessBoard board, int bitRef, int direction) {
        for (int i=0; i<=6; i++) {
            assertEquals(-1, getIndexOfNextDirectionAttackerAfterIndex(board, bitRef, direction, i));
        }
    }

    private void confirmNoAttackers(EngineChessBoard board, int bitRef) {
        for (int direction=0; direction<=7; direction++) {
            confirmNoAttackersInDirection(board, bitRef, direction);
        }
    }

    private void confirmNoAttackersInDirections(EngineChessBoard board, int bitRef, List<Integer> directions) {
        for (Integer direction : directions) {
            confirmNoAttackersInDirection(board, bitRef, direction);
        }
    }

    private void confirmIndexesContainingAttackerAfterIndex(EngineChessBoard board, int bitRef, int direction, List<Integer> expectedIndexes) {

        for (int i=0; i<expectedIndexes.size(); i++) {
            assertEquals(expectedIndexes.get(i).intValue(), getIndexOfNextDirectionAttackerAfterIndex(board, bitRef, direction, i));
        }
    }

    public void testGetIndexOfNextDirectionAttackerAfterIndex() {
        EngineChessBoard board = new EngineChessBoard(FenUtils.getBoardModel("4k3/p1pprpb1/bnr1p3/3QN1n1/1p1NP1p1/7p/PPPBBPPP/R3K2R w KQ - 0 1"));

        confirmNoAttackersInDirections(board, 63, Arrays.asList(0,3,4,5,6,7));
        confirmIndexesContainingAttackerAfterIndex(board, 63, 1, Arrays.asList(-1,-1,3,-1,-1,-1,-1));
        confirmIndexesContainingAttackerAfterIndex(board, 63, 2, Arrays.asList(-1,-1,-1,-1,-1,-1,7));

        confirmNoAttackersInDirections(board, 28, Arrays.asList(0,1,2,3,4,5));
        confirmIndexesContainingAttackerAfterIndex(board, 28, 6, Arrays.asList(1,-1,-1,-1));
        confirmIndexesContainingAttackerAfterIndex(board, 28, 7, Arrays.asList(-1,3,3,-1,-1));

        confirmNoAttackersInDirections(board, 36, Arrays.asList(0,2,3,4,5,6));
        confirmIndexesContainingAttackerAfterIndex(board, 36, 1, Arrays.asList(1,-1,-1,-1));
        confirmIndexesContainingAttackerAfterIndex(board, 36, 7, Arrays.asList(1,-1,-1));
    }

    @Test
    public void testGetScoreFromCaptureList() {
        // Qxr, nxQ, Pxn, rxP - white wins the rook, black takes queen with knight, white takes knight with pawn, black takes pawn with rook
        assertEquals(-100, getScoreFromCaptureList(Arrays.asList(500, 900, 300, 100, 500)));

        // Qxr, nxQ, Pxn - white wins the rook, black takes queen with knight, white takes knight with pawn
        assertEquals(-200, getScoreFromCaptureList(Arrays.asList(500, 900, 300, 100)));

        // Pxr, qxP, Pxq, rxP - white wins the rook, no follow ups expected
        assertEquals(500, getScoreFromCaptureList(Arrays.asList(500, 100, 900, 100)));

        // Pxr, qxP, Pxq - white wins the rook, no follow ups expected
        assertEquals(500, getScoreFromCaptureList(Arrays.asList(500, 100, 900)));

        // Qxr, pxQ - white wins the rook, black takes queen with pawn
        assertEquals(-300, getScoreFromCaptureList(Arrays.asList(500, 900, 100)));

        // Qxr, nxQ - white wins the rook, black takes queen with knight
        assertEquals(-100, getScoreFromCaptureList(Arrays.asList(500, 900, 300)));

    }
}