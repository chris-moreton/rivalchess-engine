package com.netsensia.rivalchess.constants;

import com.netsensia.rivalchess.engine.core.RivalConstants;

public enum SquareOccupant {
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
    BR(11)
    ;

    private int index;

    private SquareOccupant(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int ofColour(int colour) {
        return index == SquareOccupant.NONE.getIndex() ? index :
                colour == RivalConstants.WHITE
                ? index % 6
                : index % 6 + 6;
    }

    public static SquareOccupant fromIndex(int index){
        for(SquareOccupant cp : SquareOccupant.values()){
            if(cp.index == index) {
                return cp;
            }
        }
        return SquareOccupant.NONE;
    }
}