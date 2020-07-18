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

    var depthZeroMoveScores = IntArray(MAX_LEGAL_MOVES)

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

        iterativeDeepening(1, Window(-Int.MAX_VALUE, Int.MAX_VALUE))

        setSearchComplete()
    }

    private fun iterativeDeepening(depth: Int, aspirationWindow: Window) {
        iterativeDeepeningDepth = depth
        val newWindow = aspirationSearch(depth, aspirationWindow.low, aspirationWindow.high, 1)
        reorderDepthZeroMoves()
        if (currentPath.score >= MATE_SCORE_START) {
            val matePath = solveForMate(engineBoard, VALUE_MATE - currentPath.score)
            if (matePath != null) currentPath.setPath(matePath)
        }
        else if (!abortingSearch && depth < finalDepthToSearch) iterativeDeepening(depth + 1, newWindow)

    }

    private fun aspirationSearch(depth: Int, low: Int, high: Int, attempt: Int): Window {
        val path = searchZero(engineBoard, depth, 0, low, high)

        val newLow = if (path.score <= low) low - widenAspiration(attempt) else low
        val newHigh = if (path.score >= high) high + widenAspiration(attempt) else high

        if (newLow != low || newHigh != high && !abortingSearch) return aspirationSearch(depth, newLow, newHigh, attempt + 1)

        if (!abortingSearch) currentPath.setPath(path)
        return Window(currentPath.score - ASPIRATION_RADIUS, currentPath.score + ASPIRATION_RADIUS)

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
                val newPath = getPathFromSearch(move, useScoutSearch, depth, ply+1, myLow, high, extensions, isCheck).also { path ->
                    path.score = adjustedMateScore(-path.score)
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

        val theseMoves = orderMoves(highRankingMove, ply)
        moveOrderStatus[ply] = MoveOrder.NONE
        var legalMoveCount = 0

        for (move in theseMoves) {

            if (board.makeMove(move)) {

                legalMoveCount ++

                val plyExtensions = (checkExtend + threatExtend + pawnPushExtension()).coerceAtMost(maxExtensionsForPly(ply))
                val updatedExtensions = (extensions + plyExtensions).coerceAtMost(MAX_FRACTIONAL_EXTENSIONS)

                val moveGivesCheck = board.isCheck(mover)
                val lmr = lateMoveReductions(depthRemaining, legalMoveCount, moveGivesCheck, extensions, updatedExtensions)
                val adjustedDepth = depth - lmr
                val firstPath =
                        scoutSearch(useScoutSearch, adjustedDepth, ply+1, localLow, localHigh, updatedExtensions, moveGivesCheck).also {
                            it.score = adjustedMateScore(-it.score)
                        }

                // If we used LMR and it didn't fail low, research
                val newPath = if (lmr > 0 && firstPath.score > localLow)
                    scoutSearch(useScoutSearch, depth, ply+1, localLow, localHigh, updatedExtensions, moveGivesCheck).also {
                        it.score = adjustedMateScore(-it.score)
                    } else firstPath

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
                        useScoutSearch = useScoutSearch(depth, updatedExtensions)
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

    var a = 0
    private fun orderMoves(highRankingMove: Int, ply: Int): IntArray {
        val moves = engineBoard.moveGenerator().generateLegalMoves().moves
        val movesToSort = mutableListOf<Int>()
        moveSequence(moves).forEach { move ->
            val score = if (move == highRankingMove) 127 else
                (if (move == mateKiller[ply]) 126 else {
                    val toSquare = toSquare(move)
                    val isCapture = engineBoard.getBitboardTypeOfPieceOnSquare(toSquare, engineBoard.mover.opponent()) != BITBOARD_NONE ||
                            (1L shl toSquare and engineBoard.getBitboard(BITBOARD_ENPASSANTSQUARE) != 0L &&
                                    engineBoard.getBitboardTypeOfPieceOnSquare(fromSquare(move), engineBoard.mover) in arrayOf(BITBOARD_WP, BITBOARD_BP))
                    if (isCapture) {
                        val see = staticExchangeEvaluator.staticExchangeEvaluation(engineBoard, move)

                        if (see > 0) {
                            110 + (see / 100)
                        } else if (move and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                            109
                        } else if (see == 0) {
                            107
                        } else {
                            val historyScore = historyScore(engineBoard.mover == Colour.WHITE, fromSquare(move), toSquare)
                            if (historyScore > 5) historyScore else {
                                if (move == killerMoves[ply][0]) 106 else
                                    if (move == killerMoves[ply][1]) 105 else
                                        0
                            }
                        }
                    } else if (move and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                        108
                    } else if (moveGivesCheck(move)) {
                        99
                    } else {
                        val fromSquare = fromSquare(move)

                        val nonCaptureScore = if (move == killerMoves[ply][0]) 106 else
                            if (move == killerMoves[ply][1]) 105 else {
                                val historyScore = historyScore(engineBoard.mover == Colour.WHITE, fromSquare(move), toSquare)
                                if (historyScore == 0)
                                    (if (engineBoard.getBitboardTypeOfPieceOnSquare(toSquare, engineBoard.mover.opponent()) != BITBOARD_NONE) // losing capture
                                        1 else 50 + scorePieceSquareValues(engineBoard, fromSquare, toSquare) / 2)
                                else historyScore
                            }

                        nonCaptureScore - 60
                    }
                })
            movesToSort.add(move or ((127 - score) shl 24))
        }
        val sortedMoves = movesToSort.toIntArray()
        sortedMoves.sort()
        return sortedMoves
    }

    private fun lateMoveReductions(depthRemaining: Int, legalMoveCount: Int, moveGivesCheck: Boolean, extensions: Int, updatedExtensions: Int) =
            if (depthRemaining <= 3 || moveGivesCheck || extensions != updatedExtensions || legalMoveCount < 4) 0 else 1

    private fun wasPawnPush(): Boolean {
        val lastMove = engineBoard.moveHistory[engineBoard.numMovesMade-1]!!
        val lastMovedPiece = lastMove.movePiece
        val lastMoveTo = toSquare(lastMove.move)
        return (engineBoard.mover == Colour.WHITE && lastMovedPiece == BITBOARD_BP && lastMoveTo <= 15) ||
                (engineBoard.mover == Colour.BLACK && lastMovedPiece == BITBOARD_WP && lastMoveTo >= 48)
    }

    private fun wasCapture() = engineBoard.moveHistory[engineBoard.numMovesMade-1]!!.capturePiece != BITBOARD_NONE

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

    private fun scoutSearch(useScoutSearch: Boolean, depth: Int, ply: Int, low: Int, high: Int, newExtensions: Int, isCheck: Boolean) =
            if (useScoutSearch) {
                val scoutPath = search(engineBoard, (depth - 1), ply, -low-1, -low, newExtensions, isCheck)
                if (!abortingSearch && -scoutPath.score > low) {
                    search(engineBoard, (depth - 1), ply, -high, -low, newExtensions, isCheck)
                } else scoutPath
            } else {
                search(engineBoard, depth - 1, ply, -high, -low, newExtensions, isCheck)
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

    fun updateCurrentDepthZeroMove(move: Int, arrayIndex: Int) {
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

    fun abortIfTimeIsUp(): Boolean {
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

    private fun threatExtensions(newPath: SearchPath) = if (-newPath.score < -MATE_SCORE_START) FRACTIONAL_EXTENSION_THREAT else 0
    private fun checkExtension(isCheck: Boolean) = if (isCheck) FRACTIONAL_EXTENSION_CHECK else 0
    private fun pawnPushExtension() = if (wasPawnPush()) FRACTIONAL_EXTENSION_PAWNPUSH else 0

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

    fun determineDrawnPositionsAndGenerateDepthZeroMoves() {
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
        // if evaluate doesn't look like it will reach low, then it will stop calculating early and return
        // am incomplete value. Doesn't matter, because the score will become 'low' in a moment
        searchPath[ply].score = if (isCheck) low else evaluate(board, low)

        if (depth == 0 || searchPath[ply].score >= high) return searchPath[ply]

        var newLow = searchPath[ply].score.coerceAtLeast(low)

        setOrderedMovesArrayForQuiesce(isCheck, ply, board)
        var move = getHighestScoringMoveFromArray(orderedMoves[ply])
        var legalMoveCount = 0
        while (move != 0) {

            val movePiece = board.getBitboardTypeOfPieceOnSquare(move ushr 16, mover)

            if (!deltaPrune(isCheck, board, movePiece, move, searchPath[ply].score)) {
                if (board.makeMove(move)) {
                    legalMoveCount++

                    newPath = quiesce(board, depth - 1, ply + 1, quiescePly + 1, -high, -newLow, board.isCheck(mover)).also {
                        it.score = adjustedMateScore(-it.score)
                    }

                    board.unMakeMove()
                    if (newPath.score > searchPath[ply].score) {
                        searchPath[ply].setPath(move, newPath)
                        if (newPath.score >= high) return searchPath[ply]
                        newLow = newLow.coerceAtLeast(newPath.score)
                    }
                }
            }
            move = getHighestScoringMoveFromArray(orderedMoves[ply])
        }

        if (isCheck && legalMoveCount == 0) searchPath[ply].score = -VALUE_MATE

        return searchPath[ply]
    }

    private fun deltaPrune(isCheck: Boolean, board: EngineBoard, movePiece: Int, move: Int, low: Int): Boolean {
        if (isCheck || (board.whitePieceValues + board.blackPieceValues) < (VALUE_ROOK * 6)) return false

        val delta = (if (movePiece in arrayOf(BITBOARD_WP, BITBOARD_BP) && yCoordOfSquare(move and 63) in arrayOf(0,7))
            (VALUE_QUEEN - VALUE_PAWN) else 0) +
                pieceValue(board.getBitboardTypeOfPieceOnSquare(move and 63, mover.opponent()))

        return (delta + DELTA_PRUNING_MARGIN < low)
    }

    private fun setOrderedMovesArrayForQuiesce(isCheck: Boolean, ply: Int, board: EngineBoard) {
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

    fun initSearchVariables() {
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
