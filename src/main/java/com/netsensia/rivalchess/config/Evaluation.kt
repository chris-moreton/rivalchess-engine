package com.netsensia.rivalchess.config

import com.netsensia.rivalchess.engine.core.eval.pieceValue
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant
import java.util.*

enum class Evaluation(val value: Int) {
    TOTAL_PIECE_VALUE_PER_SIDE_AT_START(pieceValue(Piece.KNIGHT) * 2 + pieceValue(Piece.BISHOP) * 2 + pieceValue(Piece.ROOK) * 2 + pieceValue(Piece.QUEEN)),
    OPENING_PHASE_MATERIAL((TOTAL_PIECE_VALUE_PER_SIDE_AT_START.value * 0.8).toInt()),
    PAWN_TRADE_BONUS_MAX(600),
    WRONG_COLOUR_BISHOP_PENALTY_DIVISOR(2),
    WRONG_COLOUR_BISHOP_MATERIAL_LOW(pieceValue(Piece.BISHOP) * 2),
    WRONG_COLOUR_BISHOP_MATERIAL_HIGH(pieceValue(Piece.QUEEN) * 2 + pieceValue(Piece.ROOK) * 2 + pieceValue(Piece.BISHOP) * 2),
    KNIGHT_STAGE_MATERIAL_LOW(pieceValue(Piece.KNIGHT) + 8 * pieceValue(Piece.PAWN)),
    KNIGHT_STAGE_MATERIAL_HIGH(pieceValue(Piece.QUEEN) + 2 * pieceValue(Piece.ROOK) + 2 * pieceValue(Piece.BISHOP) + 6 * pieceValue(Piece.PAWN)),
    PAWN_STAGE_MATERIAL_LOW(pieceValue(Piece.ROOK)),
    PAWN_STAGE_MATERIAL_HIGH(pieceValue(Piece.QUEEN) + 2 * pieceValue(Piece.ROOK) + 2 * pieceValue(Piece.BISHOP)),
    CASTLE_BONUS_LOW_MATERIAL(pieceValue(Piece.ROOK)),
    CASTLE_BONUS_HIGH_MATERIAL(pieceValue(Piece.QUEEN) + pieceValue(Piece.ROOK) * 2 + pieceValue(Piece.BISHOP) * 2),
    VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS(3),
    VALUE_TRAPPED_BISHOP_PENALTY((pieceValue(Piece.PAWN) * 1.5).toInt()),
    VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY(pieceValue(Piece.PAWN)),
    VALUE_BISHOP_PAIR(20),
    VALUE_MATE(10000),
    MATE_SCORE_START(9000),  // Allows for a mate in 500, probably enough :)
    VALUE_SHOULD_WIN(300),
    VALUE_ROOK_ON_OPEN_FILE(25),
    VALUE_ROOK_ON_HALF_OPEN_FILE(12),
    VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING(20),
    VALUE_ROOKS_ON_SAME_FILE(8),
    KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY(2),
    VALUE_SIDE_PAWN_PENALTY(10),
    VALUE_DOUBLED_PAWN_PENALTY(25),
    VALUE_PAWN_ISLAND_PENALTY(15),  /*
     *  gets scaled up on rank and scaled down on material (0.9 - material ratio, < 0.1 = 0.1)
     *  7th rank + 0 pieces = 6*50 * 0.9 = 270
     *  5th rank, half material remaining = 4*50 * 0.4 = 80
     *  3rd rank, all material remaining = 2*60 * 0.1 = 12
     */
    VALUE_ISOLATED_PAWN_PENALTY(15),
    VALUE_BACKWARD_PAWN_PENALTY(15),
    VALUE_GUARDED_PASSED_PAWN(15),
    VALUE_KING_CANNOT_CATCH_PAWN(500),
    PAWN_ADJUST_MAX_MATERIAL(pieceValue(Piece.QUEEN) + pieceValue(Piece.ROOK)),  // passed pawn bonus starts increasing once enemy material falls below this
    VALUE_ISOLATED_DPAWN_PENALTY(30),  /*
     * King Safety
     */
    KINGSAFTEY_HALFOPEN_MIDFILE(25),
    KINGSAFTEY_HALFOPEN_NONMIDFILE(10),
    KINGSAFTEY_UNIT(16),
    KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT(KINGSAFTEY_UNIT.value * 3),
    KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT(KINGSAFTEY_UNIT.value * 2),
    KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT(KINGSAFTEY_UNIT.value * 2),
    KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT(KINGSAFTEY_UNIT.value),
    KINGSAFTEY_MAXIMUM_SHIELD_BONUS(KINGSAFTEY_UNIT.value * 8),
    KINGSAFETY_SHIELD_BASE(-(KINGSAFTEY_UNIT.value * 9)),
    KINGSAFETY_UNCASTLED_TRAPPED_ROOK(KINGSAFTEY_UNIT.value * 6),
    KINGSAFETY_ATTACK_MULTIPLIER(4),
    KINGSAFETY_MIN_PIECE_BALANCE(pieceValue(Piece.ROOK) + pieceValue(Piece.BISHOP)),
    KINGSAFETY_MAX_PIECE_BALANCE(TOTAL_PIECE_VALUE_PER_SIDE_AT_START.value),
    THREAT_SCORE_DIVISOR(64),
    EVAL_ENDGAME_TOTAL_PIECES(pieceValue(Piece.ROOK) * 6),
    ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR(5),
    ENDGAME_DRAW_DIVISOR(30),
    ENDGAME_PROBABLE_DRAW_DIVISOR(6),
    ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE(10),
    DRAW_CONTEMPT(0);

    companion object {
        private val VALUE_BISHOP_MOBILITY = intArrayOf(-15, -10, -6, -2, 2, 6, 10, 13, 16, 18, 20, 22, 23, 24)
        private val VALUE_ROOK_MOBILITY = intArrayOf(-10, -7, -4, -1, 2, 5, 7, 9, 11, 12, 13, 14, 14, 14, 14)
        private val VALUE_QUEEN_MOBILITY = intArrayOf(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10)
        private val VALUE_PASSED_PAWN_BONUS = intArrayOf(-1, 24, 26, 30, 36, 44, 56, -1)
        const val endgameSubtractInsufficientMaterialMultiplier = 0.9
        fun getBishopMobilityValue(i: Int): Int {
            return VALUE_BISHOP_MOBILITY[i]
        }

        fun getRookMobilityValue(i: Int): Int {
            return VALUE_ROOK_MOBILITY[i]
        }

        fun getQueenMobilityValue(i: Int): Int {
            return VALUE_QUEEN_MOBILITY[i]
        }

        fun getPassedPawnBonus(i: Int): Int {
            return VALUE_PASSED_PAWN_BONUS[i]
        }

        val pieceValues: List<Int>
            get() = Collections.unmodifiableList(Arrays.asList(
                    pieceValue(Piece.PAWN), pieceValue(Piece.KNIGHT), pieceValue(Piece.BISHOP), pieceValue(Piece.QUEEN), pieceValue(Piece.KING), pieceValue(Piece.ROOK),
                    pieceValue(Piece.PAWN), pieceValue(Piece.KNIGHT), pieceValue(Piece.BISHOP), pieceValue(Piece.QUEEN), pieceValue(Piece.KING), pieceValue(Piece.ROOK)
            ))

        fun getPieceValue(squareOccupant: SquareOccupant): Int {
            return pieceValues[squareOccupant.index]
        }
    }

}