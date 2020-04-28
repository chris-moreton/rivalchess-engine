package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.EngineChessBoard
import com.netsensia.rivalchess.model.Piece
import java.lang.Long.bitCount
import java.util.function.Function
import java.util.stream.Collectors

fun onlyKingRemains(board: EngineChessBoard) =
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

    return linearScale(
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

    return linearScale(
            board.whitePieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pieceSquareTempEndGame,
            pieceSquareTemp
    )
}

fun whiteKingSquareEval(board: EngineChessBoard) =
        linearScale(
                board.getBlackPieceValues(),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                PieceSquareTables.kingEndGame.get(board.getWhiteKingSquare()),
                PieceSquareTables.king.get(board.getWhiteKingSquare())
        )

fun blackKingSquareEval(board: EngineChessBoard) =
        linearScale(
                board.getWhitePieceValues(),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                PieceSquareTables.kingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare())),
                PieceSquareTables.king.get(Bitboards.bitFlippedHorizontalAxis.get(board.getBlackKingSquare()))
        )

fun linearScale(situation: Int, ref1: Int, ref2: Int, score1: Int, score2: Int) =
    if (situation < ref1) score1
    else if (situation > ref2) score2
    else (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1

fun materialDifference(board: EngineChessBoard) =
    board.whitePieceValues - board.blackPieceValues + board.whitePawnValues - board.blackPawnValues

fun twoWhiteRooksTrappingKingEval(board: EngineChessBoard) =
    if (bitCount(board.whiteRookBitboard and Bitboards.RANK_7) > 1
            && board.getBitboard(BitboardType.BK) and Bitboards.RANK_8 != 0L)
        Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.getValue() else 0

fun twoBlackRooksTrappingKingEval(board: EngineChessBoard) =
    if (bitCount(board.blackRookBitboard and Bitboards.RANK_2) > 1
            && board.whiteKingBitboard and Bitboards.RANK_1 != 0L)
        Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.getValue() else 0

fun whiteRookOpenFilesEval(board: EngineChessBoard, file: Int) =
    if (Bitboards.FILES[file] and board.whitePawnBitboard == 0L)
        if (Bitboards.FILES[file] and board.blackPawnBitboard == 0L)
            Evaluation.VALUE_ROOK_ON_OPEN_FILE.value
        else
            Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.value
    else 0

fun blackRookOpenFilesEval(board: EngineChessBoard, file: Int) =
    if ((Bitboards.FILES.get(file) and board.getBlackPawnBitboard()) == 0L)
        if ((Bitboards.FILES.get(file) and board.getWhitePawnBitboard()) == 0L)
            Evaluation.VALUE_ROOK_ON_OPEN_FILE.getValue()
        else
            Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.getValue()
    else 0

fun rookAttacks(board: EngineChessBoard, sq: Int) : Long =
    Bitboards.magicBitboards.magicMovesRook[sq][
                    ((board.getAllPiecesBitboard() and MagicBitboards.occupancyMaskRook[sq])
                            * MagicBitboards.magicNumberRook[sq]
                            ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun rookEnemyPawnMultiplier(enemyPawnValues: Int) =
        Math.min(enemyPawnValues / PieceValue.getValue(Piece.PAWN), 6)

fun combineAttacks(whiteAttacksBitboard: Long, whiteRookAttacks: Map<Int, Long>): Long {
    var whiteAttacksBitboard = whiteAttacksBitboard
    whiteAttacksBitboard = whiteRookAttacks
            .values
            .stream()
            .reduce(whiteAttacksBitboard) { a: Long, b: Long -> a or b }
    return whiteAttacksBitboard
}

fun rookAttackMap(board: EngineChessBoard, whiteRookSquares: List<Int>) =
    whiteRookSquares.stream()
            .collect(Collectors.toMap<Int, Int, Long>(Function.identity(), Function { rs: Int -> rookAttacks(board, rs) }))


fun sameFile(square1: Int, square2: Int) = square1 % 8 == square2 % 8

fun doubledRooksEval(squares: List<Int>) =
        if (squares.size > 1 && sameFile(squares.get(0), squares.get(1)))
            Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else
            if (squares.size > 2 && ( sameFile(squares.get(0), squares.get(2)) || sameFile(squares.get(1), squares.get(2))))
                Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else 0

fun whiteRookPieceSquareSum(rookSquares: List<Int>) : Int =
        rookSquares.stream().map(PieceSquareTables.rook::get).reduce(0, Integer::sum)

fun blackRookPieceSquareSum(rookSquares: List<Int>) : Int =
        rookSquares.stream().map(
                        {
                            s -> PieceSquareTables.rook.get(Bitboards.bitFlippedHorizontalAxis.get(s))
                        })
                .reduce(0, Integer::sum)
