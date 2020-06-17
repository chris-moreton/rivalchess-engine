package com.netsensia.rivalchess.engine.search

class HashProbeResult constructor (@JvmField val move: Int, @JvmField val window: Window, @JvmField val bestPath: SearchPath?)

class Window(@JvmField var low: Int, @JvmField var high: Int) {}

class AspirationSearchResult constructor (@JvmField val path: SearchPath?, @JvmField val window: Window)

class RecaptureExtensionResponse constructor (@JvmField val extend: Int, @JvmField val captureSquare: Int)