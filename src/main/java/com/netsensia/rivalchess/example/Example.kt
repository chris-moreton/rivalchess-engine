package com.netsensia.rivalchess.example

import com.netsensia.rivalchess.engine.core.search.Search
import com.netsensia.rivalchess.model.Board

fun main(args: Array<String>) {
    val board = Board.fromFen("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56")
    val searcher = Search(board)
    searcher.setMillisToThink(5000)
    searcher.setNodesToSearch(Int.MAX_VALUE)
    searcher.setSearchDepth(5)
    searcher.go()
    println("Path = ${searcher.currentPath}")
}