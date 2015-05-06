package com.netadapt.rivalchess.util;

import java.util.Random;

public class Numbers 
{
	public static long getRandomUnsignedLong()
	{
		Random r = new Random();
		return getRandomUnsignedLong(r);
	}

	public static long getRandomUnsignedLong(Random r)
	{
		// use this is you need to use a specifically seeded Random
		long l;
		do
		{	
			l = r.nextLong();
		}
		while (l < 0);
		
		return l;
	}
	
	public static int linearScale(final int situation, final int ref1, final int ref2, final int score1, final int score2)
	{
		if (situation < ref1) return score1;
		if (situation > ref2) return score2;
		return (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1;
	}
}
