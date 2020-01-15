package com.netsensia.rivalchess.model;

public class MoveHistoryItem {
    MoveState m_moveState = null;
    Board m_board = null;

    public String getAlgebraicMove() {
        String move = this.m_moveState.getMove().getSrcBoardRef().getAlgebraic(this.m_board.getNumXFiles())
                + "-"
                + this.m_moveState.getMove().getTgtBoardRef().getAlgebraic(this.m_board.getNumXFiles());

        if (this.m_moveState.isPromotionPieceCode()) {
            move += this.m_moveState.getPromotionPieceCode();
        }

        return move;

    }
}