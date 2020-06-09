package com.netsensia.rivalchess.bitboards;


public enum BitboardType {
    NONE(-1),
    WP(0),
    WN(1),
    WB(2),
    WQ(3),
    WK(4),
    WR(5),
    BP(6),
    BN(7),
    BB(8),
    BQ(9),
    BK(10),
    BR(11),
    ALL(12),
    FRIENDLY(13),
    ENEMY(14),
    ENPASSANTSQUARE(15)
    ;

    private int index;
    private static final int numBitboardTypes = 16;

    BitboardType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static BitboardType fromIndex(int index){
        for(BitboardType cp : BitboardType.values()){
            if(cp.index == index) {
                return cp;
            }
        }
        return BitboardType.NONE;
    }

    public static int getNumBitboardTypes() {
        return numBitboardTypes;
    }

}