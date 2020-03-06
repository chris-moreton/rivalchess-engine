package com.netsensia.rivalchess.enums;

public enum HashValueType {
    UPPERBOUND (0),
    LOWERBOUND (1),
    EXACTSCORE (2),
    EMPTY (3),
    ;

    private int index;

    private HashValueType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}