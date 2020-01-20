package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalEpdItemException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class EpdReaderTest {

    @Test
    public void testSize() throws IOException, IllegalEpdItemException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("epd/winAtChess.epd")).getFile());

        EpdReader epdReader = new EpdReader(file.getAbsolutePath());

        assertEquals(300, epdReader.size());
    }
}
