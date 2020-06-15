package com.netsensia.rivalchess.engine.core.eval;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000:\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u000f\u001a\u000e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0001\u001aE\u0010\u0003\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0004\u0012\u00020\u00060\u00042\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00062\u0018\u0010\n\u001a\u0014\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00060\u000bH\u0086\b\u001a\u0016\u0010\f\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u0001\u001a)\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012\u00a2\u0006\u0002\u0010\u0014\u001a\u0016\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010\u001a\u000e\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u0006\u001a\u000e\u0010\u0018\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0010\u001a \u0010\u0019\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0004\u0012\u00020\u00060\u00042\u0006\u0010\t\u001a\u00020\u0006\u001a\u0016\u0010\u001a\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u0001\u001a\u0016\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u0001\u001a)\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012\u00a2\u0006\u0002\u0010\u0014\u001a)\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012\u00a2\u0006\u0002\u0010\u0014\u001a\u0016\u0010\u001e\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010\u001a\u000e\u0010\u001f\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u0006\u001a\u000e\u0010!\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0010\u00a8\u0006\""}, d2 = {"adjustedAttackScore", "", "attackScore", "attackList", "Lkotlin/Pair;", "", "", "bitboards", "Lcom/netsensia/rivalchess/engine/core/eval/BitboardData;", "squaresBitboard", "fn", "Lkotlin/Function2;", "bishopAttacks", "sq", "blackAttackScore", "attacks", "Lcom/netsensia/rivalchess/engine/core/eval/Attacks;", "squareOccupants", "", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "(Lcom/netsensia/rivalchess/engine/core/eval/BitboardData;Lcom/netsensia/rivalchess/engine/core/eval/Attacks;[Lcom/netsensia/rivalchess/model/SquareOccupant;)I", "blackAttacksBitboard", "blackPawnAttacks", "blackPawns", "blackPieceAttacks", "knightAttackList", "queenAttacks", "rookAttacks", "threatEval", "whiteAttackScore", "whiteAttacksBitboard", "whitePawnAttacks", "whitePawns", "whitePieceAttacks", "rivalchess-engine"})
public final class AttacksKt {
    
    public static final long whitePawnAttacks(long whitePawns) {
        return 0L;
    }
    
    public static final long blackPawnAttacks(long blackPawns) {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> attackList(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long squaresBitboard, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.netsensia.rivalchess.engine.core.eval.BitboardData, ? super java.lang.Integer, java.lang.Long> fn) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final kotlin.Pair<java.util.List<java.lang.Long>, java.lang.Long> knightAttackList(long squaresBitboard) {
        return null;
    }
    
    public static final int whiteAttackScore(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.SquareOccupant[] squareOccupants) {
        return 0;
    }
    
    public static final int blackAttackScore(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.SquareOccupant[] squareOccupants) {
        return 0;
    }
    
    public static final long whitePieceAttacks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks) {
        return 0L;
    }
    
    public static final long blackPieceAttacks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks) {
        return 0L;
    }
    
    public static final long whiteAttacksBitboard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks) {
        return 0L;
    }
    
    public static final long blackAttacksBitboard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks) {
        return 0L;
    }
    
    public static final int threatEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.SquareOccupant[] squareOccupants) {
        return 0;
    }
    
    public static final int adjustedAttackScore(int attackScore) {
        return 0;
    }
    
    public static final long rookAttacks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int sq) {
        return 0L;
    }
    
    public static final long bishopAttacks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int sq) {
        return 0L;
    }
    
    public static final long queenAttacks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int sq) {
        return 0L;
    }
}