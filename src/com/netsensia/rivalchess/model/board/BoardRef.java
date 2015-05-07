package com.netsensia.rivalchess.model.board;

public class BoardRef
{
	protected int m_xFile;
	protected int m_yRank;

	public BoardRef( )
	{
		//
	}
	
	public BoardRef( int xFile, int yRank )
	{
		this.set( xFile, yRank );
	}

	public int getXFile( )
	{
		return this.m_xFile;
	}

	public int getYRank( )
	{
		return this.m_yRank;
	}

	public void set( int xFile, int yRank )
	{
		this.m_xFile = xFile;
		this.m_yRank = yRank;
	}

	public void setAlgebaicFile( char fileChar )
	{
		this.m_xFile =  fileChar - 97;
	}
	
	public void setAlgebaicRank( char rankChar )
	{
		this.m_yRank = Character.getNumericValue( rankChar )-1;
	}
		
	public char getAlgebraicXFile( int boardFiles )
    {
        // a = 97
        // z = 122
        return  (char)( 97+this.getXFile( ));
    }

	public char getAlgebraicYRank( int boardRanks )
    {
        return Character.forDigit((boardRanks - this.getYRank( )), 10);
    }
	
	public String getAlgebraic( int boardFiles )
	{
		return "" + this.getAlgebraicXFile( boardFiles )+getAlgebraicYRank( boardFiles );
	}	
	
	public boolean sameAs(BoardRef boardRef )
	{
		return  (boardRef != null && this.getXFile( ) == boardRef.getXFile( ) && this.getYRank( ) == boardRef.getYRank( ));	
	
	}
	
	@Override
	public String toString( )
	{
		return " " + this.getXFile( ) + ":" +  this.getYRank( );
	}
	
	 public boolean equals(Object o)
	 {
		 boolean isEqual = false;
		 if (o instanceof BoardRef)
		 {
			 BoardRef br = (BoardRef)o;
			 if (br.getXFile() == this.m_xFile && br.getYRank() == this.m_yRank)
			 {
				 isEqual = true;
			 }
		 }
		 return isEqual;
	 }
}
