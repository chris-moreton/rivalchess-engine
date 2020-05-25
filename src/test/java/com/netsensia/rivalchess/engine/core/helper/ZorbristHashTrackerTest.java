package com.netsensia.rivalchess.engine.core.helper;

import com.netsensia.rivalchess.config.Limit;
import com.netsensia.rivalchess.engine.core.ConstantsKt;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashCalculator;
import com.netsensia.rivalchess.engine.core.hash.ZorbristHashTracker;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.exception.InvalidMoveException;
import com.netsensia.rivalchess.model.util.FenUtils;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import org.junit.Test;

import java.util.Random;

import static com.netsensia.rivalchess.engine.core.board.MoveGenerationBoardExtensionsKt.getMovesAsArray;
import static com.netsensia.rivalchess.engine.core.board.MoveGenerationBoardExtensionsKt.numLegalMoves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ZorbristHashTrackerTest {

    @Test
    public void initHash() throws IllegalFenException {
        ZorbristHashTracker hashCalculator = new ZorbristHashTracker();

        assertEquals(8377675270202223558L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2R2k2/p7/7K/8/6p1/6P1/8/8 b - - 6 5"))));
        assertEquals(7804811707366554848L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6rk/p7/7K/8/6p1/6P1/8/4R3 w - - 5 3"))));
        assertEquals(1075668445979296707L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r3k2r/3bbppp/pB2pn2/2N5/1p3P2/8/PPP3PP/R2Q1RK1 w k - 3 4"))));
        assertEquals(1028767795214117555L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2b5/2RR2p1/4p2p/p3Pp2/k7/6P1/6P1/6K1 w - - 1 6"))));
        assertEquals(2350594996101438936L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r1b2rk1/pp3ppp/1nP1p3/7Q/3P4/N4N2/5PPb/R1B2RK1 w - - 0 3"))));
        assertEquals(811907097101711232L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6k1/3n1pbp/pN4p1/5b2/1P6/4Q3/P5PP/4B1K1 b - - 0 3"))));
        assertEquals(7064742042700254627L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6k1/1b1nqp1p/pp4p1/1P3P2/3b4/N3Q3/P5PP/1B2B1K1 b - - 1 2"))));
        assertEquals(2836738026196834385L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("6k1/1b3p1p/pp3n2/5p2/1P5q/4N3/P5PP/1B2BK2 w - - 1 5"))));
        assertEquals(5824754335603279097L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3r2k1/1p3qp1/p6p/5p2/3N1P2/2PQ3P/PP6/7K b - - 0 6"))));
        assertEquals(7155541581696875541L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2r3k1/1p3pp1/p6p/8/1P1NP3/2q4P/P5P1/1Q4K1 w - - 0 5"))));
        assertEquals(4332303940081334316L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3r2k1/1p3pp1/p6p/q7/2PNb3/1P1Q3P/P4PP1/6K1 w - - 0 3"))));
        assertEquals(1983338724879014299L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r2q2k1/pp1rbppp/4pn2/2P5/1P3B1P/5PP1/P3Q1B1/1R3RK1 b - h3 0 2"))));
        assertEquals(2598232094144017073L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("1r6/5rkp/b1QR4/5pp1/pP6/6P1/P3qPBP/5RK1 w - - 0 5"))));
        assertEquals(2711610657820992228L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("5qk1/2Q2ppp/3Rp3/p7/1r6/1P3N1P/1P3PP1/6K1 b - - 3 5"))));
        assertEquals(6130013692976895541L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2Q5/p2R1p1p/7k/6p1/P7/1q4P1/5P1P/6K1 b - - 4 6"))));
        assertEquals(1969183652360398898L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("7k/1b4p1/p6p/1p2qN2/4P3/3r4/P5PP/1B1R2K1 b - - 0 2"))));
        assertEquals(9138795155598539683L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("2r5/2p2k2/1pQ2pp1/7p/1P6/P3KP2/7P/R1R5 w - - 1 7"))));
        assertEquals(474950808986408700L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3r4/2p2k2/1pQ2pp1/8/1P5p/P4P2/5KPP/RqR5 w - - 0 6"))));
        assertEquals(8208020365696427452L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r2r2k1/p3bppp/1pn1b3/3p3n/N1P5/4BP2/PQN1B1PP/3R3K w - - 0 7"))));
        assertEquals(7861211263337003658L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r1b1Bbk1/p1B2p1p/6p1/p1np4/3P4/4PN2/PP3PPP/R4RK1 b - - 0 6"))));
        assertEquals(632436259894030524L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3nb1k1/p4pbp/2Q3p1/5B2/2p5/2P3B1/Pq3PPP/6K1 b - - 2 4"))));
        assertEquals(8552058796254150612L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("7k/2p1b1pp/8/1p1QP3/1P6/2P4r/1P5P/4q1BK w - - 5 4"))));
        assertEquals(2043404966475344765L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("N1bq1k2/pp1nr1p1/4p2p/3p1p2/1b1P4/4PNP1/1PQR1PP1/4KB1R w K - 3 5"))));
        assertEquals(8968902781539702392L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r4rk1/pp2bppp/4p3/6q1/b1BNQ3/8/PP3PPP/4RRK1 b - - 2 6"))));
        assertEquals(1798995394321752692L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r4rk1/pp1b1ppp/4p3/7n/1bBN4/P1N5/1P3PKP/2RR4 w - - 1 6"))));
        assertEquals(3427826734247474712L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("r1b2rk1/pp3ppp/3NNn2/6q1/2B5/8/PP2QPPP/2bR2K1 b - - 0 6"))));
        assertEquals(3185928038993651253L, ZorbristHashCalculator.calculateHash(new EngineBoard(FenUtils.getBoardModel("3q3k/2pnbrpp/2Q5/8/1r1PN1b1/8/PP3PPP/R1B2RK1 b - - 2 6"))));
    }

    @Test
    public void initPawnHash() throws IllegalFenException {
        ZorbristHashTracker hashCalculator = new ZorbristHashTracker();

        assertEquals(7352919752223822497L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k7/p7/q6P/2P5/2n1P3/3K4/8 w - - 0 5"))));
        assertEquals(9083264183814045386L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k7/p7/4p2P/2P5/2K1P2q/3Q4/8 b - - 1 5"))));
        assertEquals(4038912975424041097L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k7/p2P4/7P/4P3/8/4K3/5q2 w - - 1 7"))));
        assertEquals(6965160378581448749L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("k7/7P/8/p2P4/4K3/4P3/1q6/8 w - - 1 7"))));
        assertEquals(7621802180616499029L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k2P3P/8/8/8/4P3/4K3/n7 w - - 0 9"))));
        assertEquals(777983314143753872L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1k6/8/3P3P/p7/8/4P3/1q1K4/8 w - - 3 7"))));
        assertEquals(777983314143753872L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k7/3P3P/p7/1q6/3KP3/8/8 b - - 4 8"))));
        assertEquals(6309194122560902729L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k7/p3K3/4Q2P/n1P5/4q3/8/8 b - - 5 10"))));
        assertEquals(4370290739485241797L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/k7/p7/4Q2P/8/4P3/4q3/3n3K b - - 3 8"))));
        assertEquals(8612591920639253787L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/1r4k1/4p1p1/3pP2Q/8/7R/5PK1/8 b - - 1 7"))));
        assertEquals(7665015188837026086L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("6k1/8/4p1Q1/4P3/3p4/7R/1r3P2/3K4 b - - 0 9"))));
        assertEquals(6316535876622580026L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("4kQ2/2r1r3/4p1p1/3pP3/8/2P5/4KP2/7R b - - 10 9"))));
        assertEquals(6316535876622580026L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("2r5/6k1/4p1p1/3pPr2/8/2P3K1/3Q1P2/5R2 b - - 5 7"))));
        assertEquals(1324052202440718771L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("4k3/1r6/6p1/3pP3/8/2r2Q2/5P2/6KR b - - 0 8"))));
        assertEquals(6316535876622580026L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("8/1r3k2/4p1p1/3pP3/6Q1/2P2K2/5P2/7R b - - 1 6"))));
        assertEquals(2997313563610617575L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("3r1k2/1r6/4p2N/3pP3/8/2P2P2/5PK1/R6Q b - - 4 6"))));
        assertEquals(8195978013708418088L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("2r3k1/R7/4pQp1/3pP2p/2r3N1/2P2P2/5PK1/8 w - - 6 5"))));
        assertEquals(2997313563610617575L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("7Q/4kr2/4p3/2rpP3/6N1/2P2P2/5PK1/R7 w - - 3 7"))));
        assertEquals(6023279362077622357L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("2r1k3/2r5/4pQp1/3pP3/6P1/2P5/5PK1/R7 b - - 0 6"))));
        assertEquals(6577527402777839590L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r1b2q1k/ppp2Bb1/6PR/1Q2P3/3P1p2/8/PPP3P1/1K1n4 b - - 0 4"))));
        assertEquals(8567503273084485862L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r4q1k/pQ3Bb1/6Pp/3pPb2/3P1p2/8/PPP3nR/1K2R3 b - - 1 5"))));
        assertEquals(7415905118449312519L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r1b5/pp5k/4P2b/3p4/1qnP1p2/8/PPP3P1/1K1R4 w - - 0 7"))));
        assertEquals(2198063286082472680L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r3q2k/p2bPB2/6PR/1p1pb3/3P1p2/4n3/PPP3P1/1K1R4 b - - 0 7"))));
        assertEquals(1021364507498586413L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r4q1k/pB4b1/1n4Pp/4P3/3P1p2/8/PPQ3PR/1K6 b - - 0 6"))));
        assertEquals(6014530957643070140L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3rk1/5p2/N2p2p1/Q1n4p/2P1P3/5P1B/Pb5P/1K1rR3 w - - 0 4"))));
        assertEquals(850291719126799406L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3rk1/5p2/N4bp1/7p/2P5/5n1B/PPK4P/4r3 b - - 0 8"))));
        assertEquals(8137463193395310810L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3rk1/5pb1/p2p2p1/q6p/PNP1P3/3p3B/1P1R3P/1K2R3 b - a3 0 3"))));
        assertEquals(1860252939522023149L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("5rk1/5pb1/p2p2p1/4q2p/1rP1P3/3p3B/PP5P/1K1RR3 b - - 1 4"))));
        assertEquals(7051412142713837419L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3rk1/5p2/p2p2p1/7p/5N2/7B/Pn6/K6R w - - 0 7"))));
        assertEquals(2084855075960490445L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3rk1/5pb1/p2p2p1/2Q2B1p/1NP5/3p1P2/Pq1n3P/3KR3 w - - 2 4"))));
        assertEquals(5322111833311617531L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("5rk1/2N2pb1/p2p2p1/7p/P1P5/3p1P1r/8/2K1R3 w - - 1 7"))));
        assertEquals(7306199229900872213L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("5rk1/5pb1/p5p1/4p2p/2P2N2/3p1P1B/P6r/K7 w - - 1 7"))));
        assertEquals(4957166182224300487L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("5r2/5pbk/p2p2N1/7p/2P5/3p1P1r/P7/1K2R3 b - - 0 8"))));
        assertEquals(7707459286097982763L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("2r2rk1/5pb1/3p2p1/1P2q2p/1N2P3/3p3B/PP1R3P/1K2R3 w - - 1 4"))));
        assertEquals(2398052764475699268L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3rk1/5p2/p2p2p1/Q3R2p/nNP5/3R1P1B/PP6/2K5 b - - 0 6"))));
        assertEquals(2857364192485741149L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("1r3r1k/6b1/p2p1pp1/Q1n4p/1NP1P3/5P1B/PR2q2P/2K5 w - - 4 5"))));
        assertEquals(4737023572921181958L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r1bq4/1pp2p1p/p2p2p1/5N1k/Q7/8/PPPPrPPP/R1B3K1 w - - 0 6"))));
        assertEquals(4737023572921181958L, ZorbristHashCalculator.calculatePawnHash(new EngineBoard(FenUtils.getBoardModel("r1bq1rN1/1pp2pkp/p2p2p1/2n3Q1/4R3/8/PPPP1PPP/R1B3K1 b - - 5 3"))));

    }

    private void compareCalculatedHashWithTrackedHash(EngineBoard ecb, String move) throws InvalidMoveException {
        ecb.makeMove(new EngineMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move).compact));
        final long originalTrackedHashCode = ecb.trackedBoardHashCode();
        final long originalCalculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, originalTrackedHashCode);

        ecb.unMakeMove();
        final long unmadeTrackedHashCode = ecb.trackedBoardHashCode();
        final long unmadeCalculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(unmadeCalculatedHashCode, unmadeTrackedHashCode);

        ecb.makeMove(new EngineMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic(move).compact));
        assertEquals(ecb.trackedBoardHashCode(), originalTrackedHashCode);
        assertEquals(ZorbristHashCalculator.calculateHash(ecb), originalCalculatedHashCode);

    }

    private void compareCalculatedHashWithTrackedHashOnNullMove(EngineBoard ecb) throws InvalidMoveException {

        ecb.makeNullMove();
        final long originalTrackedHashCode = ecb.trackedBoardHashCode();
        final long originalCalculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, originalTrackedHashCode);

        ecb.unMakeNullMove();
        final long unmadeTrackedHashCode = ecb.trackedBoardHashCode();
        final long unmadeCalculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(unmadeCalculatedHashCode, unmadeTrackedHashCode);

        ecb.makeNullMove();
        assertEquals(ecb.trackedBoardHashCode(), originalTrackedHashCode);
        assertEquals(ZorbristHashCalculator.calculateHash(ecb), originalCalculatedHashCode);

    }

    @Test
    public void zorbristTracker() throws InvalidMoveException {

        EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

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

        assertEquals("5b1r/pN1k1ppp/3q1n2/2p5/2B3b1/8/PPP2PPP/RNB1qRK1 w - - 0 14", ecb.getFen());

        ecb = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

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
    public void testTrackerWhenMakeMoveLeavesMoverInCheck() throws InvalidMoveException {
        EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

        compareCalculatedHashWithTrackedHash(ecb, "e2e4");
        compareCalculatedHashWithTrackedHash(ecb, "e7e5");
        compareCalculatedHashWithTrackedHash(ecb, "d2d4");
        compareCalculatedHashWithTrackedHash(ecb, "f8b4");
        final long originalTrackedHashCode = ecb.trackedBoardHashCode();
        final long originalCalculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, originalTrackedHashCode);

        ecb.makeMove(new EngineMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("h2h3").compact));
        final long hashCodeAfterIllegalMove = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(originalCalculatedHashCode, hashCodeAfterIllegalMove);

        ecb.makeMove(new EngineMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("e1e2").compact));
        final long hashCodeAfterLegalMove = ZorbristHashCalculator.calculateHash(ecb);
        assertNotEquals(originalCalculatedHashCode, hashCodeAfterLegalMove);

    }

    @Test
    public void testInterestingFailure() throws InvalidMoveException {
        EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel("8/p4kp1/5p2/2P2QN1/3p3p/3PbK1P/7P/2q5 b - - 2 2"));

        ecb.makeMove(new EngineMove(ChessBoardConversion.getEngineMoveFromSimpleAlgebraic("c1g1").compact));
        final long trackedCode = ecb.trackedBoardHashCode();
        final long calculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
        assertEquals(calculatedHashCode, trackedCode);

    }

    @Test
    public void multipleRandomMovesTest() throws InvalidMoveException {

        for (int i=0; i<100; i++) {
            EngineBoard ecb = new EngineBoard(FenUtils.getBoardModel(ConstantsKt.FEN_START_POS));

            Random r = new Random();
            r.setSeed(i);

            int legalMoves[] = getMovesAsArray(ecb);

            while (!ecb.isGameOver() && ecb.getHalfMoveCount() < 100) {
                int move = legalMoves[r.nextInt(numLegalMoves(ecb))];
                ecb.makeMove(new EngineMove(move));

                final long trackedCode = ecb.trackedBoardHashCode();
                final long calculatedHashCode = ZorbristHashCalculator.calculateHash(ecb);
                assertEquals(calculatedHashCode, trackedCode);

                legalMoves = getMovesAsArray(ecb);

            }
        }

    }

}
