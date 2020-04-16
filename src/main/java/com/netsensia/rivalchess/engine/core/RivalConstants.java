package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.engine.core.eval.PieceValue;
import com.netsensia.rivalchess.model.Piece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RivalConstants
{

	/*
	 * Quiesce
	 */
	@Deprecated
	public static final int GENERATE_CHECKS_UNTIL_QUIESCE_PLY = 0;
	@Deprecated
	public static final int DELTA_PRUNING_MARGIN = 200;

	@Deprecated
	public static final int FUTILITY_MARGIN_BASE = PieceValue.getValue(Piece.PAWN) * 2;
	@Deprecated
	public static final List<Integer> FUTILITY_MARGIN =
			Collections.unmodifiableList(
					Arrays.asList(FUTILITY_MARGIN_BASE, FUTILITY_MARGIN_BASE * 2, FUTILITY_MARGIN_BASE * 3));
	@Deprecated
	public static final byte PV_MINIMUM_DISTANCE_FROM_LEAF = 2;

/***********************************	
 *	Hash table settings            *
 ***********************************/

	@Deprecated
	public static final int DEFAULT_HASHTABLE_SIZE_MB = 2;
	@Deprecated
	public static final int DEFAULT_SEARCH_HASH_HEIGHT = -9999;

	@Deprecated
	public static final boolean USE_SUPER_VERIFY_ON_HASH = false;

	@Deprecated
	public static final int HASHENTRY_MOVE = 0;
	@Deprecated
	public static final int HASHENTRY_SCORE = 1;
	@Deprecated
	public static final int HASHENTRY_HEIGHT = 2;
	@Deprecated
	public static final int HASHENTRY_FLAG = 3;
	@Deprecated
	public static final int HASHENTRY_VERSION = 4;
	@Deprecated
	public static final int HASHENTRY_64BIT1 = 5;
	@Deprecated
	public static final int HASHENTRY_64BIT2 = 6;
	@Deprecated
	public static final int HASHENTRY_LOCK1 = 7;
	@Deprecated
	public static final int NUM_HASH_FIELDS = USE_SUPER_VERIFY_ON_HASH ? 31 : 7;

	@Deprecated
	public static final int PAWNHASHENTRY_MAIN_SCORE = 0;
	@Deprecated
	public static final int PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE = 1;
	@Deprecated
	public static final int PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE = 2;
	@Deprecated
	public static final int PAWNHASHENTRY_WHITE_PASSEDPAWNS = 3;
	@Deprecated
	public static final int PAWNHASHENTRY_BLACK_PASSEDPAWNS = 4;
	@Deprecated
	public static final int PAWNHASHENTRY_LOCK = 5;
	@Deprecated
	public static final int NUM_PAWNHASH_FIELDS = 6;

	@Deprecated
	public static final int PAWNHASH_DEFAULT_SCORE = -Integer.MAX_VALUE;

	@Deprecated
	public static final int HASHPOSITION_SIZE_BYTES = 8 +  /* pointer to array */ (NUM_HASH_FIELDS * 4); /* array contents */
	@Deprecated
	public static final int PAWNHASHENTRY_SIZE_BYTES = 8 +  /* pointer to array */ (NUM_PAWNHASH_FIELDS * 8); /* array contents */

	@Deprecated
	public static final int INFINITY = 20000000;

	@Deprecated
	public static final int UCI_TIMER_INTERVAL_MILLIS = 50;
	@Deprecated
	public static final int UCI_TIMER_SAFTEY_MARGIN_MILLIS = 250;

	@Deprecated
	public static final int MAX_GAME_MOVES = 500;
	@Deprecated
	public static final int MAX_NODES_TO_SEARCH = Integer.MAX_VALUE;
	@Deprecated
	public static final int MAX_LEGAL_MOVES = 220;
	@Deprecated
	public static final int MAX_SEARCH_MILLIS = 60 * 60 * 1000; // 1 hour
	@Deprecated
	public static final int MIN_SEARCH_MILLIS = 25;
	@Deprecated
	public static final int MAX_SEARCH_DEPTH = 125;
	@Deprecated
	public static final int MAX_QUIESCE_DEPTH = 50;
	@Deprecated
	public static final int MAX_EXTENSION_DEPTH = 60;
	@Deprecated
	public static final int MAX_TREE_DEPTH = MAX_SEARCH_DEPTH + MAX_QUIESCE_DEPTH + MAX_EXTENSION_DEPTH + 1;

	@Deprecated
	public static final List<Integer> PIECE_VALUES = Collections.unmodifiableList(Arrays.asList(
			PieceValue.getValue(Piece.PAWN), PieceValue.getValue(Piece.KNIGHT), PieceValue.getValue(Piece.BISHOP), PieceValue.getValue(Piece.QUEEN), PieceValue.getValue(Piece.KING), PieceValue.getValue(Piece.ROOK),
			PieceValue.getValue(Piece.PAWN), PieceValue.getValue(Piece.KNIGHT), PieceValue.getValue(Piece.BISHOP), PieceValue.getValue(Piece.QUEEN), PieceValue.getValue(Piece.KING), PieceValue.getValue(Piece.ROOK)
	));

	@Deprecated
	public static final int TOTAL_PIECE_VALUE_PER_SIDE_AT_START = (PieceValue.getValue(Piece.KNIGHT) * 2) + (PieceValue.getValue(Piece.BISHOP) * 2) + (PieceValue.getValue(Piece.ROOK) * 2) + (PieceValue.getValue(Piece.QUEEN));

	@Deprecated
	public static final int OPENING_PHASE_MATERIAL = (int)(TOTAL_PIECE_VALUE_PER_SIDE_AT_START * 0.8);

	@Deprecated
	public static final int TRADE_BONUS_UPPER_PAWNS = 600;

	@Deprecated
	public static final int WRONG_COLOUR_BISHOP_PENALTY_DIVISOR = 2;
	@Deprecated
	public static final int WRONG_COLOUR_BISHOP_MATERIAL_LOW = PieceValue.getValue(Piece.BISHOP) * 2;
	@Deprecated
	public static final int WRONG_COLOUR_BISHOP_MATERIAL_HIGH = PieceValue.getValue(Piece.QUEEN) * 2 + PieceValue.getValue(Piece.ROOK) * 2 + PieceValue.getValue(Piece.BISHOP) * 2;

	@Deprecated
	public static final int KNIGHT_STAGE_MATERIAL_LOW = PieceValue.getValue(Piece.KNIGHT) + (8 * PieceValue.getValue(Piece.PAWN));
	@Deprecated
	public static final int KNIGHT_STAGE_MATERIAL_HIGH = PieceValue.getValue(Piece.QUEEN) + (2 * PieceValue.getValue(Piece.ROOK)) + (2 * PieceValue.getValue(Piece.BISHOP)) + (6 * PieceValue.getValue(Piece.PAWN));

	@Deprecated
	public static final int PAWN_STAGE_MATERIAL_LOW = PieceValue.getValue(Piece.ROOK);
	@Deprecated
	public static final int PAWN_STAGE_MATERIAL_HIGH = PieceValue.getValue(Piece.QUEEN) + (2 * PieceValue.getValue(Piece.ROOK)) + (2 * PieceValue.getValue(Piece.BISHOP));

	@Deprecated
	public static final int CASTLE_BONUS_LOW_MATERIAL = PieceValue.getValue(Piece.ROOK);
	@Deprecated
	public static final int CASTLE_BONUS_HIGH_MATERIAL = PieceValue.getValue(Piece.QUEEN) + (PieceValue.getValue(Piece.ROOK) * 2) + (PieceValue.getValue(Piece.BISHOP) * 2);

	@Deprecated
	public static final int[] VALUE_BISHOP_MOBILITY = {-15,-10,-6,-2,2,6,10,13,16,18,20,22,23,24};
	@Deprecated
	public static final int VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS = 3;
	@Deprecated
	public static final int VALUE_TRAPPED_BISHOP_PENALTY = (int)(PieceValue.getValue(Piece.PAWN) * 1.5);
	@Deprecated
	public static final int VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY = PieceValue.getValue(Piece.PAWN);

	@Deprecated
	public static final int VALUE_BISHOP_PAIR = 20;

	@Deprecated
	public static final int VALUE_MATE = 10000;
	@Deprecated
	public static final int MATE_SCORE_START = 9000; // Allows for a mate in 500, probably enough :)
	@Deprecated
	public static final int VALUE_SHOULD_WIN = 300;

	@Deprecated
	public static final int VALUE_ROOK_ON_OPEN_FILE = 25;
	@Deprecated
	public static final int VALUE_ROOK_ON_HALF_OPEN_FILE = 12;
	@Deprecated
	public static final int VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING = 20;
	@Deprecated
	public static final int VALUE_ROOKS_ON_SAME_FILE = 8;
	@Deprecated
	public static final int[] VALUE_ROOK_MOBILITY = {-10,-7,-4,-1,2,5,7,9,11,12,13,14,14,14,14};

	@Deprecated
	public static final int[] VALUE_QUEEN_MOBILITY = {-5,-4,-3,-2,-1,0,1,2,3,4,5,6,7,8,9,9,10,10,10,10,10,10,10,10,10,10,10,10};

	@Deprecated
	public static final int VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY = 2;

	@Deprecated
	public static final int VALUE_SIDE_PAWN_PENALTY = 10;
	@Deprecated
	public static final int VALUE_DOUBLED_PAWN_PENALTY = 25;
	@Deprecated
	public static final int VALUE_PAWN_ISLAND_PENALTY = 15;

	/*
	 *  gets scaled up on rank and scaled down on material (0.9 - material ratio, < 0.1 = 0.1)
	 *  7th rank + 0 pieces = 6*50 * 0.9 = 270
	 *  5th rank, half material remaining = 4*50 * 0.4 = 80
	 *  3rd rank, all material remaining = 2*60 * 0.1 = 12
	 */
	@Deprecated
	public static final int[] VALUE_PASSED_PAWN_BONUS = {-1,24,26,30,36,44,56,-1};
	@Deprecated
	public static final int VALUE_ISOLATED_PAWN_PENALTY = 15;
	@Deprecated
	public static final int VALUE_BACKWARD_PAWN_PENALTY = 15;
	@Deprecated
	public static final int VALUE_GUARDED_PASSED_PAWN = 15;
	@Deprecated
	public static final int VALUE_KING_CANNOT_CATCH_PAWN = 500;
	@Deprecated
	public static final int PAWN_ADJUST_MAX_MATERIAL = PieceValue.getValue(Piece.QUEEN) + PieceValue.getValue(Piece.ROOK); // passed pawn bonus starts increasing once enemy material falls below this
	@Deprecated
	public static final int VALUE_ISOLATED_DPAWN_PENALTY = 30;

	/*
	 * King Safety
	 */

	@Deprecated
	public static final int KINGSAFTEY_HALFOPEN_MIDFILE = 25;
	@Deprecated
	public static final int KINGSAFTEY_HALFOPEN_NONMIDFILE = 10;

	@Deprecated
	public static final int KINGSAFTEY_UNIT = 16;
	@Deprecated
	public static final int KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT = KINGSAFTEY_UNIT * 3;
	@Deprecated
	public static final int KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT = KINGSAFTEY_UNIT * 2;
	@Deprecated
	public static final int KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT = KINGSAFTEY_UNIT * 2;
	@Deprecated
	public static final int KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT = KINGSAFTEY_UNIT;
	@Deprecated
	public static final int KINGSAFTEY_MAXIMUM_SHIELD_BONUS = KINGSAFTEY_UNIT * 8;
	@Deprecated
	public static final int KINGSAFETY_SHIELD_BASE = -(KINGSAFTEY_UNIT * 9);
	@Deprecated
	public static final int KINGSAFETY_UNCASTLED_TRAPPED_ROOK = KINGSAFTEY_UNIT * 6;
	@Deprecated
	public static final int KINGSAFETY_ATTACK_MULTIPLIER = 4;

	@Deprecated
	public static final int KINGSAFETY_MIN_PIECE_BALANCE = PieceValue.getValue(Piece.ROOK) + PieceValue.getValue(Piece.BISHOP);
	@Deprecated
	public static final int KINGSAFETY_MAX_PIECE_BALANCE = TOTAL_PIECE_VALUE_PER_SIDE_AT_START;

	@Deprecated
	public static final int THREAT_SCORE_DIVISOR = 64;

	@Deprecated
	public static final int EVAL_ENDGAME_TOTAL_PIECES = PieceValue.getValue(Piece.ROOK) * 6;
	@Deprecated
	public static final int ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR = 5;
	@Deprecated
	public static final int ENDGAME_DRAW_DIVISOR = 30;
	@Deprecated
	public static final int ENDGAME_PROBABLE_DRAW_DIVISOR = 6;
	@Deprecated
	public static final int ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE = 10;
	@Deprecated
	public static final double ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER = 0.9;
	@Deprecated
	public static final int ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE = 50;

	@Deprecated
	public static final int DRAW_CONTEMPT = 0;

	/***********************************
	 *	Search parameters              *
	 ***********************************/

	/*
	 * History and killers
	 */
	@Deprecated
	public static final int NUM_KILLER_MOVES = 2;
	@Deprecated
	public static final int HISTORY_MAX_VALUE = 20000;

	@Deprecated
	public static final boolean USE_HISTORY_HEURISTIC = true;
	@Deprecated
	public static final boolean USE_MATE_HISTORY_KILLERS = true;

	@Deprecated
	public static final byte UPPERBOUND = 0;
	@Deprecated
	public static final byte LOWERBOUND = 1;
	@Deprecated
	public static final byte EXACTSCORE = 2;
	@Deprecated
	public static final byte EMPTY = 3;

	@Deprecated
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_QUEEN = 192;
	@Deprecated
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_ROOK = 64;
	@Deprecated
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_BISHOP = 128;
	@Deprecated
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT = 256;
	@Deprecated
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_FULL = 448;

	@Deprecated
	public static final int WP = 0;
	@Deprecated
	public static final int WN = 1;
	@Deprecated
	public static final int WB = 2;
	@Deprecated
	public static final int WQ = 3;
	@Deprecated
	public static final int WK = 4;
	@Deprecated
	public static final int WR = 5;
	@Deprecated
	public static final int BP = 6;
	@Deprecated
	public static final int BN = 7;
	@Deprecated
	public static final int BB = 8;
	@Deprecated
	public static final int BQ = 9;
	@Deprecated
	public static final int BK = 10;
	@Deprecated
	public static final int BR = 11;

	@Deprecated
	public static final int ALL = 12;
	@Deprecated
	public static final int FRIENDLY = 13;
	@Deprecated
	public static final int ENEMY = 14;
	@Deprecated
	public static final int ENPASSANTSQUARE = 15;

	@Deprecated
	public static final int NUM_BITBOARDS = 16;

	@Deprecated
	public static final int CASTLEPRIV_WK = 1;
	@Deprecated
	public static final int CASTLEPRIV_WQ = 2;
	@Deprecated
	public static final int CASTLEPRIV_BK = 4;
	@Deprecated
	public static final int CASTLEPRIV_BQ = 8;
	@Deprecated
	public static final int CASTLEPRIV_BNONE = ~CASTLEPRIV_BK & ~CASTLEPRIV_BQ;
	@Deprecated
	public static final int CASTLEPRIV_WNONE = ~CASTLEPRIV_WK & ~CASTLEPRIV_WQ;

	/*
	 * Extensions
	 */
	@Deprecated
	public static final int FRACTIONAL_EXTENSION_FULL = 8;
	@Deprecated
	public static final int FRACTIONAL_EXTENSION_THREAT = 8;
	@Deprecated
	public static final int FRACTIONAL_EXTENSION_CHECK = 8;
	@Deprecated
	public static final int FRACTIONAL_EXTENSION_RECAPTURE = 8;
	@Deprecated
	public static final int FRACTIONAL_EXTENSION_PAWN = 8;
	@Deprecated
	public static final int RECAPTURE_EXTENSION_MARGIN = 50;

	@Deprecated
	public static final int LAST_EXTENSION_LAYER = 4;

	@Deprecated
	public static final List<Integer> MAX_NEW_EXTENSIONS_TREE_PART =
			Collections.unmodifiableList(Arrays.asList(FRACTIONAL_EXTENSION_FULL, FRACTIONAL_EXTENSION_FULL / 4 * 3,
					FRACTIONAL_EXTENSION_FULL / 2, FRACTIONAL_EXTENSION_FULL / 8, 0));

	/*
	 * PVS
	 */
	@Deprecated
	public static final boolean USE_PV_SEARCH = true;

	@Deprecated
	public static final boolean USE_DELTA_PRUNING = true;

	/*
	 * Futility Pruning
	 */
	@Deprecated
	public static final boolean USE_FUTILITY_PRUNING = true;

	/*
	 * Internal Iterative Deepening
	 */
	@Deprecated
	public static final boolean USE_INTERNAL_ITERATIVE_DEEPENING = true;
	@Deprecated
	public static final int IID_MIN_DEPTH = 5;
	@Deprecated
	public static final int IID_REDUCE_DEPTH = 3;

	@Deprecated
	public static final boolean USE_HEIGHT_REPLACE_HASH = true;
	@Deprecated
	public static final boolean USE_ALWAYS_REPLACE_HASH = true;
	@Deprecated
	public static final boolean USE_PAWN_HASH = true;
	@Deprecated
	public static final boolean USE_QUICK_PAWN_HASH_RETURN = true;
	@Deprecated
	public static final boolean USE_ASPIRATION_WINDOW = true;
	@Deprecated
	public static final boolean USE_INTERNAL_OPENING_BOOK = true;
	@Deprecated
	public static final boolean USE_PIECE_SQUARES_IN_MOVE_ORDERING = true;
	@Deprecated
	public static final boolean USE_LATE_MOVE_REDUCTIONS = true;
	@Deprecated
	public static final boolean USE_HASH_TABLES = true;
	@Deprecated
	public static final boolean USE_NULLMOVE_PRUNING = true;

	/*
	 * Late move reductions
	 */
	@Deprecated
	public static final int LMR_NOT_ABOVE_ALPHA_REDUCTION = 1;
	@Deprecated
	public static final int LMR_ABOVE_ALPHA_ADDITION = 5;
	@Deprecated
	public static final int LMR_INITIAL_VALUE = 100;
	@Deprecated
	public static final int LMR_THRESHOLD = 50;
	@Deprecated
	public static final int LMR_REPLACE_VALUE_AFTER_CUT = 50;
	@Deprecated
	public static final int LMR_CUT_MARGIN = 0;
	@Deprecated
	public static final boolean LMR_RESEARCH_ON_FAIL_HIGH = true;

	@Deprecated
	public static final int LMR_LEGALMOVES_BEFORE_ATTEMPT = 4;
	@Deprecated
	public static final int NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION = -1;

	private static RivalConstants instance = null;

	private RivalConstants() {}

	public static RivalConstants getInstance() {
		if (instance == null) {
			instance = new RivalConstants();
		}
		return instance;
	}
}
