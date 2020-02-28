package com.netsensia.rivalchess.constants;

public enum Piece {
    NONE (0),
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

    public static Piece fromSquareOccupant(SquareOccupant squareOccupant) {
        switch (squareOccupant) {
            case WP:
            case BP:
                return Piece.PAWN;
            case WR:
            case BR:
                return Piece.ROOK;
            case WB:
            case BB:
                return Piece.BISHOP;
            case WN:
            case BN:
                return Piece.KNIGHT;
            case WQ:
            case BQ:
                return Piece.QUEEN;
            case WK:
            case BK:
                return Piece.KING;
            default:
                return Piece.NONE;
        }
    }

    public static Piece fromIndex(int index){
        for(Piece cp : Piece.values()){
            if(cp.value == index) {
                return cp;
            }
        }
        return Piece.NONE;
    }
}