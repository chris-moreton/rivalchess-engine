package com.netsensia.rivalchess.engine.core.bitboards;

import com.netsensia.rivalchess.constants.Colour;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class EngineBitboardsTest {

    @Test
    public void testGetAndSetPieceBitboardsByIndex() {
        EngineBitboards engineBitboards = new EngineBitboards();
        engineBitboards.setPieceBitboard(3, 829282L);
        assertEquals(829282L, engineBitboards.getPieceBitboard(3));
    }

    @Test
    public void testXorPieceBitboard() {
        EngineBitboards engineBitboards = new EngineBitboards();
        engineBitboards.setPieceBitboard(3, 829282L);
        engineBitboards.xorPieceBitboard(3, 817222323L);
        assertEquals(829282L ^ 817222323L, engineBitboards.getPieceBitboard(3));
    }

    @Test
    public void testMovePiece() {
        final String bitboardString =
                "00000000" +
                "00000000" +
                "00000000" +
                "10000100" +
                "00000101" +
                "00000000" +
                "00000000" +
                "00000000";

        final String bitboardStringExpected =
                "00010000" +
                "00000000" +
                "00000000" +
                "10000100" +
                "00000100" +
                "00000000" +
                "00000000" +
                "00000000";

        final long bitboard = new BigInteger(bitboardString, 2).longValue();
        final long bitboardExpected = new BigInteger(bitboardStringExpected, 2).longValue();

        final EngineMove engineMove = ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h4d8");

        EngineBitboards engineBitboards = new EngineBitboards();
        engineBitboards.setPieceBitboard(RivalConstants.WB, bitboard);
        engineBitboards.movePiece(RivalConstants.WB, engineMove.compact);

        assertEquals(bitboardExpected, engineBitboards.getPieceBitboard(RivalConstants.WB));
    }

    @Test
    public void isSquareAttacked() throws IllegalFenException {
        final EngineChessBoard engineChessBoard =
                new EngineChessBoard(FenUtils.getBoardModel("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -"));

        boolean[][] expectedWhiteAttacks = {
                {false,false,true,false,false,false,false,false},
                {false,false,false,true,false,false,false,true},
                {false,true,false,true,true,true,true,false},
                {false,false,true,true,true,false,true,true},
                {false,false,true,false,true,true,true,false},
                {false,false,true,false,true,true,true,true},
                {false,false,false,true,true,true,true,false},
                {false,false,false,false,false,false,false,false}
        };

        boolean[][] expectedBlackAttacks = {
                {false,false,false,false,false,true,false,true},
                {false,false,false,false,false,true,true,true},
                {false,true,false,false,false,true,true,true},
                {false,false,false,false,true,false,true,false},
                {false,false,false,true,false,true,true,false},
                {false,false,true,false,true,false,true,false},
                {false,false,false,true,false,true,true,true},
                {true,true,true,true,true,true,true,true}
        };

        EngineBitboards engineBitboards = engineChessBoard.getEngineBitboards();

        for (int y=0; y<8; y++) {
            for (int x=0; x<8; x++) {

                assertEquals(expectedWhiteAttacks[y][x], engineBitboards.isSquareAttackedBy(
                        ChessBoardConversion.getBitRefFromBoardRef(new Square(x, y)),
                        Colour.WHITE));

                assertEquals(expectedBlackAttacks[y][x], engineBitboards.isSquareAttackedBy(
                        ChessBoardConversion.getBitRefFromBoardRef(new Square(x, y)),
                        Colour.BLACK));
            }
        }
    }
}