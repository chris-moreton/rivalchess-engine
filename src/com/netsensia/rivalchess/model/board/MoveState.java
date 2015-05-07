package com.netsensia.rivalchess.model.board;

public class MoveState implements Cloneable
{
	protected char m_pieceCode = '_';
	protected char m_promotionPieceCode = '_';
	protected char m_capturedPieceCode = '_';    
	protected MoveRef m_moveRef;
	protected boolean m_isCastleKingSide = false;
	protected boolean m_isCastleQueenSide = false;
	
	public void setPieceCode(char pieceCode)
	{
		this.m_pieceCode = pieceCode;
	}
	
	public boolean isPieceCode( )
	{
		return ( this.m_pieceCode != '_');		
	}
	
	public char getPieceCode( )
	{
		return m_pieceCode;
	}
	
	public void setCastleKingSide(boolean isCastleKingSide)
	{
		this.m_isCastleKingSide = isCastleKingSide;
	}
	
	public boolean isCastleKingSide()
	{
		return m_isCastleKingSide;
	}
	
	public void setCastleQueenSide(boolean isCastleQueenSide)
	{
		this.m_isCastleQueenSide = isCastleQueenSide;
	}
	
	public boolean isCastleQueenSide( )
	{
		return m_isCastleQueenSide;
	}
	
	public boolean isPromotionPieceCode( )
	{
		return ( this.m_promotionPieceCode != '_');		
	}
	
	public void setPromotionPieceCode(char promotionPieceCode)
	{
		this.m_promotionPieceCode = promotionPieceCode;
	}
	
	public char getPromotionPieceCode( )
	{
		return m_promotionPieceCode;
	}
	
	public boolean isCapturedPieceCode( )
	{
		return ( this.m_capturedPieceCode != '_');		
	}
	
	public void setCapturedPieceCode(char capturedPieceCode)
	{
		this.m_capturedPieceCode = capturedPieceCode;
	}
	
	public char getCapturedPieceCode( )
	{
		return m_capturedPieceCode;
	}

	public MoveRef getMoveRef()
	{
		return this.m_moveRef;
	}

	public void setLastMove(MoveRef moveRef)
	{
		this.m_moveRef = moveRef;
	}
	
	public MoveState clone(  )
	{
	   	MoveState moveState;
		try
		{
			moveState = (MoveState) super.clone( );			
			moveState.m_moveRef 			= this.m_moveRef.clone( );			
			moveState.m_pieceCode 			= this.m_pieceCode;
			moveState.m_isCastleKingSide 	= this.m_isCastleKingSide; 
			moveState.m_isCastleQueenSide 	= this.m_isCastleQueenSide;
			moveState.m_promotionPieceCode 	= this.m_promotionPieceCode;
			moveState.m_capturedPieceCode 	= this.m_capturedPieceCode; 
					
			return moveState;    	
		} 
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}	
		return null;
	}
	
}