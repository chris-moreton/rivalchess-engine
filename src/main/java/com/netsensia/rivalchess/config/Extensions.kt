package com.netsensia.rivalchess.config

import java.util.*

const val FRACTIONAL_EXTENSION_FULL = 8
const val FRACTIONAL_EXTENSION_THREAT = 8
const val FRACTIONAL_EXTENSION_CHECK = 8
const val FRACTIONAL_EXTENSION_RECAPTURE = 8
const val FRACTIONAL_EXTENSION_PAWN = 8
const val RECAPTURE_EXTENSION_MARGIN = 50
const val LAST_EXTENSION_LAYER = 4

val maxNewExtensionsTreePart = intArrayOf(
                    FRACTIONAL_EXTENSION_FULL,
                    FRACTIONAL_EXTENSION_FULL / 4 * 3,
                    FRACTIONAL_EXTENSION_FULL / 2,
                    FRACTIONAL_EXTENSION_FULL / 8, 0)
