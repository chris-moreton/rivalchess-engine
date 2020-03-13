package com.netsensia.rivalchess.engine.core.type;

import com.netsensia.rivalchess.enums.PromotionPieceMask;
import com.netsensia.rivalchess.model.Move;
import com.netsensia.rivalchess.model.SquareOccupant;
import com.netsensia.rivalchess.util.ChessBoardConversion;

public final class EngineMove {
    public final int compact;

    public EngineMove(final int compact) {
        this.compact = compact;
    }

    public EngineMove(final Move move) {
        final int from = ChessBoardConversion.getBitRefFromBoardRef(move.getSrcBoardRef());
        final int to = ChessBoardConversion.getBitRefFromBoardRef(move.getTgtBoardRef());
        final SquareOccupant promotionPiece = move.getPromotedPiece();

        final int promotionPart = promotionPiece == SquareOccupant.NONE
                ? 0
                : PromotionPieceMask.fromPiece(promotionPiece.getPiece()).getValue();


        this.compact = to + (from << 16) + (promotionPart << 32);
    }
}
