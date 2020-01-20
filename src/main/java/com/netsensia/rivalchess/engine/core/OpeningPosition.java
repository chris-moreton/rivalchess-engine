package com.netsensia.rivalchess.engine.core;

public class OpeningPosition 
{
	final String fen;
	final String move;
	final int frequency;
	
	public OpeningPosition(String fen, String move, int frequency)
	{
		this.fen = fen;
		this.move = move;
		this.frequency = frequency;
	}
}
