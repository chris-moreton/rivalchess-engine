package com.netsensia.rivalchess.engine.core.search

import com.netsensia.rivalchess.config.Limit
import com.netsensia.rivalchess.util.ChessBoardConversion

class SearchPath {
    @JvmField
	val move: IntArray

    @JvmField
    var score = 0
    @JvmField
	var height = 0

    fun reset(): SearchPath {
        height = 0
        score = -Int.MAX_VALUE
        return this
    }

    fun setPath(move: Int) {
        height = 1
        this.move[0] = move
    }

    fun setPath(path: SearchPath) {
        score = path.score
        height = path.height
        if (path.height >= 0) System.arraycopy(path.move, 0, move, 0, path.height)
    }

    fun withScore(newScore: Int): SearchPath {
        score = newScore
        return this
    }

    fun withHeight(newHeight: Int): SearchPath {
        height = newHeight
        return this
    }

    fun withPath(move: Int): SearchPath {
        height = 1
        this.move[0] = move
        return this
    }

    fun withPath(compactMove: Int, path: SearchPath): SearchPath {
        setPath(compactMove, path)
        return this
    }

    fun setPath(compactMove: Int, path: SearchPath) {
        height = path.height + 1
        move[0] = compactMove
        score = path.score
        if (path.height >= 0) System.arraycopy(path.move, 0, move, 1, path.height)
    }

    override fun toString(): String {
        var retString = ""
        try {
            for (i in 0 until height) retString += if (move[i] != 0) ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(move[i]) + " " else break
        } catch (e: NullPointerException) {
            throw RuntimeException()
        }
        return retString
    }

    init {
        move = IntArray(Limit.MAX_TREE_DEPTH.value)
    }
}