package com.netsensia.rivalchess.config;

import com.netsensia.rivalchess.enums.Piece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Evaluation {

    TOTAL_PIECE_VALUE_PER_SIDE_AT_START ((Piece.KNIGHT.getValue() * 2) + (Piece.BISHOP.getValue() * 2) + (Piece.ROOK.getValue() * 2) + (Piece.QUEEN.getValue())),
    OPENING_PHASE_MATERIAL ((int)(TOTAL_PIECE_VALUE_PER_SIDE_AT_START.getValue() * 0.8)),
    TRADE_BONUS_UPPER_PAWNS (600),
    WRONG_COLOUR_BISHOP_PENALTY_DIVISOR (2),
    WRONG_COLOUR_BISHOP_MATERIAL_LOW (Piece.BISHOP.getValue() * 2),
    WRONG_COLOUR_BISHOP_MATERIAL_HIGH (Piece.QUEEN.getValue() * 2 + Piece.ROOK.getValue() * 2 + Piece.BISHOP.getValue() * 2),
    KNIGHT_STAGE_MATERIAL_LOW (Piece.KNIGHT.getValue() + (8 * Piece.PAWN.getValue())),
    KNIGHT_STAGE_MATERIAL_HIGH (Piece.QUEEN.getValue() + (2 * Piece.ROOK.getValue()) + (2 * Piece.BISHOP.getValue()) + (6 * Piece.PAWN.getValue())),
    PAWN_STAGE_MATERIAL_LOW (Piece.ROOK.getValue()),
    PAWN_STAGE_MATERIAL_HIGH (Piece.QUEEN.getValue() + (2 * Piece.ROOK.getValue()) + (2 * Piece.BISHOP.getValue())),
    CASTLE_BONUS_LOW_MATERIAL (Piece.ROOK.getValue()),
    CASTLE_BONUS_HIGH_MATERIAL (Piece.QUEEN.getValue() + (Piece.ROOK.getValue() * 2) + (Piece.BISHOP.getValue() * 2)),
    VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS (3),
    VALUE_TRAPPED_BISHOP_PENALTY ((int)(Piece.PAWN.getValue() * 1.5)),
    VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY (Piece.PAWN.getValue()),
    VALUE_BISHOP_PAIR (20),
    VALUE_MATE (10000),
    MATE_SCORE_START (9000), // Allows for a mate in 500, probably enough :)
    VALUE_SHOULD_WIN (300),
    VALUE_ROOK_ON_OPEN_FILE (25),
    VALUE_ROOK_ON_HALF_OPEN_FILE (12),
    VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING (20),
    VALUE_ROOKS_ON_SAME_FILE (8),
    VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY (2),
    VALUE_SIDE_PAWN_PENALTY (10),
    VALUE_DOUBLED_PAWN_PENALTY (25),
    VALUE_PAWN_ISLAND_PENALTY (15),
    /*
     *  gets scaled up on rank and scaled down on material (0.9 - material ratio, < 0.1 = 0.1)
     *  7th rank + 0 pieces = 6*50 * 0.9 = 270
     *  5th rank, half material remaining = 4*50 * 0.4 = 80
     *  3rd rank, all material remaining = 2*60 * 0.1 = 12
     */
    VALUE_ISOLATED_PAWN_PENALTY (15),
    VALUE_BACKWARD_PAWN_PENALTY (15),
    VALUE_GUARDED_PASSED_PAWN (15),
    VALUE_KING_CANNOT_CATCH_PAWN (500),
    PAWN_ADJUST_MAX_MATERIAL (Piece.QUEEN.getValue() + Piece.ROOK.getValue()), // passed pawn bonus starts increasing once enemy material falls below this
    VALUE_ISOLATED_DPAWN_PENALTY (30),
    /*
     * King Safety
     */
    KINGSAFTEY_HALFOPEN_MIDFILE (25),
    KINGSAFTEY_HALFOPEN_NONMIDFILE (10),
    KINGSAFTEY_UNIT (16),
    KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT (Evaluation.KINGSAFTEY_UNIT.getValue() * 3),
    KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT (Evaluation.KINGSAFTEY_UNIT.getValue() * 2),
    KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT (Evaluation.KINGSAFTEY_UNIT.getValue() * 2),
    KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT (Evaluation.KINGSAFTEY_UNIT.getValue()),
    KINGSAFTEY_MAXIMUM_SHIELD_BONUS (Evaluation.KINGSAFTEY_UNIT.getValue() * 8),
    KINGSAFETY_SHIELD_BASE (-(Evaluation.KINGSAFTEY_UNIT.getValue() * 9)),
    KINGSAFETY_UNCASTLED_TRAPPED_ROOK (Evaluation.KINGSAFTEY_UNIT.getValue() * 6),
    KINGSAFETY_ATTACK_MULTIPLIER (4),
    KINGSAFETY_MIN_PIECE_BALANCE (Piece.ROOK.getValue() + Piece.BISHOP.getValue()),
    KINGSAFETY_MAX_PIECE_BALANCE (Evaluation.TOTAL_PIECE_VALUE_PER_SIDE_AT_START.getValue()),
    THREAT_SCORE_DIVISOR (64),
    EVAL_ENDGAME_TOTAL_PIECES (Piece.ROOK.getValue() * 6),
    ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR (5),
    ENDGAME_DRAW_DIVISOR (30),
    ENDGAME_PROBABLE_DRAW_DIVISOR (6),
    ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE (10),
    ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE (50),
    DRAW_CONTEMPT (0)
    ;

    private int value;

    private Evaluation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static int[] VALUE_BISHOP_MOBILITY = {-15,-10,-6,-2,2,6,10,13,16,18,20,22,23,24};
    private static final int[] VALUE_ROOK_MOBILITY = {-10,-7,-4,-1,2,5,7,9,11,12,13,14,14,14,14};
    private static final int[] VALUE_QUEEN_MOBILITY = {-5,-4,-3,-2,-1,0,1,2,3,4,5,6,7,8,9,9,10,10,10,10,10,10,10,10,10,10,10,10};
    private static final int[] VALUE_PASSED_PAWN_BONUS = {-1,24,26,30,36,44,56,-1};
    public static final double ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER = 0.9;

    public static int getBishopMobilityValue(int i) {
        return VALUE_BISHOP_MOBILITY[i];
    }

    public static int getRookMobilityValue(int i) {
        return VALUE_ROOK_MOBILITY[i];
    }

    public static int getQueenMobilityValue(int i) {
        return VALUE_QUEEN_MOBILITY[i];
    }

    public static int getPassedPawnBonus(int i) {
        return VALUE_PASSED_PAWN_BONUS[i];
    }

    public static double getEndgameSubtractInsufficientMaterialMultiplier() {
        return ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER;
    }

    public static List<Integer> getPieceValues() {
        return Collections.unmodifiableList(Arrays.asList(
                Piece.PAWN.getValue(), Piece.KNIGHT.getValue(), Piece.BISHOP.getValue(), Piece.QUEEN.getValue(), Piece.KING.getValue(), Piece.ROOK.getValue(),
                Piece.PAWN.getValue(), Piece.KNIGHT.getValue(), Piece.BISHOP.getValue(), Piece.QUEEN.getValue(), Piece.KING.getValue(), Piece.ROOK.getValue()
        ));
    }
}
