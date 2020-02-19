package com.netsensia.rivalchess.engine.core.bitboards;

public class EngineBitboards {

    @Deprecated
    public long[] pieceBitboards;

    public long[] getPieceBitboards() {
        return pieceBitboards;
    }

    public void setPieceBitboards(long[] pieceBitboards) {
        this.pieceBitboards = pieceBitboards;
    }

    public void setPieceBitboard(int i, long bitboard) {
        pieceBitboards[i] = bitboard;
    }

    public long getPieceBitboard(int i) {
        return pieceBitboards[i];
    }
}
