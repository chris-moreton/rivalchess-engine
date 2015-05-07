package com.netsensia.rivalchess.engine;
import java.util.EventObject;

import com.netsensia.rivalchess.engine.core.SearchPath;
import com.netsensia.rivalchess.model.board.BoardModel;
import com.netsensia.rivalchess.model.MoveHistoryContainer;
import com.netsensia.rivalchess.model.board.BoardRef;
import com.netsensia.rivalchess.model.board.MoveRef;

public interface EngineServiceInterface
{
	public void startEngine( BoardModel board, MoveHistoryContainer moveHistory );
	public MoveRef getCurrentEngineMove();
	public boolean isSearchComplete();
	public BoardRef[] getLegalMoves( BoardModel board, BoardRef boardRef );
			
	public void setEngineDifficulty(int engineDifficulty );
	public int getGetEngineDifficulty( );
	
	public int getCurrentDepthIteration();
	public int getCurrentScore();
	public int getNodesPerSecond();
	public int getNodes();
	public long getSearchDuration();
	public SearchPath getCurrentPath();	
}