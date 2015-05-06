package com.netadapt.rivalchess.engine;

import java.util.EventObject;

import java.util.Vector;
import java.util.Iterator;

import com.netadapt.rivalchess.AppConstants;
import com.netadapt.rivalchess.engine.core.Bitboards;
import com.netadapt.rivalchess.engine.core.EngineChessBoard;
import com.netadapt.rivalchess.engine.core.RivalConstants;
import com.netadapt.rivalchess.engine.core.RivalSearch;
import com.netadapt.rivalchess.engine.core.SearchPath;
import com.netadapt.rivalchess.model.MoveHistoryContainer;
import com.netadapt.rivalchess.model.MoveHistoryItem;
import com.netadapt.rivalchess.model.board.BoardModel;
import com.netadapt.rivalchess.model.board.BoardRef;
import com.netadapt.rivalchess.model.board.MoveRef;
import com.netadapt.rivalchess.util.ChessBoardConversion;

public class EngineStub implements EngineServiceInterface
{
	protected Bitboards m_bitboards = null;
	public RivalSearch m_rivalSearch = null;
	protected EngineChessBoard m_engineBoard = null;
	protected MoveRef m_currentEngineMoveRef = null;
	protected int m_searchType = RivalConstants.SEARCH_TYPE_TIME;
	private int m_engineDifficulty = 4000;
	private int m_hashSizeMB = RivalConstants.DEFAULT_HASHTABLE_SIZE_MB;
	private int legalMoves[] = new int[RivalConstants.MAX_LEGAL_MOVES];
	
	public EngineStub(  )
	{							
		this.initStub( new Bitboards() );
	}

	public EngineStub( Bitboards bitboards )
	{
		this.initStub( bitboards );
	}
	
	private void initStub( Bitboards bitboards )
	{
		this.m_bitboards = bitboards;
		this.m_rivalSearch = new RivalSearch();
		this.m_rivalSearch.startEngineTimer(false);
		this.m_engineBoard = new EngineChessBoard(m_bitboards);
		
		ChessBoardConversion.getBoardRefFromBitRef( 0 ); //Force static initialisation of BitBoards.
	}
	
	
	public void newGame()
	{
		this.cancelThink( );
		// prepare the engine for a new game so that it can reset its opening books and clear its hash tables
		m_rivalSearch.newGame();
	}
	
	synchronized public int numLegalMoves(EngineChessBoard engineBoard)
	{
		int numMoves = 0;
		int i = 0;

		int legalMoves[] = new int[RivalConstants.MAX_LEGAL_MOVES];
		engineBoard.setLegalMoves(legalMoves);
		while (legalMoves[i] != 0)
		{
			if (engineBoard.makeMove(legalMoves[i]))
			{
				numMoves++;
				engineBoard.unMakeMove();
			}
			i++;

		}
		return numMoves;
	}

	synchronized public int numLegalMoves(BoardModel board)
	{
		m_engineBoard.setBoard(board);
		return numLegalMoves(m_engineBoard);

	}
	
	synchronized public boolean isMoveAvailable(BoardModel boardModel)
	{
		return this.numLegalMoves( boardModel ) > 0;
	}
	
	@Override
	synchronized public BoardRef[] getLegalMoves(BoardModel board, BoardRef boardRef)
	{
		//Log.i("getLegalMoves", "Selected square = " + ChessBoardConversion.getSimpleAlgebraicFromBitRef(ChessBoardConversion.getBitRefFromBoardRef(boardRef)));

		m_engineBoard.setBoard(board);
		
		m_engineBoard.setLegalMoves(legalMoves);
		
		Vector<BoardRef> boardRefVector = new Vector<BoardRef>();
		
		int moveNum = 0;
		BoardRef boardRefFrom;
		while (legalMoves[moveNum] != 0)
		{
			boardRefFrom = ChessBoardConversion.getBoardRefFromBitRef((legalMoves[moveNum] >>> 16) & 63);
			
			if (boardRefFrom.equals(boardRef))
			{
				if (m_engineBoard.makeMove(legalMoves[moveNum]))
				{
					boardRefVector.add(ChessBoardConversion.getBoardRefFromBitRef(legalMoves[moveNum] & 63));
					m_engineBoard.unMakeMove();
				}
			}
			moveNum ++;
		}
		
		BoardRef boardRefArray[] = new BoardRef[boardRefVector.size()];
		Iterator<BoardRef> i = boardRefVector.iterator();
		moveNum = 0;
		while (i.hasNext())
		{
			boardRefArray[moveNum] = i.next();
			//Log.i("Piece Can Move To", ChessBoardConversion.getSimpleAlgebraicFromBitRef(ChessBoardConversion.getBitRefFromBoardRef(boardRefArray[moveNum])));
			moveNum ++;
		}
		
		return boardRefArray;
	}
	
	public void clearHash()
	{
		m_rivalSearch.clearHash();
	}
	
	@Override
	synchronized public boolean isSearchComplete()
	{
		return !this.m_rivalSearch.isSearching();
	}	
	
	synchronized public void startEngine( BoardModel board, MoveHistoryContainer moveHistory )
	{
		this.getEngineMove( board, moveHistory, this.m_engineDifficulty );
	}
	
	synchronized public SearchPath getCurrentPath()
	{
		return this.m_rivalSearch.m_currentPath;
	}
	
	synchronized public boolean isSquareAttacked( BoardModel board, BoardRef boardRef, boolean isWhiteAttacking )
	{
		m_engineBoard.setBoard(board);
		
		int bitRef = ChessBoardConversion.getBitRefFromBoardRef( boardRef );
		
		return m_engineBoard.isSquareAttacked(bitRef, isWhiteAttacking);
	}
	
	@Override
	synchronized public MoveRef getCurrentEngineMove()
	{
		return ChessBoardConversion.getMoveRefFromEngineMove(this.m_rivalSearch.getCurrentMove());
	}

	public int getNodes()
	{
		if (this.m_rivalSearch == null)
		{
			return 0;
		}
		else
		{
			return this.m_rivalSearch.m_nodes;
		}
	}

	public int getNodesPerSecond()
	{
		if (this.m_rivalSearch == null)
		{
			return 0;
		}
		else
		{
			return this.m_rivalSearch.getNodesPerSecond();
		}
	}

	public long getSearchDuration()
	{
		if (this.m_rivalSearch == null)
		{
			return 0;
		}
		else
		{
			return this.m_rivalSearch.getSearchDuration();
		}
	}
	
	public double getSearchPercentComplete()
	{
		return this.m_rivalSearch.getSearchPercentComplete();
	}
	
	public int getCurrentDepthIteration()
	{
		if (this.m_rivalSearch == null)
		{
			return 0;
		}
		else
		{
			return this.m_rivalSearch.getIterativeDeepeningDepth();
		}
	}
	
	public int getEngineState()
	{
		if (this.m_rivalSearch == null)
		{
			return RivalConstants.SEARCHSTATE_NOENGINE;
		}
		else
		{
			return this.m_rivalSearch.getEngineState();  
		}
	}

	public int getCurrentScore()
	{
		if (this.m_rivalSearch == null)
		{
			return 0;
		}
		else
		{
			return this.m_rivalSearch.getCurrentScore();
		}
	}
	
	public void setHashSizeMB(int size)
	{
		this.m_hashSizeMB = size;
	}
	
	synchronized private void getEngineMove( BoardModel board, MoveHistoryContainer moveHistoryContainer, int difficulty )
	{
		m_engineBoard.setBoard(board);
		//Log.i("BOARD [call " + (s_engineMoveCounter++) + "]", "moveHistoryContainer.size( ) = " + moveHistoryContainer.size( ));
		if (moveHistoryContainer != null)
		{
			MoveHistoryItem[] moveArray = moveHistoryContainer.GetArray();
			for (int i=0; i<moveArray.length; i++)
			{
				String algebraicMove = moveArray[i].getAlgebraicMove().replaceAll("-", "");
				int compactMove = ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(algebraicMove);
				//Log.i(algebraicMove, "" + compactMove);
									
				try 
				{
					m_engineBoard.makeMove(compactMove);
				}
				catch( Exception e1 )
				{
					//Log.i("EXcEPTION", " " + algebraicMove + "][" + compactMove + "]\n" +  m_engineBoard + "\n" + e1.toString( ) );
					System.exit( 0 );
				}								    	 
			}
		}
		getEngineMove( difficulty );
	}
	
	
	synchronized public int calcGameStateId( BoardModel board, MoveHistoryContainer moveHistoryContainer )
	{
		m_engineBoard.setBoard(board);
		int gameStateId = AppConstants.GAMESTATE_NOTINCHECK; 
		
		MoveHistoryItem[] moveArray = moveHistoryContainer.GetArray();
		for (int i=0; i<moveArray.length; i++)
		{
			String algebraicMove = moveArray[i].getAlgebraicMove().replaceAll("-", "");
			int compactMove = ChessBoardConversion.getCompactMoveFromSimpleAlgebraic(algebraicMove);
			m_engineBoard.makeMove(compactMove);		
		}		
		
		if (m_engineBoard.previousOccurrencesOfThisPosition() == 2)
		{
			gameStateId = AppConstants.GAMESTATE_THREEFOLD;
		}
		else
		{
			if (this.numLegalMoves(m_engineBoard) == 0)
			{
				if (m_engineBoard.isCheck())
				{
					gameStateId = AppConstants.GAMESTATE_CHECKMATE;
				}
				else
				{
					gameStateId = AppConstants.GAMESTATE_STALEMATE;
				}
			}
			else
			{
				if (m_engineBoard.m_halfMoveCount == 100)
				{
					gameStateId = AppConstants.GAMESTATE_50MOVERULE;
				}
				else
				{
					if (m_engineBoard.isCheck())
					{			
						gameStateId = AppConstants.GAMESTATE_INCHECK;		
					}
					else
					{
						gameStateId = AppConstants.GAMESTATE_NOTINCHECK;
					}
				}
			}
		}				
		return gameStateId;		
	}
	
	synchronized private void getEngineMove( int difficulty )
	{
		m_rivalSearch.setHashSizeMB(m_hashSizeMB);
		this.m_rivalSearch.setBoard(m_engineBoard);
		
		if (this.m_searchType == RivalConstants.SEARCH_TYPE_TIME)
		{
			this.m_rivalSearch.setSearchDepth(RivalConstants.MAX_SEARCH_DEPTH-2);
			this.m_rivalSearch.setMillisToThink(difficulty);
		}
		else
		{
			this.m_rivalSearch.setSearchDepth(difficulty);
			this.m_rivalSearch.setMillisToThink(RivalConstants.MAX_SEARCH_MILLIS);
		}
		m_rivalSearch.go();			
	}

	public boolean cancelThink( )
	{
		// Ignore this for now
		// this.m_rivalEngine.CancelThink( );
		m_rivalSearch.stopSearch( );
		return true;
	}

	synchronized public void setEngineMode(int mode)
	{
		this.m_searchType = mode;
	}
	
	public boolean wasSearchCancelled( )
	{
		return m_rivalSearch.wasSearchCancelled( );
	}	

	@Override
	synchronized public void setEngineDifficulty(int engineDifficulty)
	{
		this.m_engineDifficulty  = engineDifficulty;
	}

	@Override
	public int getGetEngineDifficulty()
	{
		return this.m_engineDifficulty;
	}

	public int getAlwaysHashValuesStored() 
	{
		return this.m_rivalSearch.m_alwaysHashValuesStored;
	}

	public int getAlwaysHashValuesRetrieved() 
	{
		return this.m_rivalSearch.m_alwaysHashValuesRetrieved;
	}

	public int getHeightHashValuesStored() 
	{
		return this.m_rivalSearch.m_heightHashValuesStored;
	}

	public int getHeightHashValuesRetrieved() 
	{
		return this.m_rivalSearch.m_heightHashValuesRetrieved;
	}

	public int getPawnHashValuesStored() 
	{
		return this.m_rivalSearch.m_pawnHashValuesStored;
	}

	public int getPawnHashValuesRetrieved() 
	{
		return this.m_rivalSearch.m_pawnHashValuesRetrieved;
	}

	public int getPawnQuickValuesRetrieved() 
	{
		return this.m_rivalSearch.m_pawnQuickValuesRetrieved;
	}
	
	public int getEvalHashValuesStored() 
	{
		return this.m_rivalSearch.m_evalHashValuesStored;
	}

	public int getEvalHashValuesRetrieved() 
	{
		return this.m_rivalSearch.m_evalHashValuesRetrieved;
	}
	
	public void setDebug(boolean isDebug)
	{
		this.m_rivalSearch.setDebug(isDebug);
	}
}
