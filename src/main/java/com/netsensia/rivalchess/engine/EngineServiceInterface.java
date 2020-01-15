package com.netsensia.rivalchess.engine;

import com.netsensia.rivalchess.engine.core.SearchPath;
import com.netsensia.rivalchess.model.Board;
import com.netsensia.rivalchess.model.MoveHistory;
import com.netsensia.rivalchess.model.Square;
import com.netsensia.rivalchess.model.Move;

public interface EngineServiceInterface
{
	public void startEngine(Board board, MoveHistory moveHistory );
	public Move getCurrentEngineMove();
	public boolean isSearchComplete();
	public Square[] getLegalMoves(Board board, Square boardRef );
			
	public void setEngineDifficulty(int engineDifficulty );
	public int getGetEngineDifficulty( );
	
	public int getCurrentDepthIteration();
	public int getCurrentScore();
	public int getNodesPerSecond();
	public int getNodes();
	public long getSearchDuration();
	public SearchPath getCurrentPath();	
}