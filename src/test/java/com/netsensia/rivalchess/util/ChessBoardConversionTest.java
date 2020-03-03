package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.Square;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ChessBoardConversionTest {

    @Test
    public void getBoardRefFromBitRef() {
        assertEquals(new Square(7, 7), ChessBoardConversion.getBoardRefFromBitRef(0));
        assertEquals(new Square(0, 7), ChessBoardConversion.getBoardRefFromBitRef(7));
        assertEquals(new Square(7, 0), ChessBoardConversion.getBoardRefFromBitRef(56));
        assertEquals(new Square(0, 0), ChessBoardConversion.getBoardRefFromBitRef(63));
    }

    @Test
    public void getBitRefFromBoardRef() {
        assertEquals(0, ChessBoardConversion.getBitRefFromBoardRef(new Square(7, 7)));
        assertEquals(7, ChessBoardConversion.getBitRefFromBoardRef(new Square(0, 7)));
        assertEquals(56, ChessBoardConversion.getBitRefFromBoardRef(new Square(7, 0)));
        assertEquals(63, ChessBoardConversion.getBitRefFromBoardRef(new Square(0, 0)));
    }

    @Test
    public void getMoveRefFromEngineMove() {
        assertEquals(new Move(new Square(4, 6), new Square(4, 4)),
                ChessBoardConversion.getMoveRefFromEngineMove(720923));

        assertNotEquals(new Move(new Square(1, 1), new Square(1, 0)),
                ChessBoardConversion.getMoveRefFromEngineMove(3539198));

        Move move1 = new Move(new Square(1, 1), new Square(1, 0));
        move1.setPromotedPieceCode("Q");
        assertEquals(move1, ChessBoardConversion.getMoveRefFromEngineMove(3539198));

        Move move2 = new Move(new Square(0, 6), new Square(1, 7));
        move2.setPromotedPieceCode("n");
        assertEquals(move2, ChessBoardConversion.getMoveRefFromEngineMove(983302));
    }

    @Test
    public void getSimpleAlgebraicMoveFromCompactMove() {
        assertEquals("e2e4", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(720923));
        assertEquals("b7b8Q", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(3539198));
        assertEquals("a1a2", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(458767));
        assertEquals("a2b1n", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(983302));
        assertEquals("b7b8", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(3539006));
        assertEquals("a1h8", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(458808));
        assertEquals("g1f3", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(65554));
        assertEquals("d4d5", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(1835044));
        assertEquals("c7b8N", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(3473726));
        assertEquals("b7b8N", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(3539262));
    }

    @Test
    public void getSimpleAlgebraicFromBitRef() {
        assertEquals("h1", ChessBoardConversion.getSimpleAlgebraicFromBitRef(0));
        assertEquals("g1", ChessBoardConversion.getSimpleAlgebraicFromBitRef(1));
        assertEquals("a8", ChessBoardConversion.getSimpleAlgebraicFromBitRef(63));
    }

    @Test
    public void getPgnMoveFromCompactMove() throws IllegalFenException, InvalidMoveException {
        assertEquals("e4",
                ChessBoardConversion.getPgnMoveFromCompactMove(720923, RivalConstants.FEN_START_POS));
        assertEquals("Nf3",
                ChessBoardConversion.getPgnMoveFromCompactMove(65554, RivalConstants.FEN_START_POS));
        assertEquals("cxb8=N",
                ChessBoardConversion.getPgnMoveFromCompactMove(
                        3473726, "rnbqkb1r/ppP1pppp/21p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -"));
    }

    @Test(expected = InvalidMoveException.class)
    public void getPgnMoveFromCompactMoveIllegalMoveException() throws IllegalFenException, InvalidMoveException {
        assertEquals("Nf3",
                ChessBoardConversion.getPgnMoveFromCompactMove(1835044, RivalConstants.FEN_START_POS));
    }

    @Test
    public void getCompactMoveFromSimpleAlgebraic() {
        assertEquals(720923, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e2e4").compact);
        assertEquals(3539198, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b7b8q").compact);
        assertEquals(3539198, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b7b8Q").compact);
        assertEquals(3539262, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b7b8N").compact);
        assertEquals(458767, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("a1a2").compact);
        assertEquals(983302, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("a2b1n").compact);
        assertEquals(3539006, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("b7b8").compact);
        assertEquals(458808, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("a1h8").compact);
        assertEquals(65554, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("g1f3").compact);
        assertEquals(1835044, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("d4d5").compact);
        assertEquals(3473726, ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("c7b8n").compact);

    }
}
