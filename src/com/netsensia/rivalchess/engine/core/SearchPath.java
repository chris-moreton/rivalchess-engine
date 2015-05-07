package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.util.ChessBoardConversion;

public class SearchPath 
{
	public int[] move;
	public int score;
	public int height = 0;
	public int hashCorrectedHeight = 0;
	
	public SearchPath()
	{
		move = new int[RivalConstants.MAX_TREE_DEPTH];
	}

	public synchronized void reset()
	{
		this.height = 0;
		this.score = -RivalConstants.INFINITY;
	}
	
	public synchronized void setPath(int move)
	{
		this.height = 1;
		this.move[0] = move;
	}
	
	public synchronized void setPath(SearchPath path)
	{
		this.score = path.score;
		this.height = path.height;

		//System.arraycopy(path.move, 0, this.move, 0, path.height);
		for (int i=0; i<path.height; i++) this.move[i] = path.move[i];
	}

	public synchronized void setPath(int compactMove, SearchPath path)
	{
		this.height = path.height + 1;
		this.move[0] = compactMove;
		this.score = path.score;
		
		//System.arraycopy(path.move, 0, this.move, 1, path.height);
		for (int i=1; i<=path.height; i++) this.move[i] = path.move[i-1];
	}
	
	public synchronized int getScore()
	{
//		if (this.score > RivalConstants.MATE_SCORE_START) return RivalConstants.VALUE_MATE - this.hashCorrectedHeight; 
//		if (this.score < -RivalConstants.MATE_SCORE_START) return RivalConstants.VALUE_MATE + this.hashCorrectedHeight;
		return this.score;
	}
	
	public synchronized String toString()
	{
		String retString = "";
		
		try
		{
			if (this.move != null)
				for (int i=0; i<this.height; i++)
					if (this.move[i] != 0)
						retString += ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(this.move[i]) + " ";
					else
						break;
		} 
		catch (NullPointerException e)
		{
			throw new RuntimeException();
		}
		
		return retString;
	}
}
