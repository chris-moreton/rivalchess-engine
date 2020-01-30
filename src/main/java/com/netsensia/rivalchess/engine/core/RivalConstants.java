package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.constants.Piece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RivalConstants
{
	public static final String VERSION = "v1.0.3";

	public static final boolean IS_ANDROID_VERSION = true;

	public static final int INFINITY = 20000000;
	
	public static final boolean UCI_DEBUG = false;
	public static final int UCI_TIMER_INTERVAL_MILLIS = 50;
	public static final int UCI_TIMER_SAFTEY_MARGIN_MILLIS = 250;

	public static final int MAX_GAME_MOVES = 500;
	public static final int MAX_NODES_TO_SEARCH = Integer.MAX_VALUE;
	public static final int MAX_LEGAL_MOVES = 220;
	public static final int MAX_SEARCH_MILLIS = 60 * 60 * 1000; // 1 hour
	public static final int MIN_SEARCH_MILLIS = 25;
	public static final int MAX_SEARCH_DEPTH = 125;
	public static final int MAX_QUIESCE_DEPTH = 50;
	public static final int MAX_EXTENSION_DEPTH = 60;
	public static final int MAX_TREE_DEPTH = MAX_SEARCH_DEPTH + MAX_QUIESCE_DEPTH + MAX_EXTENSION_DEPTH + 1;

/***********************************	
 *	Evaluation function weightings *
 ***********************************/
	public static final int VALUE_KNIGHT = 390;
	public static final int VALUE_BISHOP = 390;
	public static final int VALUE_ROOK = 595;
	public static final int VALUE_QUEEN = 1175;
	public static final int VALUE_KING = 30000;
	
	public static final List<Integer> PIECE_VALUES = Collections.unmodifiableList(Arrays.asList(
			Piece.PAWN.getValue(), VALUE_KNIGHT, VALUE_BISHOP, VALUE_QUEEN, VALUE_KING, VALUE_ROOK,
			Piece.PAWN.getValue(), VALUE_KNIGHT, VALUE_BISHOP, VALUE_QUEEN, VALUE_KING, VALUE_ROOK
	));

	public static final int TOTAL_PIECE_VALUE_PER_SIDE_AT_START = (VALUE_KNIGHT * 2) + (VALUE_BISHOP * 2) + (VALUE_ROOK * 2) + (VALUE_QUEEN);

	public static final int OPENING_PHASE_MATERIAL = (int)(TOTAL_PIECE_VALUE_PER_SIDE_AT_START * 0.8);
	
	public static final int TRADE_BONUS_UPPER_PAWNS = 600;
	
	public static final int WRONG_COLOUR_BISHOP_PENALTY_DIVISOR = 2;
	public static final int WRONG_COLOUR_BISHOP_MATERIAL_LOW = VALUE_BISHOP * 2;
	public static final int WRONG_COLOUR_BISHOP_MATERIAL_HIGH = VALUE_QUEEN * 2 + VALUE_ROOK * 2 + VALUE_BISHOP * 2;
	
	public static final int KNIGHT_STAGE_MATERIAL_LOW = VALUE_KNIGHT + (8 * Piece.PAWN.getValue());
	public static final int KNIGHT_STAGE_MATERIAL_HIGH = VALUE_QUEEN + (2 * VALUE_ROOK) + (2 * VALUE_BISHOP) + (6 * Piece.PAWN.getValue());

	public static final int PAWN_STAGE_MATERIAL_LOW = VALUE_ROOK;
	public static final int PAWN_STAGE_MATERIAL_HIGH = VALUE_QUEEN + (2 * VALUE_ROOK) + (2 * VALUE_BISHOP);

	public static final int CASTLE_BONUS_LOW_MATERIAL = VALUE_ROOK;
	public static final int CASTLE_BONUS_HIGH_MATERIAL = VALUE_QUEEN + (VALUE_ROOK * 2) + (VALUE_BISHOP * 2);

	public static final int[] VALUE_BISHOP_MOBILITY = {-15,-10,-6,-2,2,6,10,13,16,18,20,22,23,24};
	public static final int VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS = 3;
	public static final int VALUE_TRAPPED_BISHOP_PENALTY = (int)(Piece.PAWN.getValue() * 1.5);
	public static final int VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY = Piece.PAWN.getValue();

	public static final int VALUE_BISHOP_PAIR = 20;
	
	public static final int VALUE_MATE = 10000;
	public static final int MATE_SCORE_START = 9000; // Allows for a mate in 500, probably enough :)
	public static final int VALUE_SHOULD_WIN = 300;
	
	public static final int VALUE_ROOK_ON_OPEN_FILE = 25;
	public static final int VALUE_ROOK_ON_HALF_OPEN_FILE = 12;
	public static final int VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING = 20;
	public static final int VALUE_ROOKS_ON_SAME_FILE = 8;
	public static final int[] VALUE_ROOK_MOBILITY = {-10,-7,-4,-1,2,5,7,9,11,12,13,14,14,14,14};
	
	public static final int[] VALUE_QUEEN_MOBILITY = {-5,-4,-3,-2,-1,0,1,2,3,4,5,6,7,8,9,9,10,10,10,10,10,10,10,10,10,10,10,10};

	public static final int VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY = 2;
	
	public static final int VALUE_SIDE_PAWN_PENALTY = 10;
	public static final int VALUE_DOUBLED_PAWN_PENALTY = 25;
	public static final int VALUE_PAWN_ISLAND_PENALTY = 15;
	
	/*
	 *  gets scaled up on rank and scaled down on material (0.9 - material ratio, < 0.1 = 0.1) 
	 *  7th rank + 0 pieces = 6*50 * 0.9 = 270  
	 *  5th rank, half material remaining = 4*50 * 0.4 = 80
	 *  3rd rank, all material remaining = 2*60 * 0.1 = 12
	 */
	public static final int[] VALUE_PASSED_PAWN_BONUS = {-1,24,26,30,36,44,56,-1};
	public static final int VALUE_ISOLATED_PAWN_PENALTY = 15;
	public static final int VALUE_BACKWARD_PAWN_PENALTY = 15;
	public static final int VALUE_GUARDED_PASSED_PAWN = 15;
	public static final int VALUE_KING_CANNOT_CATCH_PAWN = 500;
	public static final int PAWN_ADJUST_MAX_MATERIAL = VALUE_QUEEN + VALUE_ROOK; // passed pawn bonus starts increasing once enemy material falls below this
	public static final int VALUE_ISOLATED_DPAWN_PENALTY = 30;

	public static final int KINGSAFETY_RIGHTWAY_DIVISOR = 4;

	/*
	 * King Safety
	 */

	public static final int KINGSAFTEY_HALFOPEN_MIDFILE = 25;
	public static final int KINGSAFTEY_HALFOPEN_NONMIDFILE = 10;

	public static final int KINGSAFTEY_UNIT = 16;
	public static final int KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT = KINGSAFTEY_UNIT * 3;
	public static final int KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT = KINGSAFTEY_UNIT * 2;
	public static final int KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT = KINGSAFTEY_UNIT * 2;
	public static final int KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT = KINGSAFTEY_UNIT;
	public static final int KINGSAFTEY_MAXIMUM_SHIELD_BONUS = KINGSAFTEY_UNIT * 8;
	public static final int KINGSAFETY_SHIELD_BASE = -(KINGSAFTEY_UNIT * 9); 
	public static final int KINGSAFETY_UNCASTLED_TRAPPED_ROOK = KINGSAFTEY_UNIT * 6; 
	public static final int KINGSAFETY_ATTACK_MULTIPLIER = 4;
	
	public static final int KINGSAFETY_MIN_PIECE_BALANCE = VALUE_ROOK + VALUE_BISHOP;
	public static final int KINGSAFETY_MAX_PIECE_BALANCE = TOTAL_PIECE_VALUE_PER_SIDE_AT_START;
	
	public static final int THREAT_SCORE_DIVISOR = 64;

	public static final int EVAL_ENDGAME_TOTAL_PIECES = VALUE_ROOK * 6;	
	public static final int ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR = 5;
	public static final int ENDGAME_DRAW_DIVISOR = 30;
	public static final int ENDGAME_PROBABLE_DRAW_DIVISOR = 6;
	public static final int ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE = 10;
	public static final double ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER = 0.9;
	public static final int ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE = 50;
		
	public static final int DRAW_CONTEMPT = 0;
	
/***********************************	
 *	Search parameters              *
 ***********************************/

	/*
	 * History and killers
	 */
	public static final int NUM_KILLER_MOVES = 2;
	public static final boolean USE_HISTORY_HEURISTIC = true;
	public static final boolean USE_MATE_HISTORY_KILLERS = true;
	public static final int HISTORY_MAX_VALUE = 20000;
	
	/*
	 * Null move
	 */
	public static final int NULLMOVE_REDUCE_DEPTH = 2;
	public static final int NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE = 6;
	public static final int NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES = VALUE_KNIGHT;
	public static final boolean USE_NULLMOVE_PRUNING = true;

	/*
	 * Hash
	 */
	public static final int MAXIMUM_HASH_AGE = 3;
	public static final boolean USE_HEIGHT_REPLACE_HASH = true;
	public static final boolean USE_ALWAYS_REPLACE_HASH = true;

	public static final boolean USE_PAWN_HASH = true;
	public static final boolean USE_QUICK_PAWN_HASH_RETURN = true;

	public static final boolean USE_ASPIRATION_WINDOW = true;
	public static final int ASPIRATION_RADIUS = 40;
	
	public static final boolean USE_INTERNAL_OPENING_BOOK = true;
	
	public static final boolean USE_PIECE_SQUARES_IN_MOVE_ORDERING = true;
	
	public static final boolean USE_HASH_TABLES = true;

	/*
	 * Late move reductions
	 */
	public static final int LMR_NOT_ABOVE_ALPHA_REDUCTION = 1;
	public static final int LMR_ABOVE_ALPHA_ADDITION = 5;
	public static final int LMR_INITIAL_VALUE = 100;
	public static final int LMR_THRESHOLD = 50;
	public static final int LMR_REPLACE_VALUE_AFTER_CUT = 50;
	public static final int LMR_CUT_MARGIN = 0;
	public static final boolean LMR_RESEARCH_ON_FAIL_HIGH = true;
	
	public static final int LMR_LEGALMOVES_BEFORE_ATTEMPT = 4;
	public static final int NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION = -1;
	
	public static final boolean USE_LATE_MOVE_REDUCTIONS = true;

	/*
	 * Quiesce
	 */
	public static final int GENERATE_CHECKS_UNTIL_QUIESCE_PLY = 0;
	public static final int DELTA_PRUNING_MARGIN = 200;
	public static final boolean USE_DELTA_PRUNING = true;
	
	/*
	 * Futility Pruning
	 */
	public static final boolean USE_FUTILITY_PRUNING = true;
	public static final int FUTILITY_MARGIN_BASE = Piece.PAWN.getValue() * 2;
	public static final List<Integer> FUTILITY_MARGIN =
			Collections.unmodifiableList(
					Arrays.asList(FUTILITY_MARGIN_BASE, FUTILITY_MARGIN_BASE * 2, FUTILITY_MARGIN_BASE * 3));

	/*
	 * Extensions
	 */
	public static final int FRACTIONAL_EXTENSION_FULL = 8;
	public static final int FRACTIONAL_EXTENSION_THREAT = 8;
	public static final int FRACTIONAL_EXTENSION_CHECK = 8;
	public static final int FRACTIONAL_EXTENSION_RECAPTURE = 8;
	public static final int FRACTIONAL_EXTENSION_PAWN = 8;
	public static final int RECAPTURE_EXTENSION_MARGIN = 50;
	
	public static final int LAST_EXTENSION_LAYER = 4;
	
	public static final List<Integer> MAX_NEW_EXTENSIONS_TREE_PART =
			Collections.unmodifiableList(Arrays.asList(FRACTIONAL_EXTENSION_FULL, FRACTIONAL_EXTENSION_FULL / 4 * 3,
					FRACTIONAL_EXTENSION_FULL / 2, FRACTIONAL_EXTENSION_FULL / 8, 0));
	
	/*
	 * PVS
	 */
	public static final boolean USE_PV_SEARCH = true;
	public static final byte PV_MINIMUM_DISTANCE_FROM_LEAF = 2;
	
	/*
	 * Internal Iterative Deepening
	 */
	public static final boolean USE_INTERNAL_ITERATIVE_DEEPENING = true;
	public static final int IID_MIN_DEPTH = 5;
	public static final int IID_REDUCE_DEPTH = 3;
	
/***********************************	
 *	Engine flags                   *
 ***********************************/	
	public static final int SEARCHSTATE_READY = 0;
	public static final int SEARCHSTATE_SEARCHING = 1;
	public static final int SEARCHSTATE_SEARCHCOMPLETE = 2;
	public static final int SEARCHSTATE_SEARCHREQUESTED = 4;

/***********************************	
 *	Hash table settings            *
 ***********************************/	
			
	public static final int DEFAULT_HASHTABLE_SIZE_MB = 2;
	public static final int DEFAULT_SEARCH_HASH_HEIGHT = -9999;
	
	public static final boolean USE_SUPER_VERIFY_ON_HASH = false;

	public static final int HASHENTRY_MOVE = 0;
	public static final int HASHENTRY_SCORE = 1;
	public static final int HASHENTRY_HEIGHT = 2;
	public static final int HASHENTRY_FLAG = 3;
	public static final int HASHENTRY_VERSION = 4;
	public static final int HASHENTRY_64BIT1 = 5;
	public static final int HASHENTRY_64BIT2 = 6;
	public static final int HASHENTRY_LOCK1 = 7;
	public static final int NUM_HASH_FIELDS = USE_SUPER_VERIFY_ON_HASH ? 31 : 7;

	public static final int PAWNHASHENTRY_MAIN_SCORE = 0;
	public static final int PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE = 1;
	public static final int PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE = 2;
	public static final int PAWNHASHENTRY_WHITE_PASSEDPAWNS = 3;
	public static final int PAWNHASHENTRY_BLACK_PASSEDPAWNS = 4;
	public static final int PAWNHASHENTRY_LOCK = 5;
	public static final int NUM_PAWNHASH_FIELDS = 6;
	public static final int PAWNHASH_DEFAULT_SCORE = -INFINITY;
	
	public static final int HASHPOSITION_SIZE_BYTES = 8 +  /* pointer to array */ (NUM_HASH_FIELDS * 4); /* array contents */
	public static final int PAWNHASHENTRY_SIZE_BYTES = 8 +  /* pointer to array */ (NUM_PAWNHASH_FIELDS * 8); /* array contents */
	
	public static final byte UPPERBOUND = 0;
	public static final byte LOWERBOUND = 1;
	public static final byte EXACTSCORE = 2;
	public static final byte EMPTY = 3;
	
/***********************************	
 *	Board and move representation  *
 ***********************************/
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_QUEEN = 192; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_ROOK = 64; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_BISHOP = 128; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT = 256; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_FULL = 448;
	
	public static final int WP = 0;
	public static final int WN = 1;
	public static final int WB = 2;
	public static final int WQ = 3;
	public static final int WK = 4;
	public static final int WR = 5;
	public static final int BP = 6;
	public static final int BN = 7;
	public static final int BB = 8;
	public static final int BQ = 9;
	public static final int BK = 10;
	public static final int BR = 11;
	
	public static final int ALL = 12;
	public static final int FRIENDLY = 13;
	public static final int ENEMY = 14;
	public static final int ENPASSANTSQUARE = 15;

	public static final int NUM_BITBOARDS = 16;
	
	public static final int CASTLEPRIV_WK = 1;
	public static final int CASTLEPRIV_WQ = 2;
	public static final int CASTLEPRIV_BK = 4;
	public static final int CASTLEPRIV_BQ = 8;
	public static final int CASTLEPRIV_BNONE = ~CASTLEPRIV_BK & ~CASTLEPRIV_BQ;
	public static final int CASTLEPRIV_WNONE = ~CASTLEPRIV_WK & ~CASTLEPRIV_WQ;

	private static RivalConstants instance = null;

	private RivalConstants() {}

	public static RivalConstants getInstance() {
		if (instance == null) {
			instance = new RivalConstants();
		}
		return instance;
	}
}
