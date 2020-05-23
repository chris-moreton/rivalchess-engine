package com.netsensia.rivalchess.config

enum class Limit(val value: Int) {
    MAX_GAME_MOVES(500), MAX_NODES_TO_SEARCH(Int.MAX_VALUE), MAX_LEGAL_MOVES(220), MAX_SEARCH_MILLIS(60 * 60 * 1000),  // 1 hour
    MIN_SEARCH_MILLIS(25), MAX_SEARCH_DEPTH(125), MAX_QUIESCE_DEPTH(50), MAX_EXTENSION_DEPTH(60), MAX_TREE_DEPTH(MAX_SEARCH_DEPTH.value +
            MAX_QUIESCE_DEPTH.value +
            MAX_EXTENSION_DEPTH.value + 1);

}