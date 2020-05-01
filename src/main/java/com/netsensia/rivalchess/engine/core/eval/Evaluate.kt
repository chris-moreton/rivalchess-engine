package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType
import com.netsensia.rivalchess.bitboards.Bitboards
import com.netsensia.rivalchess.bitboards.MagicBitboards
import com.netsensia.rivalchess.bitboards.util.getBlackPawnAttacks
import com.netsensia.rivalchess.bitboards.util.getWhitePawnAttacks
import com.netsensia.rivalchess.bitboards.util.southFill
import com.netsensia.rivalchess.bitboards.util.squareList
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.EngineChessBoard
import com.netsensia.rivalchess.engine.core.Evaluate
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.Square
import com.netsensia.rivalchess.model.SquareOccupant
import java.lang.Long.bitCount
import java.util.function.Function
import java.util.stream.Collectors

fun onlyKingsRemain(board: EngineChessBoard) =
        board.whitePieceValues +
        board.blackPieceValues +
        board.whitePawnValues +
        board.blackPawnValues == 0

fun whitePawnPieceSquareEval(board: EngineChessBoard) : Int {

    val pawnSquares = squareList(board.getBitboard(BitboardType.WP))

    return linearScale(
            board.blackPieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pawnSquares.stream().map(PieceSquareTables.pawnEndGame::get).reduce(0, Integer::sum),
            pawnSquares.stream().map(PieceSquareTables.pawn::get).reduce(0, Integer::sum)
    )
}

fun blackPawnPieceSquareEval(board: EngineChessBoard) : Int {

    val pawnSquares = squareList(board.getBitboard(BitboardType.BP))

    return linearScale(
            board.whitePieceValues,
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pawnSquares.stream().map { PieceSquareTables.pawnEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(it)) }
                    .reduce(0, Integer::sum),
            pawnSquares.stream().map { PieceSquareTables.pawn.get(Bitboards.bitFlippedHorizontalAxis.get(it)) }
                    .reduce(0, Integer::sum)
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
                        Function.identity(), Function { bishopAttacks(board, it) })
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

fun whiteBishopPieceSquareSum(rookSquares: List<Int>) : Int =
        rookSquares.stream().map(PieceSquareTables.bishop::get).reduce(0, Integer::sum)

fun whiteQueenPieceSquareSum(squares: List<Int>) : Int =
        squares.stream().map(PieceSquareTables.queen::get).reduce(0, Integer::sum)

fun blackRookPieceSquareSum(rookSquares: List<Int>): Int =
        rookSquares.stream().map { s ->
                    PieceSquareTables.rook.get(Bitboards.bitFlippedHorizontalAxis.get(s))
                }
                .reduce(0, Integer::sum)

fun blackBishopPieceSquareSum(rookSquares: List<Int>): Int =
        rookSquares.stream().map { s ->
                    PieceSquareTables.bishop.get(Bitboards.bitFlippedHorizontalAxis.get(s))
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

fun tradePieceBonusWhenMoreMaterial(board: EngineChessBoard, materialDifference: Int): Int {
    return linearScale(
            if (materialDifference > 0) board.blackPieceValues + board.blackPawnValues else board.whitePieceValues + board.whitePawnValues,
            0,
            Evaluation.TOTAL_PIECE_VALUE_PER_SIDE_AT_START.value,
            30 * materialDifference / 100,
            0)
}

fun tradePawnBonusWhenMoreMaterial(board: EngineChessBoard, materialDifference: Int): Int {
    return linearScale(
            if (materialDifference > 0) board.whitePawnValues else board.blackPawnValues,
            0,
            Evaluation.TRADE_BONUS_UPPER_PAWNS.value,
            -30 * materialDifference / 100,
            0)
}

fun bishopScore(board: EngineChessBoard, materialDifference: Int) =
    bishopPairEval(board) + oppositeColourBishopsEval(board, materialDifference) + trappedBishopEval(board)

fun whiteLightBishopExists(board: EngineChessBoard) = board.whiteBishopBitboard and Bitboards.LIGHT_SQUARES != 0L
fun whiteDarkBishopExists(board: EngineChessBoard) = board.whiteBishopBitboard and Bitboards.DARK_SQUARES != 0L
fun blackLightBishopExists(board: EngineChessBoard) = board.blackBishopBitboard and Bitboards.LIGHT_SQUARES != 0L
fun blackDarkBishopExists(board: EngineChessBoard) = board.blackBishopBitboard and Bitboards.DARK_SQUARES != 0L
fun whiteBishopColourCount(board: EngineChessBoard) = (if (whiteLightBishopExists(board)) 1 else 0) + if (whiteDarkBishopExists(board)) 1 else 0
fun blackBishopColourCount(board: EngineChessBoard) = (if (blackLightBishopExists(board)) 1 else 0) + if (blackDarkBishopExists(board)) 1 else 0

private fun oppositeColourBishopsEval(board: EngineChessBoard, materialDifference: Int): Int {

    if (whiteBishopColourCount(board) == 1 && blackBishopColourCount(board) == 1 &&
            whiteLightBishopExists(board) != blackLightBishopExists(board) &&
            board.whitePieceValues == board.blackPieceValues) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / Evaluation.WRONG_COLOUR_BISHOP_PENALTY_DIVISOR.value // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScale(
                board.whitePieceValues + board.blackPieceValues,
                Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_LOW.value,
                Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_HIGH.value,
                maxPenalty,
                0)
    }
    return 0
}

private fun bishopPairEval(board: EngineChessBoard) = (if (whiteBishopColourCount(board) == 2)
        Evaluation.VALUE_BISHOP_PAIR.value +
                (8 - board.whitePawnValues / PieceValue.getValue(Piece.PAWN)) *
                Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
        else 0) -
        if (blackBishopColourCount(board) == 2)
        Evaluation.VALUE_BISHOP_PAIR.value +
                (8 - board.blackPawnValues / PieceValue.getValue(Piece.PAWN)) *
                Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
         else 0

private fun trappedBishopEval(board: EngineChessBoard) =
    if (board.whiteBishopBitboard or board.blackBishopBitboard and Bitboards.A2A7H2H7 != 0L)
         blackA2PawnTrappedEval(board) +
         blackH2PawnTrappedEval(board) -
         whiteA7PawnTrappedEval(board) -
         whiteH7PawnTrappedEval(board)
    else 0

private fun blackH2PawnTrappedEval(board: EngineChessBoard): Int {
    val blackH2 = if (board.blackBishopBitboard and (1L shl Square.H2.bitRef) != 0L && board.whitePawnBitboard and (1L shl Square.G3.bitRef) != 0L && board.whitePawnBitboard and (1L shl Square.F2.bitRef) != 0L)
        if (board.blackQueenBitboard == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value else 0
    return blackH2
}

private fun blackA2PawnTrappedEval(board: EngineChessBoard): Int {
    val blackA2 = if (board.blackBishopBitboard and (1L shl Square.A2.bitRef) != 0L && board.whitePawnBitboard and (1L shl Square.B3.bitRef) != 0L && board.whitePawnBitboard and (1L shl Square.C2.bitRef) != 0L)
        Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else 0
    return blackA2
}

private fun whiteH7PawnTrappedEval(board: EngineChessBoard): Int {
    val whiteH7 = if (board.whiteBishopBitboard and (1L shl Square.H7.bitRef) != 0L && board.blackPawnBitboard and (1L shl Square.G6.bitRef) != 0L && board.blackPawnBitboard and (1L shl Square.F7.bitRef) != 0L)
        if (board.whiteQueenBitboard == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value else 0
    return whiteH7
}

private fun whiteA7PawnTrappedEval(board: EngineChessBoard): Int {
    val whiteA7 = if (board.whiteBishopBitboard and (1L shl Square.A7.bitRef) != 0L && board.blackPawnBitboard and (1L shl Square.B6.bitRef) != 0L && board.blackPawnBitboard and (1L shl Square.C7.bitRef) != 0L)
        Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else 0
    return whiteA7
}

fun whiteAttacksBitboard(board: EngineChessBoard) =
        ((combineAttacks(rookAttackMap(board, squareList(board.getBitboard(BitboardType.WR)))) or
            combineAttacks(queenAttackMap(board, squareList(board.getBitboard(BitboardType.WQ)))) or
            combineAttacks(bishopAttackMap(board, squareList(board.getBitboard(BitboardType.WB)))) or
            combineAttacks(knightAttackMap(squareList(board.getBitboard(BitboardType.WN))))) and
            (board.blackKnightBitboard or board.blackRookBitboard or board.blackQueenBitboard or board.blackBishopBitboard)) or
            getWhitePawnAttacks(board.whitePawnBitboard)

fun blackAttacksBitboard(board: EngineChessBoard) =
        ((combineAttacks(rookAttackMap(board, squareList(board.getBitboard(BitboardType.BR)))) or
            combineAttacks(queenAttackMap(board, squareList(board.getBitboard(BitboardType.BQ)))) or
            combineAttacks(bishopAttackMap(board, squareList(board.getBitboard(BitboardType.BB)))) or
            combineAttacks(knightAttackMap(squareList(board.getBitboard(BitboardType.BN))))) and
            (board.whiteKnightBitboard or board.whiteRookBitboard or board.whiteQueenBitboard or board.whiteBishopBitboard)) or
            getBlackPawnAttacks(board.blackPawnBitboard)

fun threatEval(board: EngineChessBoard, whiteAttacksBitboard: Long, blackAttacksBitboard: Long): Int {

    val whiteAttackScore = squareList(whiteAttacksBitboard)
            .stream()
            .map {
                when (board.getSquareOccupant(it)) {
                    SquareOccupant.BP -> PieceValue.getValue(Piece.PAWN)
                    SquareOccupant.BB -> PieceValue.getValue(Piece.BISHOP)
                    SquareOccupant.BR -> PieceValue.getValue(Piece.ROOK)
                    SquareOccupant.BQ -> PieceValue.getValue(Piece.QUEEN)
                    SquareOccupant.BN -> PieceValue.getValue(Piece.KNIGHT)
                    else -> 0
                }
            }
            .reduce(0, Integer::sum)

    val blackAttackScore = squareList(blackAttacksBitboard)
            .stream()
            .map {
                when (board.getSquareOccupant(it)) {
                    SquareOccupant.WP -> PieceValue.getValue(Piece.PAWN)
                    SquareOccupant.WB -> PieceValue.getValue(Piece.BISHOP)
                    SquareOccupant.WR -> PieceValue.getValue(Piece.ROOK)
                    SquareOccupant.WQ -> PieceValue.getValue(Piece.QUEEN)
                    SquareOccupant.WN -> PieceValue.getValue(Piece.KNIGHT)
                    else -> 0
                }
            }
            .reduce(0, Integer::sum)

    val whiteAdjustedScore = whiteAttackScore + whiteAttackScore * (whiteAttackScore / PieceValue.getValue(Piece.QUEEN))
    val blackAdjustedScore = blackAttackScore + blackAttackScore * (blackAttackScore / PieceValue.getValue(Piece.QUEEN))

    return (whiteAdjustedScore - blackAdjustedScore) / Evaluation.THREAT_SCORE_DIVISOR.value
}

fun isEndGame(board: EngineChessBoard) =
        (board.whitePieceValues +
            board.whitePawnValues +
            board.blackPieceValues +
            board.blackPawnValues) <= Evaluation.EVAL_ENDGAME_TOTAL_PIECES.getValue()

fun kingSafetyEval(board: EngineChessBoard, blackKingAttackedCount: Int, whiteKingAttackedCount: Int): Int {
    val averagePiecesPerSide = (board.whitePieceValues + board.blackPieceValues) / 2

    if (averagePiecesPerSide <= Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.value) {
        return 0
    }

    val whiteKingSafety: Int = Evaluate.getWhiteKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + whiteKingShieldEval(board)

    val blackKingSafety: Int = Evaluate.getBlackKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + blackKingShieldEval(board)

    return linearScale(
                averagePiecesPerSide,
                Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.value,
                Evaluation.KINGSAFETY_MAX_PIECE_BALANCE.value,
                0,
                whiteKingSafety -
                        blackKingSafety +
                        (blackKingAttackedCount - whiteKingAttackedCount) *
                        Evaluation.KINGSAFETY_ATTACK_MULTIPLIER.value)

}

private fun blackKingShieldEval(board: EngineChessBoard): Int {
    var shieldValue = 0
    if (blackKingOnFirstTwoRanks(board)) {
        with(blackKingShield(board)) {
            shieldValue += (Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.value * bitCount(board.blackPawnBitboard and this)
                    - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.value * bitCount(board.whitePawnBitboard and (this or (this ushr 8)))
                    + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.value * bitCount(board.blackPawnBitboard and (this ushr 8))
                    - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.value * bitCount(board.whitePawnBitboard and (this ushr 16)))
            shieldValue = Math.min(shieldValue, Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value)
            if (board.blackKingBitboard and Bitboards.F8G8 != 0L &&
                    board.blackRookBitboard and Bitboards.G8H8 != 0L &&
                    board.blackPawnBitboard and Bitboards.FILE_G != 0L &&
                    board.blackPawnBitboard and Bitboards.FILE_H != 0L) {
                shieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
            } else if (board.blackKingBitboard and Bitboards.B8C8 != 0L &&
                    board.blackRookBitboard and Bitboards.A8B8 != 0L &&
                    board.blackPawnBitboard and Bitboards.FILE_A != 0L &&
                    board.blackPawnBitboard and Bitboards.FILE_B != 0L) {
                shieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
            }
            val whiteOpen = southFill(this, 8) and southFill(board.whitePawnBitboard, 8).inv() and Bitboards.RANK_1
            if (whiteOpen != 0L) {
                shieldValue -= (Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(whiteOpen and Bitboards.MIDDLE_FILES_8_BIT)
                        + Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(whiteOpen and Bitboards.NONMID_FILES_8_BIT))
            }
            val blackOpen = southFill(this, 8) and southFill(board.blackPawnBitboard, 8).inv() and Bitboards.RANK_1
            if (blackOpen != 0L) {
                shieldValue -= (Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(blackOpen and Bitboards.MIDDLE_FILES_8_BIT)
                        + Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(blackOpen and Bitboards.NONMID_FILES_8_BIT))
            }
        }
    }
    return shieldValue
}

private fun whiteKingShieldEval(board: EngineChessBoard) : Int {
    var whiteShieldValue = 0;

    if (whiteKingOnFirstTwoRanks(board)) {
        with(whiteKingShield(board)) {
            whiteShieldValue = (Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.value * bitCount(board.whitePawnBitboard and this)
                    - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.value * bitCount(board.blackPawnBitboard and (this or (this shl 8)))
                    + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.value * bitCount(board.whitePawnBitboard and (this shl 8))
                    - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.value * bitCount(board.blackPawnBitboard and (this shl 16)))
            whiteShieldValue = Math.min(whiteShieldValue, Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value)
            if (board.whiteKingBitboard and Bitboards.F1G1 != 0L &&
                    board.whiteRookBitboard and Bitboards.G1H1 != 0L &&
                    board.whitePawnBitboard and Bitboards.FILE_G != 0L &&
                    board.whitePawnBitboard and Bitboards.FILE_H != 0L) {
                whiteShieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
            } else if (board.whiteKingBitboard and Bitboards.B1C1 != 0L &&
                    board.whiteRookBitboard and Bitboards.A1B1 != 0L &&
                    board.whitePawnBitboard and Bitboards.FILE_A != 0L &&
                    board.whitePawnBitboard and Bitboards.FILE_B != 0L) {
                whiteShieldValue -= Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
            }
            val whiteOpen = southFill(this, 8) and southFill(board.whitePawnBitboard, 8).inv() and Bitboards.RANK_1
            if (whiteOpen != 0L) {
                whiteShieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(whiteOpen and Bitboards.MIDDLE_FILES_8_BIT)
                whiteShieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(whiteOpen and Bitboards.NONMID_FILES_8_BIT)
            }
            val blackOpen = southFill(this, 8) and southFill(board.blackPawnBitboard, 8).inv() and Bitboards.RANK_1
            if (blackOpen != 0L) {
                whiteShieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(blackOpen and Bitboards.MIDDLE_FILES_8_BIT)
                whiteShieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(blackOpen and Bitboards.NONMID_FILES_8_BIT)
            }
        }
    }

    return whiteShieldValue
}

private fun whiteKingOnFirstTwoRanks(board: EngineChessBoard) =
        board.whiteKingSquare / 8 < 2

private fun blackKingOnFirstTwoRanks(board: EngineChessBoard) =
        board.blackKingSquare / 8 >= 6

private fun blackKingShield(board: EngineChessBoard) =
        Bitboards.whiteKingShieldMask[board.blackKingSquare % 8] shl 40

private fun whiteKingShield(board: EngineChessBoard) =
        Bitboards.whiteKingShieldMask[board.whiteKingSquare % 8]

fun whiteEvaluation(board: EngineChessBoard) : Int {
    val whiteRookSquares = squareList(board.getBitboard(BitboardType.WR))
    val whiteRookAttacks = rookAttackMap(board, whiteRookSquares)
    val whiteBishopSquares = squareList(board.getBitboard(BitboardType.WB))
    val whiteBishopAttacks = bishopAttackMap(board, whiteBishopSquares)
    val whitePieces = board.getBitboard(if (board.mover == Colour.WHITE) BitboardType.FRIENDLY else BitboardType.ENEMY)
    val whiteKnightSquares = squareList(board.getBitboard(BitboardType.WN))
    val blackPawnAttacks = getBlackPawnAttacks(board.blackPawnBitboard)
    val whiteQueenSquares = squareList(board.getBitboard(BitboardType.WQ))
    val whiteQueenAttacks = queenAttackMap(board, whiteQueenSquares)

    return whitePawnPieceSquareEval(board) +
            whiteKingSquareEval(board) +
            whiteQueenPieceSquareSum(whiteQueenSquares) +
            whiteBishopPieceSquareSum(whiteBishopSquares) +
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
            whiteBishopSquares.stream().map { s: Int ->
                Evaluation.getBishopMobilityValue(
                        bitCount(whiteBishopAttacks[s]!! and whitePieces.inv()))
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
    val blackBishopSquares = squareList(board.getBitboard(BitboardType.BB))
    val blackBishopAttacks = bishopAttackMap(board, blackBishopSquares)
    val blackPieces = board.getBitboard(if (board.mover == Colour.WHITE) BitboardType.ENEMY else BitboardType.FRIENDLY)
    val blackKnightSquares = squareList(board.getBitboard(BitboardType.BN))
    val whitePawnAttacks = getWhitePawnAttacks(board.whitePawnBitboard)
    val blackQueenSquares = squareList(board.getBitboard(BitboardType.BQ))
    val blackQueenAttacks = queenAttackMap(board, blackQueenSquares)

    return blackPawnPieceSquareEval(board) +
            blackKingSquareEval(board) +
            blackQueenPieceSquareSum(blackQueenSquares) +
            blackBishopPieceSquareSum(blackBishopSquares) +
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
            blackBishopSquares.stream().map { s: Int ->
                Evaluation.getBishopMobilityValue(
                        bitCount(blackBishopAttacks[s]!! and blackPieces.inv()))
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

fun castlingEval(board: EngineChessBoard): Int {
    var eval = 0
    val castlePrivs = (board.castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value) +
            (board.castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value) +
            (board.castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value) +
            (board.castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value)
    if (castlePrivs != 0) {
        // Value of moving King to its queenside castle destination in the middle game
        val kingSquareBonusMiddleGame = PieceSquareTables.king[1] - PieceSquareTables.king[3]
        val kingSquareBonusEndGame = PieceSquareTables.kingEndGame[1] - PieceSquareTables.kingEndGame[3]
        val rookSquareBonus = PieceSquareTables.rook[3] - PieceSquareTables.rook[0]
        var kingSquareBonusScaled = linearScale(
                board.blackPieceValues,
                Evaluation.CASTLE_BONUS_LOW_MATERIAL.value,
                Evaluation.CASTLE_BONUS_HIGH_MATERIAL.value,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)

        // don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
        // given by still having castling rights.
        var castleValue = kingSquareBonusScaled + rookSquareBonus
        if (castleValue > 0) {
            var timeToCastleKingSide = 100
            var timeToCastleQueenSide = 100
            if (board.castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value != 0) {
                timeToCastleKingSide = 2
                if (board.allPiecesBitboard and (1L shl 1) != 0L) timeToCastleKingSide++
                if (board.allPiecesBitboard and (1L shl 2) != 0L) timeToCastleKingSide++
            }
            if (board.castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value != 0) {
                timeToCastleQueenSide = 2
                if (board.allPiecesBitboard and (1L shl 6) != 0L) timeToCastleQueenSide++
                if (board.allPiecesBitboard and (1L shl 5) != 0L) timeToCastleQueenSide++
                if (board.allPiecesBitboard and (1L shl 4) != 0L) timeToCastleQueenSide++
            }
            eval += castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide)
        }
        kingSquareBonusScaled = linearScale(
                board.whitePieceValues,
                Evaluation.CASTLE_BONUS_LOW_MATERIAL.value,
                Evaluation.CASTLE_BONUS_HIGH_MATERIAL.value,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)
        castleValue = kingSquareBonusScaled + rookSquareBonus
        if (castleValue > 0) {
            var timeToCastleKingSide = 100
            var timeToCastleQueenSide = 100
            if (board.castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value != 0) {
                timeToCastleKingSide = 2
                if (board.allPiecesBitboard and (1L shl 57) != 0L) timeToCastleKingSide++
                if (board.allPiecesBitboard and (1L shl 58) != 0L) timeToCastleKingSide++
            }
            if (board.castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value != 0) {
                timeToCastleQueenSide = 2
                if (board.allPiecesBitboard and (1L shl 60) != 0L) timeToCastleQueenSide++
                if (board.allPiecesBitboard and (1L shl 61) != 0L) timeToCastleQueenSide++
                if (board.allPiecesBitboard and (1L shl 62) != 0L) timeToCastleQueenSide++
            }
            eval -= castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide)
        }
    }
    return eval
}

fun endGameAdjustment(board: EngineChessBoard, currentScore: Int): Int {
    var eval = currentScore
    if (board.whitePawnValues + board.blackPawnValues == 0 && board.whitePieceValues < PieceValue.getValue(Piece.ROOK) && board.blackPieceValues < PieceValue.getValue(Piece.ROOK)) return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
    if (eval > 0) {
        if (board.whitePawnValues == 0 && (board.whitePieceValues == PieceValue.getValue(Piece.KNIGHT) || board.whitePieceValues == PieceValue.getValue(Piece.BISHOP))) return eval - (board.whitePieceValues * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt() else if (board.whitePawnValues == 0 && board.whitePieceValues - PieceValue.getValue(Piece.BISHOP) <= board.blackPieceValues) return eval / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value else if (bitCount(board.allPiecesBitboard) > 3 && board.whiteRookBitboard or board.whiteKnightBitboard or board.whiteQueenBitboard == 0L) {
            // If this is not yet a KPK ending, and if white has only A pawns and has no dark bishop and the black king is on a8/a7/b8/b7 then this is probably a draw.
            // Do the same for H pawns
            if (board.whitePawnBitboard and Bitboards.FILE_A.inv() == 0L &&
                    board.whiteBishopBitboard and Bitboards.LIGHT_SQUARES == 0L &&
                    board.blackKingBitboard and Bitboards.A8A7B8B7 != 0L || board.whitePawnBitboard and Bitboards.FILE_H.inv() == 0L &&
                    board.whiteBishopBitboard and Bitboards.DARK_SQUARES == 0L &&
                    board.blackKingBitboard and Bitboards.H8H7G8G7 != 0L) {
                return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
            }
        }
        if (board.blackPawnValues == 0) {
            if (board.whitePieceValues - board.blackPieceValues > PieceValue.getValue(Piece.BISHOP)) {
                val whiteKnightCount = bitCount(board.whiteKnightBitboard)
                val whiteBishopCount = bitCount(board.whiteBishopBitboard)
                return if (whiteKnightCount == 2 && board.whitePieceValues == 2 * PieceValue.getValue(Piece.KNIGHT) && board.blackPieceValues == 0) eval / Evaluation.ENDGAME_DRAW_DIVISOR.value else if (whiteKnightCount == 1 && whiteBishopCount == 1 && board.whitePieceValues == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) && board.blackPieceValues == 0) {
                    eval = PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value + eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value
                    val kingSquare = board.blackKingSquare
                    eval += if (board.whiteBishopBitboard and Bitboards.DARK_SQUARES != 0L) (7 - Bitboards.distanceToH1OrA8[Bitboards.bitFlippedHorizontalAxis[kingSquare]]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value else (7 - Bitboards.distanceToH1OrA8[kingSquare]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value
                    eval
                } else eval + Evaluation.VALUE_SHOULD_WIN.value
            }
        }
    }
    if (eval < 0) {
        if (board.blackPawnValues == 0 && (board.blackPieceValues == PieceValue.getValue(Piece.KNIGHT) || board.blackPieceValues == PieceValue.getValue(Piece.BISHOP))) return eval + (board.blackPieceValues * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt() else if (board.blackPawnValues == 0 && board.blackPieceValues - PieceValue.getValue(Piece.BISHOP) <= board.whitePieceValues) return eval / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value else if (bitCount(board.allPiecesBitboard) > 3 && board.blackRookBitboard or board.blackKnightBitboard or board.blackQueenBitboard == 0L) {
            if (board.blackPawnBitboard and Bitboards.FILE_A.inv() == 0L &&
                    board.blackBishopBitboard and Bitboards.DARK_SQUARES == 0L &&
                    board.whiteKingBitboard and Bitboards.A1A2B1B2 != 0L) return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value else if (board.blackPawnBitboard and Bitboards.FILE_H.inv() == 0L &&
                    board.blackBishopBitboard and Bitboards.LIGHT_SQUARES == 0L &&
                    board.whiteKingBitboard and Bitboards.H1H2G1G2 != 0L) return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
        }
        if (board.whitePawnValues == 0) {
            if (board.blackPieceValues - board.whitePieceValues > PieceValue.getValue(Piece.BISHOP)) {
                val blackKnightCount = bitCount(board.blackKnightBitboard)
                val blackBishopCount = bitCount(board.blackBishopBitboard)
                return if (blackKnightCount == 2 && board.blackPieceValues == 2 * PieceValue.getValue(Piece.KNIGHT) && board.whitePieceValues == 0) eval / Evaluation.ENDGAME_DRAW_DIVISOR.value else if (blackKnightCount == 1 && blackBishopCount == 1 && board.blackPieceValues == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) && board.whitePieceValues == 0) {
                    eval = -(PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value) + eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value
                    val kingSquare = board.whiteKingSquare
                    eval -= if (board.blackBishopBitboard and Bitboards.DARK_SQUARES != 0L) {
                        (7 - Bitboards.distanceToH1OrA8[Bitboards.bitFlippedHorizontalAxis[kingSquare]]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value
                    } else {
                        (7 - Bitboards.distanceToH1OrA8[kingSquare]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value
                    }
                    eval
                } else eval - Evaluation.VALUE_SHOULD_WIN.value
            }
        }
    }
    return eval
}

fun evaluate(board: EngineChessBoard) =
    if (onlyKingsRemain(board)) {
        0
    } else {
        val whiteKingDangerZone = Bitboards.kingMoves[board.whiteKingSquare] or (Bitboards.kingMoves[board.whiteKingSquare] shl 8)
        val blackKingDangerZone = Bitboards.kingMoves[board.blackKingSquare] or (Bitboards.kingMoves[board.blackKingSquare] ushr 8)
        val whiteRookSquares = squareList(board.getBitboard(BitboardType.WR))
        val whiteRookAttacks = rookAttackMap(board, whiteRookSquares)
        val blackRookSquares = squareList(board.getBitboard(BitboardType.BR))
        val blackRookAttacks = rookAttackMap(board, blackRookSquares)
        val whiteQueenSquares = squareList(board.getBitboard(BitboardType.WQ))
        val whiteQueenAttacks = queenAttackMap(board, whiteQueenSquares)
        val blackQueenSquares = squareList(board.getBitboard(BitboardType.BQ))
        val blackQueenAttacks = queenAttackMap(board, blackQueenSquares)
        val whiteBishopSquares = squareList(board.getBitboard(BitboardType.WB))
        val whiteBishopAttacks = bishopAttackMap(board, whiteBishopSquares)
        val blackBishopSquares = squareList(board.getBitboard(BitboardType.BB))
        val blackBishopAttacks = bishopAttackMap(board, blackBishopSquares)
        val materialDifference = materialDifference(board)
        val blackKingAttackedCount = kingAttackCount(blackKingDangerZone, whiteRookAttacks) + kingAttackCount(blackKingDangerZone, whiteQueenAttacks) * 2 +
                kingAttackCount(blackKingDangerZone, whiteBishopAttacks)
        val whiteKingAttackedCount = kingAttackCount(whiteKingDangerZone, blackRookAttacks) + kingAttackCount(whiteKingDangerZone, blackQueenAttacks) * 2 +
                kingAttackCount(whiteKingDangerZone, blackBishopAttacks)
        val whiteAttacksBitboard = whiteAttacksBitboard(board)
        val blackAttacksBitboard = blackAttacksBitboard(board)

        val eval: Int = (materialDifference + whiteEvaluation(board) - blackEvaluation(board) +
                board.getBoardHashObject().getPawnHashEntry(board).getPawnScore() +
                tradePawnBonusWhenMoreMaterial(board, materialDifference) +
                tradePieceBonusWhenMoreMaterial(board, materialDifference) + castlingEval(board) +
                bishopScore(board, materialDifference)
                + threatEval(board, whiteAttacksBitboard, blackAttacksBitboard)
                + kingSafetyEval(board, blackKingAttackedCount, whiteKingAttackedCount))

        val endGameAdjustedScore = if (isEndGame(board)) endGameAdjustment(board, eval) else eval

        if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore
    }
