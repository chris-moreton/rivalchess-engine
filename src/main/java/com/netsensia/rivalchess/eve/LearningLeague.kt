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
import com.netsensia.rivalchess.util.getMoveRefFromCompactMove

fun main(args: Array<String>) {
    val learningLeague = LearningLeague()
    learningLeague.go()
}

class Player(var pieceValues: IntArray, var points: Int) {

    override fun toString(): String {
        return pieceValues.take(5).joinToString(" ", transform = { it.toString().padStart(6) } )
    }
}

class LearningLeague {

    private val numPlayers = 30
    private val nodesToSearch = 5000
    private val numGenerations = 2000

    var players: MutableList<Player> = mutableListOf()

    fun go() {
        createGenerationZero()
        for (generation in 0 until numGenerations) {
            roundRobin()
            displayResults(players.sortedBy { -it.points }, generation)
            createNewGeneration()
        }
    }

    private fun getPlayer(totalPoints: Int, sortedPlayers: List<Player>): Player {
        var cum = 0
        val r = secureRandom.nextInt(totalPoints)
        for (j in 0 until numPlayers) {
            cum += sortedPlayers[j].points
            if (cum >= r) return Player(sortedPlayers[j].pieceValues.copyOf(), 0)
        }
        throw Exception("Error getting player for new generation")
    }

    private fun createNewGeneration() {
        val sortedPlayers = players.sortedBy { -it.points }
        val totalPoints: Int = players.map { it.points }.sum()

        players = mutableListOf()
        for (i in 0 until numPlayers) {
            val newPlayer = getPlayer(totalPoints, sortedPlayers)

            for (k in 1 until 5) {
                if (secureRandom.nextInt(3) == 0) {
                    var adjustment = (newPlayer.pieceValues[k]).toDouble() * (secureRandom.nextInt(5).toDouble() / 100.0)
                    if (secureRandom.nextInt(2) == 0) adjustment = -adjustment
                    newPlayer.pieceValues[k] += adjustment.toInt()
                }
            }

            players.add(newPlayer)
        }
        println("".padStart(50, '='))
        println("The Next Generation")
        println("".padStart(50, '='))
        players.forEach { println(it) }
    }

    private fun roundRobin() {
        println()
        for (white in 0 until numPlayers) {
            print(white)
            for (black in 0 until numPlayers) {
                if (white != black) {
                    print(".")
                    val result = playGame(players.get(white), players.get(black))
                    when (result) {
                        WHITE_WIN -> players.get(white).points += 2
                        BLACK_WIN -> players.get(black).points += 2
                        else -> {
                            players.get(white).points++
                            players.get(black).points++
                        }
                    }
                }
            }
        }
        println()
    }

    private fun displayResults(players: List<Player>, generation: Int) {
        println("".padStart(50, '='))
        println("Generation $generation Results")
        println("".padStart(50, '='))
        players.forEach { println(it) }
    }

    private fun createGenerationZero() {
        for (i in 0 until numPlayers) {
            players.add(Player(intArrayOf(
                    100,
                    100 + secureRandom.nextInt(1500),
                    100 + secureRandom.nextInt(1500),
                    100 + secureRandom.nextInt(1500),
                    100 + secureRandom.nextInt(1500),
                    30000
            ), 0))
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
        searcher.setSearchDepth(MAX_SEARCH_DEPTH)
        searcher.setNodesToSearch(nodesToSearch + secureRandom.nextInt(nodesToSearch))
        pieceValues = player.pieceValues

        return searcher
    }
}