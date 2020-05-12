package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.model.Piece;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.model.util.FenUtils;
import junit.framework.TestCase;
import org.junit.Test;

import static com.netsensia.rivalchess.engine.core.eval.PieceValueKt.pieceValue;

public class StaticExchangeEvaluatorPremiumTest extends TestCase {

    private StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluatorPremium();

    public void assertSeeScore (final String fen, final String move, final int expectedScore) throws InvalidMoveException {
        EngineBoard engineBoard = new EngineBoard(FenUtils.getBoardModel(fen));
        assertEquals(expectedScore, staticExchangeEvaluator.staticExchangeEvaluation(
                engineBoard,
                ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move)));
    }

    @Test
    public void testStaticExchangeEvaluation() throws IllegalFenException, InvalidMoveException {
        assertSeeScore("4k3/p1pprpb1/bnr1p3/3QN1n1/1p1NP1p1/7p/PPPBBPPP/R3K2R w KQ - 0 1", "d5e6",
                pieceValue(Piece.PAWN) - pieceValue(Piece.QUEEN));

        // winning rook capture - black rook can't recapture safely
        assertSeeScore("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4", pieceValue(Piece.PAWN));

        assertSeeScore("4k2r/p1ppqpb1/bnr1p3/3PN1n1/1p2P1p1/2N2Q1p/PPPBBPPP/R3K2R w KQk - 0 1", "d5e6", 0);

        assertSeeScore("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", "f7e6", pieceValue(Piece.BISHOP));

        // pawn promotes, king has to take queen
        assertSeeScore("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2f1",
                pieceValue(Piece.KNIGHT) - pieceValue(Piece.PAWN));

        // leaves king in check
        assertSeeScore("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "h6c1", -Integer.MAX_VALUE);

        assertSeeScore("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "e5f6", 0);

        assertSeeScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "e5d7", pieceValue(Piece.PAWN) - pieceValue(Piece.KNIGHT));
        assertSeeScore("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "f3f6", pieceValue(Piece.KNIGHT) - pieceValue(Piece.QUEEN));
    }

}