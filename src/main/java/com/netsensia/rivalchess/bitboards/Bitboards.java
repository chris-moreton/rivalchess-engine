package com.netsensia.rivalchess.bitboards;

import com.netsensia.rivalchess.constants.Colour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Bitboards {
    public static final MagicBitboards magicBitboards = new MagicBitboards();

    public static final long RANK_8 = 0xFF00000000000000L;
    public static final long RANK_7 = 0x00FF000000000000L;
    public static final long RANK_6 = 0x0000FF0000000000L;
    public static final long RANK_5 = 0x000000FF00000000L;
    public static final long RANK_4 = 0x00000000FF000000L;
    public static final long RANK_3 = 0x0000000000FF0000L;
    public static final long RANK_2 = 0x000000000000FF00L;
    public static final long RANK_1 = 0x00000000000000FFL;

    public static final long FILE_A = 1L << 7 | 1L << 15 | 1L << 23 | 1L << 31 | 1L << 39 | 1L << 47 | 1L << 55 | 1L << 63;
    public static final long FILE_B = 1L << 6 | 1L << 14 | 1L << 22 | 1L << 30 | 1L << 38 | 1L << 46 | 1L << 54 | 1L << 62;
    public static final long FILE_C = 1L << 5 | 1L << 13 | 1L << 21 | 1L << 29 | 1L << 37 | 1L << 45 | 1L << 53 | 1L << 61;
    public static final long FILE_D = 1L << 4 | 1L << 12 | 1L << 20 | 1L << 28 | 1L << 36 | 1L << 44 | 1L << 52 | 1L << 60;
    public static final long FILE_E = 1L << 3 | 1L << 11 | 1L << 19 | 1L << 27 | 1L << 35 | 1L << 43 | 1L << 51 | 1L << 59;
    public static final long FILE_F = 1L << 2 | 1L << 10 | 1L << 18 | 1L << 26 | 1L << 34 | 1L << 42 | 1L << 50 | 1L << 58;
    public static final long FILE_G = 1L << 1 | 1L << 9 | 1L << 17 | 1L << 25 | 1L << 33 | 1L << 41 | 1L << 49 | 1L << 57;
    public static final long FILE_H = 1L | 1L << 8 | 1L << 16 | 1L << 24 | 1L << 32 | 1L << 40 | 1L << 48 | 1L << 56;

    public static final int A1 = 7;
    public static final int D1 = 4;
    public static final int F1 = 2;
    public static final int H1 = 0;
    public static final int A8 = 63;
    public static final int D8 = 60;
    public static final int F8 = 58;
    public static final int H8 = 56;
    public static final int A2 = 15;
    public static final int A7 = 55;
    public static final int B3 = 22;
    public static final int B6 = 46;
    public static final int C2 = 13;
    public static final int C7 = 53;
    public static final int F2 = 10;
    public static final int F7 = 50;
    public static final int G3 = 17;
    public static final int G6 = 41;
    public static final int H2 = 8;
    public static final int H7 = 48;

    public static final long MIDDLE_FILES_8_BIT = 0xE7;
    public static final long NONMID_FILES_8_BIT = 0x18;

    public static final long F1G1 = (1L << 1) | (1L << 2);
    public static final long G1H1 = 1L | (1L << 1);
    public static final long A1B1 = (1L << 7) | (1L << 6);
    public static final long B1C1 = (1L << 6) | (1L << 5);
    public static final long F8G8 = (1L << 58) | (1L << 57);
    public static final long G8H8 = (1L << 57) | (1L << 56);
    public static final long A8B8 = (1L << 63) | (1L << 62);
    public static final long B8C8 = (1L << 62) | (1L << 61);

    public static final List<Long> FILES =
            Collections.unmodifiableList(Arrays.asList(FILE_H, FILE_G, FILE_F, FILE_E, FILE_D, FILE_C, FILE_B, FILE_A));

    public static final long LIGHT_SQUARES = 0xAA55AA55AA55AA55L;
    public static final long DARK_SQUARES = 0x55AA55AA55AA55AAL;

    public static final long A1A2B1B2 = (1L << 7) | (1L << 6) | (1L << 15) | (1L << 14);
    public static final long A8A7B8B7 = (1L << 63) | (1L << 62) | (1L << 55) | (1L << 54);
    public static final long H8H7G8G7 = (1L << 57) | (1L << 56) | (1L << 49) | (1L << 48);
    public static final long H1H2G1G2 = 1L | (1L << 1) | (1L << 8) | (1L << 9);
    public static final long A2A7H2H7 = (1L << 15) | (1L << 55) | (1L << 8) | (1L << 48);

    public static final long LOW32 = 0x00000000FFFFFFFFL;

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

    public static final long WHITEKINGSIDEROOKMASK = 1L;
    public static final long WHITEQUEENSIDEROOKMASK = 1L << 7;
    public static final long BLACKKINGSIDEROOKMASK = 1L << 56;
    public static final long BLACKQUEENSIDEROOKMASK = 1L << 63;

    public static final List<Integer> distanceToH1OrA8 =
            Collections.unmodifiableList(Arrays.asList(
                    0, 1, 2, 3, 4, 5, 6, 7,
                    1, 2, 3, 4, 5, 6, 7, 6,
                    2, 3, 4, 5, 6, 7, 6, 5,
                    3, 4, 5, 6, 7, 6, 5, 4,
                    4, 5, 6, 7, 6, 5, 4, 3,
                    5, 6, 7, 6, 5, 4, 3, 2,
                    6, 7, 6, 5, 4, 3, 2, 1,
                    7, 6, 5, 4, 3, 2, 1, 0));

    public static final List<Long> knightMoves = Collections.unmodifiableList(Arrays.asList(
            0x20400L,
            0x50800L, 0xa1100L, 0x142200L, 0x284400L, 0x508800L, 0xa01000L, 0x402000L, 0x2040004L,
            0x5080008L, 0xa110011L, 0x14220022L, 0x28440044L, 0x50880088L, 0xa0100010L, 0x40200020L, 0x204000402L,
            0x508000805L, 0xa1100110aL, 0x1422002214L, 0x2844004428L, 0x5088008850L, 0xa0100010a0L, 0x4020002040L, 0x20400040200L,
            0x50800080500L, 0xa1100110a00L, 0x142200221400L, 0x284400442800L, 0x508800885000L, 0xa0100010a000L, 0x402000204000L, 0x2040004020000L,
            0x5080008050000L, 0xa1100110a0000L, 0x14220022140000L, 0x28440044280000L, 0x50880088500000L, 0xa0100010a00000L, 0x40200020400000L, 0x204000402000000L,
            0x508000805000000L, 0xa1100110a000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L, 0x400040200000000L,
            0x800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L, 0x4020000000000L,
            0x8050000000000L, 0x110a0000000000L, 0x22140000000000L, 0x44280000000000L, 0x88500000000000L, 0x10a00000000000L, 0x20400000000000L));

    public static final List<Long> kingMoves = Collections.unmodifiableList(Arrays.asList(
            0x302L,
            0x705L, 0xe0aL, 0x1c14L, 0x3828L, 0x7050L, 0xe0a0L, 0xc040L, 0x30203L,
            0x70507L, 0xe0a0eL, 0x1c141cL, 0x382838L, 0x705070L, 0xe0a0e0L, 0xc040c0L, 0x3020300L,
            0x7050700L, 0xe0a0e00L, 0x1c141c00L, 0x38283800L, 0x70507000L, 0xe0a0e000L, 0xc040c000L, 0x302030000L,
            0x705070000L, 0xe0a0e0000L, 0x1c141c0000L, 0x3828380000L, 0x7050700000L, 0xe0a0e00000L, 0xc040c00000L, 0x30203000000L,
            0x70507000000L, 0xe0a0e000000L, 0x1c141c000000L, 0x382838000000L, 0x705070000000L, 0xe0a0e0000000L, 0xc040c0000000L, 0x3020300000000L,
            0x7050700000000L, 0xe0a0e00000000L, 0x1c141c00000000L, 0x38283800000000L, 0x70507000000000L, 0xe0a0e000000000L, 0xc040c000000000L, 0x302030000000000L,
            0x705070000000000L, 0xe0a0e0000000000L, 0x1c141c0000000000L, 0x3828380000000000L, 0x7050700000000000L, 0xe0a0e00000000000L, 0xc040c00000000000L, 0x203000000000000L,
            0x507000000000000L, 0xa0e000000000000L, 0x141c000000000000L, 0x2838000000000000L, 0x5070000000000000L, 0xa0e0000000000000L, 0x40c0000000000000L));

    public static final List<Long> whitePawnMovesForward = Collections.unmodifiableList(Arrays.asList(
            0x100L, 0x200L, 0x400L, 0x800L, 0x1000L, 0x2000L, 0x4000L, 0x8000L,
            0x10000L, 0x20000L, 0x40000L, 0x80000L, 0x100000L, 0x200000L, 0x400000L, 0x800000L,
            0x1000000L, 0x2000000L, 0x4000000L, 0x8000000L, 0x10000000L, 0x20000000L, 0x40000000L, 0x80000000L,
            0x100000000L, 0x200000000L, 0x400000000L, 0x800000000L, 0x1000000000L, 0x2000000000L, 0x4000000000L, 0x8000000000L,
            0x10000000000L, 0x20000000000L, 0x40000000000L, 0x80000000000L, 0x100000000000L, 0x200000000000L, 0x400000000000L, 0x800000000000L,
            0x1000000000000L, 0x2000000000000L, 0x4000000000000L, 0x8000000000000L, 0x10000000000000L, 0x20000000000000L, 0x40000000000000L, 0x80000000000000L,
            0x100000000000000L, 0x200000000000000L, 0x400000000000000L, 0x800000000000000L, 0x1000000000000000L, 0x2000000000000000L, 0x4000000000000000L, 0x8000000000000000L,
            0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L));

    public static final List<Long> whitePawnMovesCapture = Collections.unmodifiableList(Arrays.asList(
            0x200L, 0x500L, 0xa00L, 0x1400L, 0x2800L, 0x5000L, 0xa000L, 0x4000L,
            0x20000L, 0x50000L, 0xa0000L, 0x140000L, 0x280000L, 0x500000L, 0xa00000L, 0x400000L,
            0x2000000L, 0x5000000L, 0xa000000L, 0x14000000L, 0x28000000L, 0x50000000L, 0xa0000000L, 0x40000000L,
            0x200000000L, 0x500000000L, 0xa00000000L, 0x1400000000L, 0x2800000000L, 0x5000000000L, 0xa000000000L, 0x4000000000L,
            0x20000000000L, 0x50000000000L, 0xa0000000000L, 0x140000000000L, 0x280000000000L, 0x500000000000L, 0xa00000000000L, 0x400000000000L,
            0x2000000000000L, 0x5000000000000L, 0xa000000000000L, 0x14000000000000L, 0x28000000000000L, 0x50000000000000L, 0xa0000000000000L, 0x40000000000000L,
            0x200000000000000L, 0x500000000000000L, 0xa00000000000000L, 0x1400000000000000L, 0x2800000000000000L, 0x5000000000000000L, 0xa000000000000000L, 0x4000000000000000L,
            0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L));

    public static final List<Long> blackPawnMovesForward = Collections.unmodifiableList(Arrays.asList(
            0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L,
            0x1L, 0x2L, 0x4L, 0x8L, 0x10L, 0x20L, 0x40L, 0x80L,
            0x100L, 0x200L, 0x400L, 0x800L, 0x1000L, 0x2000L, 0x4000L, 0x8000L,
            0x10000L, 0x20000L, 0x40000L, 0x80000L, 0x100000L, 0x200000L, 0x400000L, 0x800000L,
            0x1000000L, 0x2000000L, 0x4000000L, 0x8000000L, 0x10000000L, 0x20000000L, 0x40000000L, 0x80000000L,
            0x100000000L, 0x200000000L, 0x400000000L, 0x800000000L, 0x1000000000L, 0x2000000000L, 0x4000000000L, 0x8000000000L,
            0x10000000000L, 0x20000000000L, 0x40000000000L, 0x80000000000L, 0x100000000000L, 0x200000000000L, 0x400000000000L, 0x800000000000L,
            0x1000000000000L, 0x2000000000000L, 0x4000000000000L, 0x8000000000000L, 0x10000000000000L, 0x20000000000000L, 0x40000000000000L, 0x80000000000000L));

    public static final List<Long> blackPawnMovesCapture = Collections.unmodifiableList(Arrays.asList(
            0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L, 0x0L,
            0x2L, 0x5L, 0xaL, 0x14L, 0x28L, 0x50L, 0xa0L, 0x40L,
            0x200L, 0x500L, 0xa00L, 0x1400L, 0x2800L, 0x5000L, 0xa000L, 0x4000L,
            0x20000L, 0x50000L, 0xa0000L, 0x140000L, 0x280000L, 0x500000L, 0xa00000L, 0x400000L,
            0x2000000L, 0x5000000L, 0xa000000L, 0x14000000L, 0x28000000L, 0x50000000L, 0xa0000000L, 0x40000000L,
            0x200000000L, 0x500000000L, 0xa00000000L, 0x1400000000L, 0x2800000000L, 0x5000000000L, 0xa000000000L, 0x4000000000L,
            0x20000000000L, 0x50000000000L, 0xa0000000000L, 0x140000000000L, 0x280000000000L, 0x500000000000L, 0xa00000000000L, 0x400000000000L,
            0x2000000000000L, 0x5000000000000L, 0xa000000000000L, 0x14000000000000L, 0x28000000000000L, 0x50000000000000L, 0xa0000000000000L, 0x40000000000000L));

    public static final List<Long> whitePassedPawnMask = Collections.unmodifiableList(Arrays.asList(
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            0x0003030303030000L, 0x0007070707070000L, 0x000E0E0E0E0E0000L, 0x001C1C1C1C1C0000L, 0x0038383838380000L, 0x0070707070700000L, 0x00E0E0E0E0E00000L, 0x00C0C0C0C0C00000L,
            0x0003030303000000L, 0x0007070707000000L, 0x000E0E0E0E000000L, 0x001C1C1C1C000000L, 0x0038383838000000L, 0x0070707070000000L, 0x00E0E0E0E0000000L, 0x00C0C0C0C0000000L,
            0x0003030300000000L, 0x0007070700000000L, 0x000E0E0E00000000L, 0x001C1C1C00000000L, 0x0038383800000000L, 0x0070707000000000L, 0x00E0E0E000000000L, 0x00C0C0C000000000L,
            0x0003030000000000L, 0x0007070000000000L, 0x000E0E0000000000L, 0x001C1C0000000000L, 0x0038380000000000L, 0x0070700000000000L, 0x00E0E00000000000L, 0x00C0C00000000000L,
            0x0003000000000000L, 0x0007000000000000L, 0x000E000000000000L, 0x001C000000000000L, 0x0038000000000000L, 0x0070000000000000L, 0x00E0000000000000L, 0x00C0000000000000L,
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));

    public static final List<Long> blackPassedPawnMask = Collections.unmodifiableList(Arrays.asList(
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
            0x0000000000000300L, 0x0000000000000700L, 0x0000000000000E00L, 0x0000000000001C00L, 0x0000000000003800L, 0x0000000000007000L, 0x000000000000E000L, 0x000000000000C000L,
            0x0000000000030300L, 0x0000000000070700L, 0x00000000000E0E00L, 0x00000000001C1C00L, 0x0000000000383800L, 0x0000000000707000L, 0x0000000000E0E000L, 0x0000000000C0C000L,
            0x0000000003030300L, 0x0000000007070700L, 0x000000000E0E0E00L, 0x000000001C1C1C00L, 0x0000000038383800L, 0x0000000070707000L, 0x00000000E0E0E000L, 0x00000000C0C0C000L,
            0x0000000303030300L, 0x0000000707070700L, 0x0000000E0E0E0E00L, 0x0000001C1C1C1C00L, 0x0000003838383800L, 0x0000007070707000L, 0x000000E0E0E0E000L, 0x000000C0C0C0C000L,
            0x0000030303030300L, 0x0000070707070700L, 0x00000E0E0E0E0E00L, 0x00001C1C1C1C1C00L, 0x0000383838383800L, 0x0000707070707000L, 0x0000E0E0E0E0E000L, 0x0000C0C0C0C0C000L,
            0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));

    public static final List<Long> whiteKingShieldMask = Collections.unmodifiableList(Arrays.asList(
            (1L << 9 | 1L << 8),
            (1L << 10 | 1L << 9 | 1L << 8),
            (1L << 11 | 1L << 10 | 1L << 9),
            (1L << 12 | 1L << 11 | 1L << 10),
            (1L << 13 | 1L << 12 | 1L << 11),
            (1L << 14 | 1L << 13 | 1L << 12),
            (1L << 15 | 1L << 14 | 1L << 13),
            (1L << 15 | 1L << 14)));

    public static final List<Integer> pieceSquareTablePawn = Collections.unmodifiableList(Arrays.asList(
                    0, 0, 0, 0, 0, 0, 0, 0,
                    -6, 4, 4, -15, -15, 4, 4, -6,
                    -6, 4, 2, 5, 5, 2, 4, -6,
                    -6, 4, 5, 16, 16, 5, 4, -6,
                    -5, 4, 10, 20, 20, 10, 4, -5,
                    3, 12, 20, 28, 28, 20, 12, 3,
                    8, 16, 24, 32, 32, 24, 16, 8,
                    0, 0, 0, 0, 0, 0, 0, 0));

    public static final List<Integer> pieceSquareTablePawnEndGame = Collections.unmodifiableList(Arrays.asList(
                    0, 0, 0, 0, 0, 0, 0, 0,
                    -20, 0, 0, 0, 0, 0, 0, -20,
                    -15, 5, 5, 5, 5, 5, 5, -15,
                    -10, 10, 10, 10, 10, 10, 10, -10,
                    5, 25, 25, 25, 25, 25, 25, 5,
                    20, 30, 35, 35, 35, 35, 30, 20,
                    25, 40, 45, 45, 45, 45, 40, 25,
                    0, 0, 0, 0, 0, 0, 0, 0
    ));

    public static final List<Integer> pieceSquareTableKnight = Collections.unmodifiableList(Arrays.asList(
                    -50, -40, -30, -20, -20, -30, -40, -50,
                    -40, -30, -10, 0, 0, -10, -30, -40,
                    -20, -10, 0, 0, 0, 0, -10, -20,
                    -17, 0, 3, 20, 20, 3, 0, -17,
                    -17, 0, 10, 20, 20, 10, 0, -17,
                    -20, 5, 7, 15, 15, 7, 5, -20,
                    -40, -30, -10, 0, 0, -10, -30, -40,
                    -50, -40, -30, -20, -20, -30, -40, -50
    ));

    public static final List<Integer> pieceSquareTableKnightEndGame = Collections.unmodifiableList(Arrays.asList(
                    -50, -40, -30, -20, -20, -30, -40, -50,
                    -40, -30, -10, -5, -5, -10, -30, -40,
                    -30, -10, 0, 10, 10, 0, -10, -30,
                    -20, -5, 10, 20, 20, 10, -5, -20,
                    -20, -5, 10, 20, 20, 10, -5, -20,
                    -30, -10, 0, 10, 10, 0, -10, -30,
                    -40, -30, -10, -5, -5, -10, -30, -40,
                    -50, -40, -30, -20, -20, -30, -40, -50
    ));

    public static final List<Integer> pieceSquareTableBishop = Collections.unmodifiableList(Arrays.asList(
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 5, 2, 2, 2, 2, 5, 0,
                    0, 3, 5, 5, 5, 5, 3, 0,
                    0, 2, 5, 5, 5, 5, 2, 0,
                    0, 2, 5, 5, 5, 5, 2, 0,
                    0, 2, 5, 5, 5, 5, 2, 0,
                    0, 5, 2, 2, 2, 2, 5, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
    ));

    public static final List<Integer>  pieceSquareTableRook = Collections.unmodifiableList(Arrays.asList(
                    0, 3, 5, 5, 5, 5, 3, 0,
                    -3, 2, 5, 5, 5, 5, 2, -3,
                    -2, 0, 0, 2, 2, 0, 0, -2,
                    -2, 0, 0, 0, 0, 0, 0, -2,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    15, 20, 20, 20, 20, 20, 20, 15,
                    0, 3, 5, 5, 5, 5, 3, 0
    ));

    public static final List<Integer>  pieceSquareTableQueen = Collections.unmodifiableList(Arrays.asList(
                    -10, -5, 0, 0, 0, 0, -5, -10,
                    -5, 0, 5, 5, 5, 5, 0, -5,
                    0, 5, 5, 6, 6, 5, 5, 0,
                    0, 5, 6, 6, 6, 6, 5, 0,
                    0, 5, 6, 6, 6, 6, 5, 0,
                    0, 5, 5, 6, 6, 5, 5, 0,
                    -5, 0, 5, 5, 5, 5, 0, -5,
                    -10, -5, 0, 0, 0, 0, -5, -10
    ));

    public static final List<Integer> pieceSquareTableKing = Collections.unmodifiableList(Arrays.asList(
                    24, 24, 9, 0, 0, 9, 24, 24,
                    16, 14, 7, -3, -3, 7, 14, 16,
                    4, -2, -5, -15, -15, -5, -2, 4,
                    -10, -15, -20, -25, -25, -20, -15, -10,
                    -15, -30, -35, -40, -40, -35, -30, -15,
                    -25, -35, -40, -45, -45, -40, -35, -25,
                    -22, -35, -40, -40, -40, -40, -35, -22,
                    -22, -35, -40, -40, -40, -40, -35, -22
    ));

    public static final List<Integer> pieceSquareTableKingEndGame = Collections.unmodifiableList(Arrays.asList(
                    0, 8, 16, 24, 24, 16, 8, 0,
                    8, 16, 24, 32, 32, 24, 16, 8,
                    16, 24, 32, 40, 40, 32, 24, 16,
                    24, 32, 40, 48, 48, 40, 32, 24,
                    24, 32, 40, 48, 48, 40, 32, 24,
                    16, 24, 32, 40, 40, 32, 24, 16,
                    8, 16, 24, 32, 32, 24, 16, 8,
                    0, 8, 16, 24, 24, 16, 8, 0
    ));

    public static final List<Integer> bitFlippedHorizontalAxis = Collections.unmodifiableList(Arrays.asList(
            56, 57, 58, 59, 60, 61, 62, 63, 48, 49, 50, 51, 52, 53, 54, 55, 40, 41, 42, 43, 44, 45, 46, 47, 32, 33, 34, 35, 36, 37, 38, 39, 24, 25, 26, 27, 28, 29, 30, 31, 16, 17, 18, 19, 20, 21, 22, 23, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7
    ));

    public static final List<Integer> bitFlippedVerticalAxis = Collections.unmodifiableList(Arrays.asList(
            7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8, 23, 22, 21, 20, 19, 18, 17, 16, 31, 30, 29, 28, 27, 26, 25, 24, 39, 38, 37, 36, 35, 34, 33, 32, 47, 46, 45, 44, 43, 42, 41, 40, 55, 54, 53, 52, 51, 50, 49, 48, 63, 62, 61, 60, 59, 58, 57, 56
    ));

    public static final List<Integer> xIncrements = Collections.unmodifiableList(Arrays.asList(-1, -1, 0, 1, 1, 1, 0, -1));
    public static final List<Integer> yIncrements = Collections.unmodifiableList(Arrays.asList(0, -1, -1, -1, 0, 1, 1, 1));
    public static final List<Integer> bitRefIncrements = Collections.unmodifiableList(Arrays.asList(-1, -9, -8, -7, 1, 9, 8, 7));

    private Bitboards() {
    }

    /*
     * Piece square tables are from White's point of view.  As h1=0 and a8=63, you'll need to flip these
     * left to right and top to bottom to see them as if they were mapped to a board with
     * White at the bottom.
     */
    public static final List<List<Integer>> pieceSquareTableMiddleGame = Collections.unmodifiableList(Arrays.asList(
            pieceSquareTablePawn, pieceSquareTableKnight, pieceSquareTableBishop,
            pieceSquareTableKing, pieceSquareTableQueen,  pieceSquareTableRook,
            flipPieceSquareValues(pieceSquareTablePawn), flipPieceSquareValues(pieceSquareTableKnight), flipPieceSquareValues(pieceSquareTableBishop),
            flipPieceSquareValues(pieceSquareTableKing), flipPieceSquareValues(pieceSquareTableQueen),  flipPieceSquareValues(pieceSquareTableRook)
    ));

    public static final List<List<Integer>> pieceSquareTableEndGame = Collections.unmodifiableList(Arrays.asList(
            pieceSquareTablePawnEndGame, pieceSquareTableKnightEndGame, pieceSquareTableBishop,
            pieceSquareTableKingEndGame, pieceSquareTableQueen,  pieceSquareTableRook,
            flipPieceSquareValues(pieceSquareTablePawnEndGame), flipPieceSquareValues(pieceSquareTableKnightEndGame), flipPieceSquareValues(pieceSquareTableBishop),
            flipPieceSquareValues(pieceSquareTableKingEndGame), flipPieceSquareValues(pieceSquareTableQueen),  flipPieceSquareValues(pieceSquareTableRook)
    ));

    public static final List<List<List<Integer>>> pieceSquareTable = Collections.unmodifiableList(Arrays.asList(
            pieceSquareTableMiddleGame,
            pieceSquareTableEndGame
    ));

    private static List<Integer> flipPieceSquareValues(List<Integer> list) {
        List<Integer> newList = new ArrayList<>();
        for (int j = 0; j < 64; j++) {
            newList.add(list.get(Bitboards.bitFlippedHorizontalAxis.get(j)));
        }
        return Collections.unmodifiableList(newList);
    }

    public static List<Integer> getSetBits(long bitboard) {
        List<Integer> setBits = new ArrayList<>();
        while (bitboard != 0) {
            final int trailingZeroes = Long.numberOfTrailingZeros(bitboard);
            setBits.add(trailingZeroes);
            bitboard ^= (1L << trailingZeroes);
        }

        return setBits;
    }

    public static long southFill(long bitboard) {
        bitboard |= (bitboard >>> 8);
        bitboard |= (bitboard >>> 16);
        bitboard |= (bitboard >>> 32);
        return bitboard;
    }

    public static long northFill(long bitboard) {
        bitboard |= (bitboard << 8);
        bitboard |= (bitboard << 16);
        bitboard |= (bitboard << 32);
        return bitboard;
    }

    public static long getPawnFiles(long pawns) {
        return southFill(pawns) & RANK_1;
    }

    public static long getBlackPawnAttacks(long blackPawns) {
        return ((blackPawns & ~FILE_A) >>> 7) | ((blackPawns & ~FILE_H) >>> 9);
    }

    public static long getWhitePawnAttacks(long whitePawns) {
        return ((whitePawns & ~FILE_A) << 9) | ((whitePawns & ~FILE_H) << 7);
    }

    public static long getWhitePassedPawns(long whitePawns, long blackPawns) {
        return whitePawns &
                ~Bitboards.southFill(blackPawns | getBlackPawnAttacks(blackPawns) | (whitePawns >>> 8));
    }

    public static long getBlackPassedPawns(long whitePawns, long blackPawns) {
        return blackPawns &
                ~Bitboards.northFill(whitePawns |  getWhitePawnAttacks (whitePawns) | (blackPawns << 8));
    }

    public static List<Long> getPawnMovesCaptureOfColour(Colour colour) {
        return colour == Colour.WHITE
                ? whitePawnMovesCapture
                : blackPawnMovesCapture;
    }
}
