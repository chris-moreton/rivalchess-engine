package com.netadapt.rivalchess.model.board;


public class MoveRef implements Cloneable
{	
	protected int m_srcXFile;
	protected int m_srcYRank;
	
	protected int m_tgtXFile;
	protected int m_tgtYRank;
			
	protected char m_promotedPieceCode;
	
	public MoveRef( BoardRef srcBoardRef, BoardRef tgtBoardRef )
	{
		this( srcBoardRef.getXFile(), srcBoardRef.getYRank( ), tgtBoardRef.getXFile(), tgtBoardRef.getYRank( ) );
		this.m_promotedPieceCode = '#';
	}
	
	public MoveRef( int srcXFile, int srcYRank, int tgtXFile, int tgtYRank )	
	{		
		this.set( srcXFile, srcYRank, tgtXFile, tgtYRank );		
	}
	
	public BoardRef getSrcBoardRef( )
	{
		return new BoardRef( this.getSrcXFile( ),  this.getSrcYRank( ));
	}
	
	public BoardRef getTgtBoardRef( )
	{
		return new BoardRef( this.getTgtXFile( ),  this.getTgtYRank( ));
	}
	
	public int getSrcXFile( )
	{
		return this.m_srcXFile;
	}

	public int getSrcYRank( )
	{
		return this.m_srcYRank;
	}
	
	public int getTgtXFile( )
	{
		return this.m_tgtXFile;
	}

	public int getTgtYRank( )
	{
		return this.m_tgtYRank;
	}
	
	public boolean isTravel( )
	{
		return ( this.getSrcXFile( ) != this.getTgtXFile( ) ||  this.getSrcYRank( ) != this.getTgtYRank( ));
	}

	public void set( int srcXFile, int srcYRank, int tgtXFile, int tgtYRank )
	{
		this.m_srcXFile = srcXFile;
		this.m_srcYRank = srcYRank;
		
		this.m_tgtXFile = tgtXFile;
		this.m_tgtYRank = tgtYRank;
	}	
		
	public void setPromotedPieceCode( char promotedPieceCode )
	{
		this.m_promotedPieceCode = promotedPieceCode;
	}
	
	public char getPromotedPieceCode( )
	{
		return this.m_promotedPieceCode;
	}
	
	public boolean isPromotedPieceCode( )
	{
		return this.m_promotedPieceCode != '#';
	}
	
	public boolean sameAs(MoveRef moveRef )
	{
		return  (
				this.getSrcXFile( ) == moveRef.getSrcXFile( ) && 
				this.getSrcYRank( ) ==  moveRef.getSrcYRank( ) &&	
				this.getTgtXFile( ) == moveRef.getTgtXFile( ) && 
				this.getTgtYRank( ) ==  moveRef.getTgtYRank( )
				);	
	
	}
	
			
	 public MoveRef clone(  )
	 {
	   	MoveRef moveRef;
		try
		{
			moveRef = (MoveRef) super.clone( );
			moveRef.m_srcXFile = this.m_srcXFile;
			moveRef.m_srcYRank = this.m_srcYRank;
			
			moveRef.m_tgtXFile = this.m_tgtXFile;
			moveRef.m_tgtYRank = this.m_tgtYRank;
			moveRef.m_promotedPieceCode = this.m_promotedPieceCode;
						
			return moveRef;    	
		} 
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}	
		return null;
	 }
	
	 @Override
	 public String toString( )
	 {
		 return this.getSrcBoardRef( ).getAlgebraic( BoardModelConstants.DEFAULT_BOARD_NUM_RANKS ) + "-" + this.getTgtBoardRef().getAlgebraic( BoardModelConstants.DEFAULT_BOARD_NUM_RANKS );
	 }
	 
	
	public String toDebugString( )
	{
		return "[" + this.m_srcXFile + "," + this.m_srcYRank + "][" + this.m_tgtXFile + "," + this.m_tgtYRank + "]" + (isPromotedPieceCode()?this.getPromotedPieceCode() + " promoted piece code":"");
	}
}
