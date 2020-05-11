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

import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getBlackPassedPawns;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.blackPawnAttacks;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getPawnFiles;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.getWhitePassedPawns;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.whitePawnAttacks;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.northFill;
import static com.netsensia.rivalchess.bitboards.util.BitboardUtilsKt.southFill;
import static com.netsensia.rivalchess.engine.core.eval.EvaluateKt.linearScale;

public class BoardHash {

    private long lastPawnHashValue = -1;
    private final ZorbristHashTracker hashTracker = new ZorbristHashTracker();

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

    public int getPawnHashIndex(long pawnHashValue) {
        return (int) (pawnHashValue % this.maxPawnHashEntries) * PawnHashIndex.getNumHashFields();
    }

    private int calculateLowWhiteMaterialPawnBonus(Colour lowMaterialColour, EngineChessBoard board, int score, long whitePassedPawnsBitboard, long blackPassedPawnsBitboard) {
        final int kingSquare = lowMaterialColour == Colour.WHITE ? board.getWhiteKingSquare() : board.getBlackKingSquare();
        final int kingX = kingSquare % 8;
        final int kingY = kingSquare / 8;
        final int lowMaterialSidePieceValues = lowMaterialColour == Colour.WHITE ? board.getWhitePieceValues() : board.getBlackPieceValues();

        long passedPawnBitboard = lowMaterialColour == Colour.WHITE
                ? blackPassedPawnsBitboard
                : whitePassedPawnsBitboard;

        int scoreAdjustment;

        while (passedPawnBitboard != 0) {
            final int sq = Long.numberOfTrailingZeros(passedPawnBitboard);

            passedPawnBitboard ^= (1L << sq);

            final int pawnDistanceFromPromotion = lowMaterialColour == Colour.WHITE ? (sq / 8) : 7 - (sq / 8);
            final int pawnDistance = Math.min(5, pawnDistanceFromPromotion);

            final int kingXDistanceFromPawn = Math.abs(kingX - (sq % 8));
            final int kingYDistanceFromPawn = lowMaterialColour == Colour.WHITE ? kingY : Math.abs(kingY - 7);
            final int kingDistanceFromPawn = Math.max(kingXDistanceFromPawn, kingYDistanceFromPawn);

            scoreAdjustment = linearScale(
                    lowMaterialSidePieceValues, 0,
                    Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(),
                    kingDistanceFromPawn * 4, 0);

            final int moverAdjustment = lowMaterialColour == board.getMover() ? 1 : 0;
            if ((pawnDistance < (kingDistanceFromPawn - moverAdjustment)) && (lowMaterialSidePieceValues == 0)) {
                scoreAdjustment += Evaluation.VALUE_KING_CANNOT_CATCH_PAWN.getValue();
            }

            score -= (lowMaterialColour == Colour.WHITE ? scoreAdjustment : -scoreAdjustment);

        }

        return score;
    }

    public int pawnScore(EngineChessBoard board) {
        int score = 0;

        final long whitePawnAttacks = whitePawnAttacks(board.getWhitePawnBitboard());
        final long blackPawnAttacks = blackPawnAttacks(board.getBlackPawnBitboard());
        final long whitePawnFiles = getPawnFiles(board.getWhitePawnBitboard());
        final long blackPawnFiles = getPawnFiles(board.getBlackPawnBitboard());

        final long whitePassedPawnsBitboard = getWhitePassedPawns(board.getWhitePawnBitboard(), board.getBlackPawnBitboard());

        final long whiteGuardedPassedPawns = whitePassedPawnsBitboard & (whitePawnAttacks(board.getWhitePawnBitboard()));

        final long blackPassedPawnsBitboad = getBlackPassedPawns(board.getWhitePawnBitboard(), board.getBlackPawnBitboard());

        long blackGuardedPassedPawns = blackPassedPawnsBitboad & (blackPawnAttacks(board.getBlackPawnBitboard()));

        int whitePassedPawnScore = Long.bitCount(whiteGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.getValue();
        int blackPassedPawnScore = Long.bitCount(blackGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.getValue();

        final long whiteIsolatedPawns = whitePawnFiles & ~(whitePawnFiles << 1) & ~(whitePawnFiles >>> 1);
        final long blackIsolatedPawns = blackPawnFiles & ~(blackPawnFiles << 1) & ~(blackPawnFiles >>> 1);

        score -= (Long.bitCount(whiteIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.getValue());
        score += (Long.bitCount(blackIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.getValue());

        if ((whiteIsolatedPawns & Bitboards.FILE_D) != 0) {
            score -= (Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.getValue());
        }
        if ((blackIsolatedPawns & Bitboards.FILE_D) != 0) {
            score += (Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.getValue());
        }

        score -=
                (Long.bitCount(
                        board.getWhitePawnBitboard() &
                                ~((board.getWhitePawnBitboard() | board.getBlackPawnBitboard()) >>> 8) &
                                (blackPawnAttacks >>> 8) &
                                ~northFill(whitePawnAttacks, 8) &
                                (blackPawnAttacks(board.getWhitePawnBitboard())) &
                                ~northFill(blackPawnFiles, 8)
                ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.getValue());

        score += (Long.bitCount(
                board.getBlackPawnBitboard() &
                        ~((board.getBlackPawnBitboard() | board.getWhitePawnBitboard()) << 8) &
                        (whitePawnAttacks << 8) &
                        ~southFill(blackPawnAttacks, 8) &
                        (whitePawnAttacks(board.getBlackPawnBitboard())) &
                        ~northFill(whitePawnFiles, 8)
        ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.getValue());

        long bitboard = whitePassedPawnsBitboard;
        while (bitboard != 0) {
            final int sq = Long.numberOfTrailingZeros(bitboard);
            bitboard ^= (1L << sq);
            whitePassedPawnScore += (Evaluation.getPassedPawnBonus(sq / 8));
        }

        bitboard = blackPassedPawnsBitboad;
        while (bitboard != 0) {
            final int sq = Long.numberOfTrailingZeros(bitboard);
            bitboard ^= (1L << sq);
            blackPassedPawnScore += (Evaluation.getPassedPawnBonus(7 - (sq / 8)));
        }

        score -= ((Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getWhitePawnBitboard() & Bitboards.FILE_H))
                        * Evaluation.VALUE_SIDE_PAWN_PENALTY.getValue());

        score += ((Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_A) + Long.bitCount(board.getBlackPawnBitboard() & Bitboards.FILE_H))
                        * Evaluation.VALUE_SIDE_PAWN_PENALTY.getValue());

        long occupiedFileMask = southFill(board.getWhitePawnBitboard(), 8) & Bitboards.RANK_1;
        score -= (Evaluation.VALUE_DOUBLED_PAWN_PENALTY.getValue() * ((board.getWhitePawnValues() / 100) - Long.bitCount(occupiedFileMask)));
        score -= (Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.getValue());

        occupiedFileMask = southFill(board.getBlackPawnBitboard(), 8) & Bitboards.RANK_1;
        score += (Evaluation.VALUE_DOUBLED_PAWN_PENALTY.getValue() * ((board.getBlackPawnValues() / 100) - Long.bitCount(occupiedFileMask)));
        score += (Long.bitCount((((~occupiedFileMask) >>> 1) & occupiedFileMask)) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.getValue());

        score += (
                linearScale(board.getBlackPieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), whitePassedPawnScore * 2, whitePassedPawnScore)
                        - linearScale(board.getWhitePieceValues(), 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue(), blackPassedPawnScore * 2, blackPassedPawnScore));

        if (board.getBlackPieceValues() < Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue()) {
            score = calculateLowWhiteMaterialPawnBonus(Colour.BLACK, board, score, whitePassedPawnsBitboard, blackPassedPawnsBitboad);
        }

        if (board.getWhitePieceValues() < Evaluation.PAWN_ADJUST_MAX_MATERIAL.getValue()) {
            score = calculateLowWhiteMaterialPawnBonus(Colour.WHITE, board, score, whitePassedPawnsBitboard, blackPassedPawnsBitboad);
        }

        return score;
    }

    public synchronized void setHashSizeMB(int hashSizeMB) {
        if (hashSizeMB < 1) {
            setMaxHashEntries(1);
            setMaxPawnHashEntries(1);
        } else {
            int mainHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 6; // two of these
            int pawnHashTableSize = ((hashSizeMB * 1024 * 1024) / 14) * 2; // one of these
            setMaxHashEntries(mainHashTableSize / HashIndex.getHashPositionSizeBytes());
            setMaxPawnHashEntries(pawnHashTableSize / PawnHashIndex.getHashPositionSizeBytes());
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

    public void setMaxPawnHashEntries(int maxPawnHashEntries) {
        this.maxPawnHashEntries = maxPawnHashEntries;
    }

    public void incVersion() {
        this.hashTableVersion++;
    }
}
