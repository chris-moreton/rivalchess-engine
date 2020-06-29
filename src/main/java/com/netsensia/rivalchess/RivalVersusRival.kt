package com.netsensia.rivalchess

import com.netsensia.rivalchess.config.MAX_SEARCH_DEPTH
import com.netsensia.rivalchess.config.MAX_SEARCH_MILLIS
import com.netsensia.rivalchess.consts.FEN_START_POS
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

val secureRandom = SecureRandom()

fun main(args: Array<String>) {
    var results= intArrayOf(0,0,0,0,0)
    for (i in 1..100) {
        results[game()] ++
        println(Arrays.toString(results))
    }
}

fun game(): Int {

    val moveList = mutableListOf<Int>()
    var board = Board.fromFen(FEN_START_POS)

    var moveCount = 0
    while (board.getLegalMoves().isNotEmpty()) {
        val searcher = Search(Board.fromFen(FEN_START_POS))
        moveList.forEach {
            searcher.makeMove(it)
        }
        if (searcher.engineBoard.halfMoveCount > 50) return FIFTY_MOVE;
        if (searcher.engineBoard.previousOccurrencesOfThisPosition() > 2) return THREE_FOLD
        searcher.clearHash()
        searcher.useOpeningBook = true
        searcher.setMillisToThink(MAX_SEARCH_MILLIS)
        searcher.setNodesToSearch(50000 + secureRandom.nextInt(50000))
        searcher.setSearchDepth(MAX_SEARCH_DEPTH)
        searcher.go()
        moveList.add(searcher.currentMove)
        board = Board.fromMove(board, getMoveRefFromCompactMove(searcher.currentMove))
        moveCount ++

    }
    println(board.getFen())
    if (board.isCheck()) {
        return if (board.sideToMove == Colour.WHITE) BLACK_WIN else WHITE_WIN
    }
    return STALEMATE
}
