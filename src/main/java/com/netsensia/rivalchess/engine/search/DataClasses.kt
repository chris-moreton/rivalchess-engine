package com.netsensia.rivalchess.engine.search

class HashProbeResult constructor (@JvmField val move: Int, @JvmField val low: Int, @JvmField val high: Int, @JvmField val bestPath: SearchPath?)

class AspirationSearchResult constructor (@JvmField val path: SearchPath?, @JvmField val low: Int, @JvmField val high: Int)

class RecaptureExtensionResponse constructor (@JvmField val extend: Int, @JvmField val captureSquare: Int)