package com.netsensia.rivalchess.engine.core.hash

import com.netsensia.rivalchess.bitboards.LOW32
import com.netsensia.rivalchess.config.DEFAULT_SEARCH_HASH_HEIGHT
import com.netsensia.rivalchess.config.USE_SUPER_VERIFY_ON_HASH
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.HashIndex
import com.netsensia.rivalchess.enums.HashValueType
import com.netsensia.rivalchess.model.SquareOccupant

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
        hashTableUseHeight[index + HashIndex.VERSION.index] = value
    }

    fun clearHash() {
        for (i in 0 until maxHashEntries) {
            hashTableUseHeight[i * HashIndex.numHashFields + HashIndex.FLAG.index] = HashValueType.EMPTY.index
            hashTableUseHeight[i * HashIndex.numHashFields + HashIndex.HASHENTRY_HEIGHT.index] = DEFAULT_SEARCH_HASH_HEIGHT
            hashTableIgnoreHeight[i * HashIndex.numHashFields + HashIndex.FLAG.index] = HashValueType.EMPTY.index
            hashTableIgnoreHeight[i * HashIndex.numHashFields + HashIndex.HASHENTRY_HEIGHT.index] = DEFAULT_SEARCH_HASH_HEIGHT
        }
    }

    fun setHashTable() {
        if (maxHashEntries != lastHashSizeCreated) {
            hashTableUseHeight = IntArray(maxHashEntries * HashIndex.numHashFields)
            hashTableIgnoreHeight = IntArray(maxHashEntries * HashIndex.numHashFields)
            lastHashSizeCreated = maxHashEntries
            for (i in 0 until maxHashEntries) {
                hashTableUseHeight[i * HashIndex.numHashFields + HashIndex.FLAG.index] = HashValueType.EMPTY.index
                hashTableUseHeight[i * HashIndex.numHashFields + HashIndex.HASHENTRY_HEIGHT.index] = DEFAULT_SEARCH_HASH_HEIGHT
                hashTableUseHeight[i * HashIndex.numHashFields + HashIndex.VERSION.index] = 1
                hashTableIgnoreHeight[i * HashIndex.numHashFields + HashIndex.FLAG.index] = HashValueType.EMPTY.index
                hashTableIgnoreHeight[i * HashIndex.numHashFields + HashIndex.HASHENTRY_HEIGHT.index] = DEFAULT_SEARCH_HASH_HEIGHT
                hashTableIgnoreHeight[i * HashIndex.numHashFields + HashIndex.VERSION.index] = 1
            }
        }
    }

    fun storeHashMove(move: Int, board: EngineBoard, score: Int, flag: Byte, height: Int) {
        val hashIndex = (board.trackedBoardHashCode() % maxHashEntries).toInt() * HashIndex.numHashFields
        if (height >= hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.index] || hashTableVersion > hashTableUseHeight[hashIndex + HashIndex.VERSION.index]) {
            if (hashTableVersion == hashTableUseHeight[hashIndex + HashIndex.VERSION.index]) {
                copyEntryFromUseHeightToIgnoreHeightTable(hashIndex)
            }
            storeSuperVerifyUseHeightInformation(board, hashIndex)
            storeMoveInHashTable(move, board, score, flag, height, hashIndex, hashTableUseHeight)
        } else {
            storeSuperVerifyAlwaysReplaceInformation(board, hashIndex)
            storeMoveInHashTable(move, board, score, flag, height, hashIndex, hashTableIgnoreHeight)
        }
    }

    private fun copyEntryFromUseHeightToIgnoreHeightTable(hashIndex: Int) {
        hashTableIgnoreHeight[hashIndex + HashIndex.MOVE.index] = hashTableUseHeight[hashIndex + HashIndex.MOVE.index]
        hashTableIgnoreHeight[hashIndex + HashIndex.SCORE.index] = hashTableUseHeight[hashIndex + HashIndex.SCORE.index]
        hashTableIgnoreHeight[hashIndex + HashIndex.FLAG.index] = hashTableUseHeight[hashIndex + HashIndex.FLAG.index]
        hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_64BIT1.index] = hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT1.index]
        hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_64BIT2.index] = hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT2.index]
        hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.index] = hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.index]
        hashTableIgnoreHeight[hashIndex + HashIndex.VERSION.index] = hashTableUseHeight[hashIndex + HashIndex.VERSION.index]
    }

    private fun storeMoveInHashTable(move: Int, board: EngineBoard, score: Int, flag: Byte, height: Int, hashIndex: Int, hashTableUseHeight: IntArray) {
        hashTableUseHeight[hashIndex + HashIndex.MOVE.index] = move
        hashTableUseHeight[hashIndex + HashIndex.SCORE.index] = score
        hashTableUseHeight[hashIndex + HashIndex.FLAG.index] = flag.toInt()
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT1.index] = (board.trackedBoardHashCode() ushr 32).toInt()
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_64BIT2.index] = (board.trackedBoardHashCode() and LOW32).toInt()
        hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_HEIGHT.index] = height
        hashTableUseHeight[hashIndex + HashIndex.VERSION.index] = hashTableVersion
    }

    private fun storeSuperVerifyAlwaysReplaceInformation(board: EngineBoard, hashIndex: Int) {
        if (USE_SUPER_VERIFY_ON_HASH) {
            for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
                hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i] = (board.getBitboard(i) ushr 32).toInt()
                hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12] = (board.getBitboard(i) and LOW32).toInt()
            }
        }
    }

    private fun storeSuperVerifyUseHeightInformation(board: EngineBoard, hashIndex: Int) {
        if (USE_SUPER_VERIFY_ON_HASH) {
            for (i in SquareOccupant.WP.index..SquareOccupant.BR.index) {
                if (hashTableVersion == hashTableUseHeight[hashIndex + HashIndex.VERSION.index]) {
                    hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i] = hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i]
                    hashTableIgnoreHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12] = hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12]
                }
                hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i] = (board.getBitboard(i) ushr 32).toInt()
                hashTableUseHeight[hashIndex + HashIndex.HASHENTRY_LOCK1.index + i + 12] = (board.getBitboard(i) and LOW32).toInt()
            }
        }
    }

    fun setHashSizeMB(hashSizeMB: Int) {
        if (hashSizeMB < 1) {
            setMaxHashEntries(1)
        } else {
            val mainHashTableSize = hashSizeMB * 1024 * 1024 / 14 * 6 // two of these
            setMaxHashEntries(mainHashTableSize / HashIndex.hashPositionSizeBytes)
        }
        setHashTable()
    }

    fun initialiseHashCode(engineBoard: EngineBoard) {
        hashTracker.initHash(engineBoard)
    }

    fun getHashIndex(engineBoard: EngineBoard): Int {
        return getHashIndex(engineBoard.trackedBoardHashCode())
    }

    fun getHashIndex(hashValue: Long): Int {
        return (hashValue % maxHashEntries).toInt() * HashIndex.numHashFields
    }

    fun move(move: EngineMove, movePiece: SquareOccupant, capturePiece: SquareOccupant) {
        hashTracker.makeMove(move, movePiece, capturePiece)
    }

    fun unMove(engineBoard: EngineBoard) {
        hashTracker.unMakeMove(engineBoard.lastMoveMade)
    }

    fun makeNullMove() {
        hashTracker.nullMove()
    }

    val trackedHashValue: Long
        get() = hashTracker.trackedBoardHashValue

    private fun setMaxHashEntries(maxHashEntries: Int) {
        this.maxHashEntries = maxHashEntries
    }

    fun incVersion() {
        hashTableVersion++
    }
}