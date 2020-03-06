package com.netsensia.rivalchess.enums;

public enum CastleBitMask {
    CASTLEPRIV_WK (1),
    CASTLEPRIV_WQ  (2),
    CASTLEPRIV_BK (4),
    CASTLEPRIV_BQ (8),
    CASTLEPRIV_BNONE (~CastleBitMask.CASTLEPRIV_BK.getValue() & ~CastleBitMask.CASTLEPRIV_BQ.getValue()),
    CASTLEPRIV_WNONE (~CastleBitMask.CASTLEPRIV_WK.getValue() & ~CastleBitMask.CASTLEPRIV_WQ.getValue())
    ;

    private int value;

    private CastleBitMask(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}