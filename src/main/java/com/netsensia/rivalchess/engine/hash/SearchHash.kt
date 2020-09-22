package com.netsensia.rivalchess.engine.hash

import com.netsensia.rivalchess.bitboards.LOW32
import com.netsensia.rivalchess.config.MAXIMUM_HASH_AGE
import com.netsensia.rivalchess.consts.*

@kotlin.ExperimentalUnsignedTypes
fun isHeightHashTableEntryValid(depthRemaining: Int, boardHash: BoardHash, hashIndex: Int) =
        if (boardHash.useHeight(hashIndex + HASHENTRY_HEIGHT) >= depthRemaining && boardHash.useHeight(hashIndex + HASHENTRY_FLAG) !=
                EMPTY && boardHash.useHeight(hashIndex + HASHENTRY_64BIT1) == (boardHash.trackedHashValue ushr 32).toInt() &&
                boardHash.useHeight(hashIndex + HASHENTRY_64BIT2) == (boardHash.trackedHashValue and LOW32).toInt()) {
            (boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HASHENTRY_VERSION) <= MAXIMUM_HASH_AGE)
        } else false

@kotlin.ExperimentalUnsignedTypes
fun isAlwaysReplaceHashTableEntryValid(depthRemaining: Int, boardHash: BoardHash, hashIndex: Int) =
        if (boardHash.ignoreHeight(hashIndex + HASHENTRY_HEIGHT) >= depthRemaining && boardHash.ignoreHeight(hashIndex + HASHENTRY_FLAG) !=
                EMPTY && boardHash.ignoreHeight(hashIndex + HASHENTRY_64BIT1) == (boardHash.trackedHashValue ushr 32).toInt() &&
                boardHash.ignoreHeight(hashIndex + HASHENTRY_64BIT2) == (boardHash.trackedHashValue and LOW32).toInt()) {
            boardHash.hashTableVersion - boardHash.useHeight(hashIndex + HASHENTRY_VERSION) <= MAXIMUM_HASH_AGE
        } else false
