package com.netsensia.rivalchess.eve

import com.netsensia.rivalchess.config.MAX_SEARCH_MILLIS
import com.netsensia.rivalchess.consts.FEN_START_POS
import com.netsensia.rivalchess.engine.eval.pieceValues
import com.netsensia.rivalchess.engine.search.Search
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.util.BoardUtils.getLegalMoves
import com.netsensia.rivalchess.model.util.BoardUtils.isCheck
import com.netsensia.rivalchess.util.getMoveRefFromCompactMove

fun main(args: Array<String>) {
    val learningLeague = LearningLeague()
    learningLeague.go()
}

class Player(var pieceValues: IntArray, var wins: Int, var played: Int) {

    override fun toString(): String {
        return pieceValues.take(5).joinToString(" ", transform = { it.toString().padStart(6)} )
    }
}

class LearningLeague {

    private val numPlayers = 3
    private var generation = 0
    private val numGenerations = 2000
    private val nextGenRepresentation = intArrayOf(2,1,0)

    var players: MutableList<Player> = mutableListOf()

    fun go() {
        createGenerationZero()
        for (generation in 0 until numGenerations) {
            roundRobin()
            displayResults(players.sortedBy { it.wins })
            createNewGeneration()
        }
    }

    private fun createNewGeneration() {
        val sortedPlayers = players.sortedBy { it.wins }
        players = mutableListOf()
        for (i in 0 until numPlayers) {
            for (j in 0 until nextGenRepresentation[i]) {
                players.add(Player(sortedPlayers[i].pieceValues.copyOf(), 0, 0))
                for (k in 1 until 5) {
                    if (secureRandom.nextInt(5) == 0) {
                        val percentage = secureRandom.nextInt(15) / 100.0
                        var adjustment = (sortedPlayers[i].pieceValues[k]).toDouble() * percentage
                        if (secureRandom.nextInt(2) == 0) adjustment = -adjustment
                        players[j].pieceValues[k] += adjustment.toInt()
                    }
                }
            }
        }
        println("".padStart(50, '='))
        println("The Next Generation")
        println("".padStart(50, '='))
        players.forEach { println(it) }
    }

    private fun roundRobin() {
        println()
        for (white in 0 until numPlayers) {
            for (black in 0 until numPlayers) {
                print(".")
                val result = playGame(players.get(white), players.get(black))
                when (result) {
                    WHITE_WIN -> players.get(white).wins++
                    BLACK_WIN -> players.get(black).wins++
                }
                players.get(white).played++
                players.get(black).played++
            }
        }
        println()
    }

    private fun displayResults(players: List<Player>) {
        println("".padStart(50, '='))
        println("Generation $generation Results")
        println("".padStart(50, '='))
        players.forEach { println(it) }
    }

    private fun createGenerationZero() {
        for (i in 0..numPlayers-1) {
            players.add(Player(intArrayOf(
                    100,
                    100 + secureRandom.nextInt(1500),
                    100 + secureRandom.nextInt(1500),
                    100 + secureRandom.nextInt(1500),
                    100 + secureRandom.nextInt(1500),
                    30000
            ), 0, 0))
        }
    }

    private fun playGame(whitePlayer: Player, blackPlayer: Player): Int {

        val moveList = mutableListOf<Int>()
        var board = Board.fromFen(FEN_START_POS)

        var moveNumber = 1
        while (board.getLegalMoves().isNotEmpty()) {
            val searcher = getSearcher(if (moveNumber % 2 == 1) whitePlayer else blackPlayer)
            moveList.forEach { searcher.makeMove(it) }
            if (searcher.engineBoard.halfMoveCount > 50) return FIFTY_MOVE
            if (searcher.engineBoard.previousOccurrencesOfThisPosition() > 2) return THREE_FOLD
            searcher.go()
            moveList.add(searcher.currentMove)
            board = Board.fromMove(board, getMoveRefFromCompactMove(searcher.currentMove))
            moveNumber ++
        }
        if (board.isCheck()) return if (board.sideToMove == Colour.WHITE) BLACK_WIN else WHITE_WIN

        return STALEMATE
    }

    private fun getSearcher(player: Player): Search {
        val searcher = Search(Board.fromFen(FEN_START_POS))
        searcher.useOpeningBook = true
        searcher.setMillisToThink(MAX_SEARCH_MILLIS)
        searcher.setSearchDepth(2)
        searcher.setNodesToSearch(Int.MAX_VALUE)
        pieceValues = player.pieceValues

        return searcher
    }
}