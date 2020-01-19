package com.netsensia.rivalchess.engine.core;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.bitboards.MagicBitboards;
import com.netsensia.rivalchess.constants.MoveOrder;
import com.netsensia.rivalchess.engine.test.epd.EPDPosition;
import com.netsensia.rivalchess.uci.EngineMonitor;
import com.netsensia.rivalchess.util.ChessBoardConversion;
import com.netsensia.rivalchess.util.Logger;
import com.netsensia.rivalchess.util.Numbers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

public final class RivalSearch implements Runnable {
    private PrintStream printStream;

    private boolean isOkToSendInfo = false;
    private PrintWriter m_out;

    private boolean m_wasSearchCancelled = false;

    private int nodes = 0;
    private long currentTimeMillis;

    private List<List<Long>> drawnPositionsAtRoot;

    private int[] drawnPositionsAtRootCount = new int[2];

    private int aspirationLow, aspirationHigh;

    private boolean quit = false;

    private int movesToSearchAtAllDepths = 0;
    private int totalMovesSearchedAtAllDepths = 0;

    public int m_futilityPrunes = 0;
    public int m_alwaysHashClashes = 0;
    public int m_alwaysBadClashes = 0;
    public int m_alwaysHashValuesRetrieved = 0;
    public int m_alwaysHashValuesStored = 0;
    public int m_heightHashClashes = 0;
    public int m_heightBadClashes = 0;
    public int m_heightHashValuesRetrieved = 0;
    public int m_heightHashValuesStored = 0;
    public int m_pawnHashValuesRetrieved = 0;
    public int m_pawnQuickValuesRetrieved = 0;
    public int m_pawnHashValuesStored = 0;
    public int m_evalHashValuesRetrieved = 0;
    public int m_evalHashValuesStored = 0;

    public int m_zugzwangCount = 0;
    public int m_largestEvalDifference = 0;

    public EPDPosition m_epdPosition = null;

    public EngineChessBoard m_board;
    protected int m_millisecondsToThink;
    protected int m_nodesToSearch = Integer.MAX_VALUE;

    protected boolean m_abortingSearch = true;
    public long m_searchStartTime = -1, m_searchTargetEndTime, m_searchEndTime = 0;
    protected int m_finalDepthToSearch = 1;
    protected int m_previousFinalDepthToSearch = -1;
    protected long m_previousSearchMillis = -1;
    protected int m_iterativeDeepeningCurrentDepth = 0; // current search depth for iterative deepening

    private OpeningLibrary m_openingLibrary;

    private int[][] killerMoves;
    private int[] mateKiller;
    private int[][][] historyMovesSuccess = new int[2][64][64];
    private int[][][] historyMovesFail = new int[2][64][64];
    private int[][][] historyPruneMoves = new int[2][64][64];

    private boolean m_useOpeningBook = RivalConstants.USE_INTERNAL_OPENING_BOOK;
    private boolean m_inBook = m_useOpeningBook;

    public int checkExtensions = 0;
    public int threatExtensions = 0;
    public int immediateThreatExtensions = 0;
    public int pawnExtensions = 0;
    public int lateMoveReductions = 0;
    public int lateMoveDoubleReductions = 0;
    public int recaptureExtensions = 0;
    public int recaptureExtensionAttempts = 0;

    private int m_searchState;
    private int m_hashTableVersion = 1;
    private int[] hashTableHeight;
    private int[] hashTableAlways;
    private long[] pawnHashTable;
    public int m_maxHashEntries;
    private int m_maxPawnHashEntries;
    private int m_lastHashSizeCreated;

    public int m_currentDepthZeroMove;
    public int m_currentDepthZeroMoveNumber;

    public SearchPath m_currentPath;
    private String m_currentPathString;

    private int[][] orderedMoves;
    private SearchPath[] searchPath;

    private int[] depthZeroLegalMoves;
    private int[] depthZeroMoveScores;

    private long[][] profiler;
    private String[] profilerDescs;

    private boolean m_isUCIMode = false;

    private static byte[] rivalKPKBitbase = null;

    public RivalSearch() {
        this(System.out);
    }

    public RivalSearch(PrintStream printStream) {

        drawnPositionsAtRoot = new ArrayList<>();
        drawnPositionsAtRoot.add(new ArrayList<>());
        drawnPositionsAtRoot.add(new ArrayList<>());

        this.printStream = printStream;

        this.setCurrentTimeMillis(System.currentTimeMillis());

        this.m_currentPath = new SearchPath();
        this.m_currentPathString = "";

        this.m_searchState = RivalConstants.SEARCHSTATE_READY;

        this.m_hashTableVersion = 0;

        this.searchPath = new SearchPath[RivalConstants.MAX_TREE_DEPTH];
        this.killerMoves = new int[RivalConstants.MAX_TREE_DEPTH][RivalConstants.NUM_KILLER_MOVES];
        this.mateKiller = new int[RivalConstants.MAX_TREE_DEPTH];
        for (int i = 0; i < RivalConstants.MAX_TREE_DEPTH; i++) {
            this.searchPath[i] = new SearchPath();
            this.killerMoves[i] = new int[RivalConstants.NUM_KILLER_MOVES];
        }

        orderedMoves = new int[RivalConstants.MAX_TREE_DEPTH][RivalConstants.MAX_LEGAL_MOVES];

        depthZeroLegalMoves = orderedMoves[0];
        depthZeroMoveScores = new int[RivalConstants.MAX_LEGAL_MOVES];

        setHashSizeMB(RivalConstants.DEFAULT_HASHTABLE_SIZE_MB);

        if (RivalConstants.PROFILING) {
            profilerDescs = new String[RivalConstants.PROFILE_SLOTS];
            profiler = new long[RivalConstants.PROFILE_SLOTS][RivalConstants.PROFILE_DATA_COUNT];
            for (int i = 0; i < RivalConstants.PROFILE_SLOTS; i++) {
                profiler[i][RivalConstants.PROFILE_COUNT] = 0;
                profiler[i][RivalConstants.PROFILE_TOTALTIME] = 0;
            }
        }

        int byteArraySize = (64 * 48 * 32 * 2) / 8;

        if (!RivalConstants.IS_ANDROID_VERSION) {
            rivalKPKBitbase = new byte[byteArraySize];
            loadBitbase(rivalKPKBitbase, byteArraySize, "/kpk.rival");
        }

        if (RivalConstants.USE_INTERNAL_OPENING_BOOK) m_openingLibrary = new OpeningLibrary();
    }

    public synchronized void quit() {
        quit = true;
    }

    private void loadBitbase(byte[] byteArray, int byteArraySize, String fileLoc) {
        InputStream inStream;
        inStream = getClass().getResourceAsStream(fileLoc);
        try {
            int fileSize = inStream.read(byteArray);
            if (fileSize != byteArraySize) {
                printStream.println("Error: " + fileLoc + " file size is " + fileSize);
                System.exit(0);
            }
            inStream.close();
        } catch (IOException e) {
            printStream.println("Could not load " + fileLoc + " file");
            System.exit(0);
        }
    }

    public void setEPDPosition(EPDPosition position) {
        this.m_epdPosition = position;
    }

    public void startEngineTimer(boolean isUCIMode) {
        this.m_isUCIMode = isUCIMode;
        EngineMonitor m_monitor = new EngineMonitor(this);
        new Timer().schedule(m_monitor, RivalConstants.UCI_TIMER_INTERVAL_MILLIS, RivalConstants.UCI_TIMER_INTERVAL_MILLIS);
    }

    public boolean isUCIMode() {
        return this.m_isUCIMode;
    }

    public void showProfileTimes() {
        if (RivalConstants.PROFILING) {
            for (int i = 0; i < RivalConstants.PROFILE_SLOTS; i++) {
                if (profiler[i][RivalConstants.PROFILE_COUNT] > 0) {
                    NumberFormat f = NumberFormat.getInstance();
                    f.setMaximumFractionDigits(10);
                    f.setMinimumFractionDigits(10);
                    double average = ((double) profiler[i][RivalConstants.PROFILE_TOTALTIME] / profiler[i][RivalConstants.PROFILE_COUNT]) / 1000.0;
                    printStream.println("Slot " + i + " = " + (profiler[i][RivalConstants.PROFILE_TOTALTIME] / 1000.0) + "/" + profiler[i][RivalConstants.PROFILE_COUNT] + " = " + f.format(average) + " (" + profilerDescs[i] + ")");
                }
            }
        }
    }

    public synchronized void setHashSizeMB(int hashSizeMB) {
        if (hashSizeMB < 1) {
            this.m_maxHashEntries = 1;
            this.m_maxPawnHashEntries = 1;
        } else {
            int mainHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 6; // two of these
            int pawnHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 2; // one of these
            this.m_maxHashEntries = mainHashTableSize / RivalConstants.HASHPOSITION_SIZE_BYTES;
            this.m_maxPawnHashEntries = pawnHashTableSize / RivalConstants.PAWNHASHENTRY_SIZE_BYTES;
        }

        setHashTable();
    }

    public synchronized void setBoard(EngineChessBoard engineBoard) {
        this.m_board = engineBoard;
        this.m_hashTableVersion++;

        setHashTable();
    }

    public synchronized void newGame() {
        if (RivalConstants.UCI_DEBUG) {
            printStream.println("Getting ready for a new game");
        }
        m_inBook = this.m_useOpeningBook;
        clearHash();
    }

    public synchronized void clearHash() {
        for (int i = 0; i < m_maxHashEntries; i++) {
            this.hashTableHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
            this.hashTableHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
            this.hashTableAlways[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
            this.hashTableAlways[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
            if (RivalConstants.USE_PAWN_HASH) {
                this.pawnHashTable[i * RivalConstants.NUM_PAWNHASH_FIELDS + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
            }
        }
    }

    private synchronized void setHashTable() {
        if (m_maxHashEntries != m_lastHashSizeCreated) {
            this.hashTableHeight = new int[m_maxHashEntries * RivalConstants.NUM_HASH_FIELDS];
            this.hashTableAlways = new int[m_maxHashEntries * RivalConstants.NUM_HASH_FIELDS];
            if (RivalConstants.USE_PAWN_HASH) {
                this.pawnHashTable = new long[m_maxHashEntries * RivalConstants.NUM_PAWNHASH_FIELDS];
            }
            m_lastHashSizeCreated = m_maxHashEntries;
            for (int i = 0; i < m_maxHashEntries; i++) {
                this.hashTableHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
                this.hashTableHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_VERSION] = 1;
                this.hashTableAlways[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
                this.hashTableAlways[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableAlways[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_VERSION] = 1;
                if (RivalConstants.USE_PAWN_HASH) {
                    this.pawnHashTable[i * RivalConstants.NUM_PAWNHASH_FIELDS + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
                }
            }
        }
    }

    long lastPawnHashValue = -1;
    int lastPawnScore = 0;
    int lastWhitePassedPawnScore = 0;
    int lastBlackPassedPawnScore = 0;
    long lastWhitePassedPawns = 0;
    long lastBlackPassedPawns = 0;

    private int getPawnScore(EngineChessBoard board, int totalPieceScore) {
        int pawnScore = RivalConstants.PAWNHASH_DEFAULT_SCORE;
        int whitePassedPawnScore = RivalConstants.PAWNHASH_DEFAULT_SCORE;
        int blackPassedPawnScore = RivalConstants.PAWNHASH_DEFAULT_SCORE;
        long whitePassedPawns = 0, blackPassedPawns = 0;

        final long pawnHashValue = board.m_pawnHashValue;

        final int pawnHashIndex = (int) (pawnHashValue % this.m_maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;

        if (RivalConstants.USE_PAWN_HASH) {
            if (RivalConstants.USE_QUICK_PAWN_HASH_RETURN && lastPawnHashValue == pawnHashValue) {
                pawnScore = lastPawnScore;
                whitePassedPawnScore = lastWhitePassedPawnScore;
                blackPassedPawnScore = lastBlackPassedPawnScore;
                whitePassedPawns = lastWhitePassedPawns;
                blackPassedPawns = lastBlackPassedPawns;
                m_pawnQuickValuesRetrieved++;
            } else {
                if (this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_LOCK] == pawnHashValue) {
                    pawnScore = (int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_MAIN_SCORE];
                    if (pawnScore != RivalConstants.PAWNHASH_DEFAULT_SCORE) {
                        whitePassedPawnScore = (int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE];
                        blackPassedPawnScore = (int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE];
                        whitePassedPawns = this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWNS];
                        blackPassedPawns = this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWNS];
                        m_pawnHashValuesRetrieved++;
                    }
                }
            }
        }

        if (pawnScore == RivalConstants.PAWNHASH_DEFAULT_SCORE) {
            final long whitePawnAttacks = ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_A) << 9) | ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_H) << 7);
            final long blackPawnAttacks = ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_A) >>> 7) | ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_H) >>> 9);
            final long whitePawnFiles = Bitboards.southFill(board.m_pieceBitboards[RivalConstants.WP]) & Bitboards.RANK_1;
            final long blackPawnFiles = Bitboards.southFill(board.m_pieceBitboards[RivalConstants.BP]) & Bitboards.RANK_1;

            pawnScore = 0;

            whitePassedPawns =
                    board.m_pieceBitboards[RivalConstants.WP] &
                            ~Bitboards.southFill(board.m_pieceBitboards[RivalConstants.BP] | blackPawnAttacks | (board.m_pieceBitboards[RivalConstants.WP] >>> 8));

            final long whiteGuardedPassedPawns = whitePassedPawns & (((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_A) << 9) |
                    ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_H) << 7));

            blackPassedPawns =
                    board.m_pieceBitboards[RivalConstants.BP] &
                            ~Bitboards.northFill(board.m_pieceBitboards[RivalConstants.WP] | whitePawnAttacks | (board.m_pieceBitboards[RivalConstants.BP] << 8));

            long blackGuardedPassedPawns = blackPassedPawns & (((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_A) >>> 7) |
                    ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_H) >>> 9));

            whitePassedPawnScore = Long.bitCount(whiteGuardedPassedPawns) * RivalConstants.VALUE_GUARDED_PASSED_PAWN;
            blackPassedPawnScore = Long.bitCount(blackGuardedPassedPawns) * RivalConstants.VALUE_GUARDED_PASSED_PAWN;

            final long whiteIsolatedPawns = whitePawnFiles & ~(whitePawnFiles << 1) & ~(whitePawnFiles >>> 1);
            final long blackIsolatedPawns = blackPawnFiles & ~(blackPawnFiles << 1) & ~(blackPawnFiles >>> 1);
            pawnScore -= Long.bitCount(whiteIsolatedPawns) * RivalConstants.VALUE_ISOLATED_PAWN_PENALTY;
            pawnScore += Long.bitCount(blackIsolatedPawns) * RivalConstants.VALUE_ISOLATED_PAWN_PENALTY;

            if ((whiteIsolatedPawns & Bitboards.FILE_D) != 0) pawnScore -= RivalConstants.VALUE_ISOLATED_DPAWN_PENALTY;
            if ((blackIsolatedPawns & Bitboards.FILE_D) != 0) pawnScore += RivalConstants.VALUE_ISOLATED_DPAWN_PENALTY;

            pawnScore -=
                    Long.bitCount(
                            board.m_pieceBitboards[RivalConstants.WP] &
                                    ~((board.m_pieceBitboards[RivalConstants.WP] | board.m_pieceBitboards[RivalConstants.BP]) >>> 8) &
                                    (blackPawnAttacks >>> 8) &
                                    ~Bitboards.northFill(whitePawnAttacks) &
                                    (((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_A) >>> 7) | ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_H) >>> 9)) &
                                    ~Bitboards.northFill(blackPawnFiles)
                    ) * RivalConstants.VALUE_BACKWARD_PAWN_PENALTY;

            pawnScore += Long.bitCount(
                    board.m_pieceBitboards[RivalConstants.BP] &
                            ~((board.m_pieceBitboards[RivalConstants.BP] | board.m_pieceBitboards[RivalConstants.WP]) << 8) &
                            (whitePawnAttacks << 8) &
                            ~Bitboards.southFill(blackPawnAttacks) &
                            (((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_A) << 9) | ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_H) << 7)) &
                            ~Bitboards.northFill(whitePawnFiles)
            ) * RivalConstants.VALUE_BACKWARD_PAWN_PENALTY;

            int sq;
            long bitboard = whitePassedPawns;
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                whitePassedPawnScore += RivalConstants.VALUE_PASSED_PAWN_BONUS[sq / 8];
            }

            bitboard = blackPassedPawns;
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                blackPassedPawnScore += RivalConstants.VALUE_PASSED_PAWN_BONUS[7 - (sq / 8)];
            }

            pawnScore -=
                    (Long.bitCount(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_A) + Long.bitCount(board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_H))
                            * RivalConstants.VALUE_SIDE_PAWN_PENALTY;

            pawnScore +=
                    (Long.bitCount(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_A) + Long.bitCount(board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_H))
                            * RivalConstants.VALUE_SIDE_PAWN_PENALTY;

            long occupiedFileMask = Bitboards.southFill(board.m_pieceBitboards[RivalConstants.WP]) & Bitboards.RANK_1;
            pawnScore -= (int) (RivalConstants.VALUE_DOUBLED_PAWN_PENALTY * ((board.whitePawnValues / 100) - Long.bitCount(occupiedFileMask)));
            pawnScore -= Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * RivalConstants.VALUE_PAWN_ISLAND_PENALTY;

            occupiedFileMask = Bitboards.southFill(board.m_pieceBitboards[RivalConstants.BP]) & Bitboards.RANK_1;
            pawnScore += (int) (RivalConstants.VALUE_DOUBLED_PAWN_PENALTY * ((board.blackPawnValues / 100) - Long.bitCount(occupiedFileMask)));
            pawnScore += Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * RivalConstants.VALUE_PAWN_ISLAND_PENALTY;

            if (RivalConstants.USE_PAWN_HASH) {
                m_pawnHashValuesStored++;
                this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = pawnScore;
                this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE] = whitePassedPawnScore;
                this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE] = blackPassedPawnScore;
                this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWNS] = whitePassedPawns;
                this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWNS] = blackPassedPawns;
                this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_LOCK] = pawnHashValue;
            }
        }

        if (RivalConstants.USE_QUICK_PAWN_HASH_RETURN) {
            lastPawnHashValue = pawnHashValue;
            lastPawnScore = pawnScore;
            lastWhitePassedPawnScore = whitePassedPawnScore;
            lastBlackPassedPawnScore = blackPassedPawnScore;
            lastWhitePassedPawns = whitePassedPawns;
            lastBlackPassedPawns = blackPassedPawns;
        }

        pawnScore +=
                Numbers.linearScale(board.blackPieceValues, 0, RivalConstants.PAWN_ADJUST_MAX_MATERIAL, whitePassedPawnScore * 2, whitePassedPawnScore)
                        - Numbers.linearScale(board.whitePieceValues, 0, RivalConstants.PAWN_ADJUST_MAX_MATERIAL, blackPassedPawnScore * 2, blackPassedPawnScore);

        if (board.blackPieceValues < RivalConstants.PAWN_ADJUST_MAX_MATERIAL) {
            final int kingX = board.m_blackKingSquare % 8;
            final int kingY = board.m_blackKingSquare / 8;
            long bitboard = whitePassedPawns;
            int sq;
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                final int pawnDistance = Math.min(5, 7 - (sq / 8));
                final int kingDistance = Math.max(Math.abs(kingX - (sq % 8)), Math.abs(kingY - 7));
                pawnScore += Numbers.linearScale(board.blackPieceValues, 0, RivalConstants.PAWN_ADJUST_MAX_MATERIAL, kingDistance * 4, 0);
                if ((pawnDistance < (kingDistance - (board.m_isWhiteToMove ? 0 : 1))) && (board.blackPieceValues == 0))
                    pawnScore += RivalConstants.VALUE_KING_CANNOT_CATCH_PAWN;
            }
        }

        if (board.whitePieceValues < RivalConstants.PAWN_ADJUST_MAX_MATERIAL) {
            final int kingX = board.m_whiteKingSquare % 8;
            final int kingY = board.m_whiteKingSquare / 8;
            long bitboard = blackPassedPawns;
            int sq;
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                final int pawnDistance = Math.min(5, (sq / 8));
                final int kingDistance = Math.max(Math.abs(kingX - (sq % 8)), Math.abs(kingY - 0));
                pawnScore -= Numbers.linearScale(board.whitePieceValues, 0, RivalConstants.PAWN_ADJUST_MAX_MATERIAL, kingDistance * 4, 0);
                if ((pawnDistance < (kingDistance - (board.m_isWhiteToMove ? 1 : 0))) && (board.whitePieceValues == 0))
                    pawnScore -= RivalConstants.VALUE_KING_CANNOT_CATCH_PAWN;
            }
        }

        return pawnScore;
    }

    private int scoreRightWayPositions(EngineChessBoard board, int h1, int h2, int h3, int g1, int g2, int g3, int f1, int f2, int f3, int f4, int offset, int cornerColour) {
        int safety = 0;

        if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << h1)) == 0) {
            if ((board.m_pieceBitboards[RivalConstants.WR + offset] & (1L << f1)) != 0) {
                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << f2)) != 0) {
                    if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g2)) != 0) {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                            // (A)
                            safety += 120;
                        } else {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h3)) != 0) {
                                if ((board.m_pieceBitboards[RivalConstants.WN + offset] & (1L << f3)) != 0) {
                                    // (D)
                                    safety += 70;
                                    // check for bishop of same colour as h3
                                    long bits = (cornerColour == RivalConstants.WHITE ? Bitboards.LIGHT_SQUARES : Bitboards.DARK_SQUARES);
                                    if ((bits & board.m_pieceBitboards[RivalConstants.WB + offset]) != 0) {
                                        safety -= 30;
                                    }
                                }
                            }
                        }
                    } else {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g3)) != 0) {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                if ((board.m_pieceBitboards[RivalConstants.WB + offset] & (1L << g2)) != 0) {
                                    // (B)
                                    safety += 100;
                                }
                            } else {
                                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h3)) != 0) {
                                    if ((board.m_pieceBitboards[RivalConstants.WB + offset] & (1L << g2)) != 0) {
                                        // (C)
                                        safety += 70;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << f4)) != 0) {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g2)) != 0) {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                // (E)
                                safety += 50;
                                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                    if ((board.m_pieceBitboards[RivalConstants.WN + offset] & (1L << f3)) != 0) {
                                        safety += 40;
                                    }
                                }
                            }
                        }
                    } else {
                        if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << f3)) != 0) {
                            if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << g2)) != 0) {
                                if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h2)) != 0) {
                                    // (F)
                                    safety += 25;
                                } else {
                                    if ((board.m_pieceBitboards[RivalConstants.WP + offset] & (1L << h3)) != 0) {
                                        // (H)
                                        safety -= 30;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return safety / RivalConstants.KINGSAFETY_RIGHTWAY_DIVISOR;
    }

    private int[] indexOfFirstAttackerInDirection = new int[8];
    private int[] captureList = new int[32];

    final public int staticExchangeEvaluation(EngineChessBoard board, int move) {
        final int toSquare = move & 63;

        captureList[0] =
                ((1L << toSquare) == board.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE]) ?
                        RivalConstants.VALUE_PAWN :
                        RivalConstants.PIECE_VALUES.get(board.squareContents[toSquare]);

        int numCaptures = 1;

        if (board.makeMove(move & ~RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL)) {
            int currentPieceOnSquare = board.squareContents[toSquare];
            int currentSquareValue = RivalConstants.PIECE_VALUES.get(currentPieceOnSquare);

            indexOfFirstAttackerInDirection[0] = getNextDirectionAttackerAfterIndex(board, toSquare, 0, 0);
            indexOfFirstAttackerInDirection[1] = getNextDirectionAttackerAfterIndex(board, toSquare, 1, 0);
            indexOfFirstAttackerInDirection[2] = getNextDirectionAttackerAfterIndex(board, toSquare, 2, 0);
            indexOfFirstAttackerInDirection[3] = getNextDirectionAttackerAfterIndex(board, toSquare, 3, 0);
            indexOfFirstAttackerInDirection[4] = getNextDirectionAttackerAfterIndex(board, toSquare, 4, 0);
            indexOfFirstAttackerInDirection[5] = getNextDirectionAttackerAfterIndex(board, toSquare, 5, 0);
            indexOfFirstAttackerInDirection[6] = getNextDirectionAttackerAfterIndex(board, toSquare, 6, 0);
            indexOfFirstAttackerInDirection[7] = getNextDirectionAttackerAfterIndex(board, toSquare, 7, 0);

            int whiteKnightAttackCount = board.m_pieceBitboards[RivalConstants.WN] == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(toSquare) & board.m_pieceBitboards[RivalConstants.WN]);
            int blackKnightAttackCount = board.m_pieceBitboards[RivalConstants.BN] == 0 ? 0 : Long.bitCount(Bitboards.knightMoves.get(toSquare) & board.m_pieceBitboards[RivalConstants.BN]);

            boolean isWhiteToMove = board.m_isWhiteToMove;

            int bestDir, lowestPieceValue;

            do {
                bestDir = -1;
                lowestPieceValue = RivalConstants.INFINITY;
                for (int dir = 0; dir < 8; dir++) {
                    if (indexOfFirstAttackerInDirection[dir] > 0) {
                        int pieceType = board.squareContents[toSquare + Bitboards.bitRefIncrements.get(dir) * indexOfFirstAttackerInDirection[dir]];
                        if (isWhiteToMove == (pieceType <= RivalConstants.WR)) {
                            if (RivalConstants.PIECE_VALUES.get(pieceType) < lowestPieceValue) {
                                bestDir = dir;
                                lowestPieceValue = RivalConstants.PIECE_VALUES.get(pieceType);
                            }
                        }
                    }
                }

                if (RivalConstants.VALUE_KNIGHT < lowestPieceValue && (isWhiteToMove ? whiteKnightAttackCount : blackKnightAttackCount) > 0) {
                    if (isWhiteToMove) whiteKnightAttackCount--;
                    else blackKnightAttackCount--;
                    lowestPieceValue = RivalConstants.VALUE_KNIGHT;
                    bestDir = 8;
                }

                if (bestDir == -1) break;

                captureList[numCaptures++] = currentSquareValue;

                if (currentSquareValue == RivalConstants.PIECE_VALUES.get(RivalConstants.WK)) break;

                currentSquareValue = lowestPieceValue;

                if (bestDir != 8)
                    indexOfFirstAttackerInDirection[bestDir] = getNextDirectionAttackerAfterIndex(board, toSquare, bestDir, indexOfFirstAttackerInDirection[bestDir]);

                isWhiteToMove = !isWhiteToMove;
            }
            while (true);

            board.unMakeMove();
        } else {
            return -RivalConstants.INFINITY;
        }

        int score = 0;
        for (int i = numCaptures - 1; i > 0; i--) score = Math.max(0, captureList[i] - score);
        return captureList[0] - score;
    }

    private int getNextDirectionAttackerAfterIndex(EngineChessBoard board, int bitRef, int direction, int index) {
        final int xInc = Bitboards.xIncrements.get(direction);
        final int yInc = Bitboards.yIncrements.get(direction);
        int pieceType;
        index++;
        int x = (bitRef % 8) + xInc * index;
        if (x < 0 || x > 7) return -1;
        int y = (bitRef / 8) + yInc * index;
        if (y < 0 || y > 7) return -1;
        bitRef += Bitboards.bitRefIncrements.get(direction) * index;
        do {
            pieceType = board.squareContents[bitRef];
            switch (pieceType % 6) {
                case -1:
                    break;
                case RivalConstants.WK:
                    return (index == 1) ? 1 : -1;
                case RivalConstants.WP:
                    return ((index == 1) && (yInc == (pieceType == RivalConstants.WP ? -1 : 1)) && (xInc != 0)) ? 1 : -1;
                case RivalConstants.WQ:
                    return index;
                case RivalConstants.WR:
                    return ((direction & 1) == 0) ? index : -1;
                case RivalConstants.WB:
                    return ((direction & 1) != 0) ? index : -1;
                case RivalConstants.WN:
                    return -1;
            }
            x += xInc;
            if (x < 0 || x > 7) return -1;
            y += yInc;
            if (y < 0 || y > 7) return -1;
            bitRef += Bitboards.bitRefIncrements.get(direction);
        }
        while (index++ > 0);
        return -1;
    }

    int highest = -9999;
    int lowest = 9999;

    public int evaluate(EngineChessBoard board) {
        if (RivalConstants.COUNT_NODES_IN_EVALUATE_ONLY) this.setNodes(this.getNodes() + 1);

        if (board.whitePieceValues + board.blackPieceValues + board.whitePawnValues + board.blackPawnValues == 0)
            return 0;

        int sq;
        long bitboard;

        int whiteKingAttackedCount = 0;
        int blackKingAttackedCount = 0;
        long whiteAttacksBitboard = 0;
        long blackAttacksBitboard = 0;
        final long whitePawnAttacks = ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_A) << 9) | ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_H) << 7);
        final long blackPawnAttacks = ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_A) >>> 7) | ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_H) >>> 9);
        final long whitePieces = board.m_pieceBitboards[board.m_isWhiteToMove ? RivalConstants.FRIENDLY : RivalConstants.ENEMY];
        final long blackPieces = board.m_pieceBitboards[board.m_isWhiteToMove ? RivalConstants.ENEMY : RivalConstants.FRIENDLY];
        final long whiteKingDangerZone = Bitboards.kingMoves.get(board.m_whiteKingSquare) | (Bitboards.kingMoves.get(board.m_whiteKingSquare) << 8);
        final long blackKingDangerZone = Bitboards.kingMoves.get(board.m_blackKingSquare) | (Bitboards.kingMoves.get(board.m_blackKingSquare) >>> 8);

        final int materialDifference = board.whitePieceValues - board.blackPieceValues + board.whitePawnValues - board.blackPawnValues;

        int eval =
                (RivalConstants.TRACK_PIECE_SQUARE_VALUES)
                        ?
                        materialDifference
                                + Numbers.linearScale(board.blackPieceValues, RivalConstants.PAWN_STAGE_MATERIAL_LOW, RivalConstants.PAWN_STAGE_MATERIAL_HIGH, board.pieceSquareValuesEndGame[RivalConstants.WP], board.pieceSquareValues[RivalConstants.WP])
                                - Numbers.linearScale(board.whitePieceValues, RivalConstants.PAWN_STAGE_MATERIAL_LOW, RivalConstants.PAWN_STAGE_MATERIAL_HIGH, board.pieceSquareValuesEndGame[RivalConstants.BP], board.pieceSquareValues[RivalConstants.BP])
                                + (board.pieceSquareValues[RivalConstants.WR] * Math.min(board.blackPawnValues / RivalConstants.VALUE_PAWN, 6) / 6)
                                - (board.pieceSquareValues[RivalConstants.BR] * Math.min(board.whitePawnValues / RivalConstants.VALUE_PAWN, 6) / 6)
                                + board.pieceSquareValues[RivalConstants.WB]
                                - board.pieceSquareValues[RivalConstants.BB]
                                + Numbers.linearScale(board.blackPieceValues + board.blackPawnValues, RivalConstants.KNIGHT_STAGE_MATERIAL_LOW, RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH, board.pieceSquareValuesEndGame[RivalConstants.WN], board.pieceSquareValues[RivalConstants.WN])
                                - Numbers.linearScale(board.whitePieceValues + board.whitePawnValues, RivalConstants.KNIGHT_STAGE_MATERIAL_LOW, RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH, board.pieceSquareValuesEndGame[RivalConstants.BN], board.pieceSquareValues[RivalConstants.BN])
                                + board.pieceSquareValues[RivalConstants.WQ]
                                - board.pieceSquareValues[RivalConstants.BQ]
                                + Numbers.linearScale(board.blackPieceValues, RivalConstants.VALUE_ROOK, RivalConstants.OPENING_PHASE_MATERIAL, Bitboards.pieceSquareTableKingEndGame.get(board.m_whiteKingSquare), Bitboards.pieceSquareTableKing.get(board.m_whiteKingSquare))
                                - Numbers.linearScale(board.whitePieceValues, RivalConstants.VALUE_ROOK, RivalConstants.OPENING_PHASE_MATERIAL, Bitboards.pieceSquareTableKingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.m_blackKingSquare)), Bitboards.pieceSquareTableKing.get(Bitboards.bitFlippedHorizontalAxis.get(board.m_blackKingSquare)))
                        :
                        materialDifference;

        int pieceSquareTemp = 0;
        int pieceSquareTempEndGame = 0;

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
            bitboard = board.m_pieceBitboards[RivalConstants.WP];
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                pieceSquareTemp += Bitboards.pieceSquareTablePawn.get(sq);
                pieceSquareTempEndGame += Bitboards.pieceSquareTablePawnEndGame.get(sq);
            }

            eval += Numbers.linearScale(board.blackPieceValues, RivalConstants.PAWN_STAGE_MATERIAL_LOW, RivalConstants.PAWN_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

            pieceSquareTemp = 0;
            pieceSquareTempEndGame = 0;
            bitboard = board.m_pieceBitboards[RivalConstants.BP];
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                pieceSquareTemp += Bitboards.pieceSquareTablePawn.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
                pieceSquareTempEndGame += Bitboards.pieceSquareTablePawnEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
            }

            eval -= Numbers.linearScale(board.whitePieceValues, RivalConstants.PAWN_STAGE_MATERIAL_LOW, RivalConstants.PAWN_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

            eval += Numbers.linearScale(board.blackPieceValues, RivalConstants.VALUE_ROOK, RivalConstants.OPENING_PHASE_MATERIAL, Bitboards.pieceSquareTableKingEndGame.get(board.m_whiteKingSquare), Bitboards.pieceSquareTableKing.get(board.m_whiteKingSquare))
                    - Numbers.linearScale(board.whitePieceValues, RivalConstants.VALUE_ROOK, RivalConstants.OPENING_PHASE_MATERIAL, Bitboards.pieceSquareTableKingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.m_blackKingSquare)), Bitboards.pieceSquareTableKing.get(Bitboards.bitFlippedHorizontalAxis.get(board.m_blackKingSquare)));
        }

        int lastSq = -1;
        int file = -1;

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp = 0;
        bitboard = board.m_pieceBitboards[RivalConstants.WR];
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (lastSq != -1 && file == (lastSq % 8)) eval += RivalConstants.VALUE_ROOKS_ON_SAME_FILE;

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp += Bitboards.pieceSquareTableRook.get(sq);

            final long allAttacks = Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];

            eval += RivalConstants.VALUE_ROOK_MOBILITY[Long.bitCount(allAttacks & ~whitePieces)];
            whiteAttacksBitboard |= allAttacks;
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone);

            file = sq % 8;

            if ((Bitboards.FILES.get(file) & board.m_pieceBitboards[RivalConstants.WP]) == 0)
                if ((Bitboards.FILES.get(file) & board.m_pieceBitboards[RivalConstants.BP]) == 0)
                    eval += RivalConstants.VALUE_ROOK_ON_OPEN_FILE;
                else
                    eval += RivalConstants.VALUE_ROOK_ON_HALF_OPEN_FILE;

            lastSq = sq;
        }

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
            eval += (pieceSquareTemp * Math.min(board.blackPawnValues / RivalConstants.VALUE_PAWN, 6) / 6);

        if (Long.bitCount(board.m_pieceBitboards[RivalConstants.WR] & Bitboards.RANK_7) > 1 && (board.m_pieceBitboards[RivalConstants.BK] & Bitboards.RANK_8) != 0)
            eval += RivalConstants.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING;

        bitboard = board.m_pieceBitboards[RivalConstants.BR];
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp = 0;
        lastSq = -1;
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (lastSq != -1 && file == (lastSq % 8)) eval -= RivalConstants.VALUE_ROOKS_ON_SAME_FILE;

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
                pieceSquareTemp += Bitboards.pieceSquareTableRook.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            file = sq % 8;

            final long allAttacks = Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];
            eval -= RivalConstants.VALUE_ROOK_MOBILITY[Long.bitCount(allAttacks & ~blackPieces)];
            blackAttacksBitboard |= allAttacks;
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone);

            if ((Bitboards.FILES.get(file) & board.m_pieceBitboards[RivalConstants.BP]) == 0)
                if ((Bitboards.FILES.get(file) & board.m_pieceBitboards[RivalConstants.WP]) == 0)
                    eval -= RivalConstants.VALUE_ROOK_ON_OPEN_FILE;
                else
                    eval -= RivalConstants.VALUE_ROOK_ON_HALF_OPEN_FILE;

            lastSq = sq;
        }

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
            eval -= (pieceSquareTemp * Math.min(board.whitePawnValues / RivalConstants.VALUE_PAWN, 6) / 6);

        if (Long.bitCount(board.m_pieceBitboards[RivalConstants.BR] & Bitboards.RANK_2) > 1 && (board.m_pieceBitboards[RivalConstants.WK] & Bitboards.RANK_1) != 0)
            eval -= RivalConstants.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING;

        bitboard = board.m_pieceBitboards[RivalConstants.WN];

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
            pieceSquareTemp = 0;
            pieceSquareTempEndGame = 0;
        }
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            final long knightAttacks = Bitboards.knightMoves.get(sq);

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
                pieceSquareTemp += Bitboards.pieceSquareTableKnight.get(sq);
                pieceSquareTempEndGame += Bitboards.pieceSquareTableKnightEndGame.get(sq);
            }

            whiteAttacksBitboard |= knightAttacks;
            eval -= Long.bitCount(knightAttacks & (blackPawnAttacks | board.m_pieceBitboards[RivalConstants.WP])) * RivalConstants.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY;
        }

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
            eval += Numbers.linearScale(board.blackPieceValues + board.blackPawnValues, RivalConstants.KNIGHT_STAGE_MATERIAL_LOW, RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
            pieceSquareTemp = 0;
            pieceSquareTempEndGame = 0;
        }
        bitboard = board.m_pieceBitboards[RivalConstants.BN];
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
                pieceSquareTemp += Bitboards.pieceSquareTableKnight.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
                pieceSquareTempEndGame += Bitboards.pieceSquareTableKnightEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(sq));
            }

            final long knightAttacks = Bitboards.knightMoves.get(sq);

            blackAttacksBitboard |= knightAttacks;
            eval += Long.bitCount(knightAttacks & (whitePawnAttacks | board.m_pieceBitboards[RivalConstants.BP])) * RivalConstants.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY;
        }

        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
            eval -= Numbers.linearScale(board.whitePieceValues + board.whitePawnValues, RivalConstants.KNIGHT_STAGE_MATERIAL_LOW, RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH, pieceSquareTempEndGame, pieceSquareTemp);

        bitboard = board.m_pieceBitboards[RivalConstants.WQ];
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval += Bitboards.pieceSquareTableQueen.get(sq);

            final long allAttacks =
                    Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])] |
                            Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];

            whiteAttacksBitboard |= allAttacks;
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone) * 2;

            eval += RivalConstants.VALUE_QUEEN_MOBILITY[Long.bitCount(allAttacks & ~whitePieces)];
        }

        bitboard = board.m_pieceBitboards[RivalConstants.BQ];
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
                eval -= Bitboards.pieceSquareTableQueen.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            final long allAttacks =
                    Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])] |
                            Bitboards.magicBitboards.magicMovesRook[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskRook[sq]) * MagicBitboards.magicNumberRook[sq]) >>> MagicBitboards.magicNumberShiftsRook[sq])];

            blackAttacksBitboard |= allAttacks;
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone) * 2;

            eval -= RivalConstants.VALUE_QUEEN_MOBILITY[Long.bitCount(allAttacks & ~blackPieces)];
        }

        eval += getPawnScore(board, board.whitePieceValues + board.blackPieceValues);

        eval +=
                Numbers.linearScale((materialDifference > 0) ? board.whitePawnValues : board.blackPawnValues, 0, RivalConstants.TRADE_BONUS_UPPER_PAWNS, -30 * materialDifference / 100, 0) +
                        Numbers.linearScale((materialDifference > 0) ? board.blackPieceValues + board.blackPawnValues : board.whitePieceValues + board.whitePawnValues, 0, RivalConstants.TOTAL_PIECE_VALUE_PER_SIDE_AT_START, 30 * materialDifference / 100, 0);

        final int castlePrivs =
                (board.m_castlePrivileges & RivalConstants.CASTLEPRIV_WK) +
                        (board.m_castlePrivileges & RivalConstants.CASTLEPRIV_WQ) +
                        (board.m_castlePrivileges & RivalConstants.CASTLEPRIV_BK) +
                        (board.m_castlePrivileges & RivalConstants.CASTLEPRIV_BQ);

        if (castlePrivs != 0) {
            // Value of moving King to its queenside castle destination in the middle game
            int kingSquareBonusMiddleGame = Bitboards.pieceSquareTableKing.get(1) - Bitboards.pieceSquareTableKing.get(3);
            int kingSquareBonusEndGame = Bitboards.pieceSquareTableKingEndGame.get(1) - Bitboards.pieceSquareTableKingEndGame.get(3);
            int rookSquareBonus = Bitboards.pieceSquareTableRook.get(3) - Bitboards.pieceSquareTableRook.get(0);
            int kingSquareBonusScaled =
                    Numbers.linearScale(
                            board.blackPieceValues,
                            RivalConstants.CASTLE_BONUS_LOW_MATERIAL,
                            RivalConstants.CASTLE_BONUS_HIGH_MATERIAL,
                            kingSquareBonusEndGame,
                            kingSquareBonusMiddleGame);

            // don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
            // given by still having castling rights.
            int castleValue = kingSquareBonusScaled + rookSquareBonus;

            if (castleValue > 0) {
                int timeToCastleKingSide = 100;
                int timeToCastleQueenSide = 100;
                if ((board.m_castlePrivileges & RivalConstants.CASTLEPRIV_WK) != 0) {
                    timeToCastleKingSide = 2;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 1)) != 0) timeToCastleKingSide++;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 2)) != 0) timeToCastleKingSide++;
                }
                if ((board.m_castlePrivileges & RivalConstants.CASTLEPRIV_WQ) != 0) {
                    timeToCastleQueenSide = 2;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 6)) != 0) timeToCastleQueenSide++;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 5)) != 0) timeToCastleQueenSide++;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 4)) != 0) timeToCastleQueenSide++;
                }
                eval += castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide);
            }

            kingSquareBonusScaled =
                    Numbers.linearScale(
                            board.whitePieceValues,
                            RivalConstants.CASTLE_BONUS_LOW_MATERIAL,
                            RivalConstants.CASTLE_BONUS_HIGH_MATERIAL,
                            kingSquareBonusEndGame,
                            kingSquareBonusMiddleGame);

            castleValue = kingSquareBonusScaled + rookSquareBonus;

            if (castleValue > 0) {
                int timeToCastleKingSide = 100;
                int timeToCastleQueenSide = 100;
                if ((board.m_castlePrivileges & RivalConstants.CASTLEPRIV_BK) != 0) {
                    timeToCastleKingSide = 2;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 57)) != 0) timeToCastleKingSide++;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 58)) != 0) timeToCastleKingSide++;
                }
                if ((board.m_castlePrivileges & RivalConstants.CASTLEPRIV_BQ) != 0) {
                    timeToCastleQueenSide = 2;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 60)) != 0) timeToCastleQueenSide++;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 61)) != 0) timeToCastleQueenSide++;
                    if ((board.m_pieceBitboards[RivalConstants.ALL] & (1L << 62)) != 0) timeToCastleQueenSide++;
                }
                eval -= castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide);
            }
        }

        final boolean whiteLightBishopExists = (board.m_pieceBitboards[RivalConstants.WB] & Bitboards.LIGHT_SQUARES) != 0;
        final boolean whiteDarkBishopExists = (board.m_pieceBitboards[RivalConstants.WB] & Bitboards.DARK_SQUARES) != 0;
        final boolean blackLightBishopExists = (board.m_pieceBitboards[RivalConstants.BB] & Bitboards.LIGHT_SQUARES) != 0;
        final boolean blackDarkBishopExists = (board.m_pieceBitboards[RivalConstants.BB] & Bitboards.DARK_SQUARES) != 0;

        final int whiteBishopColourCount = (whiteLightBishopExists ? 1 : 0) + (whiteDarkBishopExists ? 1 : 0);
        final int blackBishopColourCount = (blackLightBishopExists ? 1 : 0) + (blackDarkBishopExists ? 1 : 0);

        int bishopScore = 0;

        bitboard = board.m_pieceBitboards[RivalConstants.WB];
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval += Bitboards.pieceSquareTableBishop.get(sq);

            final long allAttacks = Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])];
            whiteAttacksBitboard |= allAttacks;
            blackKingAttackedCount += Long.bitCount(allAttacks & blackKingDangerZone);

            bishopScore += RivalConstants.VALUE_BISHOP_MOBILITY[Long.bitCount(allAttacks & ~whitePieces)];
        }

        if (whiteBishopColourCount == 2)
            bishopScore += RivalConstants.VALUE_BISHOP_PAIR + ((8 - (board.whitePawnValues / RivalConstants.VALUE_PAWN)) * RivalConstants.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS);

        bitboard = board.m_pieceBitboards[RivalConstants.BB];
        while (bitboard != 0) {
            bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));

            if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES)
                eval -= Bitboards.pieceSquareTableBishop.get(Bitboards.bitFlippedHorizontalAxis.get(sq));

            final long allAttacks = Bitboards.magicBitboards.magicMovesBishop[sq][(int) (((board.m_pieceBitboards[RivalConstants.ALL] & MagicBitboards.occupancyMaskBishop[sq]) * MagicBitboards.magicNumberBishop[sq]) >>> MagicBitboards.magicNumberShiftsBishop[sq])];
            blackAttacksBitboard |= allAttacks;
            whiteKingAttackedCount += Long.bitCount(allAttacks & whiteKingDangerZone);

            bishopScore -= RivalConstants.VALUE_BISHOP_MOBILITY[Long.bitCount(allAttacks & ~blackPieces)];
        }

        if (blackBishopColourCount == 2)
            bishopScore -= RivalConstants.VALUE_BISHOP_PAIR + ((8 - (board.blackPawnValues / RivalConstants.VALUE_PAWN)) * RivalConstants.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS);

        if (whiteBishopColourCount == 1 && blackBishopColourCount == 1 && whiteLightBishopExists != blackLightBishopExists && board.whitePieceValues == board.blackPieceValues) {
            // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
            final int maxPenalty = (eval + bishopScore) / RivalConstants.WRONG_COLOUR_BISHOP_PENALTY_DIVISOR; // mostly pawns as material is identical

            // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
            bishopScore -= Numbers.linearScale(
                    board.whitePieceValues + board.blackPieceValues,
                    RivalConstants.WRONG_COLOUR_BISHOP_MATERIAL_LOW,
                    RivalConstants.WRONG_COLOUR_BISHOP_MATERIAL_HIGH,
                    maxPenalty,
                    0);
        }

        if (((board.m_pieceBitboards[RivalConstants.WB] | board.m_pieceBitboards[RivalConstants.BB]) & Bitboards.A2A7H2H7) != 0) {
            if ((board.m_pieceBitboards[RivalConstants.WB] & (1L << Bitboards.A7)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.BP] & (1L << Bitboards.B6)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.BP] & (1L << Bitboards.C7)) != 0)
                bishopScore -= RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY;

            if ((board.m_pieceBitboards[RivalConstants.WB] & (1L << Bitboards.H7)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.BP] & (1L << Bitboards.G6)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.BP] & (1L << Bitboards.F7)) != 0)
                bishopScore -= (board.m_pieceBitboards[RivalConstants.WQ] == 0) ?
                        RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY :
                        RivalConstants.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY;

            if ((board.m_pieceBitboards[RivalConstants.BB] & (1L << Bitboards.A2)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.WP] & (1L << Bitboards.B3)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.WP] & (1L << Bitboards.C2)) != 0)
                bishopScore += RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY;

            if ((board.m_pieceBitboards[RivalConstants.BB] & (1L << Bitboards.H2)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.WP] & (1L << Bitboards.G3)) != 0 &&
                    (board.m_pieceBitboards[RivalConstants.WP] & (1L << Bitboards.F2)) != 0)
                bishopScore += (board.m_pieceBitboards[RivalConstants.BQ] == 0) ?
                        RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY :
                        RivalConstants.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY;
        }

        eval += bishopScore;

        // Everything white attacks with pieces.  Does not include attacked pawns.
        whiteAttacksBitboard &= board.m_pieceBitboards[RivalConstants.BN] | board.m_pieceBitboards[RivalConstants.BR] | board.m_pieceBitboards[RivalConstants.BQ] | board.m_pieceBitboards[RivalConstants.BB];
        // Plus anything white attacks with pawns.
        whiteAttacksBitboard |= ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_A) << 9) | ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_H) << 7);

        int temp = 0;

        bitboard = whiteAttacksBitboard & blackPieces & ~board.m_pieceBitboards[RivalConstants.BK];

        while (bitboard != 0) {
            bitboard ^= ((1L << (sq = Long.numberOfTrailingZeros(bitboard))));
            if (board.squareContents[sq] == RivalConstants.BP) temp += RivalConstants.VALUE_PAWN;
            else if (board.squareContents[sq] == RivalConstants.BN) temp += RivalConstants.VALUE_KNIGHT;
            else if (board.squareContents[sq] == RivalConstants.BR) temp += RivalConstants.VALUE_ROOK;
            else if (board.squareContents[sq] == RivalConstants.BQ) temp += RivalConstants.VALUE_QUEEN;
            else if (board.squareContents[sq] == RivalConstants.BB) temp += RivalConstants.VALUE_BISHOP;
        }

        int threatScore = temp + temp * (temp / RivalConstants.VALUE_QUEEN);

        blackAttacksBitboard &= board.m_pieceBitboards[RivalConstants.WN] | board.m_pieceBitboards[RivalConstants.WR] | board.m_pieceBitboards[RivalConstants.WQ] | board.m_pieceBitboards[RivalConstants.WB];
        blackAttacksBitboard |= ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_A) >>> 7) | ((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_H) >>> 9);

        temp = 0;

        bitboard = blackAttacksBitboard & whitePieces & ~board.m_pieceBitboards[RivalConstants.WK];

        while (bitboard != 0) {
            bitboard ^= ((1L << (sq = Long.numberOfTrailingZeros(bitboard))));
            if (board.squareContents[sq] == RivalConstants.WP) temp += RivalConstants.VALUE_PAWN;
            else if (board.squareContents[sq] == RivalConstants.WN) temp += RivalConstants.VALUE_KNIGHT;
            else if (board.squareContents[sq] == RivalConstants.WR) temp += RivalConstants.VALUE_ROOK;
            else if (board.squareContents[sq] == RivalConstants.WQ) temp += RivalConstants.VALUE_QUEEN;
            else if (board.squareContents[sq] == RivalConstants.WB) temp += RivalConstants.VALUE_BISHOP;
        }

        threatScore -= temp + temp * (temp / RivalConstants.VALUE_QUEEN);
        threatScore /= RivalConstants.THREAT_SCORE_DIVISOR;

        eval += threatScore;

        final int averagePiecesPerSide = (board.whitePieceValues + board.blackPieceValues) / 2;
        int whiteKingSafety = 0;
        int blackKingSafety = 0;
        int kingSafety = 0;
        if (averagePiecesPerSide > RivalConstants.KINGSAFETY_MIN_PIECE_BALANCE) {
            int h1 = 0, h2 = 8, h3 = 16, g1 = 1, g2 = 9, g3 = 17, f1 = 2, f2 = 10, f3 = 18, f4 = 26;

            if (board.m_whiteKingSquare == 1 || board.m_whiteKingSquare == 8) {
                whiteKingSafety = scoreRightWayPositions(board, h1, h2, h3, g1, g2, g3, f1, f2, f3, f4, 0, RivalConstants.WHITE);
            }

            if (board.m_blackKingSquare == 57 || board.m_blackKingSquare == 48) {
                h1 = 56;
                h2 = 48;
                h3 = 40;
                g1 = 57;
                g2 = 49;
                g3 = 41;
                f1 = 58;
                f2 = 50;
                f3 = 42;
                f4 = 34;
                blackKingSafety = scoreRightWayPositions(board, h1, h2, h3, g1, g2, g3, f1, f2, f3, f4, 6, RivalConstants.BLACK);
            }

            int halfOpenFilePenalty = 0;
            int shieldValue = 0;
            if (board.m_whiteKingSquare / 8 < 2) {
                final long kingShield = Bitboards.whiteKingShieldMask.get(board.m_whiteKingSquare % 8);

                shieldValue += RivalConstants.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.WP] & kingShield)
                        - RivalConstants.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.BP] & (kingShield | (kingShield << 8)))
                        + RivalConstants.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.WP] & (kingShield << 8))
                        - RivalConstants.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.BP] & (kingShield << 16));

                shieldValue = Math.min(shieldValue, RivalConstants.KINGSAFTEY_MAXIMUM_SHIELD_BONUS);

                if (((board.m_pieceBitboards[RivalConstants.WK] & Bitboards.F1G1) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WR] & Bitboards.G1H1) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_G) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_H) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                } else if (((board.m_pieceBitboards[RivalConstants.WK] & Bitboards.B1C1) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WR] & Bitboards.A1B1) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_A) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WP] & Bitboards.FILE_B) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                }

                final long whiteOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.m_pieceBitboards[RivalConstants.WP])) & Bitboards.RANK_1;
                if (whiteOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(whiteOpen & Bitboards.MIDDLE_FILES_8_BIT);
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(whiteOpen & Bitboards.NONMID_FILES_8_BIT);
                }
                final long blackOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.m_pieceBitboards[RivalConstants.BP])) & Bitboards.RANK_1;
                if (blackOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(blackOpen & Bitboards.MIDDLE_FILES_8_BIT);
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(blackOpen & Bitboards.NONMID_FILES_8_BIT);
                }
            }

            whiteKingSafety += RivalConstants.KINGSAFETY_SHIELD_BASE + shieldValue - halfOpenFilePenalty;

            shieldValue = 0;
            halfOpenFilePenalty = 0;
            if (board.m_blackKingSquare / 8 >= 6) {
                final long kingShield = Bitboards.whiteKingShieldMask.get(board.m_blackKingSquare % 8) << 40;
                shieldValue += RivalConstants.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.BP] & kingShield)
                        - RivalConstants.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.WP] & (kingShield | (kingShield >>> 8)))
                        + RivalConstants.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.BP] & (kingShield >>> 8))
                        - RivalConstants.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * Long.bitCount(board.m_pieceBitboards[RivalConstants.WP] & (kingShield >>> 16));

                shieldValue = Math.min(shieldValue, RivalConstants.KINGSAFTEY_MAXIMUM_SHIELD_BONUS);

                if (((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.F8G8) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BR] & Bitboards.G8H8) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_G) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_H) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                } else if (((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.B8C8) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BR] & Bitboards.A8B8) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_A) != 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BP] & Bitboards.FILE_B) != 0)) {
                    shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK;
                }

                final long whiteOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.m_pieceBitboards[RivalConstants.WP])) & Bitboards.RANK_1;
                if (whiteOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(whiteOpen & Bitboards.MIDDLE_FILES_8_BIT)
                            + RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(whiteOpen & Bitboards.NONMID_FILES_8_BIT);
                }
                final long blackOpen = Bitboards.southFill(kingShield) & (~Bitboards.southFill(board.m_pieceBitboards[RivalConstants.BP])) & Bitboards.RANK_1;
                if (blackOpen != 0) {
                    halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * Long.bitCount(blackOpen & Bitboards.MIDDLE_FILES_8_BIT)
                            + RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * Long.bitCount(blackOpen & Bitboards.NONMID_FILES_8_BIT);
                }
            }

            blackKingSafety += RivalConstants.KINGSAFETY_SHIELD_BASE + shieldValue - halfOpenFilePenalty;

            kingSafety =
                    Numbers.linearScale(
                            averagePiecesPerSide,
                            RivalConstants.KINGSAFETY_MIN_PIECE_BALANCE,
                            RivalConstants.KINGSAFETY_MAX_PIECE_BALANCE,
                            0,
                            (whiteKingSafety - blackKingSafety) + (blackKingAttackedCount - whiteKingAttackedCount) * RivalConstants.KINGSAFETY_ATTACK_MULTIPLIER);

        }

        eval += kingSafety;

        if (board.whitePieceValues + board.whitePawnValues + board.blackPieceValues + board.blackPawnValues <= RivalConstants.EVAL_ENDGAME_TOTAL_PIECES) {
            eval = endGameAdjustment(board, eval);
        }

        eval = board.m_isWhiteToMove ? eval : -eval;

        return eval;
    }

    public int endGameAdjustment(EngineChessBoard board, int currentScore) {
        int eval = currentScore;

        if (rivalKPKBitbase != null && board.whitePieceValues + board.blackPieceValues == 0 && board.whitePawnValues + board.blackPawnValues == RivalConstants.VALUE_PAWN) {
            if (board.whitePawnValues == RivalConstants.VALUE_PAWN) {
                return kpkLookup(
                        board.m_whiteKingSquare,
                        board.m_blackKingSquare,
                        Long.numberOfTrailingZeros(board.m_pieceBitboards[RivalConstants.WP]),
                        board.m_isWhiteToMove);
            } else {
                // flip the position so that the black pawn becomes white, and negate the result
                return -kpkLookup(
                        Bitboards.bitFlippedHorizontalAxis.get(board.m_blackKingSquare),
                        Bitboards.bitFlippedHorizontalAxis.get(board.m_whiteKingSquare),
                        Bitboards.bitFlippedHorizontalAxis.get(Long.numberOfTrailingZeros(board.m_pieceBitboards[RivalConstants.BP])),
                        !board.m_isWhiteToMove);
            }
        }

        if (board.whitePawnValues + board.blackPawnValues == 0 && board.whitePieceValues < RivalConstants.VALUE_ROOK && board.blackPieceValues < RivalConstants.VALUE_ROOK)
            return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;

        if (eval > 0) {
            if (board.whitePawnValues == 0 && (board.whitePieceValues == RivalConstants.VALUE_KNIGHT || board.whitePieceValues == RivalConstants.VALUE_BISHOP))
                return eval - (int) (board.whitePieceValues * RivalConstants.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER);
            else if (board.whitePawnValues == 0 && board.whitePieceValues - RivalConstants.VALUE_BISHOP <= board.blackPieceValues)
                return eval / RivalConstants.ENDGAME_PROBABLE_DRAW_DIVISOR;
            else if (Long.bitCount(board.m_pieceBitboards[RivalConstants.ALL]) > 3 && (board.m_pieceBitboards[RivalConstants.WR] | board.m_pieceBitboards[RivalConstants.WN] | board.m_pieceBitboards[RivalConstants.WQ]) == 0) {
                // If this is not yet a KPK ending, and if white has only A pawns and has no dark bishop and the black king is on a8/a7/b8/b7 then this is probably a draw.
                // Do the same for H pawns

                if (((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_A) == 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WB] & Bitboards.LIGHT_SQUARES) == 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.A8A7B8B7) != 0) || ((board.m_pieceBitboards[RivalConstants.WP] & ~Bitboards.FILE_H) == 0) &&
                                ((board.m_pieceBitboards[RivalConstants.WB] & Bitboards.DARK_SQUARES) == 0) &&
                                ((board.m_pieceBitboards[RivalConstants.BK] & Bitboards.H8H7G8G7) != 0)) {
                    return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                }

            }
            if (board.blackPawnValues == 0) {
                if (board.whitePieceValues - board.blackPieceValues > RivalConstants.VALUE_BISHOP) {
                    int whiteKnightCount = Long.bitCount(board.m_pieceBitboards[RivalConstants.WN]);
                    int whiteBishopCount = Long.bitCount(board.m_pieceBitboards[RivalConstants.WB]);
                    if ((whiteKnightCount == 2) && (board.whitePieceValues == 2 * RivalConstants.VALUE_KNIGHT) && (board.blackPieceValues == 0))
                        return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                    else if ((whiteKnightCount == 1) && (whiteBishopCount == 1) && (board.whitePieceValues == RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP) && board.blackPieceValues == 0) {
                        eval = RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP + RivalConstants.VALUE_SHOULD_WIN + (eval / RivalConstants.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR);
                        final int kingSquare = board.m_blackKingSquare;

                        if ((board.m_pieceBitboards[RivalConstants.WB] & Bitboards.DARK_SQUARES) != 0)
                            eval += (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;
                        else
                            eval += (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;

                        return eval;
                    } else
                        return eval + RivalConstants.VALUE_SHOULD_WIN;
                }
            }
        }
        if (eval < 0) {
            if (board.blackPawnValues == 0 && (board.blackPieceValues == RivalConstants.VALUE_KNIGHT || board.blackPieceValues == RivalConstants.VALUE_BISHOP))
                return eval + (int) (board.blackPieceValues * RivalConstants.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER);
            else if (board.blackPawnValues == 0 && board.blackPieceValues - RivalConstants.VALUE_BISHOP <= board.whitePieceValues)
                return eval / RivalConstants.ENDGAME_PROBABLE_DRAW_DIVISOR;
            else if (Long.bitCount(board.m_pieceBitboards[RivalConstants.ALL]) > 3 && (board.m_pieceBitboards[RivalConstants.BR] | board.m_pieceBitboards[RivalConstants.BN] | board.m_pieceBitboards[RivalConstants.BQ]) == 0) {
                if (((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_A) == 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BB] & Bitboards.DARK_SQUARES) == 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WK] & Bitboards.A1A2B1B2) != 0))
                    return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                else if (((board.m_pieceBitboards[RivalConstants.BP] & ~Bitboards.FILE_H) == 0) &&
                        ((board.m_pieceBitboards[RivalConstants.BB] & Bitboards.LIGHT_SQUARES) == 0) &&
                        ((board.m_pieceBitboards[RivalConstants.WK] & Bitboards.H1H2G1G2) != 0))
                    return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
            }
            if (board.whitePawnValues == 0) {
                if (board.blackPieceValues - board.whitePieceValues > RivalConstants.VALUE_BISHOP) {
                    int blackKnightCount = Long.bitCount(board.m_pieceBitboards[RivalConstants.BN]);
                    int blackBishopCount = Long.bitCount(board.m_pieceBitboards[RivalConstants.BB]);
                    if ((blackKnightCount == 2) && (board.blackPieceValues == 2 * RivalConstants.VALUE_KNIGHT) && (board.whitePieceValues == 0))
                        return eval / RivalConstants.ENDGAME_DRAW_DIVISOR;
                    else if ((blackKnightCount == 1) && (blackBishopCount == 1) && (board.blackPieceValues == RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP) && board.whitePieceValues == 0) {
                        eval = -(RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP + RivalConstants.VALUE_SHOULD_WIN) + (eval / RivalConstants.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR);
                        final int kingSquare = board.m_whiteKingSquare;
                        if ((board.m_pieceBitboards[RivalConstants.BB] & Bitboards.DARK_SQUARES) != 0) {
                            eval -= (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;
                        } else {
                            eval -= (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE;
                        }
                        return eval;
                    } else
                        return eval - RivalConstants.VALUE_SHOULD_WIN;
                }
            }
        }

        return eval;
    }

    public int kpkLookup(int attackingKingSquare, int defendingKingSquare, int pawnSquare, boolean isAttackerToMove) {
        if (attackingKingSquare % 8 >= 4) {
            /*
             * Flip board on vertical axis to bring White king to right-hand side of board
             */
            attackingKingSquare = Bitboards.bitFlippedVerticalAxis.get(attackingKingSquare);
            defendingKingSquare = Bitboards.bitFlippedVerticalAxis.get(defendingKingSquare);
            pawnSquare = Bitboards.bitFlippedVerticalAxis.get(pawnSquare);
        }

        int attackingKingBitbaseIndex = (attackingKingSquare / 8) * 4 + (attackingKingSquare % 8);
        int pawnBitbaseIndex = pawnSquare - 8;

        int index =
                (attackingKingBitbaseIndex * 64 * 48 * 2) +
                        (defendingKingSquare * 48 * 2) +
                        (pawnBitbaseIndex * 2) +
                        (isAttackerToMove ? 0 : 1);

        int byteIndex = index / 8;
        int bitIndex = index % 8;

        int byteElement = rivalKPKBitbase[byteIndex];
        boolean isWon = (byteElement & (1L << bitIndex)) != 0;

        if (!isWon) return 0;

        int pawnDistanceFromPromotion = 7 - (pawnSquare / 8);

        return RivalConstants.VALUE_QUEEN - RivalConstants.ENDGAME_KPK_PAWN_PENALTY_PER_SQUARE * pawnDistanceFromPromotion;
    }

    public void storeHashMove(int move, EngineChessBoard board, int score, byte flag, int height) {
        int hashIndex = (int) (board.m_hashValue % this.m_maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;

        if (height >= this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] || this.m_hashTableVersion > this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
            if (this.m_hashTableVersion == this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
                // move this entry to the always replace table, but not if it is an entry from a previous search
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_MOVE] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_MOVE];
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE];
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG];
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_64BIT1] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1];
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_64BIT2] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2];
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT];
                this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_VERSION] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION];
            }

            if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
                for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
                    if (this.m_hashTableVersion == this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
                        this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i];
                        this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12];
                    }
                    this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = (int) (board.m_pieceBitboards[i] >>> 32);
                    this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = (int) (board.m_pieceBitboards[i] & Bitboards.LOW32);
                }
            }

            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = move;
            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = score;
            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = flag;
            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = (int) (board.m_hashValue >>> 32);
            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = (int) (board.m_hashValue & Bitboards.LOW32);
            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = height;
            this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = this.m_hashTableVersion;
            this.m_heightHashValuesStored++;
        } else {
            if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
                for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
                    this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = (int) (board.m_pieceBitboards[i] >>> 32);
                    this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = (int) (board.m_pieceBitboards[i] & Bitboards.LOW32);
                }
            }
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_MOVE] = move;
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE] = score;
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] = flag;
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_64BIT1] = (int) (board.m_hashValue >>> 32);
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_64BIT2] = (int) (board.m_hashValue & Bitboards.LOW32);
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = height;
            this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_VERSION] = this.m_hashTableVersion;
            this.m_alwaysHashValuesStored++;
        }
    }

    int[] movesForSorting;

    private void scoreQuiesceMoves(EngineChessBoard board, int ply, boolean includeChecks) {
        int i, score;
        int promotionMask = 0;

        int moveCount = 0;

        movesForSorting = orderedMoves[ply];

        for (i = 0; movesForSorting[i] != 0; i++) {
            promotionMask = (movesForSorting[i] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL);
            int toSquare = movesForSorting[i] & 63;

            int capturePiece = board.squareContents[toSquare] % 6;
            if (capturePiece == -1 &&
                    ((1L << toSquare) & board.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE]) != 0 &&
                    board.squareContents[(movesForSorting[i] >>> 16) & 63] % 6 == RivalConstants.WP)
                capturePiece = RivalConstants.WP;

            score = 0;

            // clear out additional info stored with the move
            movesForSorting[i] &= 0x00FFFFFF;
            if (capturePiece > -1) {
                int see = staticExchangeEvaluation(board, movesForSorting[i]);
                if (see > 0) score = 100 + (int) (((double) see / RivalConstants.VALUE_QUEEN) * 10);
                if (promotionMask == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) score += 9;
            } else if (promotionMask == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                score = 116;
            } else if (includeChecks) {
                score = 100;
            }

            if (score > 0) movesForSorting[moveCount++] = movesForSorting[i] | ((127 - score) << 24);
        }

        movesForSorting[moveCount] = 0;
    }

    MoveOrder[] moveOrderStatus = new MoveOrder[RivalConstants.MAX_TREE_DEPTH];

    private int getHighScoreMove(EngineChessBoard board, int ply, int hashMove) {
        if (moveOrderStatus[ply] == MoveOrder.NONE && hashMove != 0) {
            for (int c = 0; orderedMoves[ply][c] != 0; c++) {
                if (orderedMoves[ply][c] == hashMove) {
                    orderedMoves[ply][c] = -1;
                    return hashMove;
                }
            }
        }

        if (moveOrderStatus[ply] == MoveOrder.NONE) {
            moveOrderStatus[ply] = MoveOrder.CAPTURES;
            if (scoreFullWidthCaptures(board, ply) == 0) {
                // no captures, so move to next stage
                scoreFullWidthMoves(board, ply);
                moveOrderStatus[ply] = MoveOrder.ALL;
            }
        }

        int move = getHighScoreMove(orderedMoves[ply]);

        if (move == 0 && moveOrderStatus[ply] == MoveOrder.CAPTURES) {
            // we move into here if we had some captures but they are now used up
            scoreFullWidthMoves(board, ply);
            moveOrderStatus[ply] = MoveOrder.ALL;
            move = getHighScoreMove(orderedMoves[ply]);
        }

        return move;
    }

    public int getHighScoreMove(int[] theseMoves) {
        int bestMove = 0;
        int bestIndex = -1;
        int bestScore = Integer.MAX_VALUE;

        for (int c = 0; theseMoves[c] != 0; c++) {
            if (theseMoves[c] != -1) {
                // update best move found so far, but don't consider moves with no score
                if (theseMoves[c] < bestScore && (theseMoves[c] >> 24) != 127) {
                    bestScore = theseMoves[c];
                    bestMove = theseMoves[c];
                    bestIndex = c;
                }
            }
        }

        if (bestIndex != -1) theseMoves[bestIndex] = -1;

        return bestMove & 0x00FFFFFF;
    }

    public SearchPath quiesce(EngineChessBoard board, final int depth, int ply, int quiescePly, int low, int high, boolean isCheck) {
        if (!RivalConstants.COUNT_NODES_IN_EVALUATE_ONLY) setNodes(getNodes() + 1);

        SearchPath newPath;
        SearchPath bestPath;

        bestPath = searchPath[ply];
        bestPath.reset();

        int evalScore = evaluate(board);
        bestPath.score = (isCheck ? -RivalConstants.VALUE_MATE : evalScore);

        if (depth == 0 || bestPath.score >= high) {
            bestPath.score = bestPath.score;
            return bestPath;
        }

        if (bestPath.score > low) low = bestPath.score;

        int[] theseMoves = orderedMoves[ply];

        if (isCheck) {
            board.setLegalMoves(theseMoves);
            scoreFullWidthMoves(board, ply);
        } else {
            board.setLegalQuiesceMoves(theseMoves, quiescePly <= RivalConstants.GENERATE_CHECKS_UNTIL_QUIESCE_PLY);
            scoreQuiesceMoves(board, ply, quiescePly <= RivalConstants.GENERATE_CHECKS_UNTIL_QUIESCE_PLY);
        }

        int move;

        int legalMoveCount = 0;
        while ((move = getHighScoreMove(theseMoves)) != 0) {
            if (RivalConstants.USE_DELTA_PRUNING && !isCheck) {
                int materialIncrease = board.lastCapturePiece() > -1 ? RivalConstants.PIECE_VALUES.get(board.lastCapturePiece() % 6) : 0;
                switch (move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) {
                    case 0:
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN:
                        materialIncrease += RivalConstants.VALUE_QUEEN - RivalConstants.VALUE_PAWN;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP:
                        materialIncrease += RivalConstants.VALUE_BISHOP - RivalConstants.VALUE_PAWN;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT:
                        materialIncrease += RivalConstants.VALUE_KNIGHT - RivalConstants.VALUE_PAWN;
                        break;
                    case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK:
                        materialIncrease += RivalConstants.VALUE_ROOK - RivalConstants.VALUE_PAWN;
                        break;
                }
                if (materialIncrease + evalScore + RivalConstants.DELTA_PRUNING_MARGIN < low) continue;
            }

            if (board.makeMove(move)) {
                legalMoveCount++;

                newPath = quiesce(board, depth - 1, ply + 1, quiescePly + 1, -high, -low, (quiescePly <= RivalConstants.GENERATE_CHECKS_UNTIL_QUIESCE_PLY && board.isCheck()));
                newPath.score = -newPath.score;
                if (newPath.score > bestPath.score) bestPath.setPath(move, newPath);
                if (newPath.score >= high) {
                    board.unMakeMove();
                    return bestPath;
                }
                if (newPath.score > low) low = newPath.score;
                board.unMakeMove();
            }
        }

        if (isCheck && legalMoveCount == 0) {
            bestPath.score = -RivalConstants.VALUE_MATE;
        }
        return bestPath;
    }

    private int scoreFullWidthCaptures(EngineChessBoard board, int ply) {
        int i, score;
        int count = 0;

        movesForSorting = orderedMoves[ply];

        for (i = 0; movesForSorting[i] != 0; i++) {
            if (movesForSorting[i] != -1) {
                score = 0;

                int toSquare = movesForSorting[i] & 63;
                int capturePiece = board.squareContents[toSquare] % 6;
                if (capturePiece == -1 &&
                        ((1L << toSquare) & board.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE]) != 0 &&
                        board.squareContents[(movesForSorting[i] >>> 16) & 63] % 6 == RivalConstants.WP)
                    capturePiece = RivalConstants.WP;

                movesForSorting[i] &= 0x00FFFFFF;

                if (movesForSorting[i] == mateKiller[ply]) {
                    score = 126;
                } else if (capturePiece > -1) {
                    int see = staticExchangeEvaluation(board, movesForSorting[i]);
                    if (see > -RivalConstants.INFINITY) see = (int) (((double) see / RivalConstants.VALUE_QUEEN) * 10);

                    if (see > 0) score = 110 + see;
                    else if ((movesForSorting[i] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN)
                        score = 109;
                    else if (see == 0) score = 107;
                    else if (see < 0) {
                        // losing captures with a winning history
                        int historyScore = historyScore(board.m_isWhiteToMove, ((movesForSorting[i] >>> 16) & 63), toSquare);
                        if (historyScore > 5) {
                            score = historyScore;
                        } else {
                            for (int j = 0; j < RivalConstants.NUM_KILLER_MOVES; j++) {
                                if (movesForSorting[i] == killerMoves[ply][j]) {
                                    score = 106 - j;
                                    break;
                                }
                            }
                        }
                    }

                } else if ((movesForSorting[i] & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                    score = 108;
                }

                if (score > 0) count++;
                movesForSorting[i] |= ((127 - score) << 24);
            }
        }
        return count;
    }

    private int historyScore(boolean isWhite, int from, int to) {
        int success = historyMovesSuccess[isWhite ? 0 : 1][from][to];
        int fail = historyMovesFail[isWhite ? 0 : 1][from][to];
        int total = success + fail;
        if (total > 0)
            return (success * 10 / total);
        else
            return 0;
    }

    private void scoreFullWidthMoves(EngineChessBoard board, int ply) {
        int i, j, score;
        int fromSquare, toSquare;

        movesForSorting = orderedMoves[ply];

        for (i = 0; movesForSorting[i] != 0; i++) {
            if (movesForSorting[i] != -1) {
                fromSquare = ((movesForSorting[i] >>> 16) & 63);
                toSquare = (movesForSorting[i] & 63);

                score = 0;

                movesForSorting[i] &= 0x00FFFFFF;

                for (j = 0; j < RivalConstants.NUM_KILLER_MOVES; j++) {
                    if (movesForSorting[i] == killerMoves[ply][j]) {
                        score = 106 - j;
                        break;
                    }
                }

                if (score == 0 && RivalConstants.USE_HISTORY_HEURISTIC && historyMovesSuccess[board.m_isWhiteToMove ? 0 : 1][fromSquare][toSquare] > 0)
                    score = 90 + historyScore(board.m_isWhiteToMove, fromSquare, toSquare);

                // must be a losing capture otherwise would have been scored in previous phase
                // give it a score of 1 to place towards the end of the list
                if (score == 0 && board.squareContents[toSquare] % 6 > -1) score = 1;

                if (score == 0 && RivalConstants.USE_PIECE_SQUARES_IN_MOVE_ORDERING) {
                    int ps = 0;
                    if (!board.m_isWhiteToMove) {
                        fromSquare = Bitboards.bitFlippedHorizontalAxis.get(fromSquare);
                        toSquare = Bitboards.bitFlippedHorizontalAxis.get(toSquare);
                    }
                    switch (board.squareContents[fromSquare] % 6) {
                        case RivalConstants.WP:
                            ps =
                                    Numbers.linearScale(
                                            (board.m_isWhiteToMove ? board.blackPieceValues : board.whitePieceValues),
                                            RivalConstants.PAWN_STAGE_MATERIAL_LOW,
                                            RivalConstants.PAWN_STAGE_MATERIAL_HIGH,
                                            Bitboards.pieceSquareTablePawnEndGame.get(toSquare) - Bitboards.pieceSquareTablePawnEndGame.get(fromSquare),
                                            Bitboards.pieceSquareTablePawn.get(toSquare) - Bitboards.pieceSquareTablePawn.get(fromSquare));
                            break;
                        case RivalConstants.WN:
                            ps =
                                    Numbers.linearScale(
                                            (board.m_isWhiteToMove ? board.blackPieceValues + board.blackPawnValues : board.whitePieceValues + board.whitePawnValues),
                                            RivalConstants.KNIGHT_STAGE_MATERIAL_LOW,
                                            RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH,
                                            Bitboards.pieceSquareTableKnightEndGame.get(toSquare) - Bitboards.pieceSquareTableKnightEndGame.get(fromSquare),
                                            Bitboards.pieceSquareTableKnight.get(toSquare) - Bitboards.pieceSquareTableKnight.get(fromSquare));
                            break;
                        case RivalConstants.WB:
                            ps = Bitboards.pieceSquareTableBishop.get(toSquare) - Bitboards.pieceSquareTableBishop.get(fromSquare);
                            break;
                        case RivalConstants.WR:
                            ps = Bitboards.pieceSquareTableRook.get(toSquare) - Bitboards.pieceSquareTableRook.get(fromSquare);
                            break;
                        case RivalConstants.WQ:
                            ps = Bitboards.pieceSquareTableQueen.get(toSquare) - Bitboards.pieceSquareTableQueen.get(fromSquare);
                            break;
                        case RivalConstants.WK:
                            ps =
                                    Numbers.linearScale(
                                            (board.m_isWhiteToMove ? board.blackPieceValues : board.whitePieceValues),
                                            RivalConstants.VALUE_ROOK,
                                            RivalConstants.OPENING_PHASE_MATERIAL,
                                            Bitboards.pieceSquareTableKingEndGame.get(toSquare) - Bitboards.pieceSquareTableKingEndGame.get(fromSquare),
                                            Bitboards.pieceSquareTableKing.get(toSquare) - Bitboards.pieceSquareTableKing.get(fromSquare));
                            break;
                    }

                    score = 50 + (ps / 2);
                }
                movesForSorting[i] |= ((127 - score) << 24);
            }
        }
    }

    SearchPath zugPath = new SearchPath();

    public SearchPath search(EngineChessBoard board, final int depth, int ply, int low, int high, int extensions, boolean canVerifyNullMove, int recaptureSquare, boolean isCheck) {
        if (!RivalConstants.COUNT_NODES_IN_EVALUATE_ONLY) setNodes(getNodes() + 1);

        if (this.getCurrentTimeMillis() > this.m_searchTargetEndTime || this.getNodes() >= this.m_nodesToSearch) {
            this.m_abortingSearch = true;
            this.setOkToSendInfo(false);
            return null;
        }

        SearchPath newPath, bestPath;

        bestPath = searchPath[ply];
        bestPath.reset();

        if (board.previousOccurrencesOfThisPosition() == 2 || board.m_halfMoveCount >= 100) {
            bestPath.score = RivalConstants.DRAW_CONTEMPT;
            return bestPath;
        }

        if (board.whitePieceValues + board.blackPieceValues + board.whitePawnValues + board.blackPawnValues == 0) {
            bestPath.score = 0;
            return bestPath;
        }

        final int depthRemaining = depth + (extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL);

        byte flag = RivalConstants.UPPERBOUND;

        final int hashIndex = (int) (board.m_hashValue % this.m_maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;
        int hashMove = 0;

        if (RivalConstants.USE_HASH_TABLES) {
            if (RivalConstants.USE_HEIGHT_REPLACE_HASH &&
                    this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] >= depthRemaining &&
                    this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] != RivalConstants.EMPTY) {
                if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] == (int) (board.m_hashValue >>> 32) &&
                        this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] == (int) (board.m_hashValue & Bitboards.LOW32)) {
                    boolean isLocked = this.m_hashTableVersion - this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] <= RivalConstants.MAXIMUM_HASH_AGE;
                    if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
                        for (int i = RivalConstants.WP; i <= RivalConstants.BR && isLocked; i++) {
                            if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] != (int) (board.m_pieceBitboards[i] >>> 32) ||
                                    this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] != (int) (board.m_pieceBitboards[i] & Bitboards.LOW32)) {
                                isLocked = false;
                                this.m_heightBadClashes++;
                                printStream.println("Height bad clash " + board.m_hashValue);
                                System.exit(0);
                            }
                        }
                    }

                    if (isLocked) {
                        this.m_heightHashValuesRetrieved++;
                        this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = this.m_hashTableVersion;
                        hashMove = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_MOVE];
                        if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] == RivalConstants.LOWERBOUND) {
                            if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] > low)
                                low = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE];
                        } else if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] == RivalConstants.UPPERBOUND) {
                            if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] < high)
                                high = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE];
                        }

                        if (this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] == RivalConstants.EXACTSCORE || low >= high) {
                            bestPath.score = this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_SCORE];
                            bestPath.setPath(hashMove);
                            return bestPath;
                        }
                    }
                } else {
                    m_heightHashClashes++;
                }
            }

            if (RivalConstants.USE_ALWAYS_REPLACE_HASH &&
                    hashMove == 0 &&
                    this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_HEIGHT] >= depthRemaining &&
                    this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] != RivalConstants.EMPTY) {
                if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_64BIT1] == (int) (board.m_hashValue >>> 32) &&
                        this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_64BIT2] == (int) (board.m_hashValue & Bitboards.LOW32)) {
                    boolean isLocked = this.m_hashTableVersion - this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] <= RivalConstants.MAXIMUM_HASH_AGE;
                    if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
                        for (int i = RivalConstants.WP; i <= RivalConstants.BR && isLocked; i++) {
                            if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] != (int) (board.m_pieceBitboards[i] >>> 32) ||
                                    this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] != (int) (board.m_pieceBitboards[i] & Bitboards.LOW32)) {
                                isLocked = false;
                                this.m_alwaysBadClashes++;
                                printStream.println("Always bad clash " + board.m_hashValue);
                                System.exit(0);
                            }
                        }
                    }

                    if (isLocked) {
                        this.m_alwaysHashValuesRetrieved++;
                        hashMove = this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_MOVE];
                        if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] == RivalConstants.LOWERBOUND) {
                            if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE] > low)
                                low = this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE];
                        } else if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] == RivalConstants.UPPERBOUND) {
                            if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE] < high)
                                high = this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE];
                        }

                        if (this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] == RivalConstants.EXACTSCORE || low >= high) {
                            bestPath.score = this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_SCORE];
                            bestPath.setPath(hashMove);
                            return bestPath;
                        }
                    }
                } else {
                    m_alwaysHashClashes++;
                }
            }
        }

        int checkExtend = 0;
        if ((extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
            checkExtend = 0;
            if (RivalConstants.FRACTIONAL_EXTENSION_CHECK > 0 && isCheck) {
                checkExtend = 1;
                checkExtensions++;
            }
        }

        if (depthRemaining <= 0) {
            bestPath = quiesce(board, RivalConstants.MAX_QUIESCE_DEPTH - 1, ply, 0, low, high, isCheck);
            if (bestPath.score < low) flag = RivalConstants.UPPERBOUND;
            else if (bestPath.score > high) flag = RivalConstants.LOWERBOUND;
            else
                flag = RivalConstants.EXACTSCORE;

            storeHashMove(0, board, bestPath.score, flag, 0);
            return bestPath;
        }

        if (RivalConstants.USE_INTERNAL_ITERATIVE_DEEPENING && depthRemaining >= RivalConstants.IID_MIN_DEPTH && hashMove == 0 && !board.isOnNullMove()) {
            boolean doIt = true;

            if (RivalConstants.IID_PV_NODES_ONLY) {
                int lowCheck = (ply % 2 == 0) ? aspirationLow : -aspirationHigh;
                int highCheck = (ply % 2 == 0) ? aspirationHigh : -aspirationLow;
                doIt = (low == lowCheck && high == highCheck);
            }
            if (doIt) {
                if (depth - RivalConstants.IID_REDUCE_DEPTH > 0) {
                    newPath = search(board, (byte) (depth - RivalConstants.IID_REDUCE_DEPTH), ply, low, high, extensions, canVerifyNullMove, recaptureSquare, isCheck);
                    // it's not really a hash move, but this will cause the order routine to rank it first
                    if (newPath != null && newPath.height > 0) hashMove = newPath.move[0];
                }
            }
            bestPath.reset();
            // We reset this here because it may have been mucked about with during IID
            // Notice that the search calls with ply, not ply+1, because search is for this level (we haven't made a move)
        }

        int bestMoveForHash = 0;
        boolean scoutSearch = false;
        byte verifyingNullMoveDepthReduction = 0;
        int threatExtend = 0, pawnExtend = 0;

        int nullMoveReduceDepth = (depthRemaining > RivalConstants.NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE) ? RivalConstants.NULLMOVE_REDUCE_DEPTH + 1 : RivalConstants.NULLMOVE_REDUCE_DEPTH;
        if (RivalConstants.USE_NULLMOVE_PRUNING && !isCheck && !board.isOnNullMove() && depthRemaining > 1) {
            if ((board.m_isWhiteToMove ? board.whitePieceValues : board.blackPieceValues) >= RivalConstants.NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES &&
                    (board.m_isWhiteToMove ? board.whitePawnValues : board.blackPawnValues) > 0) {
                board.makeNullMove();
                newPath = search(board, (byte) (depth - nullMoveReduceDepth - 1), ply + 1, -high, -low, extensions, canVerifyNullMove, -1, false);
                if (newPath != null) if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                if (!this.m_abortingSearch) {
                    if (-newPath.score >= high) {
                        if (RivalConstants.USE_VERIFIED_NULLMOVE && (board.m_isWhiteToMove ? board.whitePieceValues : board.blackPieceValues) < RivalConstants.VERIFIED_WHEN_PIECEVALUES_LESS_THAN && canVerifyNullMove) {
                            // rather than return here as we would normally when finding a null move cutoff,
                            // we will perform a normal search with a reduced depth (-1)
                            // If, after the search we find that there was no cutoff, it indicates that we were right
                            // to be cautious, so we re-search with a normal depth
                            verifyingNullMoveDepthReduction = 1;
                            if (RivalConstants.DEBUG_ZUGZWANGS) {
                                zugPath.setPath(newPath);
                            }
                        } else {
                            bestPath.score = -newPath.score;
                            board.unMakeNullMove();
                            return bestPath;
                        }
                    } else if (
                            RivalConstants.FRACTIONAL_EXTENSION_THREAT > 0 &&
                                    -newPath.score < -RivalConstants.MATE_SCORE_START &&
                                    (extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
                        threatExtensions++;
                        threatExtend = 1;
                    } else {
                        threatExtend = 0;
                    }
                }
                board.unMakeNullMove();
            }
        }

        int[] theseMoves = orderedMoves[ply];
        board.setLegalMoves(theseMoves);
        moveOrderStatus[ply] = MoveOrder.NONE;

        boolean research;

        do {
            research = false;
            int legalMoveCount = 0;

            int futilityPruningEvaluation = -RivalConstants.INFINITY;

            boolean wasCheckBeforeMove = board.isCheck();

            // Check to see if we can futility prune this whole node
            boolean canFutilityPrune = false;
            int futilityScore = low;
            if (RivalConstants.USE_FUTILITY_PRUNING) {
                if (depthRemaining < 4 && !wasCheckBeforeMove && threatExtend == 0 && Math.abs(low) < RivalConstants.MATE_SCORE_START && Math.abs(high) < RivalConstants.MATE_SCORE_START) {
                    if (futilityPruningEvaluation == -RivalConstants.INFINITY)
                        futilityPruningEvaluation = evaluate(board);
                    futilityScore = futilityPruningEvaluation + RivalConstants.FUTILITY_MARGIN.get(depthRemaining - 1);
                    if (futilityScore < low) canFutilityPrune = true;
                }
            }

            int lateMoveReductionsMade = 0;
            int newExtensions = 0;
            int reductions = 0;

            int move;
            while ((move = getHighScoreMove(board, ply, hashMove)) != 0 && !this.m_abortingSearch) {
                final int targetPiece = board.squareContents[move & 63];
                final int movePiece = board.squareContents[(move >>> 16) & 63];
                int recaptureExtend = 0;

                int newRecaptureSquare = -1;

                int currentSEEValue = -RivalConstants.INFINITY;
                if (RivalConstants.FRACTIONAL_EXTENSION_RECAPTURE > 0 && (extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
                    recaptureExtensionAttempts++;
                    recaptureExtend = 0;
                    if (targetPiece != -1 && RivalConstants.PIECE_VALUES.get(movePiece) == RivalConstants.PIECE_VALUES.get(targetPiece)) {
                        currentSEEValue = staticExchangeEvaluation(board, move);
                        if (Math.abs(currentSEEValue) <= RivalConstants.RECAPTURE_EXTENSION_MARGIN)
                            newRecaptureSquare = (move & 63);
                    }

                    if ((move & 63) == recaptureSquare) {
                        if (currentSEEValue == -RivalConstants.INFINITY)
                            currentSEEValue = staticExchangeEvaluation(board, move);
                        if (Math.abs(currentSEEValue) > RivalConstants.PIECE_VALUES.get(board.squareContents[recaptureSquare]) - RivalConstants.RECAPTURE_EXTENSION_MARGIN) {
                            recaptureExtend = 1;
                            recaptureExtensions++;
                        }
                    }
                }

                if (board.makeMove(move)) {
                    legalMoveCount++;

                    isCheck = board.isCheck();

                    if (RivalConstants.USE_FUTILITY_PRUNING && canFutilityPrune && !isCheck && !board.wasCapture() && !board.wasPawnPush()) {
                        m_futilityPrunes++;
                        newPath = searchPath[ply + 1];
                        newPath.reset();
                        newPath.score = -futilityScore; // newPath.score gets reversed later
                    } else {
                        if ((extensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) < RivalConstants.MAX_EXTENSION_DEPTH) {
                            pawnExtend = 0;
                            if (RivalConstants.FRACTIONAL_EXTENSION_PAWN > 0) {
                                if (board.wasPawnPush()) {
                                    pawnExtend = 1;
                                    pawnExtensions++;
                                }
                            }
                        }

                        int partOfTree = ply / this.m_iterativeDeepeningCurrentDepth;
                        int maxNewExtensionsInThisPart = RivalConstants.MAX_NEW_EXTENSIONS_TREE_PART.get(partOfTree > RivalConstants.LAST_EXTENSION_LAYER ? RivalConstants.LAST_EXTENSION_LAYER : partOfTree);

                        newExtensions =
                                extensions +
                                        Math.min(
                                                (checkExtend * RivalConstants.FRACTIONAL_EXTENSION_CHECK) +
                                                        (threatExtend * RivalConstants.FRACTIONAL_EXTENSION_THREAT) +
                                                        (recaptureExtend * RivalConstants.FRACTIONAL_EXTENSION_RECAPTURE) +
                                                        (pawnExtend * RivalConstants.FRACTIONAL_EXTENSION_PAWN),
                                                maxNewExtensionsInThisPart);

                        int lateMoveReduction = 0;
                        if (
                                RivalConstants.USE_LATE_MOVE_REDUCTIONS &&
                                        newExtensions == 0 &&
                                        legalMoveCount > RivalConstants.LMR_LEGALMOVES_BEFORE_ATTEMPT &&
                                        depthRemaining - verifyingNullMoveDepthReduction > 1 &&
                                        move != hashMove &&
                                        !wasCheckBeforeMove &&
                                        historyPruneMoves[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] <= RivalConstants.LMR_THRESHOLD &&
                                        !board.wasCapture() &&
                                        (RivalConstants.FRACTIONAL_EXTENSION_PAWN > 0 || !board.wasPawnPush())) {
                            if (-evaluate(board) <= low + RivalConstants.LMR_CUT_MARGIN) {
                                lateMoveReduction = 1;
                                lateMoveReductions++;
                                historyPruneMoves[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] = RivalConstants.LMR_REPLACE_VALUE_AFTER_CUT;
                            }
                        }

                        if (RivalConstants.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION > -1) {
                            lateMoveReductionsMade += lateMoveReduction;
                            if (lateMoveReductionsMade > RivalConstants.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION && depthRemaining > 3) {
                                lateMoveDoubleReductions++;
                                lateMoveReduction = 2;
                            }
                        }

                        reductions = verifyingNullMoveDepthReduction + lateMoveReduction;
                        boolean lmrResearch;

                        do {
                            lmrResearch = false;
                            if (scoutSearch) {
                                newPath = search(m_board, (byte) (depth - 1) - reductions, ply + 1, -low - 1, -low, newExtensions, canVerifyNullMove, newRecaptureSquare, isCheck);
                                if (newPath != null)
                                    if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                                    else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                                if (!this.m_abortingSearch && -newPath.score > low) {
                                    // research with normal window
                                    newPath = search(m_board, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, canVerifyNullMove, newRecaptureSquare, isCheck);
                                    if (newPath != null)
                                        if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                                        else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                                }
                            } else {
                                newPath = search(board, (byte) (depth - 1) - reductions, ply + 1, -high, -low, newExtensions, canVerifyNullMove, newRecaptureSquare, isCheck);
                                if (newPath != null)
                                    if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                                    else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                            }
                            if (!this.m_abortingSearch && lateMoveReduction > 0 && -newPath.score >= low) {
                                lmrResearch = RivalConstants.LMR_RESEARCH_ON_FAIL_HIGH;
                                lateMoveReduction = 0;
                            }
                        }
                        // this move lead to a beta cut-off so research without the reduction
                        while (lmrResearch);
                    }

                    if (!this.m_abortingSearch) {
                        newPath.score = -newPath.score;

                        if (newPath.score >= high) {
                            if (RivalConstants.USE_HISTORY_HEURISTIC) {
                                historyMovesSuccess[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] += depthRemaining;
                                if (historyMovesSuccess[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] > RivalConstants.HISTORY_MAX_VALUE) {
                                    for (int i = 0; i < 2; i++)
                                        for (int j = 0; j < 64; j++)
                                            for (int k = 0; k < 64; k++) {
                                                if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2;
                                                if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2;
                                            }
                                }
                            }

                            if (RivalConstants.USE_LATE_MOVE_REDUCTIONS)
                                historyPruneMoves[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] += RivalConstants.LMR_ABOVE_ALPHA_ADDITION;

                            board.unMakeMove();
                            bestPath.setPath(move, newPath);
                            storeHashMove(move, board, newPath.score, RivalConstants.LOWERBOUND, depthRemaining);

                            if (RivalConstants.NUM_KILLER_MOVES > 0) {
                                if ((board.m_pieceBitboards[RivalConstants.ENEMY] & (move & 63)) == 0 || (move & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL) == 0) {
                                    // if this move is in second place, or if it's not in the table at all,
                                    // then move first to second, and replace first with this move
                                    if (killerMoves[ply][0] != move) {
                                        for (int j = RivalConstants.NUM_KILLER_MOVES - 1; j > 0; j--) {
                                            killerMoves[ply][j] = killerMoves[ply][j - 1];
                                        }
                                        killerMoves[ply][0] = move;
                                    }
                                    if (RivalConstants.USE_MATE_HISTORY_KILLERS && newPath.score > RivalConstants.MATE_SCORE_START) {
                                        mateKiller[ply] = move;
                                    }
                                }
                            }
                            //profileStop(10);
                            return bestPath;
                        }

                        if (RivalConstants.USE_HISTORY_HEURISTIC) {
                            historyMovesFail[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] += depthRemaining;
                            if (historyMovesFail[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] > RivalConstants.HISTORY_MAX_VALUE) {
                                for (int i = 0; i < 2; i++)
                                    for (int j = 0; j < 64; j++)
                                        for (int k = 0; k < 64; k++) {
                                            if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2;
                                            if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2;
                                        }
                            }
                        }

                        if (newPath.score > bestPath.score) {
                            bestPath.setPath(move, newPath);
                        }

                        if (newPath.score > low) {
                            flag = RivalConstants.EXACTSCORE;
                            bestMoveForHash = move;
                            low = newPath.score;
                            scoutSearch = RivalConstants.USE_PV_SEARCH && depth - reductions + (newExtensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) >= RivalConstants.PV_MINIMUM_DISTANCE_FROM_LEAF;

                            if (RivalConstants.USE_LATE_MOVE_REDUCTIONS)
                                historyPruneMoves[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] += RivalConstants.LMR_ABOVE_ALPHA_ADDITION;
                        } else if (RivalConstants.USE_LATE_MOVE_REDUCTIONS) {
                            historyPruneMoves[board.m_isWhiteToMove ? 1 : 0][(move >>> 16) & 63][(move & 63)] -= RivalConstants.LMR_NOT_ABOVE_ALPHA_REDUCTION;
                        }
                    }
                    board.unMakeMove();
                }
            }

            if (!this.m_abortingSearch) {
                if (legalMoveCount == 0) {
                    boolean isMate = board.m_isWhiteToMove ? board.isSquareAttacked(board.m_whiteKingSquare, false) : board.isSquareAttacked(board.m_blackKingSquare, true);
                    bestPath.score = isMate ? -RivalConstants.VALUE_MATE : 0;
                    storeHashMove(0, board, bestPath.score, RivalConstants.EXACTSCORE, RivalConstants.MAX_SEARCH_DEPTH);
                    return bestPath;
                }

                if (RivalConstants.USE_VERIFIED_NULLMOVE && verifyingNullMoveDepthReduction == 1 && bestPath.score < high) {
                    verifyingNullMoveDepthReduction = 0;
                    canVerifyNullMove = true;
                    m_zugzwangCount++;
                    research = true;
                    if (RivalConstants.DEBUG_ZUGZWANGS) {
                        board.printBoard();
                        board.printPreviousMoves();
                        printStream.println("Null move gave a score of " + -zugPath.score + " " + zugPath + " which was better than " + high + " but the best move at a reduced depth looks like " + bestPath.score + " " + bestPath);
                        System.exit(0);
                    }
                }

                if (!research) {
                    storeHashMove(bestMoveForHash, board, bestPath.score, (byte) flag, depthRemaining);
                    return bestPath;
                }
            }
        }
        while (research);

        return null;
    }

    public SearchPath searchZero(EngineChessBoard board, byte depth, int ply, int low, int high) {
        if (!RivalConstants.COUNT_NODES_IN_EVALUATE_ONLY) setNodes(getNodes() + 1);

        int numMoves = 0;
        byte flag = RivalConstants.UPPERBOUND;
        int move;
        int bestMoveForHash = 0;

        move = orderedMoves[0][numMoves] & 0x00FFFFFF; // clear sort score

        SearchPath newPath;
        SearchPath bestPath = searchPath[0];
        bestPath.reset();

        int numLegalMovesAtDepthZero = 0;

        boolean scoutSearch = false;

        int checkExtend = 0, pawnExtend = 0;

        while (move != 0 && !this.m_abortingSearch) {
            if (m_board.makeMove(move)) {
                boolean isCheck = board.isCheck();

                totalMovesSearchedAtAllDepths++;
                checkExtend = 0;
                pawnExtend = 0;
                if (RivalConstants.FRACTIONAL_EXTENSION_CHECK > 0 && isCheck) {
                    checkExtend = 1;
                    checkExtensions++;
                } else if (RivalConstants.FRACTIONAL_EXTENSION_PAWN > 0) {
                    if (board.wasPawnPush()) {
                        pawnExtend = 1;
                        pawnExtensions++;
                    }
                }

                int newExtensions =
                        Math.min(
                                (checkExtend * RivalConstants.FRACTIONAL_EXTENSION_CHECK) +
                                        (pawnExtend * RivalConstants.FRACTIONAL_EXTENSION_PAWN),
                                RivalConstants.FRACTIONAL_EXTENSION_FULL);

                numLegalMovesAtDepthZero++;

                m_currentDepthZeroMove = move;
                m_currentDepthZeroMoveNumber = numLegalMovesAtDepthZero;

                boolean canMakeNullMove = true;
                if (isDrawnAtRoot(m_board, 1)) {
                    int hashIndex = (int) (board.m_hashValue % this.m_maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;
                    this.hashTableAlways[hashIndex + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
                    this.hashTableHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = RivalConstants.EMPTY;
                    canMakeNullMove = false;
                }

                if (isDrawnAtRoot(m_board, 0)) {
                    newPath = new SearchPath();
                    newPath.score = 0;
                    newPath.setPath(move);
                } else {
                    if (scoutSearch) {
                        newPath = search(m_board, (byte) (depth - 1), ply + 1, -low - 1, -low, newExtensions, canMakeNullMove, -1, isCheck);
                        if (newPath != null) if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                        else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                        if (!this.m_abortingSearch && -newPath.score > low) {
                            newPath = search(m_board, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, canMakeNullMove, -1, isCheck);
                            if (newPath != null) if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                            else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                        }
                    } else {
                        newPath = search(m_board, (byte) (depth - 1), ply + 1, -high, -low, newExtensions, canMakeNullMove, -1, isCheck);
                        if (newPath != null) if (newPath.score > RivalConstants.MATE_SCORE_START) newPath.score--;
                        else if (newPath.score < -RivalConstants.MATE_SCORE_START) newPath.score++;
                    }
                }
                if (!this.m_abortingSearch) {
                    newPath.score = -newPath.score;

                    if (newPath.score >= high) {
                        board.unMakeMove();
                        bestPath.setPath(move, newPath);
                        storeHashMove(move, board, newPath.score, RivalConstants.LOWERBOUND, depth);
                        depthZeroMoveScores[numMoves] = newPath.score;
                        return bestPath;
                    }

                    if (newPath.score > bestPath.score) {
                        bestPath.setPath(move, newPath);
                    }

                    if (newPath.score > low) {
                        flag = RivalConstants.EXACTSCORE;
                        bestMoveForHash = move;
                        low = newPath.score;

                        scoutSearch = RivalConstants.USE_PV_SEARCH && depth + (newExtensions / RivalConstants.FRACTIONAL_EXTENSION_FULL) >= RivalConstants.PV_MINIMUM_DISTANCE_FROM_LEAF;
                        m_currentPath.setPath(bestPath);
                        m_currentPathString = "" + m_currentPath;
                    }

                    depthZeroMoveScores[numMoves] = newPath.score;
                }
                m_board.unMakeMove();
            } else {
                depthZeroMoveScores[numMoves] = -RivalConstants.INFINITY;
            }
            numMoves++;
            move = orderedMoves[0][numMoves] & 0x00FFFFFF;
        }

        if (!this.m_abortingSearch) {
            if (numLegalMovesAtDepthZero == 1 && this.m_millisecondsToThink < RivalConstants.MAX_SEARCH_MILLIS) {
                this.m_abortingSearch = true;
                m_currentPath.setPath(bestPath); // otherwise we will crash!
                m_currentPathString = "" + m_currentPath;
            } else {
                storeHashMove(bestMoveForHash, board, bestPath.score, flag, depth);
            }

            return bestPath;
        } else {
            return null;
        }
    }

    public void go() {
        this.m_wasSearchCancelled = false;
        this.m_searchState = RivalConstants.SEARCHSTATE_SEARCHING;
        this.m_abortingSearch = false;
        this.totalMovesSearchedAtAllDepths = 0;

        this.m_hashTableVersion++;

        m_largestEvalDifference = 0;

        this.m_searchStartTime = System.currentTimeMillis();
        this.m_searchEndTime = 0;
        this.m_searchTargetEndTime = this.m_searchStartTime + this.m_millisecondsToThink - RivalConstants.UCI_TIMER_INTERVAL_MILLIS;

        this.m_heightHashClashes = 0;
        this.m_alwaysHashClashes = 0;
        this.m_alwaysHashValuesRetrieved = 0;
        this.m_heightHashValuesRetrieved = 0;
        this.m_alwaysHashValuesStored = 0;
        this.m_heightHashValuesStored = 0;
        this.m_pawnHashValuesStored = 0;
        this.m_evalHashValuesStored = 0;

        this.m_zugzwangCount = 0;

        aspirationLow = -RivalConstants.INFINITY;
        aspirationHigh = RivalConstants.INFINITY;
        this.setNodes(0);

        for (int i = 0; i < RivalConstants.MAX_TREE_DEPTH; i++) {
            this.mateKiller[i] = -1;
            for (int j = 0; j < RivalConstants.NUM_KILLER_MOVES; j++) {
                this.killerMoves[i][j] = -1;
            }
        }

        for (int i = 0; i < 64; i++)
            for (int j = 0; j < 64; j++) {
                historyMovesSuccess[0][i][j] = 0;
                historyMovesSuccess[1][i][j] = 0;
                historyMovesFail[0][i][j] = 0;
                historyMovesFail[1][i][j] = 0;
                historyPruneMoves[0][i][j] = RivalConstants.LMR_INITIAL_VALUE;
                historyPruneMoves[1][i][j] = RivalConstants.LMR_INITIAL_VALUE;
            }

        SearchPath path;

        boolean m_isDebug = false;
        if (m_isDebug) {
            printStream.println("Hash: " + m_board.m_hashValue);
            m_board.printLegalMoves(true, true);
            printStream.println("Is Check = " + m_board.isCheck());
            printStream.println("Eval = " + evaluate(m_board));
            SearchPath sp = quiesce(m_board, 40, 0, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY, m_board.isCheck());
            printStream.println("Quiesce = " + sp.score);
            printStream.println("FEN = " + m_board.getFen());
        }

        try {
            m_board.setLegalMoves(depthZeroLegalMoves);
            int depthZeroMoveCount = 0;
            int currentDepthZeroValidMoves = 0;

            int c = 0;
            int[] depth1MovesTemp = new int[RivalConstants.MAX_LEGAL_MOVES];
            int move = depthZeroLegalMoves[c] & 0x00FFFFFF;
            getDrawnPositionsAtRootCount()[0] = 0;
            getDrawnPositionsAtRootCount()[1] = 0;
            int legal = 0;
            int bestNewbieScore = -RivalConstants.INFINITY;

            while (move != 0) {
                if (m_board.makeMove(move)) {
                    boolean ply0Draw = false;
                    boolean ply1Draw = false;

                    legal++;

                    if (this.m_iterativeDeepeningCurrentDepth < 1) // super beginner mode
                    {
                        SearchPath sp = quiesce(m_board, 40, 1, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY, m_board.isCheck());
                        sp.score = -sp.score;
                        if (sp.score > bestNewbieScore) {
                            bestNewbieScore = sp.score;
                            m_currentPath.reset();
                            m_currentPath.setPath(move);
                            m_currentPath.score = sp.score;
                            m_currentPathString = "" + m_currentPath;
                        }
                    } else if (legal == 1) {
                        // use this opportunity to set a move in the odd event that there is no time to search
                        m_currentPath.reset();
                        m_currentPath.setPath(move);
                        m_currentPath.score = 0;
                        m_currentPathString = "" + m_currentPath;
                    }
                    if (m_board.previousOccurrencesOfThisPosition() == 2) {
                        ply0Draw = true;
                    }

                    m_board.setLegalMoves(depth1MovesTemp);

                    int c1 = 0;
                    int move1 = depth1MovesTemp[c1] & 0x00FFFFFF;

                    while (move1 != 0) {
                        if (m_board.makeMove(move1)) {
                            if (m_board.previousOccurrencesOfThisPosition() == 2) {
                                ply1Draw = true;
                            }
                            m_board.unMakeMove();
                        }
                        move1 = depth1MovesTemp[++c1] & 0x00FFFFFF;
                    }

                    if (ply0Draw) {
                        drawnPositionsAtRoot.get(0).add(m_board.m_hashValue);
                    }
                    if (ply1Draw) {
                        drawnPositionsAtRoot.get(1).add(m_board.m_hashValue);
                    }

                    m_board.unMakeMove();
                    currentDepthZeroValidMoves++;
                }
                depthZeroMoveCount++;

                move = depthZeroLegalMoves[++c] & 0x00FFFFFF;
            }

            if (this.m_useOpeningBook && m_board.getFen().trim().equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -")) {
                this.m_inBook = true;
            }

            if (this.m_inBook) {
                int libraryMove = m_openingLibrary.getMove(m_board.getFen());
                if (libraryMove > 0 && m_board.isMoveLegal(libraryMove)) {
                    path = new SearchPath();
                    path.setPath(libraryMove);
                    m_currentPath = path;
                    m_currentPathString = "" + m_currentPath;
                    setSearchComplete();
                    return;
                } else {
                    this.m_inBook = false;
                }
            }

            while (depthZeroLegalMoves[depthZeroMoveCount] != 0) depthZeroMoveCount++;

            scoreFullWidthMoves(m_board, 0);

            this.movesToSearchAtAllDepths = currentDepthZeroValidMoves * this.m_finalDepthToSearch;

            for (byte depth = 1; depth <= this.m_finalDepthToSearch && !this.m_abortingSearch; depth++) {
                this.m_iterativeDeepeningCurrentDepth = depth;

                if (depth > 1) setOkToSendInfo(true);

                if (RivalConstants.USE_ASPIRATION_WINDOW) {
                    path = searchZero(m_board, depth, 0, aspirationLow, aspirationHigh);

                    if (!this.m_abortingSearch && path.score <= aspirationLow) {
                        aspirationLow = -RivalConstants.INFINITY;
                        path = searchZero(m_board, depth, 0, aspirationLow, aspirationHigh);
                    } else if (!this.m_abortingSearch && path.score >= aspirationHigh) {
                        aspirationHigh = RivalConstants.INFINITY;
                        path = searchZero(m_board, depth, 0, aspirationLow, aspirationHigh);
                    }

                    if (!this.m_abortingSearch && (path.score <= aspirationLow || path.score >= aspirationHigh)) {
                        path = searchZero(m_board, depth, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY);
                    }

                    if (!this.m_abortingSearch) {
                        m_currentPath.setPath(path);
                        m_currentPathString = "" + m_currentPath;
                        aspirationLow = path.score - RivalConstants.ASPIRATION_RADIUS;
                        aspirationHigh = path.score + RivalConstants.ASPIRATION_RADIUS;
                    }
                } else {
                    path = searchZero(m_board, depth, 0, -RivalConstants.INFINITY, RivalConstants.INFINITY);
                }

                if (!this.m_abortingSearch) {
                    m_currentPath.setPath(path);
                    m_currentPathString = "" + m_currentPath;
                    if (path.score > RivalConstants.MATE_SCORE_START) {
                        setSearchComplete();
                        if (this.m_epdPosition != null) {
                            m_epdPosition.setPlyMove(depth - 1, ChessBoardConversion.getPGNMoveFromCompactMove(getCurrentMove(), m_board));
                        }
                        return;
                    }
                    for (int pass = 1; pass < depthZeroMoveCount; pass++) {
                        for (int i = 0; i < depthZeroMoveCount - pass; i++) {
                            if (depthZeroMoveScores[i] < depthZeroMoveScores[i + 1]) {
                                int tempScore;
                                tempScore = depthZeroMoveScores[i];
                                depthZeroMoveScores[i] = depthZeroMoveScores[i + 1];
                                depthZeroMoveScores[i + 1] = tempScore;

                                int tempMove;
                                tempMove = orderedMoves[0][i];
                                orderedMoves[0][i] = orderedMoves[0][i + 1];
                                orderedMoves[0][i + 1] = tempMove;
                            }
                        }
                    }
                }
                if (this.m_epdPosition != null) {
                    m_epdPosition.setPlyMove(depth - 1, ChessBoardConversion.getPGNMoveFromCompactMove(getCurrentMove(), m_board));
                    if (m_epdPosition.canTerminate()) {
                        setSearchComplete();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setSearchComplete();
            if (RivalConstants.UCI_DEBUG) {
                printStream.println("My search is finally complete");
            }
        }
    }

    public synchronized int getEngineState() {
        return this.m_searchState;
    }

    public void setMillisToThink(int millisToThink) {
        this.m_millisecondsToThink = millisToThink;

        if (this.m_millisecondsToThink < RivalConstants.MIN_SEARCH_MILLIS) {
            this.m_millisecondsToThink = RivalConstants.MIN_SEARCH_MILLIS;
        }
    }

    public void setNodesToSearch(int nodesToSearch) {
        this.m_nodesToSearch = nodesToSearch;
    }

    public void setSearchDepth(int searchDepth) {
        if (searchDepth == -1) {
            Random randomGenerator = new Random();
            searchDepth = randomGenerator.nextInt(3);
            if (searchDepth == 2) {
                searchDepth = 1;
            }
        }
        this.m_finalDepthToSearch = searchDepth;
    }

    public int getNodes() {
        return this.nodes;
    }

    public synchronized boolean isSearching() {
        return this.m_searchState == RivalConstants.SEARCHSTATE_SEARCHING || this.m_searchState == RivalConstants.SEARCHSTATE_SEARCHREQUESTED;
    }

    public int getCurrentScore() {
        return this.m_currentPath.getScore();
    }

    public String getCurrentScoreHuman() {
        int score = this.m_currentPath.getScore();
        int abs = Math.abs(score);
        if (abs > RivalConstants.MATE_SCORE_START) {
            int mateIn = ((RivalConstants.VALUE_MATE - abs) + 1) / 2;
            return "mate " + (score < 0 ? "-" : "") + mateIn;
        }
        return "cp " + score;
    }

    public double getSearchPercentComplete() {
        // return 0 if search not running
        double percent = 0.0;
        long now, duration;

        if (isSearching()) {
            now = System.currentTimeMillis();
            duration = now - this.m_searchStartTime;
            if (this.m_millisecondsToThink == RivalConstants.MAX_SEARCH_MILLIS) {
                if (totalMovesSearchedAtAllDepths > 0 && movesToSearchAtAllDepths > 0) {
                    percent = ((double) totalMovesSearchedAtAllDepths / movesToSearchAtAllDepths) * 100.0;
                }
            } else {
                percent = ((double) duration / this.m_millisecondsToThink) * 100.0;
            }
        }

        return percent;
    }

    public long getSearchDuration() {
        long timePassed = 0;
        switch (this.m_searchState) {
            case RivalConstants.SEARCHSTATE_READY:
                timePassed = 0;
                break;
            case RivalConstants.SEARCHSTATE_SEARCHING:
                timePassed = System.currentTimeMillis() - this.m_searchStartTime;
                break;
            case RivalConstants.SEARCHSTATE_SEARCHCOMPLETE:
                timePassed = this.m_searchEndTime - this.m_searchStartTime;
                break;
        }
        return timePassed;
    }

    public int getNodesPerSecond() {
        long timePassed = getSearchDuration();
        if (timePassed == 0) {
            return 0;
        } else {
            return (int) (((double) this.getNodes() / (double) timePassed) * 1000.0);
        }
    }

    private boolean isDrawnAtRoot(EngineChessBoard board, int ply) {
        int i;
        for (i = 0; i < getDrawnPositionsAtRootCount()[ply]; i++) {
            if (drawnPositionsAtRoot.get(ply).get(i).equals(board.m_hashValue)) {
                return true;
            }
        }
        return false;
    }

    public int getIterativeDeepeningDepth() {
        return this.m_iterativeDeepeningCurrentDepth;
    }

    public int getCurrentMove() {
        return this.m_currentPath.move[0];
    }

    public String getCurrentPathString() {
        return this.m_currentPathString; // don't want to calculate it if called from another thread
    }

    public synchronized void startSearch() {
        this.m_searchState = RivalConstants.SEARCHSTATE_SEARCHREQUESTED;
        if (RivalConstants.UCI_DEBUG) {
            printStream.println("My search has been requested");
        }
    }

    public synchronized void stopSearch() {
        if (isSearching()) {
            if (RivalConstants.UCI_DEBUG) {
                if (this.m_abortingSearch) {
                    printStream.println("My current search is no longer wanted, I know, I'm already aborting");
                } else {
                    printStream.println("My current search is no longer wanted, I'll abort then");
                }
            }
        } else {
            if (RivalConstants.UCI_DEBUG) {
                if (this.m_abortingSearch) {
                    printStream.println("I'm not even searching but am being asked to stop, and I'm already aborting");
                } else {
                    printStream.println("I'm not even searching but am being asked to stop, and weirdly m_abortingSearch is false");
                }
            }
        }
        this.m_abortingSearch = true;
        this.m_wasSearchCancelled = true;
    }

    public boolean wasSearchCancelled() {
        return this.m_wasSearchCancelled;
    }

    public synchronized void setSearchComplete() {
        this.m_searchEndTime = System.currentTimeMillis();
        this.m_searchState = RivalConstants.SEARCHSTATE_SEARCHCOMPLETE;
        this.m_previousSearchMillis = this.m_searchEndTime - this.m_searchStartTime;
        this.m_previousFinalDepthToSearch = this.m_finalDepthToSearch;
    }

    public void setUseOpeningBook(boolean useBook) {
        this.m_useOpeningBook = RivalConstants.USE_INTERNAL_OPENING_BOOK && useBook;
        this.m_inBook = this.m_useOpeningBook;
    }

    public boolean isAbortingSearch() {
        return this.m_abortingSearch;
    }

    public void run() {
        while (!quit) {
            Thread.yield();
            if (this.m_searchState == RivalConstants.SEARCHSTATE_SEARCHREQUESTED) {
                go();

                setOkToSendInfo(false);

                if (m_isUCIMode) {
                    String s1 =
                            "info" +
                                    " currmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(m_currentDepthZeroMove) +
                                    " currmovenumber " + m_currentDepthZeroMoveNumber +
                                    " depth " + getIterativeDeepeningDepth() +
                                    " score " + getCurrentScoreHuman() +
                                    " pv " + getCurrentPathString() +
                                    " time " + getSearchDuration() +
                                    " nodes " + getNodes() +
                                    " nps " + getNodesPerSecond();

                    String s2 = "bestmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(getCurrentMove());
                    printStream.println(s1);
                    if (m_out != null) Logger.log(m_out, s1, "<");
                    printStream.println(s2);
                    if (m_out != null) Logger.log(m_out, s2, "<");
                }
            }
        }
    }

    public boolean isOkToSendInfo() {
        return isOkToSendInfo;
    }

    public void setOkToSendInfo(boolean okToSendInfo) {
        this.isOkToSendInfo = okToSendInfo;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public List<List<Long>> getDrawnPositionsAtRoot() {
        return drawnPositionsAtRoot;
    }

    public int[] getDrawnPositionsAtRootCount() {
        return drawnPositionsAtRootCount;
    }

}
