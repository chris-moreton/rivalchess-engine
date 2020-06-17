package com.netsensia.rivalchess.config

import com.netsensia.rivalchess.engine.eval.VALUE_KNIGHT

const val HISTORY_MAX_VALUE = 20000
const val ASPIRATION_RADIUS = 40
const val MAXIMUM_HASH_AGE = 3
const val NULLMOVE_REDUCE_DEPTH = 2
const val NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE = 6
const val NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES = VALUE_KNIGHT
const val GENERATE_CHECKS_UNTIL_QUIESCE_PLY = 0
const val PV_MINIMUM_DISTANCE_FROM_LEAF = 2
