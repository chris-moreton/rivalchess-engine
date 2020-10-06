package com.netsensia.rivalchess.engine.bitboards;

import com.netsensia.rivalchess.bitboards.EngineBitboards;
import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.engine.type.EngineMove;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.engine.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;
import org.junit.Test;

import java.math.BigInteger;

import static com.netsensia.rivalchess.consts.BitboardsKt.*;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getBitRefFromBoardRef;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic;
import static org.junit.Assert.assertEquals;

public class EngineBitboardsTest {

    private EngineBitboards bitboards = new EngineBitboards();

    @Test
    public void testGetAndSetPieceBitboardsByIndex() {
        bitboards.setPieceBitboard(BITBOARD_WQ, 829282L);
        assertEquals(829282L, bitboards.pieceBitboards[BITBOARD_WQ]);
    }

    @Test
    public void testXorPieceBitboard() {
        bitboards.setPieceBitboard(BITBOARD_WQ, 829282L);
        bitboards.xorPieceBitboard(BITBOARD_WQ, 817222323L);
        assertEquals(829282L ^ 817222323L, bitboards.pieceBitboards[BITBOARD_WQ]);
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

        final EngineMove engineMove = getEngineMoveFromSimpleAlgebraic("h4d8");

        bitboards.setPieceBitboard(BITBOARD_WB, bitboard);
        bitboards.movePiece(BITBOARD_WB, engineMove.compact);

        assertEquals(bitboardExpected, bitboards.pieceBitboards[BITBOARD_WB]);
    }

    @Test
    public void isSquareAttacked() throws IllegalFenException {
        final EngineBoard engineBoard =
                new EngineBoard(FenUtils.getBoardModel("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -"));

        boolean[][] expectedWhiteAttacks = {
                {false, false, true, false, false, false, false, false},
                {false, false, false, true, false, false, false, true},
                {false, true, false, true, true, true, true, false},
                {false, false, true, true, true, false, true, true},
                {false, false, true, false, true, true, true, false},
                {false, false, true, false, true, true, true, true},
                {false, false, false, true, true, true, true, false},
                {false, false, false, false, false, false, false, false}
        };

        boolean[][] expectedBlackAttacks = {
                {false, false, false, false, false, true, false, true},
                {false, false, false, false, false, true, true, true},
                {false, true, false, false, false, true, true, true},
                {false, false, false, false, true, false, true, false},
                {false, false, false, true, false, true, true, false},
                {false, false, true, false, true, false, true, false},
                {false, false, false, true, false, true, true, true},
                {true, true, true, true, true, true, true, true}
        };

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {

                assertEquals(expectedWhiteAttacks[y][x], engineBoard.engineBitboards.isSquareAttackedBy(
                        getBitRefFromBoardRef(Square.fromCoords(x, y)),
                        Colour.WHITE));

                assertEquals(expectedBlackAttacks[y][x], engineBoard.engineBitboards.isSquareAttackedBy(
                        getBitRefFromBoardRef(Square.fromCoords(x, y)),
                        Colour.BLACK));
            }
        }
    }

    @Test
    public void testBitboardValues() throws IllegalFenException {
        final EngineBoard engineBoard =
                new EngineBoard(FenUtils.getBoardModel("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56"));

        assertEquals(6.34693087133696E14, engineBoard.engineBitboards.pieceBitboards[BITBOARD_BP], 0);
        assertEquals(8.796093022208E12, engineBoard.engineBitboards.pieceBitboards[BITBOARD_BQ], 0);
        assertEquals(0.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_BN], 0);
        assertEquals(0.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_BB], 0);
        assertEquals(1.44115188075855872E17, engineBoard.engineBitboards.pieceBitboards[BITBOARD_BK], 0);
        assertEquals(16384.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_BR], 0);
        assertEquals(5.404360704E9, engineBoard.engineBitboards.pieceBitboards[BITBOARD_WP], 0);
        assertEquals(0.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_WQ], 0);
        assertEquals(2048.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_WN], 0);
        assertEquals(262144.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_WB], 0);
        assertEquals(1048576.0, engineBoard.engineBitboards.pieceBitboards[BITBOARD_WK], 0);
        assertEquals(6.7108864E7, engineBoard.engineBitboards.pieceBitboards[BITBOARD_WR], 0);


    }
}