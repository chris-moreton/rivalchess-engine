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
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class WacDetailedTest {

    companion object {
        private const val MAX_SEARCH_SECONDS = 1000
        private var search: Search? = null
    }

    @Before
    fun setup() {
        search = Search()
        Thread(search).start()
    }

    @Test
    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun winAtChess2018() {
        runEpdSuite("winAtChessDetailed.epd")
    }

    @Throws(IllegalFenException::class)
    private fun testPosition(epdItem: EpdItem) {
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
        println(epdItem.id + " " + dtf.format(now))
        try {
            Awaitility.await().atMost(MAX_SEARCH_SECONDS.toLong(), TimeUnit.SECONDS).until { !search!!.isSearching }
        } catch (e: ConditionTimeoutException) {
            search!!.stopSearch()
            var state: SearchState
            do state = search!!.engineState while (state !== SearchState.READY && state !== SearchState.COMPLETE)
        }

        val move = getPgnMoveFromCompactMove(search!!.currentMove, epdItem.fen)
        val score = search!!.currentPath.score

        println("Looking for " + move + " with score $score in " + epdItem.bestMoves + " with score range (${epdItem.minScore},${epdItem.maxScore})")
        val correctMove = epdItem.bestMoves.contains(move)
        val correctScore = epdItem.minScore < score && epdItem.maxScore > score
        Assert.assertTrue(correctMove && correctScore)
    }

    @Throws(IOException::class, IllegalEpdItemException::class, IllegalFenException::class, InterruptedException::class)
    fun runEpdSuite(filename: String) {
        val classLoader = javaClass.classLoader
        val file = File(Objects.requireNonNull(classLoader.getResource("epd/$filename")).file)
        val epdReader = EpdReader(file.absolutePath)
        runEpdSuite(epdReader)
    }

    @Throws(IllegalFenException::class, InterruptedException::class)
    private fun runEpdSuite(epdReader: EpdReader) {
        for (epdItem in epdReader) {
            testPosition(epdItem)
        }
    }

}