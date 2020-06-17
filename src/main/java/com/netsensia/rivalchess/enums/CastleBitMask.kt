package com.netsensia.rivalchess.enums

enum class CastleBitMask(val value: Int) {
    CASTLEPRIV_WK(1),
    CASTLEPRIV_WQ(2),
    CASTLEPRIV_BK(4),
    CASTLEPRIV_BQ(8),
    CASTLEPRIV_BNONE(CASTLEPRIV_BK.value.inv() and CASTLEPRIV_BQ.value.inv()),
    CASTLEPRIV_WNONE(CASTLEPRIV_WK.value.inv() and CASTLEPRIV_WQ.value.inv());
}