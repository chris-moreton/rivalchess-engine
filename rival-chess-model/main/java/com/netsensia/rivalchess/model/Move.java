package com.netsensia.rivalchess.model;

public class Move {
    protected int srcXFile;
    protected int srcYRank;

    protected int m_tgtXFile;
    protected int m_tgtYRank;

    protected String m_promotedPieceCode = "";

    public Move(Square srcBoardRef, Square tgtBoardRef) {
        this(srcBoardRef.getXFile(), srcBoardRef.getYRank(), tgtBoardRef.getXFile(), tgtBoardRef.getYRank());
    }

    public Move(int srcXFile, int srcYRank, int tgtXFile, int tgtYRank) {
        this.set(srcXFile, srcYRank, tgtXFile, tgtYRank);
    }

    public Square getSrcBoardRef() {
        return new Square(this.getSrcXFile(), this.getSrcYRank());
    }

    public Square getTgtBoardRef() {
        return new Square(this.getTgtXFile(), this.getTgtYRank());
    }

    public int getSrcXFile() {
        return this.srcXFile;
    }

    public int getSrcYRank() {
        return this.srcYRank;
    }

    public int getTgtXFile() {
        return this.m_tgtXFile;
    }

    public int getTgtYRank() {
        return this.m_tgtYRank;
    }

    public void set(int srcXFile, int srcYRank, int tgtXFile, int tgtYRank) {
        this.srcXFile = srcXFile;
        this.srcYRank = srcYRank;

        this.m_tgtXFile = tgtXFile;
        this.m_tgtYRank = tgtYRank;
    }

    public void setPromotedPieceCode(String promotedPieceCode) {
        this.m_promotedPieceCode = promotedPieceCode;
    }

    public String getPromotedPieceCode() {
        return this.m_promotedPieceCode;
    }

    @Override
    public String toString() {
        return
                this.getSrcBoardRef().getAlgebraic(Board.DEFAULT_BOARD_NUM_RANKS) + "-" +
                        this.getTgtBoardRef().getAlgebraic(Board.DEFAULT_BOARD_NUM_RANKS) +
                        this.m_promotedPieceCode == " " ? "" : this.m_promotedPieceCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Move) {
            Move move = (Move) o;
            return (move.getSrcBoardRef().equals(this.getSrcBoardRef())
                    && move.getTgtBoardRef().equals(this.getTgtBoardRef())
                    && move.getPromotedPieceCode() == this.getPromotedPieceCode());
            }
        return false;
    }

}
