package com.netsensia.rivalchess.util;

public class BitwiseOperation 
{
	public static long rotateLeft(long x, int s)
	{
		return (x << s) | (x >>> (64-s));		
	}

	public static long rotateRight(long x, int s)
	{
		return (x >>> s) | (x << (64-s));		
	}
	
	public static int countSetBits(long l)
	{
		int count = 0;

		while (l != 0) 
		{
		    if ((l & 0x1) == 0x1) count++;
		    l >>>= 1;
		}
		
		return count;
	}

	public static long rotateBitboard90AntiClockwise(long b)
	{
		long t; // temporary
		
		// reflect b against diagonal line going through bits 1<<7 and 1<<56
		t = (b ^ (b >> 63)) & 0x0000000000000001L; b ^= t ^ (t << 63);
		t = (b ^ (b >> 54)) & 0x0000000000000102L; b ^= t ^ (t << 54);
		t = (b ^ (b >> 45)) & 0x0000000000010204L; b ^= t ^ (t << 45);
		t = (b ^ (b >> 36)) & 0x0000000001020408L; b ^= t ^ (t << 36);
		t = (b ^ (b >> 27)) & 0x0000000102040810L; b ^= t ^ (t << 27);
		t = (b ^ (b >> 18)) & 0x0000010204081020L; b ^= t ^ (t << 18);
		t = (b ^ (b >>  9)) & 0x0001020408102040L; b ^= t ^ (t <<  9);
		
		// reflect b against vertical centre line
		t = (b ^ (b >>  7)) & 0x0101010101010101L; b ^= t ^ (t <<  7);
		t = (b ^ (b >>  5)) & 0x0202020202020202L; b ^= t ^ (t <<  5);
		t = (b ^ (b >>  3)) & 0x0404040404040404L; b ^= t ^ (t <<  3);
		t = (b ^ (b >>  1)) & 0x0808080808080808L; b ^= t ^ (t <<  1);
		
		return b;
	}
	
	public static long rotateBitboard90Clockwise(long b)
	{
		for (int i=0; i<3; i++) b = BitwiseOperation.rotateBitboard90AntiClockwise(b);
		return b;
	}
	
	public static long reverseRotateBitboard45AntiClockwiseFast(long x)
	{
		x ^= 0xAAAAAAAAAAAAAAAAL & (x ^ ((x << 8) | (x >>> 56)));
		x ^= 0xCCCCCCCCCCCCCCCCL & (x ^ ((x << 16) | (x >>> 48)));
		x ^= 0xF0F0F0F0F0F0F0F0L & (x ^ ((x << 32) | (x >>> 32)));
		return x;
	}

	public static long rotateBitboard45AntiClockwiseFast(long x)
	{
		x ^= 0xAAAAAAAAAAAAAAAAL & (x ^ ((x >>> 8) | (x << 56)));
		x ^= 0xCCCCCCCCCCCCCCCCL & (x ^ ((x >>> 16) | (x << 48)));
		x ^= 0xF0F0F0F0F0F0F0F0L & (x ^ ((x >>> 32) | (x << 32)));
		return x;
	}
	
	public static long reverseRotateBitboard45ClockwiseFast(long x)
	{
		x ^= 0x5555555555555555L & (x ^ ((x << 8) | (x >>> 56)));
		x ^= 0x3333333333333333L & (x ^ ((x << 16) | (x >>> 48)));
		x ^= 0x0f0f0f0f0f0f0f0fL & (x ^ ((x << 32) | (x >>> 32)));
		return x;
	}

	public static long rotateBitboard45ClockwiseFast(long x)
	{
		x ^= 0x5555555555555555L & (x ^ ((x >>> 8) | (x << 56)));
		x ^= 0x3333333333333333L & (x ^ ((x >>> 16) | (x << 48)));
		x ^= 0x0f0f0f0f0f0f0f0fL & (x ^ ((x >>> 32) | (x << 32)));
		return x;
	}
	
}
