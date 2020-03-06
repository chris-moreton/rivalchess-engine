package com.netsensia.rivalchess.config;

public enum Uci {

    UCI_TIMER_INTERVAL_MILLIS (50),
    UCI_TIMER_SAFTEY_MARGIN_MILLIS (250)
    ;

    private int value;

    private Uci(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
