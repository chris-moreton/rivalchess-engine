package com.netsensia.rivalchess.config

const val VALUE_PAWN = 100
const val VALUE_KNIGHT = 390
const val VALUE_BISHOP = 400
const val VALUE_ROOK = 595
const val VALUE_QUEEN = 1175

const val TOTAL_PIECE_VALUE_PER_SIDE_AT_START = VALUE_KNIGHT * 2 + VALUE_BISHOP * 2 + VALUE_ROOK * 2 + VALUE_QUEEN
const val OPENING_PHASE_MATERIAL = (TOTAL_PIECE_VALUE_PER_SIDE_AT_START * 0.8).toInt()
const val PAWN_TRADE_BONUS_MAX = 600
const val WRONG_COLOUR_BISHOP_PENALTY_DIVISOR = 2
const val WRONG_COLOUR_BISHOP_MATERIAL_LOW = VALUE_BISHOP * 2
const val WRONG_COLOUR_BISHOP_MATERIAL_HIGH = VALUE_QUEEN * 2 + VALUE_ROOK * 2 + VALUE_BISHOP * 2
const val KNIGHT_STAGE_MATERIAL_LOW = VALUE_KNIGHT + 8 * VALUE_PAWN
const val KNIGHT_STAGE_MATERIAL_HIGH = VALUE_QUEEN + 2 * VALUE_ROOK + 2 * VALUE_BISHOP + 6 * VALUE_PAWN
const val PAWN_STAGE_MATERIAL_LOW = VALUE_ROOK
const val PAWN_STAGE_MATERIAL_HIGH = VALUE_QUEEN + 2 * VALUE_ROOK + 2 * VALUE_BISHOP
const val VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS = 3
const val VALUE_TRAPPED_BISHOP_PENALTY = (VALUE_PAWN * 1.5).toInt()
const val VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY = VALUE_PAWN
const val VALUE_BISHOP_PAIR = 20
const val VALUE_MATE = 10000
const val MATE_SCORE_START = 9000  // Allows for a mate in 500, probably enough :)
const val VALUE_SHOULD_WIN = 300
const val VALUE_ROOK_ON_OPEN_FILE = 25
const val VALUE_ROOK_ON_HALF_OPEN_FILE = 12
const val VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING = 20
const val VALUE_ROOKS_ON_SAME_FILE = 8
const val KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY = 2
const val VALUE_SIDE_PAWN_PENALTY = 10
const val VALUE_DOUBLED_PAWN_PENALTY = 25
const val VALUE_PAWN_ISLAND_PENALTY = 15
const val VALUE_ISOLATED_PAWN_PENALTY = 15
const val VALUE_BACKWARD_PAWN_PENALTY = 15
const val VALUE_GUARDED_PASSED_PAWN = 15
const val VALUE_KING_CANNOT_CATCH_PAWN = 500
const val PAWN_ADJUST_MAX_MATERIAL = VALUE_QUEEN + VALUE_ROOK  // passed pawn bonus starts increasing once enemy material falls below this
const val VALUE_ISOLATED_DPAWN_PENALTY = 30
const val KINGSAFTEY_HALFOPEN_MIDFILE = 25
const val KINGSAFTEY_HALFOPEN_NONMIDFILE = 10
const val KINGSAFTEY_UNIT = 16
const val KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT = KINGSAFTEY_UNIT * 3
const val KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT = KINGSAFTEY_UNIT * 2
const val KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT = KINGSAFTEY_UNIT * 2
const val KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT = KINGSAFTEY_UNIT
const val KINGSAFTEY_MAXIMUM_SHIELD_BONUS = KINGSAFTEY_UNIT * 8
const val KINGSAFETY_SHIELD_BASE = -(KINGSAFTEY_UNIT * 9)
const val KINGSAFETY_UNCASTLED_TRAPPED_ROOK = KINGSAFTEY_UNIT * 6
const val KINGSAFETY_ATTACK_MULTIPLIER = 4
const val KINGSAFETY_MIN_PIECE_BALANCE = VALUE_ROOK + VALUE_BISHOP
const val KINGSAFETY_MAX_PIECE_BALANCE = TOTAL_PIECE_VALUE_PER_SIDE_AT_START
const val THREAT_SCORE_DIVISOR = 64
const val EVAL_ENDGAME_TOTAL_PIECES = VALUE_ROOK * 6
const val ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR = 5
const val ENDGAME_DRAW_DIVISOR = 30
const val ENDGAME_PROBABLE_DRAW_DIVISOR = 6
const val ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE = 10
const val KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING = 0

val VALUE_BISHOP_MOBILITY = intArrayOf(-15, -10, -6, -2, 2, 6, 10, 13, 16, 18, 20, 22, 23, 24)
val VALUE_ROOK_MOBILITY = intArrayOf(-10, -7, -4, -1, 2, 5, 7, 9, 11, 12, 13, 14, 14, 14, 14)
val VALUE_QUEEN_MOBILITY = intArrayOf(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10)
val VALUE_PASSED_PAWN_BONUS = intArrayOf(-1, 24, 26, 30, 36, 44, 56, -1)

const val endgameSubtractInsufficientMaterialMultiplier = 0.9


