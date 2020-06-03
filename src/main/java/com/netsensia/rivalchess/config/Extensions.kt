package com.netsensia.rivalchess.config

import java.util.*

enum class Extensions(val value: Int) {
    FRACTIONAL_EXTENSION_FULL(8), FRACTIONAL_EXTENSION_THREAT(8), FRACTIONAL_EXTENSION_CHECK(8), FRACTIONAL_EXTENSION_RECAPTURE(8),
    FRACTIONAL_EXTENSION_PAWN(8), RECAPTURE_EXTENSION_MARGIN(50), LAST_EXTENSION_LAYER(4);

    companion object {
        val maxNewExtensionsTreePart: List<Int>
            get() = Collections.unmodifiableList(Arrays.asList(
                    FRACTIONAL_EXTENSION_FULL.value,
                    FRACTIONAL_EXTENSION_FULL.value / 4 * 3,
                    FRACTIONAL_EXTENSION_FULL.value / 2,
                    FRACTIONAL_EXTENSION_FULL.value / 8, 0))
    }

}