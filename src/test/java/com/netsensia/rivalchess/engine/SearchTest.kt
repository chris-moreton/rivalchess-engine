package com.netsensia.rivalchess.engine

import com.netsensia.rivalchess.config.MATE_SCORE_START
import com.netsensia.rivalchess.config.MAX_SEARCH_DEPTH
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

class SearchTest {
    companion object {
        private const val RECALCULATE = false
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    fun testMateIn8ToCheckmate_problematic_1() {
        solveForMate("8/8/1K6/4q3/8/7k/8/8 b - - 0 1", "e5d5", willMateIn(7))
        solveForMate("8/8/8/4q3/2K5/8/6k1/8 b - - 0 1", "g2f3", willMateIn(7))
        solveForMate("8/8/8/3q4/8/2K2k2/8/8 b - - 0 1", "f3e4", willMateIn(6))
        solveForMate("8/8/8/3q4/1K4k1/8/8/8 b - - 0 1", "g4f3", willMateIn(7))
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    fun testMateIn8ToCheckmate_problematic_2() {
        solveForMate("8/8/3q4/8/1K6/7k/8/8 w - - 0 1", "b4c4", willBeMatedIn(8))
        solveForMate("8/8/8/2q5/8/2K2k2/8/8 w - - 0 1", "c3b3", willBeMatedIn(4))
        solveForMate("8/8/8/1K1q4/8/7k/8/8 w - - 0 1", "b5b4", willBeMatedIn(8))
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    fun testMateIn8ToCheckmate_problematic_3() {
        solveForMate("8/8/8/8/2K5/7k/1q6/8 b - - 0 1", "h3g4", willMateIn(9))
        solveForMate("8/8/8/4q3/1K6/7k/8/8 b - - 0 1", "e5d4", willMateIn(8))
        solveForMate("8/8/8/3q4/K7/7k/8/8 b - - 0 1", "d5c6", willMateIn(8))
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    fun testMateIn8ToCheckmate_problematic_4() {
        solveForMate("8/8/8/4q3/8/3K4/5k2/8 b - - 0 1", "e5d5", willMateIn(6))
        solveForMate("8/8/3q4/1K6/8/7k/8/8 b - - 0 1", "d6c7", willMateIn(8))
        solveForMate("8/8/8/2K5/8/7k/1q6/8 b - - 0 1", "b2c3", willMateIn(9))
        solveForMate("8/8/8/3q4/K5k1/8/8/8 w - - 0 1", "a4b4", willBeMatedIn(6))
        solveForMate("8/8/8/8/5q2/2K5/5k2/8 b - - 0 1", listOf("f2e2","f2e3"), willMateIn(5))
        solveForMate("8/8/8/2q5/8/5k2/1K6/8 b - - 0 1", "c5b4", willMateIn(5))

    }

    @Test
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testQueenVKingEndGame() {
        solveForMate("8/8/8/8/1q6/8/4k3/2K5 b - - 0 1", "e2d3", willMateIn(2))
        solveForMate("8/8/8/8/1q6/3k4/8/3K4 b - - 0 1", "b4b1", willMateIn(1))
        solveForMate("8/8/8/8/1q6/4k3/2K5/8 b - - 0 1", "e3e2", willMateIn(3))
        solveForMate("8/8/8/8/1q6/8/2K1k3/8 b - - 0 1", "e2e3", willMateIn(3))
        solveForMate("8/8/8/8/5q2/8/1K2k3/8 b - - 0 1", "f4b4", willMateIn(4))
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    fun testQueenVKingEndGameDodgyPositions() {
        solveForMate("8/8/8/8/5q2/K7/3k4/8 b - - 0 1", "d2c2", willMateIn(2))
        solveForMate("8/8/8/8/5q2/1K6/4k3/8 b - - 0 1", "e2d3", willMateIn(4))
        solveForMate("8/8/8/3q4/1K6/5k2/8/8 w - - 0 1", "b4c3", willBeMatedIn(6))
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    @Test
    @Ignore
    fun testMateIn8ToCheckmate_1() {
        solveForMate("8/8/8/8/1q6/3k4/8/3K4 b - - 0 1", "b4b1", willMateIn(1))
        solveForMate("8/8/8/8/1q6/3k4/8/2K5 w - - 0 1", "c1d1", willBeMatedIn(1))
        solveForMate("8/8/8/8/1q6/4k3/8/2K5 b - - 0 1", "e3d3", willMateIn(2))
        solveForMate("8/8/8/8/1q6/4k3/2K5/8 w - - 0 1", "c2c1", willBeMatedIn(2))
        solveForMate("8/8/8/1q6/8/4k3/2K5/8 b - - 0 1", "b5b4", willMateIn(3))
        solveForMate("8/8/8/1q6/8/2K1k3/8/8 w - - 0 1", "c3c2", willBeMatedIn(3))
        solveForMate("8/8/8/1q6/8/2K2k2/8/8 b - - 0 1", "f3e3", willMateIn(4))
        solveForMate("8/8/8/1q6/8/5k2/1K6/8 w - - 0 1", "b2c2", willBeMatedIn(5))
    }

    fun willMateIn(score: Int) = 10000-(score*2)+1
    fun willBeMatedIn(score: Int) = -10000+(score*2)

    @Test
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testBestMoves() {
        assertBestMove("8/4k3/8/8/2p2P2/8/2P5/5K2 b - - 0 1", "e7e6", -226);
        assertBestMove("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", "g4g2", -1664);
        assertBestMove("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", "f7e6", -249);
        assertBestMove("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", "g5g7", 9995);
        assertBestMove("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2h1q", 1220);
        assertBestMove("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "e5d4", 178);
        assertBestMove("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "d2d4", 77);
        assertBestMove("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4", 28);
        assertBestMove("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "e2a6", 30);
    }

    @Test
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testNodeCount() {
        assertNodeCount("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", 3474);
        assertNodeCount("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 24363);
        assertNodeCount("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 14446);
        assertNodeCount("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 67826);
        assertNodeCount("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 40617);
        assertNodeCount("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 190098);
        assertNodeCount("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -", 104683);
        assertNodeCount("8/k1b5/P4p2/1Pp2p1p/K1P2P1P/8/3B4/8 w - -", 3088);
        assertNodeCount("3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - -", 161236);
        assertNodeCount("4r3/1Q1qk2p/p4pp1/3Pb3/P7/6PP/5P2/4R1K1 w - -", 157171);
        assertNodeCount("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", 35339);
        assertNodeCount("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", 30389);
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun assertBestMove(fen: String, expectedMove: String, expectedScore: Int, searchDepth: Int = MAX_SEARCH_DEPTH, searchMillis: Int = 5000, searchNodes: Int = 500000) {
        val search = Search()
        val board = Board.fromFen(fen)
        Thread(search).start()
        search.setBoard(board)
        search.setHashSizeMB(8)
        search.setSearchDepth(searchDepth)
        search.setMillisToThink(searchMillis)
        search.setNodesToSearch(searchNodes)
        search.startSearch()
        TimeUnit.MILLISECONDS.sleep(100)
        Awaitility.await().atMost(searchMillis.toLong() + 1000, TimeUnit.MILLISECONDS).until { !search.isSearching }

        if (RECALCULATE) {
            println("assertBestMove(\"" + fen + "\", \"" + getSimpleAlgebraicMoveFromCompactMove(search.currentMove) + "\", " + search.currentScore + ");")
        } else {
            Assert.assertEquals(expectedMove, getSimpleAlgebraicMoveFromCompactMove(search.currentMove))
            Assert.assertEquals(expectedScore.toLong(), search.currentScore.toLong())
        }

    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun solveForMate(fen: String, expectedMove: String, expectedScore: Int, tolerateScore: Boolean = false) {
        solveForMate(fen, listOf(expectedMove), expectedScore, tolerateScore)
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun solveForMate(fen: String, expectedMoves: List<String>, expectedScore: Int, tolerateScore: Boolean = false) {
        val search = Search()
        val board = Board.fromFen(fen)
        Thread(search).start()
        search.setBoard(board)
        search.setHashSizeMB(8)
        search.setSearchDepth(Int.MAX_VALUE)
        search.setMillisToThink(MAX_SEARCH_MILLIS)
        search.setNodesToSearch(20000000)
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

    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun assertNodeCount(fen: String, expectedNodes: Int) {
        val board = Board.fromFen(fen)
        val search = Search()
        Thread(search).start()
        search.setBoard(board)
        search.setSearchDepth(6)
        search.setMillisToThink(MAX_SEARCH_MILLIS)
        search.startSearch()
        TimeUnit.SECONDS.sleep(1)
        Awaitility.await().atMost(60, TimeUnit.SECONDS).until { !search.isSearching }
        if (RECALCULATE) {
            println("assertNodeCount(\"" + fen + "\", " + search.nodes + ");")
        } else {
            Assert.assertEquals(expectedNodes, search.nodes)
        }
    }

}