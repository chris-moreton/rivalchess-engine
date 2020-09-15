package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.MIDDLE_FILES_8_BIT
import com.netsensia.rivalchess.bitboards.NONMID_FILES_8_BIT
import com.netsensia.rivalchess.bitboards.RANK_1
import com.netsensia.rivalchess.bitboards.util.popCount
import com.netsensia.rivalchess.bitboards.util.southFill
import com.netsensia.rivalchess.config.KINGSAFTEY_HALFOPEN_MIDFILE
import com.netsensia.rivalchess.config.KINGSAFTEY_HALFOPEN_NONMIDFILE

fun openFiles(kingShield: Long, pawnBitboard: Long) = southFill(kingShield) and southFill(pawnBitboard).inv() and RANK_1

fun openFilesKingShieldEval(kingShield: Long, pawnBitboard: Long): Int {
    val openFiles = openFiles(kingShield, pawnBitboard)
    return if (openFiles != 0L) {
        KINGSAFTEY_HALFOPEN_MIDFILE * popCount(openFiles and MIDDLE_FILES_8_BIT) +
                KINGSAFTEY_HALFOPEN_NONMIDFILE * popCount(openFiles and NONMID_FILES_8_BIT)
    } else 0
}
