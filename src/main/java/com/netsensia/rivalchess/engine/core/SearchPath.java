package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.util.ChessBoardConversion;

public class SearchPath 
{
	public final int[] move;
	public int score;
	public int height = 0;

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

		if (path.height >= 0) System.arraycopy(path.move, 0, this.move, 0, path.height);
	}

	public synchronized void setPath(int compactMove, SearchPath path)
	{
		this.height = path.height + 1;
		this.move[0] = compactMove;
		this.score = path.score;

		if (path.height >= 0) System.arraycopy(path.move, 0, this.move, 1, path.height);
	}
	
	public synchronized int getScore()
	{
		return this.score;
	}
	
	public synchronized String toString()
	{
		String retString = "";
		
		try
		{
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
