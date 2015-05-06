package com.netadapt.rivalchess.engine;
import java.util.EventObject;

import com.netadapt.rivalchess.engine.core.SearchPath;
import com.netadapt.rivalchess.model.board.BoardModel;
import com.netadapt.rivalchess.model.MoveHistoryContainer;
import com.netadapt.rivalchess.model.board.BoardRef;
import com.netadapt.rivalchess.model.board.MoveRef;

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