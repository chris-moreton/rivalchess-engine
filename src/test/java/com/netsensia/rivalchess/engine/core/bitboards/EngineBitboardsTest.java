package com.netsensia.rivalchess.engine.core.bitboards;

import com.netsensia.rivalchess.bitboards.EngineBitboards;
import com.netsensia.rivalchess.bitboards.BitboardType;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class EngineBitboardsTest {

    @Test
    public void testGetAndSetPieceBitboardsByIndex() {
        EngineBitboards engineBitboards = EngineBitboards.getInstance();
        engineBitboards.setPieceBitboard(BitboardType.WQ, 829282L);
        assertEquals(829282L, engineBitboards.getPieceBitboard(BitboardType.WQ));
    }

    @Test
    public void testXorPieceBitboard() {
        EngineBitboards engineBitboards = EngineBitboards.getInstance();
        engineBitboards.setPieceBitboard(BitboardType.WQ, 829282L);
        engineBitboards.xorPieceBitboard(BitboardType.WQ, 817222323L);
        assertEquals(829282L ^ 817222323L, engineBitboards.getPieceBitboard(BitboardType.WQ));
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

        EngineBitboards engineBitboards = EngineBitboards.getInstance();
        engineBitboards.setPieceBitboard(BitboardType.WB, bitboard);
        engineBitboards.movePiece(SquareOccupant.WB, engineMove.compact);

        assertEquals(bitboardExpected, engineBitboards.getPieceBitboard(BitboardType.WB));
    }

    @Test
    public void isSquareAttacked() throws IllegalFenException {
        final EngineBoard engineBoard =
                new EngineBoard(FenUtils.getBoardModel("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -"));

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

        EngineBitboards engineBitboards = engineBoard.engineBitboards;

        for (int y=0; y<8; y++) {
            for (int x=0; x<8; x++) {

                assertEquals(expectedWhiteAttacks[y][x], engineBitboards.isSquareAttackedBy(
                        ChessBoardConversion.getBitRefFromBoardRef(Square.fromCoords(x, y)),
                        Colour.WHITE));

                assertEquals(expectedBlackAttacks[y][x], engineBitboards.isSquareAttackedBy(
                        ChessBoardConversion.getBitRefFromBoardRef(Square.fromCoords(x, y)),
                        Colour.BLACK));
            }
        }
    }
}