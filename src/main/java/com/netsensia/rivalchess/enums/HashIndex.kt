package com.netsensia.rivalchess.enums

import com.netsensia.rivalchess.config.USE_SUPER_VERIFY_ON_HASH

enum class HashIndex(val index: Int) {
    MOVE(0), SCORE(1), HASHENTRY_HEIGHT(2), FLAG(3), VERSION(4), HASHENTRY_64BIT1(5), HASHENTRY_64BIT2(6), HASHENTRY_LOCK1(7);

    companion object {
        const val numIndexes = 6
        val numHashFields: Int
            get() = if (USE_SUPER_VERIFY_ON_HASH) 31 else 7

        /* pointer to array */
        val hashPositionSizeBytes: Int
            get() = 8 +  /* pointer to array */numHashFields * 4
    }

}