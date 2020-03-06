package com.netsensia.rivalchess.config;

public enum IterativeDeepening {

    IID_MIN_DEPTH (5),
    IID_REDUCE_DEPTH (3)
    ;

    private int value;

    private IterativeDeepening(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
