package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.bitboards.util.getBlackPawnAttacks
import com.netsensia.rivalchess.bitboards.util.getWhitePawnAttacks
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.EngineChessBoard
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import java.lang.Long.bitCount
import java.util.function.Function
import java.util.stream.Collectors

fun onlyKingRemains(board: EngineChessBoard) =
        board.getWhitePieceValues() +
        board.getBlackPieceValues() +
        board.getWhitePawnValues() +
        board.getBlackPawnValues() == 0

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

fun rookEnemyPawnMultiplier(enemyPawnValues: Int) =
        Math.min(enemyPawnValues / PieceValue.getValue(Piece.PAWN), 6)

fun combineAttacks(attackMap: Map<Int, Long>) =
    attackMap.values.stream().reduce(0) { a: Long, b: Long -> a or b }

fun knightAttackMap(squares: List<Int>) =
        squares.stream()
                .collect(Collectors.toMap<Int, Int, Long>(
                        Function.identity(), Function { s: Int -> Bitboards.knightMoves.get(s) }))

fun rookAttacks(board: EngineChessBoard, sq: Int) : Long =
        Bitboards.magicBitboards.magicMovesRook[sq][
                ((board.getAllPiecesBitboard() and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq]
                        ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun rookAttackMap(board: EngineChessBoard, whiteRookSquares: List<Int>) =
    whiteRookSquares.stream()
            .collect(Collectors.toMap<Int, Int, Long>(
                    Function.identity(), Function { rs: Int -> rookAttacks(board, rs) })
            )

fun bishopAttacks(board: EngineChessBoard, sq: Int) =
        Bitboards.magicBitboards.magicMovesBishop[sq][
                ((board.getAllPiecesBitboard() and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq]
                        ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun bishopAttackMap(board: EngineChessBoard, whiteBishopSquares: List<Int>) =
        whiteBishopSquares.stream()
                .collect(Collectors.toMap<Int, Int, Long>(
                        Function.identity(), Function { rs: Int -> bishopAttacks(board, rs) })
                )

fun queenAttacks(board: EngineChessBoard, sq: Int) = rookAttacks(board, sq) or bishopAttacks(board, sq)

fun queenAttackMap(board: EngineChessBoard, whiteQueenSquares: List<Int>) =
        whiteQueenSquares.stream()
                .collect(Collectors.toMap<Int, Int, Long>(
                        Function.identity(), Function { rs: Int -> queenAttacks(board, rs) })
                )

fun sameFile(square1: Int, square2: Int) = square1 % 8 == square2 % 8

fun doubledRooksEval(squares: List<Int>) =
        if (squares.size > 1 && sameFile(squares.get(0), squares.get(1)))
            Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else
            if (squares.size > 2 && ( sameFile(squares.get(0), squares.get(2)) || sameFile(squares.get(1), squares.get(2))))
                Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else 0

fun whiteRookPieceSquareSum(rookSquares: List<Int>) : Int =
        rookSquares.stream().map(PieceSquareTables.rook::get).reduce(0, Integer::sum)

fun whiteQueenPieceSquareSum(squares: List<Int>) : Int =
        squares.stream().map(PieceSquareTables.queen::get).reduce(0, Integer::sum)

fun blackRookPieceSquareSum(rookSquares: List<Int>): Int =
        rookSquares.stream().map { s ->
                    PieceSquareTables.rook.get(Bitboards.bitFlippedHorizontalAxis.get(s))
                }
                .reduce(0, Integer::sum)

fun blackQueenPieceSquareSum(squares: List<Int>): Int =
        squares.stream().map { s ->
                    PieceSquareTables.queen.get(Bitboards.bitFlippedHorizontalAxis.get(s))
                }
                .reduce(0, Integer::sum)

fun kingAttackCount(dangerZone: Long, attacks: Map<Int, Long>): Int {
    return attacks.entries.stream()
            .map { s: Map.Entry<Int, Long> -> bitCount(s.value and dangerZone) }
            .reduce(0, Integer::sum)
}

fun whiteEvaluation(board: EngineChessBoard) : Int {
    val whiteRookSquares = squareList(board.getBitboard(BitboardType.WR))
    val whiteRookAttacks = rookAttackMap(board, whiteRookSquares)
    val whitePieces = board.getBitboard(if (board.mover == Colour.WHITE) BitboardType.FRIENDLY else BitboardType.ENEMY)
    val whiteKnightSquares = squareList(board.getBitboard(BitboardType.WN))
    val blackPawnAttacks = getBlackPawnAttacks(board.blackPawnBitboard)
    val whiteQueenSquares = squareList(board.getBitboard(BitboardType.WQ))
    val whiteQueenAttacks = queenAttackMap(board, whiteQueenSquares)

    return whitePawnPieceSquareEval(board) +
            whiteKingSquareEval(board) +
            whiteQueenPieceSquareSum(whiteQueenSquares) +
            twoWhiteRooksTrappingKingEval(board) +
            doubledRooksEval(whiteRookSquares) +
            whiteRookSquares.stream().map { s: Int -> whiteRookOpenFilesEval(board, s % 8) }.reduce(0, Integer::sum) +
            whiteRookSquares.stream().map { s: Int ->
                Evaluation.getRookMobilityValue(
                        bitCount(whiteRookAttacks[s]!! and whitePieces.inv()))
            }.reduce(0, Integer::sum) +
            whiteQueenSquares.stream().map { s: Int ->
                Evaluation.getQueenMobilityValue(
                        bitCount(whiteQueenAttacks[s]!! and whitePieces.inv()))
            }.reduce(0, Integer::sum) +
            (whiteRookPieceSquareSum(whiteRookSquares) * rookEnemyPawnMultiplier(board.getBlackPawnValues()) / 6) -
            whiteKnightSquares.stream()
                    .map { s: Int ->
                        bitCount(Bitboards.knightMoves[s] and
                                (blackPawnAttacks or board.whitePawnBitboard)) *
                                Evaluation.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY.value
                    }
                    .reduce(0, Integer::sum) +
            linearScale(board.blackPieceValues + board.blackPawnValues,
                    Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                    Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                    whiteKnightSquares.stream().map { i: Int -> PieceSquareTables.knightEndGame[i] }.reduce(0, Integer::sum),
                    whiteKnightSquares.stream().map { i: Int -> PieceSquareTables.knight[i] }.reduce(0, Integer::sum)
            )
}

fun blackEvaluation(board: EngineChessBoard) : Int {
    val blackRookSquares = squareList(board.getBitboard(BitboardType.BR))
    val blackRookAttacks = rookAttackMap(board, blackRookSquares)
    val blackPieces = board.getBitboard(if (board.mover == Colour.WHITE) BitboardType.ENEMY else BitboardType.FRIENDLY)
    val blackKnightSquares = squareList(board.getBitboard(BitboardType.BN))
    val whitePawnAttacks = getWhitePawnAttacks(board.whitePawnBitboard)
    val blackQueenSquares = squareList(board.getBitboard(BitboardType.BQ))
    val blackQueenAttacks = queenAttackMap(board, blackQueenSquares)

    return blackPawnPieceSquareEval(board) +
            blackKingSquareEval(board) +
            blackQueenPieceSquareSum(blackQueenSquares) +
            twoBlackRooksTrappingKingEval(board) +
            doubledRooksEval(blackRookSquares) +
            blackRookSquares.stream().map { s: Int -> blackRookOpenFilesEval(board, s % 8) }.reduce(0, Integer::sum) +
            blackRookSquares.stream().map { s: Int ->
                Evaluation.getRookMobilityValue(
                        bitCount(blackRookAttacks[s]!! and blackPieces.inv()))
            }.reduce(0, Integer::sum) +
            blackQueenSquares.stream().map { s: Int ->
                Evaluation.getQueenMobilityValue(
                        bitCount(blackQueenAttacks[s]!! and blackPieces.inv()))
            }.reduce(0, Integer::sum) +
            (blackRookPieceSquareSum(blackRookSquares) * rookEnemyPawnMultiplier(board.getWhitePawnValues()) / 6) -
            blackKnightSquares.stream()
                    .map { s: Int ->
                        bitCount(Bitboards.knightMoves[s] and
                                (whitePawnAttacks or board.blackPawnBitboard)) *
                                Evaluation.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY.value
                    }
                    .reduce(0, Integer::sum) +
            linearScale(
                    board.whitePieceValues + board.whitePawnValues,
                    Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                    Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                    blackKnightSquares.stream().map { s: Int -> PieceSquareTables.knightEndGame[Bitboards.bitFlippedHorizontalAxis[s]] }.reduce(0, Integer::sum),
                    blackKnightSquares.stream().map { s: Int -> PieceSquareTables.knight[Bitboards.bitFlippedHorizontalAxis[s]] }.reduce(0, Integer::sum)
            )

}
