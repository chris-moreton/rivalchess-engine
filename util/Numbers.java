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
}
