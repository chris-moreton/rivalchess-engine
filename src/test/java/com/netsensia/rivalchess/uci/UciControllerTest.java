package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.RivalSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;


public class UciControllerTest {

    static RivalSearch m_engine;
    UCIController uciController;
    ByteArrayOutputStream outSpy;

    @Before
    public void setUp() throws InterruptedException {

        outSpy = new ByteArrayOutputStream();

        m_engine = new RivalSearch(new PrintStream(outSpy));

        m_engine.startEngineTimer(true);
        m_engine.setHashSizeMB(32);

        new Thread(m_engine).start();

        uciController = new UCIController(m_engine, 1, new PrintStream(outSpy));

        new Thread(uciController).start();

        TimeUnit.SECONDS.sleep(2);

    }

    @Test
    public void testUciOkResponse() throws InterruptedException, IOException {
        uciController.processUCICommand("uci");

        assertTrue(outSpy.toString().contains("uciok"));
    }

    @Test
    public void testGenerateMove() throws InterruptedException {

        uciController.processUCICommand("ucinewgame");
        uciController.processUCICommand("position startpos");
        uciController.processUCICommand("go depth 3");

        TimeUnit.SECONDS.sleep(5);

        while (m_engine.isSearching()) {}

        assertTrue(outSpy.toString().contains("bestmove g1f3"));
    }
}