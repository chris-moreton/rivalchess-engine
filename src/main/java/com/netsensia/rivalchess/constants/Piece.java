package com.netsensia.rivalchess.constants;

public enum Piece {
    PAWN  (100),
    KNIGHT(390),
    ROOK   (595),
    KING (30000),
    QUEEN (1175),
    BISHOP (390)
    ;

    private int value;

    private Piece(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}