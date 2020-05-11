package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.model.Piece;

public class PieceValue {

    public static int getValue(Piece piece) {
        switch (piece) {
            case PAWN: return 100;
            case KNIGHT: return 390;
            case BISHOP: return 390;
            case ROOK: return 595;
            case KING: return 30000;
            case QUEEN: return 1175;
            case NONE: return 0;
            default:
                throw new RuntimeException("Unknown piece");
        }
    }
}
