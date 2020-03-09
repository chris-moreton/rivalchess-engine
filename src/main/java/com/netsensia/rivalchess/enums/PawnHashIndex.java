package com.netsensia.rivalchess.enums;

public enum PawnHashIndex {
    PAWNHASHENTRY_MAIN_SCORE(1),
    PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE(1),
    PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE(2),
    PAWNHASHENTRY_WHITE_PASSEDPAWNS(3),
    PAWNHASHENTRY_BLACK_PASSEDPAWNS(4),
    PAWNHASHENTRY_LOCK(5),
    ;

    private int index;
    private static final int numPawnHashIndexes = 6;

    private PawnHashIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static int getNumHashFields() {
        return numPawnHashIndexes;
    }

    public static int getHashPositionSizeBytes() {
        return 8 +  /* pointer to array */ (getNumHashFields() * 8);
    }

}