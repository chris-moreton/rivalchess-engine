package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.engine.board.EngineBoard

class MaterialValues(val board: EngineBoard) {
        @JvmField
        val whitePieces = board.whitePieceValues
        @JvmField
        val blackPieces = board.blackPieceValues
        @JvmField
        val whitePawns = board.whitePawnValues
        @JvmField
        val blackPawns = board.blackPawnValues
}
