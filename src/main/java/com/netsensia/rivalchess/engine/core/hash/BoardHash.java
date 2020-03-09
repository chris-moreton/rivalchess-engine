package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.config.Evaluation;
import com.netsensia.rivalchess.config.FeatureFlag;
import com.netsensia.rivalchess.config.Hash;
import com.netsensia.rivalchess.enums.Colour;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.engine.core.eval.PawnHashEntry;
import com.netsensia.rivalchess.engine.core.type.EngineMove;
import com.netsensia.rivalchess.enums.HashIndex;
import com.netsensia.rivalchess.enums.HashValueType;
import com.netsensia.rivalchess.enums.PawnHashIndex;
import com.netsensia.rivalchess.enums.SquareOccupant;
import com.netsensia.rivalchess.util.Numbers;

import static com.netsensia.rivalchess.config.Hash.DEFAULT_SEARCH_HASH_HEIGHT;

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
                this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_HEIGHT.getIndex()] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableUseHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_VERSION.getIndex()] = 1;
                this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_FLAG.getIndex()] = HashValueType.EMPTY.getIndex();
                this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_HEIGHT.getIndex()] = RivalConstants.DEFAULT_SEARCH_HASH_HEIGHT;
                this.hashTableIgnoreHeight[i * HashIndex.getNumHashFields() + HashIndex.HASHENTRY_VERSION.getIndex()] = 1;
                if (FeatureFlag.USE_PAWN_HASH.isActive()) {
                    this.pawnHashTable[i * PawnHashIndex.getNumHashFields() + RivalConstants.PAWNHASHENTRY_MAIN_SCORE] = RivalConstants.PAWNHASH_DEFAULT_SCORE;
                }
            }
        }
    }

    public void storeHashMove(int move, EngineChessBoard board, int score, byte flag, int height) {
        final int hashIndex = (int) (board.trackedBoardHashCode() % maxHashEntries) * HashIndex.getNumHashFields();

        if (height >= this.hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()] || hashTableVersion > this.hashTableUseHeight[hashIndex + RivalConstants.HASHENTRY_VERSION]) {
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

    public int getPawnHashIndex(long pawnHashValue) {
        return (int) (pawnHashValue % this.maxPawnHashEntries) * PawnHashIndex.getNumHashFields();
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
            populatePawnHashEntry(board, pawnHashEntry, pawnHashIndex);
        }

        calculatePawnScore(board, pawnHashEntry);

        setLastPawnHashValueAndEntry(board, pawnHashEntry);

        return pawnHashEntry;
    }

    private void calculatePawnScore(EngineChessBoard board, PawnHashEntry pawnHashEntry) {
        pawnHashEntry.incPawnScore(
                Numbers.linearScale(board.getBlackPieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), pawnHashEntry.getWhitePassedPawnScore() * 2, pawnHashEntry.getWhitePassedPawnScore())
                        - Numbers.linearScale(board.getWhitePieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), pawnHashEntry.getBlackPassedPawnScore() * 2, pawnHashEntry.getBlackPassedPawnScore()));

        if (board.getBlackPieceValues() < Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue()) {
            calculateLowWhiteMaterialPawnBonus(Colour.BLACK, board, pawnHashEntry);
        }

        if (board.getWhitePieceValues() < Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue()) {
            calculateLowWhiteMaterialPawnBonus(Colour.WHITE, board, pawnHashEntry);
        }
    }

    private void calculateLowWhiteMaterialPawnBonus(Colour lowMaterialColour, EngineChessBoard board, PawnHashEntry pawnHashEntry) {
        final int kingSquare = lowMaterialColour == Colour.WHITE ? board.getWhiteKingSquare() : board.getBlackKingSquare();
        final int kingX = kingSquare % 8;
        final int kingY = kingSquare / 8;
        final int lowMaterialSidePieceValues = lowMaterialColour == Colour.WHITE ? board.getWhitePieceValues() : board.getBlackPieceValues();

        long passedPawnBitboard = lowMaterialColour == Colour.WHITE
                ? pawnHashEntry.getBlackPassedPawnsBitboard()
                : pawnHashEntry.getWhitePassedPawnsBitboard();

        int scoreAdjustment;

        while (passedPawnBitboard != 0) {
            final int sq = Long.numberOfTrailingZeros(passedPawnBitboard);

            passedPawnBitboard ^= (1L << sq);

            final int pawnDistanceFromPromotion = lowMaterialColour == Colour.WHITE ? (sq / 8) : 7 - (sq / 8);
            final int pawnDistance = Math.min(5, pawnDistanceFromPromotion);

            final int kingXDistanceFromPawn = Math.abs(kingX - (sq % 8));
            final int kingYDistanceFromPawn = lowMaterialColour == Colour.WHITE ? kingY : Math.abs(kingY - 7);
            final int kingDistanceFromPawn = Math.max(kingXDistanceFromPawn, kingYDistanceFromPawn);

            scoreAdjustment = Numbers.linearScale(
                    lowMaterialSidePieceValues, 0,
                    Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(),
                    kingDistanceFromPawn * 4, 0);

            final int moverAdjustment = lowMaterialColour == board.getMover() ? 1 : 0;
            if ((pawnDistance < (kingDistanceFromPawn - moverAdjustment)) && (lowMaterialSidePieceValues == 0)) {
                scoreAdjustment += Evaluation.VALUE_KING_CANNOT_CATCH_PAWN.getValue();
            }

            pawnHashEntry.decPawnScore(lowMaterialColour == Colour.WHITE ? scoreAdjustment : -scoreAdjustment);

        }
    }

    private void populatePawnHashEntry(EngineChessBoard board, PawnHashEntry pawnHashEntry, int pawnHashIndex) {
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

        updateWhitePassedPawnScore(pawnHashEntry);
        updateBlackPassedPawnScore(pawnHashEntry);

        updatePawnScoreForSidePawnPenalty(board, pawnHashEntry);

        long occupiedFileMask = Bitboards.southFill(board.getWhitePawnBitboard()) & Bitboards.RANK_1;
        pawnHashEntry.decPawnScore(Evaluation.VALUE_DOUBLED_PAWN_PENALTY.getValue() * ((board.getWhitePawnValues() / 100) - Long.bitCount(occupiedFileMask)));
        pawnHashEntry.decPawnScore(Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.getValue());

        occupiedFileMask = Bitboards.southFill(board.getBlackPawnBitboard()) & Bitboards.RANK_1;
        pawnHashEntry.incPawnScore(Evaluation.VALUE_DOUBLED_PAWN_PENALTY.getValue() * ((board.getBlackPawnValues() / 100) - Long.bitCount(occupiedFileMask)));
        pawnHashEntry.incPawnScore(Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.getValue());

        storePawnHashEntry(board, pawnHashEntry, pawnHashIndex);
    }

    private void updatePawnScoreForSidePawnPenalty(EngineChessBoard board, PawnHashEntry pawnHashEntry) {
        pawnHashEntry.decPawnScore(
                (Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_H))
                        * Evaluation.VALUE_SIDE_PAWN_PENALTY.getValue());

        pawnHashEntry.incPawnScore(
                (Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_H))
                        * Evaluation.VALUE_SIDE_PAWN_PENALTY.getValue());
    }

    private void updateBlackPassedPawnScore(PawnHashEntry pawnHashEntry) {
        long bitboard = pawnHashEntry.getBlackPassedPawnsBitboard();
        while (bitboard != 0) {
            final int sq = Long.numberOfTrailingZeros(bitboard);
            bitboard ^= (1L << sq);
            pawnHashEntry.incBlackPassedPawnScore(Evaluation.getPassedPawnBonus(7 - (sq / 8)));
        }
    }

    private void updateWhitePassedPawnScore(PawnHashEntry pawnHashEntry) {
        long bitboard = pawnHashEntry.getWhitePassedPawnsBitboard();
        while (bitboard != 0) {
            final int sq = Long.numberOfTrailingZeros(bitboard);
            bitboard ^= (1L << sq);
            pawnHashEntry.incWhitePassedPawnScore(Evaluation.getPassedPawnBonus(sq / 8));
        }
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

        if (this.pawnHashTable[pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_LOCK.getIndex()] == pawnHashValue) {
            pawnHashEntry.setPawnScore((int) this.pawnHashTable[pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_MAIN_SCORE.getIndex()]);
            if (pawnHashEntry.getPawnScore() != -Integer.MAX_VALUE) {
                pawnHashEntry.setWhitePassedPawnScore((int) this.pawnHashTable[pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_WHITE_PASSEDPAWN_SCORE.getIndex()]);
                pawnHashEntry.setBlackPassedPawnScore((int) this.pawnHashTable[pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_BLACK_PASSEDPAWN_SCORE.getIndex()]);
                pawnHashEntry.setWhitePassedPawnsBitboard(this.pawnHashTable[pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_WHITE_PASSEDPAWNS.getIndex()]);
                pawnHashEntry.setBlackPassedPawnsBitboard(this.pawnHashTable[pawnHashIndex + PawnHashIndex.PAWNHASHENTRY_BLACK_PASSEDPAWNS.getIndex()]);
            }
        }

        return pawnHashEntry;
    }

    public void setLastPawnHashValueAndEntry(EngineChessBoard board, PawnHashEntry pawnHashEntry) {
        if (FeatureFlag.USE_QUICK_PAWN_HASH_RETURN.isActive()) {
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
        return (int) (hashValue % maxHashEntries) * HashIndex.getNumHashFields();
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
