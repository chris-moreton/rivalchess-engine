package com.netsensia.rivalchess.model.board;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BoardRefTest {

    @Test
    public void testConstructorAndGettersAndSetters() {
        BoardRef br = new BoardRef(1,2);

        assertEquals(1, br.getXFile());
        assertEquals(2, br.getYRank());
    }

    @Test
    public void testEquals() {
        BoardRef br1 = new BoardRef(1, 2);
        BoardRef br2 = new BoardRef(2, 1);
        BoardRef br3 = new BoardRef(1, 2);

        Object o = new Object();

        assertTrue(br1.equals(br3));
        assertFalse(br1.equals(br2));
        assertTrue(br3.equals(br1));
        assertFalse(br2.equals(br1));
        assertFalse(br2.equals(br3));
        assertFalse(br3.equals(br2));

        assertFalse(o.equals(br1));
        assertFalse(br1.equals(o));

    }
}
