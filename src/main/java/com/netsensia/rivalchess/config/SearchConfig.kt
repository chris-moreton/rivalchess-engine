package com.netsensia.rivalchess.config

import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.model.Piece
import java.util.*

enum class SearchConfig(val value: Int) {
    NUM_KILLER_MOVES(2), HISTORY_MAX_VALUE(20000), ASPIRATION_RADIUS(40), MAXIMUM_HASH_AGE(3), NULLMOVE_REDUCE_DEPTH(2), NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE(6), NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES(pieceValue(Piece.KNIGHT)), GENERATE_CHECKS_UNTIL_QUIESCE_PLY(0), DELTA_PRUNING_MARGIN(200), FUTILITY_MARGIN_BASE(pieceValue(Piece.PAWN) * 2), PV_MINIMUM_DISTANCE_FROM_LEAF(2);

    companion object {
        private val futilityMargin = Collections.unmodifiableList(
                Arrays.asList(FUTILITY_MARGIN_BASE.value, FUTILITY_MARGIN_BASE.value * 2, FUTILITY_MARGIN_BASE.value * 3))

        fun getFutilityMargin(i: Int): Int {
            return futilityMargin[i]
        }
    }

}