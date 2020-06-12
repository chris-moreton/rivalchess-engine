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

    @ExperimentalStdlibApi
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
        makeMove(engineBoard, seeBoard, "e8f8")
        makeMove(engineBoard, seeBoard, "f1b5")

        unMakeMove(engineBoard, seeBoard)
        makeMove(engineBoard, seeBoard, "f1b5")

        unMakeMove(engineBoard, seeBoard)
        unMakeMove(engineBoard, seeBoard)

        makeMove(engineBoard, seeBoard, "e8f8")
        makeMove(engineBoard, seeBoard, "f1b5")

        makeMove(engineBoard, seeBoard, "d8d2")
        makeMove(engineBoard, seeBoard, "d1d2")
        makeMove(engineBoard, seeBoard, "f7f5")
        makeMove(engineBoard, seeBoard, "b1c3")
        makeMove(engineBoard, seeBoard, "f5f4")
        makeMove(engineBoard, seeBoard, "g2g4")
        makeMove(engineBoard, seeBoard, "f4g3")

        unMakeMove(engineBoard, seeBoard)

        makeMove(engineBoard, seeBoard, "f4g3")

        unMakeMove(engineBoard, seeBoard)
        unMakeMove(engineBoard, seeBoard)

        makeMove(engineBoard, seeBoard, "g2g4")
        makeMove(engineBoard, seeBoard, "f4g3")
    }

    @ExperimentalStdlibApi
    private fun makeMove(board: EngineBoard, seeBoard: SeeBoard, moveString: String) {
        var move: EngineMove

        board.makeMove(getEngineMoveFromSimpleAlgebraic(moveString).also { move = it } )
        seeBoard.makeMove(move)
        assertPieceBitboardsMatch(board, seeBoard)
        assertEquals(board.mover, seeBoard.mover)

        board.unMakeMove()
        seeBoard.unMakeMove()
        assertPieceBitboardsMatch(board, seeBoard)
        assertEquals(board.mover, seeBoard.mover)

        board.makeMove(getEngineMoveFromSimpleAlgebraic(moveString).also { move = it } )
        seeBoard.makeMove(move)
        assertEquals(board.mover, seeBoard.mover)
    }

    @ExperimentalStdlibApi
    private fun unMakeMove(board: EngineBoard, seeBoard: SeeBoard) {

        board.unMakeMove()
        seeBoard.unMakeMove()
        assertPieceBitboardsMatch(board, seeBoard)
        assertEquals(board.mover, seeBoard.mover)

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

    @ExperimentalStdlibApi
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
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.H8, Square.G7))))
    }

    @Test
    fun moveGenerationFromComplexPosition3() {
        val engineBoard = EngineBoard(getBoardModel("2r1r2k/pp2qpp1/1nn1b2p/3pN3/2pP4/2P3Q1/PPB4P/R3R1K1 w - - 1 6"))
        val seeBoard = SeeBoard(engineBoard)
        val moves = seeBoard.generateCaptureMovesOnSquare(59).toList()
        assertEquals(0, moves.size)
    }

    @Test
    fun moveGenerationFromComplexPosition4() {
        val engineBoard = EngineBoard(getBoardModel("2rrq2k/pp3pp1/1nn1b1Bp/3pN3/2pP4/2P3Q1/PP5P/R4RK1 w - - 0 3"))
        val seeBoard = SeeBoard(engineBoard)
        var moves = seeBoard.generateCaptureMovesOnSquare(50).toList()
        assertEquals(3, moves.size)
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.E5, Square.F7))))
        moves = seeBoard.generateCaptureMovesOnSquare(50).toList()
        assertEquals(2, moves.size)
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.E6, Square.F7))))
        moves = seeBoard.generateCaptureMovesOnSquare(50).toList()
        assertEquals(2, moves.size)
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.F1, Square.F7))))
        moves = seeBoard.generateCaptureMovesOnSquare(50).toList()
        assertEquals(1, moves.size)
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.E8, Square.F7))))
        moves = seeBoard.generateCaptureMovesOnSquare(50).toList()
        assertEquals(1, moves.size)
        assertTrue(seeBoard.makeMove(EngineMove(Move(Square.G6, Square.F7))))
        moves = seeBoard.generateCaptureMovesOnSquare(50).toList()
        assertEquals(0, moves.size)
    }

    @Test
    fun moveGenerationFromComplexPosition5() {
        val engineBoard = EngineBoard(getBoardModel("2rr3k/pp3pp1/1nn1bN1p/3p4/3P4/2PQ4/PPB4q/R4RK1 w - - 0 3"))
        val seeBoard = SeeBoard(engineBoard)
        val moves = seeBoard.generateCaptureMovesOnSquare(8).toList()
        assertEquals(1, moves.size)
    }

    @Test
    fun moveGenerationFromComplexPosition6() {
        val engineBoard = EngineBoard(getBoardModel("2rr3k/pp3pp1/1n1qbN1p/3pn3/2pP1R2/2P3Q1/PPB4P/R5K1 w - - 0 2"))
        val seeBoard = SeeBoard(engineBoard)
        val moves = seeBoard.generateCaptureMovesOnSquare(35).toList()
        assertEquals(1, moves.size)
    }

    private fun assertPieceBitboardsMatch(board: EngineBoard, seeBoard: SeeBoard) {

        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_ENPASSANTSQUARE), seeBoard.bitboardMap[BITBOARD_ENPASSANTSQUARE])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_WP), seeBoard.bitboardMap[BITBOARD_WP])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_WQ), seeBoard.bitboardMap[BITBOARD_WQ])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_WK), seeBoard.bitboardMap[BITBOARD_WK])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_WN), seeBoard.bitboardMap[BITBOARD_WN])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_WB), seeBoard.bitboardMap[BITBOARD_WB])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_WR), seeBoard.bitboardMap[BITBOARD_WR])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_BP), seeBoard.bitboardMap[BITBOARD_BP])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_BQ), seeBoard.bitboardMap[BITBOARD_BQ])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_BK), seeBoard.bitboardMap[BITBOARD_BK])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_BN), seeBoard.bitboardMap[BITBOARD_BN])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_BB), seeBoard.bitboardMap[BITBOARD_BB])
        assertEquals(board.engineBitboards.getPieceBitboard(BITBOARD_BR), seeBoard.bitboardMap[BITBOARD_BR])

        assertEquals(board.mover, seeBoard.mover)
    }

}