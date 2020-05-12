package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.config.FeatureFlag;
import com.netsensia.rivalchess.config.SearchConfig;
import com.netsensia.rivalchess.engine.core.board.EngineBoard;
import com.netsensia.rivalchess.enums.HashIndex;
import com.netsensia.rivalchess.enums.HashValueType;
import com.netsensia.rivalchess.exception.HashVerificationException;
import com.netsensia.rivalchess.model.SquareOccupant;

public class SearchHashHelper {

    private SearchHashHelper() {}

    public static boolean isHeightHashTableEntryValid(int depthRemaining, EngineBoard board) {
        final BoardHash boardHash = board.getBoardHashObject();
        final long hashValue = boardHash.getTrackedHashValue();
        final int hashIndex = boardHash.getHashIndex(board);

        if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()) >= depthRemaining &&
                boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) != HashValueType.EMPTY.getIndex() &&
                boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_64BIT1.getIndex()) == (int) (hashValue >>> 32) &&
                boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_64BIT2.getIndex()) == (int) (hashValue & Bitboards.LOW32)) {

            boolean isLocked =
                    boardHash.getHashTableVersion()
                            - boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_VERSION.getIndex())
                            <= SearchConfig.MAXIMUM_HASH_AGE.getValue();

            if (isLocked) {
                superVerifyUseHeightHash(board);
            }

            return isLocked;
        }

        return false;
    }

    public static boolean isAlwaysReplaceHashTableEntryValid(int depthRemaining, EngineBoard board) {

        final BoardHash boardHash = board.getBoardHashObject();
        final long hashValue = boardHash.getTrackedHashValue();
        final int hashIndex = boardHash.getHashIndex(board);

        if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_HEIGHT.getIndex()) >= depthRemaining &&
                boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_FLAG.getIndex()) != HashValueType.EMPTY.getIndex() &&
                boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_64BIT1.getIndex()) == (int) (hashValue >>> 32) &&
                boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_64BIT2.getIndex()) == (int) (hashValue & Bitboards.LOW32)) {

            boolean isLocked = boardHash.getHashTableVersion() - boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_VERSION.getIndex()) <= SearchConfig.MAXIMUM_HASH_AGE.getValue();

            if (isLocked) {
                superVerifyAlwaysReplaceHash(board);
            }

            return isLocked;
        }

        return false;
    }

    private static void superVerifyUseHeightHash(EngineBoard board) {
        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);

        if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive()) {
            for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
                if (boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i) != (int) (board.getBitboardByIndex(i) >>> 32) ||
                        boardHash.getHashTableUseHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i + 12) != (int) (board.getBitboardByIndex(i) & Bitboards.LOW32)) {
                    throw new HashVerificationException("Height bad clash " + ZorbristHashCalculator.calculateHash(board));
                }
            }

        }
    }

    private static void superVerifyAlwaysReplaceHash(EngineBoard board) {
        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);

        if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive()) {
            for (int i = SquareOccupant.WP.getIndex(); i <= SquareOccupant.BR.getIndex(); i++) {
                if (boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i) != (int) (board.getBitboardByIndex(i) >>> 32) ||
                        boardHash.getHashTableIgnoreHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.getIndex() + i + 12) != (int) (board.getBitboardByIndex(i) & Bitboards.LOW32)) {
                    System.exit(0);
                }
            }
        }
    }
}
