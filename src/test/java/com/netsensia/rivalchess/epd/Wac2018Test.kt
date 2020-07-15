package com.netsensia.rivalchess.epd

import com.netsensia.rivalchess.config.MAX_SEARCH_DEPTH
import com.netsensia.rivalchess.engine.search.Search
import com.netsensia.rivalchess.enums.SearchState
import com.netsensia.rivalchess.exception.IllegalEpdItemException
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.model.Board
import com.netsensia.rivalchess.util.EpdItem
import com.netsensia.rivalchess.util.EpdReader
import com.netsensia.rivalchess.util.getPgnMoveFromCompactMove
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class Wac2018Test {

    companion object {
        private const val MAX_NODES_TO_SEARCH = 500000
        private const val MAX_SEARCH_SECONDS = 1000
        private var search: Search? = null
        private var fails = 0
        private const val RECALCULATE_FAILURES = false
    }

    private val failingPositions: List<String> = listOf(
            "WAC2018.071", // Fail 1
            "WAC2018.100", // Fail 3
            "WAC2018.130", // Fail 4
            "WAC2018.133", // Fail 5
            "WAC2018.141", // Fail 6
            "WAC2018.147", // Fail 7
            "WAC2018.152", // Fail 8
            "WAC2018.157", // Fail 9
            "WAC2018.163", // Fail 10
            "WAC2018.200", // Fail 11
            "WAC2018.213", // Fail 12
            "WAC2018.222", // Fail 13
            "WAC2018.237", // Fail 14
            "WAC2018.265" // Fail 15
    )

    @Before
    fun setup() {
        search = Search()
        Thread(search).start()
    }

    @Test
    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun winAtChess2018() {
        runEpdSuite("winAtChess2018.epd", "WAC2018.003", true)
    }

    @Test
    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun winAtChess2018Fails() {
        runEpdSuite("winAtChess2018.epd", "WAC2018.003", false)
    }

    @Throws(IllegalFenException::class)
    private fun testPosition(epdItem: EpdItem, expectedToPass: Boolean) {
        val board = Board.fromFen(epdItem.fen)

        search!!.quit()
        search = Search()
        Thread(search).start()
        search!!.setBoard(board)
        search!!.setSearchDepth(MAX_SEARCH_DEPTH - 2)
        search!!.setMillisToThink(MAX_SEARCH_SECONDS * 1000)
        search!!.setNodesToSearch(epdItem.maxNodesToSearch)
        search!!.clearHash()
        search!!.startSearch()
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        val now = LocalDateTime.now()
        if (!RECALCULATE_FAILURES) println(epdItem.id + " " + dtf.format(now))

        try {
            Awaitility.await().atMost(MAX_SEARCH_SECONDS.toLong(), TimeUnit.SECONDS).until { !search!!.isSearching }
        } catch (e: ConditionTimeoutException) {
            search!!.stopSearch()
            var state: SearchState
            do state = search!!.engineState while (state !== SearchState.READY && state !== SearchState.COMPLETE)
        }

        val move = getPgnMoveFromCompactMove(search!!.currentMove, epdItem.fen)
        val score = search!!.currentPath.score

        if (RECALCULATE_FAILURES) {
            if (!epdItem.bestMoves.contains(move)) {
                fails++
                println("\"" + epdItem.id + "\", // Fail " + fails)
            }
        } else {
            println("Looking for " + move + " with score $score in " + epdItem.bestMoves + " with score range (${epdItem.minScore},${epdItem.maxScore})")
            val correctMove = epdItem.bestMoves.contains(move)
            val correctScore = epdItem.minScore < score && epdItem.maxScore > score
            val passed = correctMove && correctScore
            Assert.assertEquals(expectedToPass, passed)
        }
    }

    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun runEpdSuite(filename: String, startAtId: String, expectedToPass: Boolean) {
        val classLoader = javaClass.classLoader
        val file = File(Objects.requireNonNull(classLoader.getResource("epd/$filename")).file)
        val epdReader = EpdReader(file.absolutePath)
        runEpdSuite(startAtId, epdReader, expectedToPass)
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun runEpdSuite(startAtId: String, epdReader: EpdReader, expectedToPass: Boolean) {
        var processTests = false
        for (epdItem in epdReader) {
            processTests = processTests || epdItem.id == startAtId
            if (processTests && (RECALCULATE_FAILURES || expectedToPass != failingPositions.contains(epdItem.id))) {
                testPosition(epdItem, expectedToPass)
            }
        }
    }

}