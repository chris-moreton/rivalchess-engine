package com.netsensia.rivalchess.config;

public enum Hash {

    PAWN_HASH_DEFAULT_SCORE (-Integer.MAX_VALUE),
    DEFAULT_HASHTABLE_SIZE_MB (2),
    DEFAULT_SEARCH_HASH_HEIGHT (-9999),
    ;

    private int value;

    private Hash(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
