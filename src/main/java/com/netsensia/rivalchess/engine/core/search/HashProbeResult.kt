package com.netsensia.rivalchess.engine.core.search

data class HashProbeResult (val move: Int, val window: Window, val bestPath: SearchPath?)