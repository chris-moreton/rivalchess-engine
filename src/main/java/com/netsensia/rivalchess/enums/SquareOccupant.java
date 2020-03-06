package com.netsensia.rivalchess.enums;

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

    public int ofColour(Colour colour) {
        return index == SquareOccupant.NONE.getIndex() ? index :
                colour == Colour.WHITE
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

    public Colour getColour() {
        if (index == SquareOccupant.NONE.getIndex()) {
            throw new RuntimeException("Can't get piece colour of an empty square");
        }

        return index <= SquareOccupant.WR.getIndex() ? Colour.WHITE : Colour.BLACK;
    }

    public Piece getPiece() {
        switch (this) {
            case NONE:
                return Piece.NONE;
            case WP:
            case BP:
                return Piece.PAWN;
            case WK:
            case BK:
                return Piece.KING;
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
            default:
                throw new RuntimeException("Invalid SquareOccupant");
        }
    }

    public static SquareOccupant fromString(String piece){
        switch (piece.trim()) {
            case "q":
                return SquareOccupant.BQ;
            case "r":
                return SquareOccupant.BR;
            case "n":
                return SquareOccupant.BN;
            case "b":
                return SquareOccupant.BB;
            case "Q":
                return SquareOccupant.WQ;
            case "R":
                return SquareOccupant.WR;
            case "N":
                return SquareOccupant.WN;
            case "B":
                return SquareOccupant.WB;
            default:
                return SquareOccupant.NONE;
        }

    }
}