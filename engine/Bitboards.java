package com.netadapt.rivalchess.engine;

import com.netadapt.rivalchess.util.BitwiseOperation;
import com.netadapt.rivalchess.util.ChessBoardConversion;
import com.netadapt.rivalchess.model.BoardRef;

public final class Bitboards 
{

	public static int setBits[] = new int[64];

	public static final long KING_9 = // G2 
			1L << 18 | 1L << 17 | 1L << 16 |
			1L << 10 |            1L << 8 |
			1L << 2  | 1L << 1  | 1L;

	public static final long KNIGHT_18 = // F3 
		1L << 35 | 1L << 33 | 1L << 28 | 1L << 24 | 1L << 12 | 1L << 8 | 1L << 3 | 1L << 1;
	
	public static final long FILE_A = 1L << 7 | 1L << 15 | 1L << 23 | 1L << 31 | 1L << 39 | 1L << 47 | 1L << 55 | 1L << 63;
	public static final long FILE_B = 1L << 6 | 1L << 14 | 1L << 22 | 1L << 30 | 1L << 38 | 1L << 46 | 1L << 54 | 1L << 62;
	public static final long FILE_C = 1L << 5 | 1L << 13 | 1L << 21 | 1L << 29 | 1L << 37 | 1L << 45 | 1L << 53 | 1L << 61;
	public static final long FILE_D = 1L << 4 | 1L << 12 | 1L << 20 | 1L << 28 | 1L << 36 | 1L << 44 | 1L << 52 | 1L << 60;
	public static final long FILE_E = 1L << 3 | 1L << 11 | 1L << 19 | 1L << 27 | 1L << 35 | 1L << 43 | 1L << 51 | 1L << 59;
	public static final long FILE_F = 1L << 2 | 1L << 10 | 1L << 18 | 1L << 26 | 1L << 34 | 1L << 42 | 1L << 50 | 1L << 58;
	public static final long FILE_G = 1L << 1 | 1L << 9 | 1L << 17 | 1L << 25 | 1L << 33 | 1L << 41 | 1L << 49 | 1L << 57;
	public static final long FILE_H = 1L | 1L << 8 | 1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56;
	
	public static final long WHITE_SQUARES =
		1L << 63 | 1L << 61 | 1L << 59 | 1L << 57 |
		1L << 54 | 1L << 52 | 1L << 50 | 1L << 48 |
		1L << 47 | 1L << 45 | 1L << 43 | 1L << 41 |
		1L << 38 | 1L << 36 | 1L << 34 | 1L << 32 |
		1L << 31 | 1L << 29 | 1L << 27 | 1L << 25 |
		1L << 22 | 1L << 20 | 1L << 18 | 1L << 16 |
		1L << 15 | 1L << 13 | 1L << 11 | 1L << 9 |
		1L << 6 | 1L << 4 | 1L << 2 | 1;
	
	public static final long WHITE_QUADRANT =
		1L << 63 | 1L << 62 | 1L << 61 | 1L << 60 |
		1L << 55 | 1L << 54 | 1L << 53 | 1L << 52 |
		1L << 47 | 1L << 46 | 1L << 45 | 1L << 44 |
		1L << 39 | 1L << 38 | 1L << 37 | 1L << 36 |
		1L << 27 | 1L << 26 | 1L << 25 | 1L << 24 |
		1L << 19 | 1L << 18 | 1L << 17 | 1L << 16 |
		1L << 11 | 1L << 10 | 1L << 9 | 1L << 8 |
		1L << 3 | 1L << 2 | 1L << 1 | 1;
	
	public static final long QUEENSIDE = FILE_A | FILE_B | FILE_C | FILE_D;
	public static final long KINGSIDE = FILE_E | FILE_F | FILE_G | FILE_H;
	
	public static final long HIGH32 = 0xFFFFFFFF00000000L;
	public static final long LOW32 = 0x00000000FFFFFFFFL;
	
	public static final long RANK_8 = 0xFF00000000000000L;
	public static final long RANK_7 = 0x00FF000000000000L;
	public static final long RANK_6 = 0x0000FF0000000000L;
	public static final long RANK_5 = 0x000000FF00000000L;
	public static final long RANK_4 = 0x00000000FF000000L;
	public static final long RANK_3 = 0x0000000000FF0000L;
	public static final long RANK_2 = 0x000000000000FF00L;
	public static final long RANK_1 = 0x00000000000000FFL;

	public static final long KING_TUCK = 0xE3000000000000E3L;
	
	public static final long WHITEKINGSIDECASTLESQUARES = 1L << 1 | 1L << 2;  
	public static final long WHITEQUEENSIDECASTLESQUARES = 1L << 4 | 1L << 5 | 1L << 6;  
	public static final long BLACKKINGSIDECASTLESQUARES = 1L << 57 | 1L << 58;  
	public static final long BLACKQUEENSIDECASTLESQUARES = 1L << 62 | 1L << 61 | 1L << 60;

	public static final long WHITEKINGSIDECASTLEMOVEMASK = 1L << 1 | 1L << 3;
	public static final long WHITEQUEENSIDECASTLEMOVEMASK = 1L << 3 | 1L << 5;
	public static final long BLACKKINGSIDECASTLEMOVEMASK = 1L << 57 | 1L << 59;
	public static final long BLACKQUEENSIDECASTLEMOVEMASK = 1L << 59 | 1L << 61;
	
	public static final long WHITEKINGSIDECASTLEROOKMOVE = 1L | 1L << 2;  
	public static final long WHITEQUEENSIDECASTLEROOKMOVE = 1L << 4 | 1L << 7;  
	public static final long BLACKKINGSIDECASTLEROOKMOVE = 1L << 56 | 1L << 58;  
	public static final long BLACKQUEENSIDECASTLEROOKMOVE = 1L << 60 | 1L << 63;
	
	public static final long WHITEBISHOPSTARTSQUARES = 1L << 5 | 1L << 2;
	public static final long BLACKBISHOPSTARTSQUARES = 1L << 61 | 1L << 58;
	public static final long WHITEKNIGHTSTARTSQUARES = 1L << 6 | 1L << 1;
	public static final long BLACKKNIGHTSTARTSQUARES = 1L << 62 | 1L << 57; 
	public static final long WHITEQUEENSTARTSQUARE = 1L << 4;
	public static final long BLACKQUEENSTARTSQUARE = 1L << 60;
	public static final long WHITEROOKSTARTSQUARES = 1L << 7 | 1L << 0;
	public static final long BLACKROOKSTARTSQUARES = 1L << 63 | 1L << 56;
	public static final long WHITEKINGSTARTSQUARE = 1L << 3;
	public static final long BLACKKINGSTARTSQUARE = 1L << 59;
	public static final long WHITEPAWNSTARTSQUARES = 1L << 13 | 1L << 12 | 1L << 11 | 1L << 10; // ignore side pawns
	public static final long BLACKPAWNSTARTSQUARES = 1L << 61 | 1L << 60 | 1L << 59 | 1L << 58;

	public static final long WHITEKINGSIDEROOKMASK = 1L;  
	public static final long WHITEQUEENSIDEROOKMASK = 1L << 7;  
	public static final long BLACKKINGSIDEROOKMASK = 1L << 56;  
	public static final long BLACKQUEENSIDEROOKMASK = 1L << 63;

	public static final long WHITE_KINGSAFETY_KINGSIDE_MASK = 1L << 0 | 1L << 1 | 1L << 2 | 1L << 3;
	public static final long WHITE_KINGSAFETY_QUEENSIDE_MASK = 1L << 5 | 1L << 6 | 1L << 7 | 1L << 3 | 1L << 4;
	public static final long WHITE_KINGSAFETY_KINGSIDE_1 = 1L << 8 | 1L << 9 | 1L << 10;
	public static final long WHITE_KINGSAFETY_KINGSIDE_2 = 1L << 16 | 1L << 17 | 1L << 18;
	public static final long WHITE_KINGSAFETY_QUEENSIDE_1 = 1L << 13 | 1L << 14 | 1L << 15;
	public static final long WHITE_KINGSAFETY_QUEENSIDE_2 = 1L << 21 | 1L << 22 | 1L << 23;
	public static final long BLACK_KINGSAFETY_KINGSIDE_MASK = 1L << 58 | 1L << 57 | 1L << 56 | 1L << 59;
	public static final long BLACK_KINGSAFETY_QUEENSIDE_MASK = 1L << 61 | 1L << 62 | 1L << 63 | 1L << 59 | 1L << 60;
	public static final long BLACK_KINGSAFETY_KINGSIDE_1 = 1L << 48 | 1L << 49 | 1L << 50;
	public static final long BLACK_KINGSAFETY_KINGSIDE_2 = 1L << 40 | 1L << 41 | 1L << 42;
	public static final long BLACK_KINGSAFETY_QUEENSIDE_1 = 1L << 53 | 1L << 54 | 1L << 55;
	public static final long BLACK_KINGSAFETY_QUEENSIDE_2 = 1L << 45 | 1L << 46 | 1L << 47;
	
	public static final long MIDDLE_OCCUPANCY =
		1L << 45 | 1L << 44 | 1L << 43 | 1L << 42 | 
		1L << 37 | 1L << 36 | 1L << 35 | 1L << 34 | 
		1L << 29 | 1L << 28 | 1L << 27 | 1L << 26 | 
		1L << 21 | 1L << 20 | 1L << 19 | 1L << 18;

	public static final long TIGHT_MIDDLE_OCCUPANCY =
		1L << 36 | 1L << 35 | 
		1L << 28 | 1L << 27; 
	
	public final int[] byteLengthMask = {0,1,3,7,15,31,63,127,255};
	
	//public long[] whitePawnMovesForward, whitePawnMovesCapture;
	//public long[] blackPawnMovesForward, blackPawnMovesCapture;
	
	public static final long[] knightMoves = {
        0x20400L, 
        0x50800L,            0xa1100L,           0x142200L,           0x284400L,           0x508800L,           0xa01000L,           0x402000L,          0x2040004L, 
      0x5080008L,          0xa110011L,         0x14220022L,         0x28440044L,         0x50880088L,         0xa0100010L,         0x40200020L,        0x204000402L, 
    0x508000805L,        0xa1100110aL,       0x1422002214L,       0x2844004428L,       0x5088008850L,       0xa0100010a0L,       0x4020002040L,      0x20400040200L, 
  0x50800080500L,      0xa1100110a00L,     0x142200221400L,     0x284400442800L,     0x508800885000L,     0xa0100010a000L,     0x402000204000L,    0x2040004020000L, 
0x5080008050000L,    0xa1100110a0000L,   0x14220022140000L,   0x28440044280000L,   0x50880088500000L,   0xa0100010a00000L,   0x40200020400000L,  0x204000402000000L, 
0x508000805000000L,  0xa1100110a000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L,  0x400040200000000L, 
0x800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L,    0x4020000000000L, 
0x8050000000000L,   0x110a0000000000L,   0x22140000000000L,   0x44280000000000L,   0x88500000000000L,   0x10a00000000000L,   0x20400000000000L, };	
	
	public static final long[] kingMoves = {
        0x302L, 
        0x705L,              0xe0aL,             0x1c14L,             0x3828L,             0x7050L,             0xe0a0L,             0xc040L,            0x30203L, 
      0x70507L,            0xe0a0eL,           0x1c141cL,           0x382838L,           0x705070L,           0xe0a0e0L,           0xc040c0L,          0x3020300L, 
    0x7050700L,          0xe0a0e00L,         0x1c141c00L,         0x38283800L,         0x70507000L,         0xe0a0e000L,         0xc040c000L,        0x302030000L, 
  0x705070000L,        0xe0a0e0000L,       0x1c141c0000L,       0x3828380000L,       0x7050700000L,       0xe0a0e00000L,       0xc040c00000L,      0x30203000000L, 
0x70507000000L,      0xe0a0e000000L,     0x1c141c000000L,     0x382838000000L,     0x705070000000L,     0xe0a0e0000000L,     0xc040c0000000L,    0x3020300000000L, 
0x7050700000000L,    0xe0a0e00000000L,   0x1c141c00000000L,   0x38283800000000L,   0x70507000000000L,   0xe0a0e000000000L,   0xc040c000000000L,  0x302030000000000L, 
0x705070000000000L,  0xe0a0e0000000000L, 0x1c141c0000000000L, 0x3828380000000000L, 0x7050700000000000L, 0xe0a0e00000000000L, 0xc040c00000000000L,  0x203000000000000L, 
0x507000000000000L,  0xa0e000000000000L, 0x141c000000000000L, 0x2838000000000000L, 0x5070000000000000L, 0xa0e0000000000000L, 0x40c0000000000000L, };
	
	public static final long[] whitePawnMovesForward = {
		        0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,            0x10000L, 
		    0x20000L,            0x40000L,            0x80000L,           0x100000L,           0x200000L,           0x400000L,           0x800000L,          0x1000000L, 
		  0x2000000L,          0x4000000L,          0x8000000L,         0x10000000L,         0x20000000L,         0x40000000L,         0x80000000L,        0x100000000L, 
		0x200000000L,        0x400000000L,        0x800000000L,       0x1000000000L,       0x2000000000L,       0x4000000000L,       0x8000000000L,      0x10000000000L, 
		0x20000000000L,      0x40000000000L,      0x80000000000L,     0x100000000000L,     0x200000000000L,     0x400000000000L,     0x800000000000L,    0x1000000000000L, 
		0x2000000000000L,    0x4000000000000L,    0x8000000000000L,   0x10000000000000L,   0x20000000000000L,   0x40000000000000L,   0x80000000000000L,  0x100000000000000L, 
		0x200000000000000L,  0x400000000000000L,  0x800000000000000L, 0x1000000000000000L, 0x2000000000000000L, 0x4000000000000000L, 0x8000000000000000L,                0x0L, 
        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L, };
	
	public static final long[] whitePawnMovesCapture = {
		        0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,            0x20000L, 
		    0x50000L,            0xa0000L,           0x140000L,           0x280000L,           0x500000L,           0xa00000L,           0x400000L,          0x2000000L, 
		  0x5000000L,          0xa000000L,         0x14000000L,         0x28000000L,         0x50000000L,         0xa0000000L,         0x40000000L,        0x200000000L, 
		0x500000000L,        0xa00000000L,       0x1400000000L,       0x2800000000L,       0x5000000000L,       0xa000000000L,       0x4000000000L,      0x20000000000L, 
		0x50000000000L,      0xa0000000000L,     0x140000000000L,     0x280000000000L,     0x500000000000L,     0xa00000000000L,     0x400000000000L,    0x2000000000000L, 
		0x5000000000000L,    0xa000000000000L,   0x14000000000000L,   0x28000000000000L,   0x50000000000000L,   0xa0000000000000L,   0x40000000000000L,  0x200000000000000L, 
		0x500000000000000L,  0xa00000000000000L, 0x1400000000000000L, 0x2800000000000000L, 0x5000000000000000L, 0xa000000000000000L, 0x4000000000000000L,                0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L, };

	public static final long[] blackPawnMovesForward = {
		        0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x1L, 
		        0x2L,                0x4L,                0x8L,               0x10L,               0x20L,               0x40L,               0x80L,              0x100L, 
		      0x200L,              0x400L,              0x800L,             0x1000L,             0x2000L,             0x4000L,             0x8000L,            0x10000L, 
		    0x20000L,            0x40000L,            0x80000L,           0x100000L,           0x200000L,           0x400000L,           0x800000L,          0x1000000L, 
		  0x2000000L,          0x4000000L,          0x8000000L,         0x10000000L,         0x20000000L,         0x40000000L,         0x80000000L,        0x100000000L, 
		0x200000000L,        0x400000000L,        0x800000000L,       0x1000000000L,       0x2000000000L,       0x4000000000L,       0x8000000000L,      0x10000000000L, 
		0x20000000000L,      0x40000000000L,      0x80000000000L,     0x100000000000L,     0x200000000000L,     0x400000000000L,     0x800000000000L,                0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L, };

	public static final long[] blackPawnMovesCapture = {
		        0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x2L, 
		        0x5L,                0xaL,               0x14L,               0x28L,               0x50L,               0xa0L,               0x40L,              0x200L, 
		      0x500L,              0xa00L,             0x1400L,             0x2800L,             0x5000L,             0xa000L,             0x4000L,            0x20000L, 
		    0x50000L,            0xa0000L,           0x140000L,           0x280000L,           0x500000L,           0xa00000L,           0x400000L,          0x2000000L, 
		  0x5000000L,          0xa000000L,         0x14000000L,         0x28000000L,         0x50000000L,         0xa0000000L,         0x40000000L,        0x200000000L, 
		0x500000000L,        0xa00000000L,       0x1400000000L,       0x2800000000L,       0x5000000000L,       0xa000000000L,       0x4000000000L,      0x20000000000L, 
		0x50000000000L,      0xa0000000000L,     0x140000000000L,     0x280000000000L,     0x500000000000L,     0xa00000000000L,     0x400000000000L,                0x0L, 
		        0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L,                0x0L, };
	
	public long[][] horizontalSlideMoves;
	public long[][] verticalSlideMoves;
	public long[][] antiClockwiseDiagonalSlideMoves;
	public long[][] clockwiseDiagonalSlideMoves;
	
	public static final long[] isolatedPawnMask =
	{
		0,0,0,0,0,0,0,0,
		0x0003030303030300L,0x0007070707070700L,0x000E0E0E0E0E0E00L,0x001C1C1C1C1C1C00L,0x0038383838383800L,0x0070707070707000L,0x00E0E0E0E0E0E000L,0x00C0C0C0C0C0C000L,
		0x0003030303030300L,0x0007070707070700L,0x000E0E0E0E0E0E00L,0x001C1C1C1C1C1C00L,0x0038383838383800L,0x0070707070707000L,0x00E0E0E0E0E0E000L,0x00C0C0C0C0C0C000L,
		0x0003030303030300L,0x0007070707070700L,0x000E0E0E0E0E0E00L,0x001C1C1C1C1C1C00L,0x0038383838383800L,0x0070707070707000L,0x00E0E0E0E0E0E000L,0x00C0C0C0C0C0C000L,
		0x0003030303030300L,0x0007070707070700L,0x000E0E0E0E0E0E00L,0x001C1C1C1C1C1C00L,0x0038383838383800L,0x0070707070707000L,0x00E0E0E0E0E0E000L,0x00C0C0C0C0C0C000L,
		0x0003030303030300L,0x0007070707070700L,0x000E0E0E0E0E0E00L,0x001C1C1C1C1C1C00L,0x0038383838383800L,0x0070707070707000L,0x00E0E0E0E0E0E000L,0x00C0C0C0C0C0C000L,
		0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0
	};

	public static final long[] whitePassedPawnMask =
	{
		0,0,0,0,0,0,0,0,
		0x0003030303030000L,0x0007070707070000L,0x000E0E0E0E0E0000L,0x001C1C1C1C1C0000L,0x0038383838380000L,0x0070707070700000L,0x00E0E0E0E0E00000L,0x00C0C0C0C0C00000L,
		0x0003030303000000L,0x0007070707000000L,0x000E0E0E0E000000L,0x001C1C1C1C000000L,0x0038383838000000L,0x0070707070000000L,0x00E0E0E0E0000000L,0x00C0C0C0C0000000L,
		0x0003030300000000L,0x0007070700000000L,0x000E0E0E00000000L,0x001C1C1C00000000L,0x0038383800000000L,0x0070707000000000L,0x00E0E0E000000000L,0x00C0C0C000000000L,
		0x0003030000000000L,0x0007070000000000L,0x000E0E0000000000L,0x001C1C0000000000L,0x0038380000000000L,0x0070700000000000L,0x00E0E00000000000L,0x00C0C00000000000L,
		0x0003000000000000L,0x0007000000000000L,0x000E000000000000L,0x001C000000000000L,0x0038000000000000L,0x0070000000000000L,0x00E0000000000000L,0x00C0000000000000L,
		0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0
	};
	
	public static final long[] blackPassedPawnMask =
	{
		0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,
		0x0000000000000300L,0x0000000000000700L,0x0000000000000E00L,0x0000000000001C00L,0x0000000000003800L,0x0000000000007000L,0x000000000000E000L,0x000000000000C000L,
		0x0000000000030300L,0x0000000000070700L,0x00000000000E0E00L,0x00000000001C1C00L,0x0000000000383800L,0x0000000000707000L,0x0000000000E0E000L,0x0000000000C0C000L,
		0x0000000003030300L,0x0000000007070700L,0x000000000E0E0E00L,0x000000001C1C1C00L,0x0000000038383800L,0x0000000070707000L,0x00000000E0E0E000L,0x00000000C0C0C000L,
		0x0000000303030300L,0x0000000707070700L,0x0000000E0E0E0E00L,0x0000001C1C1C1C00L,0x0000003838383800L,0x0000007070707000L,0x000000E0E0E0E000L,0x000000C0C0C0C000L,
		0x0000030303030300L,0x0000070707070700L,0x00000E0E0E0E0E00L,0x00001C1C1C1C1C00L,0x0000383838383800L,0x0000707070707000L,0x0000E0E0E0E0E000L,0x0000C0C0C0C0C000L,
		0,0,0,0,0,0,0,0
	};

	public static final int[] centrePoints =
	{ 
			0,0,0,0,0,0,0,0,
			0,1,1,1,1,1,1,0,
			0,1,2,2,2,2,1,0,
			0,1,2,3,3,2,1,0,
			0,1,2,3,3,2,1,0,
			0,1,2,2,2,2,1,0,
			0,1,1,1,1,1,1,1,
			0,0,0,0,0,0,0,0
	};
		
	public final int[] indexRotatedRight90 =
		{ 
			7,15,23,31,39,47,55,63,
			6,14,22,30,38,46,54,62,
			5,13,21,29,37,45,53,61,
			4,12,20,28,36,44,52,60,
			3,11,19,27,35,43,51,59,
			2,10,18,26,34,42,50,58,
			1,9,17,25,33,41,49,57,
			0,8,16,24,32,40,48,56
		};
	
	public final int[] diagonalLengthAfterClockwiseRotation =
	{
			1,2,3,4,5,6,7,8,
			2,3,4,5,6,7,8,7,
			3,4,5,6,7,8,7,6,
			4,5,6,7,8,7,6,5,
			5,6,7,8,7,6,5,4,
			6,7,8,7,6,5,4,3,
			7,8,7,6,5,4,3,2,
			8,7,6,5,4,3,2,1
			
	};
	
	public final int[] diagonalShiftAfterClockwiseRotation =
	{
			 8,16,24,32,40,48,56, 0,
			16,24,32,40,48,56, 0, 9,
			24,32,40,48,56, 0, 9,18,
			32,40,48,56, 0, 9,18,27,
			40,48,56, 0, 9,18,27,36,
			48,56, 0, 9,18,27,36,45,
			56, 0, 9,18,27,36,45,54,
			 0, 9,18,27,36,45,54,63
	};

	public final int[] occupancyPositionAfterClockwiseRotation =
	{
			0,1,2,3,4,5,6,7,
			0,1,2,3,4,5,6,6,
			0,1,2,3,4,5,5,5,
			0,1,2,3,4,4,4,4,
			0,1,2,3,3,3,3,3,
			0,1,2,2,2,2,2,2,
			0,1,1,1,1,1,1,1,
			0,0,0,0,0,0,0,0
	};
	
	public final int[] diagonalLengthAfterAntiClockwiseRotation =
	{
			8,7,6,5,4,3,2,1,
			7,8,7,6,5,4,3,2,
			6,7,8,7,6,5,4,3,
			5,6,7,8,7,6,5,4,
			4,5,6,7,8,7,6,5,
			3,4,5,6,7,8,7,6,
			2,3,4,5,6,7,8,7,
			1,2,3,4,5,6,7,8
	};
	
	public final int[] diagonalShiftAfterAntiClockwiseRotation =
	{
			 0,57,50,43,36,29,22,15,
			 8, 0,57,50,43,36,29,22,
			16, 8, 0,57,50,43,36,29,
			24,16, 8, 0,57,50,43,36,
			32,24,16, 8, 0,57,50,43,
			40,32,24,16, 8, 0,57,50,
			48,40,32,24,16, 8, 0,57,
			56,48,40,32,24,16, 8, 0
	};

	public final int[] occupancyPositionAfterAntiClockwiseRotation =
	{
			0,0,0,0,0,0,0,0,
			0,1,1,1,1,1,1,1,
			0,1,2,2,2,2,2,2,
			0,1,2,3,3,3,3,3,
			0,1,2,3,4,4,4,4,
			0,1,2,3,4,5,5,5,
			0,1,2,3,4,5,6,6,
			0,1,2,3,4,5,6,7
	};
	
	public static final int[] pieceSquareTablePawn = new int[]
    {
           0,  0,  0,  0,  0,  0,  0,  0,
           5, 10, 10,-25,-25, 10, 10,  5,
           5, -5,-10,  0,  0,-10, -5,  5,
           0,  0,  0, 25, 25,  0,  0,  0,
           5,  5, 10, 27, 27, 10,  5,  5,
           10, 10, 20, 30, 30, 20, 10, 10,
           50, 50, 50, 50, 50, 50, 50, 50,
           0,  0,  0,  0,  0,  0,  0,  0
    };	

	public static final int[] pieceSquareTableKnight = new int[]
    {
	    -50,-40,-30,-30,-30,-30,-40,-50,
	    -40,-20,  0,  0,  0,  0,-20,-40,
	    -30,  0, 10, 15, 15, 10,  0,-30,
	    -30,  5, 15, 20, 20, 15,  5,-30,
	    -30,  0, 15, 20, 20, 15,  0,-30,
	    -30,  5, 10, 15, 15, 10,  5,-30,
	    -40,-20,  0,  5,  5,  0,-20,-40,
	    -50,-40,-20,-30,-30,-20,-40,-50
    };	
	
	public static final int[] pieceSquareTableBishop = new int[]
    {
	    -20,-10,-40,-10,-10,-40,-10,-20,
	    -10,  5,  0,  0,  0,  0,  5,-10,
	    -10, 10, 10, 10, 10, 10, 10,-10,
	    -10,  0, 10, 10, 10, 10,  0,-10,
	    -10,  5,  5, 10, 10,  5,  5,-10,
	    -10,  0,  5, 10, 10,  5,  0,-10,
	    -10,  0,  0,  0,  0,  0,  0,-10,
	    -20,-10,-10,-10,-10,-10,-10,-20,
    };	

	public static final int[] pieceSquareTableRook = new int[]
    {
	     0, 0,20,20,20,20, 0, 0,
	     0, 0,20,20,20,20, 0, 0,
	     0, 0, 0,10,10, 0, 0, 0,
	     0, 0, 0,10,10, 0, 0, 0,
	     0, 0, 0,10,10, 0, 0, 0,
	     0, 0, 0,10,10, 0, 0, 0,
	    40,40,40,40,40,40,40,40,
	    10,10,10,10,10,10,10,10,
    };	

	public static final int[] pieceSquareTableQueenQueensideCastle = new int[]
	{
	     0, 5, 5,15,15, 5, 5, 5,
	     0, 5, 5, 5, 5,10,10,10,
	     0, 0, 0, 0,10,25,25,25,
	     0, 0, 5, 5,10,25,25,25,
	     0, 0, 5, 5,10,10,25,25,
	     0, 0, 5, 5,15,10,25,25,
	     0, 0, 0, 5, 5,10,10,15,
	     0, 0, 0, 0, 5, 5, 5, 5,
    };	

	public static final int[] pieceSquareTableQueenKingsideCastle = new int[]
 	{
	      5, 5, 5,15,15, 5, 5, 0,
 	     10,10,10, 5, 5, 5, 5, 0,
 	     25,25,25,10, 0, 5, 0, 0,
 	     25,25,25,10, 5, 5, 0, 0,
 	     25,25,10,15, 5, 5, 0, 0,
 	     25,25,10,15, 5, 5, 0, 0,
 	     15,10,10, 5, 5, 0, 0, 0,
 	     5, 5, 5, 5, 0, 0, 0, 0,
     };	
	
	public static final int[] pieceSquareTableKing = new int[]
	{
		   20,  30,  10,   0,   0,  10,  30,  20,
		   20,  20,   0,   0,   0,   0,  20,  20,
		  -10, -20, -20, -20, -20, -20, -20, -10, 
		  -20, -30, -30, -40, -40, -30, -30, -20,
		  -30, -40, -40, -50, -50, -40, -40, -30,
		  -30, -40, -40, -50, -50, -40, -40, -30,
		  -30, -40, -40, -50, -50, -40, -40, -30,
		  -30, -40, -40, -50, -50, -40, -40, -30,
    };	

	public static final int[] pieceSquareTableKingEndGame = new int[]
  	{
	    -50,-30,-30,-30,-30,-30,-30,-50,
	    -30,-30,  0,  0,  0,  0,-30,-30,
	    -30,-10, 20, 30, 30, 20,-10,-30,
	    -30,-10, 30, 40, 40, 30,-10,-30,
	    -30,-10, 30, 40, 40, 30,-10,-30,
	    -30,-10, 20, 30, 30, 20,-10,-30,
	    -30,-20,-10,  0,  0,-10,-20,-30,
	    -50,-40,-30,-20,-20,-30,-40,-50,
    };	

	public static final int[] pieceSquareEnemyKingEndGame = new int[]
  	{
	    50,30,30,30,30,30,30,50,
	    30,20,10, 5, 5,10,20,30,
	    30,10,10, 0, 0,10,10,30,
	    20,10, 0, 0, 0, 0,10,30,
	    20,10, 0, 0, 0, 0,10,30,
	    30,10,10, 0, 0,10,10,30,
	    30,20,10, 5, 5,10,20,30,
	    50,30,30,30,30,30,30,50
    };	
	
	public static int[][] tropism;
	
	public static byte[] firstBit16;
	public static byte[] firstBit8;
	
	public Bitboards()
	{
		if (!RivalConstants.GENERATE_SLIDING_MOVES_WITH_LOOPS)
		{
			antiClockwiseDiagonalSlideMoves = new long[64][64];
			clockwiseDiagonalSlideMoves = new long[64][64];

			generateHorizontalSlideBitboards();
			generateVerticalSlideBitboards();
			generateDiagonalBitboards();
		}
		generateFirstBitValues();
		generateTropismValues();
	}
	
	public static int getFirstBit8(long bitboard)
	{
		if ((bitboard & 0xff00000000000000L) != 0) return firstBit8[(int)(bitboard >>> 56L)] + 56;
		if ((bitboard & 0x00ff000000000000L) != 0) return firstBit8[(int)(bitboard >>> 48L)] + 48;
		if ((bitboard & 0x0000ff0000000000L) != 0) return firstBit8[(int)(bitboard >>> 40L)] + 40;
		if ((bitboard & 0x000000ff00000000L) != 0) return firstBit8[(int)(bitboard >>> 32L)] + 32;
		if ((bitboard & 0x00000000ff000000L) != 0) return firstBit8[(int)(bitboard >>> 24L)] + 24;
		if ((bitboard & 0x0000000000ff0000L) != 0) return firstBit8[(int)(bitboard >>> 16L)] + 16;
		if ((bitboard & 0x000000000000ff00L) != 0) return firstBit8[(int)(bitboard >>> 8L)] + 8;

		return firstBit8[(int)(bitboard)];
	}

	public static int getFirstBit16(long bitboard)
	{
		if ((bitboard & 0xffff000000000000L) != 0) return firstBit16[(int)(bitboard >>> 48L)] + 48;
		if ((bitboard & 0x0000ffff00000000L) != 0) return firstBit16[(int)(bitboard >>> 32L)] + 32;
		if ((bitboard & 0x00000000ffff0000L) != 0) return firstBit16[(int)(bitboard >>> 16L)] + 16;
		return firstBit16[(int)(bitboard)];
	}
	
	public static int countSetBits(long bitboard)
	{
		int count = 0;

		while (bitboard != 0) 
		{
			count ++;
			if ((bitboard & 0xffff000000000000L) != 0) bitboard ^= 1L << (firstBit16[(int)(bitboard >>> 48L)] + 48); else
				if ((bitboard & 0x0000ffff00000000L) != 0) bitboard ^= 1L << (firstBit16[(int)(bitboard >>> 32L)] + 32); else
					if ((bitboard & 0x00000000ffff0000L) != 0) bitboard ^= 1L << (firstBit16[(int)(bitboard >>> 16L)] + 16); else
						bitboard ^= 1L << firstBit16[(int)(bitboard)];
		}
		
		return count;
	}
	
	public void generateTropismValues()
	{
		tropism = new int[64][64];
		int i, j, iFile, jFile, iRank, jRank, t1, t2;
		
		for (i=0; i<64; i++)
		{
			for (j=0; j<64; j++)
			{
				iFile = i % 8;
				jFile = j % 8;
				iRank = i / 8;
				jRank = j / 8;
				t1 = 7 - Math.abs(iFile - jFile);
				t2 = 7 - Math.abs(iRank - jRank);
				tropism[i][j] = Math.min(t1, t2);
			}
		}
	}
	
	public void generateFirstBitValues()
	{
		firstBit8 = new byte[256];
		firstBit16 = new byte[65536];

		firstBit8[0] = -1;
		for (int i=1; i<256; i++)
		{
			for (byte j=0; j<8; j++)
			{
				if ((i & (1L << j)) != 0)
				{
					firstBit8[i] = j;
					break;
				}
			}
		}
		
		firstBit16[0] = -1;
		//System.out.print(-1 + ",");
		for (int i=1; i<65536; i++)
		{
			for (byte j=0; j<16; j++)
			{
				if ((i & (1L << j)) != 0)
				{
					firstBit16[i] = j;
					break;
				}
			}
			//System.out.print(firstBit16[i] + ",");
			//if (i % 500 == 0)
			//{
			//	System.out.println();
			//}
		}
		
		//System.exit(0);
	}
	
	public long getHorizontalSlideMoves256Bitmap(int file, int occupancy)
	{
		boolean[] squarePopulated = new boolean[8];
		boolean[] squareAvailable = new boolean[8];
		int i;
		long retBitboard = 0L;
		
		for (i=0; i<8; i++)squareAvailable[i] = false;
		
		squarePopulated[0] = (occupancy & 1) != 0;
		squarePopulated[1] = (occupancy & 2) != 0;
		squarePopulated[2] = (occupancy & 4) != 0;
		squarePopulated[3] = (occupancy & 8) != 0;
		squarePopulated[4] = (occupancy & 16) != 0;
		squarePopulated[5] = (occupancy & 32) != 0;
		squarePopulated[6] = (occupancy & 64) != 0;
		squarePopulated[7] = (occupancy & 128) != 0;
		
		// go left
		for (i=file+1; i<=7; i++)
		{
			squareAvailable[i] = true;
			if (squarePopulated[i])
			{
				break;
			}
		}

		// go right
		for (i=file-1; i>=0; i--)
		{
			squareAvailable[i] = true;
			if (squarePopulated[i])
			{
				break;
			}
		}
		
		for (i=0; i<=7; i++)
		{
			if (squareAvailable[i])
			{
				retBitboard |= (long)Math.pow(2,i); 
			}
		}
		
		return retBitboard;
	}
	
	public void calculateDiagonalMoves(int bitRef, int occupancy, boolean isClockwise)
	{
		int i;
		int bigOccupancy;
		int bitRefPositionInOccupancy;
		int iBitValue;
		long bitboardAvailableSquares; // starts as 8 bit mask and is then turned into a bitboard
		
		bigOccupancy = occupancy << 1;
		if (isClockwise)
		{
			bitRefPositionInOccupancy = occupancyPositionAfterClockwiseRotation[bitRef];
		}
		else
		{
			bitRefPositionInOccupancy = occupancyPositionAfterAntiClockwiseRotation[bitRef];
		}
		bitboardAvailableSquares = 0L;

		for (i=bitRefPositionInOccupancy-1; i>=0; i--)
		{
			iBitValue = (int)Math.pow(2,i);
			bitboardAvailableSquares |= iBitValue;
			if ((bigOccupancy & iBitValue) != 0) // if i bit is set in occupancy
			{
				break;
			}
		}
		for (i=bitRefPositionInOccupancy+1; i<=7; i++)
		{
			iBitValue = (int)Math.pow(2,i);
			bitboardAvailableSquares |= iBitValue;
			if ((bigOccupancy & iBitValue) != 0) // if i bit is set in occupancy
			{
				break;
			}
		}
		
		if (isClockwise)
		{
			// wipe out unwanted bits which are longer than the diagonal
			bitboardAvailableSquares &= (int)Math.pow(2, diagonalLengthAfterClockwiseRotation[bitRef]) - 1;
			// shift this mask into place according to the bitRef
			bitboardAvailableSquares <<= diagonalShiftAfterClockwiseRotation[bitRef];
			// rotate the board clockwise so it references the original position
			bitboardAvailableSquares = BitwiseOperation.reverseRotateBitboard45ClockwiseFast(bitboardAvailableSquares);
			
			clockwiseDiagonalSlideMoves[bitRef][occupancy] = bitboardAvailableSquares;
		}
		else
		{
			// wipe out unwanted bits which are longer than the diagonal
			bitboardAvailableSquares &= (int)Math.pow(2, diagonalLengthAfterAntiClockwiseRotation[bitRef]) - 1;
			// shift this mask into place according to the bitRef
			bitboardAvailableSquares <<= diagonalShiftAfterAntiClockwiseRotation[bitRef];
			// rotate the board clockwise so it references the original position
			bitboardAvailableSquares = BitwiseOperation.reverseRotateBitboard45AntiClockwiseFast(bitboardAvailableSquares);
			
			antiClockwiseDiagonalSlideMoves[bitRef][occupancy] = bitboardAvailableSquares;
		}
	}
	
	public void generateDiagonalBitboards()
	{
		int occupancy;
		
		for (int bitRef=0; bitRef<64; bitRef++)
		{
			for (occupancy=0; occupancy<64; occupancy++)
			{
				calculateDiagonalMoves(bitRef, occupancy, true); 
				calculateDiagonalMoves(bitRef, occupancy, false); 
			}
		}
	}

	public void generateHorizontalSlideBitboards()
	{
		horizontalSlideMoves = new long[64][64];
		int rank, file, occupancy;
		
		// run along the bottom rank, right to left
		for (file=0; file<=7; file++)
		{
			for (occupancy=0; occupancy<64; occupancy++)
			{
				horizontalSlideMoves[file][occupancy] = getHorizontalSlideMoves256Bitmap(file, (occupancy << 1));
				
				// calculate for each rank
				for (rank=1; rank<=7; rank++)
				{
					horizontalSlideMoves[file+(rank*8)][occupancy] = horizontalSlideMoves[file][occupancy] << (rank*8);
				}
			}
		}
		//printBitboard(horizontalSlideMoves[45][12]);
		//printBitboard(rotate90Right(horizontalSlideMoves[45][12]));
	}
	
	public void generateVerticalSlideBitboards()
	{
		// requires prior call to generateHorizontalSlideBitboards()
		verticalSlideMoves = new long[64][64];
		for (int i=0; i<64; i++)
		{
			for (int j=0; j<64; j++)
			{
				// we rotate the AllPieces bitboard left to calc the occupancy when generating moves
				// so flip these right, including the index, to generate the correct grid to use
				
				verticalSlideMoves[indexRotatedRight90[i]][j] = BitwiseOperation.rotateBitboard90Clockwise(horizontalSlideMoves[i][j]);
			}
		}
		//printBitboard(verticalSlideMoves[7][129]);
	}
	
//	public void generatePawnBitboards()
//	{
//		whitePawnMovesForward = new long[64];
//		blackPawnMovesForward = new long[64];
//		whitePawnMovesCapture = new long[64];
//		blackPawnMovesCapture = new long[64];
//		int i;
//		long pow;
//		for (i=8; i<56; i++)
//		{
//			pow = (long)Math.pow(2,i);
//			whitePawnMovesForward[i] = pow << 8;
//			blackPawnMovesForward[i] = pow >>> 8;
//			whitePawnMovesCapture[i] = (pow << 7) & ~FILE_A;
//			blackPawnMovesCapture[i] = (pow >>> 7) & ~FILE_H;
//			whitePawnMovesCapture[i] |= (pow << 9) & ~FILE_H;
//			blackPawnMovesCapture[i] |= (pow >>> 9) & ~FILE_A;
//		}
//
//		System.out.println("public static final long[] whitePawnMovesForward = {");
//		for (i=0; i<64; i++)
//		{
//			System.out.print(pad("0x" + Long.toHexString(whitePawnMovesForward[i]) + "L,", 20));
//			if (i % 8 == 0) System.out.println();
//		}
//		System.out.println("}");
//		System.out.println("public static final long[] whitePawnMovesCapture = {");
//		for (i=0; i<64; i++)
//		{
//			System.out.print(pad("0x" + Long.toHexString(whitePawnMovesCapture[i]) + "L,", 20));
//			if (i % 8 == 0) System.out.println();
//		}
//		System.out.println("}");
//		System.out.println("public static final long[] blackPawnMovesForward = {");
//		for (i=0; i<64; i++)
//		{
//			System.out.print(pad("0x" + Long.toHexString(blackPawnMovesForward[i]) + "L,", 20));
//			if (i % 8 == 0) System.out.println();
//		}
//		System.out.println("}");
//		System.out.println("public static final long[] blackPawnMovesCapture = {");
//		for (i=0; i<64; i++)
//		{
//			System.out.print(pad("0x" + Long.toHexString(blackPawnMovesCapture[i]) + "L,", 20));
//			if (i % 8 == 0) System.out.println();
//		}
//		System.out.println("}");
//		
//		System.exit(0);
//	}

//	public void generateKnightBitboards()
//	{
//		knightMoves = new long[64];
//		
//		for (int bit=63; bit>=23; bit-=8)
//		{
//			knightMoves[bit] = (KNIGHT_18 << (bit-18)) & ~FILE_H & ~FILE_G;
//			knightMoves[bit-1] = (KNIGHT_18 << (bit-19)) & ~FILE_H;
//			knightMoves[bit-2] = (KNIGHT_18 << (bit-20));
//			knightMoves[bit-3] = (KNIGHT_18 << (bit-21));
//			knightMoves[bit-4] = (KNIGHT_18 << (bit-22));
//			knightMoves[bit-5] = (KNIGHT_18 << (bit-23));
//			knightMoves[bit-6] = (KNIGHT_18 << (bit-24)) & ~FILE_A;
//			knightMoves[bit-7] = (KNIGHT_18 << (bit-25)) & ~FILE_A & ~FILE_B;
//		}
//		
//		knightMoves[23] = (KNIGHT_18 << 5) & ~FILE_H & ~FILE_G;
//		knightMoves[22] = (KNIGHT_18 << 4) & ~FILE_H;
//		knightMoves[21] = (KNIGHT_18 << 3);
//		knightMoves[20] = (KNIGHT_18 << 2);
//		knightMoves[19] = (KNIGHT_18 << 1);
//		knightMoves[18] = KNIGHT_18;
//		knightMoves[17] = (KNIGHT_18 >>> 1) & ~FILE_A;
//		knightMoves[16] = (KNIGHT_18 >>> 2) & ~FILE_A & ~FILE_B;
//
//		knightMoves[15] = (KNIGHT_18 >>> 3) & ~FILE_H & ~FILE_G;
//		knightMoves[14] = (KNIGHT_18 >>> 4) & ~FILE_H;
//		knightMoves[13] = (KNIGHT_18 >>> 5);
//		knightMoves[12] = (KNIGHT_18 >>> 6);
//		knightMoves[11] = (KNIGHT_18 >>> 7);
//		knightMoves[10] = (KNIGHT_18 >>> 8);
//		knightMoves[9] = (KNIGHT_18 >>> 9) & ~FILE_A;
//		knightMoves[8] = (KNIGHT_18 >>> 10) & ~FILE_A & ~FILE_B;
//
//		knightMoves[7] = (KNIGHT_18 >>> 11) & ~FILE_H & ~FILE_G;
//		knightMoves[6] = (KNIGHT_18 >>> 12) & ~FILE_H;
//		knightMoves[5] = (KNIGHT_18 >>> 13);
//		knightMoves[4] = (KNIGHT_18 >>> 14);
//		knightMoves[3] = (KNIGHT_18 >>> 15);
//		knightMoves[2] = (KNIGHT_18 >>> 16);
//		knightMoves[1] = (KNIGHT_18 >>> 17) & ~FILE_A;
//		knightMoves[0] = (KNIGHT_18 >>> 18) & ~FILE_A & ~FILE_B;
//		
//		print64ElementArray(knightMoves);
//		System.exit(0);
//	}
	
//	public void generateKingMoveBitboards()
//	{
//		kingMoves = new long[64];
//		
//		for (int bit=63; bit>=23; bit-=8)
//		{
//			kingMoves[bit] = (KING_9 << (bit-9)) & ~FILE_H;
//			kingMoves[bit-1] = (KING_9 << (bit-10));
//			kingMoves[bit-2] = (KING_9 << (bit-11));
//			kingMoves[bit-3] = (KING_9 << (bit-12));
//			kingMoves[bit-4] = (KING_9 << (bit-13));
//			kingMoves[bit-5] = (KING_9 << (bit-14));
//			kingMoves[bit-6] = (KING_9 << (bit-15));
//			kingMoves[bit-7] = (KING_9 << (bit-16)) & ~FILE_A;
//		}
//		
//		kingMoves[15] = (KING_9 << 6) & ~FILE_H;
//		kingMoves[14] = (KING_9 << 5);
//		kingMoves[13] = (KING_9 << 4);
//		kingMoves[12] = (KING_9 << 3);
//		kingMoves[11] = (KING_9 << 2);
//		kingMoves[10] = (KING_9 << 1);
//		kingMoves[9] = KING_9;
//		kingMoves[8] = (KING_9 >>> 1) & ~FILE_A;
//		
//		kingMoves[7] = (KING_9 >>> 2) & ~FILE_H;
//		kingMoves[6] = (KING_9 >>> 3);
//		kingMoves[5] = (KING_9 >>> 4);
//		kingMoves[4] = (KING_9 >>> 5);
//		kingMoves[3] = (KING_9 >>> 6);
//		kingMoves[2] = (KING_9 >>> 7);
//		kingMoves[1] = (KING_9 >>> 8);
//		kingMoves[0] = (KING_9 >>> 9) & ~FILE_A;
//		
//		print64ElementArray(kingMoves);
//		System.exit(0);
//	}
	
	public static int[] getSetBitsNew(long bitboard)
	{
		int[] setBits = new int[64];
		int bitsSet = 0;
		while (bitboard != 0)
		{
			setBits[bitsSet] = Bitboards.getFirstBit16(bitboard);
			bitboard ^= (1L << setBits[bitsSet++]);
		}
		
		setBits[bitsSet] = -1;
		return setBits;
	}

	public static int[] getSetBits(long bitboard)
	{
		int bitsSet = 0;
		while (bitboard != 0)
		{
			setBits[bitsSet] = Bitboards.getFirstBit16(bitboard);
			bitboard ^= (1L << setBits[bitsSet++]);
		}
		
		setBits[bitsSet] = -1;
		return setBits;
	}

	public static int getSetBitsWithArray(long bitboard, int[] a)
	{
		int bitsSet = 0;
		while (bitboard != 0)
		{
			a[bitsSet] = Bitboards.getFirstBit16(bitboard);
			bitboard ^= (1L << a[bitsSet++]);
		}
		
		a[bitsSet] = -1;
		return bitsSet;
	}
	
	public static int[] getSetBitsSlow(long bitboard)
	{
		int bitsSet = 0;
		for (int i=0; i<64; i++)
		{
			if ((bitboard & 1L) == 1)
			{
				setBits[bitsSet] = i;
				bitsSet ++;
			}
			bitboard >>>= 1L;
			if (bitboard == 0)
			{
				break;
			}
		}
		
		setBits[bitsSet] = -1;
		return setBits;
	}
	
	public static String pad(String s, int size)
	{
		String retStr = "";
		int l = s.length();
		for (int i=l; i<size; i++)
		{
			retStr += " ";
		}
		return retStr + s + " ";
	}
	
	public static void printBitboard(long bitboard)
	{
		long mask;
		char bit;
		for (int i=63; i>=0; i--)
		{
			mask = 1L << i;
			if ((bitboard & mask) != 0)
			{
				bit = '*';
			}
			else
			{
				bit = '-';
			}
			if (i % 8 == 0)
			{
				System.out.println(bit);
			}
			else
			{
				System.out.print(bit);
			}
		}
		System.out.println();
	}
	
	public void print64ElementArray(long[] a)
	{
		System.out.println("public static final long[] a = {");
		for (int i=0; i<64; i++)
		{
			System.out.print(pad("0x" + Long.toHexString(a[i]) + "L,", 20));
			if (i % 8 == 0) System.out.println();
		}
		System.out.println("};");
	}
}
