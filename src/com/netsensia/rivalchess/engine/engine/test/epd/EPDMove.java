package com.netadapt.rivalchess.engine.test.epd;

public class EPDMove 
{
	public String pgnMove;
	public int millis;
	
	public EPDMove(String pgnMove, int millis)
	{
		this.pgnMove = pgnMove;
		this.millis = millis;
	}
}
