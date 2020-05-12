package com.netsensia.rivalchess.config;

import com.netsensia.rivalchess.model.Piece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.netsensia.rivalchess.engine.core.eval.PieceValueKt.pieceValue;

public enum SearchConfig {

    NUM_KILLER_MOVES (2),
    HISTORY_MAX_VALUE (20000),
    ASPIRATION_RADIUS (40),
    MAXIMUM_HASH_AGE (3),
    NULLMOVE_REDUCE_DEPTH (2),
    NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE (6),
    NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES (pieceValue(Piece.KNIGHT)),
    GENERATE_CHECKS_UNTIL_QUIESCE_PLY (0),
    DELTA_PRUNING_MARGIN (200),
    FUTILITY_MARGIN_BASE (pieceValue(Piece.PAWN) * 2),
    PV_MINIMUM_DISTANCE_FROM_LEAF (2),
    ;

    private static final List<Integer> futilityMargin =
            Collections.unmodifiableList(
                    Arrays.asList(FUTILITY_MARGIN_BASE.getValue(), FUTILITY_MARGIN_BASE.getValue() * 2, FUTILITY_MARGIN_BASE.getValue() * 3));

    private int value;

    private SearchConfig(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int getFutilityMargin(int i) {
        return futilityMargin.get(i);
    }

}
