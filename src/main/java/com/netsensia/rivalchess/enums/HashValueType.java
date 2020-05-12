package com.netsensia.rivalchess.enums;

public enum HashValueType {
    UPPER(0),
    LOWER(1),
    EXACT(2),
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