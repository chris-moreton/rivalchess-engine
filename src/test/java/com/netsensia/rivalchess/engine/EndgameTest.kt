package com.netsensia.rivalchess.engine

import com.netsensia.rivalchess.config.MATE_SCORE_START
import com.netsensia.rivalchess.config.MAX_SEARCH_MILLIS
import com.netsensia.rivalchess.engine.search.Search
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.util.getSimpleAlgebraicMoveFromCompactMove
import org.awaitility.Awaitility
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.TimeUnit

@kotlin.ExperimentalUnsignedTypes
class EndgameTest {

    companion object {
        const val MAX_NODES_TO_SEARCH = 10000000
    }

    @Test
    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testQueenVKingEndGameWinning() {
        solveForMate("8/8/8/8/1q6/8/4k3/2K5 b - - 0 1", "e2d3", willMateIn(2))
        solveForMate("8/8/8/8/1q6/3k4/8/3K4 b - - 0 1", "b4b1", willMateIn(1))
        solveForMate("8/8/8/8/1q6/4k3/2K5/8 b - - 0 1", "e3e2", willMateIn(3))
        solveForMate("8/8/8/8/1q6/8/2K1k3/8 b - - 0 1", "e2e3", willMateIn(3))
        solveForMate("8/8/8/8/5q2/K7/3k4/8 b - - 0 1", "d2c2", willMateIn(2))
        solveForMate("8/8/8/1q6/8/2K2k2/8/8 b - - 0 1", "f3e3", willMateIn(4))
        solveForMate("8/8/8/8/1q6/3k4/8/3K4 b - - 0 1", "b4b1", willMateIn(1))
        solveForMate("8/8/8/8/1q6/4k3/8/2K5 b - - 0 1", "e3d3", willMateIn(2))
        solveForMate("8/8/8/1q6/8/4k3/2K5/8 b - - 0 1", "b5b4", willMateIn(3))
    }

    @Test
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testQueenVKingEndGameLosing() {
        solveForMate("8/8/8/8/1q6/3k4/8/2K5 w - - 0 1", "c1d1", willBeMatedIn(1))
        solveForMate("8/8/8/8/1q6/4k3/2K5/8 w - - 0 1", "c2c1", willBeMatedIn(2))
        solveForMate("8/8/8/1q6/8/2K1k3/8/8 w - - 0 1", "c3c2", willBeMatedIn(3))
    }

    @Test
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testArenaGlitch() {
        solveForMate("8/8/7P/3Q1R2/8/8/4k3/6K1 w - - 9 68", "f5e5", willMateIn(1))
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    @kotlin.ExperimentalUnsignedTypes
    fun testMateIn8ToCheckmate_problematic_1() {
        solveForMate("8/8/8/8/5q2/8/1K2k3/8 b - - 0 1", "f4b4", willMateIn(4))
        solveForMate("8/8/8/3q4/1K4k1/8/8/8 b - - 0 1", "g4f3", willMateIn(7))
        solveForMate("8/8/8/3q4/8/2K2k2/8/8 b - - 0 1", listOf("d5c5","f3e4","f3e3"), willMateIn(6))
        solveForMate("8/8/1K6/4q3/8/7k/8/8 b - - 0 1", "e5d5", willMateIn(7))
        solveForMate("8/8/8/4q3/2K5/8/6k1/8 b - - 0 1", "g2f3", willMateIn(7))
        solveForMate("8/8/3q4/8/1K6/7k/8/8 w - - 0 1", "b4c4", willBeMatedIn(8))
        solveForMate("8/8/8/2q5/8/2K2k2/8/8 w - - 0 1", "c3b3", willBeMatedIn(4))
        solveForMate("8/8/8/1K1q4/8/7k/8/8 w - - 0 1", "b5b4", willBeMatedIn(8))
        solveForMate("8/8/8/1q6/8/5k2/1K6/8 w - - 0 1", "b2c2", willBeMatedIn(5))
        solveForMate("8/8/8/8/2K5/7k/1q6/8 b - - 0 1", "h3g4", willMateIn(9))
        solveForMate("8/8/8/4q3/1K6/7k/8/8 b - - 0 1", "e5d4", willMateIn(8))
        solveForMate("8/8/8/3q4/K7/7k/8/8 b - - 0 1", "d5c6", willMateIn(8))
        solveForMate("8/8/8/4q3/8/3K4/5k2/8 b - - 0 1", "e5d5", willMateIn(6))
        solveForMate("8/8/3q4/1K6/8/7k/8/8 b - - 0 1", "d6c7", willMateIn(8))
        solveForMate("8/8/8/2K5/8/7k/1q6/8 b - - 0 1", "b2c3", willMateIn(9))
        solveForMate("8/8/8/3q4/K5k1/8/8/8 w - - 0 1", "a4b4", willBeMatedIn(6))
        solveForMate("8/8/8/8/5q2/2K5/5k2/8 b - - 0 1", listOf("f2e2","f2e3"), willMateIn(5))
        solveForMate("8/8/8/2q5/8/5k2/1K6/8 b - - 0 1", "c5b4", willMateIn(5))
        solveForMate("8/8/8/3q4/1K6/5k2/8/8 w - - 0 1", "b4c3", willBeMatedIn(6))
        solveForMate("8/8/8/8/5q2/1K6/4k3/8 b - - 0 1", listOf("e2d2","e2d3"), willMateIn(4))
    }

    private fun willMateIn(score: Int) = 10000-(score*2)+1
    private fun willBeMatedIn(score: Int) = -10000+(score*2)

    @Throws(IllegalFenException::class, InterruptedException::class)
    @kotlin.ExperimentalUnsignedTypes
    private fun solveForMate(fen: String, expectedMove: String, expectedScore: Int, tolerateScore: Boolean = false) {
        solveForMate(fen, listOf(expectedMove), expectedScore, tolerateScore)
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @kotlin.ExperimentalUnsignedTypes
    private fun solveForMate(fen: String, expectedMoves: List<String>, expectedScore: Int, tolerateScore: Boolean = false) {
        val search = Search()
        val board = Board.fromFen(fen)
        Thread(search).start()
        search.setBoard(board)
        search.setHashSizeMB(8)
        search.setSearchDepth(Int.MAX_VALUE)
        search.setMillisToThink(MAX_SEARCH_MILLIS)
        search.setNodesToSearch(MAX_NODES_TO_SEARCH)
        search.startSearch()
        TimeUnit.MILLISECONDS.sleep(100)
        Awaitility.await().atMost(310000, TimeUnit.MILLISECONDS).until { !search.isSearching }
        println("path " + search.currentPath.toString().trim { it <= ' ' } + " | nodes " + search.nodes + " | score " + search.currentScoreHuman + " (" + search.currentScore + ")")
        Assert.assertTrue(expectedMoves.contains(getSimpleAlgebraicMoveFromCompactMove(search.currentMove)))
        val score  = search.currentScore.toLong()
        if (tolerateScore) {
            Assert.assertTrue(score <= MATE_SCORE_START || score >= MATE_SCORE_START)
        } else {
            Assert.assertEquals(expectedScore.toLong(), score)
        }
    }


}