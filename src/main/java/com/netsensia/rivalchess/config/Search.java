package com.netsensia.rivalchess.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Search {

    NUM_KILLER_MOVES (2),
    HISTORY_MAX_VALUE (20000),
    ASPIRATION_RADIUS (40),
    ;

    private int value;

    private Search(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
