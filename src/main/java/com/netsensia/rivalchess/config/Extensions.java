package com.netsensia.rivalchess.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Extensions {

    FRACTIONAL_EXTENSION_FULL (8),
    FRACTIONAL_EXTENSION_THREAT (8),
    FRACTIONAL_EXTENSION_CHECK (8),
    FRACTIONAL_EXTENSION_RECAPTURE (8),
    FRACTIONAL_EXTENSION_PAWN (8),
    RECAPTURE_EXTENSION_MARGIN (50),
    LAST_EXTENSION_LAYER (4)
    ;

    private int value;

    private Extensions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static List<Integer> getMaxNewExtensionsTreePart() {
        return Collections.unmodifiableList(Arrays.asList(
                Extensions.FRACTIONAL_EXTENSION_FULL.getValue(),
                Extensions.FRACTIONAL_EXTENSION_FULL.getValue() / 4 * 3,
                Extensions.FRACTIONAL_EXTENSION_FULL.getValue() / 2,
                Extensions.FRACTIONAL_EXTENSION_FULL.getValue() / 8, 0));
    }
}
