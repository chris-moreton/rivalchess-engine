package com.netsensia.rivalchess.epd;

import com.netsensia.rivalchess.exception.IllegalEpdItemException;
import com.netsensia.rivalchess.util.EpdItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EpdItemTest {

    @Test
    public void testEpdItemPartsWithOneBestMove() throws IllegalEpdItemException {
        final String epdString = "2kr2nr/pp1n1ppp/2p1p3/q7/1b1P1B2/P1N2Q1P/1PP1BPP1/R3K2R w KQ - bm axb4; id \"WAC.269\";";

        EpdItem epdItem = new EpdItem(epdString);

        assertEquals(1, epdItem.getBestMoves().size());
        assertEquals("axb4", epdItem.getBestMoves().get(0));
        assertEquals("2kr2nr/pp1n1ppp/2p1p3/q7/1b1P1B2/P1N2Q1P/1PP1BPP1/R3K2R w KQ -", epdItem.getFen());
        assertEquals("WAC.269", epdItem.getId());
    }

    @Test
    public void testEpdItemPartsWithThreeBestMoves() throws IllegalEpdItemException {
        final String epdString = "3r3k/1r3p1p/p1pB1p2/8/p1qNP1Q1/P6P/1P4P1/3R3K w - - bm Bf8,Nf5,Qf4; id \"WAC.294\";";

        EpdItem epdItem = new EpdItem(epdString);

        assertEquals(3, epdItem.getBestMoves().size());
        assertEquals("Bf8", epdItem.getBestMoves().get(0));
        assertEquals("Nf5", epdItem.getBestMoves().get(1));
        assertEquals("Qf4", epdItem.getBestMoves().get(2));
        assertEquals("3r3k/1r3p1p/p1pB1p2/8/p1qNP1Q1/P6P/1P4P1/3R3K w - -", epdItem.getFen());
        assertEquals("WAC.294", epdItem.getId());
    }

    @Test
    public void testEpdItemWithNodesPart() throws IllegalEpdItemException {
        final String epdString = "2kr2nr/pp1n1ppp/2p1p3/q7/1b1P1B2/P1N2Q1P/1PP1BPP1/R3K2R w KQ - bm axb4; id \"WAC.269\"; nodes 19283;";

        EpdItem epdItem = new EpdItem(epdString);

        assertEquals(1, epdItem.getBestMoves().size());
        assertEquals("axb4", epdItem.getBestMoves().get(0));
        assertEquals("2kr2nr/pp1n1ppp/2p1p3/q7/1b1P1B2/P1N2Q1P/1PP1BPP1/R3K2R w KQ -", epdItem.getFen());
        assertEquals("WAC.269", epdItem.getId());
        assertEquals(19283, epdItem.getMaxNodesToSearch());

    }
}
