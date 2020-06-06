package com.netsensia.rivalchess.config

enum class FeatureFlag(val isActive: Boolean) {
    USE_HEIGHT_REPLACE_HASH(true),
    USE_ALWAYS_REPLACE_HASH(true),
    USE_NULL_MOVE_PRUNING(true), // +17 fails when turned off
    USE_HISTORY_HEURISTIC(true), // +5 fails when turned off
    USE_MATE_HISTORY_KILLERS(true), // +3 fails when turned off
    USE_INTERNAL_OPENING_BOOK(true),
    USE_PV_SEARCH(true), // +28 fails when turned off
    USE_INTERNAL_ITERATIVE_DEEPENING(true),
    USE_SUPER_VERIFY_ON_HASH(false);
}