package com.netsensia.rivalchess.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EpdReader {

    private static List<String> tests = new ArrayList<>();

    public EpdReader(String filename) throws IOException {

        File file = new File(filename);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        String line;

        while ((line = br.readLine()) != null) {
            if (!line.trim().equals("")) {
                tests.add(line);
            }
        }
        fr.close();
    }

    public int size() {
        return tests.size();
    }
}
