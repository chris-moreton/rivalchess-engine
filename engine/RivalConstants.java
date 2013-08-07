package com.netadapt.rivalchess.engine;

public final class RivalConstants 
{
	public static final String VERSION = "0.001";
	public static final int INFINITY = 20000000;
	
/***********************************	
 *	Search paramaters              *
 ***********************************/
	public static final boolean USE_KILLER_HEURISTIC = true;
	public static final int NUM_KILLER_MOVES = 2;

	public static final boolean USE_SLIGHT_RANDOM_IN_EVALUATION = false;
	public static final int EVAL_EVALUATION_RANDOM_SIZE = 10;

	public static final byte PV_MINIMUM_DISTANCE_FROM_LEAF = 2;

	public static final boolean USE_STATIC_EXCHANGE_EVALUATION = false;
	
	public static final boolean USE_NULLMOVE_PRUNING = true;
	public static final int NULLMOVE_REDUCE_DEPTH = 3;
	public static final int NULLMOVE_MINIMUM_DISTANCE_FROM_LEAF = 4;
	public static final boolean STORE_NULLMOVE_CUTOFFS_IN_HASH = false;

	public static final boolean USE_HASH_TABLES = true;
	public static final boolean USE_HEIGHT_REPLACE_HASH = true;
	public static final boolean USE_ALWAYS_REPLACE_HASH = true;

	public static final boolean USE_EVAL_HASH = false;
	public static final boolean USE_PAWN_HASH = true;
	
	public static final boolean USE_EVAL_FUTILITY_TEST = false;
	public static final boolean USE_ASPIRATION_WINDOW = true;
	public static final int ASPIRATION_RADIUS = 40;
	
	public static final boolean USE_INTERNAL_OPENING_BOOK = false;
	
	public static final boolean USE_PV_SEARCH = true;
	
	public static final int MAX_EXTENSION_DEPTH = 20;
	public static final boolean USE_CHECK_EXTENSIONS = true;
	public static final boolean USE_RECAPTURE_EXTENSIONS = true;
	
	public static final boolean RETURN_ALPHA_INSTEAD_OF_BESTSCORE = false;

	public static final int PROMOTION_PIECE_TOSQUARE_MASK_QUEEN = 192; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_ROOK = 64; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_BISHOP = 128; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT = 256; 
	public static final int PROMOTION_PIECE_TOSQUARE_MASK_FULL = 448;
	
	public static final int MAX_GAME_MOVES = 500;

	public static final int MAX_SEARCH_MILLIS = 60 * 60 * 1000; // 1 hour
	public static final int MAX_SEARCH_DEPTH = 75;
	public static final int MAX_QUIESCE_DEPTH = 50;
	public static final int MAX_LEGAL_MOVES = 220;
	public static final int NUM_ENGINEMOVE_SORT_CATEGORIES = 7;
	public static final int MAX_TREE_DEPTH = MAX_SEARCH_DEPTH + MAX_QUIESCE_DEPTH + MAX_EXTENSION_DEPTH + 1;
		
/***********************************	
 *	Evaluation function weightings *
 ***********************************/
	public static final int VALUE_PAWN = 100;
	public static final int VALUE_KNIGHT = 310;
	public static final int VALUE_BISHOP = 320;
	public static final int VALUE_ROOK = 500;
	public static final int VALUE_QUEEN = 900;

	public static final int VALUE_QUEENEARLY_PENALTY = 40;
	
	public static final int PIECE_SQUARE_IMPORTANCE_DIVISOR = 4;

	public static final int VALUE_OPENING_CASTLE_BONUS = 55;
	public static final int VALUE_OPENING_QUEENSIDECASTLERIGHTS_BONUS = 15;
	public static final int VALUE_OPENING_KINGSIDECASTLERIGHTS_BONUS = 25;
	public static final int VALUE_OPENING_UNDEVELOPED_MINOR_PENALTY = 25;
	
	public static final int VALUE_MIDDLE_KNIGHT = 5;
	public static final int VALUE_MIDDLE_BISHOP = 4;
	public static final int VALUE_MIDDLE_PAWN = 3;
	public static final int VALUE_BISHOP_PAIR = 25;
	public static final int VALUE_TIGHT_MIDDLE_PAWN = 25;
	
	public static final int VALUE_MATE = 10000;
	
	public static final int VALUE_ROOK_ON_SEVENTH_RANK = 25;
	public static final int VALUE_ROOKS_ON_SAME_FILE = 20;
	public static final int VALUE_ROOKS_ON_SAME_RANK = 10;
	
	public static final int VALUE_QUEEN_TROPISM = 6;
	public static final int VALUE_KNIGHT_TROPISM = 6;
	public static final int VALUE_LONEKING_TROPISM = 10;
	public static final int VALUE_KINGSAFETY_PAWNPROTECTION = 16;
	public static final int VALUE_KINGSAFETY_MINORPROTECTION = 8;
	
	public static final int VALUE_SIDE_PAWN_PENALTY = 15;
	public static final int VALUE_DOUBLED_PAWN_PENALTY = 10;
	public static final int VALUE_PASSED_PAWN_BONUS = 15;
	public static final int VALUE_ISOLATED_PAWN_PENALTY = 15;
	public static final int VALUE_ISOLATED_DPAWN_PENALTY = 45;
	public static final int VALUE_PAWN_SUPPORT = 2;

	public static final int VALUE_KINGKING_TROPISM = 20;
	
	public static final int VALUE_KING_CENTRE = 50;
	
	public static final int EVAL_ENDGAME_PIECES = VALUE_QUEEN + VALUE_KNIGHT;
	
	public static final int DRAW_CONTEMPT = 0;
	public static final int EVAL_FUTILITY_WINDOW = 350;
	
/***********************************	
 *	Engine flags                   *
 ***********************************/	
	public static final int SEARCHSTATE_READY = 0;
	public static final int SEARCHSTATE_SEARCHSTARTED = 1;
	public static final int SEARCHSTATE_SEARCHCOMPLETE = 2;
	public static final int SEARCHSTATE_NOENGINE = 3;
	
	public static final int SEARCH_TYPE_TIME = 0;
	public static final int SEARCH_TYPE_DEPTH = 1;

/***********************************	
 *	Hash table settings            *
 ***********************************/	
			
	public static final int DEFAULT_HASHTABLE_SIZE_MB = 4;
	
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
	
	public static final int MAXIMUM_HASH_AGE = 0;
	
	public static final int HASH_SEED = 8288277;

	public static final int PAWNHASHENTRY_SCORE = 0;
	public static final int PAWNHASHENTRY_LOCK1 = 1;
	public static final int PAWNHASHENTRY_LOCK2 = 2;
	public static final int NUM_PAWNHASH_FIELDS = 3;
	public static final int PAWNHASH_DEFAULT_SCORE = -9999;
	
	public static final int HASHPOSITION_SIZE_BYTES = 0 + 8 /* pointer to array */ + (NUM_HASH_FIELDS * 4); /* array contents */
	public static final int PAWNHASHENTRY_SIZE_BYTES = 0 + 8 /* pointer to array */ + (NUM_PAWNHASH_FIELDS * 4); /* array contents */
	
	public static final byte UPPERBOUND = 0;
	public static final byte LOWERBOUND = 1;
	public static final byte EXACTSCORE = 2;
	public static final byte EMPTY = 3;
	
/***********************************	
 *	Board representation           *
 ***********************************/
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	
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
	
	public static final int ALLWHITE = 12;
	public static final int ALLBLACK = 13;
	public static final int ALL = 14;
	public static final int FRIENDLY = 15;
	public static final int ENEMY = 16;
	public static final int ENPASSANTSQUARE = 17;
	public static final int WHITEKINGSIDECASTLEMASK = 18;
	public static final int WHITEQUEENSIDECASTLEMASK = 19;
	public static final int BLACKKINGSIDECASTLEMASK = 20;
	public static final int BLACKQUEENSIDECASTLEMASK = 21;
	public static final int ROTATED90ANTI = 22;
	public static final int ROTATED45ANTI = 23;
	public static final int ROTATED45CLOCK = 24;
	
	public static final boolean GENERATE_SLIDING_MOVES_WITH_LOOPS = true;

	public static final int NUM_BITBOARDS = GENERATE_SLIDING_MOVES_WITH_LOOPS ? 22 : 25;
	
	public static final boolean TRACK_PIECE_LOCATIONS = false;
}
