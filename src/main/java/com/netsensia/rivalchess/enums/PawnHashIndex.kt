package com.netsensia.rivalchess.enums

enum class PawnHashIndex(val index: Int) {
    PAWNHASHENTRY_MAIN_SCORE(0), PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE(1), PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE(2), PAWNHASHENTRY_WHITE_PASSEDPAWNS(3), PAWNHASHENTRY_BLACK_PASSEDPAWNS(4), PAWNHASHENTRY_LOCK(5);

    companion object {
        const val numHashFields = 6

        /* pointer to array */
        val hashPositionSizeBytes: Int
            get() = 8 +  /* pointer to array */numHashFields * 8
    }

}