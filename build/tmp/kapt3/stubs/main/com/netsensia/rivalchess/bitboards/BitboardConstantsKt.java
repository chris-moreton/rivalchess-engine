package com.netsensia.rivalchess.bitboards;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0012\n\u0002\u0010!\n\u0000\n\u0002\u0010 \n\u0002\b#\n\u0002\u0010\b\n\u0002\b\u0016\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0002\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0003\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0004\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0005\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0006\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0007\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\b\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\t\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\n\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u000b\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\f\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\r\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u000e\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u000f\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0010\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0011\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u0012\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"5\u0010\u0013\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u000e\u0010\u0019\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u001a\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u001b\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u001c\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u001d\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u001e\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\u001f\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010 \u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010!\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\"\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010#\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010$\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010%\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010&\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010\'\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010(\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010)\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010*\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010+\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010,\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010-\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010.\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u0010/\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00100\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00101\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00102\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00103\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00104\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00105\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00106\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00107\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"\u000e\u00108\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\"5\u00109\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010:0: \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010:0:\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010\u0018\"5\u0010<\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010\u0018\"5\u0010>\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010\u0018\"5\u0010@\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010\u0018\"5\u0010B\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010:0: \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010:0:\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010\u0018\"5\u0010D\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010\u0018\"\u0017\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00010\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010\u0018\"5\u0010H\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u0010\u0018\"5\u0010J\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010\u0018\"5\u0010L\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u0010\u0018\"5\u0010N\u001a&\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001 \u0015*\u0012\u0012\f\u0012\n \u0015*\u0004\u0018\u00010\u00010\u0001\u0018\u00010\u00160\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010\u0018\u00a8\u0006P"}, d2 = {"A1A2B1B2", "", "A1B1", "A2A7H2H7", "A8A7B8B7", "A8B8", "B1C1", "B8C8", "BLACKKINGSIDECASTLEMOVEMASK", "BLACKKINGSIDECASTLEROOKMOVE", "BLACKKINGSIDECASTLESQUARES", "BLACKKINGSIDEROOKMASK", "BLACKQUEENSIDECASTLEMOVEMASK", "BLACKQUEENSIDECASTLEROOKMOVE", "BLACKQUEENSIDECASTLESQUARES", "BLACKQUEENSIDEROOKMASK", "DARK_SQUARES", "F1G1", "F8G8", "FILES", "", "kotlin.jvm.PlatformType", "", "getFILES", "()Ljava/util/List;", "FILE_A", "FILE_B", "FILE_C", "FILE_D", "FILE_E", "FILE_F", "FILE_G", "FILE_H", "G1H1", "G8H8", "H1H2G1G2", "H8H7G8G7", "LIGHT_SQUARES", "LOW32", "MIDDLE_FILES_8_BIT", "NONMID_FILES_8_BIT", "RANK_1", "RANK_2", "RANK_3", "RANK_4", "RANK_5", "RANK_6", "RANK_7", "RANK_8", "WHITEKINGSIDECASTLEMOVEMASK", "WHITEKINGSIDECASTLEROOKMOVE", "WHITEKINGSIDECASTLESQUARES", "WHITEKINGSIDEROOKMASK", "WHITEQUEENSIDECASTLEMOVEMASK", "WHITEQUEENSIDECASTLEROOKMOVE", "WHITEQUEENSIDECASTLESQUARES", "WHITEQUEENSIDEROOKMASK", "bitFlippedHorizontalAxis", "", "getBitFlippedHorizontalAxis", "blackPassedPawnMask", "getBlackPassedPawnMask", "blackPawnMovesCapture", "getBlackPawnMovesCapture", "blackPawnMovesForward", "getBlackPawnMovesForward", "distanceToH1OrA8", "getDistanceToH1OrA8", "kingMoves", "getKingMoves", "knightMoves", "getKnightMoves", "whiteKingShieldMask", "getWhiteKingShieldMask", "whitePassedPawnMask", "getWhitePassedPawnMask", "whitePawnMovesCapture", "getWhitePawnMovesCapture", "whitePawnMovesForward", "getWhitePawnMovesForward", "rivalchess-engine"})
public final class BitboardConstantsKt {
    public static final long RANK_8 = -72057594037927936L;
    public static final long RANK_7 = 71776119061217280L;
    public static final long RANK_6 = 280375465082880L;
    public static final long RANK_5 = 1095216660480L;
    public static final long RANK_4 = 4278190080L;
    public static final long RANK_3 = 16711680L;
    public static final long RANK_2 = 65280L;
    public static final long RANK_1 = 255L;
    public static final long FILE_A = -9187201950435737472L;
    public static final long FILE_B = 4629771061636907072L;
    public static final long FILE_C = 2314885530818453536L;
    public static final long FILE_D = 1157442765409226768L;
    public static final long FILE_E = 578721382704613384L;
    public static final long FILE_F = 289360691352306692L;
    public static final long FILE_G = 144680345676153346L;
    public static final long FILE_H = 72340172838076673L;
    public static final long MIDDLE_FILES_8_BIT = 231L;
    public static final long NONMID_FILES_8_BIT = 24L;
    public static final long F1G1 = 6L;
    public static final long G1H1 = 3L;
    public static final long A1B1 = 192L;
    public static final long B1C1 = 96L;
    public static final long F8G8 = 432345564227567616L;
    public static final long G8H8 = 216172782113783808L;
    public static final long A8B8 = -4611686018427387904L;
    public static final long B8C8 = 6917529027641081856L;
    private static final java.util.List<java.lang.Long> FILES = null;
    public static final long LIGHT_SQUARES = -6172840429334713771L;
    public static final long DARK_SQUARES = 6172840429334713770L;
    public static final long A1A2B1B2 = 49344L;
    public static final long A8A7B8B7 = -4557642822898941952L;
    public static final long H8H7G8G7 = 217017207043915776L;
    public static final long H1H2G1G2 = 771L;
    public static final long A2A7H2H7 = 36310271995707648L;
    public static final long LOW32 = 4294967295L;
    public static final long WHITEKINGSIDECASTLESQUARES = 6L;
    public static final long WHITEQUEENSIDECASTLESQUARES = 112L;
    public static final long BLACKKINGSIDECASTLESQUARES = 432345564227567616L;
    public static final long BLACKQUEENSIDECASTLESQUARES = 8070450532247928832L;
    public static final long WHITEKINGSIDECASTLEMOVEMASK = 10L;
    public static final long WHITEQUEENSIDECASTLEMOVEMASK = 40L;
    public static final long BLACKKINGSIDECASTLEMOVEMASK = 720575940379279360L;
    public static final long BLACKQUEENSIDECASTLEMOVEMASK = 2882303761517117440L;
    public static final long WHITEKINGSIDECASTLEROOKMOVE = 5L;
    public static final long WHITEQUEENSIDECASTLEROOKMOVE = 144L;
    public static final long BLACKKINGSIDECASTLEROOKMOVE = 360287970189639680L;
    public static final long BLACKQUEENSIDECASTLEROOKMOVE = -8070450532247928832L;
    public static final long WHITEKINGSIDEROOKMASK = 1L;
    public static final long WHITEQUEENSIDEROOKMASK = 128L;
    public static final long BLACKKINGSIDEROOKMASK = 72057594037927936L;
    public static final long BLACKQUEENSIDEROOKMASK = -9223372036854775808L;
    private static final java.util.List<java.lang.Integer> distanceToH1OrA8 = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.Long> knightMoves = null;
    private static final java.util.List<java.lang.Long> kingMoves = null;
    private static final java.util.List<java.lang.Long> whitePawnMovesForward = null;
    private static final java.util.List<java.lang.Long> whitePawnMovesCapture = null;
    private static final java.util.List<java.lang.Long> blackPawnMovesForward = null;
    private static final java.util.List<java.lang.Long> blackPawnMovesCapture = null;
    private static final java.util.List<java.lang.Long> whitePassedPawnMask = null;
    private static final java.util.List<java.lang.Long> blackPassedPawnMask = null;
    private static final java.util.List<java.lang.Long> whiteKingShieldMask = null;
    private static final java.util.List<java.lang.Integer> bitFlippedHorizontalAxis = null;
    
    public static final java.util.List<java.lang.Long> getFILES() {
        return null;
    }
    
    public static final java.util.List<java.lang.Integer> getDistanceToH1OrA8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.util.List<java.lang.Long> getKnightMoves() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getKingMoves() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getWhitePawnMovesForward() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getWhitePawnMovesCapture() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getBlackPawnMovesForward() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getBlackPawnMovesCapture() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getWhitePassedPawnMask() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getBlackPassedPawnMask() {
        return null;
    }
    
    public static final java.util.List<java.lang.Long> getWhiteKingShieldMask() {
        return null;
    }
    
    public static final java.util.List<java.lang.Integer> getBitFlippedHorizontalAxis() {
        return null;
    }
}