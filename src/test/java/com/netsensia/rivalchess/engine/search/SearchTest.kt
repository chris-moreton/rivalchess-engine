package com.netsensia.rivalchess.engine.search

import com.netsensia.rivalchess.config.MAX_SEARCH_DEPTH
import com.netsensia.rivalchess.config.MAX_SEARCH_MILLIS
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.util.getSimpleAlgebraicMoveFromCompactMove
import org.awaitility.Awaitility
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.TimeUnit

@Ignore
class SearchTest {
    companion object {
        private const val RECALCULATE = true
    }

    @Test
    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testBestMoves() {
        assertBestMove("r1bq1rk1/ppp1ppbp/3p1np1/3P4/1nPQP3/2N1BP2/PP4PP/R3KBNR b - - 0 1", "b4c2", 874)
        assertBestMove("r1bqk2r/ppp2ppp/2p2n2/4p3/2P5/2N2N1P/PP1PPKP1/R1B2B1R b - - 0 1", "e5e4", 571)
        assertBestMove("8/4k3/8/8/2p2P2/8/2P5/5K2 b - - 0 1", "e7e6", -218)
        assertBestMove("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", "g4g2", -1450)
        assertBestMove("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", "f7e6", -486)
        assertBestMove("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", "f4e5", 9995)
        assertBestMove("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", "g2h1q", 1299)
        assertBestMove("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", "e5d4", 164)
        assertBestMove("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", "d2d4", 96)
        assertBestMove("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", "b4f4", 23)
        assertBestMove("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", "e2a6", -23)
    }

    @Test
    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testNodeCount() {
        assertNodeCount("5k2/5p1p/p3B1p1/P5P1/3K1P1P/8/8/8 b - -", 1384)
        assertNodeCount("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 1 67", 291076)
        assertNodeCount("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3 0 28", 2815)
        assertNodeCount("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 23525)
        assertNodeCount("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 7717)
        assertNodeCount("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 103593)
        assertNodeCount("6k1/p5p1/5p2/2P2Q2/3pN2p/3PbK1P/7P/6q1 b - -", 14792)
        assertNodeCount("8/k1b5/P4p2/1Pp2p1p/K1P2P1P/8/3B4/8 w - -", 4092)
        assertNodeCount("3r2k1/ppp2ppp/6q1/b4n2/3nQB2/2p5/P4PPP/RN3RK1 b - -", 21225)
        assertNodeCount("4r3/1Q1qk2p/p4pp1/3Pb3/P7/6PP/5P2/4R1K1 w - -", 19185)
        assertNodeCount("k7/5RP1/1P6/1K6/6r1/8/8/8 b - -", 5445)
        assertNodeCount("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", 19606)
    }

    @Test
    @Ignore
    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    fun testNodeCountDeep() {
        assertNodeCount("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3", 10477423, 14);
    }

    @kotlin.ExperimentalUnsignedTypes
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
        TimeUnit.MILLISECONDS.sleep(500)
        Awaitility.await().atMost(searchMillis.toLong() + 1000, TimeUnit.MILLISECONDS).until { !search.isSearching }

        if (RECALCULATE) {
            println("assertBestMove(\"" + fen + "\", \"" + getSimpleAlgebraicMoveFromCompactMove(search.currentMove) + "\", " + search.currentScore + ")")
        } else {
            Assert.assertEquals(expectedMove, getSimpleAlgebraicMoveFromCompactMove(search.currentMove))
            Assert.assertEquals(expectedScore.toLong(), search.currentScore.toLong())
        }

    }

    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun assertNodeCount(fen: String, expectedNodes: Int, searchDepth: Int = 6) {
        val board = Board.fromFen(fen)
        val search = Search()
        Thread(search).start()
        search.setBoard(board)
        search.setSearchDepth(searchDepth)
        search.setMillisToThink(MAX_SEARCH_MILLIS)
        search.startSearch()
        TimeUnit.SECONDS.sleep(1)
        Awaitility.await().atMost(60, TimeUnit.SECONDS).until { !search.isSearching }
        if (RECALCULATE) {
            println("assertNodeCount(\"" + fen + "\", " + search.nodes + ")")
        } else {
            Assert.assertEquals(expectedNodes, search.nodes)
        }
    }

}