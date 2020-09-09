package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.consts.BitboardsKt;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import org.junit.Test;

import static com.netsensia.rivalchess.consts.GameKt.FEN_START_POS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ChessBoardConversionTest {

    @Test
    public void getBoardRefFromBitRef() {
        assertEquals(Square.fromCoords(7, 7), Square.fromBitRef(0));
        assertEquals(Square.fromCoords(0, 7), Square.fromBitRef(7));
        assertEquals(Square.fromCoords(7, 0), Square.fromBitRef(56));
        assertEquals(Square.fromCoords(0, 0), Square.fromBitRef(63));
    }

    @Test
    public void getBitRefFromBoardRef() {
        assertEquals(0, ChessBoardConversionKt.getBitRefFromBoardRef(Square.fromCoords(7, 7)));
        assertEquals(7, ChessBoardConversionKt.getBitRefFromBoardRef(Square.fromCoords(0, 7)));
        assertEquals(56, ChessBoardConversionKt.getBitRefFromBoardRef(Square.fromCoords(7, 0)));
        assertEquals(63, ChessBoardConversionKt.getBitRefFromBoardRef(Square.fromCoords(0, 0)));
    }

    @Test
    public void getMoveRefFromEngineMove() {
        assertEquals(new Move(Square.fromCoords(4, 6), Square.fromCoords(4, 4)),
                ChessBoardConversionKt.getMoveRefFromCompactMove(720923));

        assertNotEquals(new Move(Square.fromCoords(1, 1), Square.fromCoords(1, 0)),
                ChessBoardConversionKt.getMoveRefFromCompactMove(3539198));

        Move move1 = new Move(Square.fromCoords(1, 1), Square.fromCoords(1, 0), SquareOccupant.WQ);
        assertEquals(move1, ChessBoardConversionKt.getMoveRefFromCompactMove(3539198));

        Move move2 = new Move(Square.fromCoords(0, 6), Square.fromCoords(1, 7), SquareOccupant.BN);
        assertEquals(move2, ChessBoardConversionKt.getMoveRefFromCompactMove(983302));
    }

    @Test
    public void getSimpleAlgebraicMoveFromCompactMove() {
        assertEquals("e2e4", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(720923));
        assertEquals("b7b8Q", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(3539198));
        assertEquals("a1a2", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(458767));
        assertEquals("a2b1n", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(983302));
        assertEquals("b7b8", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(3539006));
        assertEquals("a1h8", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(458808));
        assertEquals("g1f3", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(65554));
        assertEquals("d4d5", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(1835044));
        assertEquals("c7b8N", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(3473726));
        assertEquals("b7b8N", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(3539262));
    }

    @Test
    public void getSimpleAlgebraicFromBitRef() {
        assertEquals("h1", ChessBoardConversionKt.getSimpleAlgebraicFromBitRef(0));
        assertEquals("g1", ChessBoardConversionKt.getSimpleAlgebraicFromBitRef(1));
        assertEquals("a8", ChessBoardConversionKt.getSimpleAlgebraicFromBitRef(63));
    }

    @Test
    public void getPgnMoveFromCompactMove() throws IllegalFenException {
        assertEquals("e4",
                ChessBoardConversionKt.getPgnMoveFromCompactMove(720923, FEN_START_POS));
        assertEquals("Nf3",
                ChessBoardConversionKt.getPgnMoveFromCompactMove(65554, FEN_START_POS));
        assertEquals("cxb8=N",
                ChessBoardConversionKt.getPgnMoveFromCompactMove(
                        3473726, "rnbqkb1r/ppP1pppp/21p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq -"));
    }

    @Test
    public void getCompactMoveFromSimpleAlgebraic() {
        assertEquals(720923, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("e2e4").compact);
        assertEquals(3539198, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b7b8q").compact);
        assertEquals(3539198, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b7b8Q").compact);
        assertEquals(3539262, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b7b8N").compact);
        assertEquals(458767, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("a1a2").compact);
        assertEquals(983302, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("a2b1n").compact);
        assertEquals(3539006, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("b7b8").compact);
        assertEquals(458808, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("a1h8").compact);
        assertEquals(65554, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("g1f3").compact);
        assertEquals(1835044, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("d4d5").compact);
        assertEquals(3473726, ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic("c7b8n").compact);

    }
}
