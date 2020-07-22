package com.netsensia.rivalchess.util;

import com.netsensia.rivalchess.exception.IllegalEpdItemException;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpdItem {

    private final String fen;

    private final List<String> bestMoves;
    private final String id;

    private int maxNodesToSearch;
    private int minScore;
    private int maxScore;

    public EpdItem(String line) throws IllegalEpdItemException {
        final String[] parts = line.split("bm|;");

        fen = parts[0].trim();
        bestMoves = Arrays.asList(parts[1].trim().split(",| "));

        Pattern pattern = Pattern.compile("id \"(.*?)\"");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            id = matcher.group(1);
        } else {
            throw new IllegalEpdItemException("Could not parse EPD test item id " + line);
        }

        maxNodesToSearch = Integer.parseInt(getValue("nodes", line, "500000"));
        final String[] scoreRange = getValue("cp", line, "-10000 10000").split(" ");
        minScore = Integer.parseInt(scoreRange[0]);
        maxScore = Integer.parseInt(scoreRange[1]);
    }

    private String getValue(final String key, final String line, final String defaultValue) {
        Pattern pattern = Pattern.compile(key + " (.*?);");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) return matcher.group(1);

        return defaultValue;
    }

    public String getFen() {
        return fen;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
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

    public void setMaxNodesToSearch(int maxNodesToSearch) {
        this.maxNodesToSearch = maxNodesToSearch;
    }

}
