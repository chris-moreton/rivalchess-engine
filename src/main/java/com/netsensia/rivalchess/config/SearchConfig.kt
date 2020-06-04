package com.netsensia.rivalchess.config

import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.model.Piece
import java.util.*

enum class SearchConfig(val value: Int) {
    HISTORY_MAX_VALUE(20000),
    ASPIRATION_RADIUS(40),
    MAXIMUM_HASH_AGE(3),
    NULLMOVE_REDUCE_DEPTH(2),
    NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE(6),
    NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES(pieceValue(Piece.KNIGHT)),
    GENERATE_CHECKS_UNTIL_QUIESCE_PLY(0),
    PV_MINIMUM_DISTANCE_FROM_LEAF(2);
}