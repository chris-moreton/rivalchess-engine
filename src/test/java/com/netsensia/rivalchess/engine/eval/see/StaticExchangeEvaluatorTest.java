package com.netsensia.rivalchess.engine.eval.see;

import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.util.FenUtils;
import junit.framework.TestCase;
import org.junit.Test;

import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WB;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WN;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WP;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WQ;
import static com.netsensia.rivalchess.consts.BitboardsKt.BITBOARD_WR;
import static com.netsensia.rivalchess.engine.eval.PieceValueKt.pieceValue;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic;

public class StaticExchangeEvaluatorTest extends TestCase {

    private com.netsensia.rivalchess.engine.eval.see.StaticExchangeEvaluator staticExchangeEvaluator =
            new com.netsensia.rivalchess.engine.eval.see.StaticExchangeEvaluator();

    public void assertSeeScore (final String fen, final String move, final int expectedScore) {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(fen));
        assertEquals(expectedScore, staticExchangeEvaluator.staticExchangeEvaluation(
                engineBoard,
                getEngineMoveFromSimpleAlgebraic(move).compact));
    }

    @Test
    public void testStaticExchangeEvaluation() throws IllegalFenException {
        assertSeeScore("4k3/p1pprpb1/bnr1p3/3QN1n1/1p1NP1p1/7p/PPPBBPPP/R3K2R w KQ - 0 1", "d5e6",
                pieceValue(BITBOARD_WP) - pieceValue(BITBOARD_WQ));

        // winning rook capture - black rook can't recapture safely
        assertSeeScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4", pieceValue(BITBOARD_WP));

        assertSeeScore("4k2r/p1ppqpb1/bnr1p3/3PN1n1/1p2P1p1/2N2Q1p/PPPBBPPP/R3K2R w KQk - 0 1", "d5e6", 0);

        assertSeeScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", "f7e6", pieceValue(BITBOARD_WB));

        // pawn promotes, king has to take queen
        assertSeeScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2f1",
                pieceValue(BITBOARD_WN) - pieceValue(BITBOARD_WP));

        // leaves king in check
        assertSeeScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "h6c1", -Integer.MAX_VALUE);

        assertSeeScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "e5f6", 0);

        assertSeeScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "e5d7", pieceValue(BITBOARD_WP) - pieceValue(BITBOARD_WN));
        assertSeeScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "f3f6", pieceValue(BITBOARD_WN) - pieceValue(BITBOARD_WQ));

        assertSeeScore("2rr3k/pp3pp1/1nnqbN1p/3pN3/1PpP4/2P3Q1/P1B4P/R4RK1 b - b3 0 1", "c4b3", 0);

        assertSeeScore("2rr1q1k/pp3pp1/1nn1b2p/3pN2N/2pP4/2P3Q1/PPB4P/R4RK1 w - - 2 2", "g3g7", pieceValue(BITBOARD_WP) - pieceValue(BITBOARD_WN));
        assertSeeScore("2rr1q2/pp3ppk/1nn1b1Np/3p3N/2pP4/2P3Q1/PPB4P/R4RK1 w - - 4 3", "g6f8", pieceValue(BITBOARD_WQ) - pieceValue(BITBOARD_WN));
        assertSeeScore("3r3r/ppk2ppp/3q4/2b5/2P2Bn1/3R1Q2/P4PPP/5RK1 w - - 0 5", "d3d6", pieceValue(BITBOARD_WQ) - pieceValue(BITBOARD_WR));

    }

}