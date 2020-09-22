package com.netsensia.rivalchess.eve

import com.netsensia.rivalchess.config.MAX_SEARCH_DEPTH
import com.netsensia.rivalchess.config.MAX_SEARCH_MILLIS
import com.netsensia.rivalchess.consts.FEN_START_POS
import com.netsensia.rivalchess.engine.eval.pieceValues
import com.netsensia.rivalchess.engine.search.Search
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.util.BoardUtils.getLegalMoves
import com.netsensia.rivalchess.model.util.BoardUtils.isCheck
import com.netsensia.rivalchess.model.util.FenUtils.getFen
import com.netsensia.rivalchess.util.getMoveRefFromCompactMove
import java.security.SecureRandom
import java.util.*

const val WHITE_WIN = 0
const val BLACK_WIN = 1
const val THREE_FOLD = 2
const val FIFTY_MOVE = 3
const val STALEMATE = 4
const val CHAMPION_WIN = 5
const val CHALLENGER_WIN = 6
const val GAME_TOO_LONG = 7

val secureRandom = SecureRandom()

@kotlin.ExperimentalUnsignedTypes
fun main() {
    val results= intArrayOf(0,0,0,0,0,0,0)
    for (i in 1..10000) {
        val result = game(i)
        results[result] ++
        if (result == WHITE_WIN) results[if (i % 2 == 0) CHAMPION_WIN else CHALLENGER_WIN] ++
        if (result == BLACK_WIN) results[if (i % 2 == 0) CHALLENGER_WIN else CHAMPION_WIN] ++
        println(Arrays.toString(results))
    }
}

@kotlin.ExperimentalUnsignedTypes
fun game(gameNumber: Int): Int {

    val moveList = mutableListOf<Int>()
    var board = Board.fromFen(FEN_START_POS)

    var moveNumber = 0
    while (board.getLegalMoves().isNotEmpty()) {
        val searcher = getSearcher(gameNumber, moveNumber)
        moveList.forEach { searcher.makeMove(it) }
        if (searcher.engineBoard.halfMoveCount > 50) return result(board, FIFTY_MOVE, gameNumber)
        if (searcher.engineBoard.previousOccurrencesOfThisPosition() > 2) return result(board, THREE_FOLD, gameNumber)
        searcher.go()
        moveList.add(searcher.currentMove)
        board = Board.fromMove(board, getMoveRefFromCompactMove(searcher.currentMove))
        moveNumber ++
    }
    if (board.isCheck()) return result(board, if (board.sideToMove == Colour.WHITE) BLACK_WIN else WHITE_WIN, gameNumber)

    return result(board, STALEMATE, gameNumber)
}

@kotlin.ExperimentalUnsignedTypes
fun getSearcher(gameNumber: Int, moveNumber: Int): Search {
    val searcher = Search(Board.fromFen(FEN_START_POS))
    searcher.useOpeningBook = true
    searcher.setMillisToThink(MAX_SEARCH_MILLIS)
    searcher.setSearchDepth(MAX_SEARCH_DEPTH)
    val isChampionsMove = (gameNumber % 2 == moveNumber % 2)
    searcher.setNodesToSearch(10000 + secureRandom.nextInt(5000))
    if (isChampionsMove) {
        pieceValues = intArrayOf(100,550,600,1000,2000,30000)
    } else {
        pieceValues = intArrayOf(100,550,600,1000,2000,30000)
    }

    return searcher
}

fun result(board: Board, result: Int, gameNumber: Int): Int {
    when (result) {
        WHITE_WIN -> println((if (gameNumber % 2 == 0) "Champion" else "Challenger") + " wins as white")
        BLACK_WIN -> println((if (gameNumber % 2 == 0) "Challenger" else "Champion") + " wins as black")
        FIFTY_MOVE -> println("Fifty move rule")
        THREE_FOLD -> println("Three fold repetition")
        STALEMATE -> println("Stalemate")
    }
    println(board.getFen())

    return result
}
