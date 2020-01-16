package com.netsensia.rivalchess.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EpdReaderTest {

    @Test
    public void testSize() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("epd/winAtChess.epd").getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());

        assertEquals(300, epdReader.size());
    }
}
