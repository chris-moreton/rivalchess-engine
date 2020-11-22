package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.pawnValues
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square

@JvmOverloads
@kotlin.ExperimentalUnsignedTypes
fun evaluate(board: EngineBoard): Int {

    if (onlyKingsRemain(board)) return 0

    val materialDifference = materialDifferenceEval(board)
    val isEndGame = isEndGame(board)

    val attacks = Attacks(board)

    val whitePiecesInverted = (if (board.mover == Colour.WHITE) board.getBitboard(BITBOARD_FRIENDLY) else board.getBitboard(BITBOARD_ENEMY)).inv()
    val blackPiecesInverted = (if (board.mover == Colour.WHITE) board.getBitboard(BITBOARD_ENEMY) else board.getBitboard(BITBOARD_FRIENDLY)).inv()

    val score =
            materialDifference +
            pawnScore(attacks, board) +
            tradePawnBonusWhenMoreMaterial(board, materialDifference) +
            tradePieceBonusWhenMoreMaterial(board, materialDifference) +
            pawnPieceSquareEval(board) +
            kingSafetyEval(board, attacks) +
            kingSquareEval(board) +
            threatEval(attacks, board) +
            doubledRooksEval(board) +
            rooksEval(board, whitePiecesInverted, blackPiecesInverted) +
            bishopsEval(board, whitePiecesInverted, blackPiecesInverted) +
            knightsEval(board, attacks) +
            queensEval(board, whitePiecesInverted, blackPiecesInverted) +
            bishopScore(board, materialDifference)

    val endGameAdjustedScore = if (isEndGame) endGameAdjustment(board, score) else score

    return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore
}

@kotlin.ExperimentalUnsignedTypes
private fun queensEval(board: EngineBoard, whitePiecesInverted: Long, blackPiecesInverted: Long) =
        (whiteQueensEval(board, whitePiecesInverted) - blackQueensEval(board, blackPiecesInverted))

@kotlin.ExperimentalUnsignedTypes
private fun kingSquareEval(board: EngineBoard) =
        (whiteKingSquareEval(board) - blackKingSquareEval(board))

@kotlin.ExperimentalUnsignedTypes
private fun knightsEval(board: EngineBoard, attacks: Attacks) =
        (whiteKnightsEval(board, attacks) - blackKnightsEval(board, attacks))

@kotlin.ExperimentalUnsignedTypes
private fun bishopsEval(board: EngineBoard, whitePiecesInverted: Long, blackPiecesInverted: Long) =
        (whiteBishopEval(board, whitePiecesInverted) - blackBishopsEval(board, blackPiecesInverted))

@kotlin.ExperimentalUnsignedTypes
private fun pawnPieceSquareEval(board: EngineBoard) = whitePawnsEval(board) - blackPawnsEval(board)

@kotlin.ExperimentalUnsignedTypes
private fun rooksEval(board: EngineBoard, whitePiecesInverted: Long, blackPiecesInverted: Long) =
        (whiteRooksEval(board, whitePiecesInverted) - blackRooksEval(board, blackPiecesInverted))

@kotlin.ExperimentalUnsignedTypes
private fun doubledRooksEval(board: EngineBoard) =
        (doubledRooksEval(board.getBitboard(BITBOARD_WR)) - doubledRooksEval(board.getBitboard(BITBOARD_BR)))

@kotlin.ExperimentalUnsignedTypes
fun materialDifferenceEval(board: EngineBoard) =
        board.whitePieceValues - board.blackPieceValues + board.pawnValues(BITBOARD_WP) - board.pawnValues(BITBOARD_BP)

fun exactlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L && bitboard != 0L

@kotlin.ExperimentalUnsignedTypes
fun onlyKingsRemain(board: EngineBoard) = exactlyOneBitSet(board.getBitboard(BITBOARD_ENEMY)) and exactlyOneBitSet(board.getBitboard(BITBOARD_FRIENDLY))

@kotlin.ExperimentalUnsignedTypes
fun whiteKingSquareEval(board: EngineBoard) =
        linearScale(
                board.blackPieceValues,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                kingEndGamePieceSquareTable[board.whiteKingSquareCalculated],
                kingPieceSquareTable[board.whiteKingSquareCalculated]
        )

@kotlin.ExperimentalUnsignedTypes
fun blackKingSquareEval(board: EngineBoard) =
        linearScale(
                board.whitePieceValues,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                kingEndGamePieceSquareTable[bitFlippedHorizontalAxis[board.blackKingSquareCalculated]],
                kingPieceSquareTable[bitFlippedHorizontalAxis[board.blackKingSquareCalculated]]
        )

fun linearScale(x: Int, min: Int, max: Int, a: Int, b: Int) =
        when {
            x < min -> a
            x > max -> b
            else -> a + (x - min) * (b - a) / (max - min)
        }

@kotlin.ExperimentalUnsignedTypes
fun whiteRookOpenFilesEval(board: EngineBoard, file: Int) =
        if (FILES[file] and board.getBitboard(BITBOARD_WP) == 0L)
            if (FILES[file] and board.getBitboard(BITBOARD_BP) == 0L) VALUE_ROOK_ON_OPEN_FILE
            else VALUE_ROOK_ON_HALF_OPEN_FILE
        else 0

@kotlin.ExperimentalUnsignedTypes
fun blackRookOpenFilesEval(board: EngineBoard, file: Int) =
        if ((FILES[file] and board.getBitboard(BITBOARD_BP)) == 0L)
            if ((FILES[file] and board.getBitboard(BITBOARD_WP)) == 0L) VALUE_ROOK_ON_OPEN_FILE
            else VALUE_ROOK_ON_HALF_OPEN_FILE
        else 0

@kotlin.ExperimentalUnsignedTypes
fun rookEnemyPawnMultiplier(enemyPawnValues: Int) = (enemyPawnValues / VALUE_PAWN).coerceAtMost(6)

@kotlin.ExperimentalUnsignedTypes
fun doubledRooksEval(rookBitboard: Long): Int {
    val files = booleanArrayOf(false,false,false,false,false,false,false,false)
    applyToSquares(rookBitboard) { square ->
        if (files[square % 8]) return VALUE_ROOKS_ON_SAME_FILE
        files[square % 8] = true
    }
    return 0
}

@kotlin.ExperimentalUnsignedTypes
fun flippedSquareTableScore(table: IntArray, bit: Int) = table[bitFlippedHorizontalAxis[bit]]

@kotlin.ExperimentalUnsignedTypes
fun kingAttackCount(dangerZone: Long, attacks: LongArray): Int {
    var acc = 0
    for (it in attacks) {
        if (it == -1L) return acc
        acc += popCount(it and dangerZone)
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun tradePieceBonusWhenMoreMaterial(board: EngineBoard, materialDifference: Int) =
        linearScale(
                if (materialDifference > 0)
                    board.blackPieceValues + board.pawnValues(BITBOARD_BP) else
                    board.whitePieceValues + board.pawnValues(BITBOARD_WP),
                0,
                TOTAL_PIECE_VALUE_PER_SIDE_AT_START,
                30 * materialDifference / 100,
                0)

@kotlin.ExperimentalUnsignedTypes
fun tradePawnBonusWhenMoreMaterial(board: EngineBoard, materialDifference: Int) =
        linearScale(
                if (materialDifference > 0) board.pawnValues(BITBOARD_WP) else board.pawnValues(BITBOARD_BP),
                0,
                PAWN_TRADE_BONUS_MAX,
                -30 * materialDifference / 100,
                0)

@kotlin.ExperimentalUnsignedTypes
fun bishopScore(board: EngineBoard, materialDifference: Int) =
        bishopPairEval(board) + oppositeColourBishopsEval(board, materialDifference) + trappedBishopEval(board)

@kotlin.ExperimentalUnsignedTypes
fun atLeastOnePieceOnLightSquare(bitboard: Long) = bitboard and LIGHT_SQUARES != 0L
@kotlin.ExperimentalUnsignedTypes
fun atLeastOnePieceOnDarkSquare(bitboard: Long) = bitboard and DARK_SQUARES != 0L
@kotlin.ExperimentalUnsignedTypes
fun whiteBishopColourCount(board: EngineBoard) = (if (atLeastOnePieceOnLightSquare(board.getBitboard(BITBOARD_WB))) 1 else 0) + if (atLeastOnePieceOnDarkSquare(board.getBitboard(BITBOARD_WB))) 1 else 0
@kotlin.ExperimentalUnsignedTypes
fun blackBishopColourCount(board: EngineBoard) = (if (atLeastOnePieceOnLightSquare(board.getBitboard(BITBOARD_BB))) 1 else 0) + if (atLeastOnePieceOnDarkSquare(board.getBitboard(BITBOARD_BB))) 1 else 0

@kotlin.ExperimentalUnsignedTypes
fun oppositeColourBishopsEval(board: EngineBoard, materialDifference: Int): Int {

    if (whiteBishopColourCount(board) == 1 && blackBishopColourCount(board) == 1 &&
            atLeastOnePieceOnLightSquare(board.getBitboard(BITBOARD_WB)) != atLeastOnePieceOnLightSquare(board.getBitboard(BITBOARD_BB)) &&
            board.whitePieceValues == board.blackPieceValues) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / WRONG_COLOUR_BISHOP_PENALTY_DIVISOR // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScale(
                board.whitePieceValues + board.blackPieceValues,
                WRONG_COLOUR_BISHOP_MATERIAL_LOW,
                WRONG_COLOUR_BISHOP_MATERIAL_HIGH,
                maxPenalty,
                0)
    }
    return 0
}

@kotlin.ExperimentalUnsignedTypes
fun bishopPairEval(board: EngineBoard) =
        bishopPairEvalForColour(board, ::whiteBishopColourCount, popCount(board.getBitboard(BITBOARD_WP))) -
        bishopPairEvalForColour(board, ::blackBishopColourCount, popCount(board.getBitboard(BITBOARD_BP)))

@kotlin.ExperimentalUnsignedTypes
inline fun bishopPairEvalForColour(board: EngineBoard, fn: (EngineBoard) -> Int, pawnCount: Int) =
        (if (fn(board) == 2) VALUE_BISHOP_PAIR + (8 - pawnCount) * VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS else 0)

@kotlin.ExperimentalUnsignedTypes
fun trappedBishopEval(board: EngineBoard) =
        if ((board.getBitboard(BITBOARD_WB) or board.getBitboard(BITBOARD_BB)) and A2A7H2H7 != 0L)
            blackA2TrappedBishopEval(board) + blackH2TrappedBishopEval(board) -
                    whiteA7TrappedBishopEval(board) - whiteH7TrappedBishopEval(board)
        else 0

@kotlin.ExperimentalUnsignedTypes
fun blackH2TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_BB) and (1L shl Square.H2.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.G3.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.F2.bitRef) != 0L)
            if (board.getBitboard(BITBOARD_BQ) == 0L) VALUE_TRAPPED_BISHOP_PENALTY
            else VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        else 0

@kotlin.ExperimentalUnsignedTypes
fun blackA2TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_BB) and (1L shl Square.A2.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.B3.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.C2.bitRef) != 0L)
            VALUE_TRAPPED_BISHOP_PENALTY
        else 0

@kotlin.ExperimentalUnsignedTypes
fun whiteH7TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_WB) and (1L shl Square.H7.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.G6.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.F7.bitRef) != 0L)
            if (board.getBitboard(BITBOARD_WQ) == 0L) VALUE_TRAPPED_BISHOP_PENALTY
            else VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        else 0

@kotlin.ExperimentalUnsignedTypes
fun whiteA7TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_WB) and (1L shl Square.A7.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.B6.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.C7.bitRef) != 0L)
            VALUE_TRAPPED_BISHOP_PENALTY
        else 0

@kotlin.ExperimentalUnsignedTypes
fun blackPieceBitboard(board: EngineBoard) = (board.getBitboard(BITBOARD_BN) or board.getBitboard(BITBOARD_BR) or board.getBitboard(BITBOARD_BQ) or board.getBitboard(BITBOARD_BB))

@kotlin.ExperimentalUnsignedTypes
fun whitePieceBitboard(board: EngineBoard) = (board.getBitboard(BITBOARD_WN) or board.getBitboard(BITBOARD_WR) or board.getBitboard(BITBOARD_WQ) or board.getBitboard(BITBOARD_WB))

@kotlin.ExperimentalUnsignedTypes
fun isEndGame(board: EngineBoard) =
        (board.whitePieceValues +
                board.pawnValues(BITBOARD_WP) +
                board.blackPieceValues +
                board.pawnValues(BITBOARD_BP)) <= EVAL_ENDGAME_TOTAL_PIECES

@kotlin.ExperimentalUnsignedTypes
fun kingSafetyEval(board: EngineBoard, attacks: Attacks): Int {

    val averagePiecesPerSide = (board.whitePieceValues + board.blackPieceValues) / 2
    if (averagePiecesPerSide <= KINGSAFETY_MIN_PIECE_BALANCE) return 0

    val whiteKingAttackedCount = whiteKingAttackCount(whiteKingDangerZone[board.whiteKingSquareCalculated], attacks)
    val blackKingAttackedCount = blackKingAttackCount(blackKingDangerZone[board.blackKingSquareCalculated], attacks)

    val whiteKingSafety: Int = whiteKingShieldEval(board)
    val blackKingSafety: Int = blackKingShieldEval(board)

    return linearScale(
            averagePiecesPerSide,
            KINGSAFETY_MIN_PIECE_BALANCE,
            KINGSAFETY_MAX_PIECE_BALANCE,
            0,
            whiteKingSafety - blackKingSafety + (blackKingAttackedCount - whiteKingAttackedCount) * KINGSAFETY_ATTACK_MULTIPLIER)
}

@kotlin.ExperimentalUnsignedTypes
private fun whiteKingAttackCount(whiteKingDangerZone: Long, attacks: Attacks): Int {
    return kingAttackCount(whiteKingDangerZone, attacks.blackRooksAttackArray) +
            kingAttackCount(whiteKingDangerZone, attacks.blackQueensAttackArray) * 2 +
            kingAttackCount(whiteKingDangerZone, attacks.blackBishopsAttackArray)
}

@kotlin.ExperimentalUnsignedTypes
private fun blackKingAttackCount(blackKingDangerZone: Long, attacks: Attacks): Int {
    return kingAttackCount(blackKingDangerZone, attacks.whiteRooksAttackArray) +
            kingAttackCount(blackKingDangerZone, attacks.whiteQueensAttackArray) * 2 +
            kingAttackCount(blackKingDangerZone, attacks.whiteBishopsAttackArray)
}

@kotlin.ExperimentalUnsignedTypes
fun uncastledTrappedWhiteRookEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_WK) and F1G1 != 0L &&
                board.getBitboard(BITBOARD_WR) and G1H1 != 0L &&
                board.getBitboard(BITBOARD_WP) and FILE_G != 0L &&
                board.getBitboard(BITBOARD_WP) and FILE_H != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else (if (board.getBitboard(BITBOARD_WK) and B1C1 != 0L &&
                board.getBitboard(BITBOARD_WR) and A1B1 != 0L &&
                board.getBitboard(BITBOARD_WP) and FILE_A != 0L &&
                board.getBitboard(BITBOARD_WP) and FILE_B != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else 0)

@kotlin.ExperimentalUnsignedTypes
fun uncastledTrappedBlackRookEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_BK) and F8G8 != 0L && board.getBitboard(BITBOARD_BR) and G8H8 != 0L &&
                board.getBitboard(BITBOARD_BP) and FILE_G != 0L && board.getBitboard(BITBOARD_BP) and FILE_H != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else (if (board.getBitboard(BITBOARD_BK) and B8C8 != 0L && board.getBitboard(BITBOARD_BR) and A8B8 != 0L &&
                board.getBitboard(BITBOARD_BP) and FILE_A != 0L && board.getBitboard(BITBOARD_BP) and FILE_B != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else 0)

@kotlin.ExperimentalUnsignedTypes
fun openFiles(kingShield: Long, pawnBitboard: Long) = southFill(kingShield) and southFill(pawnBitboard).inv() and RANK_1

@kotlin.ExperimentalUnsignedTypes
fun whiteKingShieldEval(board: EngineBoard) =
        KINGSAFETY_SHIELD_BASE +
                if (whiteKingOnFirstTwoRanks(board)) {
                    combineWhiteKingShieldEval(board, whiteKingShield(board))
                } else 0

@kotlin.ExperimentalUnsignedTypes
fun combineWhiteKingShieldEval(board: EngineBoard, kingShield: Long) =
        pawnShieldEval(board.getBitboard(BITBOARD_WP), board.getBitboard(BITBOARD_BP), kingShield, Long::shl)
                .coerceAtMost(KINGSAFTEY_MAXIMUM_SHIELD_BONUS) -
                uncastledTrappedWhiteRookEval(board)

@kotlin.ExperimentalUnsignedTypes
fun blackKingShieldEval(board: EngineBoard) =
        KINGSAFETY_SHIELD_BASE +
                if (blackKingOnFirstTwoRanks(board)) {
                    combineBlackKingShieldEval(board, blackKingShield(board))
                } else 0

@kotlin.ExperimentalUnsignedTypes
fun combineBlackKingShieldEval(board: EngineBoard, kingShield: Long) =
        pawnShieldEval(board.getBitboard(BITBOARD_BP), board.getBitboard(BITBOARD_WP), kingShield, Long::ushr)
                .coerceAtMost(KINGSAFTEY_MAXIMUM_SHIELD_BONUS) -
                uncastledTrappedBlackRookEval(board)

@kotlin.ExperimentalUnsignedTypes
fun whiteKingOnFirstTwoRanks(board: EngineBoard) = yCoordOfSquare(board.whiteKingSquareCalculated) < 2

@kotlin.ExperimentalUnsignedTypes
fun blackKingOnFirstTwoRanks(board: EngineBoard) = yCoordOfSquare(board.blackKingSquareCalculated) >= 6

@kotlin.ExperimentalUnsignedTypes
fun blackKingShield(board: EngineBoard) = whiteKingShieldMask[board.blackKingSquareCalculated % 8] shl 40

@kotlin.ExperimentalUnsignedTypes
fun whiteKingShield(board: EngineBoard): Long = whiteKingShieldMask[board.whiteKingSquareCalculated % 8]

@kotlin.ExperimentalUnsignedTypes
fun endGameAdjustment(board: EngineBoard, currentScore: Int) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(board)) currentScore / ENDGAME_DRAW_DIVISOR
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(board, currentScore)
            else -> blackWinningEndGameAdjustment(board, currentScore)
        }

@kotlin.ExperimentalUnsignedTypes
fun penaltyForKingNotBeingNearOtherKing(board: EngineBoard) =
        (kotlin.math.abs(xCoordOfSquare(board.whiteKingSquareCalculated) - xCoordOfSquare(board.blackKingSquareCalculated)) +
                kotlin.math.abs(yCoordOfSquare(board.whiteKingSquareCalculated) - yCoordOfSquare(board.blackKingSquareCalculated))
                ) * KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING

@kotlin.ExperimentalUnsignedTypes
fun blackWinningEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        (if (blackHasInsufficientMaterial(board)) currentScore + (board.blackPieceValues * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probableDrawWhenBlackIsWinning(board)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noBlackRooksQueensOrBishops(board) && (blackBishopDrawOnFileA(board) || blackBishopDrawOnFileH(board))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (board.getBitboard(BITBOARD_WP) == 0L) blackWinningNoWhitePawnsEndGameAdjustment(board, currentScore)
        else currentScore) + penaltyForKingNotBeingNearOtherKing(board) - (kingInCornerPieceSquareTable[board.whiteKingSquareCalculated] * KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING)

@kotlin.ExperimentalUnsignedTypes
fun blackWinningNoWhitePawnsEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        if (blackMoreThanABishopUpInNonPawns(board)) {
            if (blackHasOnlyTwoKnights(board) && board.whitePieceValues == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (blackHasOnlyAKnightAndBishop(board) && board.whitePieceValues == 0) {
                blackKnightAndBishopVKingEval(currentScore, board)
            } else currentScore - VALUE_SHOULD_WIN
        } else currentScore

@kotlin.ExperimentalUnsignedTypes
fun blackKnightAndBishopVKingEval(currentScore: Int, board: EngineBoard): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (atLeastOnePieceOnDarkSquare(board.getBitboard(BITBOARD_BB)))
        enemyKingCloseToDarkCornerMateSquareValue(board.whiteKingSquareCalculated)
    else
        enemyKingCloseToLightCornerMateSquareValue(board.whiteKingSquareCalculated)
}

@kotlin.ExperimentalUnsignedTypes
fun whiteWinningEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        (if (whiteHasInsufficientMaterial(board)) currentScore - (board.whitePieceValues * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probablyDrawWhenWhiteIsWinning(board)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noWhiteRooksQueensOrKnights(board) && (whiteBishopDrawOnFileA(board) || whiteBishopDrawOnFileH(board))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (board.getBitboard(BITBOARD_BP) == 0L) whiteWinningNoBlackPawnsEndGameAdjustment(board, currentScore)
        else currentScore) - penaltyForKingNotBeingNearOtherKing(board) + (kingInCornerPieceSquareTable[board.blackKingSquareCalculated] * KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING)

@kotlin.ExperimentalUnsignedTypes
fun whiteWinningNoBlackPawnsEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        if (whiteMoreThanABishopUpInNonPawns(board)) {
            if (whiteHasOnlyTwoKnights(board) && board.blackPieceValues == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (whiteHasOnlyAKnightAndBishop(board) && board.blackPieceValues == 0)
                whiteKnightAndBishopVKingEval(currentScore, board)
            else currentScore + VALUE_SHOULD_WIN
        } else currentScore

@kotlin.ExperimentalUnsignedTypes
fun whiteKnightAndBishopVKingEval(currentScore: Int, board: EngineBoard): Int {
    whiteShouldWinWithKnightAndBishopValue(currentScore)
    return +if (atLeastOnePieceOnDarkSquare(board.getBitboard(BITBOARD_WB))) enemyKingCloseToDarkCornerMateSquareValue(board.blackKingSquareCalculated)
    else enemyKingCloseToLightCornerMateSquareValue(board.blackKingSquareCalculated)
}

@kotlin.ExperimentalUnsignedTypes
fun enemyKingCloseToDarkCornerMateSquareValue(kingSquare: Int) = enemyKingCloseToLightCornerMateSquareValue(bitFlippedHorizontalAxis[kingSquare])

@kotlin.ExperimentalUnsignedTypes
fun enemyKingCloseToLightCornerMateSquareValue(kingSquare: Int) = (7 - distanceToH1OrA8[kingSquare]) * ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE

@kotlin.ExperimentalUnsignedTypes
fun blackShouldWinWithKnightAndBishopValue(eval: Int) =
        -(VALUE_KNIGHT + VALUE_BISHOP + VALUE_SHOULD_WIN) +
                eval / ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR

@kotlin.ExperimentalUnsignedTypes
fun whiteShouldWinWithKnightAndBishopValue(eval: Int) =
        VALUE_KNIGHT + VALUE_BISHOP + VALUE_SHOULD_WIN + eval / ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR

@kotlin.ExperimentalUnsignedTypes
fun whiteHasOnlyAKnightAndBishop(board: EngineBoard) =
        exactlyOneBitSet(board.getBitboard(BITBOARD_WN)) && (board.whitePieceValues == VALUE_KNIGHT + VALUE_BISHOP)

@kotlin.ExperimentalUnsignedTypes
fun blackHasOnlyAKnightAndBishop(board: EngineBoard) =
        exactlyOneBitSet(board.getBitboard(BITBOARD_BN)) && (board.blackPieceValues == VALUE_KNIGHT + VALUE_BISHOP)

@kotlin.ExperimentalUnsignedTypes
fun whiteHasOnlyTwoKnights(board: EngineBoard) =
        popCount(board.getBitboard(BITBOARD_WN)) == 2 && (board.whitePieceValues == 2 * VALUE_KNIGHT)

@kotlin.ExperimentalUnsignedTypes
fun blackHasOnlyTwoKnights(board: EngineBoard) =
        popCount(board.getBitboard(BITBOARD_BN)) == 2 && (board.blackPieceValues == 2 * VALUE_KNIGHT)

@kotlin.ExperimentalUnsignedTypes
fun blackMoreThanABishopUpInNonPawns(board: EngineBoard) =
        board.blackPieceValues - board.whitePieceValues > VALUE_BISHOP

@kotlin.ExperimentalUnsignedTypes
fun whiteMoreThanABishopUpInNonPawns(board: EngineBoard) =
        board.whitePieceValues - board.blackPieceValues > VALUE_BISHOP

@kotlin.ExperimentalUnsignedTypes
fun noBlackRooksQueensOrBishops(board: EngineBoard) =
        board.getBitboard(BITBOARD_BR) or board.getBitboard(BITBOARD_BN) or board.getBitboard(BITBOARD_BQ) == 0L

@kotlin.ExperimentalUnsignedTypes
fun bothSidesHaveOnlyOneKnightOrBishopEach(board: EngineBoard) =
        noPawnsRemain(board) && board.whitePieceValues < VALUE_ROOK && board.blackPieceValues < VALUE_ROOK

@kotlin.ExperimentalUnsignedTypes
fun noPawnsRemain(board: EngineBoard) = board.getBitboard(BITBOARD_WP) + board.getBitboard(BITBOARD_BP) == 0L

@kotlin.ExperimentalUnsignedTypes
fun noWhiteRooksQueensOrKnights(board: EngineBoard) =
        board.getBitboard(BITBOARD_WR) or board.getBitboard(BITBOARD_WN) or board.getBitboard(BITBOARD_WQ) == 0L

@kotlin.ExperimentalUnsignedTypes
fun blackBishopDrawOnFileH(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_BP) and FILE_H.inv() == 0L &&
            board.getBitboard(BITBOARD_BB) and LIGHT_SQUARES == 0L &&
            board.getBitboard(BITBOARD_WK) and H1H2G1G2 != 0L
}

@kotlin.ExperimentalUnsignedTypes
fun blackBishopDrawOnFileA(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_BP) and FILE_A.inv() == 0L &&
            board.getBitboard(BITBOARD_BB) and DARK_SQUARES == 0L &&
            board.getBitboard(BITBOARD_WK) and A1A2B1B2 != 0L
}

@kotlin.ExperimentalUnsignedTypes
fun whiteBishopDrawOnFileA(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_WP) and FILE_A.inv() == 0L &&
            board.getBitboard(BITBOARD_WB) and LIGHT_SQUARES == 0L &&
            board.getBitboard(BITBOARD_BK) and A8A7B8B7 != 0L
}

@kotlin.ExperimentalUnsignedTypes
fun whiteBishopDrawOnFileH(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_WP) and FILE_H.inv() == 0L &&
            board.getBitboard(BITBOARD_WB) and DARK_SQUARES == 0L &&
            board.getBitboard(BITBOARD_BK) and H8H7G8G7 != 0L
}

@kotlin.ExperimentalUnsignedTypes
fun probableDrawWhenBlackIsWinning(board: EngineBoard) =
        board.pawnValues(BITBOARD_BP) == 0 && board.blackPieceValues - VALUE_BISHOP <= board.whitePieceValues

@kotlin.ExperimentalUnsignedTypes
fun probablyDrawWhenWhiteIsWinning(board: EngineBoard) =
        board.pawnValues(BITBOARD_WP) == 0 && board.whitePieceValues - VALUE_BISHOP <= board.blackPieceValues

@kotlin.ExperimentalUnsignedTypes
fun blackHasInsufficientMaterial(board: EngineBoard) =
        board.getBitboard(BITBOARD_BP) == 0L && (board.blackPieceValues == VALUE_KNIGHT || board.blackPieceValues == VALUE_BISHOP)

@kotlin.ExperimentalUnsignedTypes
fun whiteHasInsufficientMaterial(board: EngineBoard) =
        board.getBitboard(BITBOARD_WP) == 0L && (board.whitePieceValues == VALUE_KNIGHT || board.whitePieceValues == VALUE_BISHOP)

@kotlin.ExperimentalUnsignedTypes
fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        popCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY

@kotlin.ExperimentalUnsignedTypes
fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        knightMoves[square] and (enemyPawnAttacks or friendlyPawns)



@kotlin.ExperimentalUnsignedTypes
fun blackKnightsEval(board: EngineBoard, attacks: Attacks) : Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BN)) {
        acc += linearScale(
                board.whitePieceValues + board.pawnValues(BITBOARD_WP),
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                knightEndGamePieceSquareTable[bitFlippedHorizontalAxis[it]],
                knightPieceSquareTable[bitFlippedHorizontalAxis[it]]
        ) - blockedKnightPenaltyEval(it, attacks.whitePawnsAttackBitboard, board.getBitboard(BITBOARD_BP))
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun whiteKnightsEval(board: EngineBoard, attacks: Attacks) : Int {

    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WN)) {
        acc += linearScale(board.blackPieceValues + board.pawnValues(BITBOARD_BP),
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                knightEndGamePieceSquareTable[it],
                knightPieceSquareTable[it]
        ) - blockedKnightPenaltyEval(it, attacks.blackPawnsAttackBitboard, board.getBitboard(BITBOARD_WP))
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun blackBishopsEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BB)) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(board.getBitboard(BITBOARD_ALL), it) and blackPiecesInverted)] +
                flippedSquareTableScore(bishopPieceSquareTable, it)
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun whiteBishopEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WB)) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(board.getBitboard(BITBOARD_ALL), it) and whitePiecesInverted)] + bishopPieceSquareTable[it]
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
private fun blackQueensEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BQ)) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(board.getBitboard(BITBOARD_ALL), it) and blackPiecesInverted)] + flippedSquareTableScore(queenPieceSquareTable, it)
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
private fun whiteQueensEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WQ)) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(board.getBitboard(BITBOARD_ALL), it) and whitePiecesInverted)] + queenPieceSquareTable[it]
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
private fun blackRooksEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BR)) {
        acc += blackRookOpenFilesEval(board, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(board.getBitboard(BITBOARD_ALL), it) and blackPiecesInverted)] +
                flippedSquareTableScore(rookPieceSquareTable, it) * rookEnemyPawnMultiplier(board.pawnValues(BITBOARD_WP)) / 6
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun whiteRooksEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WR)) {
        acc += whiteRookOpenFilesEval(board, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(board.getBitboard(BITBOARD_ALL), it) and whitePiecesInverted)] +
                rookPieceSquareTable[it] * rookEnemyPawnMultiplier(board.pawnValues(BITBOARD_BP)) / 6
    }
    return acc
}

