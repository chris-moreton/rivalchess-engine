package com.netadapt.rivalchess.engine;
import java.util.EventObject;

import com.netadapt.rivalchess.FenChess;
import com.netadapt.rivalchess.model.BoardModel;
import com.netadapt.rivalchess.model.BoardRef;
import com.netadapt.rivalchess.model.MoveRef;

public interface EngineServiceInterface
{
	public void startEngine( BoardModel board );
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
	
	public boolean handleCancelThink( EventObject e);
		
}
