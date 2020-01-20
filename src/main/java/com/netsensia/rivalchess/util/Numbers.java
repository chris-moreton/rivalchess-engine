package com.netsensia.rivalchess.util;

public class Numbers 
{

	public static int linearScale(final int situation, final int ref1, final int ref2, final int score1, final int score2)
	{
		if (situation < ref1) return score1;
		if (situation > ref2) return score2;
		return (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1;
	}
}
