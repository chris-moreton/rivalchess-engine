package com.netsensia.rivalchess.engine.search

import UCI_TIMER_INTERVAL_MILLIS
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.consts.BITBOARD_ENEMY
import com.netsensia.rivalchess.consts.FEN_START_POS
import com.netsensia.rivalchess.engine.board.*
import com.netsensia.rivalchess.engine.eval.*
import com.netsensia.rivalchess.engine.eval.see.StaticExchangeEvaluator
import com.netsensia.rivalchess.engine.hash.isAlwaysReplaceHashTableEntryValid
import com.netsensia.rivalchess.engine.hash.isHeightHashTableEntryValid
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.enums.*
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.openings.OpeningLibrary
import com.netsensia.rivalchess.util.getSimpleAlgebraicMoveFromCompactMove
import java.io.PrintStream
import java.util.*

class Search @JvmOverloads constructor(printStream: PrintStream = System.out, board: Board = getBoardModel(FEN_START_POS)) : Runnable {
    private val printStream: PrintStream
    val staticExchangeEvaluator: StaticExchangeEvaluator = StaticExchangeEvaluator()

    val moveOrderStatus = arrayOfNulls<MoveOrder>(MAX_TREE_DEPTH)
    private val drawnPositionsAtRoot: MutableList<MutableList<Long>>
    private val drawnPositionsAtRootCount = mutableListOf(0,0)
    val mateKiller = IntArray(MAX_TREE_DEPTH)
    val killerMoves: Array<IntArray>
    val historyMovesSuccess = Array(2) { Array(64) { IntArray(64) } }
    val historyMovesFail = Array(2) { Array(64) { IntArray(64) } }
    val orderedMoves: Array<IntArray>
    private val searchPath: Array<SearchPath>

    private var depthZeroMoveScores = IntArray(MAX_LEGAL_MOVES)

    var engineState: SearchState
        private set
    private var quit = false
    var nodes = 0

    private var millisToThink = 0
    private var nodesToSearch = Int.MAX_VALUE
    @JvmField
    var abortingSearch = true
    private var searchStartTime: Long = -1
    private var searchTargetEndTime: Long = 0
    private var searchEndTime: Long = 0
    private var finalDepthToSearch = 1
    var iterativeDeepeningDepth = 1

    var currentDepthZeroMove = 0
    var currentDepthZeroMoveNumber = 0
    var currentPath: SearchPath
    var isUciMode = false

    val engineBoard = EngineBoard(board)

    var useOpeningBook = USE_INTERNAL_OPENING_BOOK
        set(useOpeningBook) {
            field = USE_INTERNAL_OPENING_BOOK && useOpeningBook
        }

    constructor(board: Board) : this(System.out, board) {}

    init {
        drawnPositionsAtRoot = ArrayList()
        drawnPositionsAtRoot.add(ArrayList())
        drawnPositionsAtRoot.add(ArrayList())
        this.printStream = printStream
        currentPath = SearchPath()
        engineState = SearchState.READY
        searchPath = Array(MAX_TREE_DEPTH) { SearchPath() }
        killerMoves = Array(MAX_TREE_DEPTH) { IntArray(2) }
        for (i in 0 until MAX_TREE_DEPTH) killerMoves[i] = IntArray(2)
        orderedMoves = Array(MAX_TREE_DEPTH) { IntArray(MAX_LEGAL_MOVES) }
    }

    fun go() {
        initSearchVariables()
        if (isBookMoveAvailable()) return
        determineDrawnPositionsAndGenerateDepthZeroMoves()
        var result = AspirationSearchResult(null, -Int.MAX_VALUE, Int.MAX_VALUE)

        for (depth in 1..finalDepthToSearch) {
            iterativeDeepeningDepth = depth
            result = aspirationSearch(depth, result.low, result.high)
            reorderDepthZeroMoves()
            if (abortingSearch) break
            currentPath.setPath(result.path!!)
            if (result.path!!.score > MATE_SCORE_START) break
        }

        setSearchComplete()
    }

    private fun aspirationSearch(depth: Int, low: Int, high: Int): AspirationSearchResult {
        var path: SearchPath
        var newLow = low
        var newHigh = high

        path = searchZero(engineBoard, depth, 0, low, high)

        if (!abortingSearch && path.score <= newLow) {
            newLow = -Int.MAX_VALUE
            path = searchZero(engineBoard, depth, 0, newLow, high)
        } else if (!abortingSearch && path.score >= high) {
            newHigh = Int.MAX_VALUE
            path = searchZero(engineBoard, depth, 0, low, newHigh)
        }

        if (!abortingSearch && (path.score <= newLow || path.score >= newHigh))
            path = searchZero(engineBoard, depth, 0, -Int.MAX_VALUE, Int.MAX_VALUE)

        if (!abortingSearch) {
            currentPath.setPath(path)
            newLow = path.score - ASPIRATION_RADIUS
            newHigh = path.score + ASPIRATION_RADIUS
        }

        return AspirationSearchResult(path, newLow, newHigh)
    }

    fun searchZero(board: EngineBoard, depth: Int, ply: Int, low: Int, high: Int): SearchPath {
        nodes ++
        var myLow = low
        var numMoves = 0
        var hashEntryType = UPPER
        var bestMoveForHash = 0
        var numLegalMoves = 0
        var useScoutSearch = false
        val bestPath = searchPath[0].reset()

        moveSequence(orderedMoves[0]).forEach {
            val move = moveNoScore(it)

            if (abortingSearch) return SearchPath()

            depthZeroMoveScores[numMoves] = -Int.MAX_VALUE

            if (engineBoard.makeMove(move)) {
                updateCurrentDepthZeroMove(move, ++numLegalMoves)

                val isCheck = board.isCheck(mover)
                val extensions = checkExtension(isCheck)
                val newPath = getPathFromSearch(move, useScoutSearch, depth, ply+1, myLow, high, extensions, isCheck).also {
                    it.score = adjustedMateScore(-it.score)
                }

                if (abortingSearch) return SearchPath()

                if (newPath.score >= high) {
                    board.unMakeMove()
                    engineBoard.boardHashObject.storeHashMove(move, board, newPath.score, LOWER, depth)
                    depthZeroMoveScores[numMoves] = newPath.score
                    return bestPath.withMoveAndScore(move, newPath.score)
                }

                if (newPath.score > bestPath.score) {
                    bestPath.setPath(move, newPath)
                    if (newPath.score > myLow) {
                        hashEntryType = EXACT
                        bestMoveForHash = move
                        myLow = newPath.score
                        useScoutSearch = useScoutSearch(depth, extensions)
                        currentPath.setPath(bestPath)
                    }
                }
                depthZeroMoveScores[numMoves] = newPath.score

                engineBoard.unMakeMove()
            }
            numMoves++
        }

        if (abortingSearch) return SearchPath()

        abortingSearch = onlyOneMoveAndNotOnFixedTime(numLegalMoves)

        engineBoard.boardHashObject.storeHashMove(bestMoveForHash, board, bestPath.score, hashEntryType, depth)
        return bestPath
    }

    fun search(board: EngineBoard, depth: Int, ply: Int, low: Int, high: Int, extensions: Int, isCheck: Boolean): SearchPath {

        nodes++

        if (abortIfTimeIsUp()) return SearchPath()
        val searchPathPly = searchPath[ply].reset()

        if (isDraw()) return searchPathPly.withScore(0)

        val depthRemaining = depth + extensions / FRACTIONAL_EXTENSION_FULL

        val hashProbeResult = hashProbe(board, depthRemaining, Window(low, high), searchPathPly)
        if (hashProbeResult.bestPath != null) return searchPathPly

        var localLow = hashProbeResult.window.low
        val localHigh = hashProbeResult.window.high

        val checkExtend = checkExtension(isCheck)

        var hashFlag = UPPER
        if (depthRemaining <= 0) return finalPath(board, ply, localLow, localHigh, isCheck)

        val highRankingMove = highRankingMove(board, hashProbeResult.move, depthRemaining, depth, ply, localLow, localHigh, extensions, isCheck)
        searchPathPly.reset()

        var bestMoveForHash = 0
        var useScoutSearch = false
        var threatExtend = 0

        if (performNullMove(board, depthRemaining, isCheck))
            searchNullMove(board, depth, nullMoveReduceDepth(depthRemaining), ply+1, localLow, localHigh, extensions).also {
                if (abortingSearch) return SearchPath()
                if (-it.score >= localHigh) return searchPathPly.withScore(-it.score)
                threatExtend = threatExtensions(it)
            }

        orderedMoves[ply] = board.moveGenerator().generateLegalMoves().moves
        moveOrderStatus[ply] = MoveOrder.NONE
        var legalMoveCount = 0
        val moveExtensions = (checkExtend + threatExtend).coerceAtMost(maxExtensionsForPly(ply))
        val newExtensions = (extensions + moveExtensions).coerceAtMost(MAX_FRACTIONAL_EXTENSIONS)
        for (move in highScoreMoveSequence(board, ply, highRankingMove)) {

            if (board.makeMove(move)) {

                legalMoveCount ++
                val newPath =
                    scoutSearch(useScoutSearch, depth, ply+1, localLow, localHigh, newExtensions, board.isCheck(mover)).also {
                        it.score = adjustedMateScore(-it.score)
                    }

                if (abortingSearch) return SearchPath()

                if (newPath.score >= localHigh) {
                    updateHistoryMoves(board.mover, move, depthRemaining, true)
                    board.unMakeMove()
                    board.boardHashObject.storeHashMove(move, board, newPath.score, LOWER, depthRemaining)
                    updateKillerMoves(board.getBitboard(BITBOARD_ENEMY), move, ply, newPath)
                    return searchPathPly.withMoveAndScore(move, newPath.score)
                }

                if (newPath.score > searchPathPly.score) {
                    searchPathPly.setPath(move, newPath)
                    if (newPath.score > localLow) {
                        hashFlag = EXACT
                        bestMoveForHash = move
                        localLow = newPath.score
                        useScoutSearch = useScoutSearch(depth, newExtensions)
                    }
                }

                updateHistoryMoves(board.mover, move, depthRemaining, false)
                board.unMakeMove()
            }
        }
        if (abortingSearch) return SearchPath()
        if (legalMoveCount == 0) {
            searchPathPly.withScore(if (board.isCheck(mover)) -(VALUE_MATE-0) else 0)
            board.boardHashObject.storeHashMove(0, board, searchPathPly.score, EXACT, MAX_SEARCH_DEPTH)
            return searchPathPly
        }
        board.boardHashObject.storeHashMove(bestMoveForHash, board, searchPathPly.score, hashFlag, depthRemaining)
        return searchPathPly
    }

    private fun adjustedMateScore(score: Int) = if (score > MATE_SCORE_START) score-1 else (if (score < -MATE_SCORE_START) score+1 else score)

    private fun isDraw() = engineBoard.previousOccurrencesOfThisPosition() == 2 || engineBoard.halfMoveCount >= 100 || engineBoard.onlyKingsRemain()

    private fun onlyOneMoveAndNotOnFixedTime(numLegalMoves: Int) = numLegalMoves == 1 && millisToThink < MAX_SEARCH_MILLIS

    private fun highScoreMoveSequence(board: EngineBoard, ply: Int, highRankingMove: Int) = sequence {
        while (getHighScoreMove(board, ply, highRankingMove).also { if (it != 0) yield(it) } != 0);
    }

    private fun maxExtensionsForPly(ply: Int) = maxNewExtensionsTreePart[(ply / iterativeDeepeningDepth).coerceAtMost(LAST_EXTENSION_LAYER)]

    private fun updateKillerMoves(enemyBitboard: Long, move: Int, ply: Int, newPath: SearchPath) {
        if (enemyBitboard and toSquare(move).toLong() == 0L || move and PROMOTION_PIECE_TOSQUARE_MASK_FULL == 0) {
            killerMoves[ply][1] = killerMoves[ply][0]
            killerMoves[ply][0] = move
            if (USE_MATE_HISTORY_KILLERS && newPath.score > MATE_SCORE_START) mateKiller[ply] = move
        }
    }

    private fun scoutSearch(useScoutSearch: Boolean, depth: Int, ply: Int, low: Int, high: Int, newExtensions: Int, localIsCheck: Boolean) =
        if (useScoutSearch) {
            val scoutPath = search(engineBoard, (depth - 1), ply, -low-1, -low, newExtensions, localIsCheck)
            if (!abortingSearch && -scoutPath.score > low) {
                search(engineBoard, (depth - 1), ply, -high, -low, newExtensions, localIsCheck)
            } else scoutPath
        } else {
            search(engineBoard, depth - 1, ply, -high, -low, newExtensions, localIsCheck)
        }

    private fun getPathFromSearch(move: Int, scoutSearch: Boolean, depth: Int, ply: Int, low: Int, high: Int, extensions: Int, isCheck: Boolean) =
        if (isDrawnAtRoot()) SearchPath().withScore(0).withPath(move) else
            scoutSearch(scoutSearch, depth, ply, low, high, extensions, isCheck)

    private fun highRankingMove(board: EngineBoard, hashMove: Int, depthRemaining: Int, depth: Int, ply: Int, low: Int, high: Int, extensions: Int, isCheck: Boolean): Int {
        if (hashMove == 0 && !board.isOnNullMove && USE_INTERNAL_ITERATIVE_DEEPENING && depthRemaining >= IID_MIN_DEPTH) {
            val iidDepth = depth - IID_REDUCE_DEPTH
            if (iidDepth > 0) search(board, iidDepth, ply, low, high, extensions, isCheck).also {
                if (it.height > 0) return it.move[0]
            }
        }
        return hashMove
    }

    private fun updateHistoryMoves(mover: Colour, move: Int, depthRemaining: Int, success: Boolean) {
        val historyMovesArray = if (success) historyMovesSuccess else historyMovesFail
        val moverIndex = if (mover == Colour.WHITE) 1 else 0
        val fromSquare = fromSquare(move)
        val toSquare = toSquare(move)
        historyMovesArray[moverIndex][fromSquare][toSquare] += depthRemaining
        if (historyMovesArray[moverIndex][fromSquare][toSquare] > HISTORY_MAX_VALUE) {
            for (i in 0..1) for (j in 0..63) for (k in 0..63) {
                if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2
                if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2
            }
        }
    }

    private fun updateCurrentDepthZeroMove(move: Int, arrayIndex: Int) {
        currentDepthZeroMove = move
        currentDepthZeroMoveNumber = arrayIndex
    }

    private fun hashProbe(board: EngineBoard, depthRemaining: Int, window: Window, bestPath: SearchPath): HashProbeResult {
        val boardHash = board.boardHashObject
        var hashMove = 0
        val hashIndex = board.boardHashObject.getHashIndex(board)

        if (USE_HEIGHT_REPLACE_HASH && isHeightHashTableEntryValid(depthRemaining, boardHash, hashIndex)) {
            boardHash.setHashTableUseHeightVersion(hashIndex, boardHash.hashTableVersion)
            hashMove = boardHash.useHeight(hashIndex + HASHENTRY_MOVE)
            val flag = boardHash.useHeight(hashIndex + HASHENTRY_FLAG)
            val score = boardHash.useHeight(hashIndex + HASHENTRY_SCORE)

            if (hashProbeResult(flag, score, window)) return HashProbeResult(hashMove, window, bestPath.withScore(score).withPath(hashMove))
        }

        if (USE_ALWAYS_REPLACE_HASH && hashMove == 0 && isAlwaysReplaceHashTableEntryValid(depthRemaining, boardHash, hashIndex)) {
            hashMove = boardHash.ignoreHeight(hashIndex + HASHENTRY_MOVE)
            val flag = boardHash.ignoreHeight(hashIndex + HASHENTRY_FLAG)
            val score = boardHash.ignoreHeight(hashIndex + HASHENTRY_SCORE)
            if (hashProbeResult(flag, score, window)) return HashProbeResult(hashMove, window, bestPath.withScore(score).withPath(hashMove))
        }

        return HashProbeResult(hashMove, window, null)
    }

    private fun hashProbeResult(flag: Int, score: Int, window: Window): Boolean {
        when (flag) {
            EXACT -> return true
            LOWER -> if (score > window.low) window.low = score
            UPPER -> if (score < window.high) window.high = score
        }
        return window.low >= window.high
    }

    private fun finalPath(board: EngineBoard, ply: Int, low: Int, high: Int, isCheck: Boolean): SearchPath {
        val bestPath = quiesce(board, MAX_QUIESCE_DEPTH - 1, ply, 0, low, high, isCheck)
        val hashFlag = if (bestPath.score < low) UPPER else if (bestPath.score > high) LOWER else EXACT
        board.boardHashObject.storeHashMove(0, board, bestPath.score, hashFlag,0)
        return bestPath
    }

    private fun abortIfTimeIsUp(): Boolean {
        if (System.currentTimeMillis() > searchTargetEndTime || nodes >= nodesToSearch) abortingSearch = true
        return abortingSearch
    }

    private fun performNullMove(board: EngineBoard, depthRemaining: Int, isCheck: Boolean) =
            ((USE_NULL_MOVE_PRUNING && !isCheck && !board.isOnNullMove && depthRemaining > 1) &&
                    ((if (board.mover == Colour.WHITE) board.whitePieceValues else board.blackPieceValues) >= NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES &&
                            (if (board.mover == Colour.WHITE) board.whitePawnValues else board.blackPawnValues) > 0))

    private fun searchNullMove(board: EngineBoard, depth: Int, nullMoveReduceDepth: Int, ply: Int, low: Int, high: Int, extensions: Int): SearchPath {
        board.makeNullMove()
        val newPath = search(board, (depth - nullMoveReduceDepth - 1), ply, -high, -low, extensions, false)
        board.unMakeNullMove()
        return newPath
    }

    private fun threatExtensions(newPath: SearchPath) =
            if (-newPath.score < -MATE_SCORE_START) FRACTIONAL_EXTENSION_THREAT else 0

    private fun checkExtension(isCheck: Boolean) =
            if (isCheck) FRACTIONAL_EXTENSION_CHECK else 0

    private fun reorderDepthZeroMoves() {
        val moveCount = moveCount(orderedMoves[0])
        for (pass in 1 until moveCount) {
            for (i in 0 until moveCount - pass) {
                if (depthZeroMoveScores[i] < depthZeroMoveScores[i + 1]) {
                    swapElements(depthZeroMoveScores, i, i + 1)
                    swapElements(orderedMoves[0], i, i + 1)
                }
            }
        }
    }

    private fun determineDrawnPositionsAndGenerateDepthZeroMoves() {
        orderedMoves[0] = engineBoard.moveGenerator().generateLegalMoves().moves
        moveSequence(orderedMoves[0]).forEach {
            if (engineBoard.makeMove(it)) {
                val plyDraw = booleanArrayOf(false, false)

                if (engineBoard.previousOccurrencesOfThisPosition() == 2) plyDraw[0] = true

                moveSequence(engineBoard.moveGenerator().generateLegalMoves().moves).forEach {
                    if (engineBoard.makeMove(moveNoScore(it))) {
                        if (engineBoard.previousOccurrencesOfThisPosition() == 2) plyDraw[1] = true
                        engineBoard.unMakeMove()
                    }
                }
                for (i in 0..1)
                    if (plyDraw[i]) drawnPositionsAtRoot[i].add(engineBoard.boardHashObject.trackedHashValue)
                engineBoard.unMakeMove()
            }
        }
        scoreFullWidthMoves(engineBoard, 0)
    }

    private fun isBookMoveAvailable(): Boolean {
        if (useOpeningBook) {
            val libraryMove = OpeningLibrary.getMove(getFen())
            if (libraryMove != null) {
                currentPath = SearchPath().withPath(EngineMove(libraryMove).compact)
                setSearchComplete()
                return true
            }
        }
        return false
    }

    fun setHashSizeMB(hashSizeMB: Int) {
        engineBoard.boardHashObject.setHashSizeMB(hashSizeMB)
    }

    fun setBoard(board: Board) {
        engineBoard.setBoard(board)
        engineBoard.boardHashObject.incVersion()
        engineBoard.boardHashObject.setHashTable()
    }

    fun clearHash() {
        engineBoard.boardHashObject.clearHash()
    }

    fun newGame() {
        engineBoard.boardHashObject.clearHash()
    }

    private fun scoreQuiesceMoves(board: EngineBoard, ply: Int) {
        var moveCount = 0
        var i = 0
        while (orderedMoves[ply][i] != 0) {
            var move = orderedMoves[ply][i]
            val isCapture = board.isCapture(move)
            move = moveNoScore(move)
            val score = board.getScore(move, isCapture, staticExchangeEvaluator)
            if (score > 0) orderedMoves[ply][moveCount++] = move or (127 - score shl 24)
            i++
        }
        orderedMoves[ply][moveCount] = 0
    }

    private fun quiesce(board: EngineBoard, depth: Int, ply: Int, quiescePly: Int, low: Int, high: Int, isCheck: Boolean): SearchPath {
        nodes ++
        var newPath: SearchPath

        searchPath[ply].height = 0
        searchPath[ply].score = if (isCheck) low else evaluate(board)

        if (depth == 0 || searchPath[ply].score >= high) return searchPath[ply]

        var newLow = searchPath[ply].score.coerceAtLeast(low)
        setOrderedMovesArrayForQuiesce(isCheck, ply, board, quiescePly)
        var move = getHighestScoringMoveFromArray(orderedMoves[ply])
        var legalMoveCount = 0
        while (move != 0) {
            if (board.makeMove(move)) {
                legalMoveCount ++
                newPath = quiesce(board, depth - 1,ply + 1,quiescePly + 1, -high, -newLow, board.isCheck(mover)).also {
                    it.score = adjustedMateScore(-it.score)
                }
                board.unMakeMove()
                if (newPath.score > searchPath[ply].score) {
                    searchPath[ply].setPath(move, newPath)
                    if (newPath.score >= high) return searchPath[ply]
                    newLow = newLow.coerceAtLeast(newPath.score)
                }
            }
            move = getHighestScoringMoveFromArray(orderedMoves[ply])
        }

        if (isCheck && legalMoveCount == 0) searchPath[ply].score = -VALUE_MATE

        return searchPath[ply]
    }

    private fun setOrderedMovesArrayForQuiesce(isCheck: Boolean, ply: Int, board: EngineBoard, quiescePly: Int) {
        if (isCheck) {
            orderedMoves[ply] = board.moveGenerator().generateLegalMoves().moves
            scoreFullWidthMoves(board, ply)
        } else {
            orderedMoves[ply] = board.moveGenerator().generateLegalQuiesceMoves().moves
            scoreQuiesceMoves(board, ply)
        }
    }

    val mover: Colour
        inline get() = engineBoard.mover

    private fun initSearchVariables() {
        engineState = SearchState.SEARCHING
        abortingSearch = false
        engineBoard.boardHashObject.incVersion()
        searchStartTime = System.currentTimeMillis()
        searchEndTime = 0
        searchTargetEndTime = searchStartTime + millisToThink - UCI_TIMER_INTERVAL_MILLIS
        nodes = 0
        setupMateAndKillerMoveTables()
        setupHistoryMoveTable()
    }

    private fun setupMateAndKillerMoveTables() {
        for (i in 0 until MAX_TREE_DEPTH) {
            mateKiller[i] = -1
            killerMoves[i][0] = -1
            killerMoves[i][1] = -1
        }
    }

    private fun setupHistoryMoveTable() {
        for (i in 0..63) for (j in 0..63) {
            historyMovesSuccess[0][i][j] = 0
            historyMovesSuccess[1][i][j] = 0
            historyMovesFail[0][i][j] = 0
            historyMovesFail[1][i][j] = 0
        }
    }

    fun setMillisToThink(millisToThink: Int) {
        this.millisToThink = if (millisToThink < MIN_SEARCH_MILLIS) MIN_SEARCH_MILLIS else millisToThink
    }

    fun setNodesToSearch(nodesToSearch: Int) {
        this.nodesToSearch = nodesToSearch
    }

    fun setSearchDepth(searchDepth: Int) {
        finalDepthToSearch = if (searchDepth < 0) 1 else searchDepth
    }

    val isSearching: Boolean
        get() = engineState === SearchState.SEARCHING || engineState === SearchState.REQUESTED

    val currentScoreHuman: String
        get() {
            val score = currentPath.score
            val abs = Math.abs(score)
            if (abs > MATE_SCORE_START) {
                val mateIn = (VALUE_MATE - abs + 1) / 2
                return "mate " + (if (score < 0) "-" else "") + mateIn
            }
            return "cp $score"
        }

    val currentScore: Int
        get() = currentPath.score

    val searchDuration: Long
        get() {
            return when (engineState) {
                SearchState.READY -> 0
                SearchState.SEARCHING -> System.currentTimeMillis() - searchStartTime
                SearchState.COMPLETE -> searchEndTime - searchStartTime
                SearchState.REQUESTED -> 0
            }
        }

    val nodesPerSecond: Int
        get() {
            val timePassed = searchDuration
            return if (timePassed == 0L) 0 else (nodes.toDouble() / timePassed.toDouble() * 1000.0).toInt()
        }

    private fun isDrawnAtRoot(): Boolean {
        val boardHash = engineBoard.boardHashObject
        var i = 0
        while (i < drawnPositionsAtRootCount[0]) {
            if (drawnPositionsAtRoot[0][i] == boardHash.trackedHashValue) {
                return true
            }
            i++
        }
        return false
    }

    val currentMove: Int
        get() = currentPath.move[0]

    fun startSearch() {
        engineState = SearchState.REQUESTED
    }

    fun stopSearch() {
        abortingSearch = true
    }

    fun setSearchComplete() {
        searchEndTime = System.currentTimeMillis()
        engineState = SearchState.COMPLETE
    }

    fun quit() {
        quit = true
    }

    override fun run() {
        while (!quit) {
            Thread.yield()
            if (engineState === SearchState.REQUESTED) {
                go()
                if (isUciMode) {
                    val s1 = "info" +
                            " currmove " + getSimpleAlgebraicMoveFromCompactMove(currentDepthZeroMove) +
                            " currmovenumber " + currentDepthZeroMoveNumber +
                            " depth " + iterativeDeepeningDepth +
                            " score " + currentScoreHuman +
                            " pv " + currentPath.toString() +
                            " time " + searchDuration +
                            " nodes " + nodes +
                            " nps " + nodesPerSecond
                    val s2 = "bestmove " + getSimpleAlgebraicMoveFromCompactMove(currentMove)
                    printStream.println(s1)
                    printStream.println(s2)
                }
            }
        }
    }

    fun isOkToSendInfo() = engineState == SearchState.SEARCHING && iterativeDeepeningDepth > 1 && !abortingSearch

    fun getFen() = engineBoard.getFen()

    fun makeMove(compactMove: Int) {
        engineBoard.makeMove(compactMove)
    }

}
