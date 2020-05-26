package com.netsensia.rivalchess.config

enum class FeatureFlag(val isActive: Boolean) {
    USE_HASH_TABLES(true),
    USE_HEIGHT_REPLACE_HASH(true),
    USE_ALWAYS_REPLACE_HASH(true),
    USE_ASPIRATION_WINDOW(true),
    USE_NULL_MOVE_PRUNING(true),
    USE_DELTA_PRUNING(true),
    USE_FUTILITY_PRUNING(true),
    USE_HISTORY_HEURISTIC(true),
    USE_MATE_HISTORY_KILLERS(true),
    USE_INTERNAL_OPENING_BOOK(true),
    USE_PIECE_SQUARES_IN_MOVE_ORDERING(true),
    USE_LATE_MOVE_REDUCTIONS(true),
    LMR_RESEARCH_ON_FAIL_HIGH(true),
    USE_PV_SEARCH(true),
    USE_INTERNAL_ITERATIVE_DEEPENING(true),
    USE_SUPER_VERIFY_ON_HASH(false);

}