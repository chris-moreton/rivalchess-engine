package com.netsensia.rivalchess.engine.core.eval

data class MaterialValues(val bitboardData: BitboardData) {
        val whitePieces by lazy { whitePieceValues(bitboardData) }
        val blackPieces by lazy { blackPieceValues(bitboardData) }
        val whitePawns by lazy { whitePawnValues(bitboardData) }
        val blackPawns by lazy { blackPawnValues(bitboardData) }
}