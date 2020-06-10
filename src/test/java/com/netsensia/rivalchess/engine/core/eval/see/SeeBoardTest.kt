package com.netsensia.rivalchess.engine.core.eval.see

import com.netsensia.rivalchess.engine.core.*
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.engine.core.board.getFen
import com.netsensia.rivalchess.engine.core.board.makeMove
import com.netsensia.rivalchess.engine.core.board.unMakeMove
import com.netsensia.rivalchess.engine.core.type.EngineMove
import com.netsensia.rivalchess.exception.IllegalFenException
import com.netsensia.rivalchess.exception.InvalidMoveException
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.util.FenUtils.getBoardModel
import com.netsensia.rivalchess.util.getEngineMoveFromSimpleAlgebraic
import org.junit.Assert.*
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
        makeMove(engineBoard, seeBoard, "a7a5")
        makeMove(engineBoard, seeBoard, "e7f8Q")
    }

    @Test
    fun moveGenerationFromStartPosition() {
        val engineBoard = EngineBoard(getBoardModel(FEN_START_POS))
        val seeBoard = SeeBoard(engineBoard)
        assertEquals(0, seeBoard.generateCaptureMovesOnSquare(45).toList().size)
    }

    @Test
    fun moveGenerationFromComplexPosition1() {
        val engineBoard = EngineBoard(getBoardModel("2rr3k/pp3pp1/1nnqbN1p/3pN3/2pP4/1PPQ4/P1B4P/R4RK1 w - - 1 6"))
        val seeBoard = SeeBoard(engineBoard)
        val moves = seeBoard.generateCaptureMovesOnSquare(29).toList()
        assertEquals(3, moves.size)
        assertTrue(moves.contains(EngineMove(Move(Square.B3, Square.C4))))
        assertTrue(moves.contains(EngineMove(Move(Square.D3, Square.C4))))
        assertTrue(moves.contains(EngineMove(Move(Square.E5, Square.C4))))
    }

    @Test
    fun moveGenerationFromComplexPosition2() {
        val engineBoard = EngineBoard(getBoardModel("2rr3k/pp3pN1/1nn1b2p/3pq3/2pP4/2P3Q1/PPB4P/R4RK1 b - - 1 6"))
        val seeBoard = SeeBoard(engineBoard)
        val moves = seeBoard.generateCaptureMovesOnSquare(49).toList()
        assertEquals(2, moves.size)
        assertTrue(moves.contains(EngineMove(Move(Square.E5, Square.G7))))
        assertTrue(moves.contains(EngineMove(Move(Square.H8, Square.G7))))
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.E5, Square.G7))))
        seeBoard.unMakeMove()
        assertFalse(seeBoard.makeMove(EngineMove(Move(Square.H8, Square.G7))))
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