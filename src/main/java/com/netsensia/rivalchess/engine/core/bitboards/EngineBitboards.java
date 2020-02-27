package com.netsensia.rivalchess.engine.core.bitboards;

import com.netsensia.rivalchess.engine.core.RivalConstants;

import java.util.Arrays;

public class EngineBitboards {

    /**
     * @deprecated Use getters and setters
     */
    @Deprecated
    public long[] pieceBitboards;

    public void setPieceBitboard(int i, long bitboard) {
        pieceBitboards[i] = bitboard;
    }

    public long getPieceBitboard(int i) {
        return pieceBitboards[i];
    }

    public void xorPieceBitboard(int i, long xorBy) {
        this.pieceBitboards[i] ^= xorBy;
    }

    public void reset() {
        pieceBitboards = new long[RivalConstants.NUM_BITBOARDS];
        Arrays.fill(pieceBitboards, 0);
    }
}
