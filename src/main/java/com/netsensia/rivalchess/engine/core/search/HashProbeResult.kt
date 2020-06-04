package com.netsensia.rivalchess.engine.core.search

data class HashProbeResult (val move: Int, val low: Int, val high: Int, val bestPath: SearchPath?)