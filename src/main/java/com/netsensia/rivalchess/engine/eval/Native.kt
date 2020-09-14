package com.netsensia.rivalchess.engine.eval

import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.annotation.Platform

@Platform(include=["eval.h"])
object Native {
    external fun linearScale(x: Int, min: Int, max: Int, a: Int, b: Int): Int

    init {
        Loader.load()
    }
}