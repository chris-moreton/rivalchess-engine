package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.bitboards.LOW32
import com.netsensia.rivalchess.config.FeatureFlag
import com.netsensia.rivalchess.config.SearchConfig
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.enums.HashIndex
import com.netsensia.rivalchess.enums.HashValueType
import com.netsensia.rivalchess.exception.HashVerificationException
import com.netsensia.rivalchess.model.SquareOccupant

fun isHeightHashTableEntryValid(depthRemaining: Int, boardHash: BoardHash, hashIndex: Int) =
        if (boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_HEIGHT.index) >= depthRemaining && boardHash.useHeight(hashIndex + HashIndex.FLAG.index) !=
                HashValueType.EMPTY.index && boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_64BIT1.index) == (boardHash.trackedHashValue ushr 32).toInt() &&
                boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_64BIT2.index) == (boardHash.trackedHashValue and LOW32).toInt()) {
            (boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HashIndex.VERSION.index) <= SearchConfig.MAXIMUM_HASH_AGE.value)
        } else false


fun isAlwaysReplaceHashTableEntryValid(depthRemaining: Int, boardHash: BoardHash, hashIndex: Int) =
        if (boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_HEIGHT.index) >= depthRemaining && boardHash.ignoreHeight(hashIndex + HashIndex.FLAG.index) !=
                HashValueType.EMPTY.index && boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_64BIT1.index) == (boardHash.trackedHashValue ushr 32).toInt() &&
                boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_64BIT2.index) == (boardHash.trackedHashValue and LOW32).toInt()) {
            boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HashIndex.VERSION.index) <= SearchConfig.MAXIMUM_HASH_AGE.value
        } else false


private fun superVerifyUseHeightHash(board: EngineBoard) {
    val boardHash = board.boardHashObject
    val hashIndex = boardHash.getHashIndex(board)
    if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive) {
        for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
            if (boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i) != (board.getBitboard(i) ushr 32).toInt() ||
                    boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12) != (board.getBitboard(i) and LOW32).toInt()) {
                throw HashVerificationException("Height bad clash " + ZorbristHashCalculator.calculateHash(board))
            }
        }
    }
}

private fun superVerifyAlwaysReplaceHash(board: EngineBoard) {
    val boardHash = board.boardHashObject
    val hashIndex = boardHash.getHashIndex(board)
    if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive) {
        for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
            if (boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i) != (board.getBitboard(i) ushr 32).toInt() ||
                    boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12) != (board.getBitboard(i) and LOW32).toInt()) {
                System.exit(0)
            }
        }
    }
}
