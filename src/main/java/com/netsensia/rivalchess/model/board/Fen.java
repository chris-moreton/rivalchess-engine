package com.netsensia.rivalchess.model.board;



abstract class Fen
{
	protected boolean m_isValidFen 	= false;	
	protected String m_fenStr = "";
	protected BoardModel m_board;
	
	abstract public boolean setFromStr( String fenStr );
	abstract public String getFen( );
	
	public Fen( BoardModel board )
	{
		this.m_board = board;
	}

	public boolean isValid( )
	{
		return this.m_isValidFen;
	}	
	
	 public BoardModel getBoard( )
	 {
		 return this.m_board;
	 }
}