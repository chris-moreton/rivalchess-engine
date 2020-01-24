package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalEpdItemException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EpdReader implements Iterable<EpdItem>     {

    private final List<EpdItem> epdItems = new ArrayList<>();

    public EpdReader(String filename) throws IOException, IllegalEpdItemException {

        File file = new File(filename);

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;

        while ((line = br.readLine()) != null) {
            if (!line.trim().equals("")) {
                epdItems.add(new EpdItem(line));
            }
        }
        fr.close();
    }

    public int size() {
        return epdItems.size();
    }

    @Override
    public Iterator<EpdItem> iterator() {
        return epdItems.iterator();
    }

}
