package com.netadapt.rivalchess.engine;

public class EngineMove implements Comparable<EngineMove>
{
	public int from;
	public int to;
	public int score;

	public EngineMove(int from, int to, int score)
	{
		this.from = from;
		this.to = to;
		this.score = score;
	}
	
	public int compareTo(EngineMove o)
	{
		return this.score - o.score; 
		//return o.score - this.score; // -1 if this.score is higher
		//return this.score > o.score ? -1 : (this.score == o.score ? 0 : 1); 
	}
}
