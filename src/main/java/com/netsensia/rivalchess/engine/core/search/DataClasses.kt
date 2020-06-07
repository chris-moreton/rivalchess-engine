package com.netsensia.rivalchess.engine.core.search

data class HashProbeResult (val move: Int, val window: Window, val bestPath: SearchPath?)

data class Window(var low: Int, var high: Int)

data class AspirationSearchResult (val path: SearchPath?, val window: Window)

data class RecaptureExtensionResponse(val extend: Int, val captureSquare: Int)