package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.bitboards.LOW32
import com.netsensia.rivalchess.config.FeatureFlag
import com.netsensia.rivalchess.config.SearchConfig
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.enums.HashIndex
import com.netsensia.rivalchess.enums.HashValueType
import com.netsensia.rivalchess.exception.HashVerificationException
import com.netsensia.rivalchess.model.SquareOccupant

fun isHeightHashTableEntryValid(depthRemaining: Int, board: EngineBoard): Boolean {
    val boardHash = board.boardHashObject
    val hashValue = boardHash.trackedHashValue
    val hashIndex = boardHash.getHashIndex(board)
    if (boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_HEIGHT.index) >= depthRemaining && boardHash.useHeight(hashIndex + HashIndex.FLAG.index) != HashValueType.EMPTY.index && boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_64BIT1.index) == (hashValue ushr 32).toInt() && boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_64BIT2.index) == (hashValue and LOW32).toInt()) {
        val isLocked = (boardHash.hashTableVersion
                - boardHash.useHeight(hashIndex + HashIndex.VERSION.index)
                <= SearchConfig.MAXIMUM_HASH_AGE.value)
        if (isLocked) {
            superVerifyUseHeightHash(board)
        }
        return isLocked
    }
    return false
}

fun isAlwaysReplaceHashTableEntryValid(depthRemaining: Int, board: EngineBoard): Boolean {
    val boardHash = board.boardHashObject
    val hashValue = boardHash.trackedHashValue
    val hashIndex = boardHash.getHashIndex(board)
    if (boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_HEIGHT.index) >= depthRemaining && boardHash.ignoreHeight(hashIndex + HashIndex.FLAG.index) != HashValueType.EMPTY.index && boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_64BIT1.index) == (hashValue ushr 32).toInt() && boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_64BIT2.index) == (hashValue and LOW32).toInt()) {
        val isLocked = boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HashIndex.VERSION.index) <= SearchConfig.MAXIMUM_HASH_AGE.value
        if (isLocked) {
            superVerifyAlwaysReplaceHash(board)
        }
        return isLocked
    }
    return false
}

private fun superVerifyUseHeightHash(board: EngineBoard) {
    val boardHash = board.boardHashObject
    val hashIndex = boardHash.getHashIndex(board)
    if (FeatureFlag.USE_SUPER_VERIFY_ON_HASH.isActive) {
        for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
            if (boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i) != (board.getBitboardByIndex(i) ushr 32).toInt() ||
                    boardHash.useHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12) != (board.getBitboardByIndex(i) and LOW32).toInt()) {
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
            if (boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i) != (board.getBitboardByIndex(i) ushr 32).toInt() ||
                    boardHash.ignoreHeight(hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12) != (board.getBitboardByIndex(i) and LOW32).toInt()) {
                System.exit(0)
            }
        }
    }
}
