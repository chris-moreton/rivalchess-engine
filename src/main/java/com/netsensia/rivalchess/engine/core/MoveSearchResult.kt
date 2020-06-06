package com.netsensia.rivalchess.engine.core

import com.netsensia.rivalchess.engine.core.search.SearchPath
import com.netsensia.rivalchess.enums.HashValueType

data class MoveSearchResult(
        val bestPath: SearchPath,
        val low: Int,
        val scoutSearch: Boolean,
        val bestMoveForHash: Int,
        val threatExtend: Int,
        val hashFlag: Int,
        val moveIsLegal: Boolean) {

    class Builder {
        var bestPath: SearchPath = SearchPath()
        var low: Int = -Int.MAX_VALUE
        var scoutSearch: Boolean = false
        var bestMoveForHash: Int = 0
        var threatExtend: Int = 0
        var hashFlag: Int = HashValueType.UPPER.index
        var moveIsLegal: Boolean = false

        fun withBestPath(v: SearchPath) = apply { bestPath = v }
        fun withLow(v: Int) = apply { low = v }
        fun withScoutSearch(v: Boolean) = apply { scoutSearch = v }
        fun withBestMoveForHash(v: Int) = apply { bestMoveForHash = v }
        fun withThreatExtend(v: Int) = apply { threatExtend = v }
        fun withHashFlag(v: Int) = apply { hashFlag = v }
        fun withMoveIsLegal(v: Boolean) = apply { moveIsLegal = v }

        fun build() = MoveSearchResult(bestPath, low, scoutSearch, bestMoveForHash, threatExtend, hashFlag, moveIsLegal)
    }
}
