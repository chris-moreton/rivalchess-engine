package com.netsensia.rivalchess.constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColourTest {

    @Test
    public void opponent() {
        Colour colour = Colour.WHITE;
        assertEquals(Colour.BLACK, Colour.WHITE.opponent());
        assertEquals(Colour.WHITE, Colour.BLACK.opponent());
    }
}
