package com.netsensia.rivalchess.engine.core.hash;

import com.netsensia.rivalchess.bitboards.Bitboards;
import com.netsensia.rivalchess.engine.core.EngineChessBoard;
import com.netsensia.rivalchess.engine.core.RivalConstants;
import com.netsensia.rivalchess.exception.HashVerificationException;

public class SearchHashHelper {

    private SearchHashHelper() {}

    public static boolean isHeightHashTableEntryValid(int depthRemaining, EngineChessBoard board) {
        final BoardHash boardHash = board.getBoardHashObject();
        final long hashValue = boardHash.getTrackedHashValue();
        final int hashIndex = boardHash.getHashIndex(board);

        if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_HEIGHT) >= depthRemaining &&
                boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) != RivalConstants.EMPTY &&
                boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_64BIT1) == (int) (hashValue >>> 32) &&
                boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_64BIT2) == (int) (hashValue & Bitboards.LOW32)) {

            boolean isLocked =
                    boardHash.getHashTableVersion()
                            - boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_VERSION)
                            <= RivalConstants.MAXIMUM_HASH_AGE;

            if (isLocked) {
                superVerifyUseHeightHash(board);
            }

            return isLocked;
        }

        return false;
    }

    public static boolean isAlwaysReplaceHashTableEntryValid(int depthRemaining, EngineChessBoard board) {

        final BoardHash boardHash = board.getBoardHashObject();
        final long hashValue = boardHash.getTrackedHashValue();
        final int hashIndex = boardHash.getHashIndex(board);

        if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_HEIGHT) >= depthRemaining &&
                boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_FLAG) != RivalConstants.EMPTY &&
                boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_64BIT1) == (int) (hashValue >>> 32) &&
                boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_64BIT2) == (int) (hashValue & Bitboards.LOW32)) {

            boolean isLocked = boardHash.getHashTableVersion() - boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_VERSION) <= RivalConstants.MAXIMUM_HASH_AGE;

            if (isLocked) {
                superVerifyAlwaysReplaceHash(board);
            }

            return isLocked;
        }

        return false;
    }

    private static void superVerifyUseHeightHash(EngineChessBoard board) {
        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);

        if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
            for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
                if (boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_LOCK1 + i) != (int) (board.getBitboardByIndex(i) >>> 32) ||
                        boardHash.getHashTableUseHeight(hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12) != (int) (board.getBitboardByIndex(i) & Bitboards.LOW32)) {
                    throw new HashVerificationException("Height bad clash " + ZorbristHashCalculator.calculateHash(board));
                }
            }

        }
    }

    private static void superVerifyAlwaysReplaceHash(EngineChessBoard board) {
        final BoardHash boardHash = board.getBoardHashObject();
        final int hashIndex = boardHash.getHashIndex(board);

        if (RivalConstants.USE_SUPER_VERIFY_ON_HASH) {
            for (int i = RivalConstants.WP; i <= RivalConstants.BR; i++) {
                if (boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_LOCK1 + i) != (int) (board.getBitboardByIndex(i) >>> 32) ||
                        boardHash.getHashTableIgnoreHeight(hashIndex + RivalConstants.HASHENTRY_LOCK1 + i + 12) != (int) (board.getBitboardByIndex(i) & Bitboards.LOW32)) {
                    System.exit(0);
                }
            }
        }
    }
}
