package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.config.Evaluation;
import com.netsensia.rivalchess.config.FeatureFlag;
import com.netsensia.rivalchess.enums.Colour;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.eval.PawnHashEntry;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.enums.HashValueType;
import com.netsensia.rivalchess.enums.PawnHashIndex;
import com.netsensia.rivalchess.enums.SquareOccupant;
import com.netsensia.rivalchess.util.Numbers;

public class BoardHash {

    private long lastPawnHashValue = -1;
    private ZorbristHashTracker hashCalculator = new ZorbristHashTracker();

    private PawnHashEntry lastPawnHashEntry = new PawnHashEntry();

    private int hashTableVersion;
    private int[] hashTableUseHeight;
    private int[] hashTableIgnoreHeight;
    private long[] pawnHashTable;
    private int maxHashEntries;
    private int maxPawnHashEntries;
    private int lastHashSizeCreated;

    public int getHashTableUseHeight(int index) {
        return hashTableUseHeight[index];
    }

    public int getHashTableIgnoreHeight(int index) {
        return hashTableIgnoreHeight[index];
    }

    public void setHashTableUseHeight(int index, int value) {
        hashTableUseHeight[index] = value;
    }

    public void setPawnHashTable(int index, long value) {
        pawnHashTable[index] = value;
    }

    public synchronized void clearHash() {
        for (int i = 0; i < maxHashEntries; i++) {
            this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = HashValueType.EMPTY.getIndex();
            this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
            this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = HashValueType.EMPTY.getIndex();
            this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
            if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                this.pawnHashTable[i * RivalConstants.NUM_PAWNHASH_FIELDS + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
            }
        }
    }

    public synchronized void setHashTable() {
        if (maxHashEntries != lastHashSizeCreated) {
            this.hashTableUseHeight = new int[maxHashEntries * RivalConstants.NUM_HASH_FIELDS];
            this.hashTableIgnoreHeight = new int[maxHashEntries * RivalConstants.NUM_HASH_FIELDS];
            if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                this.pawnHashTable = new long[maxHashEntries * RivalConstants.NUM_PAWNHASH_FIELDS];
            }
            lastHashSizeCreated = maxHashEntries;
            for (int i = 0; i < maxHashEntries; i++) {
                this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = HashValueType.EMPTY.getIndex();
                this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableUseHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_VERSION] = 1;
                this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_FLAG] = HashValueType.EMPTY.getIndex();
                this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_HEIGHT] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableIgnoreHeight[i * RivalConstants.NUM_HASH_FIELDS + RivalConstants.HASHENTRY_VERSION] = 1;
                if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                    this.pawnHashTable[i * RivalConstants.NUM_PAWNHASH_FIELDS + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
                }
            }
        }
    }

    public void storeHashMove(int move, EngineChessBoard board, int score, byte flag, int height) {
        final int hashIndex = (int) (board.trackedBoardHashCode() % maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;

        if (height >= this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] || hashTableVersion > this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
            if (hashTableVersion == this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
                // move this entry to the always replace table, but not if it is an entry from a previous search
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_MOVE];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_SCORE];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_FLAG];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT];
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION];
            }

            storeSuperVerifyUseHeightInformation(board, hashIndex);

            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = move;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = score;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = flag;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = (int) (board.trackedBoardHashCode() >>> 32);
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = (int) (board.trackedBoardHashCode() & Bitboards.LOW32);
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = height;
            this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = hashTableVersion;
        } else {
            storeSuperVerifyAlwaysReplaceInformation(board, hashIndex);

            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_MOVE] = move;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_SCORE] = score;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_FLAG] = flag;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT1] = (int) (board.trackedBoardHashCode() >>> 32);
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_64BIT2] = (int) (board.trackedBoardHashCode() & Bitboards.LOW32);
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_HEIGHT] = height;
            this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_VERSION] = hashTableVersion;
        }
    }

    private void storeSuperVerifyAlwaysReplaceInformation(EngineChessBoard board, final int hashIndex) {
        if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
            for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = (int) (board.getBitboardByIndex(i) >>> 32);
                this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = (int) (board.getBitboardByIndex(i) & Bitboards.LOW32);
            }
        }
    }

    private void storeSuperVerifyUseHeightInformation(EngineChessBoard board, final int hashIndex) {
        if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {

            for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
                if (hashTableVersion == this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
                    this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i];
                    this.hashTableIgnoreHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12];
                }
                this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i] = (int) (board.getBitboardByIndex(i) >>> 32);
                this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12] = (int) (board.getBitboardByIndex(i) & Bitboards.LOW32);
            }
        }
    }

    public int getPawnHashIndex(long pawnHashValue) {
        return (int) (pawnHashValue % this.maxPawnHashEntries) * RivalConstants.NUM_PAWNHASH_FIELDS;
    }

    public PawnHashEntry getPawnHashEntry(EngineChessBoard board) {
        PawnHashEntry pawnHashEntry;

        final long pawnHashValue = board.trackedPawnHashCode();
        final int pawnHashIndex = getPawnHashIndex(pawnHashValue);

        if (FeatureFlag.USE_PAWN_HASH.isActive()) {
            if (FeatureFlag.USE_QUICK_PAWN_HASH_RETURN.isActive() && lastPawnHashValue == pawnHashValue) {
                pawnHashEntry = new PawnHashEntry(lastPawnHashEntry);
            } else {
                pawnHashEntry = getPawnStatsFromHash(pawnHashValue, pawnHashIndex);
            }
        } else {
            pawnHashEntry = new PawnHashEntry();
        }

        if (!pawnHashEntry.isPopulated()) {
            final long whitePawnAttacks = Bitboards.getWhitePawnAttacks(board.getWhitePawnBitboard());
            final long blackPawnAttacks = Bitboards.getBlackPawnAttacks(board.getBlackPawnBitboard());
            final long whitePawnFiles = Bitboards.getPawnFiles(board.getWhitePawnBitboard());
            final long blackPawnFiles = Bitboards.getPawnFiles(board.getBlackPawnBitboard());

            pawnHashEntry.setPawnScore(0);

            pawnHashEntry.setWhitePassedPawnsBitboard(Bitboards.getWhitePassedPawns(board.getWhitePawnBitboard(), board.getBlackPawnBitboard()));

            final long whiteGuardedPassedPawns = pawnHashEntry.getWhitePassedPawnsBitboard() & (Bitboards.getWhitePawnAttacks(board.getWhitePawnBitboard()));

            pawnHashEntry.setBlackPassedPawnsBitboard(Bitboards.getBlackPassedPawns(board.getWhitePawnBitboard(), board.getBlackPawnBitboard()));

            long blackGuardedPassedPawns = pawnHashEntry.getBlackPassedPawnsBitboard() & (Bitboards.getBlackPawnAttacks(board.getBlackPawnBitboard()));

            pawnHashEntry.setWhitePassedPawnScore(Long.bitCount(whiteGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.getValue());
            pawnHashEntry.setBlackPassedPawnScore(Long.bitCount(blackGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.getValue());

            final long whiteIsolatedPawns = whitePawnFiles & ~(whitePawnFiles << 1) & ~(whitePawnFiles >>> 1);
            final long blackIsolatedPawns = blackPawnFiles & ~(blackPawnFiles << 1) & ~(blackPawnFiles >>> 1);
            pawnHashEntry.decPawnScore(Long.bitCount(whiteIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.getValue());
            pawnHashEntry.incPawnScore(Long.bitCount(blackIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.getValue());

            if ((whiteIsolatedPawns & Bitboards.FILE_D) != 0) {
                pawnHashEntry.decPawnScore(Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.getValue());
            }
            if ((blackIsolatedPawns & Bitboards.FILE_D) != 0) {
                pawnHashEntry.incPawnScore(Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.getValue());
            }

            pawnHashEntry.decPawnScore(
                    Long.bitCount(
                            board.getWhitePawnBitboard() &
                                    ~((board.getWhitePawnBitboard() | board.getBlackPawnBitboard()) >>> 8) &
                                    (blackPawnAttacks >>> 8) &
                                    ~Bitboards.northFill(whitePawnAttacks) &
                                    (Bitboards.getBlackPawnAttacks(board.getWhitePawnBitboard())) &
                                    ~Bitboards.northFill(blackPawnFiles)
                    ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.getValue());

            pawnHashEntry.incPawnScore(Long.bitCount(
                    board.getBlackPawnBitboard() &
                            ~((board.getBlackPawnBitboard() | board.getWhitePawnBitboard()) << 8) &
                            (whitePawnAttacks << 8) &
                            ~Bitboards.southFill(blackPawnAttacks) &
                            (Bitboards.getWhitePawnAttacks(board.getBlackPawnBitboard())) &
                            ~Bitboards.northFill(whitePawnFiles)
            ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.getValue());

            int sq;
            long bitboard = pawnHashEntry.getWhitePassedPawnsBitboard();
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                pawnHashEntry.addWhitePassedPawnScore(Evaluation.getPassedPawnBonus(sq / 8));
            }

            bitboard = pawnHashEntry.getBlackPassedPawnsBitboard();
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                pawnHashEntry.addBlackPassedPawnScore(Evaluation.getPassedPawnBonus(7 - (sq / 8)));
            }

            pawnHashEntry.decPawnScore(
                    (Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_H))
                            * Evaluation.VALUE_SIDE_PAWN_PENALTY.getValue());

            pawnHashEntry.incPawnScore(
                    (Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_H))
                            * Evaluation.VALUE_SIDE_PAWN_PENALTY.getValue());

            long occupiedFileMask = Bitboards.southFill(board.getWhitePawnBitboard()) & Bitboards.RANK_1;
            pawnHashEntry.decPawnScore(Evaluation.VALUE_DOUBLED_PAWN_PENALTY.getValue() * ((board.getWhitePawnValues() / 100) - Long.bitCount(occupiedFileMask)));
            pawnHashEntry.decPawnScore(Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.getValue());

            occupiedFileMask = Bitboards.southFill(board.getBlackPawnBitboard()) & Bitboards.RANK_1;
            pawnHashEntry.incPawnScore(Evaluation.VALUE_DOUBLED_PAWN_PENALTY.getValue() * ((board.getBlackPawnValues() / 100) - Long.bitCount(occupiedFileMask)));
            pawnHashEntry.incPawnScore(Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.getValue());

            storePawnHashEntry(board, pawnHashEntry, pawnHashIndex);
        }

        pawnHashEntry.incPawnScore(
                Numbers.linearScale(board.getBlackPieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), pawnHashEntry.getWhitePassedPawnScore() * 2, pawnHashEntry.getWhitePassedPawnScore())
                        - Numbers.linearScale(board.getWhitePieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), pawnHashEntry.getBlackPassedPawnScore() * 2, pawnHashEntry.getBlackPassedPawnScore()));

        if (board.getBlackPieceValues() < Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue()) {
            final int kingX = board.getBlackKingSquare() % 8;
            final int kingY = board.getBlackKingSquare() / 8;
            long bitboard = pawnHashEntry.getWhitePassedPawnsBitboard();
            int sq;
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                final int pawnDistance = Math.min(5, 7 - (sq / 8));
                final int kingDistance = Math.max(Math.abs(kingX - (sq % 8)), Math.abs(kingY - 7));
                pawnHashEntry.incPawnScore(Numbers.linearScale(board.getBlackPieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), kingDistance * 4, 0));
                if ((pawnDistance < (kingDistance - (board.getMover() == Colour.WHITE ? 0 : 1))) && (board.getBlackPieceValues() == 0))
                    pawnHashEntry.incPawnScore(RivalConstants.VALUE_KING_CANNOT_CATCH_PAWN);
            }
        }

        if (board.getWhitePieceValues() < Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue()) {
            final int kingX = board.getWhiteKingSquare() % 8;
            final int kingY = board.getWhiteKingSquare() / 8;
            long bitboard = pawnHashEntry.getBlackPassedPawnsBitboard();
            int sq;
            while (bitboard != 0) {
                bitboard ^= (1L << (sq = Long.numberOfTrailingZeros(bitboard)));
                final int pawnDistance = Math.min(5, (sq / 8));
                final int kingDistance = Math.max(Math.abs(kingX - (sq % 8)), kingY);
                pawnHashEntry.decPawnScore(Numbers.linearScale(board.getWhitePieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), kingDistance * 4, 0));
                if ((pawnDistance < (kingDistance - (board.getMover() == Colour.WHITE ? 1 : 0))) && (board.getWhitePieceValues() == 0))
                    pawnHashEntry.decPawnScore(Evaluation.VALUE_KING_CANNOT_CATCH_PAWN.getValue());
            }
        }

        setLastPawnHashValueAndEntry(board, pawnHashEntry);

        return pawnHashEntry;
    }

    private void storePawnHashEntry(EngineChessBoard board, PawnHashEntry pawnHashEntry, int pawnHashIndex) {
        if (FeatureFlag.USE_PAWN_HASH.isActive()) {
            setPawnHashTable(pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_MAIN_SCORE.getIndex(), pawnHashEntry.getPawnScore());
            setPawnHashTable(pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE.getIndex(), pawnHashEntry.getWhitePassedPawnScore());
            setPawnHashTable(pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE.getIndex(), pawnHashEntry.getBlackPassedPawnScore());
            setPawnHashTable(pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_WHITE_PASSEDPAWNS.getIndex(), pawnHashEntry.getWhitePassedPawnsBitboard());
            setPawnHashTable(pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_BLACK_PASSEDPAWNS.getIndex(), pawnHashEntry.getBlackPassedPawnsBitboard());
            setPawnHashTable(pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_LOCK.getIndex(), board.trackedPawnHashCode());
        }
    }

    private PawnHashEntry getPawnStatsFromHash(long pawnHashValue, int pawnHashIndex) {
        PawnHashEntry pawnHashEntry = new PawnHashEntry();

        if (this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_LOCK] == pawnHashValue) {
            pawnHashEntry.setPawnScore((int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_MAIN_SCORE]);
            if (pawnHashEntry.getPawnScore() != RivalConstants.PAWNHASH_DEFAULT_SCORE) {
                pawnHashEntry.setWhitePassedPawnScore((int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE]);
                pawnHashEntry.setBlackPassedPawnScore((int) this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE]);
                pawnHashEntry.setWhitePassedPawnsBitboard(this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_WHITE_PASSEDPAWNS]);
                pawnHashEntry.setBlackPassedPawnsBitboard(this.pawnHashTable[pawnHashIndex + RivalConstants.PAWNHASHENTRY_BLACK_PASSEDPAWNS]);
            }
        }

        return pawnHashEntry;
    }

    public void setLastPawnHashValueAndEntry(EngineChessBoard board, PawnHashEntry pawnHashEntry) {
        if (RivalConstants.USE_QUICK_PAWN_HASH_RETURN) {
            setLastPawnHashValue(board.trackedPawnHashCode());
            setLastPawnHashEntry(new PawnHashEntry(pawnHashEntry));
        }
    }

    public synchronized void setHashSizeMB(int hashSizeMB) {
        if (hashSizeMB < 1) {
            setMaxHashEntries(1);
            setMaxPawnHashEntries(1);
        } else {
            int mainHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 6; // two of these
            int pawnHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 2; // one of these
            setMaxHashEntries(mainHashTableSize / RivalConstants.HASHPOSITION_SIZE_BYTES);
            setMaxPawnHashEntries(pawnHashTableSize / RivalConstants.PAWNHASHENTRY_SIZE_BYTES);
        }

        setHashTable();
    }

    public long pawnHashCode(EngineChessBoard engineChessBoard) {
        return ZorbristHashCalculator.calculatePawnHash(engineChessBoard);
    }

    public synchronized void initialiseHashCode(EngineChessBoard engineChessBoard) {
        hashCalculator.initHash(engineChessBoard);
    }

    public int getHashIndex(EngineChessBoard engineChessBoard) {
        return getHashIndex(engineChessBoard.trackedBoardHashCode());
    }

    public int getHashIndex(long hashValue) {
        return (int) (hashValue % maxHashEntries) * RivalConstants.NUM_HASH_FIELDS;
    }

    public void move(EngineChessBoard engineChessBoard, EngineMove move) {
        hashCalculator.makeMove(engineChessBoard, move);
    }

    public void unMove(EngineChessBoard engineChessBoard) {
        hashCalculator.unMakeMove(engineChessBoard.getLastMoveMade());
    }

    public void makeNullMove() {
        hashCalculator.nullMove();
    }

    public long getTrackedHashValue() {
        return hashCalculator.getTrackedBoardHashValue();
    }

    public long getTrackedPawnHashValue() {
        return hashCalculator.getTrackedPawnHashValue();
    }

    public int getHashTableVersion() {
        return hashTableVersion;
    }

    public void setHashTableVersion(int hashTableVersion) {
        this.hashTableVersion = hashTableVersion;
    }

    public void setMaxHashEntries(int maxHashEntries) {
        this.maxHashEntries = maxHashEntries;
    }

    public void setMaxPawnHashEntries(int maxPawnHashEntries) {
        this.maxPawnHashEntries = maxPawnHashEntries;
    }

    public void setLastPawnHashValue(long lastPawnHashValue) {
        this.lastPawnHashValue = lastPawnHashValue;
    }

    public void setLastPawnHashEntry(PawnHashEntry lastPawnHashEntry) {
        this.lastPawnHashEntry = lastPawnHashEntry;
    }

    public void incVersion() {
        this.hashTableVersion++;
    }
}
