package com.netsensia.rivalchess.config;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b:\b\u0086\u0001\u0018\u0000 <2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001<B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011j\u0002\b\u0012j\u0002\b\u0013j\u0002\b\u0014j\u0002\b\u0015j\u0002\b\u0016j\u0002\b\u0017j\u0002\b\u0018j\u0002\b\u0019j\u0002\b\u001aj\u0002\b\u001bj\u0002\b\u001cj\u0002\b\u001dj\u0002\b\u001ej\u0002\b\u001fj\u0002\b j\u0002\b!j\u0002\b\"j\u0002\b#j\u0002\b$j\u0002\b%j\u0002\b&j\u0002\b\'j\u0002\b(j\u0002\b)j\u0002\b*j\u0002\b+j\u0002\b,j\u0002\b-j\u0002\b.j\u0002\b/j\u0002\b0j\u0002\b1j\u0002\b2j\u0002\b3j\u0002\b4j\u0002\b5j\u0002\b6j\u0002\b7j\u0002\b8j\u0002\b9j\u0002\b:j\u0002\b;\u00a8\u0006="}, d2 = {"Lcom/netsensia/rivalchess/config/Evaluation;", "", "value", "", "(Ljava/lang/String;II)V", "getValue", "()I", "TOTAL_PIECE_VALUE_PER_SIDE_AT_START", "OPENING_PHASE_MATERIAL", "PAWN_TRADE_BONUS_MAX", "WRONG_COLOUR_BISHOP_PENALTY_DIVISOR", "WRONG_COLOUR_BISHOP_MATERIAL_LOW", "WRONG_COLOUR_BISHOP_MATERIAL_HIGH", "KNIGHT_STAGE_MATERIAL_LOW", "KNIGHT_STAGE_MATERIAL_HIGH", "PAWN_STAGE_MATERIAL_LOW", "PAWN_STAGE_MATERIAL_HIGH", "CASTLE_BONUS_LOW_MATERIAL", "CASTLE_BONUS_HIGH_MATERIAL", "VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS", "VALUE_TRAPPED_BISHOP_PENALTY", "VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY", "VALUE_BISHOP_PAIR", "VALUE_MATE", "MATE_SCORE_START", "VALUE_SHOULD_WIN", "VALUE_ROOK_ON_OPEN_FILE", "VALUE_ROOK_ON_HALF_OPEN_FILE", "VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING", "VALUE_ROOKS_ON_SAME_FILE", "KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY", "VALUE_SIDE_PAWN_PENALTY", "VALUE_DOUBLED_PAWN_PENALTY", "VALUE_PAWN_ISLAND_PENALTY", "VALUE_ISOLATED_PAWN_PENALTY", "VALUE_BACKWARD_PAWN_PENALTY", "VALUE_GUARDED_PASSED_PAWN", "VALUE_KING_CANNOT_CATCH_PAWN", "PAWN_ADJUST_MAX_MATERIAL", "VALUE_ISOLATED_DPAWN_PENALTY", "KINGSAFTEY_HALFOPEN_MIDFILE", "KINGSAFTEY_HALFOPEN_NONMIDFILE", "KINGSAFTEY_UNIT", "KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT", "KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT", "KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT", "KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT", "KINGSAFTEY_MAXIMUM_SHIELD_BONUS", "KINGSAFETY_SHIELD_BASE", "KINGSAFETY_UNCASTLED_TRAPPED_ROOK", "KINGSAFETY_ATTACK_MULTIPLIER", "KINGSAFETY_MIN_PIECE_BALANCE", "KINGSAFETY_MAX_PIECE_BALANCE", "THREAT_SCORE_DIVISOR", "EVAL_ENDGAME_TOTAL_PIECES", "ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR", "ENDGAME_DRAW_DIVISOR", "ENDGAME_PROBABLE_DRAW_DIVISOR", "ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE", "DRAW_CONTEMPT", "Companion", "rivalchess-engine"})
public enum Evaluation {
    /*public static final*/ TOTAL_PIECE_VALUE_PER_SIDE_AT_START /* = new TOTAL_PIECE_VALUE_PER_SIDE_AT_START(0) */,
    /*public static final*/ OPENING_PHASE_MATERIAL /* = new OPENING_PHASE_MATERIAL(0) */,
    /*public static final*/ PAWN_TRADE_BONUS_MAX /* = new PAWN_TRADE_BONUS_MAX(0) */,
    /*public static final*/ WRONG_COLOUR_BISHOP_PENALTY_DIVISOR /* = new WRONG_COLOUR_BISHOP_PENALTY_DIVISOR(0) */,
    /*public static final*/ WRONG_COLOUR_BISHOP_MATERIAL_LOW /* = new WRONG_COLOUR_BISHOP_MATERIAL_LOW(0) */,
    /*public static final*/ WRONG_COLOUR_BISHOP_MATERIAL_HIGH /* = new WRONG_COLOUR_BISHOP_MATERIAL_HIGH(0) */,
    /*public static final*/ KNIGHT_STAGE_MATERIAL_LOW /* = new KNIGHT_STAGE_MATERIAL_LOW(0) */,
    /*public static final*/ KNIGHT_STAGE_MATERIAL_HIGH /* = new KNIGHT_STAGE_MATERIAL_HIGH(0) */,
    /*public static final*/ PAWN_STAGE_MATERIAL_LOW /* = new PAWN_STAGE_MATERIAL_LOW(0) */,
    /*public static final*/ PAWN_STAGE_MATERIAL_HIGH /* = new PAWN_STAGE_MATERIAL_HIGH(0) */,
    /*public static final*/ CASTLE_BONUS_LOW_MATERIAL /* = new CASTLE_BONUS_LOW_MATERIAL(0) */,
    /*public static final*/ CASTLE_BONUS_HIGH_MATERIAL /* = new CASTLE_BONUS_HIGH_MATERIAL(0) */,
    /*public static final*/ VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS /* = new VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS(0) */,
    /*public static final*/ VALUE_TRAPPED_BISHOP_PENALTY /* = new VALUE_TRAPPED_BISHOP_PENALTY(0) */,
    /*public static final*/ VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY /* = new VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY(0) */,
    /*public static final*/ VALUE_BISHOP_PAIR /* = new VALUE_BISHOP_PAIR(0) */,
    /*public static final*/ VALUE_MATE /* = new VALUE_MATE(0) */,
    /*public static final*/ MATE_SCORE_START /* = new MATE_SCORE_START(0) */,
    /*public static final*/ VALUE_SHOULD_WIN /* = new VALUE_SHOULD_WIN(0) */,
    /*public static final*/ VALUE_ROOK_ON_OPEN_FILE /* = new VALUE_ROOK_ON_OPEN_FILE(0) */,
    /*public static final*/ VALUE_ROOK_ON_HALF_OPEN_FILE /* = new VALUE_ROOK_ON_HALF_OPEN_FILE(0) */,
    /*public static final*/ VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING /* = new VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING(0) */,
    /*public static final*/ VALUE_ROOKS_ON_SAME_FILE /* = new VALUE_ROOKS_ON_SAME_FILE(0) */,
    /*public static final*/ KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY /* = new KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY(0) */,
    /*public static final*/ VALUE_SIDE_PAWN_PENALTY /* = new VALUE_SIDE_PAWN_PENALTY(0) */,
    /*public static final*/ VALUE_DOUBLED_PAWN_PENALTY /* = new VALUE_DOUBLED_PAWN_PENALTY(0) */,
    /*public static final*/ VALUE_PAWN_ISLAND_PENALTY /* = new VALUE_PAWN_ISLAND_PENALTY(0) */,
    /*public static final*/ VALUE_ISOLATED_PAWN_PENALTY /* = new VALUE_ISOLATED_PAWN_PENALTY(0) */,
    /*public static final*/ VALUE_BACKWARD_PAWN_PENALTY /* = new VALUE_BACKWARD_PAWN_PENALTY(0) */,
    /*public static final*/ VALUE_GUARDED_PASSED_PAWN /* = new VALUE_GUARDED_PASSED_PAWN(0) */,
    /*public static final*/ VALUE_KING_CANNOT_CATCH_PAWN /* = new VALUE_KING_CANNOT_CATCH_PAWN(0) */,
    /*public static final*/ PAWN_ADJUST_MAX_MATERIAL /* = new PAWN_ADJUST_MAX_MATERIAL(0) */,
    /*public static final*/ VALUE_ISOLATED_DPAWN_PENALTY /* = new VALUE_ISOLATED_DPAWN_PENALTY(0) */,
    /*public static final*/ KINGSAFTEY_HALFOPEN_MIDFILE /* = new KINGSAFTEY_HALFOPEN_MIDFILE(0) */,
    /*public static final*/ KINGSAFTEY_HALFOPEN_NONMIDFILE /* = new KINGSAFTEY_HALFOPEN_NONMIDFILE(0) */,
    /*public static final*/ KINGSAFTEY_UNIT /* = new KINGSAFTEY_UNIT(0) */,
    /*public static final*/ KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT /* = new KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT(0) */,
    /*public static final*/ KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT /* = new KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT(0) */,
    /*public static final*/ KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT /* = new KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT(0) */,
    /*public static final*/ KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT /* = new KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT(0) */,
    /*public static final*/ KINGSAFTEY_MAXIMUM_SHIELD_BONUS /* = new KINGSAFTEY_MAXIMUM_SHIELD_BONUS(0) */,
    /*public static final*/ KINGSAFETY_SHIELD_BASE /* = new KINGSAFETY_SHIELD_BASE(0) */,
    /*public static final*/ KINGSAFETY_UNCASTLED_TRAPPED_ROOK /* = new KINGSAFETY_UNCASTLED_TRAPPED_ROOK(0) */,
    /*public static final*/ KINGSAFETY_ATTACK_MULTIPLIER /* = new KINGSAFETY_ATTACK_MULTIPLIER(0) */,
    /*public static final*/ KINGSAFETY_MIN_PIECE_BALANCE /* = new KINGSAFETY_MIN_PIECE_BALANCE(0) */,
    /*public static final*/ KINGSAFETY_MAX_PIECE_BALANCE /* = new KINGSAFETY_MAX_PIECE_BALANCE(0) */,
    /*public static final*/ THREAT_SCORE_DIVISOR /* = new THREAT_SCORE_DIVISOR(0) */,
    /*public static final*/ EVAL_ENDGAME_TOTAL_PIECES /* = new EVAL_ENDGAME_TOTAL_PIECES(0) */,
    /*public static final*/ ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR /* = new ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR(0) */,
    /*public static final*/ ENDGAME_DRAW_DIVISOR /* = new ENDGAME_DRAW_DIVISOR(0) */,
    /*public static final*/ ENDGAME_PROBABLE_DRAW_DIVISOR /* = new ENDGAME_PROBABLE_DRAW_DIVISOR(0) */,
    /*public static final*/ ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE /* = new ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE(0) */,
    /*public static final*/ DRAW_CONTEMPT /* = new DRAW_CONTEMPT(0) */;
    private final int value = 0;
    private static final int[] VALUE_BISHOP_MOBILITY = {-15, -10, -6, -2, 2, 6, 10, 13, 16, 18, 20, 22, 23, 24};
    private static final int[] VALUE_ROOK_MOBILITY = {-10, -7, -4, -1, 2, 5, 7, 9, 11, 12, 13, 14, 14, 14, 14};
    private static final int[] VALUE_QUEEN_MOBILITY = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
    private static final int[] VALUE_PASSED_PAWN_BONUS = {-1, 24, 26, 30, 36, 44, 56, -1};
    public static final double endgameSubtractInsufficientMaterialMultiplier = 0.9;
    public static final com.netsensia.rivalchess.config.Evaluation.Companion Companion = null;
    
    public final int getValue() {
        return 0;
    }
    
    Evaluation(int value) {
    }
    
    @kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0015\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\fJ\u000e\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\fJ\u000e\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\fJ\u000e\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b8F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0017"}, d2 = {"Lcom/netsensia/rivalchess/config/Evaluation$Companion;", "", "()V", "VALUE_BISHOP_MOBILITY", "", "VALUE_PASSED_PAWN_BONUS", "VALUE_QUEEN_MOBILITY", "VALUE_ROOK_MOBILITY", "endgameSubtractInsufficientMaterialMultiplier", "", "pieceValues", "", "", "getPieceValues", "()Ljava/util/List;", "getBishopMobilityValue", "i", "getPassedPawnBonus", "getPieceValue", "squareOccupant", "Lcom/netsensia/rivalchess/model/SquareOccupant;", "getQueenMobilityValue", "getRookMobilityValue", "rivalchess-engine"})
    public static final class Companion {
        
        public final int getBishopMobilityValue(int i) {
            return 0;
        }
        
        public final int getRookMobilityValue(int i) {
            return 0;
        }
        
        public final int getQueenMobilityValue(int i) {
            return 0;
        }
        
        public final int getPassedPawnBonus(int i) {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.Integer> getPieceValues() {
            return null;
        }
        
        public final int getPieceValue(@org.jetbrains.annotations.NotNull()
        com.netsensia.rivalchess.model.SquareOccupant squareOccupant) {
            return 0;
        }
        
        private Companion() {
            super();
        }
    }
}