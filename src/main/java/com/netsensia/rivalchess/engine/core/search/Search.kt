package com.netsensia.rivalchess.engine.core.search

import com.ea.async.Async
import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.bitFlippedHorizontalAxis
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.engine.core.FEN_START_POS
import com.netsensia.rivalchess.engine.core.board.*
import com.netsensia.rivalchess.engine.core.eval.*
import com.netsensia.rivalchess.engine.core.hash.isAlwaysReplaceHashTableEntryValid
import com.netsensia.rivalchess.engine.core.hash.isHeightHashTableEntryValid
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.enums.*
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.openings.OpeningLibrary
import com.netsensia.rivalchess.util.ChessBoardConversion
import java.io.PrintStream
import java.util.*

class Search @JvmOverloads constructor(printStream: PrintStream = System.out, board: Board = getBoardModel(FEN_START_POS)) : Runnable {
    private val printStream: PrintStream
    private val engineBoard = EngineBoard(getBoardModel(FEN_START_POS))
    private val staticExchangeEvaluator: StaticExchangeEvaluator = StaticExchangeEvaluatorPremium()

    private val moveOrderStatus = arrayOfNulls<MoveOrder>(Limit.MAX_TREE_DEPTH.value)
    private val drawnPositionsAtRoot: MutableList<MutableList<Long>>
    private val drawnPositionsAtRootCount = mutableListOf(0,0)
    private val mateKiller: MutableList<Int> = ArrayList()
    private val killerMoves: Array<IntArray>
    private val historyMovesSuccess = Array(2) { Array(64) { IntArray(64) } }
    private val historyMovesFail = Array(2) { Array(64) { IntArray(64) } }
    private val historyPruneMoves = Array(2) { Array(64) { IntArray(64) } }
    private val orderedMoves: Array<IntArray>
    private val searchPath: Array<SearchPath>
    private var depthZeroMoveScores: IntArray

    var okToSendInfo = false
    var engineState: SearchState
        private set
    private var quit = false
    var nodes = 0
    var millisSetByEngineMonitor: Long = 0
    private var aspirationLow = 0
    private var aspirationHigh = 0
    private var millisToThink = 0
    private var nodesToSearch = Int.MAX_VALUE
    var isAbortingSearch = true
        private set
    private var searchStartTime: Long = -1
    private var searchTargetEndTime: Long = 0
    private var searchEndTime: Long = 0
    private var finalDepthToSearch = 1
    var iterativeDeepeningDepth = 1 // current search depth for iterative deepening
    var currentDepthZeroMove = 0
    var currentDepthZeroMoveNumber = 0
    var currentPath: SearchPath
    var isUciMode = false

    var useOpeningBook = FeatureFlag.USE_INTERNAL_OPENING_BOOK.isActive
        set(useOpeningBook) {
            field = FeatureFlag.USE_INTERNAL_OPENING_BOOK.isActive && useOpeningBook
        }

    constructor(board: Board) : this(System.out, board) {}

    private fun go() {
        initSearchVariables()
        if (isBookMoveAvailable()) return
        determineDrawnPositionsAndGenerateDepthZeroMoves()

        for (depth in 1..finalDepthToSearch) {

            iterativeDeepeningDepth = depth
            if (depth > 1) okToSendInfo = true

            val path = if (FeatureFlag.USE_ASPIRATION_WINDOW.isActive)
                aspirationSearch(depth) else
                searchZero(engineBoard, depth, 0, -Int.MAX_VALUE, Int.MAX_VALUE)

            if (isAbortingSearch) break

            currentPath.setPath(Objects.requireNonNull(path)!!)
            if (path!!.score > Evaluation.MATE_SCORE_START.value) return setSearchComplete()
            reorderMoves(0)
        }
        setSearchComplete()
    }

    private fun aspirationSearch(depth: Int): SearchPath? {
        var path: SearchPath?
        path = searchZero(engineBoard, depth, 0, aspirationLow, aspirationHigh)
        if (!isAbortingSearch && Objects.requireNonNull(path)!!.score <= aspirationLow) {
            aspirationLow = -Int.MAX_VALUE
            path = searchZero(engineBoard, depth, 0, aspirationLow, aspirationHigh)
        } else if (!isAbortingSearch && path!!.score >= aspirationHigh) {
            aspirationHigh = Int.MAX_VALUE
            path = searchZero(engineBoard, depth, 0, aspirationLow, aspirationHigh)
        }
        if (!isAbortingSearch && (Objects.requireNonNull(path)!!.score <= aspirationLow || path!!.score >= aspirationHigh)) {
            path = searchZero(engineBoard, depth, 0, -Int.MAX_VALUE, Int.MAX_VALUE)
        }
        if (!isAbortingSearch) {
            currentPath.setPath(Objects.requireNonNull(path)!!)
            aspirationLow = path!!.score - SearchConfig.ASPIRATION_RADIUS.value
            aspirationHigh = path.score + SearchConfig.ASPIRATION_RADIUS.value
        }
        return path
    }

    @Throws(InvalidMoveException::class)
    fun searchZero(board: EngineBoard, depth: Int, ply: Int, low: Int, high: Int): SearchPath? {
        var low = low
        nodes += 1
        var numMoves = 0
        var flag = HashValueType.UPPER.index
        var bestMoveForHash = 0
        var newPath: SearchPath?
        val bestPath = searchPath[0]
        bestPath.reset()
        var numLegalMovesAtDepthZero = 0
        var scoutSearch = false
        var checkExtend: Int
        var pawnExtend: Int
        
        var move = moveNoScore(orderedMoves[0][numMoves])
        while (move != 0 && !isAbortingSearch) {
            if (engineBoard.makeMove(EngineMove(move))) {
                val isCheck = board.isCheck(mover)
                checkExtend = 0
                pawnExtend = 0
                if (Extensions.FRACTIONAL_EXTENSION_CHECK.value > 0 && isCheck) {
                    checkExtend = 1
                } else if (Extensions.FRACTIONAL_EXTENSION_PAWN.value > 0) {
                    if (board.wasPawnPush()) {
                        pawnExtend = 1
                    }
                }
                val newExtensions = (checkExtend * Extensions.FRACTIONAL_EXTENSION_CHECK.value + pawnExtend * Extensions.FRACTIONAL_EXTENSION_PAWN.value)
                        .coerceAtMost(Extensions.FRACTIONAL_EXTENSION_FULL.value)

                numLegalMovesAtDepthZero++
                currentDepthZeroMove = move
                currentDepthZeroMoveNumber = numLegalMovesAtDepthZero
                val boardHash = engineBoard.boardHashObject

                if (isDrawnAtRoot(0)) newPath = SearchPath().withScore(0).withPath(move) else {
                    if (scoutSearch) {
                        newPath = search(engineBoard, (depth - 1), ply + 1, -low - 1, -low, newExtensions, -1, isCheck)
                        if (newPath != null) adjustScoreForMateDepth(newPath)

                        if (!isAbortingSearch && -Objects.requireNonNull(newPath)!!.score > low) {
                            newPath = search(engineBoard, (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck). also {
                                if (it != null) adjustScoreForMateDepth(it)
                            }
                        }
                    } else {
                        newPath = search(engineBoard, (depth - 1), ply + 1, -high, -low, newExtensions, -1, isCheck)
                        if (newPath != null) adjustScoreForMateDepth(newPath)
                    }
                }

                if (!isAbortingSearch) {
                    Objects.requireNonNull(newPath)!!.score = -Objects.requireNonNull(newPath)!!.score
                    if (newPath!!.score >= high) {
                        board.unMakeMove()
                        bestPath.setPath(move, newPath)
                        boardHash.storeHashMove(move, board, newPath.score, HashValueType.LOWER.index.toByte(), depth.toInt())
                        depthZeroMoveScores[numMoves] = newPath.score
                        return bestPath
                    }
                    if (newPath.score > bestPath.score) {
                        bestPath.setPath(move, newPath)
                    }
                    if (newPath.score > low) {
                        flag = HashValueType.EXACT.index
                        bestMoveForHash = move
                        low = newPath.score
                        scoutSearch = FeatureFlag.USE_PV_SEARCH.isActive && depth + newExtensions / Extensions.FRACTIONAL_EXTENSION_FULL.value >= SearchConfig.PV_MINIMUM_DISTANCE_FROM_LEAF.value
                        currentPath.setPath(bestPath)
                    }
                    depthZeroMoveScores[numMoves] = newPath.score
                }
                engineBoard.unMakeMove()
            } else {
                depthZeroMoveScores[numMoves] = -Int.MAX_VALUE
            }
            numMoves++
            move = moveNoScore(orderedMoves[0][numMoves])
        }
        return if (!isAbortingSearch) {
            if (numLegalMovesAtDepthZero == 1 && millisToThink < Limit.MAX_SEARCH_MILLIS.value) {
                isAbortingSearch = true
                currentPath.setPath(bestPath) // otherwise we will crash!
            } else {
                val boardHash = engineBoard.boardHashObject
                boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, flag.toByte(), depth.toInt())
            }
            bestPath
        } else {
            null
        }
    }

    private fun adjustScoreForMateDepth(newPath: SearchPath) {
        newPath.score += if (newPath.score > Evaluation.MATE_SCORE_START.value) -1
        else if (newPath.score < -Evaluation.MATE_SCORE_START.value) 1
        else 0
    }

    private fun reorderMoves(ply: Int) {
        val moveCount = moveCount(orderedMoves[ply])
        for (pass in 1 until moveCount) {
            for (i in 0 until moveCount - pass) {
                if (depthZeroMoveScores[i] < depthZeroMoveScores[i + 1]) {
                    val tempScore: Int = depthZeroMoveScores[i]
                    depthZeroMoveScores[i] = depthZeroMoveScores[i + 1]
                    depthZeroMoveScores[i + 1] = tempScore
                    val tempMove: Int = orderedMoves[ply][i]
                    orderedMoves[ply][i] = orderedMoves[ply][i + 1]
                    orderedMoves[ply][i + 1] = tempMove
                }
            }
        }
    }

    private fun determineDrawnPositionsAndGenerateDepthZeroMoves() {
        moveSequence(setPlyMoves(0)).forEach {
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

    private fun setPlyMoves(ply: Int): IntArray {
        orderedMoves[ply] = boardMoves()
        return orderedMoves[ply]
    }

    private fun boardMoves() = engineBoard.moveGenerator().generateLegalMoves().getMoveArray()

    private fun isBookMoveAvailable(): Boolean {
        if (useOpeningBook) {
            val libraryMove = OpeningLibrary.getMove(fen)
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

    companion object {
        init {
            Async.init()
        }
    }

    @Throws(InvalidMoveException::class)
    private fun scoreQuiesceMoves(board: EngineBoard, ply: Int, includeChecks: Boolean) {
        var moveCount = 0
        var i = 0
        while (orderedMoves[ply][i] != 0) {
            var move = orderedMoves[ply][i]
            val isCapture = board.isCapture(move)

            // clear out additional info stored with the move
            move = moveNoScore(move)
            val score = board.getScore(move, includeChecks, isCapture, staticExchangeEvaluator)
            if (score > 0) {
                orderedMoves[ply][moveCount++] = move or (127 - score shl 24)
            }
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
        nodes += 1
        var newPath: SearchPath
        val evalScore = evaluate(board)

        searchPath[ply].height = 0
        searchPath[ply].score = if (isCheck) -Evaluation.VALUE_MATE.value else evalScore

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
                        quiescePly <= SearchConfig.GENERATE_CHECKS_UNTIL_QUIESCE_PLY.value && board.isCheck(mover))
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
            searchPath[ply].score = -Evaluation.VALUE_MATE.value
        }
        return searchPath[ply]
    }

    private fun setOrderedMovesArrayForQuiesce(isCheck: Boolean, ply: Int, board: EngineBoard, quiescePly: Int) {
        if (isCheck) {
            orderedMoves[ply] = board.moveGenerator()
                    .generateLegalMoves()
                    .getMoveArray()
            scoreFullWidthMoves(board, ply)
        } else {
            orderedMoves[ply] = board.moveGenerator()
                    .generateLegalQuiesceMoves(quiescePly <= SearchConfig.GENERATE_CHECKS_UNTIL_QUIESCE_PLY.value)
                    .getMoveArray()
            scoreQuiesceMoves(board, ply, quiescePly <= SearchConfig.GENERATE_CHECKS_UNTIL_QUIESCE_PLY.value)
        }
    }

    private fun adjustedSee(see: Int) =
            if (see > -Int.MAX_VALUE) (see.toDouble() / pieceValue(Piece.QUEEN) * 10).toInt() else see

    @Throws(InvalidMoveException::class)
    private fun scoreFullWidthCaptures(board: EngineBoard, ply: Int): Int {
        var movesScored = 0
        var i = -1
        while (orderedMoves[ply][++i] != 0) {
            if (orderedMoves[ply][i] != -1 && scoreMove(ply, i, board) > 0) {
                movesScored++
            }
        }
        return movesScored
    }

    private fun scoreMove(ply: Int, i: Int, board: EngineBoard): Int {
        var score = 0
        val toSquare = orderedMoves[ply][i] and 63
        val isCapture = board.getSquareOccupant(toSquare) != SquareOccupant.NONE ||
                (1L shl toSquare and board.getBitboard(BitboardType.ENPASSANTSQUARE) != 0L &&
                        board.getSquareOccupant(orderedMoves[ply][i] ushr 16 and 63).piece == Piece.PAWN)

        orderedMoves[ply][i] = moveNoScore(orderedMoves[ply][i])
        if (orderedMoves[ply][i] == mateKiller[ply]) {
            score = 126
        } else if (isCapture) {

            val see = adjustedSee(staticExchangeEvaluator.staticExchangeEvaluation(board, EngineMove(orderedMoves[ply][i])))

            score = if (see > 0) {
                110 + see
            } else if (orderedMoves[ply][i] and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value ==
                    PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
                109
            } else if (see == 0) {
                107
            } else {
                scoreLosingCapturesWithWinningHistory(board, ply, i, score, orderedMoves[ply], toSquare)
            }
        } else if (orderedMoves[ply][i] and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value ==
                PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN.value) {
            score = 108
        }
        orderedMoves[ply][i] = orderedMoves[ply][i] or (127 - score shl 24)
        return score
    }

    private fun scoreLosingCapturesWithWinningHistory(board: EngineBoard, ply: Int, i: Int, score: Int, movesForSorting: IntArray, toSquare: Int): Int {
        val historyScore = historyScore(board.mover == Colour.WHITE, movesForSorting[i] ushr 16 and 63, toSquare)
        if (historyScore > 5) {
            return historyScore
        } else {
            for (j in 0 until SearchConfig.NUM_KILLER_MOVES.value) {
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
                val fromSquare = orderedMoves[ply][i] ushr 16 and 63
                val toSquare = orderedMoves[ply][i] and 63
                orderedMoves[ply][i] = moveNoScore(orderedMoves[ply][i])

                val killerScore = scoreKillerMoves(ply, i, orderedMoves[ply])
                val historyScore = scoreHistoryHeuristic(board, killerScore, fromSquare, toSquare)

                val finalScore = if (historyScore == 0)
                    (if (board.getSquareOccupant(toSquare) != SquareOccupant.NONE) 1 // losing capture
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
        when (board.getSquareOccupant(fromSquare).piece) {
            Piece.PAWN -> linearScale(
                    if (board.mover == Colour.WHITE) board.blackPieceValues else board.whitePieceValues,
                    Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
                    Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
                    PieceSquareTables.pawnEndGame[toSquare] - PieceSquareTables.pawnEndGame[fromSquare],
                    PieceSquareTables.pawn[toSquare] - PieceSquareTables.pawn[fromSquare])
            Piece.KNIGHT -> linearScale(
                    if (board.mover == Colour.WHITE) board.blackPieceValues + board.blackPawnValues else board.whitePieceValues + board.whitePawnValues,
                    Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                    Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                    PieceSquareTables.knightEndGame[toSquare] - PieceSquareTables.knightEndGame[fromSquare],
                    PieceSquareTables.knight[toSquare] - PieceSquareTables.knight[fromSquare])
            Piece.BISHOP -> PieceSquareTables.bishop[toSquare] - PieceSquareTables.bishop[fromSquare]
            Piece.ROOK -> PieceSquareTables.rook[toSquare] - PieceSquareTables.rook[fromSquare]
            Piece.QUEEN -> PieceSquareTables.queen[toSquare] - PieceSquareTables.queen[fromSquare]
            Piece.KING -> linearScale(
                    if (board.mover == Colour.WHITE) board.blackPieceValues else board.whitePieceValues,
                    pieceValue(Piece.ROOK),
                    Evaluation.OPENING_PHASE_MATERIAL.value,
                    PieceSquareTables.kingEndGame[toSquare] - PieceSquareTables.kingEndGame[fromSquare],
                    PieceSquareTables.king[toSquare] - PieceSquareTables.king[fromSquare])
            else -> 0
        }

    private fun scoreHistoryHeuristic(board: EngineBoard, score: Int, fromSquare: Int, toSquare: Int) =
        if (score == 0 && FeatureFlag.USE_HISTORY_HEURISTIC.isActive &&
                historyMovesSuccess[if (board.mover == Colour.WHITE) 0 else 1][fromSquare][toSquare] > 0) {
            90 + historyScore(board.mover == Colour.WHITE, fromSquare, toSquare)
        } else score

    private fun scoreKillerMoves(ply: Int, i: Int, movesForSorting: IntArray): Int {
        for (j in 0 until SearchConfig.NUM_KILLER_MOVES.value) {
            if (movesForSorting[i] == killerMoves[ply][j]) {
                return 106 - j
            }
        }
        return 0
    }

    @Throws(InvalidMoveException::class)
    fun search(
            board: EngineBoard,
            depth: Int,
            ply: Int,
            low: Int,
            high: Int,
            extensions: Int,
            recaptureSquare: Int,
            isCheck: Boolean): SearchPath? {
        var low = low
        var high = high
        var isCheck = isCheck
        nodes++
        if (millisSetByEngineMonitor > searchTargetEndTime || nodes >= nodesToSearch) {
            isAbortingSearch = true
            okToSendInfo = false
            return null
        }
        var newPath: SearchPath?
        var bestPath = searchPath[ply]
        bestPath.reset()
        if (board.previousOccurrencesOfThisPosition() == 2 || board.halfMoveCount >= 100) {
            return bestPath.withScore(Evaluation.DRAW_CONTEMPT.value)
        }
        if (board.onlyKingsRemain()) {
            return bestPath.withScore(0)
        }
        val depthRemaining = depth + extensions / Extensions.FRACTIONAL_EXTENSION_FULL.value
        var flag = HashValueType.UPPER.index
        val boardHash = board.boardHashObject
        val hashIndex = boardHash.getHashIndex(board)
        var hashMove = 0
        if (FeatureFlag.USE_HASH_TABLES.isActive) {
            if (FeatureFlag.USE_HEIGHT_REPLACE_HASH.isActive && isHeightHashTableEntryValid(depthRemaining, board)) {
                boardHash.setHashTableUseHeight(hashIndex + HashIndex.VERSION.index, boardHash.hashTableVersion)
                hashMove = boardHash.useHeight(hashIndex + HashIndex.MOVE.index)
                if (boardHash.useHeight(hashIndex + HashIndex.FLAG.index) == HashValueType.LOWER.index) {
                    if (boardHash.useHeight(hashIndex + HashIndex.SCORE.index) > low) low = boardHash.useHeight(hashIndex + HashIndex.SCORE.index)
                } else if (boardHash.useHeight(hashIndex + HashIndex.FLAG.index) == HashValueType.UPPER.index &&
                        boardHash.useHeight(hashIndex + HashIndex.SCORE.index) < high) high = boardHash.useHeight(hashIndex + HashIndex.SCORE.index)
                if (boardHash.useHeight(hashIndex + HashIndex.FLAG.index) == HashValueType.EXACT.index || low >= high) {
                    return bestPath.withScore(boardHash.useHeight(hashIndex + HashIndex.SCORE.index)).withPath(hashMove)
                }
            }
            if (FeatureFlag.USE_ALWAYS_REPLACE_HASH.isActive && hashMove == 0 && isAlwaysReplaceHashTableEntryValid(depthRemaining, board)) {
                hashMove = boardHash.ignoreHeight(hashIndex + HashIndex.MOVE.index)
                if (boardHash.ignoreHeight(hashIndex + HashIndex.FLAG.index) == HashValueType.LOWER.index) {
                    if (boardHash.ignoreHeight(hashIndex + HashIndex.SCORE.index) > low) low = boardHash.ignoreHeight(hashIndex + HashIndex.SCORE.index)
                } else if (boardHash.ignoreHeight(hashIndex + HashIndex.FLAG.index) == HashValueType.UPPER.index &&
                        boardHash.ignoreHeight(hashIndex + HashIndex.SCORE.index) < high) high = boardHash.ignoreHeight(hashIndex + HashIndex.SCORE.index)
                if (boardHash.ignoreHeight(hashIndex + HashIndex.FLAG.index) == HashValueType.EXACT.index || low >= high) {
                    return bestPath.withScore(boardHash.ignoreHeight(hashIndex + HashIndex.SCORE.index)).withPath(hashMove)
                }
            }
        }
        var checkExtend = 0
        if (extensions / Extensions.FRACTIONAL_EXTENSION_FULL.value < Limit.MAX_EXTENSION_DEPTH.value) {
            if (Extensions.FRACTIONAL_EXTENSION_CHECK.value > 0 && isCheck) {
                checkExtend = 1
            }
        }
        if (depthRemaining <= 0) {
            bestPath = quiesce(board, Limit.MAX_QUIESCE_DEPTH.value - 1, ply, 0, low, high, isCheck)
            flag = if (bestPath.score < low) HashValueType.UPPER.index else if (bestPath.score > high) HashValueType.LOWER.index else HashValueType.EXACT.index
            boardHash.storeHashMove(0, board, bestPath.score, flag.toByte(), 0)
            return bestPath
        }
        if (FeatureFlag.USE_INTERNAL_ITERATIVE_DEEPENING.isActive
                && depthRemaining >= IterativeDeepening.IID_MIN_DEPTH.value && hashMove == 0 && !board.isOnNullMove) {
            if (depth - IterativeDeepening.IID_REDUCE_DEPTH.value > 0) {
                newPath = search(board, (depth - IterativeDeepening.IID_REDUCE_DEPTH.value), ply, low, high, extensions, recaptureSquare, isCheck)
                // it's not really a hash move, but this will cause the order routine to rank it first
                if (newPath != null && newPath.height > 0) hashMove = newPath.move[0]
            }
            bestPath.reset()
            // We reset this here because it may have been mucked about with during IID
            // Notice that the search calls with ply, not ply+1, because search is for this level (we haven't made a move)
        }
        var bestMoveForHash = 0
        var scoutSearch = false
        val verifyingNullMoveDepthReduction: Byte = 0
        var threatExtend = 0
        var pawnExtend = 0
        val nullMoveReduceDepth = if (depthRemaining > SearchConfig.NULLMOVE_DEPTH_REMAINING_FOR_RD_INCREASE.value) SearchConfig.NULLMOVE_REDUCE_DEPTH.value + 1 else SearchConfig.NULLMOVE_REDUCE_DEPTH.value
        if (FeatureFlag.USE_NULL_MOVE_PRUNING.isActive && !isCheck && !board.isOnNullMove && depthRemaining > 1) {
            if ((if (board.mover == Colour.WHITE) board.whitePieceValues else board.blackPieceValues) >= SearchConfig.NULLMOVE_MINIMUM_FRIENDLY_PIECEVALUES.value &&
                    (if (board.mover == Colour.WHITE) board.whitePawnValues else board.blackPawnValues) > 0) {
                board.makeNullMove()
                newPath = search(board, (depth - nullMoveReduceDepth - 1), ply + 1, -high, -low, extensions, -1, false)
                if (newPath != null) if (newPath.score > Evaluation.MATE_SCORE_START.value) newPath.score-- else if (newPath.score < -Evaluation.MATE_SCORE_START.value) newPath.score++
                if (!isAbortingSearch) {
                    if (-Objects.requireNonNull(newPath)!!.score >= high) {
                        bestPath.score = -newPath!!.score
                        board.unMakeNullMove()
                        return bestPath
                    } else if (Extensions.FRACTIONAL_EXTENSION_THREAT.value > 0 && -newPath!!.score < -Evaluation.MATE_SCORE_START.value && extensions / Extensions.FRACTIONAL_EXTENSION_FULL.value < Limit.MAX_EXTENSION_DEPTH.value) {
                        threatExtend = 1
                    } else {
                        threatExtend = 0
                    }
                }
                board.unMakeNullMove()
            }
        }
        orderedMoves[ply] = board.moveGenerator().generateLegalMoves().getMoveArray()
        moveOrderStatus[ply] = MoveOrder.NONE
        var research: Boolean
        do {
            research = false
            var legalMoveCount = 0
            var futilityPruningEvaluation: Int
            val wasCheckBeforeMove = board.isCheck(mover)

            // Check to see if we can futility prune this whole node
            var canFutilityPrune = false
            var futilityScore = low
            if (FeatureFlag.USE_FUTILITY_PRUNING.isActive && depthRemaining < 4 && !wasCheckBeforeMove && threatExtend == 0 && Math.abs(low) < Evaluation.MATE_SCORE_START.value && Math.abs(high) < Evaluation.MATE_SCORE_START.value) {
                futilityPruningEvaluation = evaluate(board)
                futilityScore = futilityPruningEvaluation + SearchConfig.getFutilityMargin(depthRemaining - 1)
                if (futilityScore < low) canFutilityPrune = true
            }
            var lateMoveReductionsMade = 0
            var newExtensions = 0
            var reductions = 0
            var move: Int
            while (getHighScoreMove(board, ply, hashMove).also { move = it } != 0 && !isAbortingSearch) {
                val targetPiece = board.getSquareOccupant(move and 63).index
                val movePiece = board.getSquareOccupant(move ushr 16 and 63).index
                var recaptureExtend = 0
                var newRecaptureSquare = -1
                var currentSEEValue = -Int.MAX_VALUE
                if (Extensions.FRACTIONAL_EXTENSION_RECAPTURE.value > 0 && extensions / Extensions.FRACTIONAL_EXTENSION_FULL.value
                        < Limit.MAX_EXTENSION_DEPTH.value) {
                    recaptureExtend = 0
                    if (targetPiece != -1 && Evaluation.pieceValues[movePiece] == Evaluation.pieceValues[targetPiece]) {
                        currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, EngineMove(move))
                        if (Math.abs(currentSEEValue) <= Extensions.RECAPTURE_EXTENSION_MARGIN.value) newRecaptureSquare = move and 63
                    }
                    if (move and 63 == recaptureSquare) {
                        if (currentSEEValue == -Int.MAX_VALUE) currentSEEValue = staticExchangeEvaluator.staticExchangeEvaluation(board, EngineMove(move))
                        if (Math.abs(currentSEEValue) > Evaluation.getPieceValue(board.getSquareOccupant(recaptureSquare))
                                - Extensions.RECAPTURE_EXTENSION_MARGIN.value) {
                            recaptureExtend = 1
                        }
                    }
                }
                if (board.makeMove(EngineMove(move))) {
                    legalMoveCount++
                    isCheck = board.isCheck(mover)
                    if (FeatureFlag.USE_FUTILITY_PRUNING.isActive && canFutilityPrune && !isCheck && board.wasCapture() && !board.wasPawnPush()) {
                        newPath = searchPath[ply + 1]
                        newPath.height = 0
                        newPath.score = -futilityScore // newPath.score gets reversed later
                    } else {
                        if (extensions / Extensions.FRACTIONAL_EXTENSION_FULL.value
                                < Limit.MAX_EXTENSION_DEPTH.value) {
                            pawnExtend = 0
                            if (Extensions.FRACTIONAL_EXTENSION_PAWN.value > 0) {
                                if (board.wasPawnPush()) {
                                    pawnExtend = 1
                                }
                            }
                        }
                        val partOfTree = ply / iterativeDeepeningDepth
                        val maxNewExtensionsInThisPart = Extensions.maxNewExtensionsTreePart[Math.min(partOfTree, Extensions.LAST_EXTENSION_LAYER.value)]
                        newExtensions = extensions +
                                Math.min(
                                        checkExtend * Extensions.FRACTIONAL_EXTENSION_CHECK.value +
                                                threatExtend * Extensions.FRACTIONAL_EXTENSION_THREAT.value +
                                                recaptureExtend * Extensions.FRACTIONAL_EXTENSION_RECAPTURE.value +
                                                pawnExtend * Extensions.FRACTIONAL_EXTENSION_PAWN.value,
                                        maxNewExtensionsInThisPart)
                        var lateMoveReduction = 0
                        if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive && newExtensions == 0 && legalMoveCount > LateMoveReductions.LMR_LEGALMOVES_BEFORE_ATTEMPT.value && depthRemaining - verifyingNullMoveDepthReduction > 1 && move != hashMove &&
                                !wasCheckBeforeMove && (historyPruneMoves[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63]
                                        <= LateMoveReductions.LMR_THRESHOLD.value) &&
                                board.wasCapture() &&
                                (Extensions.FRACTIONAL_EXTENSION_PAWN.value > 0 || !board.wasPawnPush())) {
                            if (-evaluate(board) <= low + LateMoveReductions.LMR_CUT_MARGIN.value) {
                                lateMoveReduction = 1
                                historyPruneMoves[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63] = LateMoveReductions.LMR_REPLACE_VALUE_AFTER_CUT.value
                            }
                        }
                        if (LateMoveReductions.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION.value > -1) {
                            lateMoveReductionsMade += lateMoveReduction
                            if (lateMoveReductionsMade > LateMoveReductions.NUM_LMR_FINDS_BEFORE_EXTRA_REDUCTION.value && depthRemaining > 3) {
                                lateMoveReduction = 2
                            }
                        }
                        reductions = verifyingNullMoveDepthReduction + lateMoveReduction
                        var lmrResearch: Boolean
                        do {
                            lmrResearch = false
                            if (scoutSearch) {
                                newPath = search(engineBoard, (depth - 1).toByte() - reductions, ply + 1, -low - 1, -low, newExtensions, newRecaptureSquare, isCheck)
                                if (newPath != null) if (newPath.score > Evaluation.MATE_SCORE_START.value) newPath.score-- else if (newPath.score < -Evaluation.MATE_SCORE_START.value) newPath.score++
                                if (!isAbortingSearch && -Objects.requireNonNull(newPath)!!.score > low) {
                                    // research with normal window
                                    newPath = search(engineBoard, (depth - 1).toByte() - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck)
                                    if (newPath != null) if (newPath.score > Evaluation.MATE_SCORE_START.value) newPath.score-- else if (newPath.score < -Evaluation.MATE_SCORE_START.value) newPath.score++
                                }
                            } else {
                                newPath = search(board, (depth - 1).toByte() - reductions, ply + 1, -high, -low, newExtensions, newRecaptureSquare, isCheck)
                                if (newPath != null) if (newPath.score > Evaluation.MATE_SCORE_START.value) newPath.score-- else if (newPath.score < -Evaluation.MATE_SCORE_START.value) newPath.score++
                            }
                            if (!isAbortingSearch && lateMoveReduction > 0 && -Objects.requireNonNull(newPath)!!.score >= low) {
                                lmrResearch = FeatureFlag.LMR_RESEARCH_ON_FAIL_HIGH.isActive
                                lateMoveReduction = 0
                            }
                        } while (lmrResearch)
                    }
                    if (!isAbortingSearch) {
                        Objects.requireNonNull(newPath)!!.score = -newPath!!.score
                        if (newPath.score >= high) {
                            if (FeatureFlag.USE_HISTORY_HEURISTIC.isActive) {
                                historyMovesSuccess[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63] += depthRemaining
                                if (historyMovesSuccess[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63]
                                        > SearchConfig.HISTORY_MAX_VALUE.value) {
                                    for (i in 0..1) for (j in 0..63) for (k in 0..63) {
                                        if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2
                                        if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2
                                    }
                                }
                            }
                            if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive) historyPruneMoves[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63] += LateMoveReductions.LMR_ABOVE_ALPHA_ADDITION.value
                            board.unMakeMove()
                            bestPath.setPath(move, newPath)
                            boardHash.storeHashMove(move, board, newPath.score, HashValueType.LOWER.index.toByte(), depthRemaining)
                            if (SearchConfig.NUM_KILLER_MOVES.value > 0) {
                                if (board.getBitboard(BitboardType.ENEMY) and (move and 63).toLong() == 0L || move and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value == 0) {
                                    // if this move is in second place, or if it's not in the table at all,
                                    // then move first to second, and replace first with this move
                                    if (killerMoves[ply][0] != move) {
                                        System.arraycopy(killerMoves[ply], 0, killerMoves[ply], 1, SearchConfig.NUM_KILLER_MOVES.value - 1)
                                        killerMoves[ply][0] = move
                                    }
                                    if (FeatureFlag.USE_MATE_HISTORY_KILLERS.isActive && newPath.score > Evaluation.MATE_SCORE_START.value) {
                                        mateKiller[ply] = move
                                    }
                                }
                            }
                            return bestPath
                        }
                        if (FeatureFlag.USE_HISTORY_HEURISTIC.isActive) {
                            historyMovesFail[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63] += depthRemaining
                            if (historyMovesFail[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63]
                                    > SearchConfig.HISTORY_MAX_VALUE.value) {
                                for (i in 0..1) for (j in 0..63) for (k in 0..63) {
                                    if (historyMovesSuccess[i][j][k] > 0) historyMovesSuccess[i][j][k] /= 2
                                    if (historyMovesFail[i][j][k] > 0) historyMovesFail[i][j][k] /= 2
                                }
                            }
                        }
                        if (newPath.score > bestPath.score) {
                            bestPath.setPath(move, newPath)
                        }
                        if (newPath.score > low) {
                            flag = HashValueType.EXACT.index
                            bestMoveForHash = move
                            low = newPath.score
                            scoutSearch = FeatureFlag.USE_PV_SEARCH.isActive && depth - reductions +
                                    (newExtensions / Extensions.FRACTIONAL_EXTENSION_FULL.value) >=
                                    SearchConfig.PV_MINIMUM_DISTANCE_FROM_LEAF.value
                            if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive) historyPruneMoves[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63] += LateMoveReductions.LMR_ABOVE_ALPHA_ADDITION.value
                        } else if (FeatureFlag.USE_LATE_MOVE_REDUCTIONS.isActive) {
                            historyPruneMoves[if (board.mover == Colour.WHITE) 1 else 0][move ushr 16 and 63][move and 63] -= LateMoveReductions.LMR_NOT_ABOVE_ALPHA_REDUCTION.value
                        }
                    }
                    board.unMakeMove()
                }
            }
            if (!isAbortingSearch) {
                if (legalMoveCount == 0) {
                    bestPath.score = if (board.isCheck(mover)) -Evaluation.VALUE_MATE.value else 0
                    boardHash.storeHashMove(0, board, bestPath.score, HashValueType.EXACT.index.toByte(), Limit.MAX_SEARCH_DEPTH.value)
                    return bestPath
                }
                if (!research) {
                    boardHash.storeHashMove(bestMoveForHash, board, bestPath.score, flag.toByte(), depthRemaining)
                    return bestPath
                }
            }
        } while (research)
        return null
    }

    val mover: Colour
        get() = engineBoard.mover

    @Throws(InvalidMoveException::class)
    fun makeMove(engineMove: EngineMove) {
        engineBoard.makeMove(engineMove)
    }

    private fun initSearchVariables() {
        engineState = SearchState.SEARCHING
        isAbortingSearch = false
        val boardHash = engineBoard.boardHashObject
        boardHash.incVersion()
        searchStartTime = System.currentTimeMillis()
        searchEndTime = 0
        searchTargetEndTime = searchStartTime + millisToThink - Uci.UCI_TIMER_INTERVAL_MILLIS.value
        aspirationLow = -Int.MAX_VALUE
        aspirationHigh = Int.MAX_VALUE
        nodes = 0
        setupMateAndKillerMoveTables()
        setupHistoryMoveTable()
    }

    private fun setupMateAndKillerMoveTables() {
        for (i in 0 until Limit.MAX_TREE_DEPTH.value) {
            mateKiller.add(-1)
            for (j in 0 until SearchConfig.NUM_KILLER_MOVES.value) {
                killerMoves[i][j] = -1
            }
        }
    }

    private fun setupHistoryMoveTable() {
        for (i in 0..63) for (j in 0..63) {
            historyMovesSuccess[0][i][j] = 0
            historyMovesSuccess[1][i][j] = 0
            historyMovesFail[0][i][j] = 0
            historyMovesFail[1][i][j] = 0
            historyPruneMoves[0][i][j] = LateMoveReductions.LMR_INITIAL_VALUE.value
            historyPruneMoves[1][i][j] = LateMoveReductions.LMR_INITIAL_VALUE.value
        }
    }

    fun setMillisToThink(millisToThink: Int) {
        this.millisToThink = millisToThink
        if (this.millisToThink < Limit.MIN_SEARCH_MILLIS.value) {
            this.millisToThink = Limit.MIN_SEARCH_MILLIS.value
        }
    }

    fun setNodesToSearch(nodesToSearch: Int) {
        this.nodesToSearch = nodesToSearch
    }

    fun setSearchDepth(searchDepth: Int) {
        finalDepthToSearch = if (searchDepth < 0) 1 else searchDepth
    }

    @get:Synchronized
    val isSearching: Boolean
        get() = engineState === SearchState.SEARCHING || engineState === SearchState.REQUESTED

    val currentScoreHuman: String
        get() {
            val score = currentPath.score
            val abs = Math.abs(score)
            if (abs > Evaluation.MATE_SCORE_START.value) {
                val mateIn = (Evaluation.VALUE_MATE.value - abs + 1) / 2
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
            return if (timePassed == 0L) {
                0
            } else {
                (nodes.toDouble() / timePassed.toDouble() * 1000.0).toInt()
            }
        }

    private fun isDrawnAtRoot(ply: Int): Boolean {
        var i: Int
        val boardHash = engineBoard.boardHashObject
        i = 0
        while (i < drawnPositionsAtRootCount[ply]) {
            if (drawnPositionsAtRoot[ply][i] == boardHash.trackedHashValue) {
                return true
            }
            i++
        }
        return false
    }

    val currentMove: Int
        get() = currentPath.move[0]

    @Synchronized
    fun startSearch() {
        engineState = SearchState.REQUESTED
    }

    @Synchronized
    fun stopSearch() {
        isAbortingSearch = true
    }

    @Synchronized
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
                okToSendInfo = false
                if (isUciMode) {
                    val s1 = "info" +
                            " currmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(currentDepthZeroMove) +
                            " currmovenumber " + currentDepthZeroMoveNumber +
                            " depth " + iterativeDeepeningDepth +
                            " score " + currentScoreHuman +
                            " pv " + currentPath.toString() +
                            " time " + searchDuration +
                            " nodes " + nodes +
                            " nps " + nodesPerSecond
                    val s2 = "bestmove " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(currentMove)
                    printStream.println(s1)
                    printStream.println(s2)
                }
            }
        }
    }

    val fen: String
        get() = engineBoard.getFen()

    init {
        engineBoard.setBoard(board)
        drawnPositionsAtRoot = ArrayList()
        drawnPositionsAtRoot.add(ArrayList())
        drawnPositionsAtRoot.add(ArrayList())
        this.printStream = printStream
        millisSetByEngineMonitor = System.currentTimeMillis()
        currentPath = SearchPath()
        engineState = SearchState.READY
        searchPath = Array(Limit.MAX_TREE_DEPTH.value) { SearchPath() }
        killerMoves = Array(Limit.MAX_TREE_DEPTH.value) { IntArray(SearchConfig.NUM_KILLER_MOVES.value) }
        for (i in 0 until Limit.MAX_TREE_DEPTH.value) {
            killerMoves[i] = IntArray(SearchConfig.NUM_KILLER_MOVES.value)
        }
        orderedMoves = Array(Limit.MAX_TREE_DEPTH.value) { IntArray(Limit.MAX_LEGAL_MOVES.value) }
        depthZeroMoveScores = IntArray(Limit.MAX_LEGAL_MOVES.value)
    }
}