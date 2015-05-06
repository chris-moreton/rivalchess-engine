package com.netadapt.rivalchess.engine.core;

public class OpeningPosition 
{
	String fen;
	String move;
	int frequency;
	
	public OpeningPosition(String fen, String move, int frequency)
	{
		this.fen = fen;
		this.move = move;
		this.frequency = frequency;
	}
}
