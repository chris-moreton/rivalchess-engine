package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.BitboardType.*
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
import java.lang.Long.numberOfTrailingZeros
import java.util.function.Function
import java.util.stream.Collectors

data class BitboardData(
        val WP: Long = 0L,
        val WB: Long = 0L,
        val WN: Long = 0L,
        val WK: Long = 0L,
        val WQ: Long = 0L,
        val WR: Long = 0L,
        val BP: Long = 0L,
        val BB: Long = 0L,
        val BN: Long = 0L,
        val BK: Long = 0L,
        val BQ: Long = 0L,
        val BR: Long = 0L,
        val ENEMY: Long = 0L,
        val FRIENDLY: Long = 0L,
        val ALL: Long = 0L,
        val ENPASSANTSQUARE: Long = 0L
)

fun onlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L

fun onlyKingsRemain(bitboards: BitboardData) = onlyOneBitSet(bitboards.ENEMY) and onlyOneBitSet(bitboards.FRIENDLY)

fun whitePawnPieceSquareEval(bitboards: BitboardData) : Int {

    val pawnSquares = squareList(bitboards.WP)

    return linearScale(
            blackPieceValues(bitboards),
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pawnSquares.stream().map(PieceSquareTables.pawnEndGame::get).reduce(0, Integer::sum),
            pawnSquares.stream().map(PieceSquareTables.pawn::get).reduce(0, Integer::sum)
    )
}

fun blackPawnPieceSquareEval(bitboards: BitboardData) : Int {

    val pawnSquares = squareList(bitboards.BP)

    return linearScale(
            whitePieceValues(bitboards),
            Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
            Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
            pawnSquares.stream().map { PieceSquareTables.pawnEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(it)) }
                    .reduce(0, Integer::sum),
            pawnSquares.stream().map { PieceSquareTables.pawn.get(Bitboards.bitFlippedHorizontalAxis.get(it)) }
                    .reduce(0, Integer::sum)
    )
}

fun whiteKingSquareEval(bitboards: BitboardData) =
        linearScale(
                blackPieceValues(bitboards),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                PieceSquareTables.kingEndGame.get(whiteKingSquare(bitboards)),
                PieceSquareTables.king.get(whiteKingSquare(bitboards))
        )

fun blackKingSquareEval(bitboards: BitboardData) =
        linearScale(
                whitePieceValues(bitboards),
                PieceValue.getValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.getValue(),
                PieceSquareTables.kingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(blackKingSquare(bitboards))),
                PieceSquareTables.king.get(Bitboards.bitFlippedHorizontalAxis.get(blackKingSquare(bitboards)))
        )

fun linearScale(situation: Int, ref1: Int, ref2: Int, score1: Int, score2: Int) =
    if (situation < ref1) score1
    else if (situation > ref2) score2
    else (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1

fun materialDifference(bitboards: BitboardData) =
                whitePieceValues(bitboards) -
                blackPieceValues(bitboards) +
                whitePawnValues(bitboards) -
                blackPawnValues(bitboards)

fun twoWhiteRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.WR and Bitboards.RANK_7) > 1
                && bitboards.BK and Bitboards.RANK_8 != 0L)
            Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.getValue() else 0

fun twoBlackRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.BR and Bitboards.RANK_2) > 1
                && bitboards.WK and Bitboards.RANK_1 != 0L)
            Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.getValue() else 0

fun whiteRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if (Bitboards.FILES[file] and bitboards.WP == 0L)
            if (Bitboards.FILES[file] and bitboards.BP == 0L)
                Evaluation.VALUE_ROOK_ON_OPEN_FILE.value
            else
                Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.value
        else 0

fun blackRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if ((Bitboards.FILES.get(file) and bitboards.BP) == 0L)
            if ((Bitboards.FILES.get(file) and bitboards.WP) == 0L)
                Evaluation.VALUE_ROOK_ON_OPEN_FILE.getValue()
            else
                Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.getValue()
        else 0

fun rookEnemyPawnMultiplier(enemyPawnValues: Int) =
        Math.min(enemyPawnValues / PieceValue.getValue(Piece.PAWN), 6)

fun orList(list: List<Long>) = list.stream().reduce(0, Long::or)

fun knightAttackList(squares: List<Int>) =
        squares.stream().map { Bitboards.knightMoves.get(it) }.collect(Collectors.toList())

fun rookAttacks(bitboards: BitboardData, sq: Int) : Long =
        Bitboards.magicBitboards.magicMovesRook[sq][
                ((bitboards.ALL and MagicBitboards.occupancyMaskRook[sq])
                        * MagicBitboards.magicNumberRook[sq]
                        ushr MagicBitboards.magicNumberShiftsRook[sq]).toInt()]

fun rookAttackMap(bitboards: BitboardData, whiteRookSquares: List<Int>) =
        whiteRookSquares.stream()
                .collect(Collectors.toMap<Int, Int, Long>(
                        Function.identity(), Function { rookAttacks(bitboards, it) })
                )

fun rookAttackList(bitboards: BitboardData, whiteRookSquares: List<Int>) =
        whiteRookSquares.stream().map { s -> rookAttacks(bitboards, s)}.collect(Collectors.toList())

fun bishopAttacks(bitboards: BitboardData, sq: Int) =
        Bitboards.magicBitboards.magicMovesBishop[sq][
                ((bitboards.ALL and MagicBitboards.occupancyMaskBishop[sq])
                        * MagicBitboards.magicNumberBishop[sq]
                        ushr MagicBitboards.magicNumberShiftsBishop[sq]).toInt()]

fun bishopAttackMap(bitboards: BitboardData, whiteBishopSquares: List<Int>) =
        whiteBishopSquares.stream()
                .collect(Collectors.toMap<Int, Int, Long>(
                        Function.identity(), Function { bishopAttacks(bitboards, it) })
                )

fun bishopAttackList(bitboards: BitboardData, whiteBishopSquares: List<Int>) =
        whiteBishopSquares.stream().map { s -> bishopAttacks(bitboards, s)}.collect(Collectors.toList())

fun queenAttacks(bitboards: BitboardData, sq: Int) = rookAttacks(bitboards, sq) or bishopAttacks(bitboards, sq)

fun queenAttackMap(bitboards: BitboardData, whiteQueenSquares: List<Int>) =
        whiteQueenSquares.stream()
                .collect(Collectors.toMap<Int, Int, Long>(
                        Function.identity(), Function { rs: Int -> queenAttacks(bitboards, rs) })
                )

fun queenAttackList(bitboards: BitboardData, whiteQueenSquares: List<Int>) =
        whiteQueenSquares.stream().map { s -> queenAttacks(bitboards, s)}.collect(Collectors.toList())

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

fun whiteQueenPieceSquareSum(whiteQueenSquares: List<Int>) : Int =
        whiteQueenSquares.stream().map(PieceSquareTables.queen::get).reduce(0, Integer::sum)

fun flippedSquareTableScore(table: List<Int>, bit: Int) = table.get(Bitboards.bitFlippedHorizontalAxis.get(bit))

fun blackRookPieceSquareSum(rookSquares: List<Int>): Int =
        rookSquares.stream().map {flippedSquareTableScore(PieceSquareTables.rook, it)}.reduce(0, Integer::sum)

fun blackBishopPieceSquareSum(bishopSquares: List<Int>): Int =
        bishopSquares.stream().map {flippedSquareTableScore(PieceSquareTables.bishop, it)}.reduce(0, Integer::sum)

fun blackQueenPieceSquareSum(queenSquares: List<Int>): Int =
        queenSquares.stream().map {flippedSquareTableScore(PieceSquareTables.queen, it)}.reduce(0, Integer::sum)

fun kingAttackCount(dangerZone: Long, attacks: Map<Int, Long>): Int {
    return attacks.entries.stream()
            .map { s: Map.Entry<Int, Long> -> bitCount(s.value and dangerZone) }
            .reduce(0, Integer::sum)
}

fun tradePieceBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int): Int {
    return linearScale(
            if (materialDifference > 0)
                    blackPieceValues(bitboards) +
                    blackPawnValues(bitboards) else
                    whitePieceValues(bitboards) +
                    whitePawnValues(bitboards),
            0,
            Evaluation.TOTAL_PIECE_VALUE_PER_SIDE_AT_START.value,
            30 * materialDifference / 100,
            0)
}

fun tradePawnBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int): Int {
    return linearScale(
            if (materialDifference > 0) whitePawnValues(bitboards) else blackPawnValues(bitboards),
            0,
            Evaluation.TRADE_BONUS_UPPER_PAWNS.value,
            -30 * materialDifference / 100,
            0)
}

fun bishopScore(bitboards: BitboardData, materialDifference: Int) =
    bishopPairEval(bitboards) + oppositeColourBishopsEval(bitboards, materialDifference) + trappedBishopEval(bitboards)

fun whiteLightBishopExists(bitboards: BitboardData) = bitboards.WB and Bitboards.LIGHT_SQUARES != 0L

fun whiteDarkBishopExists(bitboards: BitboardData) = bitboards.WB and Bitboards.DARK_SQUARES != 0L

fun blackLightBishopExists(bitboards: BitboardData) = bitboards.BB and Bitboards.LIGHT_SQUARES != 0L

fun blackDarkBishopExists(bitboards: BitboardData) = bitboards.BB and Bitboards.DARK_SQUARES != 0L

fun whiteBishopColourCount(bitboards: BitboardData) =
        (if (whiteLightBishopExists(bitboards)) 1 else 0) + if (whiteDarkBishopExists(bitboards)) 1 else 0

fun blackBishopColourCount(bitboards: BitboardData) =
        (if (blackLightBishopExists(bitboards)) 1 else 0) + if (blackDarkBishopExists(bitboards)) 1 else 0

private fun oppositeColourBishopsEval(bitboards: BitboardData, materialDifference: Int): Int {

    if (whiteBishopColourCount(bitboards) == 1 && blackBishopColourCount(bitboards) == 1 &&
            whiteLightBishopExists(bitboards) != blackLightBishopExists(bitboards) &&
            whitePieceValues(bitboards) == blackPieceValues(bitboards)) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / Evaluation.WRONG_COLOUR_BISHOP_PENALTY_DIVISOR.value // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScale(
                whitePieceValues(bitboards) + blackPieceValues(bitboards),
                Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_LOW.value,
                Evaluation.WRONG_COLOUR_BISHOP_MATERIAL_HIGH.value,
                maxPenalty,
                0)
    }
    return 0
}

private fun bishopPairEval(bitboards: BitboardData) = (if (whiteBishopColourCount(bitboards) == 2)
        Evaluation.VALUE_BISHOP_PAIR.value +
                (8 - whitePawnValues(bitboards) / PieceValue.getValue(Piece.PAWN)) *
                Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
        else 0) -
        if (blackBishopColourCount(bitboards) == 2)
        Evaluation.VALUE_BISHOP_PAIR.value +
                (8 - blackPawnValues(bitboards) / PieceValue.getValue(Piece.PAWN)) *
                Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
         else 0

private fun trappedBishopEval(bitboards: BitboardData) =
    if (bitboards.WB or bitboards.BB and Bitboards.A2A7H2H7 != 0L)
         blackA2PawnTrappedEval(bitboards) +
         blackH2PawnTrappedEval(bitboards) -
         whiteA7PawnTrappedEval(bitboards) -
         whiteH7PawnTrappedEval(bitboards)
    else 0

private fun blackH2PawnTrappedEval(bitboards: BitboardData): Int {
    val blackH2 = if (bitboards.BB and (1L shl Square.H2.bitRef) != 0L && bitboards.WP and (1L shl Square.G3.bitRef) != 0L && bitboards.WP and (1L shl Square.F2.bitRef) != 0L)
        if (bitboards.BQ == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value else 0
    return blackH2
}

private fun blackA2PawnTrappedEval(bitboards: BitboardData): Int {
    val blackA2 = if (bitboards.BB and (1L shl Square.A2.bitRef) != 0L && bitboards.WP and (1L shl Square.B3.bitRef) != 0L && bitboards.WP and (1L shl Square.C2.bitRef) != 0L)
        Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else 0
    return blackA2
}

private fun whiteH7PawnTrappedEval(bitboards: BitboardData): Int {
    val whiteH7 = if (bitboards.WB and (1L shl Square.H7.bitRef) != 0L && bitboards.BP and (1L shl Square.G6.bitRef) != 0L && bitboards.BP and (1L shl Square.F7.bitRef) != 0L)
        if (bitboards.WQ == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value else 0
    return whiteH7
}

private fun whiteA7PawnTrappedEval(bitboards: BitboardData): Int {
    val whiteA7 = if (bitboards.WB and (1L shl Square.A7.bitRef) != 0L && bitboards.BP and (1L shl Square.B6.bitRef) != 0L && bitboards.BP and (1L shl Square.C7.bitRef) != 0L)
        Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value else 0
    return whiteA7
}

fun whiteAttacksBitboard(bitboards: BitboardData) =
        ((orList(rookAttackList(bitboards, squareList(bitboards.WR))) or
                orList(queenAttackList(bitboards, squareList(bitboards.WQ))) or
                orList(bishopAttackList(bitboards, squareList(bitboards.WB))) or
                orList(knightAttackList(squareList(bitboards.WN)))) and
            (bitboards.BN or bitboards.BR or bitboards.BQ or bitboards.BB)) or
            getWhitePawnAttacks(bitboards.WP)

fun blackAttacksBitboard(bitboards: BitboardData) =
        ((orList(rookAttackList(bitboards, squareList(bitboards.BR))) or
                orList(queenAttackList(bitboards, squareList(bitboards.BQ))) or
                orList(bishopAttackList(bitboards, squareList(bitboards.BB))) or
                orList(knightAttackList(squareList(bitboards.BN)))) and
            (bitboards.WN or bitboards.WR or bitboards.WQ or bitboards.WB)) or
            getBlackPawnAttacks(bitboards.BP)

fun threatEval(bitboards: BitboardData, board: EngineChessBoard): Int {

    val whiteAttacksBitboard = whiteAttacksBitboard(bitboards)
    val blackAttacksBitboard = blackAttacksBitboard(bitboards)

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

fun isEndGame(bitboards: BitboardData) =
        (whitePieceValues(bitboards) +
            whitePawnValues(bitboards) +
            blackPieceValues(bitboards) +
            blackPawnValues(bitboards)) <= Evaluation.EVAL_ENDGAME_TOTAL_PIECES.getValue()

fun kingSafetyEval(bitboards: BitboardData, board: EngineChessBoard): Int {
    val whiteKingDangerZone = Bitboards.kingMoves[whiteKingSquare(bitboards)] or
            (Bitboards.kingMoves[whiteKingSquare(bitboards)] shl 8)
    val blackKingDangerZone = Bitboards.kingMoves[blackKingSquare(bitboards)] or
            (Bitboards.kingMoves[blackKingSquare(bitboards)] ushr 8)
    val whiteRookSquares = squareList(bitboards.WR)
    val whiteRookAttacks = rookAttackMap(bitboards, whiteRookSquares)
    val blackRookSquares = squareList(bitboards.BR)
    val blackRookAttacks = rookAttackMap(bitboards, blackRookSquares)
    val whiteQueenSquares = squareList(bitboards.WQ)
    val whiteQueenAttacks = queenAttackMap(bitboards, whiteQueenSquares)
    val blackQueenSquares = squareList(bitboards.BQ)
    val blackQueenAttacks = queenAttackMap(bitboards, blackQueenSquares)
    val whiteBishopSquares = squareList(bitboards.WB)
    val whiteBishopAttacks = bishopAttackMap(bitboards, whiteBishopSquares)
    val blackBishopSquares = squareList(bitboards.BB)
    val blackBishopAttacks = bishopAttackMap(bitboards, blackBishopSquares)
    val blackKingAttackedCount = kingAttackCount(blackKingDangerZone, whiteRookAttacks) + kingAttackCount(blackKingDangerZone, whiteQueenAttacks) * 2 +
            kingAttackCount(blackKingDangerZone, whiteBishopAttacks)
    val whiteKingAttackedCount = kingAttackCount(whiteKingDangerZone, blackRookAttacks) + kingAttackCount(whiteKingDangerZone, blackQueenAttacks) * 2 +
            kingAttackCount(whiteKingDangerZone, blackBishopAttacks)

    val averagePiecesPerSide = (whitePieceValues(bitboards) + blackPieceValues(bitboards)) / 2

    if (averagePiecesPerSide <= Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.value) {
        return 0
    }

    val whiteKingSafety: Int = Evaluate.getWhiteKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + whiteKingShieldEval(bitboards)

    val blackKingSafety: Int = Evaluate.getBlackKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + blackKingShieldEval(bitboards)

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

private fun whitePawnShieldEval(bitboards: BitboardData, whitePawnShield: Long) =
        (Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.value * bitCount(bitboards.WP and whitePawnShield)
                - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.value * bitCount(bitboards.BP and (whitePawnShield or (whitePawnShield shl 8)))
                + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.value * bitCount(bitboards.WP and (whitePawnShield shl 8))
                - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.value * bitCount(bitboards.BP and (whitePawnShield shl 16)))

private fun uncastledTrappedWhiteRookEval(bitboards: BitboardData) =
        if (bitboards.WK and Bitboards.F1G1 != 0L &&
                bitboards.WR and Bitboards.G1H1 != 0L &&
                bitboards.WP and Bitboards.FILE_G != 0L &&
                bitboards.WP and Bitboards.FILE_H != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
         else (if (bitboards.WK and Bitboards.B1C1 != 0L &&
                bitboards.WR and Bitboards.A1B1 != 0L &&
                bitboards.WP and Bitboards.FILE_A != 0L &&
                bitboards.WP and Bitboards.FILE_B != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
         else 0)

private fun whiteKingShieldEval(bitboards: BitboardData) : Int {
    var shieldValue = 0

    if (whiteKingOnFirstTwoRanks(bitboards)) {
        with(whiteKingShield(bitboards)) {

            shieldValue = Math.min(whitePawnShieldEval(bitboards, this), Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value) -
                    uncastledTrappedWhiteRookEval(bitboards)

            val whiteOpen = southFill(this, 8) and southFill(bitboards.WP, 8).inv() and Bitboards.RANK_1
            if (whiteOpen != 0L) {
                shieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(whiteOpen and Bitboards.MIDDLE_FILES_8_BIT)
                shieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(whiteOpen and Bitboards.NONMID_FILES_8_BIT)
            }
            val blackOpen = southFill(this, 8) and southFill(bitboards.BP, 8).inv() and Bitboards.RANK_1
            if (blackOpen != 0L) {
                shieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(blackOpen and Bitboards.MIDDLE_FILES_8_BIT)
                shieldValue -= Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(blackOpen and Bitboards.NONMID_FILES_8_BIT)
            }
        }
    }

    return shieldValue
}

private fun blackPawnShieldEval(bitboards: BitboardData, blackPawnShield: Long) =
        (Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.value * bitCount(bitboards.BP and blackPawnShield)
                - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.value * bitCount(bitboards.WP and (blackPawnShield or (blackPawnShield ushr 8)))
                + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.value * bitCount(bitboards.BP and (blackPawnShield ushr 8))
                - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.value * bitCount(bitboards.WP and (blackPawnShield ushr 16)))

private fun uncastledTrappedBlackRookEval(bitboards: BitboardData) =
        if (bitboards.BK and Bitboards.F8G8 != 0L &&
                bitboards.BR and Bitboards.G8H8 != 0L &&
                bitboards.BP and Bitboards.FILE_G != 0L &&
                bitboards.BP and Bitboards.FILE_H != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else (if (bitboards.BK and Bitboards.B8C8 != 0L &&
                bitboards.BR and Bitboards.A8B8 != 0L &&
                bitboards.BP and Bitboards.FILE_A != 0L &&
                bitboards.BP and Bitboards.FILE_B != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else 0)

private fun blackKingShieldEval(bitboards: BitboardData): Int {
    var shieldValue = 0
    if (blackKingOnFirstTwoRanks(bitboards)) {
        with(blackKingShield(bitboards)) {
            shieldValue =
                    Math.min(blackPawnShieldEval(bitboards, this), Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value) -
                            uncastledTrappedBlackRookEval(bitboards)

            val whiteOpen = southFill(this, 8) and southFill(bitboards.WP, 8).inv() and Bitboards.RANK_1
            if (whiteOpen != 0L) {
                shieldValue -= (Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(whiteOpen and Bitboards.MIDDLE_FILES_8_BIT)
                        + Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(whiteOpen and Bitboards.NONMID_FILES_8_BIT))
            }
            val blackOpen = southFill(this, 8) and southFill(bitboards.BP, 8).inv() and Bitboards.RANK_1
            if (blackOpen != 0L) {
                shieldValue -= (Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(blackOpen and Bitboards.MIDDLE_FILES_8_BIT)
                        + Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(blackOpen and Bitboards.NONMID_FILES_8_BIT))
            }
        }
    }
    return shieldValue
}

private fun whiteKingOnFirstTwoRanks(bitboards: BitboardData) =
        whiteKingSquare(bitboards) / 8 < 2

private fun blackKingOnFirstTwoRanks(bitboards: BitboardData) =
        blackKingSquare(bitboards) / 8 >= 6

private fun blackKingShield(bitboards: BitboardData) =
        Bitboards.whiteKingShieldMask[blackKingSquare(bitboards) % 8] shl 40

private fun whiteKingShield(bitboards: BitboardData) =
        Bitboards.whiteKingShieldMask[whiteKingSquare(bitboards) % 8]

fun castlingEval(bitboards: BitboardData, castlePrivileges: Int): Int {
    var eval = 0
    val castlePrivs = (castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value) +
            (castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value) +
            (castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value) +
            (castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value)
    if (castlePrivs != 0) {
        // Value of moving King to its queenside castle destination in the middle game
        val kingSquareBonusMiddleGame = PieceSquareTables.king[1] - PieceSquareTables.king[3]
        val kingSquareBonusEndGame = PieceSquareTables.kingEndGame[1] - PieceSquareTables.kingEndGame[3]
        val rookSquareBonus = PieceSquareTables.rook[3] - PieceSquareTables.rook[0]
        var kingSquareBonusScaled = linearScale(
                blackPieceValues(bitboards),
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
            if (castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value != 0) {
                timeToCastleKingSide = 2
                if (bitboards.ALL and (1L shl 1) != 0L) timeToCastleKingSide++
                if (bitboards.ALL and (1L shl 2) != 0L) timeToCastleKingSide++
            }
            if (castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value != 0) {
                timeToCastleQueenSide = 2
                if (bitboards.ALL and (1L shl 6) != 0L) timeToCastleQueenSide++
                if (bitboards.ALL and (1L shl 5) != 0L) timeToCastleQueenSide++
                if (bitboards.ALL and (1L shl 4) != 0L) timeToCastleQueenSide++
            }
            eval += castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide)
        }
        kingSquareBonusScaled = linearScale(
                whitePieceValues(bitboards),
                Evaluation.CASTLE_BONUS_LOW_MATERIAL.value,
                Evaluation.CASTLE_BONUS_HIGH_MATERIAL.value,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)
        castleValue = kingSquareBonusScaled + rookSquareBonus
        if (castleValue > 0) {
            var timeToCastleKingSide = 100
            var timeToCastleQueenSide = 100
            if (castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value != 0) {
                timeToCastleKingSide = 2
                if (bitboards.ALL and (1L shl 57) != 0L) timeToCastleKingSide++
                if (bitboards.ALL and (1L shl 58) != 0L) timeToCastleKingSide++
            }
            if (castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value != 0) {
                timeToCastleQueenSide = 2
                if (bitboards.ALL and (1L shl 60) != 0L) timeToCastleQueenSide++
                if (bitboards.ALL and (1L shl 61) != 0L) timeToCastleQueenSide++
                if (bitboards.ALL and (1L shl 62) != 0L) timeToCastleQueenSide++
            }
            eval -= castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide)
        }
    }
    return eval
}

fun endGameAdjustment(bitboards: BitboardData, currentScore: Int): Int {
    var eval = currentScore
    if (whitePawnValues(bitboards) + blackPawnValues(bitboards) == 0 && whitePieceValues(bitboards) < PieceValue.getValue(Piece.ROOK) && blackPieceValues(bitboards) < PieceValue.getValue(Piece.ROOK)) return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
    if (eval > 0) {
        if (whitePawnValues(bitboards) == 0 && (whitePieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) || whitePieceValues(bitboards) == PieceValue.getValue(Piece.BISHOP))) return eval - (whitePieceValues(bitboards) * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt() else if (whitePawnValues(bitboards) == 0 && whitePieceValues(bitboards) - PieceValue.getValue(Piece.BISHOP) <= blackPieceValues(bitboards)) return eval / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value
        else if (bitCount(bitboards.ALL) > 3 &&
                bitboards.WR or bitboards.WN or bitboards.WQ == 0L) {
            // If this is not yet a KPK ending, and if white has only A pawns and has no dark bishop and the black king is on a8/a7/b8/b7 then this is probably a draw.
            // Do the same for H pawns
            if (bitboards.WP and Bitboards.FILE_A.inv() == 0L &&
                    bitboards.WB and Bitboards.LIGHT_SQUARES == 0L &&
                    bitboards.BK and Bitboards.A8A7B8B7 != 0L || bitboards.WP and Bitboards.FILE_H.inv() == 0L &&
                    bitboards.WB and Bitboards.DARK_SQUARES == 0L &&
                    bitboards.BK and Bitboards.H8H7G8G7 != 0L) {
                return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
            }
        }
        if (blackPawnValues(bitboards) == 0) {
            if (whitePieceValues(bitboards) - blackPieceValues(bitboards) > PieceValue.getValue(Piece.BISHOP)) {
                val whiteKnightCount = bitCount(bitboards.WN)
                val whiteBishopCount = bitCount(bitboards.WB)
                return if (whiteKnightCount == 2 && whitePieceValues(bitboards) == 2 * PieceValue.getValue(Piece.KNIGHT) && blackPieceValues(bitboards) == 0) eval / Evaluation.ENDGAME_DRAW_DIVISOR.value else if (whiteKnightCount == 1 && whiteBishopCount == 1 && whitePieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) && blackPieceValues(bitboards) == 0) {
                    eval = PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value + eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value
                    val kingSquare = blackKingSquare(bitboards)
                    eval += if (bitboards.WB and Bitboards.DARK_SQUARES != 0L) (7 - Bitboards.distanceToH1OrA8[Bitboards.bitFlippedHorizontalAxis[kingSquare]]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value else (7 - Bitboards.distanceToH1OrA8[kingSquare]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value
                    eval
                } else eval + Evaluation.VALUE_SHOULD_WIN.value
            }
        }
    }
    if (eval < 0) {
        if (blackPawnValues(bitboards) == 0 && (blackPieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) || blackPieceValues(bitboards) == PieceValue.getValue(Piece.BISHOP))) return eval + (blackPieceValues(bitboards) * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt() else if (blackPawnValues(bitboards) == 0 && blackPieceValues(bitboards) - PieceValue.getValue(Piece.BISHOP) <= whitePieceValues(bitboards))
            return eval / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value else if (bitCount(bitboards.ALL) > 3 && bitboards.BR or
                bitboards.BN or bitboards.BQ == 0L) {
            if (bitboards.BP and Bitboards.FILE_A.inv() == 0L &&
                    bitboards.BB and Bitboards.DARK_SQUARES == 0L &&
                    bitboards.WK and Bitboards.A1A2B1B2 != 0L) return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
            else if (bitboards.BP and Bitboards.FILE_H.inv() == 0L &&
                    bitboards.BB and Bitboards.LIGHT_SQUARES == 0L &&
                    bitboards.WK and Bitboards.H1H2G1G2 != 0L) return eval / Evaluation.ENDGAME_DRAW_DIVISOR.value
        }
        if (whitePawnValues(bitboards) == 0) {
            if (blackPieceValues(bitboards) - whitePieceValues(bitboards) > PieceValue.getValue(Piece.BISHOP)) {
                val blackKnightCount = bitCount(bitboards.BN)
                val blackBishopCount = bitCount(bitboards.BB)
                return if (blackKnightCount == 2 && blackPieceValues(bitboards) == 2 * PieceValue.getValue(Piece.KNIGHT) && whitePieceValues(bitboards) == 0) eval / Evaluation.ENDGAME_DRAW_DIVISOR.value else if (blackKnightCount == 1 && blackBishopCount == 1 && blackPieceValues(bitboards) == PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) && whitePieceValues(bitboards) == 0) {
                    eval = -(PieceValue.getValue(Piece.KNIGHT) + PieceValue.getValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value) + eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value
                    val kingSquare = whiteKingSquare(bitboards)
                    eval -= if (bitboards.BB and Bitboards.DARK_SQUARES != 0L) {
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

fun whitePieceValues(bitboards: BitboardData): Int {
    return bitCount(bitboards.WN) * PieceValue.getValue(Piece.KNIGHT) +
            bitCount(bitboards.WR) * PieceValue.getValue(Piece.ROOK) +
            bitCount(bitboards.WB) * PieceValue.getValue(Piece.BISHOP) +
            bitCount(bitboards.WQ) * PieceValue.getValue(Piece.QUEEN)
}

fun blackPieceValues(bitboards: BitboardData): Int {
    return bitCount(bitboards.BN) * PieceValue.getValue(Piece.KNIGHT) +
            bitCount(bitboards.BR) * PieceValue.getValue(Piece.ROOK) +
            bitCount(bitboards.BB) * PieceValue.getValue(Piece.BISHOP) +
            bitCount(bitboards.BQ) * PieceValue.getValue(Piece.QUEEN)
}

fun whitePawnValues(bitboards: BitboardData): Int {
    return bitCount(bitboards.WP) * PieceValue.getValue(Piece.PAWN)
}

fun blackPawnValues(bitboards: BitboardData): Int {
    return bitCount(bitboards.BP) * PieceValue.getValue(Piece.PAWN)
}

fun whiteKingSquare(bitboards: BitboardData) = numberOfTrailingZeros(bitboards.WK)

fun blackKingSquare(bitboards: BitboardData) = numberOfTrailingZeros(bitboards.BK)

private fun initBitboardData(board: EngineChessBoard): BitboardData {
    return BitboardData(
            WP = board.getBitboard(WP),
            WB = board.getBitboard(WB),
            WN = board.getBitboard(WN),
            WK = board.getBitboard(WK),
            WQ = board.getBitboard(WQ),
            WR = board.getBitboard(WR),
            BP = board.getBitboard(BP),
            BB = board.getBitboard(BB),
            BN = board.getBitboard(BN),
            BK = board.getBitboard(BK),
            BQ = board.getBitboard(BQ),
            BR = board.getBitboard(BR),
            ENPASSANTSQUARE = board.getBitboard(ENPASSANTSQUARE),
            ENEMY = board.getBitboard(ENEMY),
            ALL = board.getBitboard(ALL),
            FRIENDLY = board.getBitboard(FRIENDLY)
    )
}

fun evaluate(board: EngineChessBoard) : Int {

    val bitboards = initBitboardData(board)

    if (onlyKingsRemain(bitboards)) {
        return 0
    } else {

        val materialDifference = materialDifference(bitboards)

        val whiteRookSquares = squareList(bitboards.WR)
        val whiteRookAttacks = rookAttackMap(bitboards, whiteRookSquares)
        val whiteBishopSquares = squareList(bitboards.WB)
        val whiteBishopAttacks = bishopAttackMap(bitboards, whiteBishopSquares)
        val whitePieces = if (board.mover == Colour.WHITE) bitboards.FRIENDLY else bitboards.ENEMY
        val whiteKnightSquares = squareList(bitboards.WN)
        val blackPawnAttacks = getBlackPawnAttacks(bitboards.BP)
        val whiteQueenSquares = squareList(bitboards.WQ)
        val whiteQueenAttacks = queenAttackMap(bitboards, whiteQueenSquares)
        val blackRookSquares = squareList(bitboards.BR)
        val blackRookAttacks = rookAttackMap(bitboards, blackRookSquares)
        val blackBishopSquares = squareList(bitboards.BB)
        val blackBishopAttacks = bishopAttackMap(bitboards, blackBishopSquares)
        val blackPieces = if (board.mover == Colour.WHITE) bitboards.ENEMY else bitboards.FRIENDLY
        val blackKnightSquares = squareList(bitboards.BN)
        val whitePawnAttacks = getWhitePawnAttacks(bitboards.WP)
        val blackQueenSquares = squareList(bitboards.BQ)
        val blackQueenAttacks = queenAttackMap(bitboards, blackQueenSquares)

        val eval: Int = (materialDifference
                + whitePawnPieceSquareEval(bitboards)
                + whiteKingSquareEval(bitboards)
                + whiteQueenPieceSquareSum(whiteQueenSquares)
                + whiteBishopPieceSquareSum(whiteBishopSquares)
                + twoWhiteRooksTrappingKingEval(bitboards)
                + doubledRooksEval(whiteRookSquares)
                + whiteRookSquares.stream().map { whiteRookOpenFilesEval(bitboards, it % 8) }.reduce(0, Integer::sum)
                + whiteRookSquares.stream().map { Evaluation.getRookMobilityValue(bitCount(whiteRookAttacks[it]!! and whitePieces.inv())) }.reduce(0, Integer::sum)
                + whiteQueenSquares.stream().map { Evaluation.getQueenMobilityValue( bitCount(whiteQueenAttacks[it]!! and whitePieces.inv())) }.reduce(0, Integer::sum)
                + whiteBishopSquares.stream().map { Evaluation.getBishopMobilityValue(bitCount(whiteBishopAttacks[it]!! and whitePieces.inv())) }.reduce(0, Integer::sum)
                + whiteRookPieceSquareSum(whiteRookSquares) * rookEnemyPawnMultiplier(blackPawnValues(bitboards)) / 6
                - whiteKnightSquares.stream().map { blockedKnightPenaltyEval(it, blackPawnAttacks, bitboards.WP) }.reduce(0, Integer::sum)
                + whiteKnightPieceSquareEval(bitboards, whiteKnightSquares)
                - blackPawnPieceSquareEval(bitboards)
                - blackKingSquareEval(bitboards)
                - blackQueenPieceSquareSum(blackQueenSquares)
                - blackBishopPieceSquareSum(blackBishopSquares)
                - twoBlackRooksTrappingKingEval(bitboards)
                - doubledRooksEval(blackRookSquares)
                - blackRookSquares.stream().map { blackRookOpenFilesEval(bitboards, it % 8) }.reduce(0, Integer::sum)
                - blackRookSquares.stream().map {Evaluation.getRookMobilityValue(bitCount(blackRookAttacks[it]!! and blackPieces.inv())) }.reduce(0, Integer::sum)
                - blackQueenSquares.stream().map { Evaluation.getQueenMobilityValue(bitCount(blackQueenAttacks[it]!! and blackPieces.inv())) }.reduce(0, Integer::sum)
                - blackBishopSquares.stream().map { Evaluation.getBishopMobilityValue(bitCount(blackBishopAttacks[it]!! and blackPieces.inv())) }.reduce(0, Integer::sum)
                - (blackRookPieceSquareSum(blackRookSquares) * rookEnemyPawnMultiplier(whitePawnValues(bitboards)) / 6)
                + blackKnightSquares.stream().map { blockedKnightPenaltyEval(it, whitePawnAttacks, bitboards.BP) }.reduce(0, Integer::sum)
                - blackKnightPieceSquareEval(bitboards, blackKnightSquares)
                + board.boardHashObject.getPawnHashEntry(board).pawnScore
                + tradePawnBonusWhenMoreMaterial(bitboards, materialDifference)
                + tradePieceBonusWhenMoreMaterial(bitboards, materialDifference)
                + castlingEval(bitboards, board.castlePrivileges)
                + bishopScore(bitboards, materialDifference)
                + threatEval(bitboards, board)
                + kingSafetyEval(bitboards, board))

        val endGameAdjustedScore = if (isEndGame(bitboards)) endGameAdjustment(bitboards, eval) else eval

        return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore
    }
}

private fun blackKnightPieceSquareEval(bitboards: BitboardData, blackKnightSquares: List<Int>) =
    linearScale(
            whitePieceValues(bitboards) + whitePawnValues(bitboards),
            Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
            Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
            blackKnightSquares.stream().map { s: Int -> PieceSquareTables.knightEndGame[Bitboards.bitFlippedHorizontalAxis[s]] }.reduce(0, Integer::sum),
            blackKnightSquares.stream().map { s: Int -> PieceSquareTables.knight[Bitboards.bitFlippedHorizontalAxis[s]] }.reduce(0, Integer::sum)
    )

private fun whiteKnightPieceSquareEval(bitboards: BitboardData, whiteKnightSquares: List<Int>) =
    linearScale(blackPieceValues(bitboards) + blackPawnValues(bitboards),
            Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
            Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
            whiteKnightSquares.stream().map { PieceSquareTables.knightEndGame[it] }.reduce(0, Integer::sum),
            whiteKnightSquares.stream().map { PieceSquareTables.knight[it] }.reduce(0, Integer::sum)
    )

private fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        bitCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * Evaluation.KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY.value

private fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        Bitboards.knightMoves[square] and (enemyPawnAttacks or friendlyPawns)
