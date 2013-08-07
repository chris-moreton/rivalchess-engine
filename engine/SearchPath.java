package com.netadapt.rivalchess.engine;

import com.netadapt.rivalchess.util.ChessBoardConversion;

public class SearchPath 
{
	public int[] move;
	public int score;
	public int height = 0;

	public void reset()
	{
		this.height = 0;
		this.move = null;
		this.score = -RivalConstants.INFINITY;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}

	public void setPath(int move)
	{
		this.move = new int[1];
		this.height = 1;
		this.move[0] = move;
	}
	
	public void setPath(SearchPath path)
	{
		int pathLength;
		if (path.move != null)
		{
			pathLength = path.move.length;
			this.move = new int[pathLength];
			for (int i=0; i<pathLength; i++)
			{
	 			this.move[i] = path.move[i];
			}
		}
		
		this.score = path.score;
		this.height = path.height;
	}

	public void setPath(int compactMove, SearchPath path)
	{
		this.height = path.height + 1;
		
		int pathLength;
		if (path.move == null)
		{
			pathLength = 1;
			this.move = new int[pathLength];
		}
		else
		{
			pathLength = path.move.length + 1;
			this.move = new int[pathLength];
		}

		this.move[0] = compactMove;
		
		this.score = path.score;
		
		for (int i=1; i<pathLength; i++)
		{
 			this.move[i] = path.move[i-1];
		}
	}
	
	public String toString()
	{
		String retString = "";
		
		try
		{
			if (this.move != null)
			{
				for (int i=0; i<this.move.length; i++)
				{
					retString += ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(this.move[i]) + " ";
				}
			}
		} 
		catch (NullPointerException e)
		{
		}
		
		return retString;
	}
}
