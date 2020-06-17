package com.netsensia.rivalchess.engine.type;

import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.util.ChessBoardConversionKt;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineMoveTest {

    @Test
    public void testFromMoveWithoutPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(0,0,7, 7));
        assertEquals("a8h1", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackQueenPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(5,6,5, 7, SquareOccupant.BQ));
        assertEquals("f2f1q", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteQueenPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(0,1,0, 0, SquareOccupant.WQ));
        assertEquals("a7a8Q", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackKnightPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(5,6,5, 7, SquareOccupant.BN));
        assertEquals("f2f1n", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteKnightPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(0, 1, 0, 0, SquareOccupant.WN));
        assertEquals("a7a8N", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackBishopPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(5,6,5, 7, SquareOccupant.BB));
        assertEquals("f2f1b", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteBishopPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(0, 1, 0, 0, SquareOccupant.WB));
        assertEquals("a7a8B", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithBlackRookPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(5,6,5, 7, SquareOccupant.BR));
        assertEquals("f2f1r", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }

    @Test
    public void testFromMoveWithWhiteRookPromotion() {
        com.netsensia.rivalchess.engine.type.EngineMove engineMove = new com.netsensia.rivalchess.engine.type.EngineMove(new Move(0, 1, 0, 0, SquareOccupant.WR));
        assertEquals("a7a8R", ChessBoardConversionKt.getSimpleAlgebraicMoveFromCompactMove(engineMove.compact));
    }
}