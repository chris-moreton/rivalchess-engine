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
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Ignore
class WacDetailedTest {

    private val epdStats = mutableMapOf<String, Int>()

    @kotlin.ExperimentalUnsignedTypes
    companion object {
        private const val RATIO_MAX = 10
        private const val RECALCULATE = true
        private const val REWRITE_EPD_FILE = true
        private const val MAX_SEARCH_SECONDS = 1000
        private var search: Search? = null
    }

    @Before
    @kotlin.ExperimentalUnsignedTypes
    fun setup() {
        search = Search()
        Thread(search).start()
    }

    @kotlin.ExperimentalUnsignedTypes
    @Test
    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun winAtChess2018() {
        runEpdSuite("winAtChessDetailed.epd")
    }

    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class)
    private fun testPosition(epdItem: EpdItem, nodesToSearch: Int, tryFewerNodes: Boolean = true, tryMoreNodes: Boolean = true) {
        val board = Board.fromFen(epdItem.fen)

        search!!.quit()
        search = Search()
        Thread(search).start()
        search!!.setBoard(board)
        search!!.setSearchDepth(MAX_SEARCH_DEPTH - 2)
        search!!.setMillisToThink(MAX_SEARCH_SECONDS * 1000)
        search!!.setNodesToSearch(nodesToSearch)
        search!!.clearHash()
        search!!.startSearch()
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        val now = LocalDateTime.now()
        if (!RECALCULATE) println(epdItem.id + " " + dtf.format(now) + " " + epdItem.fen)
        try {
            Awaitility.await().atMost(MAX_SEARCH_SECONDS.toLong(), TimeUnit.SECONDS).until { !search!!.isSearching }
        } catch (e: ConditionTimeoutException) {
            search!!.stopSearch()
            var state: SearchState
            do state = search!!.engineState while (state !== SearchState.READY && state !== SearchState.COMPLETE)
        }

        val move = getPgnMoveFromCompactMove(search!!.currentMove, epdItem.fen)
        val score = search!!.currentPath.score

        if (!RECALCULATE) println("Looking for " + move + " with score $score in " + epdItem.bestMoves +
                " with score range (${epdItem.minScore},${epdItem.maxScore}). Depth was ${search!!.iterativeDeepeningDepth}." +
                " Nodes: $nodesToSearch. Actual Nodes: ${search!!.nodes}")
        val passed = epdItem.bestMoves.contains(move) && epdItem.minScore <= score && epdItem.maxScore >= score

        if (RECALCULATE) {
            if (passed) {
                epdStats.put(epdItem.id, nodesToSearch - epdItem.maxNodesToSearch)
                if (nodesToSearch > 1000 && tryFewerNodes) testPosition(epdItem, ((nodesToSearch * 0.9).toInt() / 1000) * 1000, tryFewerNodes = true, tryMoreNodes = false)
            } else {
                if (tryMoreNodes) testPosition(epdItem, ((nodesToSearch * 1.1111).toInt() / 1000) * 1000 + 1000, tryMoreNodes = true, tryFewerNodes = false)
            }
        } else {
            println(search!!.currentPath)
            Assert.assertTrue(passed)
        }
    }

    @kotlin.ExperimentalUnsignedTypes
    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun runEpdSuite(filename: String) {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("epd/$filename").file)
        val epdReader = EpdReader(file.absolutePath)
        runEpdSuite(epdReader)
    }

    @kotlin.ExperimentalUnsignedTypes
    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun runEpdSuite(epdReader: EpdReader) {

        var sum = 0
        var better = 0
        var worse = 0
        var same = 0
        var totalRatio = 0.0
        var count = 0

        val file = File("newfile.epd")
        file.writeText("")

        for (epdItem in epdReader) {
            testPosition(epdItem, epdItem.maxNodesToSearch)
            if (RECALCULATE) {
                val nodeDifference = epdStats.get(epdItem.id)
                if (nodeDifference != null) {
                    count ++
                    if (nodeDifference == 0) same++ else if (nodeDifference < 0) better++ else worse++
                    val passFail = if (nodeDifference <= 0) "passed in time" else "eventually passed"
                    val passNodes = epdItem.maxNodesToSearch + nodeDifference
                    val ratio = passNodes / epdItem.maxNodesToSearch.toDouble()
                    totalRatio += ratio
                    println("${epdItem.id} $passFail using $passNodes nodes, node difference is $nodeDifference")
                    if (ratio > RATIO_MAX) {
                        Assert.assertTrue(false)
                    }
                    val averageRatio = totalRatio / count
                    sum += nodeDifference
                    println("Node difference = $sum, better = $better, worse = $worse, same = $same, ratio = $averageRatio")
                    if (REWRITE_EPD_FILE)
                        file.appendText("${epdItem.fen} bm ${epdItem.bestMoves.toTypedArray().joinToString(",")}; " +
                                        "cp ${epdItem.minScore} ${epdItem.maxScore}; nodes ${(epdItem.maxNodesToSearch + epdStats.get(epdItem.id)!!)}; id \"${epdItem.id}\";\n")
                }
            } else {
                println("========================================================================================")
            }
        }

    }

}