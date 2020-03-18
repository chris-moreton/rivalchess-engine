package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FenUtilsTest {
    @Test
    public void testGetChessBoard() throws IllegalFenException {

        Board board = FenUtils.getBoardModel("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56");

        assertEquals(board.getSquareOccupant(Square.fromCoords(0,0)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,1)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,2)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,3)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,4)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,5)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,6)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(0,7)), SquareOccupant.NONE);

        assertEquals(board.getSquareOccupant(Square.fromCoords(1,7)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,6)), SquareOccupant.BR);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,5)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,4)), SquareOccupant.WP);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,3)), SquareOccupant.BP);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,2)), SquareOccupant.BP);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,1)), SquareOccupant.NONE);
        assertEquals(board.getSquareOccupant(Square.fromCoords(1,0)), SquareOccupant.NONE);

        assertTrue(board.getSideToMove() == Colour.BLACK);
        assertEquals(6, board.getEnPassantFile());
    }

    @Test
    public void testInvertFen() throws IllegalFenException {
        String actual = FenUtils.invertFen("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56");
        assertEquals("8/1R2n3/2pk1b2/1p3rp1/1P5p/1P2Q2P/6P1/6K1 w - b6 5 56", actual);
    }
}