package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.config.Evaluation;
import com.netsensia.rivalchess.config.FeatureFlag;
import com.netsensia.rivalchess.config.Hash;
import com.netsensia.rivalchess.model.Colour;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.enums.HashIndex;
import com.netsensia.rivalchess.enums.HashValueType;
import com.netsensia.rivalchess.enums.PawnHashIndex;
import com.netsensia.rivalchess.model.SquareOccupant;

public class BoardHash {

    private final ZorbristHashTracker hashTracker = new ZorbristHashTracker();

    private int hashTableVersion;
    private int[] hashTableUseHeight;
    private int[] hashTableIgnoreHeight;
    private long[] pawnHashTable;
    private int maxHashEntries;
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
            this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_FLAG.getIndex()] = HashValueType.EMPTY.getIndex();
            this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_HEIGHT.getIndex()] = Hash.DEFAULT_SEARCH_HASH_HEIGHT.getValue();
            this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_FLAG.getIndex()] = HashValueType.EMPTY.getIndex();
            this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_HEIGHT.getIndex()] = Hash.DEFAULT_SEARCH_HASH_HEIGHT.getValue();
            if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                this.pawnHashTable[i * PawnHashIndex.getNumHashFields() + PawnHashIndex.PAWNHASHENTRY_MAIN_SCORE.getIndex()] = -Integer.MAX_VALUE;
            }
        }
    }

    public synchronized void setHashTable() {
        if (maxHashEntries != lastHashSizeCreated) {
            this.hashTableUseHeight = new int[maxHashEntries * HashIndex.getNumHashFields()];
            this.hashTableIgnoreHeight = new int[maxHashEntries * HashIndex.getNumHashFields()];
            if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                this.pawnHashTable = new long[maxHashEntries * PawnHashIndex.getNumHashFields()];
            }
            lastHashSizeCreated = maxHashEntries;
            for (int i = 0; i < maxHashEntries; i++) {
                this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_FLAG.getIndex()] = HashValueType.EMPTY.getIndex();
                this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_HEIGHT.getIndex()] = Hash.DEFAULT_SEARCH_HASH_HEIGHT.getValue();
                this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_VERSION.getIndex()] = 1;
                this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_FLAG.getIndex()] = HashValueType.EMPTY.getIndex();
                this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_HEIGHT.getIndex()] = Hash.DEFAULT_SEARCH_HASH_HEIGHT.getValue();
                this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_VERSION.getIndex()] = 1;
                if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                    this.pawnHashTable[i * PawnHashIndex.getNumHashFields() + PawnHashIndex.PAWNHASHENTRY_MAIN_SCORE.getIndex()] = -Integer.MAX_VALUE;
                }
            }
        }
    }

    public void storeHashMove(int move, EngineChessBoard board, int score, byte flag, int height) {
        final int hashIndex = (int) (board.trackedBoardHashCode() % maxHashEntries) * HashIndex.getNumHashFields();

        if (height >= this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()] || hashTableVersion > this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()]) {
            if (hashTableVersion == this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()]) {
                copyEntryFromUseHeightToIgnoreHeightTable(hashIndex);
            }

            storeSuperVerifyUseHeightInformation(board, hashIndex);

            storeMoveInHashTable(move, board, score, flag, height, hashIndex, this.hashTableUseHeight);
        } else {
            storeSuperVerifyAlwaysReplaceInformation(board, hashIndex);

            storeMoveInHashTable(move, board, score, flag, height, hashIndex, this.hashTableIgnoreHeight);
        }
    }

    private void copyEntryFromUseHeightToIgnoreHeightTable(int hashIndex) {
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_MOVE.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_MOVE.getIndex()];
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()];
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()];
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_64BIT1.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT1.getIndex()];
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_64BIT2.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT2.getIndex()];
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()];
        this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()];
    }

    private void storeMoveInHashTable(int move, EngineChessBoard board, int score, byte flag, int height, int hashIndex, int[] hashTableUseHeight) {
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_MOVE.getIndex()] = move;
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_SCORE.getIndex()] = score;
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()] = flag;
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT1.getIndex()] = (int) (board.trackedBoardHashCode() >>> 32);
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT2.getIndex()] = (int) (board.trackedBoardHashCode() & Bitboards.LOW32);
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()] = height;
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()] = hashTableVersion;
    }

    private void storeSuperVerifyAlwaysReplaceInformation(EngineChessBoard board, final int hashIndex) {
        if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive()) {
            for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
                this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i] = (int) (board.getBitboardByIndex(i) >>> 32);
                this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i + 12] = (int) (board.getBitboardByIndex(i) & Bitboards.LOW32);
            }
        }
    }

    private void storeSuperVerifyUseHeightInformation(EngineChessBoard board, final int hashIndex) {
        if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive()) {
            for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
                if (hashTableVersion == this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()]) {
                    this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i];
                    this.hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i + 12] = this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i + 12];
                }
                this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i] = (int) (board.getBitboardByIndex(i) >>> 32);
                this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i + 12] = (int) (board.getBitboardByIndex(i) & Bitboards.LOW32);
            }
        }
    }

    public synchronized void setHashSizeMB(int hashSizeMB) {
        if (hashSizeMB < 1) {
            setMaxHashEntries(1);
        } else {
            int mainHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 6; // two of these
            setMaxHashEntries(mainHashTableSize / HashIndex.getHashPositionSizeBytes());
        }

        setHashTable();
    }

    public synchronized void initialiseHashCode(EngineChessBoard engineChessBoard) {
        hashTracker.initHash(engineChessBoard);
    }

    public int getHashIndex(EngineChessBoard engineChessBoard) {
        return getHashIndex(engineChessBoard.trackedBoardHashCode());
    }

    public int getHashIndex(long hashValue) {
        return (int) (hashValue % maxHashEntries) * HashIndex.getNumHashFields();
    }

    public void move(EngineChessBoard engineChessBoard, EngineMove move) {
        hashTracker.makeMove(engineChessBoard, move);
    }

    public void unMove(EngineChessBoard engineChessBoard) {
        hashTracker.unMakeMove(engineChessBoard.getLastMoveMade());
    }

    public void makeNullMove() {
        hashTracker.nullMove();
    }

    public long getTrackedHashValue() {
        return hashTracker.getTrackedBoardHashValue();
    }

    public long getTrackedPawnHashValue() {
        return hashTracker.getTrackedPawnHashValue();
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

    public void incVersion() {
        this.hashTableVersion++;
    }
}
