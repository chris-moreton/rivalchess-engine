package com.netsensia.rivalchess.engine.test.epd;

import java.util.ArrayList;


public class EPDPosition 
{
	public String fen;
	public String id;
	public String epd;
	public ArrayList<String> bestMoves;
	public ArrayList<EPDMove> plyMoves;
	public String moveGiven;
	public int maxMillis;
	public int extraPlies;
	public long startTime, endTime;
	
	public EPDPosition(String epd, String fen, String[] bestMoves, String id, int maxMillis, int extraPlies)
	{
		this.maxMillis = maxMillis;
		this.extraPlies = extraPlies;
		this.fen = fen;
		this.id = id;
		this.epd = epd;
		this.bestMoves = new ArrayList<String>();
		this.plyMoves = new ArrayList<EPDMove>();
		for (String bm : bestMoves) this.bestMoves.add(bm.trim());
	}
	
	public void setStartTime(long millis)
	{
		this.startTime = millis;
	}

	public void setEndTime(long millis)
	{
		this.endTime = millis;
	}
	
	public long getTimeTaken()
	{
		return this.endTime - this.startTime;
	}
	
	public String getLastMove()
	{
		EPDMove epdMove = plyMoves.get(plyMoves.size()-1);
		return epdMove.pgnMove;
	}
	
	public String bestMoves()
	{
		String s = "";
		for (String validMove : bestMoves)
		{
			s += " " + validMove;
		}
		return s.trim();
	}
	
	public boolean canTerminate()
	{
		int inARow = 0;
		for (EPDMove plyMove : plyMoves)
		{
			boolean moveIsValid = false;
			for (String validMove : bestMoves)
			{
				if (validMove.equals(plyMove.pgnMove))
				{
					moveIsValid = true;
					break;
				}
			}
			if (moveIsValid)
			{
				inARow ++;
				if (inARow == this.extraPlies)
				{
					return true;
				}
			}
			else
			{
				inARow = 0;
			}
		}
		return false;
	}
	
	public long totalTimeUsed()
	{
		return this.endTime - this.startTime;
	}
	
	public int millisWhenFirstCorrect()
	{
		for (EPDMove plyMove : plyMoves)
		{
			for (String validMove : bestMoves)
			{
				if (validMove.equals(plyMove.pgnMove))
				{
					return plyMove.millis;
				}
			}
		}
		return -1;
	}
	
	public void setPlyMove(int ply, String pgnMove)
	{
		this.plyMoves.add(ply, new EPDMove(pgnMove.trim(), (int)(System.currentTimeMillis() - this.startTime)));
	}
}
