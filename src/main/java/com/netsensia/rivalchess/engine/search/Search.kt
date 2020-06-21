package com.netsensia.rivalchess.engine.search

import UCI_TIMER_INTERVAL_MILLIS
import com.netsensia.rivalchess.bitboards.bitFlippedHorizontalAxis
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.consts.BITBOARD_ENEMY
import com.netsensia.rivalchess.consts.BITBOARD_ENPASSANTSQUARE
import com.netsensia.rivalchess.consts.FEN_START_POS
import com.netsensia.rivalchess.engine.board.*
import com.netsensia.rivalchess.engine.eval.*
import com.netsensia.rivalchess.engine.eval.see.StaticExchangeEvaluator
import com.netsensia.rivalchess.engine.eval.see.StaticExchangeEvaluatorSeeBoard
import com.netsensia.rivalchess.engine.hash.isAlwaysReplaceHashTableEntryValid
import com.netsensia.rivalchess.engine.hash.isHeightHashTableEntryValid
import com.netsensia.rivalchess.engine.type.EngineMove
import com.netsensia.rivalchess.enums.*
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.openings.OpeningLibrary
import com.netsensia.rivalchess.util.getSimpleAlgebraicMoveFromCompactMove
import java.io.PrintStream
import java.util.*
import kotlin.math.abs

class Search @JvmOverloads constructor(printStream: PrintStream = System.out, board: Board = getBoardModel(FEN_START_POS)) : Runnable {
    private val printStream: PrintStream
    private val engineBoard = EngineBoard(getBoardModel(FEN_START_POS))
    private val staticExchangeEvaluator: StaticExchangeEvaluator = StaticExchangeEvaluatorSeeBoard()

    private val moveOrderStatus = arrayOfNulls<MoveOrder>(MAX_TREE_DEPTH)
    private val drawnPositionsAtRoot: MutableList<MutableList<Long>>
    private val drawnPositionsAtRootCount = mutableListOf(0,0)
    private val mateKiller = IntArray(MAX_TREE_DEPTH) {-1}
    private val killerMoves: Array<IntArray>
    private val historyMovesSuccess = Array(2) { Array(64) { IntArray(64) } }
    private val historyMovesFail = Array(2) { Array(64) { IntArray(64) } }
    private val orderedMoves: Array<IntArray>
    private val searchPath: Array<SearchPath>

    private var depthZeroMoveScores = IntArray(MAX_LEGAL_MOVES)

    var engineState: SearchState
        private set
    private var quit = false
    var nodes = 0
    var millisSetByEngineMonitor: Long = 0

    private var millisToThink = 0
    private var nodesToSearch = Int.MAX_VALUE
    @JvmField
    var abortingSearch = true
    private var searchStartTime: Long = -1
    private var searchTargetEndTime: Long = 0
    private var searchEndTime: Long = 0
    private var finalDepthToSearch = 1
    var iterativeDeepeningDepth = 1 // current search depth for iterative deepening

    var currentDepthZeroMove = 0
    var currentDepthZeroMoveNumber = 0
    var currentPath: SearchPath
    var isUciMode = false

    var useOpeningBook = USE_INTERNAL_OPENING_BOOK
        set(useOpeningBook) {
            field = USE_INTERNAL_OPENING_BOOK && useOpeningBook
        }

    constructor(board: Board) : this(System.out, board) {}

    fun go() {
        initSearchVariables()
        if (isBookMoveAvailable()) return
        determineDrawnPositionsAndGenerateDepthZeroMoves()
        var result = AspirationSearchResult(null, Window(-Int.MAX_VALUE, Int.MAX_VALUE))

        for (depth in 1..finalDepthToSearch) {
            iterativeDeepeningDepth = depth
            result = aspirationSearch(depth, result.window)
            reorderDepthZeroMoves()
            if (abortingSearch) break
            currentPath.setPath((result.path)!!)
            if (result.path!!.score > MATE_SCORE_START) break
        }
        setSearchComplete()
    }

    private fun aspirationSearch(depth: Int, aspirationWindow: Window): AspirationSearchResult {
        var path: SearchPath
        var low = aspirationWindow.low
        var high = aspirationWindow.high

        path = searchZero(engineBoard, depth, 0, Window(low, high))
        if (!abortingSearch && path.score <= low) {
            low = -Int.MAX_VALUE
            path = searchZero(engineBoard, depth, 0, Window(low, high))
        } else if (!abortingSearch && path.score >= high) {
            high = Int.MAX_VALUE
            path = searchZero(engineBoard, depth, 0, Window(low, high))
        }

        if (!abortingSearch && (path.score <= low || path.score >= high))
            path = searchZero(engineBoard, depth, 0, Window(-Int.MAX_VALUE, Int.MAX_VALUE))

        if (!abortingSearch) {
            currentPath.setPath(path)
            low = path.score - ASPIRATION_RADIUS
            high = path.score + ASPIRATION_RADIUS
        }
        return AspirationSearchResult(path, Window(low, high))
    }

    @Throws(InvalidMoveException::class)
    fun searchZero(board: EngineBoard, depth: Int, ply: Int, window: Window): SearchPath {
        nodes ++
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

            if (engineBoard.makeMove(EngineMove(move))) {
                updateCurrentDepthZeroMove(move, ++numLegalMoves)

                val isCheck = board.isCheck(mover)
                val extensions = getExtensions(isCheck, board.wasPawnPush())
                val newPath = getPathFromSearch(move, useScoutSearch, depth, ply, Window(window.low, window.high), extensions, isCheck)

                if (abortingSearch) return SearchPath()

                newPath.score = -newPath.score
                if (newPath.score >= window.high) {
                    board.unMakeMove()
                    engineBoard.boardHashObject.storeHashMove(move, board, newPath.score, LOWER.toByte(), depth)
                    depthZeroMoveScores[numMoves] = newPath.score
                    return bestPath.withPath(move, newPath)
                }

                if (newPath.score > bestPath.score) {
                    bestPath.setPath(move, newPath)
                    if (newPath.score > window.low) {
                        hashEntryType = EXACT
                        bestMoveForHash = move
                        window.low = newPath.score
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

        engineBoard.boardHashObject.storeHashMove(bestMoveForHash, board, bestPath.score, hashEntryType.toByte(), depth)
        return bestPath
    }

    private fun onlyOneMoveAndNotOnFixedTime(numLegalMoves: Int) = numLegalMoves == 1 && millisToThink < MAX_SEARCH_MILLIS

    fun search(board: EngineBoard, depth: Int, ply: Int, window: Window, extensions: Int, recaptureSquare: Int, isCheck: Boolean): SearchPath {

        nodes++

        if (abortIfTimeIsUp()) return SearchPath()
        val searchPathPly = searchPath[ply].reset()

        if (board.previousOccurrencesOfThisPosition() == 2 || board.halfMoveCount >= 100) return searchPathPly.withScore(DRAW_CONTEMPT)
        if (board.onlyKingsRemain()) return searchPathPly.withScore(0)

        val depthRemaining = depth + extensions / FRACTIONAL_EXTENSION_FULL

        val hashProbeResult = hashProbe(board, depthRemaining, window, searchPathPly)
        if (hashProbeResult.bestPath != null) return searchPathPly
        var localLow = hashProbeResult.window.low
        val localHigh = hashProbeResult.window.high

        val checkExtend = checkExtension(extensions, isCheck)

        var hashFlag = UPPER
        if (depthRemaining <= 0) return finalPath(board, ply, localLow, localHigh, isCheck)

        val highRankingMove = highRankingMove(board, hashProbeResult.move, depthRemaining, depth, ply, Window(localLow, localHigh), extensions, recaptureSquare, isCheck)
        searchPathPly.reset()

        var bestMoveForHash = 0
        var useScoutSearch = false
        var threatExtend = 0

        if (performNullMove(board, depthRemaining, isCheck))
            searchNullMove(board, depth, nullMoveReduceDepth(depthRemaining), ply, Window(localLow, localHigh), extensions).also {
                if (abortingSearch) return SearchPath()
                adjustScoreForMateDepth(it)
                if (-it.score >= localHigh) return searchPathPly.withScore(-it.score)
                else threatExtend = threatExtensions(it, extensions)
            }

        orderedMoves[ply] = board.moveGenerator().generateLegalMoves().moves
        moveOrderStatus[ply] = MoveOrder.NONE
        val startNodes = nodes
        highScoreMoveSequence(board, ply, highRankingMove).forEach {
            val move = it
            val recaptureExtensionResponse =
                    recaptureExtensions(extensions,
                            board.getBitboardTypeOfPieceOnSquare(toSquare(move), board.mover.opponent()),
                            board.getBitboardTypeOfPieceOnSquare(fromSquare(move), board.mover), board, move, recaptureSquare)

            if (board.makeMove(EngineMove(move))) {

                val newExtensions = extensions + extensions(checkExtend, threatExtend, recaptureExtensionResponse.extend, pawnExtensions(extensions, board), maxExtensionsForPly(ply))

                val newPath =
                        scoutSearch(useScoutSearch, depth, ply, Window(localLow, localHigh), newExtensions,
                                recaptureExtensionResponse.captureSquare, board.isCheck(mover), board).also {
                            it.score = -it.score
                        }

                if (abortingSearch) return SearchPath()

                if (newPath.score >= localHigh) {
                    updateHistoryMoves(board.mover, move, depthRemaining, true)
                    board.unMakeMove()
                    board.boardHashObject.storeHashMove(move, board, newPath.score, LOWER.toByte(), depthRemaining)
                    updateKillerMoves(board.getBitboard(BITBOARD_ENEMY), move, ply, newPath)
                    return searchPathPly.withPath(move, newPath)
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
        if (nodes == startNodes) {
            board.boardHashObject.storeHashMove(0, board, searchPathPly.score, EXACT.toByte(), MAX_SEARCH_DEPTH)
            return searchPathPly.withScore(if (board.isCheck(mover)) -VALUE_MATE else 0)
        }
        board.boardHashObject.storeHashMove(bestMoveForHash, board, searchPathPly.score, hashFlag.toByte(), depthRemaining)
        return searchPathPly
    }

    private fun highScoreMoveSequence(board: EngineBoard, ply: Int, highRankingMove: Int) = sequence {
        while (getHighScoreMove(board, ply, highRankingMove).also { if (it != 0) yield(it) } != 0);
    }

    private fun pawnExtensions(extensions: Int, board: EngineBoard) =
            if (extensions / FRACTIONAL_EXTENSION_FULL < MAX_EXTENSION_DEPTH &&
                    FRACTIONAL_EXTENSION_PAWN > 0 && board.wasPawnPush()) 1 else 0

    private fun maxExtensionsForPly(ply: Int) =
            maxNewExtensionsTreePart[(ply / iterativeDeepeningDepth).coerceAtMost(LAST_EXTENSION_LAYER)]

    private fun updateKillerMoves(enemyBitboard: Long, move: Int, ply: Int, newPath: SearchPath) {
        if (enemyBitboard and toSquare(move).toLong() == 0L || move and PROMOTION_PIECE_TOSQUARE_MASK_FULL == 0) {
            killerMoves[ply][1] = killerMoves[ply][0]
            killerMoves[ply][0] = move
            if (USE_MATE_HISTORY_KILLERS && newPath.score > MATE_SCORE_START) mateKiller[ply] = move
        }
    }

    private fun recaptureExtensions(extensions: Int, targetPiece: Int, movePiece: Int, board: EngineBoard, move: Int, recaptureSquare: Int) =
            if (FRACTIONAL_EXTENSION_RECAPTURE > 0 && extensions / FRACTIONAL_EXTENSION_FULL < MAX_EXTENSION_DEPTH) {
                var currentSEEValue = -Int.MAX_VALUE
                var recaptureExtend = 0
                var newRecaptureSquare = -1
                if (targetPiece != -1 && pieceValues[movePiece] == pieceValues[targetPiece]) {
                    currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, EngineMove(move))
                    if (abs(currentSEEValue) <= RECAPTURE_EXTENSION_MARGIN) newRecaptureSquare = toSquare(move)
                }
                if (toSquare(move) == recaptureSquare) {
                    if (currentSEEValue == -Int.MAX_VALUE) currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, EngineMove(move))
                    if (abs(currentSEEValue) > getPieceValue(board.getPieceIndex(recaptureSquare)) - RECAPTURE_EXTENSION_MARGIN) {
                        recaptureExtend = 1
                    }
                }
                RecaptureExtensionResponse(recaptureExtend, newRecaptureSquare)
            } else {
                RecaptureExtensionResponse(-Int.MAX_VALUE, -1)
            }

    private fun scoutSearch(useScoutSearch: Boolean, depth: Int, ply: Int, window: Window, newExtensions: Int, newRecaptureSquare: Int, localIsCheck: Boolean, board: EngineBoard) =
            if (useScoutSearch) {
                val scoutPath = search(engineBoard, (depth - 1), ply + 1, Window(-window.low - 1, -window.low), newExtensions, newRecaptureSquare, localIsCheck).also {
                    if (it.score > MATE_SCORE_START) it.score-- else
                        if (it.score < -MATE_SCORE_START) it.score++
                }

                if (!abortingSearch && -scoutPath.score > window.low) {
                    search(engineBoard, (depth - 1), ply + 1, Window(-window.high, -window.low), newExtensions, newRecaptureSquare, localIsCheck).also {
                        if (it.score > MATE_SCORE_START) it.score-- else
                            if (it.score < -MATE_SCORE_START) it.score++
                    }
                } else scoutPath
            } else {
                search(board, depth - 1, ply + 1, Window(-window.high, -window.low), newExtensions, newRecaptureSquare, localIsCheck).also {
                    if (it.score > MATE_SCORE_START) it.score-- else
                        if (it.score < -MATE_SCORE_START) it.score++
                }
            }

    private fun highRankingMove(board: EngineBoard, hashMove: Int, depthRemaining: Int, depth: Int, ply: Int, window: Window, extensions: Int, recaptureSquare: Int, isCheck: Boolean): Int {
        if (hashMove == 0 && !board.isOnNullMove && USE_INTERNAL_ITERATIVE_DEEPENING && depthRemaining >= IID_MIN_DEPTH) {
            val iidDepth = depth - IID_REDUCE_DEPTH
            if (iidDepth > 0) search(board, iidDepth, ply, window, extensions, recaptureSquare, isCheck).also {
                if (it.height > 0) return it.move[0]
            }
        }
        return hashMove
    }

    private fun extensions(checkExtend: Int, threatExtend: Int, recaptureExtend: Int, pawnExtend: Int, maxNewExtensionsInThisPart: Int) =
            (checkExtend * FRACTIONAL_EXTENSION_CHECK +
                    threatExtend * FRACTIONAL_EXTENSION_THREAT +
                    recaptureExtend * FRACTIONAL_EXTENSION_RECAPTURE +
                    pawnExtend * FRACTIONAL_EXTENSION_PAWN).coerceAtMost(maxNewExtensionsInThisPart)

    private fun updateHistoryMoves(mover: Colour, move: Int, depthRemaining: Int, success: Boolean) {
        val historyMovesArray = if (success) historyMovesSuccess else historyMovesFail
        val moverIndex = if (mover == Colour.WHITE) 1 else 0
        val fromSquare = fromSquare(move)
        val toSquare = toSquare(move)
        if (USE_HISTORY_HEURISTIC) {
            historyMovesArray[moverIndex][fromSquare][toSquare] += depthRemaining
            if (historyMovesArray[moverIndex][fromSquare][toSquare] > HISTORY_MAX_VALUE) {
                for (i in 0..1) for (j in 0..63) for (k in 0..63) {
                    if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2
                    if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2
                }
            }
        }
    }

    private fun updateCurrentDepthZeroMove(move: Int, arrayIndex: Int) {
        currentDepthZeroMove = move
        currentDepthZeroMoveNumber = arrayIndex
    }

    private fun getExtensions(isCheck: Boolean, wasPawnPush: Boolean) =
            if (FRACTIONAL_EXTENSION_CHECK > 0 && isCheck) FRACTIONAL_EXTENSION_CHECK
            else if (FRACTIONAL_EXTENSION_PAWN > 0 && wasPawnPush) FRACTIONAL_EXTENSION_PAWN
            else 0

    private fun getPathFromSearch(move: Int, scoutSearch: Boolean, depth: Int, ply: Int, window: Window, extensions: Int, isCheck: Boolean) =
            if (isDrawnAtRoot()) SearchPath().withScore(0).withPath(move) else
                if (scoutSearch) {
                    val scoutPath = search(engineBoard, (depth - 1), ply + 1, Window(-window.low - 1, -window.low), extensions, -1, isCheck).also {
                        adjustScoreForMateDepth(it)
                    }

                    if (!abortingSearch && -scoutPath.score > window.low)
                        search(engineBoard, (depth - 1), ply + 1, Window(-window.high, -window.low), extensions, -1, isCheck)
                    else
                        scoutPath
                } else {
                    search(engineBoard, (depth - 1), ply + 1, Window(-window.high, -window.low), extensions, -1, isCheck)
                }.also {
                    adjustScoreForMateDepth(it)
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
        board.boardHashObject.storeHashMove(0, board, bestPath.score, hashFlag.toByte(),0)
        return bestPath
    }

    private fun abortIfTimeIsUp(): Boolean {
        if (millisSetByEngineMonitor > searchTargetEndTime || nodes >= nodesToSearch) abortingSearch = true
        return abortingSearch
    }

    private fun performNullMove(board: EngineBoard, depthRemaining: Int, isCheck: Boolean) =
            ((USE_NULL_MOVE_PRUNING && !isCheck && !board.isOnNullMove && depthRemaining > 1) &&
                    ((if (board.mover == Colour.WHITE) board.whitePieceValues else board.blackPieceValues) >= NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES &&
                            (if (board.mover == Colour.WHITE) board.whitePawnValues else board.blackPawnValues) > 0))

    private fun searchNullMove(board: EngineBoard, depth: Int, nullMoveReduceDepth: Int, ply: Int, window: Window, extensions: Int): SearchPath {
        board.makeNullMove()
        val newPath = search(board, (depth - nullMoveReduceDepth - 1), ply + 1, Window(-window.high, -window.low), extensions, -1, false)
        board.unMakeNullMove()
        return newPath
    }

    private fun threatExtensions(newPath: SearchPath, extensions: Int) =
            if (FRACTIONAL_EXTENSION_THREAT > 0 && -newPath.score < -MATE_SCORE_START && extensions / FRACTIONAL_EXTENSION_FULL < MAX_EXTENSION_DEPTH) 1 else 0

    private fun checkExtension(extensions: Int, isCheck: Boolean) =
            if (extensions / FRACTIONAL_EXTENSION_FULL < MAX_EXTENSION_DEPTH && FRACTIONAL_EXTENSION_CHECK > 0 && isCheck) 1 else 0

    private fun adjustScoreForMateDepth(newPath: SearchPath) {
        newPath.score += if (newPath.score > MATE_SCORE_START) -1
        else if (newPath.score < -MATE_SCORE_START) 1
        else 0
    }

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
        moveSequence(setDepthZeroMoves()).forEach {
            if (engineBoard.makeMove(EngineMove(it))) {
                val plyDraw = mutableListOf(false, false)

                if (engineBoard.previousOccurrencesOfThisPosition() == 2) plyDraw[0] = true

                moveSequence(boardMoves()).forEach {
                    if (engineBoard.makeMove(EngineMove(moveNoScore(it)))) {
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

    private fun setDepthZeroMoves(): IntArray {
        orderedMoves[0] = boardMoves()
        return orderedMoves[0]
    }

    private fun boardMoves() = engineBoard.moveGenerator().generateLegalMoves().moves

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

    @Throws(InvalidMoveException::class)
    private fun scoreQuiesceMoves(board: EngineBoard, ply: Int, includeChecks: Boolean) {
        var moveCount = 0
        var i = 0
        while (orderedMoves[ply][i] != 0) {
            var move = orderedMoves[ply][i]
            val isCapture = board.isCapture(move)
            move = moveNoScore(move)
            val score = board.getScore(move, includeChecks, isCapture, staticExchangeEvaluator)
            if (score > 0) orderedMoves[ply][moveCount++] = move or (127 - score shl 24)
            i++
        }
        orderedMoves[ply][moveCount] = 0
    }

    @Throws(InvalidMoveException::class)
    private fun getHighScoreMove(board: EngineBoard, ply: Int, hashMove: Int): Int {
        if (moveOrderStatus[ply] === MoveOrder.NONE) {
            if (hashMove != 0) {
                var c = 0
                while (orderedMoves[ply][c] != 0) {
                    if (orderedMoves[ply][c] == hashMove) {
                        orderedMoves[ply][c] = -1
                        return hashMove
                    }
                    c++
                }
            }
            moveOrderStatus[ply] = MoveOrder.CAPTURES
            if (scoreFullWidthCaptures(board, ply) == 0) {
                // no captures, so move to next stage
                scoreFullWidthMoves(board, ply)
                moveOrderStatus[ply] = MoveOrder.ALL
            }
        }
        val move = getHighestScoringMoveFromArray(orderedMoves[ply])
        return if (move == 0 && moveOrderStatus[ply] === MoveOrder.CAPTURES) {
            // we move into here if we had some captures but they are now used up
            scoreFullWidthMoves(board, ply)
            moveOrderStatus[ply] = MoveOrder.ALL
            getHighestScoringMoveFromArray(orderedMoves[ply])
        } else move
    }

    fun getHighestScoringMoveFromArray(theseMoves: IntArray): Int {
        var bestIndex = -1
        var best = Int.MAX_VALUE
        var c = -1
        while (theseMoves[++c] != 0) {
            if (theseMoves[c] != -1 && theseMoves[c] < best && theseMoves[c] shr 24 != 127) {
                // update best move found so far, but don't consider moves with no score
                best = theseMoves[c]
                bestIndex = c
            }
        }
        return if (best == Int.MAX_VALUE) {
            0
        } else {
            theseMoves[bestIndex] = -1
            moveNoScore(best)
        }
    }

    @Throws(InvalidMoveException::class)
    fun quiesce(board: EngineBoard, depth: Int, ply: Int, quiescePly: Int, low: Int, high: Int, isCheck: Boolean): SearchPath {
        nodes ++
        var newPath: SearchPath
        val evalScore = evaluate(board)

        searchPath[ply].height = 0
        searchPath[ply].score = if (isCheck) -VALUE_MATE else evalScore

        if (depth == 0 || searchPath[ply].score >= high) return searchPath[ply]
        var newLow = searchPath[ply].score.coerceAtLeast(low)
        setOrderedMovesArrayForQuiesce(isCheck, ply, board, quiescePly)
        var move = getHighestScoringMoveFromArray(orderedMoves[ply])
        var legalMoveCount = 0
        while (move != 0) {
            if (board.makeMove(EngineMove(move))) {
                legalMoveCount++
                newPath = quiesce(
                        board,
                        depth - 1,
                        ply + 1,
                        quiescePly + 1,
                        -high,
                        -newLow,
                        quiescePly <= GENERATE_CHECKS_UNTIL_QUIESCE_PLY && board.isCheck(mover))
                board.unMakeMove()
                newPath.score = -newPath.score
                if (newPath.score > searchPath[ply].score) {
                    searchPath[ply].setPath(move, newPath)
                }
                if (newPath.score >= high) {
                    return searchPath[ply]
                }
                newLow = newLow.coerceAtLeast(newPath.score)
            }
            move = getHighestScoringMoveFromArray(orderedMoves[ply])
        }
        if (isCheck && legalMoveCount == 0) {
            // all moves have been found to be illegal - delta pruning doesn't occur when in check
            searchPath[ply].score = -VALUE_MATE
        }
        return searchPath[ply]
    }

    private fun setOrderedMovesArrayForQuiesce(isCheck: Boolean, ply: Int, board: EngineBoard, quiescePly: Int) {
        if (isCheck) {
            orderedMoves[ply] = board.moveGenerator().generateLegalMoves().moves
            scoreFullWidthMoves(board, ply)
        } else {
            orderedMoves[ply] = board.moveGenerator()
                    .generateLegalQuiesceMoves(quiescePly <= GENERATE_CHECKS_UNTIL_QUIESCE_PLY)
                    .moves
            scoreQuiesceMoves(board, ply, quiescePly <= GENERATE_CHECKS_UNTIL_QUIESCE_PLY)
        }
    }

    private fun adjustedSee(see: Int) = if (see > -Int.MAX_VALUE) (see.toDouble() / VALUE_QUEEN * 10).toInt() else see

    @Throws(InvalidMoveException::class)
    private fun scoreFullWidthCaptures(board: EngineBoard, ply: Int): Int {
        var movesScored = 0
        var i = -1
        while (orderedMoves[ply][++i] != 0) {
            if (orderedMoves[ply][i] != -1 && scoreMove(ply, i, board) > 0) movesScored++
        }
        return movesScored
    }

    private fun scoreMove(ply: Int, i: Int, board: EngineBoard): Int {
        var score = 0
        val toSquare = toSquare(orderedMoves[ply][i])
        val isCapture = board.getPieceIndex(toSquare) != BITBOARD_NONE ||
                (1L shl toSquare and board.getBitboard(BITBOARD_ENPASSANTSQUARE) != 0L &&
                        board.getPieceIndex(fromSquare(orderedMoves[ply][i])) in arrayOf(BITBOARD_WP, BITBOARD_BP))

        orderedMoves[ply][i] = moveNoScore(orderedMoves[ply][i])
        if (orderedMoves[ply][i] == mateKiller[ply]) {
            score = 126
        } else if (isCapture) {

            val see = adjustedSee(staticExchangeEvaluator.staticExchangeEvaluation(board, EngineMove(orderedMoves[ply][i])))

            score = if (see > 0) {
                110 + see
            } else if (orderedMoves[ply][i] and PROMOTION_PIECE_TOSQUARE_MASK_FULL ==
                    PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
                109
            } else if (see == 0) {
                107
            } else {
                scoreLosingCapturesWithWinningHistory(board, ply, i, score, orderedMoves[ply], toSquare)
            }
        } else if (orderedMoves[ply][i] and PROMOTION_PIECE_TOSQUARE_MASK_FULL == PROMOTION_PIECE_TOSQUARE_MASK_QUEEN) {
            score = 108
        }
        orderedMoves[ply][i] = orderedMoves[ply][i] or (127 - score shl 24)
        return score
    }

    private fun scoreLosingCapturesWithWinningHistory(board: EngineBoard, ply: Int, i: Int, score: Int, movesForSorting: IntArray, toSquare: Int): Int {
        val historyScore = historyScore(board.mover == Colour.WHITE, fromSquare(movesForSorting[i]), toSquare)
        if (historyScore > 5) {
            return historyScore
        } else {
            for (j in 0 until 2) {
                if (movesForSorting[i] == killerMoves[ply][j]) {
                    return 106 - j
                }
            }
        }
        return score
    }

    private fun historyScore(isWhite: Boolean, from: Int, to: Int): Int {
        val colourIndex = if (isWhite) 0 else 1
        val success = historyMovesSuccess[colourIndex][from][to]
        val total = success + historyMovesFail[colourIndex][from][to]
        return if (total > 0) success * 10 / total else 0
    }

    private fun scoreFullWidthMoves(board: EngineBoard, ply: Int) {
        var i = 0
        while (orderedMoves[ply][i] != 0) {
            if (orderedMoves[ply][i] != -1) {
                val fromSquare = fromSquare(orderedMoves[ply][i])
                val toSquare = toSquare(orderedMoves[ply][i])
                orderedMoves[ply][i] = moveNoScore(orderedMoves[ply][i])

                val killerScore = scoreKillerMoves(ply, i, orderedMoves[ply])
                val historyScore = scoreHistoryHeuristic(board, killerScore, fromSquare, toSquare)

                val finalScore = if (historyScore == 0)
                    (if (board.getPieceIndex(toSquare) != BITBOARD_NONE) 1 // losing capture
                    else 50 + scorePieceSquareValues(
                            board,
                            if (board.mover == Colour.WHITE) fromSquare else bitFlippedHorizontalAxis[fromSquare],
                            if (board.mover == Colour.WHITE) toSquare else bitFlippedHorizontalAxis[toSquare]) / 2)
                else historyScore

                orderedMoves[ply][i] = orderedMoves[ply][i] or (127 - finalScore shl 24)
            }
            i++
        }
    }

    private fun scorePieceSquareValues(board: EngineBoard, fromSquare: Int, toSquare: Int) =
            when (board.getPieceIndex(fromSquare)) {
                BITBOARD_WP, BITBOARD_BP -> linearScale(
                        if (board.mover == Colour.WHITE) board.blackPieceValues else board.whitePieceValues,
                        PAWN_STAGE_MATERIAL_LOW,
                        PAWN_STAGE_MATERIAL_HIGH,
                        PieceSquareTables.pawnEndGame[toSquare] - PieceSquareTables.pawnEndGame[fromSquare],
                        PieceSquareTables.pawn[toSquare] - PieceSquareTables.pawn[fromSquare])
                BITBOARD_WN, BITBOARD_BN -> linearScale(
                        if (board.mover == Colour.WHITE) board.blackPieceValues + board.blackPawnValues else board.whitePieceValues + board.whitePawnValues,
                        KNIGHT_STAGE_MATERIAL_LOW,
                        KNIGHT_STAGE_MATERIAL_HIGH,
                        PieceSquareTables.knightEndGame[toSquare] - PieceSquareTables.knightEndGame[fromSquare],
                        PieceSquareTables.knight[toSquare] - PieceSquareTables.knight[fromSquare])
                BITBOARD_WB, BITBOARD_BB -> PieceSquareTables.bishop[toSquare] - PieceSquareTables.bishop[fromSquare]
                BITBOARD_WR, BITBOARD_BR -> PieceSquareTables.rook[toSquare] - PieceSquareTables.rook[fromSquare]
                BITBOARD_WQ, BITBOARD_BQ -> PieceSquareTables.queen[toSquare] - PieceSquareTables.queen[fromSquare]
                BITBOARD_WK, BITBOARD_BK -> linearScale(
                        if (board.mover == Colour.WHITE) board.blackPieceValues else board.whitePieceValues,
                        VALUE_ROOK,
                        OPENING_PHASE_MATERIAL,
                        PieceSquareTables.kingEndGame[toSquare] - PieceSquareTables.kingEndGame[fromSquare],
                        PieceSquareTables.king[toSquare] - PieceSquareTables.king[fromSquare])
                else -> 0
            }

    private fun scoreHistoryHeuristic(board: EngineBoard, score: Int, fromSquare: Int, toSquare: Int) =
            if (score == 0 && USE_HISTORY_HEURISTIC && historyMovesSuccess[if (board.mover == Colour.WHITE) 0 else 1][fromSquare][toSquare] > 0) {
                90 + historyScore(board.mover == Colour.WHITE, fromSquare, toSquare)
            } else score

    private fun scoreKillerMoves(ply: Int, i: Int, movesForSorting: IntArray): Int {
        if (movesForSorting[i] == killerMoves[ply][0]) return 106
        if (movesForSorting[i] == killerMoves[ply][1]) return 105
        return 0
    }

    val mover: Colour
        get() = engineBoard.mover

    private fun initSearchVariables() {
        engineState = SearchState.SEARCHING
        abortingSearch = false
        val boardHash = engineBoard.boardHashObject
        boardHash.incVersion()
        searchStartTime = System.currentTimeMillis()
        searchEndTime = 0
        searchTargetEndTime = searchStartTime + millisToThink - UCI_TIMER_INTERVAL_MILLIS
        nodes = 0
        setupMateAndKillerMoveTables()
        setupHistoryMoveTable()
    }

    private fun setupMateAndKillerMoveTables() {
        for (i in 0 until MAX_TREE_DEPTH) {
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

    fun makeMove(engineMove: EngineMove) {
        engineBoard.makeMove(engineMove)
    }

    init {
        engineBoard.setBoard(board)
        drawnPositionsAtRoot = ArrayList()
        drawnPositionsAtRoot.add(ArrayList())
        drawnPositionsAtRoot.add(ArrayList())
        this.printStream = printStream
        millisSetByEngineMonitor = System.currentTimeMillis()
        currentPath = SearchPath()
        engineState = SearchState.READY
        searchPath = Array(MAX_TREE_DEPTH) { SearchPath() }
        killerMoves = Array(MAX_TREE_DEPTH) { IntArray(2) }
        for (i in 0 until MAX_TREE_DEPTH) killerMoves[i] = IntArray(2)
        orderedMoves = Array(MAX_TREE_DEPTH) { IntArray(MAX_LEGAL_MOVES) }
    }
}