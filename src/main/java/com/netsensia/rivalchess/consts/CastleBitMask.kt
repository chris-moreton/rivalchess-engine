package com.netsensia.rivalchess.consts

const val CASTLEPRIV_WK = 1
const val CASTLEPRIV_WQ = 2
const val CASTLEPRIV_BK = 4
const val CASTLEPRIV_BQ = 8
const val CASTLEPRIV_BNONE = CASTLEPRIV_BK.inv() and CASTLEPRIV_BQ.inv()
const val CASTLEPRIV_WNONE = CASTLEPRIV_WK.inv() and CASTLEPRIV_WQ.inv()
