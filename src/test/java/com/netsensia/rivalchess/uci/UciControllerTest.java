package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.RivalSearch;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
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
}