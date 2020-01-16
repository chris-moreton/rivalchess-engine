package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.RivalSearch;
import com.netsensia.rivalchess.exception.IllegalFenException;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.util.FenUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class UciControllerTest {

    RivalSearch rivalSearch;
    UCIController uciController;
    ByteArrayOutputStream outSpy;

    @Before
    public void setUp() throws InterruptedException {

        outSpy = new ByteArrayOutputStream();

        rivalSearch = new RivalSearch(new PrintStream(outSpy));

        rivalSearch.startEngineTimer(true);
        rivalSearch.setHashSizeMB(32);

        new Thread(rivalSearch).start();

        uciController = new UCIController(rivalSearch, 1, new PrintStream(outSpy));

        new Thread(uciController).start();

        SECONDS.sleep(2);

    }

    @Test
    public void testUciOkResponse() {
        uciController.processUCICommand("uci");

        assertTrue(outSpy.toString().contains("uciok"));
    }

    @Test
    public void testGenerateMove() throws InterruptedException {

        uciController.processUCICommand("ucinewgame");
        uciController.processUCICommand("position startpos");
        uciController.processUCICommand("go depth 3");

        SECONDS.sleep(1);

        await().atMost(10, SECONDS).until(() -> !rivalSearch.isSearching());

        assertTrue(outSpy.toString().contains("bestmove g1f3"));
    }

    public static class FenUtilsTest {
        @Test
        public void testGetChessBoard() throws IllegalFenException {

            Board board = FenUtils.getBoardModel("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56");

            assertEquals(board.getPieceCode(new Square(0,0)), '_');
            assertEquals(board.getPieceCode(new Square(0,1)), '_');
            assertEquals(board.getPieceCode(new Square(0,2)), '_');
            assertEquals(board.getPieceCode(new Square(0,3)), '_');
            assertEquals(board.getPieceCode(new Square(0,4)), '_');
            assertEquals(board.getPieceCode(new Square(0,5)), '_');
            assertEquals(board.getPieceCode(new Square(0,6)), '_');
            assertEquals(board.getPieceCode(new Square(0,7)), '_');

            assertEquals(board.getPieceCode(new Square(1,7)), '_');
            assertEquals(board.getPieceCode(new Square(1,6)), 'r');
            assertEquals(board.getPieceCode(new Square(1,5)), '_');
            assertEquals(board.getPieceCode(new Square(1,4)), 'P');
            assertEquals(board.getPieceCode(new Square(1,3)), 'p');
            assertEquals(board.getPieceCode(new Square(1,2)), 'p');
            assertEquals(board.getPieceCode(new Square(1,1)), '_');
            assertEquals(board.getPieceCode(new Square(1,0)), '_');

            assertTrue(board.isBlackToMove());
            assertTrue(board.getEnPassantFile() == 6);
        }

        @Test
        public void testInvertFen() throws IllegalFenException {
            String actual = FenUtils.invertFen("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56");
            assertEquals("8/1R2n3/2pk1b2/1p3rp1/1P5p/1P2Q2P/6P1/6K1 w - b6 5 56", actual);
        }
    }
}