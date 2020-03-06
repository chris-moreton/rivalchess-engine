package com.netsensia.rivalchess.enums;

public enum BoardDirection {
    E (0),
    SE  (1),
    S (2),
    SW (3),
    W (4),
    NW (5),
    N (6),
    NE (7)
    ;

    private int index;

    private BoardDirection(int index) {
        this.index = index;
    }

    public static BoardDirection fromIndex(int index){
        for(BoardDirection b : BoardDirection.values()){
            if(b.index == index) {
                return b;
            }
        }
        throw new RuntimeException("Attempt was made to get invalid BoardDirection enum");
    }

    public int getIndex() {
        return index;
    }
}
