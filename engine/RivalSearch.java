package com.netadapt.rivalchess.engine;

import java.text.NumberFormat;
import java.util.Arrays;

import com.netadapt.rivalchess.util.ChessBoardConversion;

public final class RivalSearch implements Runnable
{
	public int m_nodes = 0;
	
	public int m_alwaysHashClashes = 0;
	public int m_alwaysBadClashes = 0;
	public int m_alwaysHashValuesRetrieved = 0;
	public int m_alwaysHashValuesStored = 0;
	public int m_heightHashClashes = 0;
	public int m_heightBadClashes = 0;
	public int m_heightHashValuesRetrieved = 0;
	public int m_heightHashValuesStored = 0;
	public int m_pawnHashValuesRetrieved = 0;
	public int m_pawnHashValuesStored = 0;
	public int m_evalHashValuesRetrieved = 0;
	public int m_evalHashValuesStored = 0;
	
	public int m_largestPieceSquareDifference = 0;
	
	public EngineChessBoard m_board;
	protected int m_millisecondsToThink;
	protected boolean m_abortingSearch;
	public long m_searchStartTime = -1, m_searchTargetEndTime, m_searchEndTime = -1;
	protected int m_searchDepth = 1;
	protected int m_iterativeDeepeningCurrentDepth = 0; // current search depth for iterative deepening
	
	private int[][] killerMoves;
	
	private boolean m_inBook = RivalConstants.USE_INTERNAL_OPENING_BOOK;
	
	public int zLowIndex = 1000;
	public int zHighIndex = 0;
	public int sLowIndex = 1000;
	public int sHighIndex = 0;
	public int qLowIndex = 1000;
	public int qHighIndex = 0;
	public int checkExtensions = 0;
	public int recaptureExtensions = 0;

	int[] m_sortPieceValues, m_sortPieceTakerValues;
	
	private int m_searchState;
	private int m_hashTableVersion = 1;
	private int hashTableHeight[];
	private int hashTableAlways[];
	private int pawnHashTable[];
	private int evalHashTable[];
	public int m_maxHashEntries;
	private int m_maxPawnHashEntries;
	private int m_lastHashSizeCreated;
	
	public int m_currentDepthZeroMove;
	public int m_currentDepthZeroMoveNumber;
	
	public SearchPath m_currentPath;

	private int[][] orderedMoves = new int[RivalConstants.MAX_TREE_DEPTH][RivalConstants.MAX_LEGAL_MOVES];
	private int[][] captureSquares = new int[RivalConstants.MAX_TREE_DEPTH][2];
	private SearchPath[] searchPath;
	
	private int[] whiteRookSquares;
	private int[] blackRookSquares;
	
	private int[] depthZeroLegalMoves = new int[RivalConstants.MAX_LEGAL_MOVES];
	private int[] depthZeroMoveScores = new int[RivalConstants.MAX_LEGAL_MOVES];

	private boolean m_isDebug = false;
	
	public RivalSearch()
	{
		this.m_currentPath = new SearchPath();
		
		this.m_searchState = RivalConstants.SEARCHSTATE_READY;
		
		this.m_hashTableVersion = 0;
		
		this.searchPath = new SearchPath[RivalConstants.MAX_TREE_DEPTH];
		this.killerMoves = new int[RivalConstants.MAX_TREE_DEPTH][2];
		for (int i=0; i<RivalConstants.MAX_TREE_DEPTH; i++)
		{
			this.searchPath[i] = new SearchPath();
			this.killerMoves[i] = new int[RivalConstants.NUM_KILLER_MOVES];
		}

		depthZeroLegalMoves = new int[RivalConstants.MAX_LEGAL_MOVES];
		depthZeroMoveScores = new int[RivalConstants.MAX_LEGAL_MOVES];
		
		m_sortPieceValues = new int[6];
		m_sortPieceValues[RivalConstants.WP] = 1;
		m_sortPieceValues[RivalConstants.WN] = 2;
		m_sortPieceValues[RivalConstants.WB] = 2;
		m_sortPieceValues[RivalConstants.WR] = 3;
		m_sortPieceValues[RivalConstants.WQ] = 4;
		
		setHashSizeMB(RivalConstants.DEFAULT_HASHTABLE_SIZE_MB);
	}
	
	public void setHashSizeMB(int hashSizeMB)
	{
		if (hashSizeMB < 1)
		{
			hashSizeMB = 1;
		}
		int mainHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 6;
		int pawnHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 2;
		this.m_maxHashEntries = mainHashTableSize / RivalConstants.HASHPOSITION_SIZE_BYTES;
		this.m_maxPawnHashEntries = pawnHashTableSize / RivalConstants.HASHPOSITION_SIZE_BYTES;
		setHashTable();
	}	
	
	public void setBoard(EngineChessBoard engineBoard) 
	{
		this.m_board = engineBoard;
		this.m_hashTableVersion ++;
		this.m_pawnHashValuesStored = 0;
		this.m_pawnHashValuesRetrieved = 0;
		
		stopSearch();
		setHashTable();
	}
	
	public void newGame()
	{
		m_inBook = RivalConstants.USE_INTERNAL_OPENING_BOOK;
	}
	
	public void clearHash()
	{
		for (int i=0; i<m_maxHashEntries; i++)
		{
			this.hashTableHeight[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
			this.hashTableHeight[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
			this.hashTableAlways[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
			this.hashTableAlways[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
			if (RivalConstants.USE_PAWN_HASH)
			{
				this.pawnHashTable[i*RivalConstants.NUM_PAWNHASH_FIELDS+RivalConstants.PAWNHASHENTRY_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
			}
			if (RivalConstants.USE_EVAL_HASH)
			{
				this.evalHashTable[i*RivalConstants.NUM_PAWNHASH_FIELDS+RivalConstants.PAWNHASHENTRY_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
			}
		}
	}

	private void setHashTable()
	{
		if (m_maxHashEntries != m_lastHashSizeCreated)
		{
			this.hashTableHeight = new int[m_maxHashEntries*RivalConstants.NUM_HASH_FIELDS];
			this.hashTableAlways = new int[m_maxHashEntries*RivalConstants.NUM_HASH_FIELDS];
			if (RivalConstants.USE_PAWN_HASH)
			{
				this.pawnHashTable = new int[m_maxHashEntries*RivalConstants.NUM_PAWNHASH_FIELDS];
			}
			if (RivalConstants.USE_EVAL_HASH)
			{
				this.evalHashTable = new int[m_maxHashEntries*RivalConstants.NUM_PAWNHASH_FIELDS];
			}
			m_lastHashSizeCreated = m_maxHashEntries;
			for (int i=0; i<m_maxHashEntries; i++)
			{
				this.hashTableHeight[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
				this.hashTableHeight[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
				this.hashTableHeight[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_VERSION] = 1;
				this.hashTableAlways[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
				this.hashTableAlways[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
				this.hashTableAlways[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_VERSION] = 1;
				if (RivalConstants.USE_PAWN_HASH)
				{
					this.pawnHashTable[i*RivalConstants.NUM_PAWNHASH_FIELDS+RivalConstants.PAWNHASHENTRY_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
				}
				if (RivalConstants.USE_EVAL_HASH)
				{
					this.evalHashTable[i*RivalConstants.NUM_PAWNHASH_FIELDS+RivalConstants.PAWNHASHENTRY_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
				}
			}
		}
	}
	
	private int evaluateLoneKing(EngineChessBoard board, int materialScore)
	{
		int winningSide;
		int winningKing;
		int losingKing;
		int bishopColour = -1;
		int score = materialScore;

		if (score == 0) 
		{
			return 0;
		} 
		else if (score > 0) 
		{
			winningSide = RivalConstants.WHITE;
			winningKing = board.m_whiteKingSquare;
			losingKing = board.m_blackKingSquare;
			if (score==RivalConstants.VALUE_BISHOP && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB]) == 1) return 0;
			if (score==RivalConstants.VALUE_KNIGHT && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN]) == 1) return 0;
			if (score==RivalConstants.VALUE_KNIGHT * 2 && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN]) == 2) return 0;
			if (score==RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN]) == 1 && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB]) == 1) 
			{
				bishopColour = (board.m_pieceBitboards[RivalConstants.WB] & Bitboards.WHITE_SQUARES) != 0 ? RivalConstants.WHITE : RivalConstants.BLACK;
			}
		} 
		else 
		{
			score = -score; // score from point of view of winner, will be returned as point of view of mover at end
			winningSide = RivalConstants.BLACK;
			winningKing = board.m_blackKingSquare;
			losingKing = board.m_whiteKingSquare;
			if (score==RivalConstants.VALUE_BISHOP && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB]) == 1) return 0;
			if (score==RivalConstants.VALUE_KNIGHT && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN]) == 1) return 0;
			if (score==RivalConstants.VALUE_KNIGHT * 2 && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN]) == 2) return 0;
			if (score==RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN]) == 1 && Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB]) == 1) 
			{
				bishopColour = (board.m_pieceBitboards[RivalConstants.BB] & Bitboards.WHITE_SQUARES) != 0 ? RivalConstants.WHITE : RivalConstants.BLACK;
			}
		}

		// force enemy king to edge of board
		score += Bitboards.pieceSquareEnemyKingEndGame[losingKing];

		// If needed, send enemy king to correct bishop corner
		if (
				(bishopColour==RivalConstants.WHITE && (Bitboards.WHITE_QUADRANT & (1L << losingKing)) != 0) ||
			    (bishopColour==RivalConstants.BLACK && (Bitboards.WHITE_QUADRANT & (1L << losingKing)) == 0)
		    ) 
		{
			// double the punishment for being in the wrong quadrant
			score += Bitboards.pieceSquareEnemyKingEndGame[losingKing];
		}
		
		// send friendly king close to enemy king
		score += Bitboards.tropism[winningKing][losingKing] * RivalConstants.VALUE_LONEKING_TROPISM;

		// score is from winning side's point of view, so we need to invert if winning side is not the moving side 
		if (winningSide == RivalConstants.WHITE && board.m_isWhiteToMove || winningSide == RivalConstants.BLACK && !board.m_isWhiteToMove)
		{
			return score;
		}
		else
		{
			return -score;
		}		
	}
	
	private int evaluate(EngineChessBoard board, int low, int high)
	{
		this.m_nodes ++;
		
		// Piece values, except pawns
		int whitePieceValues =
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN]) * RivalConstants.VALUE_KNIGHT +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB]) * RivalConstants.VALUE_BISHOP +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WR]) * RivalConstants.VALUE_ROOK +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WQ]) * RivalConstants.VALUE_QUEEN;
		
		int blackPieceValues =
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN]) * RivalConstants.VALUE_KNIGHT +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB]) * RivalConstants.VALUE_BISHOP +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BR]) * RivalConstants.VALUE_ROOK +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BQ]) * RivalConstants.VALUE_QUEEN;
		
		int whitePawnValues = Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP]) * RivalConstants.VALUE_PAWN;
		int blackPawnValues = Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP]) * RivalConstants.VALUE_PAWN;
		
		int eval = whitePieceValues - blackPieceValues + whitePawnValues - blackPawnValues;
		
		if ((whitePieceValues == 0 || blackPieceValues == 0) && whitePawnValues + blackPawnValues == 0)
		{
			return evaluateLoneKing(board, eval);
		}
		
		if (RivalConstants.USE_EVAL_FUTILITY_TEST)
		{
			int futilityTest = board.m_isWhiteToMove ? eval : -eval;
			if (futilityTest + RivalConstants.EVAL_FUTILITY_WINDOW < low)
			{
				return futilityTest; 
			}
		}

		int sq;
		long bitboard;
		
		int whiteMinorsOnStartSquares =
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB] & Bitboards.WHITEBISHOPSTARTSQUARES) +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN] & Bitboards.WHITEKNIGHTSTARTSQUARES);
		
		int whiteMajorsOnStartSquares =
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WQ] & Bitboards.WHITEQUEENSTARTSQUARE) +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WR] & Bitboards.WHITEROOKSTARTSQUARES);
		
		int blackMinorsOnStartSquares =
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN] & Bitboards.BLACKKNIGHTSTARTSQUARES) +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB] & Bitboards.BLACKBISHOPSTARTSQUARES);
		
		int blackMajorsOnStartSquares =
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BQ] & Bitboards.BLACKQUEENSTARTSQUARE) +
			Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BR] & Bitboards.BLACKROOKSTARTSQUARES);
		
		int whitePieceSquares = 0, blackPieceSquares = 0;
		// opening specific stuff
		if (whiteMinorsOnStartSquares + whiteMajorsOnStartSquares + blackMinorsOnStartSquares + blackMajorsOnStartSquares > 7)
		{
			// opening penalties for unmoved pieces
			eval -= whiteMinorsOnStartSquares * RivalConstants.VALUE_OPENING_UNDEVELOPED_MINOR_PENALTY;
			eval += blackMinorsOnStartSquares * RivalConstants.VALUE_OPENING_UNDEVELOPED_MINOR_PENALTY;
			
			eval -= (board.m_pieceBitboards[RivalConstants.WQ] != Bitboards.WHITEQUEENSTARTSQUARE) ? RivalConstants.VALUE_QUEENEARLY_PENALTY : 0;
			eval += (board.m_pieceBitboards[RivalConstants.BQ] != Bitboards.BLACKQUEENSTARTSQUARE) ? RivalConstants.VALUE_QUEENEARLY_PENALTY : 0;
		}
		else
		{
			// if not opening, then evaluate for queen king tropism
			bitboard = board.m_pieceBitboards[RivalConstants.WQ];
			while (bitboard != 0)
			{
				if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
				if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
				if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
				sq = Bitboards.firstBit16[(int)(bitboard)];
				bitboard ^= (1L << sq);
				eval += RivalConstants.VALUE_QUEEN_TROPISM * Bitboards.tropism[board.m_blackKingSquare][sq];
				
				if ((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.KINGSIDE) != 0)
					whitePieceSquares += Bitboards.pieceSquareTableQueenKingsideCastle[sq];
				else
					whitePieceSquares += Bitboards.pieceSquareTableQueenQueensideCastle[sq]; 
			}
			
			bitboard = board.m_pieceBitboards[RivalConstants.BQ];
			while (bitboard != 0)
			{
				if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
				if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
				if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
				sq = Bitboards.firstBit16[(int)(bitboard)];
				bitboard ^= (1L << sq);
				eval -= RivalConstants.VALUE_QUEEN_TROPISM * Bitboards.tropism[board.m_whiteKingSquare][sq];
				
				if ((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.KINGSIDE) != 0)
					blackPieceSquares += Bitboards.pieceSquareTableQueenKingsideCastle[ChessBoardConversion.flipBitRefOnHorizontalAxis(sq)];
				else
					blackPieceSquares += Bitboards.pieceSquareTableQueenQueensideCastle[ChessBoardConversion.flipBitRefOnHorizontalAxis(sq)]; 
			}			
		}
		
		int numWhiteRooks = Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WR]);
		if (numWhiteRooks == 2)
		{
			whiteRookSquares = Bitboards.getSetBits(board.m_pieceBitboards[RivalConstants.WR]);
			if (whiteRookSquares[0] / 8 == whiteRookSquares[1] / 8) eval += RivalConstants.VALUE_ROOKS_ON_SAME_RANK; else
			if (whiteRookSquares[0] % 8 == whiteRookSquares[1] % 8) eval += RivalConstants.VALUE_ROOKS_ON_SAME_FILE;
		}
		int numBlackRooks = Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WR]);
		if (numBlackRooks == 2)
		{
			blackRookSquares = Bitboards.getSetBits(board.m_pieceBitboards[RivalConstants.WR]);
			if (blackRookSquares[0] / 8 == blackRookSquares[1] / 8) eval -= RivalConstants.VALUE_ROOKS_ON_SAME_RANK; else
			if (blackRookSquares[0] % 8 == blackRookSquares[1] % 8) eval -= RivalConstants.VALUE_ROOKS_ON_SAME_FILE;
		}
		
		if (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WR] & Bitboards.RANK_1) != 0)
		
		// bonus for the bishop pair
		eval += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB]) == 2 ? RivalConstants.VALUE_BISHOP_PAIR : 0;
		eval -= Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB]) == 2 ? RivalConstants.VALUE_BISHOP_PAIR : 0;
		
		// rook squares
		// bishops
		bitboard = board.m_pieceBitboards[RivalConstants.WR];
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			sq = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << sq);
			whitePieceSquares += Bitboards.pieceSquareTableRook[sq]; 
		}

		bitboard = board.m_pieceBitboards[RivalConstants.BR];
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			sq = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << sq);
			blackPieceSquares += Bitboards.pieceSquareTableRook[ChessBoardConversion.flipBitRefOnHorizontalAxis(sq)]; 
		}
		
		// bishop squares
		bitboard = board.m_pieceBitboards[RivalConstants.WB];
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			sq = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << sq);
			whitePieceSquares += Bitboards.pieceSquareTableBishop[sq]; 
		}

		bitboard = board.m_pieceBitboards[RivalConstants.BB];
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			sq = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << sq);
			blackPieceSquares += Bitboards.pieceSquareTableBishop[ChessBoardConversion.flipBitRefOnHorizontalAxis(sq)]; 
		}
		
		// knight king tropism
		bitboard = board.m_pieceBitboards[RivalConstants.WN];
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			sq = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << sq);
			eval += RivalConstants.VALUE_KNIGHT_TROPISM * Bitboards.tropism[board.m_blackKingSquare][sq];
			whitePieceSquares += Bitboards.pieceSquareTableKnight[sq]; 
		}
		
		bitboard = board.m_pieceBitboards[RivalConstants.BN];
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			sq = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << sq);
			eval -= RivalConstants.VALUE_KNIGHT_TROPISM * Bitboards.tropism[board.m_whiteKingSquare][sq];
			blackPieceSquares += Bitboards.pieceSquareTableKnight[ChessBoardConversion.flipBitRefOnHorizontalAxis(sq)]; 
		}
		
		if (whitePieceValues < RivalConstants.EVAL_ENDGAME_PIECES && blackPieceValues < RivalConstants.EVAL_ENDGAME_PIECES)
		{
			whitePieceSquares += Bitboards.pieceSquareTableKingEndGame[board.m_whiteKingSquare]; 
			blackPieceSquares += Bitboards.pieceSquareTableKingEndGame[ChessBoardConversion.flipBitRefOnHorizontalAxis(board.m_blackKingSquare)]; 

			if (whitePieceValues > blackPieceValues)
			{
				eval += Bitboards.tropism[board.m_whiteKingSquare][board.m_blackKingSquare] * RivalConstants.VALUE_KINGKING_TROPISM;
			}
			else
			if (whitePieceValues < blackPieceValues)
			{
				eval -= Bitboards.tropism[board.m_whiteKingSquare][board.m_blackKingSquare] * RivalConstants.VALUE_KINGKING_TROPISM;
			}
		}
		else
		{
			whitePieceSquares += Bitboards.pieceSquareTableKing[board.m_whiteKingSquare];
			blackPieceSquares += Bitboards.pieceSquareTableKing[ChessBoardConversion.flipBitRefOnHorizontalAxis(board.m_blackKingSquare)]; 

			// king safety
			int whiteKingSafety = 0;
			int blackKingSafety = 0;
			if ((board.m_pieceBitboards[RivalConstants.WK] & Bitboards.WHITE_KINGSAFETY_KINGSIDE_MASK) != 0)
			{
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.WHITE_KINGSAFETY_KINGSIDE_1) * RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION;
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.WHITE_KINGSAFETY_KINGSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION / 2);
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN] & Bitboards.WHITE_KINGSAFETY_KINGSIDE_2) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB] & Bitboards.WHITE_KINGSAFETY_KINGSIDE_1) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB] & Bitboards.WHITE_KINGSAFETY_KINGSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION / 2);
			}
			else
			if ((board.m_pieceBitboards[RivalConstants.WK] & Bitboards.WHITE_KINGSAFETY_QUEENSIDE_MASK) != 0)
			{
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.WHITE_KINGSAFETY_QUEENSIDE_1) * RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION;
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.WHITE_KINGSAFETY_QUEENSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION / 2);
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN] & Bitboards.WHITE_KINGSAFETY_QUEENSIDE_2) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB] & Bitboards.WHITE_KINGSAFETY_QUEENSIDE_1) * (RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION / 2);
				whiteKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB] & Bitboards.WHITE_KINGSAFETY_QUEENSIDE_2) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
			}
	
			if ((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.BLACK_KINGSAFETY_KINGSIDE_MASK) != 0)
			{
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.BLACK_KINGSAFETY_KINGSIDE_1) * RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION;
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.BLACK_KINGSAFETY_KINGSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION / 2);
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN] & Bitboards.BLACK_KINGSAFETY_KINGSIDE_2) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB] & Bitboards.BLACK_KINGSAFETY_KINGSIDE_1) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB] & Bitboards.BLACK_KINGSAFETY_KINGSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION / 2);
			}
			else
			if ((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.BLACK_KINGSAFETY_QUEENSIDE_MASK) != 0)
			{
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.BLACK_KINGSAFETY_QUEENSIDE_1) * RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION;
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.BLACK_KINGSAFETY_QUEENSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_PAWNPROTECTION / 2);
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN] & Bitboards.BLACK_KINGSAFETY_QUEENSIDE_2) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB] & Bitboards.BLACK_KINGSAFETY_QUEENSIDE_1) * RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION;
				blackKingSafety += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB] & Bitboards.BLACK_KINGSAFETY_QUEENSIDE_2) * (RivalConstants.VALUE_KINGSAFETY_MINORPROTECTION / 2);
			}
			
			// castling evaluation
			if (board.m_whiteHasCastled)
			{
				whiteKingSafety += RivalConstants.VALUE_OPENING_CASTLE_BONUS; 
			}
			else
			{
				if (board.m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] != 0L) whiteKingSafety += RivalConstants.VALUE_OPENING_QUEENSIDECASTLERIGHTS_BONUS;
				if (board.m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] != 0L) whiteKingSafety += RivalConstants.VALUE_OPENING_KINGSIDECASTLERIGHTS_BONUS;
			}
			if (board.m_blackHasCastled)
			{
				blackKingSafety += RivalConstants.VALUE_OPENING_CASTLE_BONUS; 
			}
			else
			{
				if (board.m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] != 0L) blackKingSafety += RivalConstants.VALUE_OPENING_QUEENSIDECASTLERIGHTS_BONUS;
				if (board.m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] != 0L) blackKingSafety += RivalConstants.VALUE_OPENING_KINGSIDECASTLERIGHTS_BONUS;
			}
			
			whiteKingSafety = (int)((blackPieceValues / 1000.0) * whiteKingSafety);
			blackKingSafety = (int)((whitePieceValues / 1000.0) * blackKingSafety);
			
			eval += whiteKingSafety - blackKingSafety;
		}
		
		eval += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN]) * RivalConstants.VALUE_KNIGHT_TROPISM;
		eval -= Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN]) * RivalConstants.VALUE_KNIGHT_TROPISM;
		
		eval += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN] & Bitboards.MIDDLE_OCCUPANCY) * RivalConstants.VALUE_MIDDLE_KNIGHT;
		eval += Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB] & Bitboards.MIDDLE_OCCUPANCY) * RivalConstants.VALUE_MIDDLE_BISHOP;
		eval -= Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN] & Bitboards.MIDDLE_OCCUPANCY) * RivalConstants.VALUE_MIDDLE_KNIGHT;
		eval -= Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB] & Bitboards.MIDDLE_OCCUPANCY) * RivalConstants.VALUE_MIDDLE_BISHOP;

		int pawnScore = RivalConstants.PAWNHASH_DEFAULT_SCORE;;
		if (RivalConstants.USE_PAWN_HASH)
		{
			long pawnHashValue = board.m_whitePawnHashValue ^ board.m_blackPawnHashValue;
			int pawnHashIndex = (int)(pawnHashValue % this.m_maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;
			if (this.pawnHashTable[pawnHashIndex+RivalConstants.PAWNHASHENTRY_LOCK1] == (int)(pawnHashValue >>> 32) &&
					this.pawnHashTable[pawnHashIndex+RivalConstants.PAWNHASHENTRY_LOCK2] == (int)(pawnHashValue & Bitboards.LOW32)
				)
			{
				pawnScore = this.pawnHashTable[pawnHashIndex+RivalConstants.PAWNHASHENTRY_SCORE];
				if (pawnScore != RivalConstants.PAWNHASH_DEFAULT_SCORE)
				{
					m_pawnHashValuesRetrieved ++;
				}
			}
		}
		
		if (pawnScore == RivalConstants.PAWNHASH_DEFAULT_SCORE)
		{
			pawnScore = 0;
			
			bitboard = board.m_pieceBitboards[RivalConstants.WP];
			while (bitboard != 0)
			{
				if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
				if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
				if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
				sq = Bitboards.firstBit16[(int)(bitboard)];
				bitboard ^= (1L << sq);
				pawnScore += Bitboards.pieceSquareTablePawn[sq]; 
				if (Bitboards.countSetBits(Bitboards.whitePassedPawnMask[sq] & board.m_pieceBitboards[RivalConstants.BP]) == 0)
				{
					pawnScore += RivalConstants.VALUE_PASSED_PAWN_BONUS * (sq / 8);
				}
				if (Bitboards.countSetBits(Bitboards.isolatedPawnMask[sq] & board.m_pieceBitboards[RivalConstants.WP]) == 1)
				{
					pawnScore += sq % 8 == 4 ? RivalConstants.VALUE_ISOLATED_DPAWN_PENALTY : RivalConstants.VALUE_ISOLATED_PAWN_PENALTY;
				}
				pawnScore += Bitboards.countSetBits(Bitboards.whitePawnMovesCapture[sq] & board.m_pieceBitboards[RivalConstants.WP]) * RivalConstants.VALUE_PAWN_SUPPORT;
			}
			
			bitboard = board.m_pieceBitboards[RivalConstants.BP];
			while (bitboard != 0)
			{
				if ((bitboard & 0xffff000000000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
				if ((bitboard & 0x0000ffff00000000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
				if ((bitboard & 0x00000000ffff0000L) != 0) sq = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
				sq = Bitboards.firstBit16[(int)(bitboard)];
				bitboard ^= (1L << sq);
				pawnScore -= Bitboards.pieceSquareTablePawn[ChessBoardConversion.flipBitRefOnHorizontalAxis(sq)]; 
				if (Bitboards.countSetBits(Bitboards.blackPassedPawnMask[sq] & board.m_pieceBitboards[RivalConstants.WP]) == 0)
				{
					pawnScore -= RivalConstants.VALUE_PASSED_PAWN_BONUS * (7-(sq / 8));
				}
				if (Bitboards.countSetBits(Bitboards.isolatedPawnMask[sq] & board.m_pieceBitboards[RivalConstants.BP]) == 1)
				{
					pawnScore -= sq % 8 == 3 ? RivalConstants.VALUE_ISOLATED_DPAWN_PENALTY : RivalConstants.VALUE_ISOLATED_PAWN_PENALTY;
				}
				pawnScore -= Bitboards.countSetBits(Bitboards.blackPawnMovesCapture[sq] & board.m_pieceBitboards[RivalConstants.BP]) * RivalConstants.VALUE_PAWN_SUPPORT;
			}
			
			pawnScore -= 
				(Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_A) + Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_H))
					* RivalConstants.VALUE_SIDE_PAWN_PENALTY;

			pawnScore += 
				(Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_A) + Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_H))
					* RivalConstants.VALUE_SIDE_PAWN_PENALTY;
			
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_A) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_B) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_C) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_D) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_E) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_F) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_G) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore -= (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_H) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_A) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_B) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_C) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_D) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_E) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_F) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_G) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			pawnScore += (Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_H) > 1) ? RivalConstants.VALUE_DOUBLED_PAWN_PENALTY : 0;
			
			if (RivalConstants.USE_PAWN_HASH)
			{
				m_pawnHashValuesStored ++;
				long pawnHashValue = board.m_whitePawnHashValue ^ board.m_blackPawnHashValue;
				int pawnHashIndex = (int)(pawnHashValue % this.m_maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;
				this.pawnHashTable[pawnHashIndex+RivalConstants.PAWNHASHENTRY_SCORE] = pawnScore;
				this.pawnHashTable[pawnHashIndex+RivalConstants.PAWNHASHENTRY_LOCK1] = (int)(pawnHashValue >>> 32);
				this.pawnHashTable[pawnHashIndex+RivalConstants.PAWNHASHENTRY_LOCK2] = (int)(pawnHashValue & Bitboards.LOW32);
			}
		}
		eval += pawnScore;
		eval += ((whitePieceSquares - blackPieceSquares) / RivalConstants.PIECE_SQUARE_IMPORTANCE_DIVISOR);
		
		if (Math.abs(whitePieceSquares - blackPieceSquares) > m_largestPieceSquareDifference)
		{
			m_largestPieceSquareDifference = Math.abs(whitePieceSquares - blackPieceSquares); 
		}
		
		if (RivalConstants.USE_SLIGHT_RANDOM_IN_EVALUATION)
		{
			eval += (int)(Math.random() * RivalConstants.EVAL_EVALUATION_RANDOM_SIZE);
		}
		return board.m_isWhiteToMove ? eval : -eval;
	}
	
	private void orderMoves(EngineChessBoard board, int[] moves, int orderedMoveIndex, int hashMove)
	{
		int offsetEnemy = board.m_isWhiteToMove ? 6 : 0;
		int offsetFriendly = board.m_isWhiteToMove ? 0 : 6;
		int i,j,score;
		int promotionMask = 0;
		int fromSquare, toSquare;
		int movePiece = -1, capturePiece = -1;
		
		boolean isCapture;
		long fromMask, toMask;
		
		for (i=0; moves[i] != 0; i++)
		{
			fromSquare = ((moves[i] >> 16) & 63);
			toSquare = (moves[i] & 63);
			fromMask = (1L << fromSquare);
			toMask = (1L << toSquare);
			isCapture = (board.m_pieceBitboards[RivalConstants.ALL] & toMask) != 0;
			promotionMask = (moves[i] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL);
			
			for (j=RivalConstants.WP; j<=RivalConstants.WR; j++) {
				if ((board.m_pieceBitboards[j + offsetFriendly] & fromMask) != 0) {
					movePiece = j; // will always be in range WP - WR as colour is not important
					break;
				}
			}

			if (isCapture) {
				for (j=RivalConstants.WP; j<=RivalConstants.WR; j++) {
					if ((board.m_pieceBitboards[j + offsetEnemy] & toMask) != 0) {
						capturePiece = j; // will always be in range WP - WR as colour is not important
						break;
					}
				}
			}

			score = 0;

			try {
			if (score == 0 && moves[i] == hashMove) score = 127; else
			if (score == 0 && isCapture && board.isHanging(toSquare)) score = 122 + m_sortPieceValues[capturePiece] - m_sortPieceValues[movePiece]; else
			if (score == 0 && isCapture && m_sortPieceValues[movePiece] < m_sortPieceValues[capturePiece]) score = 118 + m_sortPieceValues[capturePiece] - m_sortPieceValues[movePiece]; else
			if (score == 0 && isCapture && promotionMask == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) score = 117; else
			if (score == 0 && promotionMask == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) score = 116;
			if (score == 0 && isCapture && m_sortPieceValues[movePiece] == m_sortPieceValues[capturePiece]) score = 115; else
			if (score == 0 && RivalConstants.USE_KILLER_HEURISTIC)
			{
				for (j=0; j<RivalConstants.NUM_KILLER_MOVES; j++) {
					if (moves[i] == killerMoves[orderedMoveIndex][j]) {
						score = 108 - (4*j);
						break;
					}
					int searchDepth = orderedMoveIndex - (RivalConstants.MAX_EXTENSION_DEPTH + 1);
					if (searchDepth + 2 <= this.m_iterativeDeepeningCurrentDepth && moves[i] == killerMoves[orderedMoveIndex+2][j]) {
						score = 106 - (4*j);
						break;
					}
				}
			}
			} catch (Exception e)
			{
				board.printPreviousBoards();
				System.out.println(fromSquare + "-" + toSquare + " " + movePiece + " " + capturePiece);
				System.exit(0);
			}
			if (score == 0)
			{
				int newFrom, newTo;
				boolean isEnemyKingOnKingSide = true;
				
				if (board.m_isWhiteToMove)
				{
					newFrom = fromSquare;
					newTo = toSquare;
				}
				else
				{
					newFrom = ChessBoardConversion.flipBitRefOnHorizontalAxis(fromSquare);
					newTo = ChessBoardConversion.flipBitRefOnHorizontalAxis(toSquare);
				}
				switch (movePiece)
				{
					case RivalConstants.WP : 
						score = Bitboards.pieceSquareTablePawn[newTo] - Bitboards.pieceSquareTablePawn[newFrom];
						break; 
					case RivalConstants.WN : 
						score = Bitboards.pieceSquareTableKnight[newTo] - Bitboards.pieceSquareTableKnight[newFrom]; 
						break; 
					case RivalConstants.WB : 
						score = Bitboards.pieceSquareTableBishop[newTo] - Bitboards.pieceSquareTableBishop[newFrom];
						break; 
					case RivalConstants.WR : 
						score = Bitboards.pieceSquareTableRook[newTo] - Bitboards.pieceSquareTableRook[newFrom];
						break; 
					case RivalConstants.WQ :
						if (board.m_isWhiteToMove && (board.m_pieceBitboards[RivalConstants.BK] & Bitboards.QUEENSIDE) != 0) isEnemyKingOnKingSide = false; else
						if (!board.m_isWhiteToMove && (board.m_pieceBitboards[RivalConstants.WK] & Bitboards.QUEENSIDE) != 0) isEnemyKingOnKingSide = false;
						if (isEnemyKingOnKingSide) 
							score = Bitboards.pieceSquareTableQueenKingsideCastle[newTo] - Bitboards.pieceSquareTableQueenKingsideCastle[newFrom]; 
						else
							score = Bitboards.pieceSquareTableQueenQueensideCastle[newTo] - Bitboards.pieceSquareTableQueenQueensideCastle[newFrom]; 
						break; 
					case RivalConstants.WK : 
						int whitePieceValues =
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WN]) * RivalConstants.VALUE_KNIGHT +
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WB]) * RivalConstants.VALUE_BISHOP +
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WR]) * RivalConstants.VALUE_ROOK +
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.WQ]) * RivalConstants.VALUE_QUEEN;
						
						int blackPieceValues =
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BN]) * RivalConstants.VALUE_KNIGHT +
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BB]) * RivalConstants.VALUE_BISHOP +
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BR]) * RivalConstants.VALUE_ROOK +
							Bitboards.countSetBits(board.m_pieceBitboards[RivalConstants.BQ]) * RivalConstants.VALUE_QUEEN;
						
						if (whitePieceValues < RivalConstants.EVAL_ENDGAME_PIECES && blackPieceValues < RivalConstants.EVAL_ENDGAME_PIECES)
						{
							score = Bitboards.pieceSquareTableKingEndGame[newTo] - Bitboards.pieceSquareTableKingEndGame[newFrom];
						}
						else
						{
							score = Bitboards.pieceSquareTableKing[newTo] - Bitboards.pieceSquareTableKing[newFrom];
						}
						break; 
				}
				score += 50;
			}
			
			if (score < 0)
			{
				score = 0;
			}

			orderedMoves[orderedMoveIndex][i] = moves[i] | ((127-score) << 24);
		}
		
		orderedMoves[orderedMoveIndex][i] = 0;
		Arrays.sort(orderedMoves[orderedMoveIndex], 0, i);
		
		/*for (i=0; orderedMoves[depth][i] != 0; i++)
		{
			System.out.println(ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(orderedMoves[depth][i] & 0x00FFFFFF));
		}*/
	}
	
	public void storeHashMove(int move, EngineChessBoard board, int score, byte flag, int height)
	{
		int hashIndex = (int)(board.m_hashValue % this.m_maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;
		
		if (height >= this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_HEIGHT] || this.m_hashTableVersion > this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_VERSION])
		{
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_MOVE] = move;
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_SCORE] = score;
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_FLAG] = flag;
			if (RivalConstants.USE_SUPER_VERIFY_ON_HASH)
			{
				for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
				{
					this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_LOCK1+i] = (int)(board.m_pieceBitboards[i] >>> 32);
					this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_LOCK1+i+12] = (int)(board.m_pieceBitboards[i] & Bitboards.LOW32);
				}
			}
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_64BIT1] = (int)(board.m_hashValue >>> 32);
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_64BIT2] = (int)(board.m_hashValue & Bitboards.LOW32);
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_HEIGHT] = height;
			this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_VERSION] = this.m_hashTableVersion;
			this.m_heightHashValuesStored ++;
		}

		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_MOVE] = move;
		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_SCORE] = score;
		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_FLAG] = flag;
		if (RivalConstants.USE_SUPER_VERIFY_ON_HASH)
		{
			for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
			{
				this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_LOCK1+i] = (int)(board.m_pieceBitboards[i] >>> 32);
				this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_LOCK1+i+12] = (int)(board.m_pieceBitboards[i] & Bitboards.LOW32);
			}
		}
		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_64BIT1] = (int)(board.m_hashValue >>> 32);
		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_64BIT2] = (int)(board.m_hashValue & Bitboards.LOW32);
		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_HEIGHT] = height;
		this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_VERSION] = this.m_hashTableVersion;
		this.m_alwaysHashValuesStored ++;
	}
	
	public void storeEvalHash(int score, long hashValue)
	{
		if (RivalConstants.USE_EVAL_HASH)
		{
			m_evalHashValuesStored ++;
			int index = (int)(hashValue % this.m_maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;
			this.evalHashTable[index+RivalConstants.PAWNHASHENTRY_SCORE] = score;
			this.evalHashTable[index+RivalConstants.PAWNHASHENTRY_LOCK1] = (int)(hashValue >>> 32);
			this.evalHashTable[index+RivalConstants.PAWNHASHENTRY_LOCK2] = (int)(hashValue & Bitboards.LOW32);
		}
	}
	
	public int quiesce(EngineChessBoard board, int depth, int low, int high, int[] legalMoves)
	{
		if (board.previousOccurencesOfThisPosition() == 2)
		{
			return RivalConstants.DRAW_CONTEMPT;
		}
		
		int bestScore = RivalConstants.PAWNHASH_DEFAULT_SCORE; 
			
		if (RivalConstants.USE_EVAL_HASH)
		{
			int index = (int)(board.m_hashValue % this.m_maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;
			if (
					this.evalHashTable[index+RivalConstants.PAWNHASHENTRY_LOCK1] == (int)(board.m_hashValue >>> 32) &&
					this.evalHashTable[index+RivalConstants.PAWNHASHENTRY_LOCK2] == (int)(board.m_hashValue & Bitboards.LOW32)
					)
			{
				int score = this.evalHashTable[index+RivalConstants.PAWNHASHENTRY_SCORE];
				if (score != RivalConstants.PAWNHASH_DEFAULT_SCORE)
				{
					m_evalHashValuesRetrieved ++;
					bestScore = score;
				}
			}
		}
		
		if (bestScore == RivalConstants.PAWNHASH_DEFAULT_SCORE)
		{
			bestScore = evaluate(board, low, high);
		}

		if (depth == 0 || bestScore >= high)
		{
			storeEvalHash(bestScore, board.m_hashValue);
			return bestScore;
		}
		
		if (bestScore > low)
		{
			low = bestScore;
		}
		
		int score;
		int numMoves = 0;
		
		int orderedMoveIndex = depth + 3 + RivalConstants.MAX_EXTENSION_DEPTH + this.m_searchDepth;

		if (this.m_isDebug)
		{
			if (orderedMoveIndex < qLowIndex) qLowIndex = orderedMoveIndex;
			if (orderedMoveIndex > qHighIndex) qHighIndex = orderedMoveIndex;
		}

		int[] theseMoves = orderedMoves[orderedMoveIndex];
		int[] nextMoves = orderedMoves[orderedMoveIndex-1];
		
		orderMoves(board, legalMoves, orderedMoveIndex, 0);

		int move = theseMoves[0] & 0x00FFFFFF;;

		while (move != 0)
		{
			board.makeMove(move);
			
			if (board.wasQuiesceMoveAndMoverNotInCheck(nextMoves))
			{
				score = -quiesce(board, depth-1, -high, -low, nextMoves);
				if (score > bestScore)
				{
					bestScore = score;
				}
				if (score >= high)
				{
					board.unMakeMove();
					storeEvalHash(score, board.m_hashValue);
					return score;
				}
				if (score > low)
				{
					low = score;
				}
			}
			board.unMakeMove();
			numMoves ++;
			move = theseMoves[numMoves] & 0x00FFFFFF;;
		}
		
		storeEvalHash(bestScore, board.m_hashValue);
		return bestScore;
	}
	
	public SearchPath search(EngineChessBoard board, int depth, int low, int high, int[] legalMoves, int extensions)
	{
		if (System.currentTimeMillis() > this.m_searchTargetEndTime)
		{
			this.m_abortingSearch = true;
			return null;
		}

		if (extensions < RivalConstants.MAX_EXTENSION_DEPTH)
		{
			if (RivalConstants.USE_CHECK_EXTENSIONS && board.isCheck())
			{
				extensions++;
				checkExtensions++;
			}
			else
			if (RivalConstants.USE_RECAPTURE_EXTENSIONS && board.movesMade() >= 2)
			{
				int a = board.captureSquareAndPiece(1);
				int b = board.captureSquareAndPiece(2);
				if (a != -1 && a == b)
				{
					extensions++;
					recaptureExtensions++;
				}
			}
		}
		
		int numMoves = 0;
		int orderedMoveIndex;
		
		orderedMoveIndex = depth + RivalConstants.MAX_EXTENSION_DEPTH + 1;
		if (this.m_isDebug)
		{
			if (orderedMoveIndex < sLowIndex) sLowIndex = orderedMoveIndex;
			if (orderedMoveIndex > sHighIndex) sHighIndex = orderedMoveIndex;
		}

		SearchPath newPath;
		SearchPath bestPath = searchPath[orderedMoveIndex];
		bestPath.reset();
		
		//board.printBoard();
		if (board.previousOccurencesOfThisPosition() == 2)
		{
			bestPath.score = RivalConstants.DRAW_CONTEMPT;
			return bestPath;
		}
		
		if (depth == -extensions)
		{
			bestPath.score = quiesce(board, RivalConstants.MAX_QUIESCE_DEPTH-1, low, high, legalMoves);
			return bestPath;
		}

		byte flag = RivalConstants.UPPERBOUND;
		
		int hashIndex = (int)(board.m_hashValue % this.m_maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;
		int hashMove = 0;
		
		if (
					RivalConstants.USE_HASH_TABLES && 
					RivalConstants.USE_HEIGHT_REPLACE_HASH && 
					this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_HEIGHT] >= depth &&
					this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_FLAG] != RivalConstants.EMPTY ) {
			
			if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_64BIT1] == (int)(board.m_hashValue >>> 32) ||
					this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_64BIT2] == (int)(board.m_hashValue & Bitboards.LOW32))
			{
				boolean isLocked = this.m_hashTableVersion - this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_VERSION] <= RivalConstants.MAXIMUM_HASH_AGE;
				if (RivalConstants.USE_SUPER_VERIFY_ON_HASH)
				{
					for (int i=RivalConstants.WP; i<=RivalConstants.BR && isLocked; i++)
					{
						if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_LOCK1+i] != (int)(board.m_pieceBitboards[i] >>> 32) ||
							this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_LOCK1+i+12] != (int)(board.m_pieceBitboards[i] & Bitboards.LOW32))
							isLocked = false;
					}
				}
	
				if ( isLocked ) {
					this.m_heightHashValuesRetrieved ++;
					this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_VERSION] = this.m_hashTableVersion;
					hashMove = this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_MOVE];
					if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_FLAG] == RivalConstants.LOWERBOUND) {
						if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_SCORE] > low) {
							low = this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_SCORE];
						}
					} else
					if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_FLAG] == RivalConstants.UPPERBOUND) {
						if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_SCORE] < high)
						{
							high = this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_SCORE];
						}
					}
					
					if (this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_FLAG] == RivalConstants.EXACTSCORE || low > high) {
						bestPath.score = this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_SCORE];
						bestPath.setPath(hashMove);
						bestPath.setHeight(this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_HEIGHT]);
						return bestPath;
					}
				}
				else
				{
					this.m_heightBadClashes ++;
				}
			}
			else
			{
				m_heightHashClashes ++;
			}
		}

		if (
					RivalConstants.USE_HASH_TABLES && 
					RivalConstants.USE_ALWAYS_REPLACE_HASH && 
					hashMove == 0 && 
					this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_HEIGHT] >= depth &&
					this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_FLAG] != RivalConstants.EMPTY) {
			
			if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_64BIT1] == (int)(board.m_hashValue >>> 32) ||
					this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_64BIT2] == (int)(board.m_hashValue & Bitboards.LOW32))
			{
				boolean isLocked = this.m_hashTableVersion - this.hashTableHeight[hashIndex+RivalConstants.HASHENTRY_VERSION] <= RivalConstants.MAXIMUM_HASH_AGE;
				if (RivalConstants.USE_SUPER_VERIFY_ON_HASH)
				{
					for (int i=RivalConstants.WP; i<=RivalConstants.BR && isLocked; i++)
					{
						if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_LOCK1+i] != (int)(board.m_pieceBitboards[i] >>> 32) ||
							this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_LOCK1+i+12] != (int)(board.m_pieceBitboards[i] & Bitboards.LOW32))
							isLocked = false;
					}
				}
	
				if ( isLocked ) {
					this.m_alwaysHashValuesRetrieved ++;
					hashMove = this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_MOVE];
					if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_FLAG] == RivalConstants.LOWERBOUND) {
						if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_SCORE] > low) {
							low = this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_SCORE];
						}
					} else
					if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_FLAG] == RivalConstants.UPPERBOUND) {
						if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_SCORE] < high)
						{
							high = this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_SCORE];
						}
					}
					
					if (this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_FLAG] == RivalConstants.EXACTSCORE || low > high) {
						bestPath.score = this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_SCORE];
						bestPath.setPath(hashMove);
						bestPath.setHeight(this.hashTableAlways[hashIndex+RivalConstants.HASHENTRY_HEIGHT]);
						return bestPath;
					}
				}
				else
				{
					this.m_alwaysBadClashes ++;					
				}
			}
			else
			{
				m_alwaysHashClashes ++;
			}
		}
		
		orderMoves(board, legalMoves, orderedMoveIndex, hashMove);
		int moveCount = 0;
		
		int[] theseMoves = orderedMoves[orderedMoveIndex];
		int[] nextMoves = orderedMoves[orderedMoveIndex-1];
		
		int move = theseMoves[0] & 0x00FFFFFF;
		
		int bestMoveForHash = 0;
		boolean scoutSearch = false;
		
		if (RivalConstants.USE_NULLMOVE_PRUNING && depth >= RivalConstants.NULLMOVE_MINIMUM_DISTANCE_FROM_LEAF && !board.isOnNullMove())
		{
			boolean canMake = true;
			if (board.m_isWhiteToMove)
			{
				canMake = !((board.m_pieceBitboards[RivalConstants.WP] | board.m_pieceBitboards[RivalConstants.WK]) == board.m_pieceBitboards[RivalConstants.FRIENDLY]);
				canMake = canMake && 
					Bitboards.countSetBits(
							board.m_pieceBitboards[RivalConstants.WQ] | board.m_pieceBitboards[RivalConstants.WR] | 
							board.m_pieceBitboards[RivalConstants.WN] | board.m_pieceBitboards[RivalConstants.WB] |
							board.m_pieceBitboards[RivalConstants.WP]) > 1;
			}
			else
			{
				canMake = !((board.m_pieceBitboards[RivalConstants.BP] | board.m_pieceBitboards[RivalConstants.BK]) == board.m_pieceBitboards[RivalConstants.FRIENDLY]);
				canMake = canMake && 
				Bitboards.countSetBits(
						board.m_pieceBitboards[RivalConstants.BQ] | board.m_pieceBitboards[RivalConstants.BR] | 
						board.m_pieceBitboards[RivalConstants.BN] | board.m_pieceBitboards[RivalConstants.BB] |
						board.m_pieceBitboards[RivalConstants.BP]) > 1;
			}
				
			if (canMake)
			{
				board.makeNullMove();
				if (!board.isNonMoverInCheck(nextMoves))
				{
					newPath = search(board, (byte)(depth-RivalConstants.NULLMOVE_REDUCE_DEPTH-1), -high, -low, nextMoves, 0);
					if (!this.m_abortingSearch)
					{
						if (-newPath.score > high)
						{
							if (RivalConstants.STORE_NULLMOVE_CUTOFFS_IN_HASH)
							{
								storeHashMove(0, board, -newPath.score, RivalConstants.LOWERBOUND, depth-RivalConstants.NULLMOVE_REDUCE_DEPTH);
							}
							bestPath.score = -newPath.score;
							board.unMakeNullMove();
							return bestPath;
						}
					}
				}
				board.unMakeNullMove();
			}
		}
		
		while (theseMoves[numMoves] != 0 && !this.m_abortingSearch)
		{
			board.makeMove(move);
			
			if (!board.isNonMoverInCheck(nextMoves))
			{
				moveCount++;
				// m_board.m_legalMoves is set inside isNonMoverInCheck()
				if (scoutSearch)
				{
					newPath = search(m_board, (byte)(depth-1), -low-1, -low, nextMoves, extensions);
					if (!this.m_abortingSearch && -newPath.score > low)
					{
						newPath = search(m_board, (byte)(depth-1), -high, -low, nextMoves, extensions);
					}
				}
				else
				{
					newPath = search(board, (byte)(depth-1), -high, -low, nextMoves, extensions);
				}
				
				if (!this.m_abortingSearch)
				{
					newPath.score = -newPath.score;
					
					if (newPath.score >= high)
					{
						board.unMakeMove();
						bestPath.setPath(move, newPath);
						storeHashMove(move, board, newPath.score, RivalConstants.LOWERBOUND, depth);
						if (RivalConstants.USE_KILLER_HEURISTIC)
						{
							if ((board.m_pieceBitboards[RivalConstants.ENEMY] & (move & 63)) == 0 || (move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == 0)
							{
								// if this move is in second place, or if it's not in the table at all,
								// then move first to second, and replace first with this move
								if (killerMoves[orderedMoveIndex][0] != move)
								{
									for (int j=RivalConstants.NUM_KILLER_MOVES-1; j>0; j--)
									{
										killerMoves[orderedMoveIndex][j] = killerMoves[orderedMoveIndex][j-1];
									}
									killerMoves[orderedMoveIndex][0] = move;
								}
							}
						}
						return bestPath;
					}
					if (newPath.score > bestPath.score)
					{
						bestPath.setPath(move, newPath);
					}
					if (newPath.score > low)
					{
						flag = RivalConstants.EXACTSCORE;
						bestMoveForHash = move;
						low = newPath.score;
						scoutSearch = RivalConstants.USE_PV_SEARCH && depth >= RivalConstants.PV_MINIMUM_DISTANCE_FROM_LEAF;
					}
				}
			}
			board.unMakeMove();
			numMoves ++;
			move = theseMoves[numMoves] & 0x00FFFFFF;;
		}
		
		if (!this.m_abortingSearch)
		{
			if (RivalConstants.RETURN_ALPHA_INSTEAD_OF_BESTSCORE)
			{
				bestPath.score = low;
			}			
			if (moveCount == 0)
			{
				flag = RivalConstants.EXACTSCORE;
				boolean isMate = 
						board.m_isWhiteToMove ? 
								board.isSquareAttacked(board.m_whiteKingSquare, false) : 
								board.isSquareAttacked(board.m_blackKingSquare, true); 
				if (isMate)
				{
					bestPath.score = -RivalConstants.VALUE_MATE + (this.m_iterativeDeepeningCurrentDepth - depth);
				}
				else
				{
					bestPath.score = 0;
				}
			}
			storeHashMove(bestMoveForHash, board, bestPath.score, (byte)flag, depth);
			return bestPath;
		}
		else
		{
			return null;
		}
	}

	public SearchPath searchZero(EngineChessBoard board, byte depth, int low, int high)
	{
		int numMoves = 0;
		byte flag = RivalConstants.UPPERBOUND;
		int move;
		int bestMoveForHash = 0;
		
		move = orderedMoves[0][numMoves] & 0x00FFFFFF; // clear sort score

		SearchPath newPath;
		SearchPath bestPath = searchPath[0];
		bestPath.reset();
		
		int numLegalMovesAtDepthZero = 0;
		
		if (this.m_isDebug)
		{
			zLowIndex = 0;
			zHighIndex = 1;
		}

		boolean scoutSearch = false;
		
		while (move != 0 && !this.m_abortingSearch)
		{
			m_board.makeMove(move);
			if (!m_board.isNonMoverInCheck(orderedMoves[this.m_searchDepth+1]))
			{
				numLegalMovesAtDepthZero ++;

				m_currentDepthZeroMove = move;
				m_currentDepthZeroMoveNumber = numLegalMovesAtDepthZero;
				
				if (scoutSearch)
				{
					newPath = search(m_board, (byte)(depth-1), -low-1, -low, orderedMoves[this.m_searchDepth+1], 0);
					if (!this.m_abortingSearch && -newPath.score > low)
					{
						newPath = search(m_board, (byte)(depth-1), -high, -low, orderedMoves[this.m_searchDepth+1], 0);
					}
				}
				else
				{
					newPath = search(m_board, (byte)(depth-1), -high, -low, orderedMoves[this.m_searchDepth+1], 0);
				}
				if (!this.m_abortingSearch)
				{
					newPath.score = -newPath.score;
					
					if (newPath.score >= high)
					{
						board.unMakeMove();
						bestPath.setPath(move, newPath);
						storeHashMove(move, board, newPath.score, RivalConstants.LOWERBOUND, depth);
						depthZeroMoveScores[numMoves] = newPath.score;
						return bestPath;
					}

					if (newPath.score > bestPath.score)
					{
						bestPath.setPath(move, newPath); 
					}

					if (newPath.score > low)
					{
						flag = RivalConstants.EXACTSCORE;
						bestMoveForHash = move;
						low = newPath.score;
						
						scoutSearch = RivalConstants.USE_PV_SEARCH && depth >= RivalConstants.PV_MINIMUM_DISTANCE_FROM_LEAF;
						m_currentPath.setPath(bestPath);
					}					
					
					depthZeroMoveScores[numMoves] = newPath.score;
				}
			}
			else
			{
				depthZeroMoveScores[numMoves] = -RivalConstants.INFINITY;
			}
			m_board.unMakeMove();
			numMoves ++;
			move = orderedMoves[0][numMoves] & 0x00FFFFFF;;
		}
		
		if (!this.m_abortingSearch)
		{
			if (numLegalMovesAtDepthZero == 1)
			{
				this.m_abortingSearch = true;
				m_currentPath.setPath(bestPath); // otherwise we will crash!
			}
			else
			{
				storeHashMove(bestMoveForHash, board, bestPath.score, flag, depth);
			}
			
			if (RivalConstants.RETURN_ALPHA_INSTEAD_OF_BESTSCORE)
			{
				bestPath.score = low;
			}
			
			return bestPath;
		}
		else
		{
			return null;
		}
	}
	
	public void go() 
	{
		this.m_hashTableVersion ++;

		this.m_searchState = RivalConstants.SEARCHSTATE_SEARCHSTARTED;
		this.m_searchStartTime = System.currentTimeMillis();
		this.m_searchEndTime = -1;
		this.m_searchTargetEndTime = this.m_searchStartTime + this.m_millisecondsToThink;
		this.m_abortingSearch = false;

		this.m_heightHashClashes = 0;
		this.m_alwaysHashClashes = 0;
		this.m_alwaysHashValuesRetrieved = 0;
		this.m_heightHashValuesRetrieved = 0;
		this.m_alwaysHashValuesStored = 0;
		this.m_heightHashValuesStored = 0;
		this.m_pawnHashValuesStored = 0;
		this.m_pawnHashValuesStored = 0;
		this.m_evalHashValuesStored = 0;
		this.m_evalHashValuesStored = 0;
		
		int aspirationLow = -RivalConstants.INFINITY;
		int aspirationHigh = RivalConstants.INFINITY;
		this.m_nodes = 0;
		
		SearchPath path;
		
		try
		{
			m_board.setLegalMoves(depthZeroLegalMoves);
			int depthZeroMoveCount = 0;

			if (this.m_isDebug)
			{
				m_board.printLegalMoves();
				System.out.println("Is Check = " + m_board.isCheck());
				System.out.println("Eval = " + evaluate(m_board, -RivalConstants.INFINITY, RivalConstants.INFINITY));
				System.out.println("Quiesce = " + quiesce(m_board, 40, -RivalConstants.INFINITY, RivalConstants.INFINITY, depthZeroLegalMoves));
				System.out.println("FEN = " + m_board.getFen());
				System.out.println("Num White Attacks on D4 = " + m_board.countAttackersWithXRays(28, true));
				System.out.println("Num Black Attacks on D4 = " + m_board.countAttackersWithXRays(28, false));
			}
			
			if (this.m_inBook)
			{
				OpeningLibrary library = new OpeningLibrary();
				int libraryMove = library.getMove(m_board.getFen());
				if (libraryMove > 0 && m_board.isMoveLegal(libraryMove))
				{
					path = new SearchPath();
					path.setPath(libraryMove);
					m_currentPath = path;
					setSearchComplete();
					return;
				}
				else
				{
					this.m_inBook = false;
				}
			}
			
			while (depthZeroLegalMoves[depthZeroMoveCount] != 0) depthZeroMoveCount++;
			
			orderMoves(m_board, depthZeroLegalMoves, 0, 0);
			
			for (byte depth=1; depth<=this.m_searchDepth && !this.m_abortingSearch; depth++)
			{
				this.m_iterativeDeepeningCurrentDepth = depth;
				
				if (depth == 2)
				{
					depth = 2;
				}
				
				if (RivalConstants.USE_ASPIRATION_WINDOW)
				{
					path = searchZero(m_board, depth, aspirationLow, aspirationHigh);
					
					if (!this.m_abortingSearch && path.score <= aspirationLow)
					{
						path = searchZero(m_board, depth, -RivalConstants.INFINITY, aspirationHigh);					
					}
					else
					if (!this.m_abortingSearch && path.score >= aspirationHigh)
					{
						path = searchZero(m_board, depth, aspirationLow, RivalConstants.INFINITY);					
					}

					if (!this.m_abortingSearch && (path.score <= aspirationLow || path.score >= aspirationHigh))
					{
						path = searchZero(m_board, depth, -RivalConstants.INFINITY, RivalConstants.INFINITY);					
					}

					if (!this.m_abortingSearch)
					{
						m_currentPath.setPath(path);
						aspirationLow = path.score - RivalConstants.ASPIRATION_RADIUS;
						aspirationHigh = path.score + RivalConstants.ASPIRATION_RADIUS;
					}
				}
				else
				{
					path = searchZero(m_board, depth, -RivalConstants.INFINITY, RivalConstants.INFINITY);
				}
				
				if (!this.m_abortingSearch)
				{
					m_currentPath.setPath(path);
					if (path.score > RivalConstants.VALUE_MATE - 200)
					{
						setSearchComplete();
						return;
					}
				    for (int pass=1; pass < depthZeroMoveCount; pass++) 
				    {
				        for (int i=0; i < depthZeroMoveCount-pass; i++) 
				        {
				            if (depthZeroMoveScores[i] < depthZeroMoveScores[i+1]) 
				            {
				                int tempScore;
				                tempScore = depthZeroMoveScores[i];  
				                depthZeroMoveScores[i] = depthZeroMoveScores[i+1];  
				                depthZeroMoveScores[i+1] = tempScore;
				                
				                int tempMove;
				                tempMove = orderedMoves[0][i];
				                orderedMoves[0][i] = orderedMoves[0][i+1];
				                orderedMoves[0][i+1] = tempMove;
				            }
				        }
				    }
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			setSearchComplete();
		}
	}
	
	public void setSearchComplete()
	{
		this.m_searchEndTime = System.currentTimeMillis();
		this.m_searchState = RivalConstants.SEARCHSTATE_SEARCHCOMPLETE;
	}

	public int getEngineState()
	{
		return this.m_searchState;
	}

	public void setMillisToThink(int millisToThink) 
	{
		this.m_millisecondsToThink = millisToThink;
		
		if (this.m_millisecondsToThink < 1000)
		{
			this.m_millisecondsToThink = 1000;
		}
	}

	public void setSearchDepth(int searchDepth) 
	{
		this.m_searchDepth = searchDepth;
	}

	public int getSearchDepth() 
	{
		return this.m_searchDepth;
	}

	public int getNodes() 
	{
		return this.m_nodes;
	}
	
	public boolean isSearchComplete()
	{
		return this.m_searchEndTime != -1;
	}
	
	public int getCurrentScore()
	{
		return this.m_currentPath.score;
	}
	
	public long getSearchDuration()
	{
		long timePassed;
		if (this.m_searchEndTime == -1)
		{
		   if (this.m_searchStartTime == -1)
		   {
			   timePassed = 0;
		   }
		   else
		   {
			   timePassed = System.currentTimeMillis() - this.m_searchStartTime;
		   }
		}
		else
		{
			timePassed = this.m_searchEndTime - this.m_searchStartTime;
		}
		return timePassed;
	}
	
	public int getNodesPerSecond()
	{
		long timePassed = getSearchDuration();
		if (timePassed == 0)
		{
			return 0;
		}
		else
		{
			return (int)(((double)this.m_nodes / (double)timePassed) * 1000.0);
		}
	}
	
	public int getIterativeDeepeningDepth()
	{
		return this.m_iterativeDeepeningCurrentDepth;
	}
	
	public int getCurrentMove()
	{
		return this.m_currentPath.move[0];
	}
	
	public long getSearchStartTime()
	{
		return this.m_searchStartTime;
	}
	
	public void stopSearch()
	{
		this.m_abortingSearch = true;
	}

	public long getSearchEndTime()
	{
		return this.m_searchEndTime;
	}
	
	public void setDebug(boolean isDebug)
	{
		this.m_isDebug = isDebug;
	}
	
	public void printHashPopulationStats()
	{
		NumberFormat nf = NumberFormat.getInstance();
		int alwaysCount = 0;
		int heightCount = 0;
		for (int i=0; i<m_maxHashEntries; i++)
		{
			if (hashTableAlways[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_FLAG] != RivalConstants.EMPTY)
			{
				alwaysCount ++;
			}
			if (hashTableHeight[i*RivalConstants.NUM_HASH_FIELDS+RivalConstants.HASHENTRY_FLAG] != RivalConstants.EMPTY)
			{
				heightCount ++;
			}
		}
		System.out.println("Height replace retrieved " + nf.format(m_heightHashValuesRetrieved) + " / " + nf.format(m_heightHashValuesStored)); 
		System.out.println("Always replace retrieved " + nf.format(m_alwaysHashValuesRetrieved) + " / " + nf.format(m_alwaysHashValuesStored)); 
		
		System.out.println("Height replace hash table population " + nf.format(heightCount) + " / " + nf.format(m_maxHashEntries) + " with " + nf.format(m_heightHashClashes) + " clashes and " + nf.format(m_heightBadClashes) + " bad clashes");
		System.out.println("Always replace hash table population " + nf.format(alwaysCount) + " / " + nf.format(m_maxHashEntries) + " with " + nf.format(m_alwaysHashClashes) + " clashes and " + nf.format(m_alwaysBadClashes) + " bad clashes");
	}
	
	public void run()
	{
	}
}
