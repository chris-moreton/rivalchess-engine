package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.board.getFen
import com.netsensia.rivalchess.engine.core.board.makeMove
import com.netsensia.rivalchess.engine.core.board.unMakeMove
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.util.getEngineMoveFromSimpleAlgebraic
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SeeBoardTest {

    @Test
    @Throws(IllegalFenException::class, InvalidMoveException::class)
    fun makeMove() {
        val engineBoard = EngineBoard(getBoardModel(FEN_START_POS))
        val seeBoard = SeeBoard(engineBoard)

        assertEquals(FEN_START_POS, engineBoard.getFen())
        makeMove(engineBoard, seeBoard, "e2e4")
        makeMove(engineBoard, seeBoard, "c7c5")
        makeMove(engineBoard, seeBoard, "e4e5")
        makeMove(engineBoard, seeBoard, "d7d5")
        makeMove(engineBoard, seeBoard, "e5d6")
        makeMove(engineBoard, seeBoard, "e7e5")
        makeMove(engineBoard, seeBoard, "a2a3")
        makeMove(engineBoard, seeBoard, "g8e7")
        makeMove(engineBoard, seeBoard, "d6e7")
    }

    private fun makeMove(board: EngineBoard, seeBoard: SeeBoard, moveString: String) {
        var move: EngineMove

        board.makeMove(getEngineMoveFromSimpleAlgebraic(moveString).also { move = it } )
        seeBoard.makeMove(move)
        assertPieceBitboardsMatch(board, seeBoard)
        board.unMakeMove()
        seeBoard.unMakeMove()
        assertPieceBitboardsMatch(board, seeBoard)

        board.makeMove(getEngineMoveFromSimpleAlgebraic(moveString).also { move = it } )
        seeBoard.makeMove(move)
    }

    private fun assertPieceBitboardsMatch(board: EngineBoard, seeBoard: SeeBoard) {
        val bitboardTypes = listOf(BITBOARD_ENPASSANTSQUARE, BITBOARD_WP, BITBOARD_WQ, BITBOARD_WK, BITBOARD_WN, BITBOARD_WB, BITBOARD_WR, BITBOARD_BP, BITBOARD_BQ, BITBOARD_BK, BITBOARD_BN, BITBOARD_BB, BITBOARD_BR)

        bitboardTypes.forEach{
            assertEquals(board.engineBitboards.getPieceBitboard(it), seeBoard.bitboardMap[it])
        }

        assertEquals(board.mover, seeBoard.mover)
    }

}