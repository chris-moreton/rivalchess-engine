package com.netsensia.rivalchess.uci;

import com.netsensia.rivalchess.engine.core.RivalSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;


public class UciControllerTest {

    static final RivalSearch m_engine = new RivalSearch();
    UCIController uciController;
    ByteArrayOutputStream outSpy;

    @Before
    public void setUp() {

        outSpy = new ByteArrayOutputStream();

        m_engine.startEngineTimer(true);
        m_engine.setHashSizeMB(32);

        new Thread(m_engine).start();

        uciController = new UCIController(m_engine, 1, new PrintStream(outSpy));

        new Thread(uciController).start();
    }

    @Test
    public void testUciConsole() throws InterruptedException, IOException {

        uciController.processUCICommand("uci");

        assertTrue(outSpy.toString().contains("uciok"));

    }
}