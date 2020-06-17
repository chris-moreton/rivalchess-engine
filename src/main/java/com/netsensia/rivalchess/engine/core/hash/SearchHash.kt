package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.bitboards.LOW32
import com.netsensia.rivalchess.config.MAXIMUM_HASH_AGE
import com.netsensia.rivalchess.enums.*

fun isHeightHashTableEntryValid(depthRemaining: Int, boardHash: BoardHash, hashIndex: Int) =
        if (boardHash.useHeight(hashIndex + HASHENTRY_HEIGHT) >= depthRemaining && boardHash.useHeight(hashIndex + HASHENTRY_FLAG) !=
                HashValueType.EMPTY.index && boardHash.useHeight(hashIndex + HASHENTRY_64BIT1) == (boardHash.trackedHashValue ushr 32).toInt() &&
                boardHash.useHeight(hashIndex + HASHENTRY_64BIT2) == (boardHash.trackedHashValue and LOW32).toInt()) {
            (boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HASHENTRY_VERSION) <= MAXIMUM_HASH_AGE)
        } else false


fun isAlwaysReplaceHashTableEntryValid(depthRemaining: Int, boardHash: BoardHash, hashIndex: Int) =
        if (boardHash.ignoreHeight(hashIndex + HASHENTRY_HEIGHT) >= depthRemaining && boardHash.ignoreHeight(hashIndex + HASHENTRY_FLAG) !=
                HashValueType.EMPTY.index && boardHash.ignoreHeight(hashIndex + HASHENTRY_64BIT1) == (boardHash.trackedHashValue ushr 32).toInt() &&
                boardHash.ignoreHeight(hashIndex + HASHENTRY_64BIT2) == (boardHash.trackedHashValue and LOW32).toInt()) {
            boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HASHENTRY_VERSION) <= MAXIMUM_HASH_AGE
        } else false
