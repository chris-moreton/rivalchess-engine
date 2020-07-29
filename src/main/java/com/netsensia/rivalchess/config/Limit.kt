package com.netsensia.rivalchess.config

const val MAX_LEGAL_MOVES = 220
const val MAX_SEARCH_MILLIS = 60 * 60 * 1000
const val MIN_SEARCH_MILLIS = 25
const val MAX_SEARCH_DEPTH = 125
const val MAX_QUIESCE_DEPTH = 50
const val MAX_HALFMOVES_IN_GAME = 750
const val MAX_TREE_DEPTH = MAX_SEARCH_DEPTH + MAX_QUIESCE_DEPTH + MAX_EXTENSION_DEPTH + 1
