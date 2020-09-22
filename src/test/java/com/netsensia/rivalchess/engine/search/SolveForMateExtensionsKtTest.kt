package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.config.MAX_SEARCH_MILLIS
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.util.getSimpleAlgebraicMoveFromCompactMove
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

@kotlin.ExperimentalUnsignedTypes
internal class SolveForMateExtensionsKtTest : TestCase() {

    @Test
    fun testSolveForMate() {
        solveForMate("8/8/8/8/1q6/3k4/8/3K4 b - - 0 1", "b4b1", willMateIn(1))
        solveForMate("8/8/8/8/1q6/4k3/8/2K5 b - - 0 1", "e3d3", willMateIn(2))
    }

    private fun solveForMate(fen: String, expectedMove: String, expectedScore: Int) {
        solveForMate(fen, listOf(expectedMove), expectedScore)
    }

    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun solveForMate(fen: String, expectedMoves: List<String>, expectedScore: Int) {
        val search = Search()
        search.setHashSizeMB(8)
        search.setSearchDepth(Int.MAX_VALUE)
        search.setMillisToThink(MAX_SEARCH_MILLIS)
        search.setNodesToSearch(Int.MAX_VALUE)
        search.initSearchVariables()
        val board = Board.fromFen(fen)
        val path = search.solveForMate(EngineBoard(board), 4)
        Assert.assertNotNull(path)
        println("path " + path.toString().trim { it <= ' ' } + " | nodes " + search.nodes + " | score " + path!!.score)
        Assert.assertTrue(expectedMoves.contains(getSimpleAlgebraicMoveFromCompactMove(path.move[0])))
        val score  = path.score.toLong()
        Assert.assertEquals(expectedScore.toLong(), score)
    }

    fun willMateIn(score: Int) = 10000-(score*2)+1
    fun willBeMatedIn(score: Int) = -10000+(score*2)
}