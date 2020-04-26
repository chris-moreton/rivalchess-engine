package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.EngineChessBoard
import com.netsensia.rivalchess.util.Numbers

fun onlyKingRemains(board: EngineChessBoard) : Boolean =
        board.getWhitePieceValues() +
        board.getBlackPieceValues() +
        board.getWhitePawnValues() +
        board.getBlackPawnValues() == 0;

fun whitePawnPieceSquareEval(board: EngineChessBoard) : Int {
    var pieceSquareTemp = 0
    var pieceSquareTempEndGame = 0
    var bitboard: Long
    var sq: Int

    bitboard = board.whitePawnBitboard
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        pieceSquareTemp += PieceSquareTables.pawn[sq]
        pieceSquareTempEndGame += PieceSquareTables.pawnEndGame[sq]
    }

    return Numbers.linearScale(
            board.blackPieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pieceSquareTempEndGame,
            pieceSquareTemp
    )
}

fun blackPawnPieceSquareEval(board: EngineChessBoard) : Int {
    var pieceSquareTemp = 0
    var pieceSquareTempEndGame = 0
    var bitboard: Long
    var sq: Int

    bitboard = board.blackPawnBitboard
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        pieceSquareTemp += PieceSquareTables.pawn[Bitboards.bitFlippedHorizontalAxis[sq]]
        pieceSquareTempEndGame += PieceSquareTables.pawnEndGame[Bitboards.bitFlippedHorizontalAxis[sq]]
    }

    return Numbers.linearScale(
            board.whitePieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pieceSquareTempEndGame,
            pieceSquareTemp
    )
}