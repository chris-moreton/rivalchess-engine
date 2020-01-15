package com.netsensia.rivalchess.model;

public class MoveState {
    protected char pieceCode = '_';
    protected char promotionPieceCode = '_';
    protected char capturedPieceCode = '_';
    protected Move move;
    protected boolean isCastleKingSide = false;
    protected boolean isCastleQueenSide = false;

	public MoveState() {

	}

	public MoveState(MoveState source) {
		this.move = new Move(this.move);
		this.pieceCode = source.pieceCode;
		this.isCastleKingSide = source.isCastleKingSide;
		this.isCastleQueenSide = source.isCastleQueenSide;
		this.promotionPieceCode = source.promotionPieceCode;
		this.capturedPieceCode = source.capturedPieceCode;
	}

    public void setPieceCode(char pieceCode) {
        this.pieceCode = pieceCode;
    }

    public char getPieceCode() {
        return pieceCode;
    }

    public void setCastleKingSide(boolean isCastleKingSide) {
        this.isCastleKingSide = isCastleKingSide;
    }

    public boolean isCastleKingSide() {
        return isCastleKingSide;
    }

    public void setCastleQueenSide(boolean isCastleQueenSide) {
        this.isCastleQueenSide = isCastleQueenSide;
    }

    public boolean isCastleQueenSide() {
        return isCastleQueenSide;
    }

    public boolean isPromotionPieceCode() {
        return (this.promotionPieceCode != '_');
    }

    public void setPromotionPieceCode(char promotionPieceCode) {
        this.promotionPieceCode = promotionPieceCode;
    }

    public char getPromotionPieceCode() {
        return promotionPieceCode;
    }

    public boolean isCapturedPieceCode() {
        return (this.capturedPieceCode != '_');
    }

    public void setCapturedPieceCode(char capturedPieceCode) {
        this.capturedPieceCode = capturedPieceCode;
    }

    public Move getMove() {
        return this.move;
    }

    public void setLastMove(Move move) {
        this.move = move;
    }

}