package com.netadapt.rivalchess.engine;

import java.util.EventObject;

import java.util.Vector;
import java.util.Iterator;

import com.netadapt.rivalchess.AppConstants;
import com.netadapt.rivalchess.model.BoardModel;
import com.netadapt.rivalchess.model.BoardRef;
import com.netadapt.rivalchess.model.MoveRef;
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
	
	public EngineStub(  )
	{				
		this.m_bitboards = new Bitboards();
		this.m_rivalSearch = new RivalSearch();
		this.m_engineBoard = new EngineChessBoard(m_bitboards);
	}

	public EngineStub( Bitboards bitboards )
	{
		this.m_bitboards = bitboards;
		this.m_rivalSearch = new RivalSearch();
		this.m_engineBoard = new EngineChessBoard(m_bitboards);
	}
	
	public int numLegalMoves( BoardModel board )
	{
		int numMoves = 0;
		int i = 0;
		
		m_engineBoard.setBoard(board);
		
		int legalMoves[] = new int[RivalConstants.MAX_LEGAL_MOVES];
		int legalMoves2[] = new int[RivalConstants.MAX_LEGAL_MOVES];
		m_engineBoard.setLegalMoves(legalMoves);
		while (legalMoves[i] != 0)
		{
			m_engineBoard.makeMove(legalMoves[i]);
			if (!m_engineBoard.isNonMoverInCheck(legalMoves2))
			{
				numMoves ++;
			}
			i++;
			m_engineBoard.unMakeMove();
		}
		return numMoves;
	}
	
	public boolean isMoveAvailable(BoardModel boardModel)
	{
		return this.numLegalMoves( boardModel ) > 0;
	}
	
	@Override
	public BoardRef[] getLegalMoves(BoardModel board, BoardRef boardRef)
	{
		m_engineBoard.setBoard(board);
		
		int legalMoves[] = new int[RivalConstants.MAX_LEGAL_MOVES];
		int legalMoves2[] = new int[RivalConstants.MAX_LEGAL_MOVES];
		
		m_engineBoard.setLegalMoves(legalMoves);
		
		Vector<BoardRef> boardRefVector = new Vector<BoardRef>();
		
		int moveNum = 0;
		BoardRef boardRefFrom, boardRefTo;
		while (legalMoves[moveNum] != 0)
		{
			m_engineBoard.makeMove(legalMoves[moveNum]);
			if (!m_engineBoard.isNonMoverInCheck(legalMoves2))
			{
				boardRefFrom = ChessBoardConversion.getBoardRefFromBitRef(legalMoves[moveNum] >>> 16);
				boardRefTo = ChessBoardConversion.getBoardRefFromBitRef(legalMoves[moveNum] & 63); 
				int promotionPieceCode = legalMoves[moveNum] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
				switch (promotionPieceCode)
				{
					case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN : 
						break;
					case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK : 
						break;
					case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT : 
						break;
					case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP : 
						break;
				}
				if (boardRefFrom.equals(boardRef))
				{
					boardRefVector.add(boardRefTo);
				}
			}
			m_engineBoard.unMakeMove();
			moveNum ++;
		}
		
		BoardRef boardRefArray[] = new BoardRef[boardRefVector.size()];
		Iterator<BoardRef> i = boardRefVector.iterator();
		moveNum = 0;
		while (i.hasNext())
		{
			boardRefArray[moveNum] = i.next();
			moveNum ++;
		}
		return boardRefArray;
	}
	
	public void clearHash()
	{
		m_rivalSearch.clearHash();
	}
	@Override
	public boolean isSearchComplete()
	{
		return this.m_rivalSearch.isSearchComplete();
	}	
	
	@Override
	public void startEngine( BoardModel board )
	{
		this.getEngineMove( board, this.m_engineDifficulty );
	}
	
	public SearchPath getCurrentPath()
	{
		return this.m_rivalSearch.m_currentPath;
	}
	
	public boolean isSquareAttacked( BoardModel board, BoardRef boardRef, boolean isWhiteAttacking )
	{
		m_engineBoard.setBoard(board);
		
		int bitRef = ChessBoardConversion.getBitRefFromBoardRef( boardRef );
		
		return m_engineBoard.isSquareAttacked(bitRef, isWhiteAttacking);
	}
	
	@Override
	public MoveRef getCurrentEngineMove()
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
	
	private void getEngineMove( BoardModel board, int difficulty )
	{
		m_engineBoard.setBoard(board);
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

	@Override
	public boolean handleCancelThink(EventObject e)
	{
		// Ignore this for now
		// this.m_rivalEngine.CancelThink( );
		return false;
	}

	public void setEngineMode(int mode)
	{
		this.m_searchType = mode;
	}

	@Override
	public void setEngineDifficulty(int engineDifficulty)
	{
		this.m_engineDifficulty  = engineDifficulty;
	}

	@Override
	public int getGetEngineDifficulty()
	{
		return this.m_engineDifficulty;
	}

	public void setCheckState(BoardModel boardModel)
	{
		BoardRef kingBoardRef = boardModel.getKingBoardRef( boardModel.isWhiteToMove( ) );						
		boolean isKingAttacked = this.isSquareAttacked(  boardModel, kingBoardRef, ! boardModel.isWhiteToMove( ) );
		
		//boardModel.setPath(this.getCurrentPath());
				
		if ( ! this.isMoveAvailable( boardModel ))
		{		
			boardModel.setCheckState( isKingAttacked?AppConstants.CHECK_STATE_CHECKMATE:AppConstants.CHECK_STATE_STALEMATE );			
		}
		else
		{
			boardModel.setCheckState( isKingAttacked?AppConstants.CHECK_STATE_IN_CHECK:AppConstants.CHECK_STATE_OUT_CHECK );			
		}		
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
