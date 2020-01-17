package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalEpdItemException;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpdItem {

    private String fen;
    private List<String> bestMoves;
    private String id;
    private int maxNodesToSearch = 10000000;

    public EpdItem(String line) throws IllegalEpdItemException {
        final String[] parts = line.split("bm|;");

        fen = parts[0].trim();
        bestMoves = Arrays.asList(parts[1].trim().split(" "));

        Pattern pattern = Pattern.compile("id \"(.*?)\"");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            id = matcher.group(1);
        } else {
            throw new IllegalEpdItemException("Could not parse EPD test item id " + line);
        }

        pattern = Pattern.compile("nodes (.*);");
        matcher = pattern.matcher(line);

        if (matcher.find()) {
            maxNodesToSearch = Integer.parseInt(matcher.group(1));
        }
    }

    public String getFen() {
        return fen;
    }

    public List<String> getBestMoves() {
        return bestMoves;
    }

    public String getId() {
        return id;
    }

    public int getMaxNodesToSearch() {
        return maxNodesToSearch;
    }

}
