package com.netsensia.rivalchess.enums;

import com.netsensia.rivalchess.config.FeatureFlag;

public enum HashIndex {
    HASHENTRY_MOVE(0),
    HASHENTRY_SCORE(1),
    HASHENTRY_HEIGHT(2),
    HASHENTRY_FLAG(3),
    HASHENTRY_VERSION(4),
    HASHENTRY_64BIT1(5),
    HASHENTRY_64BIT2(6),
    HASHENTRY_LOCK1(6),
    ;

    private int index;
    private static final int numPawnHashIndexes = 6;

    private HashIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getNumHashFields() {
        return FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive() ? 31 : 7;
    }

    public int getHashPositionSizeBytes() {
        return 8 +  /* pointer to array */ (getNumHashFields() * 4);
    }

    public int getNumIndexes() {
        return numPawnHashIndexes;
    }

}