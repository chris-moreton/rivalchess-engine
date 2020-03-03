package com.netsensia.rivalchess.engine.core.bitboards;

import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.util.ChessBoardConversion;
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
}