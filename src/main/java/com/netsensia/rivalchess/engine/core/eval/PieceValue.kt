package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.model.Piece

fun pieceValue(piece: Piece): Int {
    return when (piece) {
        Piece.PAWN -> 100
        Piece.KNIGHT -> 390
        Piece.BISHOP -> 390
        Piece.ROOK -> 595
        Piece.KING -> 30000
        Piece.QUEEN -> 1175
        Piece.NONE -> 0
    }
}
