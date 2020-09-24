package com.netsensia.rivalchess.engine.hash

import com.netsensia.rivalchess.bitboards.LOW32
import com.netsensia.rivalchess.config.DEFAULT_SEARCH_HASH_HEIGHT
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard

@kotlin.ExperimentalUnsignedTypes
class BoardHash {

    private val hashTracker = ZobristHashTracker()

    var hashTableVersion = 0

    private lateinit var hashTableUseHeight: IntArray
    private lateinit var hashTableIgnoreHeight: IntArray
    private var maxHashEntries = 0
    private var lastHashSizeCreated = 0

    fun useHeight(index: Int) = hashTableUseHeight[index]
    fun ignoreHeight(index: Int) = hashTableIgnoreHeight[index]

    fun setHashTableUseHeightVersion(index: Int, value: Int) {
        hashTableUseHeight[index + HASHENTRY_VERSION] = value
    }

    fun clearHash() {
        for (i in 0 until maxHashEntries) {
            hashTableUseHeight[i * NUM_HASH_FIELDS + HASHENTRY_FLAG] = EMPTY
            hashTableUseHeight[i * NUM_HASH_FIELDS + HASHENTRY_HEIGHT] = DEFAULT_SEARCH_HASH_HEIGHT
            hashTableIgnoreHeight[i * NUM_HASH_FIELDS + HASHENTRY_FLAG] = EMPTY
            hashTableIgnoreHeight[i * NUM_HASH_FIELDS + HASHENTRY_HEIGHT] = DEFAULT_SEARCH_HASH_HEIGHT
        }
    }

    fun setHashTable() {
        if (maxHashEntries != lastHashSizeCreated) {
            hashTableUseHeight = IntArray(maxHashEntries * NUM_HASH_FIELDS)
            hashTableIgnoreHeight = IntArray(maxHashEntries * NUM_HASH_FIELDS)
            lastHashSizeCreated = maxHashEntries
            for (i in 0 until maxHashEntries) {
                hashTableUseHeight[i * NUM_HASH_FIELDS + HASHENTRY_FLAG] = EMPTY
                hashTableUseHeight[i * NUM_HASH_FIELDS + HASHENTRY_HEIGHT] = DEFAULT_SEARCH_HASH_HEIGHT
                hashTableUseHeight[i * NUM_HASH_FIELDS + HASHENTRY_VERSION] = 1
                hashTableIgnoreHeight[i * NUM_HASH_FIELDS + HASHENTRY_FLAG] = EMPTY
                hashTableIgnoreHeight[i * NUM_HASH_FIELDS + HASHENTRY_HEIGHT] = DEFAULT_SEARCH_HASH_HEIGHT
                hashTableIgnoreHeight[i * NUM_HASH_FIELDS + HASHENTRY_VERSION] = 1
            }
        }
    }

    @kotlin.ExperimentalUnsignedTypes
    fun storeHashMove(move: Int, board: EngineBoard, score: Int, flag: Int, height: Int) {
        val hashIndex = (board.boardHashCode() % maxHashEntries).toInt() * NUM_HASH_FIELDS
        if (height >= hashTableUseHeight[hashIndex + HASHENTRY_HEIGHT] || hashTableVersion > hashTableUseHeight[hashIndex + HASHENTRY_VERSION]) {
            if (hashTableVersion == hashTableUseHeight[hashIndex + HASHENTRY_VERSION])
                copyEntryFromUseHeightToIgnoreHeightTable(hashIndex)
            storeMoveInHashTable(move, board, score, flag, height, hashIndex, hashTableUseHeight)
        } else {
            storeMoveInHashTable(move, board, score, flag, height, hashIndex, hashTableIgnoreHeight)
        }
    }

    private fun copyEntryFromUseHeightToIgnoreHeightTable(hashIndex: Int) {
        hashTableIgnoreHeight[hashIndex + HASHENTRY_MOVE] = hashTableUseHeight[hashIndex + HASHENTRY_MOVE]
        hashTableIgnoreHeight[hashIndex + HASHENTRY_SCORE] = hashTableUseHeight[hashIndex + HASHENTRY_SCORE]
        hashTableIgnoreHeight[hashIndex + HASHENTRY_FLAG] = hashTableUseHeight[hashIndex + HASHENTRY_FLAG]
        hashTableIgnoreHeight[hashIndex + HASHENTRY_64BIT1] = hashTableUseHeight[hashIndex + HASHENTRY_64BIT1]
        hashTableIgnoreHeight[hashIndex + HASHENTRY_64BIT2] = hashTableUseHeight[hashIndex + HASHENTRY_64BIT2]
        hashTableIgnoreHeight[hashIndex + HASHENTRY_HEIGHT] = hashTableUseHeight[hashIndex + HASHENTRY_HEIGHT]
        hashTableIgnoreHeight[hashIndex + HASHENTRY_VERSION] = hashTableUseHeight[hashIndex + HASHENTRY_VERSION]
    }

    @kotlin.ExperimentalUnsignedTypes
    private fun storeMoveInHashTable(move: Int, board: EngineBoard, score: Int, flag: Int, height: Int, hashIndex: Int, hashTableUseHeight: IntArray) {
        hashTableUseHeight[hashIndex + HASHENTRY_MOVE] = move
        hashTableUseHeight[hashIndex + HASHENTRY_SCORE] = score
        hashTableUseHeight[hashIndex + HASHENTRY_FLAG] = flag
        hashTableUseHeight[hashIndex + HASHENTRY_64BIT1] = (board.boardHashCode() ushr 32).toInt()
        hashTableUseHeight[hashIndex + HASHENTRY_64BIT2] = (board.boardHashCode() and LOW32).toInt()
        hashTableUseHeight[hashIndex + HASHENTRY_HEIGHT] = height
        hashTableUseHeight[hashIndex + HASHENTRY_VERSION] = hashTableVersion
    }

    fun setHashSizeMB(hashSizeMB: Int) {
        if (hashSizeMB < 1) {
            setMaxHashEntries(1)
        } else {
            val mainHashTableSize = hashSizeMB * 1024 * 1024 / 14 * 6 // two of these / 14 * 7 would be exactly half
            setMaxHashEntries(mainHashTableSize / HASH_POSITION_SIZE_BYTES)
        }
        setHashTable()
    }

    @kotlin.ExperimentalUnsignedTypes
    fun initialiseHashCode(engineBoard: EngineBoard) {
        hashTracker.initHash(engineBoard)
    }

    @kotlin.ExperimentalUnsignedTypes
    fun getHashIndex(engineBoard: EngineBoard): Int {
        return getHashIndex(engineBoard.boardHashCode())
    }

    @kotlin.ExperimentalUnsignedTypes
    fun getHashIndex(hashValue: Long): Int {
        return (hashValue % maxHashEntries).toInt() * NUM_HASH_FIELDS
    }

    @kotlin.ExperimentalUnsignedTypes
    fun move(compactMove: Int, movePiece: Int, capturePiece: Int) {
        hashTracker.makeMove(compactMove, movePiece, capturePiece)
    }

    @kotlin.ExperimentalUnsignedTypes
    fun unMove(engineBoard: EngineBoard) = hashTracker.unMakeMove(engineBoard.lastMoveMade!!)

    @kotlin.ExperimentalUnsignedTypes
    fun makeNullMove() = hashTracker.nullMove()

    val trackedHashValue: Long
        get() = hashTracker.trackedBoardHashValue

    private fun setMaxHashEntries(maxHashEntries: Int) {
        this.maxHashEntries = maxHashEntries
    }

    fun incVersion() {
        hashTableVersion++
    }
}