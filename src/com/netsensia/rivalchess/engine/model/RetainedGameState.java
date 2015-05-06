package com.netadapt.rivalchess.model;

import com.netadapt.rivalchess.AppConstants;
import com.netadapt.rivalchess.model.board.BoardModel;
import com.netadapt.rivalchess.model.board.FenChess;

//
// Note : ALL objects placed in this retainable object MUST be clonable.
//

public class RetainedGameState  implements Cloneable
{
	
	protected BoardModel m_boardModel = null;
	protected BoardModel m_intialBoardModel = null;
	protected MoveHistoryContainer m_moveHistoryContainer = null;
	protected int m_uiState = AppConstants.UI_STATE_UNKNOWN;
	protected int m_lastUiState = AppConstants.UI_STATE_UNKNOWN;	
	protected boolean m_isEngineWhite = false;
	protected boolean m_isEngineBlack = false;	
	protected int m_gameStateId = AppConstants.GAMESTATE_NOTINCHECK;
	
	
	public RetainedGameState( )
	{
		super( );
		this.m_moveHistoryContainer = new MoveHistoryContainer( );;
		this.initialise( );					
	}	
	
	public void initialise()
	{		
		this.m_uiState = AppConstants.UI_STATE_UNKNOWN;
		this.m_lastUiState = AppConstants.UI_STATE_UNKNOWN;
		this.m_gameStateId = AppConstants.GAMESTATE_NOTINCHECK;
		this.initBoardModel( );
		this.m_moveHistoryContainer.removeAllElements( );		
	}
	
	public void initBoardModel( )
	{
		this.m_boardModel = new BoardModel( );
		FenChess fenChess = new FenChess( this.m_boardModel );
		fenChess.setFromStr( AppConstants.FEN_START_CHESS );
		this.m_intialBoardModel = this.m_boardModel.clone( );
	}
	
    synchronized public RetainedGameState clone(  )
    {       	
    	RetainedGameState newRetainedGameState;
		try
		{
			newRetainedGameState = (RetainedGameState) super.clone( );			    	
	    	if ( this.m_boardModel != null )
	    	{
	    		newRetainedGameState.setBoardModel( this.m_boardModel.clone( ));
	    		newRetainedGameState.setMoveHistoryContainer( this.m_moveHistoryContainer.clone( ));
	    	}
			return newRetainedGameState;    	
		} 
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}	
		return null;
    }
	
	
	public BoardModel getBoardModel()
	{
		return this.m_boardModel;
	}

	public void setBoardModel(BoardModel boardModel)
	{
		this.m_boardModel = boardModel;
	}


	public MoveHistoryContainer getMoveHistoryContainer()
	{
		return this.m_moveHistoryContainer;
	}


	public void setMoveHistoryContainer(MoveHistoryContainer moveHistoryContainer)
	{
		this.m_moveHistoryContainer = moveHistoryContainer;
	}	
	
	public boolean isEngineWhite()
	{
		return this.m_isEngineWhite;
	}

	public void setIsEngineWhite(boolean isEngineWhite)
	{
		this.m_isEngineWhite = isEngineWhite;
	}

	public boolean isEngineBlack()
	{
		return this.m_isEngineBlack;
	}

	public void setIsEngineBlack(boolean isEngineBlack)
	{
		this.m_isEngineBlack = isEngineBlack;
	}	
	
	public boolean isHumanPlayerOnly( )
	{
		return (! this.isEngineWhite( ) && ! this.isEngineBlack( )); 
	}
	
	public boolean isEnginePlayerOnly( )
	{
		return (this.isEngineWhite( ) && this.isEngineBlack( )); 
	}
	
	synchronized public int getUiState()
	{
		return this.m_uiState;
	}

	synchronized public void setUiState(int uiState)
	{			
		this.m_uiState = uiState;		
	}
	
	synchronized public boolean isEngineMoveRequired( )
	{
		return ((this.m_boardModel.isWhiteToMove( ) && this.isEngineWhite( )) || (this.m_boardModel.isBlackToMove( ) && this.isEngineBlack( )));
	}
	
	synchronized public boolean isNewUiState( int uiState )
	{
		int prevUiState = this.m_lastUiState;
		this.m_lastUiState = uiState;
		return (prevUiState != uiState );
	}
	
	public int getLastUiState()
	{
		return this.m_lastUiState;
	}

	synchronized public void setLastUiState(int lastUiState)
	{
		this.m_lastUiState = lastUiState;
	}
	
	public BoardModel getIntialBoardModel()
	{
		return this.m_intialBoardModel;
	}

	synchronized public void setIntialBoardModel(BoardModel intialBoardModel)
	{
		this.m_intialBoardModel = intialBoardModel;
	}
	
	public boolean isGameOverState( )
	{
		return ! this.isInPlayState( );	
	}

	synchronized public boolean isInPlayState( )
	{
		return (this.m_gameStateId == AppConstants.GAMESTATE_NOTINCHECK || 
				this.m_gameStateId == AppConstants.GAMESTATE_INCHECK );		
	}
	
	public boolean isGameDrawnState( )
	{
		return (this.m_gameStateId == AppConstants.GAMESTATE_THREEFOLD || 
				this.m_gameStateId == AppConstants.GAMESTATE_50MOVERULE ||
				this.m_gameStateId == AppConstants.GAMESTATE_STALEMATE ||
				this.m_gameStateId == AppConstants.GAMESTATE_AGREEDDRAW );							
	}
	
	public int getWinner( )
	{
		if ( this.isGameOverState( ) && ! this.isGameDrawnState( ))
		{
			if (this.getBoardModel().isWhiteToMove())
			{
				return AppConstants.PLAYER_BLACK;
			}
			else
			{
				return AppConstants.PLAYER_WHITE;
			}
		}
		return AppConstants.PLAYER_UNKNOWN;
	}

	public int getGameStateId()
	{
		return this.m_gameStateId;
	}

	synchronized public void setGameStateId(int gameStateId)
	{		
		this.m_gameStateId = gameStateId;		
	}
}
