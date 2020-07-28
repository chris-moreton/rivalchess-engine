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
import java.io.File
import java.lang.System.currentTimeMillis

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

    private var longestGame = 0
    private val numPlayers = 64
    private val nodesToSearch = 5000
    private val numGenerations = 2000
    private val file = File("ga " + currentTimeMillis() + ".txt")

    var players: MutableList<Player> = mutableListOf()

    fun go() {
        file.writeText("Piece Value GA Results")
        createGenerationZero()
        for (generation in 0 until numGenerations) {
            roundRobin()
            displayResults(players.sortedBy { -it.points }, generation)
            createNewGeneration(generation)
        }
    }

    private fun getPlayer(totalPoints: Int, sortedPlayers: List<Player>): Player {
        var cum = 0
        val r = (0 until totalPoints).random()
        for (j in 0 until numPlayers) {
            cum += sortedPlayers[j].points
            if (cum >= r) return Player(sortedPlayers[j].pieceValues.copyOf(), 0)
        }
        throw Exception("Error getting player for new generation")
    }

    private fun createNewGeneration(generation: Int) {
        val sortedPlayers = players.sortedBy { -it.points }
        val totalPoints: Int = players.map { it.points }.sum()

        players = mutableListOf()
        for (i in 0 until numPlayers) {
            val newPlayer = getPlayer(totalPoints, sortedPlayers)

            for (k in 0 until 5) {
                if ((0..4).random() == 0) {
                    var adjustment = (newPlayer.pieceValues[k]).toDouble() * ((1..25).random().toDouble() / 100.0)
                    if ((0..1).random() == 0) adjustment = -adjustment
                    newPlayer.pieceValues[k] += adjustment.toInt()
                }
            }

            players.add(newPlayer)
        }

        displayGenerationResults(sortedPlayers, generation)
    }

    private fun displayGenerationResults(sortedPlayers: List<Player>, generation: Int) {
        outln("".padStart(50, '='))
        outln("The Next Generation:")
        outln("".padStart(50, '='))
        players.forEach { outln(it.toString()) }
        outln("Current Champion ($generation):")
        outln(sortedPlayers[0].toString())
        outln("Longest Game: ${longestGame}")
        outln("".padStart(50, '='))
    }

    private fun roundRobin() {
        outln()
        for (white in 0 until numPlayers) {
            out("$white ")
            for (black in 0 until numPlayers) {
                if (white != black && (0..10).random() == 0) {
                    when (playGame(players.get(white), players.get(black))) {
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
        outln()
    }

    private fun displayResults(players: List<Player>, generation: Int) {
        outln("".padStart(50, '='))
        outln("Generation $generation Results")
        outln("".padStart(50, '='))
        players.forEach { outln(it.toString()) }
    }

    private fun out(str: String) {
        file.appendText(str)
        print(str)
    }

    private fun outln(str: String) {
        out(str)
        out("\n")
    }

    private fun outln() {
        out("")
    }

    private fun createGenerationZero() {
        for (i in 0 until numPlayers) {
            players.add(Player(intArrayOf(
                    50 + (0..500).random(),
                    50 + (0..500).random(),
                    50 + (0..500).random(),
                    50 + (0..500).random(),
                    50 + (0..500).random(),
                    30000
            ), 0))
        }
    }

    private fun playGame(whitePlayer: Player, blackPlayer: Player): Int {

        val moveList = mutableListOf<Int>()
        var board = Board.fromFen(FEN_START_POS)

        var moveNumber = 1
        while (board.getLegalMoves().isNotEmpty()) {
            if (moveNumber > longestGame) {
                longestGame = moveNumber
            }
            if (moveNumber >= 500) {
                print("_X_")
                return GAME_TOO_LONG
            }
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
        searcher.setNodesToSearch(nodesToSearch + (0..nodesToSearch).random())
        pieceValues = player.pieceValues

        return searcher
    }
}