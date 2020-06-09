package com.netsensia.rivalchess.engine.core.bitboards;

import com.netsensia.rivalchess.bitboards.EngineBitboards;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.model.util.FenUtils;
import org.junit.Test;

import java.math.BigInteger;

import static com.netsensia.rivalchess.engine.core.ConstantsKt.BITBOARD_WB;
import static com.netsensia.rivalchess.engine.core.ConstantsKt.BITBOARD_WQ;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getBitRefFromBoardRef;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic;
import static org.junit.Assert.assertEquals;

public class EngineBitboardsTest {

    private EngineBitboards bitboards = new EngineBitboards();
    
    @Test
    public void testGetAndSetPieceBitboardsByIndex() {
        bitboards.setPieceBitboard(BITBOARD_WQ, 829282L);
        assertEquals(829282L, bitboards.getPieceBitboard(BITBOARD_WQ));
    }

    @Test
    public void testXorPieceBitboard() {
        bitboards.setPieceBitboard(BITBOARD_WQ, 829282L);
        bitboards.xorPieceBitboard(BITBOARD_WQ, 817222323L);
        assertEquals(829282L ^ 817222323L, bitboards.getPieceBitboard(BITBOARD_WQ));
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
        bitboards.movePiece(SquareOccupant.WB, engineMove.compact);

        assertEquals(bitboardExpected, bitboards.getPieceBitboard(BITBOARD_WB));
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
        
        for (int y=0; y<8; y++) {
            for (int x=0; x<8; x++) {

                assertEquals(expectedWhiteAttacks[y][x], engineBoard.getEngineBitboards().isSquareAttackedBy(
                        getBitRefFromBoardRef(Square.fromCoords(x, y)),
                        Colour.WHITE));

                assertEquals(expectedBlackAttacks[y][x], engineBoard.getEngineBitboards().isSquareAttackedBy(
                        getBitRefFromBoardRef(Square.fromCoords(x, y)),
                        Colour.BLACK));
            }
        }
    }
}