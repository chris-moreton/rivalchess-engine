package com.netadapt.rivalchess.model;

import com.netadapt.rivalchess.model.board.BoardModel;
import com.netadapt.rivalchess.model.board.MoveState;

public class MoveHistoryItem 
{
	int m_moveNumber = 0;	
	long m_moveTimeMilliSeconds = 0;
	String m_algerbraicMove = "";
	
	MoveState m_moveState = null;		
	BoardModel m_boardModel = null;
			
	//@deprecated
	public String getMoveNotationStr( boolean isPiecePrefix )
	{					
		if ( this.m_moveState.isCastleQueenSide( ))
		{
			return "O-O-O";	
		}
		else if ( this.m_moveState.isCastleKingSide( ))
		{			
			return "O-O";		
		}
		else
		{				
			String actionVal_str = "";
			String from_str = this.m_moveState.getMoveRef( ).getSrcBoardRef( ).getAlgebraic( this.m_boardModel.getNumXFiles( ));
			String to_str 	= this.m_moveState.getMoveRef( ).getTgtBoardRef( ).getAlgebraic( this.m_boardModel.getNumXFiles( ));
			
			//Log.i("getCapturedPieceCode", "" + this.m_moveState.getCapturedPieceCode( ));
			
			if ( this.m_moveState.isCapturedPieceCode( ) )
			{
				actionVal_str = "x";
			}			
			
			String fullWrittenMove_str = from_str + actionVal_str + to_str;
			
			if  ( this.m_moveState.isPromotionPieceCode( ) )
			{
				fullWrittenMove_str += '=' + Character.toString( this.m_moveState.getPromotionPieceCode( ));
			}		
			
			if ( isPiecePrefix )
			{
				String piecePrefix = Character.toString( this.m_moveState.getPieceCode( ));
				if ( piecePrefix.toUpperCase( ) == "P" ||  this.m_moveState.isPromotionPieceCode( )) //ignore pawns
				{
					piecePrefix = "";
				}								
				fullWrittenMove_str = piecePrefix + fullWrittenMove_str;
			}		
			return 	fullWrittenMove_str;
		}
	}	
	
	
	public String getAlgebraicMove( )
	{
		//return this.m_moveState.getMoveRef( ).getSrcBoardRef( ).getAlgebraic( this.m_boardModel.getNumXFiles( )) + "-" + this.m_moveState.getMoveRef( ).getTgtBoardRef( ).getAlgebraic( this.m_boardModel.getNumXFiles( ));
		String move = this.m_moveState.getMoveRef().getSrcBoardRef().getAlgebraic(this.m_boardModel.getNumXFiles())
				+ "-"
				+ this.m_moveState.getMoveRef().getTgtBoardRef().getAlgebraic(this.m_boardModel.getNumXFiles());

		if (this.m_moveState.isPromotionPieceCode())
		{
			move += this.m_moveState.getPromotionPieceCode();
		}
		
		return move;
		
	}
	public int getMoveNumber( )
	{
		return this.m_moveNumber;
	}
	
	public long getMoveTimeMilliSeconds( )
	{
		return this.m_moveTimeMilliSeconds;
	}
			
	/*public void setAlgerbraicMove(String algerbraicMove)
	{
		this.m_algerbraicMove = algerbraicMove;
	}*/
	
	public void setMoveNumber(int moveNumber)
	{
		this.m_moveNumber = moveNumber;
	}
	
	public void setMoveTimeMilliSeconds(long  moveTimeSeconds)
	{
		this.m_moveTimeMilliSeconds = moveTimeSeconds;
	}
				
	public boolean isMoveState( )
	{
		return this.m_moveState != null;
	}
	
	public BoardModel getBoardModel()
	{
		return this.m_boardModel;
	}
	
	public void setBoardModel(BoardModel boardModel)
	{
		this.m_boardModel = boardModel.clone( );
	}
	
	public void dbgEchoState( )
	{
		//Log.i( "MOVE ITEM STATE", this.getMoveNumber( ) + "][" + (this.getBoardModel().isWhiteToMove()?"White to move":"Black to move") + " " + this.getBoardModel().getInstanceStamp( ));
	}
	
	public MoveState getMoveState()
	{
		return this.m_moveState;
	}
	
	public void setMoveState(MoveState moveState)
	{
		this.m_moveState = moveState;
	}


}