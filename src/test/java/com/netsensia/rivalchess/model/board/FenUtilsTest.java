package com.netsensia.rivalchess.model.board;

import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.exception.IllegalFenException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FenUtilsTest {
    @Test
    public void testGetChessBoard() throws IllegalFenException {

        BoardModel boardModel = FenUtils.getBoardModel("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56");

        assertEquals(boardModel.getPieceCode(new BoardRef(0,0)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,1)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,2)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,3)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,4)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,5)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,6)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(0,7)), '_');

        assertEquals(boardModel.getPieceCode(new BoardRef(1,7)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,6)), 'r');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,5)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,4)), 'P');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,3)), 'p');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,2)), 'p');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,1)), '_');
        assertEquals(boardModel.getPieceCode(new BoardRef(1,0)), '_');

        assertTrue(boardModel.isBlackToMove());
        assertTrue(boardModel.getEnPassantFile() == 6);
    }
}
