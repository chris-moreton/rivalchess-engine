package com.netsensia.rivalchess.engine.eval.rival103

object RivalConstants {
    const val VERSION = "v1.0.3"
    const val IS_ANDROID_VERSION = true
    const val BUILDING_ENDGAME_BASES = false
    const val USE_ALTERNATE_EVAL = false
    const val TEST_ALTERNATE_EVAL = false
    const val SHOW_PS_BREAKDOWN = false
    const val INFINITY = 20000000
    const val UCI_DEBUG = false
    const val UCI_DEBUG_FILEPATH = "S:\\Java\\Chess Supp\\Engines\\"
    const val UCI_LOG = false
    const val UCI_TIMER_INTERVAL_MILLIS = 50
    const val UCI_TIMER_SAFTEY_MARGIN_MILLIS = 250
    const val DEBUG_ZUGZWANGS = false
    const val TRACK_PIECE_SQUARE_VALUES = false
    const val MAX_GAME_MOVES = 500
    const val MAX_NODES_TO_SEARCH = Int.MAX_VALUE
    const val MAX_LEGAL_MOVES = 220
    const val MAX_SEARCH_MILLIS = 60 * 60 * 1000 // 1 hour
    const val MIN_SEARCH_MILLIS = 25
    const val MAX_SEARCH_DEPTH = 125
    const val MAX_QUIESCE_DEPTH = 50
    const val MAX_EXTENSION_DEPTH = 60
    const val MAX_TREE_DEPTH = MAX_SEARCH_DEPTH + MAX_QUIESCE_DEPTH + MAX_EXTENSION_DEPTH + 1

    /***********************************
     * Profiler 					   *
     */
    const val PROFILING = false
    const val PROFILE_SLOTS = 100
    const val PROFILE_STARTTIME = 0
    const val PROFILE_COUNT = 1
    const val PROFILE_TOTALTIME = 2
    const val PROFILE_DATA_COUNT = 3

    /***********************************
     * Evaluation function weightings *
     */
    const val VALUE_PAWN = 100
    const val VALUE_KNIGHT = 390
    const val VALUE_BISHOP = 390
    const val VALUE_ROOK = 595
    const val VALUE_QUEEN = 1175
    const val VALUE_KING = 30000 // this gets used sometimes, for example in static exchange evaluation
    val PIECE_VALUES = intArrayOf(
        VALUE_PAWN, VALUE_KNIGHT, VALUE_BISHOP, VALUE_QUEEN, VALUE_KING, VALUE_ROOK,
        VALUE_PAWN, VALUE_KNIGHT, VALUE_BISHOP, VALUE_QUEEN, VALUE_KING, VALUE_ROOK
    )
    val PIECE_VALUES_MINI = intArrayOf(1, 3, 3, 5, 0, 9)
    const val TOTAL_PIECE_VALUE_PER_SIDE_AT_START = VALUE_KNIGHT * 2 + VALUE_BISHOP * 2 + VALUE_ROOK * 2 + VALUE_QUEEN
    const val TOTAL_PIECE_VALUE_AT_START = TOTAL_PIECE_VALUE_PER_SIDE_AT_START * 2
    const val TRADE_BONUS_PIECE_DIVISOR = 200
    const val TRADE_BONUS_PAWN_DIVISOR = 50
    const val VALUE_QUEENEARLY_PENALTY = 40
    const val OPENING_PHASE_MATERIAL = (TOTAL_PIECE_VALUE_PER_SIDE_AT_START * 0.8).toInt()
    const val TRADE_BONUS_UPPER_PAWNS = 600
    const val WRONG_COLOUR_BISHOP_PENALTY_DIVISOR = 2
    const val WRONG_COLOUR_BISHOP_MATERIAL_LOW = VALUE_BISHOP * 2
    const val WRONG_COLOUR_BISHOP_MATERIAL_HIGH = VALUE_QUEEN * 2 + VALUE_ROOK * 2 + VALUE_BISHOP * 2
    const val KING_STAGE_MATERIAL_LOW = VALUE_ROOK
    const val KING_STAGE_MATERIAL_HIGH = VALUE_QUEEN + 2 * VALUE_ROOK + 2 * VALUE_BISHOP
    const val KNIGHT_STAGE_MATERIAL_LOW = VALUE_KNIGHT + 8 * VALUE_PAWN
    const val KNIGHT_STAGE_MATERIAL_HIGH = VALUE_QUEEN + 2 * VALUE_ROOK + 2 * VALUE_BISHOP + 6 * VALUE_PAWN
    const val PAWN_STAGE_MATERIAL_LOW = VALUE_ROOK
    const val PAWN_STAGE_MATERIAL_HIGH = VALUE_QUEEN + 2 * VALUE_ROOK + 2 * VALUE_BISHOP
    const val KING_TROPISM_LOW_MATERIAL = VALUE_ROOK
    const val KING_TROPISM_HIGH_MATERIAL = VALUE_QUEEN + VALUE_KNIGHT
    const val CASTLE_BONUS_LOW_MATERIAL = VALUE_ROOK
    const val CASTLE_BONUS_HIGH_MATERIAL = VALUE_QUEEN + VALUE_ROOK * 2 + VALUE_BISHOP * 2
    const val VALUE_DIRECT_OPPOSITION = 20
    const val PIECE_SQUARE_IMPORTANCE_DIVISOR = 1
    const val VALUE_OPENING_UNDEVELOPED_MINOR_PENALTY = 25
    val VALUE_BISHOP_MOBILITY = intArrayOf(-15, -10, -6, -2, 2, 6, 10, 13, 16, 18, 20, 22, 23, 24)
    const val VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS = 3
    const val VALUE_TRAPPED_BISHOP_PENALTY = (VALUE_PAWN * 1.5).toInt()
    const val VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY = VALUE_PAWN
    const val VALUE_BISHOP_PAIR = 20
    const val VALUE_MATE = 10000
    const val MATE_SCORE_START = 9000 // Allows for a mate in 500, probably enough :)
    const val VALUE_SHOULD_WIN = 300
    const val VALUE_ROOK_ON_SEVENTH_RANK = 20
    const val VALUE_ROOK_ON_OPEN_FILE = 25
    const val VALUE_ROOK_ON_HALF_OPEN_FILE = 12
    const val VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING = 20
    const val VALUE_ROOKS_ON_SAME_FILE = 8
    const val VALUE_ROOKS_ON_SAME_RANK = 10
    val VALUE_ROOK_MOBILITY = intArrayOf(-10, -7, -4, -1, 2, 5, 7, 9, 11, 12, 13, 14, 14, 14, 14)
    val VALUE_QUEEN_MOBILITY =
        intArrayOf(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10)
    const val VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY = 2
    const val VALUE_QUEEN_TROPISM = 6
    const val VALUE_KNIGHT_TROPISM = 2
    const val VALUE_LONEKING_TROPISM = 10
    const val VALUE_SIDE_PAWN_PENALTY = 10
    const val VALUE_DOUBLED_PAWN_PENALTY = 25
    const val VALUE_PAWN_ISLAND_PENALTY = 15

    /*
	 *  gets scaled up on rank and scaled down on material (0.9 - material ratio, < 0.1 = 0.1)
	 *  7th rank + 0 pieces = 6*50 * 0.9 = 270
	 *  5th rank, half material remaining = 4*50 * 0.4 = 80
	 *  3rd rank, all material remaining = 2*60 * 0.1 = 12
	 */
    val VALUE_PASSED_PAWN_BONUS = intArrayOf(-1, 24, 26, 30, 36, 44, 56, -1)
    const val VALUE_ISOLATED_PAWN_PENALTY = 15
    const val VALUE_BACKWARD_PAWN_PENALTY = 15
    const val VALUE_GUARDED_PASSED_PAWN = 15
    const val VALUE_KING_CANNOT_CATCH_PAWN = 500
    const val PAWN_ADJUST_MAX_MATERIAL =
        VALUE_QUEEN + VALUE_ROOK // passed pawn bonus starts increasing once enemy material falls below this
    const val VALUE_ISOLATED_DPAWN_PENALTY = 30
    const val VALUE_CONNECTED_PASSED_PAWN = 0
    const val KINGSAFETY_RIGHTWAY_DIVISOR = 4
    const val VALUE_KINGKING_TROPISM = 20

    /*
	 * King Safety
	 */
    const val VALUE_KINGSAFETY_PAWNSHIELD_1 = 35
    const val VALUE_KINGSAFETY_PAWNSHIELD_2 = 12
    const val VALUE_KINGSAFETY_MINORPROTECTION = 12
    const val VALUE_KINGVICINITY_ATTACK_PENALTY = 4
    const val VALUE_KING_EXPOSED_AHEAD_PENALTY = 35
    const val VALUE_KING_EXPOSED_SIDE_PENALTY = 20
    const val VALUE_HANGING_KING_SQUARE_PENALTY = 10
    const val VALUE_CASTLE_APPEARANCE = 25
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
    const val VALUE_KING_SAFETY_MULTIPLIER = 0.75
    const val THREAT_SCORE_DIVISOR = 64
    const val USE_KING_REGION_ATTACK_SCORING = false
    const val EVAL_ENDGAME_TOTAL_PIECES = VALUE_ROOK * 6
    const val ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR = 5
    const val ENDGAME_DRAW_DIVISOR = 30
    const val ENDGAME_PROBABLE_DRAW_DIVISOR = 6
    const val ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE = 10
    const val ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER = 0.9
    const val ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE = 50
    const val DRAW_CONTEMPT = 0

    /***********************************
     * Search parameters              *
     */
    const val COUNT_NODES_IN_EVALUATE_ONLY = false

    /*
	 * History and killers
	 */
    const val NUM_KILLER_MOVES = 2
    const val USE_HISTORY_HEURISTIC = true
    const val USE_MATE_HISTORY_KILLERS = true
    const val HISTORY_MAX_VALUE = 20000

    /*
	 * Null move
	 */
    const val USE_VERIFIED_NULLMOVE = false
    const val VERIFIED_WHEN_PIECEVALUES_LESS_THAN = VALUE_ROOK
    const val NULLMOVE_REDUCE_DEPTH = 2
    const val NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE = 6
    const val NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES = VALUE_KNIGHT
    const val USE_NULLMOVE_PRUNING = true

    /*
	 * Hash
	 */
    const val MAXIMUM_HASH_AGE = 3
    const val USE_HEIGHT_REPLACE_HASH = true
    const val USE_ALWAYS_REPLACE_HASH = true
    const val USE_PAWN_HASH = true
    const val USE_QUICK_PAWN_HASH_RETURN = true
    const val USE_EVAL_FUTILITY_TEST = false
    const val EVAL_FUTILITY_WINDOW = 500
    const val USE_ASPIRATION_WINDOW = true
    const val ASPIRATION_RADIUS = 40
    var USE_INTERNAL_OPENING_BOOK = true
    const val USE_PIECE_SQUARES_IN_MOVE_ORDERING = true
    const val USE_HASH_TABLES = true

    /*
	 * Late move reductions
	 */
    const val LMR_NOT_ABOVE_ALPHA_REDUCTION = 1
    const val LMR_ABOVE_ALPHA_ADDITION = 5
    const val LMR_INITIAL_VALUE = 100
    const val LMR_THRESHOLD = 50
    const val LMR_REPLACE_VALUE_AFTER_CUT = 50
    const val LMR_CUT_MARGIN = 0
    const val LMR_LAZY_EVAL_MARGIN = 200
    const val LMR_RESEARCH_ON_FAIL_HIGH = true
    const val LMR_LEGALMOVES_BEFORE_ATTEMPT = 4
    const val NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION = -1
    const val USE_LATE_MOVE_REDUCTIONS = true

    /*
	 * Quiesce
	 */
    const val GENERATE_CHECKS_UNTIL_QUIESCE_PLY = 0
    const val DELTA_PRUNING_MARGIN = 200
    const val USE_DELTA_PRUNING = true

    /*
	 * Futility Pruning
	 */
    const val USE_FUTILITY_PRUNING = true
    const val FUTILITY_MARGIN_BASE = VALUE_PAWN * 2
    val FUTILITY_MARGIN = intArrayOf(FUTILITY_MARGIN_BASE, FUTILITY_MARGIN_BASE * 2, FUTILITY_MARGIN_BASE * 3)

    /*
	 * Extensions
	 */
    const val FRACTIONAL_EXTENSION_FULL = 8
    const val FRACTIONAL_EXTENSION_THREAT = 8
    const val FRACTIONAL_EXTENSION_CHECK = 8
    const val FRACTIONAL_EXTENSION_RECAPTURE = 8
    const val FRACTIONAL_EXTENSION_PAWN = 8
    const val RECAPTURE_EXTENSION_MARGIN = 50
    const val LAST_EXTENSION_LAYER = 4
    val MAX_NEW_EXTENSIONS_TREE_PART = intArrayOf(
        FRACTIONAL_EXTENSION_FULL, FRACTIONAL_EXTENSION_FULL / 4 * 3,
        FRACTIONAL_EXTENSION_FULL / 2, FRACTIONAL_EXTENSION_FULL / 8, 0
    )

    /*
	 * PVS
	 */
    const val USE_PV_SEARCH = true
    const val PV_MINIMUM_DISTANCE_FROM_LEAF: Byte = 2

    /*
	 * Internal Iterative Deepening
	 */
    const val USE_INTERNAL_ITERATIVE_DEEPENING = true
    const val IID_PV_NODES_ONLY = false
    const val IID_MIN_DEPTH = 5
    const val IID_REDUCE_DEPTH = 3

    /***********************************
     * Engine flags                   *
     */
    const val SEARCHSTATE_READY = 0
    const val SEARCHSTATE_SEARCHING = 1
    const val SEARCHSTATE_SEARCHCOMPLETE = 2
    const val SEARCHSTATE_SEARCHREQUESTED = 4
    const val SEARCHSTATE_NOENGINE = 5
    const val SEARCH_TYPE_TIME = 0
    const val SEARCH_TYPE_DEPTH = 1
    const val SEARCH_TYPE_NODES = 2

    /***********************************
     * Hash table settings            *
     */
    const val DEFAULT_HASHTABLE_SIZE_MB = 2
    const val DEFAULT_SEARCH_HASH_HEIGHT = -9999
    const val USE_SUPER_VERIFY_ON_HASH = false
    const val HASHENTRY_MOVE = 0
    const val HASHENTRY_SCORE = 1
    const val HASHENTRY_HEIGHT = 2
    const val HASHENTRY_FLAG = 3
    const val HASHENTRY_VERSION = 4
    const val HASHENTRY_64BIT1 = 5
    const val HASHENTRY_64BIT2 = 6
    const val HASHENTRY_LOCK1 = 7
    val NUM_HASH_FIELDS = if (USE_SUPER_VERIFY_ON_HASH) 31 else 7
    val PAWN_HASH_GAMEPHASE_RANDOM = longArrayOf(1954285564129274880L, 7752066032753943600L, 7150705261809077200L)
    const val HASH_SEED = 8288277
    const val PAWNHASHENTRY_MAIN_SCORE = 0
    const val PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE = 1
    const val PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE = 2
    const val PAWNHASHENTRY_WHITE_PASSEDPAWNS = 3
    const val PAWNHASHENTRY_BLACK_PASSEDPAWNS = 4
    const val PAWNHASHENTRY_LOCK = 5
    const val NUM_PAWNHASH_FIELDS = 6
    const val PAWNHASH_DEFAULT_SCORE = -INFINITY
    val HASHPOSITION_SIZE_BYTES = 0 + 8 /* pointer to array */ + NUM_HASH_FIELDS * 4 /* array contents */
    const val PAWNHASHENTRY_SIZE_BYTES = 0 + 8 /* pointer to array */ + NUM_PAWNHASH_FIELDS * 8 /* array contents */
    const val UPPERBOUND: Byte = 0
    const val LOWERBOUND: Byte = 1
    const val EXACTSCORE: Byte = 2
    const val EMPTY: Byte = 3

    /***********************************
     * Board and move representation  *
     */
    const val WHITE = 0
    const val BLACK = 1
    const val PROMOTION_PIECE_TOSQUARE_MASK_QUEEN = 192
    const val PROMOTION_PIECE_TOSQUARE_MASK_ROOK = 64
    const val PROMOTION_PIECE_TOSQUARE_MASK_BISHOP = 128
    const val PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT = 256
    const val PROMOTION_PIECE_TOSQUARE_MASK_FULL = 448
    const val WP = 0
    const val WN = 1
    const val WB = 2
    const val WQ = 3
    const val WK = 4
    const val WR = 5
    const val BP = 6
    const val BN = 7
    const val BB = 8
    const val BQ = 9
    const val BK = 10
    const val BR = 11
    const val ALL = 12
    const val FRIENDLY = 13
    const val ENEMY = 14
    const val ENPASSANTSQUARE = 15
    const val NUM_BITBOARDS = 16
    const val CASTLEPRIV_WK = 1
    const val CASTLEPRIV_WQ = 2
    const val CASTLEPRIV_BK = 4
    const val CASTLEPRIV_BQ = 8
    const val CASTLEPRIV_BNONE = CASTLEPRIV_BK.inv() and CASTLEPRIV_BQ.inv()
    const val CASTLEPRIV_WNONE = CASTLEPRIV_WK.inv() and CASTLEPRIV_WQ.inv()

    /***********************************
     * Board State *
     */
    const val GAMESTATE_INPLAY = 0
    const val GAMESTATE_THREEFOLD = 1
    const val GAMESTATE_50MOVERULE = 2
    const val GAMESTATE_CHECKMATE = 3
    const val GAMESTATE_STALEMATE = 4
    const val GAMESTATE_RESIGNATION = 5
    const val GAMESTATE_AGREEDDRAW = 6
    const val GAMEPHASE_UNKNOWN = -1
    const val GAMEPHASE_OPENING = 0
    const val GAMEPHASE_MIDDLEGAME = 1
    const val GAMEPHASE_ENDGAME = 1
}