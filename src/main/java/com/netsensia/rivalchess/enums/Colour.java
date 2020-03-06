package com.netsensia.rivalchess.enums;

public enum Colour {
    WHITE (0),
    BLACK  (1)
    ;

    private int value;

    private Colour(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Colour opponent() {
        return this == Colour.WHITE ? Colour.BLACK : Colour.WHITE;
    }
}