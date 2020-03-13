package com.netsensia.rivalchess.engine.core.type;

import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineMoveTest {

    @Test
    public void testFromMoveWithoutPromotion() {
        EngineMove engineMove = new EngineMove(new Move(0,0,7, 7));
        assertEquals("a8h1", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackQueenPromotion() {
        EngineMove engineMove = new EngineMove(new Move(5,6,5, 7, SquareOccupant.BQ));
        assertEquals("f2f1q", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteQueenPromotion() {
        EngineMove engineMove = new EngineMove(new Move(0,1,0, 0, SquareOccupant.WQ));
        assertEquals("a7a8Q", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackKnightPromotion() {
        EngineMove engineMove = new EngineMove(new Move(5,6,5, 7, SquareOccupant.BN));
        assertEquals("f2f1n", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteKnightPromotion() {
        EngineMove engineMove = new EngineMove(new Move(0, 1, 0, 0, SquareOccupant.WN));
        assertEquals("a7a8N", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackBishopPromotion() {
        EngineMove engineMove = new EngineMove(new Move(5,6,5, 7, SquareOccupant.BB));
        assertEquals("f2f1b", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteBishopPromotion() {
        EngineMove engineMove = new EngineMove(new Move(0, 1, 0, 0, SquareOccupant.WB));
        assertEquals("a7a8B", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackRookPromotion() {
        EngineMove engineMove = new EngineMove(new Move(5,6,5, 7, SquareOccupant.BR));
        assertEquals("f2f1r", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteRookPromotion() {
        EngineMove engineMove = new EngineMove(new Move(0, 1, 0, 0, SquareOccupant.WR));
        assertEquals("a7a8R", ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }
}