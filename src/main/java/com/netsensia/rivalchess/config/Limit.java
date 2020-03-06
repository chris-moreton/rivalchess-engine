package com.netsensia.rivalchess.config;

public enum Limit {

    MAX_GAME_MOVES (500),
    MAX_NODES_TO_SEARCH (Integer.MAX_VALUE),
    MAX_LEGAL_MOVES (220),
    MAX_SEARCH_MILLIS (60 * 60 * 1000), // 1 hour
    MIN_SEARCH_MILLIS (25),
    MAX_SEARCH_DEPTH (125),
    MAX_QUIESCE_DEPTH (50),
    MAX_EXTENSION_DEPTH (60),
    MAX_TREE_DEPTH (Limit.MAX_SEARCH_DEPTH.getValue() +
                            Limit.MAX_QUIESCE_DEPTH.getValue() +
                            Limit.MAX_EXTENSION_DEPTH.getValue() + 1)
    ;
    
    private int value;

    private Limit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
