package com.netsensia.rivalchess.engine.helper;

import com.netsensia.rivalchess.consts.BitboardsKt;
import com.netsensia.rivalchess.engine.board.EngineBoard;
import com.netsensia.rivalchess.engine.board.MoveGenerator;
import com.netsensia.rivalchess.engine.hash.ZobristHashCalculator;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.util.FenUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static com.netsensia.rivalchess.consts.GameKt.FEN_START_POS;
import static com.netsensia.rivalchess.engine.board.MoveMakingBoardExtensionsKt.makeMove;
import static com.netsensia.rivalchess.engine.board.MoveMakingBoardExtensionsKt.makeNullMove;
import static com.netsensia.rivalchess.engine.board.MoveMakingBoardExtensionsKt.unMakeMove;
import static com.netsensia.rivalchess.util.ChessBoardConversionKt.getEngineMoveFromSimpleAlgebraic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ZobristHashTrackerTest {

    @Test
    public void initHash() throws IllegalFenException {
        Assert.assertEquals(8377675270202223558L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2R2k2/p7/7K/8/6p1/6P1/8/8 b - - 6 5"))));
        assertEquals(7804811707366554848L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6rk/p7/7K/8/6p1/6P1/8/4R3 w - - 5 3"))));
        assertEquals(1075668445979296707L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r3k2r/3bbppp/pB2pn2/2N5/1p3P2/8/PPP3PP/R2Q1RK1 w k - 3 4"))));
        assertEquals(1028767795214117555L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2b5/2RR2p1/4p2p/p3Pp2/k7/6P1/6P1/6K1 w - - 1 6"))));
        assertEquals(2350594996101438936L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r1b2rk1/pp3ppp/1nP1p3/7Q/3P4/N4N2/5PPb/R1B2RK1 w - - 0 3"))));
        assertEquals(811907097101711232L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6k1/3n1pbp/pN4p1/5b2/1P6/4Q3/P5PP/4B1K1 b - - 0 3"))));
        assertEquals(7064742042700254627L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6k1/1b1nqp1p/pp4p1/1P3P2/3b4/N3Q3/P5PP/1B2B1K1 b - - 1 2"))));
        assertEquals(2836738026196834385L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6k1/1b3p1p/pp3n2/5p2/1P5q/4N3/P5PP/1B2BK2 w - - 1 5"))));
        assertEquals(5824754335603279097L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3r2k1/1p3qp1/p6p/5p2/3N1P2/2PQ3P/PP6/7K b - - 0 6"))));
        assertEquals(7155541581696875541L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2r3k1/1p3pp1/p6p/8/1P1NP3/2q4P/P5P1/1Q4K1 w - - 0 5"))));
        assertEquals(4332303940081334316L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3r2k1/1p3pp1/p6p/q7/2PNb3/1P1Q3P/P4PP1/6K1 w - - 0 3"))));
        assertEquals(5140297385412254177L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r2q2k1/pp1rbppp/4pn2/2P5/1P3B1P/5PP1/P3Q1B1/1R3RK1 b - h3 0 2"))));
        assertEquals(2598232094144017073L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("1r6/5rkp/b1QR4/5pp1/pP6/6P1/P3qPBP/5RK1 w - - 0 5"))));
        assertEquals(2711610657820992228L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("5qk1/2Q2ppp/3Rp3/p7/1r6/1P3N1P/1P3PP1/6K1 b - - 3 5"))));
        assertEquals(6130013692976895541L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2Q5/p2R1p1p/7k/6p1/P7/1q4P1/5P1P/6K1 b - - 4 6"))));
        assertEquals(1969183652360398898L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("7k/1b4p1/p6p/1p2qN2/4P3/3r4/P5PP/1B1R2K1 b - - 0 2"))));
        assertEquals(9138795155598539683L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2r5/2p2k2/1pQ2pp1/7p/1P6/P3KP2/7P/R1R5 w - - 1 7"))));
        assertEquals(474950808986408700L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3r4/2p2k2/1pQ2pp1/8/1P5p/P4P2/5KPP/RqR5 w - - 0 6"))));
        assertEquals(8208020365696427452L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r2r2k1/p3bppp/1pn1b3/3p3n/N1P5/4BP2/PQN1B1PP/3R3K w - - 0 7"))));
        assertEquals(7861211263337003658L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r1b1Bbk1/p1B2p1p/6p1/p1np4/3P4/4PN2/PP3PPP/R4RK1 b - - 0 6"))));
        assertEquals(632436259894030524L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3nb1k1/p4pbp/2Q3p1/5B2/2p5/2P3B1/Pq3PPP/6K1 b - - 2 4"))));
        assertEquals(8552058796254150612L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("7k/2p1b1pp/8/1p1QP3/1P6/2P4r/1P5P/4q1BK w - - 5 4"))));
        assertEquals(2043404966475344765L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("N1bq1k2/pp1nr1p1/4p2p/3p1p2/1b1P4/4PNP1/1PQR1PP1/4KB1R w K - 3 5"))));
        assertEquals(8968902781539702392L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r4rk1/pp2bppp/4p3/6q1/b1BNQ3/8/PP3PPP/4RRK1 b - - 2 6"))));
        assertEquals(1798995394321752692L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r4rk1/pp1b1ppp/4p3/7n/1bBN4/P1N5/1P3PKP/2RR4 w - - 1 6"))));
        assertEquals(3427826734247474712L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r1b2rk1/pp3ppp/3NNn2/6q1/2B5/8/PP2QPPP/2bR2K1 b - - 0 6"))));
        assertEquals(3185928038993651253L, ZobristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3q3k/2pnbrpp/2Q5/8/1r1PN1b1/8/PP3PPP/R1B2RK1 b - - 2 6"))));
    }

    private void compareCalculatedHashWithTrackedHash(EngineBoard ecb, String move) {
        makeMove(ecb, getEngineMoveFromSimpleAlgebraic(move).compact, false, true);
        final long originalTrackedHashCode = ecb.boardHashCode();
        final long originalCalculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, originalTrackedHashCode);

        unMakeMove(ecb, true);
        final long unmadeTrackedHashCode = ecb.boardHashCode();
        final long unmadeCalculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(unmadeCalculatedHashCode, unmadeTrackedHashCode);

        makeMove(ecb, getEngineMoveFromSimpleAlgebraic(move).compact, false, true);
        assertEquals(ecb.boardHashCode(), originalTrackedHashCode);
        assertEquals(ZobristHashCalculator.calculateHash(ecb), originalCalculatedHashCode);

    }

    private void compareCalculatedHashWithTrackedHashOnNullMove(EngineBoard ecb) {

        makeNullMove(ecb, 0);
        final long originalTrackedHashCode = ecb.boardHashCode();
        final long originalCalculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, originalTrackedHashCode);

        makeNullMove(ecb, 0);
        final long unmadeTrackedHashCode = ecb.boardHashCode();
        final long unmadeCalculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(unmadeCalculatedHashCode, unmadeTrackedHashCode);

        makeNullMove(ecb, 0);
        assertEquals(ecb.boardHashCode(), originalTrackedHashCode);
        assertEquals(ZobristHashCalculator.calculateHash(ecb), originalCalculatedHashCode);

    }

    @Test
    public void zorbristTracker() {

        EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        compareCalculatedHashWithTrackedHash(ecb, "e2e4");
        compareCalculatedHashWithTrackedHash(ecb, "c7c5");
        compareCalculatedHashWithTrackedHash(ecb, "e4e5");
        compareCalculatedHashWithTrackedHash(ecb, "d7d5");
        compareCalculatedHashWithTrackedHash(ecb, "e5d6");
        compareCalculatedHashWithTrackedHash(ecb, "d8d6");
        compareCalculatedHashWithTrackedHash(ecb, "f1c4");
        compareCalculatedHashWithTrackedHash(ecb, "g8f6");
        compareCalculatedHashWithTrackedHash(ecb, "g1f3");
        compareCalculatedHashWithTrackedHash(ecb, "b8c6");
        compareCalculatedHashWithTrackedHash(ecb, "e1g1");
        compareCalculatedHashWithTrackedHash(ecb, "c8g4");
        compareCalculatedHashWithTrackedHash(ecb, "d1e2");
        compareCalculatedHashWithTrackedHash(ecb, "e8c8");
        compareCalculatedHashWithTrackedHash(ecb, "d2d4");
        compareCalculatedHashWithTrackedHash(ecb, "e7e5");
        compareCalculatedHashWithTrackedHash(ecb, "d4d5");
        compareCalculatedHashWithTrackedHash(ecb, "e5e4");
        compareCalculatedHashWithTrackedHash(ecb, "d5c6");
        compareCalculatedHashWithTrackedHash(ecb, "e4f3");
        compareCalculatedHashWithTrackedHash(ecb, "c6c7");
        compareCalculatedHashWithTrackedHash(ecb, "c8d7");
        compareCalculatedHashWithTrackedHash(ecb, "c7d8N");
        compareCalculatedHashWithTrackedHash(ecb, "f3e2");
        compareCalculatedHashWithTrackedHash(ecb, "d8b7");
        compareCalculatedHashWithTrackedHash(ecb, "e2e1q");

        Assert.assertEquals("5b1r/pN1k1ppp/3q1n2/2p5/2B3b1/8/PPP2PPP/RNB1qRK1 w - - 0 14", com.netsensia.rivalchess.engine.board.BoardExtensionsKt.getFen(ecb));

        ecb = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        compareCalculatedHashWithTrackedHash(ecb, "e2e4");
        compareCalculatedHashWithTrackedHash(ecb, "e7e5");
        compareCalculatedHashWithTrackedHash(ecb, "d1e2");
        compareCalculatedHashWithTrackedHash(ecb, "f8e7");
        compareCalculatedHashWithTrackedHash(ecb, "b1c3");
        compareCalculatedHashWithTrackedHash(ecb, "g8f7");
        compareCalculatedHashWithTrackedHash(ecb, "d2d3");
        compareCalculatedHashWithTrackedHash(ecb, "e8g8");
        compareCalculatedHashWithTrackedHash(ecb, "c1d2");
        compareCalculatedHashWithTrackedHash(ecb, "h7h6");
        compareCalculatedHashWithTrackedHash(ecb, "e1c1");
        compareCalculatedHashWithTrackedHash(ecb, "a7a6");

        compareCalculatedHashWithTrackedHashOnNullMove(ecb);

    };

    @Test
    public void testTrackerWhenMakeMoveLeavesMoverInCheck() {
        EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

        compareCalculatedHashWithTrackedHash(ecb, "e2e4");
        compareCalculatedHashWithTrackedHash(ecb, "e7e5");
        compareCalculatedHashWithTrackedHash(ecb, "d2d4");
        compareCalculatedHashWithTrackedHash(ecb, "f8b4");
        final long originalTrackedHashCode = ecb.boardHashCode();
        final long originalCalculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, originalTrackedHashCode);

        makeMove(ecb, getEngineMoveFromSimpleAlgebraic("h2h3").compact, false, true);
        final long hashCodeAfterIllegalMove = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, hashCodeAfterIllegalMove);

        makeMove(ecb, getEngineMoveFromSimpleAlgebraic("e1e2").compact, false, true);
        final long hashCodeAfterLegalMove = ZobristHashCalculator.calculateHash(ecb);
        assertNotEquals(originalCalculatedHashCode, hashCodeAfterLegalMove);

    }

    @Test
    public void testInterestingFailure() {
        EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel("8/p4kp1/5p2/2P2QN1/3p3p/3PbK1P/7P/2q5 b - - 2 2"));

        makeMove(ecb, getEngineMoveFromSimpleAlgebraic("c1g1").compact, false, true);
        final long trackedCode = ecb.boardHashCode();
        final long calculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
        assertEquals(calculatedHashCode, trackedCode);
    }

    @Test
    public void multipleRandomMovesTest() {

        for (int i=0; i<100; i++) {
            EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel(FEN_START_POS));

            Random r = new Random();
            r.setSeed(i);

            MoveGenerator mg = ecb.moveGenerator();
            int legalMoves[] = mg.generateLegalMoves().getMoves();
            int numLegalMoves = mg.getMoveCount();

            while (!com.netsensia.rivalchess.engine.board.BoardExtensionsKt.isGameOver(ecb) && ecb.numMovesMade < 100) {
                int move = legalMoves[r.nextInt(numLegalMoves)];
                makeMove(ecb, move, false, true);

                final long trackedCode = ecb.boardHashCode();
                final long calculatedHashCode = ZobristHashCalculator.calculateHash(ecb);
                assertEquals(calculatedHashCode, trackedCode);

                mg = ecb.moveGenerator();
                legalMoves = mg.generateLegalMoves().getMoves();
                numLegalMoves = mg.getMoveCount();

            }
        }

    }

}
