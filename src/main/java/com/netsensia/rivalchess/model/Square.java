package com.netsensia.rivalchess.model;

public class Square {
    private int xFile;
	private int yRank;

    public Square(int xFile, int yRank) {
        this.set(xFile, yRank);
    }

    public int getXFile() {
        return this.xFile;
    }

    public int getYRank() {
        return this.yRank;
    }

    public void set(int xFile, int yRank) {
        this.xFile = xFile;
        this.yRank = yRank;
    }

    public char getAlgebraicXFile() {
        return (char) (97 + this.getXFile());
    }

    public char getAlgebraicYRank(int boardRanks) {
        return Character.forDigit((boardRanks - this.getYRank()), 10);
    }

    public String getAlgebraic(int boardFiles) {
        return "" + this.getAlgebraicXFile() + getAlgebraicYRank(boardFiles);
    }

    @Override
    public String toString() {
        return " " + this.getXFile() + ":" + this.getYRank();
    }

	@Override
	public boolean equals(Object o) {
        if (o instanceof Square) {
            Square br = (Square) o;
            return (br.getXFile() == this.xFile && br.getYRank() == this.yRank);
        }
        return false;
    }

    @Override
	public int hashCode() {
		return this.xFile * 8 + this.yRank;
	}
}
