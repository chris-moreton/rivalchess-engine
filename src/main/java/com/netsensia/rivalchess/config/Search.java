package com.netsensia.rivalchess.config;

import com.netsensia.rivalchess.enums.Piece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Search {

    NUM_KILLER_MOVES (2),
    HISTORY_MAX_VALUE (20000),
    ASPIRATION_RADIUS (40),
    MAXIMUM_HASH_AGE (3),
    NULLMOVE_REDUCE_DEPTH (2),
    NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE (6),
    NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES (Piece.KNIGHT.getValue()),
    GENERATE_CHECKS_UNTIL_QUIESCE_PLY (0),
    DELTA_PRUNING_MARGIN (200),
    FUTILITY_MARGIN_BASE (Piece.PAWN.getValue() * 2),
    PV_MINIMUM_DISTANCE_FROM_LEAF (2),
    ;

    private static final List<Integer> futilityMargin =
            Collections.unmodifiableList(
                    Arrays.asList(FUTILITY_MARGIN_BASE.getValue(), FUTILITY_MARGIN_BASE.getValue() * 2, FUTILITY_MARGIN_BASE.getValue() * 3));

    private int value;

    private Search(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getFutilityMargin(int i) {
        return futilityMargin.get(i);
    }

}
