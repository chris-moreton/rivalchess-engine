package com.netsensia.rivalchess.model.board;

import com.netsensia.rivalchess.model.Square;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SquareTest {

    @Test
    public void testConstructorAndGettersAndSetters() {
        Square br = new Square(1,2);

        assertEquals(1, br.getXFile());
        assertEquals(2, br.getYRank());
    }

    @Test
    public void testEquals() {
        Square br1 = new Square(1, 2);
        Square br2 = new Square(2, 1);
        Square br3 = new Square(1, 2);

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

    @Test
    public void testGetAlgebraicXFile() {
        Square br1 = new Square(1, 2);
        assertEquals('b', br1.getAlgebraicXFile());
    }

    @Test
    public void testGetAlgebraicYRank() {
        Square br1 = new Square(1, 2);
        assertEquals('6', br1.getAlgebraicYRank(8));
    }
}
