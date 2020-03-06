package com.netsensia.rivalchess.config;

public enum LateMoveReductions {

    LMR_NOT_ABOVE_ALPHA_REDUCTION (1),
    LMR_ABOVE_ALPHA_ADDITION (5),
    LMR_INITIAL_VALUE (100),
    LMR_THRESHOLD (50),
    LMR_REPLACE_VALUE_AFTER_CUT (50),
    LMR_CUT_MARGIN (0),

    LMR_LEGALMOVES_BEFORE_ATTEMPT (4),
    NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION (-1)
    ;

    private int value;

    private LateMoveReductions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
