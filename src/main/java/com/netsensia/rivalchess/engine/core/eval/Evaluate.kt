package com.netsensia.rivalchess.engine.core.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.config.Evaluation
import com.netsensia.rivalchess.engine.core.board.EngineBoard
import com.netsensia.rivalchess.enums.CastleBitMask
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Piece
import com.netsensia.rivalchess.model.Square
import java.lang.Long.bitCount
import kotlin.math.abs

fun materialDifferenceEval(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces +
                materialValues.whitePawns - materialValues.blackPawns

fun onlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L

fun onlyKingsRemain(bitboards: BitboardData) = onlyOneBitSet(bitboards.enemy) and onlyOneBitSet(bitboards.friendly)

fun whiteKingSquareEval(bitboards: BitboardData, kingSquares: KingSquares) =
        linearScale(
                blackPieceValues(bitboards),
                pieceValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.value,
                PieceSquareTables.kingEndGame[kingSquares.white],
                PieceSquareTables.king[kingSquares.white]
        )

fun blackKingSquareEval(bitboards: BitboardData, kingSquares: KingSquares) =
        linearScale(
                whitePieceValues(bitboards),
                pieceValue(Piece.ROOK),
                Evaluation.OPENING_PHASE_MATERIAL.value,
                PieceSquareTables.kingEndGame[Bitboards.bitFlippedHorizontalAxis[kingSquares.black]],
                PieceSquareTables.king[Bitboards.bitFlippedHorizontalAxis[kingSquares.black]]
        )

fun linearScale(situation: Int, ref1: Int, ref2: Int, score1: Int, score2: Int) =
        when {
            situation < ref1 -> score1
            situation > ref2 -> score2
            else -> (situation - ref1) * (score2 - score1) / (ref2 - ref1) + score1
        }

fun twoWhiteRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.whiteRooks and RANK_7) > 1
                && bitboards.blackKing and RANK_8 != 0L)
            Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.value else 0

fun twoBlackRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.blackRooks and RANK_2) > 1
                && bitboards.whiteKing and RANK_1 != 0L)
            Evaluation.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING.value else 0

fun whiteRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if (FILES[file] and bitboards.whitePawns == 0L)
            if (FILES[file] and bitboards.blackPawns == 0L)
                Evaluation.VALUE_ROOK_ON_OPEN_FILE.value
            else
                Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.value
        else 0

fun blackRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if ((FILES[file] and bitboards.blackPawns) == 0L)
            if ((FILES[file] and bitboards.whitePawns) == 0L)
                Evaluation.VALUE_ROOK_ON_OPEN_FILE.value
            else
                Evaluation.VALUE_ROOK_ON_HALF_OPEN_FILE.value
        else 0

fun rookEnemyPawnMultiplier(enemyPawnValues: Int) =
        (enemyPawnValues / pieceValue(Piece.PAWN)).coerceAtMost(6)

fun sameFile(square1: Int, square2: Int) = square1 % 8 == square2 % 8

fun doubledRooksEval(squares: List<Int>) =
        if (squares.size > 1 && sameFile(squares[0], squares[1]))
            Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else
            if (squares.size > 2 && (sameFile(squares[0], squares[2]) || sameFile(squares[1], squares[2])))
                Evaluation.VALUE_ROOKS_ON_SAME_FILE.value else 0

fun flippedSquareTableScore(table: List<Int>, bit: Int) = table[Bitboards.bitFlippedHorizontalAxis[bit]]

fun kingAttackCount(dangerZone: Long, attacks: List<Long>): Int {
    return attacks.asSequence()
            .map { bitCount(it and dangerZone) }
            .fold(0) { acc, i -> acc + i }
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

fun tradePawnBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int) =
    linearScale(
            if (materialDifference > 0) whitePawnValues(bitboards) else blackPawnValues(bitboards),
            0,
            Evaluation.TRADE_BONUS_UPPER_PAWNS.value,
            -30 * materialDifference / 100,
            0)

fun bishopScore(bitboards: BitboardData, materialDifference: Int, materialValues: MaterialValues) =
        bishopPairEval(bitboards, materialValues) +
                oppositeColourBishopsEval(bitboards, materialDifference) + trappedBishopEval(bitboards)

fun whiteLightBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and LIGHT_SQUARES != 0L

fun whiteDarkBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and DARK_SQUARES != 0L

fun blackLightBishopExists(bitboards: BitboardData) = bitboards.blackBishops and LIGHT_SQUARES != 0L

fun blackDarkBishopExists(bitboards: BitboardData) = bitboards.blackBishops and DARK_SQUARES != 0L

fun whiteBishopColourCount(bitboards: BitboardData) =
        (if (whiteLightBishopExists(bitboards)) 1 else 0) + if (whiteDarkBishopExists(bitboards)) 1 else 0

fun blackBishopColourCount(bitboards: BitboardData) =
        (if (blackLightBishopExists(bitboards)) 1 else 0) + if (blackDarkBishopExists(bitboards)) 1 else 0

fun oppositeColourBishopsEval(bitboards: BitboardData, materialDifference: Int): Int {

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

fun bishopPairEval(bitboards: BitboardData, materialValues: MaterialValues) = (if (whiteBishopColourCount(bitboards) == 2)
    Evaluation.VALUE_BISHOP_PAIR.value +
            (8 - materialValues.whitePawns / pieceValue(Piece.PAWN)) *
            Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
else 0) -
        if (blackBishopColourCount(bitboards) == 2)
            Evaluation.VALUE_BISHOP_PAIR.value +
                    (8 - materialValues.blackPawns / pieceValue(Piece.PAWN)) *
                    Evaluation.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS.value
        else 0

fun trappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops or bitboards.blackBishops and A2A7H2H7 != 0L)
            blackA2TrappedBishopEval(bitboards) +
                    blackH2TrappedBishopEval(bitboards) -
                    whiteA7TrappedBishopEval(bitboards) -
                    whiteH7TrappedBishopEval(bitboards)
        else 0

fun blackH2TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.blackBishops and (1L shl Square.H2.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.G3.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.F2.bitRef) != 0L)
            if (bitboards.blackQueens == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
            else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value
        else 0

fun blackA2TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.blackBishops and (1L shl Square.A2.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.B3.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.C2.bitRef) != 0L)
            Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
        else 0

fun whiteH7TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops and (1L shl Square.H7.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.G6.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.F7.bitRef) != 0L)
            if (bitboards.whiteQueens == 0L) Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
            else Evaluation.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY.value
        else 0

fun whiteA7TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops and (1L shl Square.A7.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.B6.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.C7.bitRef) != 0L)
            Evaluation.VALUE_TRAPPED_BISHOP_PENALTY.value
        else 0

fun blackPieceBitboard(bitboards: BitboardData) =
        (bitboards.blackKnights or bitboards.blackRooks or bitboards.blackQueens or bitboards.blackBishops)

fun whitePieceBitboard(bitboards: BitboardData) =
        (bitboards.whiteKnights or bitboards.whiteRooks or bitboards.whiteQueens or bitboards.whiteBishops)

fun isEndGame(bitboards: BitboardData) =
        (whitePieceValues(bitboards) +
                whitePawnValues(bitboards) +
                blackPieceValues(bitboards) +
                blackPawnValues(bitboards)) <= Evaluation.EVAL_ENDGAME_TOTAL_PIECES.value

fun kingSafetyEval(bitboards: BitboardData, attacks: Attacks, board: EngineBoard, kingSquares: KingSquares): Int {

    val whiteKingDangerZone = whiteKingDangerZone(kingSquares)

    val blackKingDangerZone = blackKingDangerZone(kingSquares)

    val blackKingAttackedCount = kingAttackCount(blackKingDangerZone, attacks.whiteRookPair.first) +
            kingAttackCount(blackKingDangerZone, attacks.whiteQueenPair.first) * 2 +
            kingAttackCount(blackKingDangerZone, attacks.whiteBishopPair.first)

    val whiteKingAttackedCount = kingAttackCount(whiteKingDangerZone, attacks.blackRookPair.first) +
            kingAttackCount(whiteKingDangerZone, attacks.blackQueenPair.first) * 2 +
            kingAttackCount(whiteKingDangerZone, attacks.blackBishopPair.first)

    val averagePiecesPerSide = (whitePieceValues(bitboards) + blackPieceValues(bitboards)) / 2

    if (averagePiecesPerSide <= Evaluation.KINGSAFETY_MIN_PIECE_BALANCE.value) {
        return 0
    }

    val whiteKingSafety: Int = getWhiteKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + whiteKingShieldEval(bitboards, kingSquares)

    val blackKingSafety: Int = getBlackKingRightWayScore(board) +
            Evaluation.KINGSAFETY_SHIELD_BASE.value + blackKingShieldEval(bitboards, kingSquares)

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

private fun blackKingDangerZone(kingSquares: KingSquares) =
        Bitboards.kingMoves[kingSquares.black] or
                (Bitboards.kingMoves[kingSquares.black] ushr 8)

private fun whiteKingDangerZone(kingSquares: KingSquares) =
        Bitboards.kingMoves[kingSquares.white] or
                (Bitboards.kingMoves[kingSquares.white] shl 8)

fun uncastledTrappedWhiteRookEval(bitboards: BitboardData) =
        if (bitboards.whiteKing and F1G1 != 0L &&
                bitboards.whiteRooks and G1H1 != 0L &&
                bitboards.whitePawns and FILE_G != 0L &&
                bitboards.whitePawns and FILE_H != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else (if (bitboards.whiteKing and B1C1 != 0L &&
                bitboards.whiteRooks and A1B1 != 0L &&
                bitboards.whitePawns and FILE_A != 0L &&
                bitboards.whitePawns and FILE_B != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else 0)

fun pawnShieldEval(friendlyPawns: Long, enemyPawns: Long, friendlyPawnShield: Long, shifter: Long.(Int) -> Long) =
        (Evaluation.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT.value * bitCount(friendlyPawns and friendlyPawnShield)
                - Evaluation.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT.value * bitCount(enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield,8)))
                + Evaluation.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT.value * bitCount(friendlyPawns and shifter(friendlyPawnShield,8))
                - Evaluation.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT.value * bitCount(enemyPawns and shifter(friendlyPawnShield,16)))

fun uncastledTrappedBlackRookEval(bitboards: BitboardData) =
        if (bitboards.blackKing and F8G8 != 0L &&
                bitboards.blackRooks and G8H8 != 0L &&
                bitboards.blackPawns and FILE_G != 0L &&
                bitboards.blackPawns and FILE_H != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else (if (bitboards.blackKing and B8C8 != 0L &&
                bitboards.blackRooks and A8B8 != 0L &&
                bitboards.blackPawns and FILE_A != 0L &&
                bitboards.blackPawns and FILE_B != 0L)
            Evaluation.KINGSAFETY_UNCASTLED_TRAPPED_ROOK.value
        else 0)

fun openFiles(kingShield: Long, pawnBitboard: Long) =
        southFill(kingShield, 8) and southFill(pawnBitboard, 8).inv() and RANK_1

fun whiteKingShieldEval(bitboards: BitboardData, kingSquares: KingSquares) =
        if (whiteKingOnFirstTwoRanks(kingSquares)) {
            combineWhiteKingShieldEval(bitboards, whiteKingShield(kingSquares))
        } else 0

fun combineWhiteKingShieldEval(bitboards: BitboardData, kingShield: Long) =
        pawnShieldEval(bitboards.whitePawns, bitboards.blackPawns, kingShield, Long::shl)
                .coerceAtMost(Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value) -
                uncastledTrappedWhiteRookEval(bitboards) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.whitePawns)) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.blackPawns))

fun openFilesKingShieldEval(openFiles: Long) =
        if (openFiles != 0L) {
            Evaluation.KINGSAFTEY_HALFOPEN_MIDFILE.value * bitCount(openFiles and MIDDLE_FILES_8_BIT) +
                    Evaluation.KINGSAFTEY_HALFOPEN_NONMIDFILE.value * bitCount(openFiles and NONMID_FILES_8_BIT)
        } else 0

fun blackKingShieldEval(bitboards: BitboardData, kingSquares: KingSquares) =
        if (blackKingOnFirstTwoRanks(kingSquares)) {
            combineBlackKingShieldEval(bitboards, blackKingShield(kingSquares))
        } else 0

fun combineBlackKingShieldEval(bitboards: BitboardData, kingShield: Long) =
        pawnShieldEval(bitboards.blackPawns, bitboards.whitePawns, kingShield, Long::ushr)
                .coerceAtMost(Evaluation.KINGSAFTEY_MAXIMUM_SHIELD_BONUS.value) -
                uncastledTrappedBlackRookEval(bitboards) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.whitePawns)) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.blackPawns))

fun whiteKingOnFirstTwoRanks(kingSquares: KingSquares) =
        yCoordOfSquare(kingSquares.white) < 2

fun blackKingOnFirstTwoRanks(kingSquares: KingSquares) =
        yCoordOfSquare(kingSquares.black) >= 6

fun blackKingShield(kingSquares: KingSquares) =
        Bitboards.whiteKingShieldMask[kingSquares.black % 8] shl 40

fun whiteKingShield(kingSquares: KingSquares): Long =
        Bitboards.whiteKingShieldMask[kingSquares.white % 8]

fun whiteCastlingEval(bitboards: BitboardData, castlePrivileges: Int) : Int {

    val whiteCastleValue = maxCastleValue(blackPieceValues(bitboards))

    return if (whiteCastleValue > 0) {
        whiteCastleValue / whiteTimeToCastleKingSide(castlePrivileges, bitboards)
                .coerceAtMost(whiteTimeToCastleQueenSide(castlePrivileges, bitboards))
    } else 0
}

fun whiteTimeToCastleQueenSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_WQ.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 6) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 5) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 4) != 0L) 1 else 0)
        } else 100


fun whiteTimeToCastleKingSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_WK.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 1) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 2) != 0L) 1 else 0)
        } else 100

fun blackTimeToCastleQueenSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_BQ.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 60) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 61) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 62) != 0L) 1 else 0)
        } else 100


fun blackTimeToCastleKingSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CastleBitMask.CASTLEPRIV_BK.value != 0) {
            2 +
                    (if (bitboards.all and (1L shl 57) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 58) != 0L) 1 else 0)
        } else 100

fun blackCastlingEval(bitboards: BitboardData, castlePrivileges: Int) : Int {
    // Value of moving King to its queenside castle destination in the middle game
    val blackCastleValue = maxCastleValue(whitePieceValues(bitboards))

    return if (blackCastleValue > 0) {
        blackCastleValue / blackTimeToCastleKingSide(castlePrivileges, bitboards)
                .coerceAtMost(blackTimeToCastleQueenSide(castlePrivileges, bitboards))
    } else 0
}

fun rookSquareBonus() = PieceSquareTables.rook[3] - PieceSquareTables.rook[0]

fun kingSquareBonusEndGame() = PieceSquareTables.kingEndGame[1] - PieceSquareTables.kingEndGame[3]

fun kingSquareBonusMiddleGame() = PieceSquareTables.king[1] - PieceSquareTables.king[3]

fun castlingEval(bitboards: BitboardData, castlePrivileges: Int) =
        if (isAnyCastleAvailable(castlePrivileges)) {
            whiteCastlingEval(bitboards, castlePrivileges) - blackCastlingEval(bitboards, castlePrivileges)
        } else 0

// don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
// given by still having castling rights.
fun maxCastleValue(pieceValues: Int) =
        kingSquareBonusScaled(pieceValues, kingSquareBonusEndGame(), kingSquareBonusMiddleGame()) + rookSquareBonus()

fun kingSquareBonusScaled(pieceValues: Int, kingSquareBonusEndGame: Int, kingSquareBonusMiddleGame: Int) =
        linearScale(
                pieceValues,
                Evaluation.CASTLE_BONUS_LOW_MATERIAL.value,
                Evaluation.CASTLE_BONUS_HIGH_MATERIAL.value,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)

fun isAnyCastleAvailable(castlePrivileges: Int) = castlePrivileges != 0

fun endGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(bitboards)) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(bitboards, currentScore, kingSquares)
            else -> blackWinningEndGameAdjustment(bitboards, currentScore, kingSquares)
        }

fun blackWinningEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (blackHasInsufficientMaterial(bitboards)) currentScore + (blackPieceValues(bitboards) * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt()
        else if (probableDrawWhenBlackIsWinning(bitboards)) currentScore / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value
        else if (noBlackRooksQueensOrBishops(bitboards) && (blackBishopDrawOnFileA(bitboards) || blackBishopDrawOnFileH(bitboards))) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
        else if (whitePawnValues(bitboards) == 0) blackWinningNoWhitePawnsEndGameAdjustment(bitboards, currentScore, kingSquares)
        else currentScore

fun blackWinningNoWhitePawnsEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (blackMoreThanABishopUpInNonPawns(bitboards)) {
            if (blackHasOnlyTwoKnights(bitboards) && whitePieceValues(bitboards) == 0) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
            else if (blackHasOnlyAKnightAndBishop(bitboards) && whitePieceValues(bitboards) == 0) {
                blackKnightAndBishopVKingEval(currentScore, bitboards, kingSquares)
            } else currentScore - Evaluation.VALUE_SHOULD_WIN.value
        } else currentScore

fun blackKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData, kingSquares: KingSquares): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (blackDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(kingSquares.white)
    else enemyKingCloseToLightCornerMateSquareValue(kingSquares.white)
}

fun whiteWinningEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (whiteHasInsufficientMaterial(bitboards)) currentScore - (whitePieceValues(bitboards) * Evaluation.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER).toInt()
        else if (probablyDrawWhenWhiteIsWinning(bitboards)) currentScore / Evaluation.ENDGAME_PROBABLE_DRAW_DIVISOR.value
        else if (noWhiteRooksQueensOrKnights(bitboards) && (whiteBishopDrawOnFileA(bitboards) || whiteBishopDrawOnFileH(bitboards))) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
        else if (blackPawnValues(bitboards) == 0) whiteWinningNoBlackPawnsEndGameAdjustment(bitboards, currentScore, kingSquares)
        else currentScore

fun whiteWinningNoBlackPawnsEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (whiteMoreThanABishopUpInNonPawns(bitboards)) {
            if (whiteHasOnlyTwoKnights(bitboards) && blackPieceValues(bitboards) == 0) currentScore / Evaluation.ENDGAME_DRAW_DIVISOR.value
            else if (whiteHasOnlyAKnightAndBishop(bitboards) && blackPieceValues(bitboards) == 0) whiteKnightAndBishopVKingEval(currentScore, bitboards, kingSquares)
            else currentScore + Evaluation.VALUE_SHOULD_WIN.value
        } else currentScore

fun whiteKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData, kingSquares: KingSquares): Int {
    whiteShouldWinWithKnightAndBishopValue(currentScore)
    return +if (whiteDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(kingSquares.black)
    else enemyKingCloseToLightCornerMateSquareValue(kingSquares.black)
}

fun enemyKingCloseToDarkCornerMateSquareValue(kingSquare: Int) =
        enemyKingCloseToLightCornerMateSquareValue(Bitboards.bitFlippedHorizontalAxis[kingSquare])

fun enemyKingCloseToLightCornerMateSquareValue(kingSquare: Int) =
        (7 - Bitboards.distanceToH1OrA8[kingSquare]) * Evaluation.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE.value

fun blackShouldWinWithKnightAndBishopValue(eval: Int) =
        -(pieceValue(Piece.KNIGHT) + pieceValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value) +
                eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value

fun whiteShouldWinWithKnightAndBishopValue(eval: Int) =
        pieceValue(Piece.KNIGHT) + pieceValue(Piece.BISHOP) + Evaluation.VALUE_SHOULD_WIN.value + eval / Evaluation.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR.value

fun whiteHasOnlyAKnightAndBishop(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) == 1 && (whitePieceValues(bitboards) == pieceValue(Piece.KNIGHT) + pieceValue(Piece.BISHOP))

fun blackHasOnlyAKnightAndBishop(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) == 1 && (blackPieceValues(bitboards) == pieceValue(Piece.KNIGHT) + pieceValue(Piece.BISHOP))

fun whiteHasOnlyTwoKnights(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) == 2 && (whitePieceValues(bitboards) == 2 * pieceValue(Piece.KNIGHT))

fun blackHasOnlyTwoKnights(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) == 2 && (blackPieceValues(bitboards) == 2 * pieceValue(Piece.KNIGHT))

fun blackMoreThanABishopUpInNonPawns(bitboards: BitboardData) =
        blackPieceValues(bitboards) - whitePieceValues(bitboards) > pieceValue(Piece.BISHOP)

fun whiteMoreThanABishopUpInNonPawns(bitboards: BitboardData) =
        whitePieceValues(bitboards) - blackPieceValues(bitboards) > pieceValue(Piece.BISHOP)

fun noBlackRooksQueensOrBishops(bitboards: BitboardData) =
        bitboards.blackRooks or bitboards.blackKnights or bitboards.blackQueens == 0L

fun bothSidesHaveOnlyOneKnightOrBishopEach(bitboards: BitboardData) =
        noPawnsRemain(bitboards) && whitePieceValues(bitboards) < pieceValue(Piece.ROOK) &&
                blackPieceValues(bitboards) < pieceValue(Piece.ROOK)

fun noPawnsRemain(bitboards: BitboardData) =
        whitePawnValues(bitboards) + blackPawnValues(bitboards) == 0

fun noWhiteRooksQueensOrKnights(bitboards: BitboardData) =
        bitboards.whiteRooks or bitboards.whiteKnights or bitboards.whiteQueens == 0L

fun blackBishopDrawOnFileH(bitboards: BitboardData): Boolean {
    return bitboards.blackPawns and FILE_H.inv() == 0L &&
            bitboards.blackBishops and LIGHT_SQUARES == 0L &&
            bitboards.whiteKing and H1H2G1G2 != 0L
}

fun blackBishopDrawOnFileA(bitboards: BitboardData): Boolean {
    return bitboards.blackPawns and FILE_A.inv() == 0L &&
            bitboards.blackBishops and DARK_SQUARES == 0L &&
            bitboards.whiteKing and A1A2B1B2 != 0L
}

fun whiteBishopDrawOnFileA(bitboards: BitboardData): Boolean {
    return bitboards.whitePawns and FILE_A.inv() == 0L &&
            bitboards.whiteBishops and LIGHT_SQUARES == 0L &&
            bitboards.blackKing and A8A7B8B7 != 0L
}

fun whiteBishopDrawOnFileH(bitboards: BitboardData): Boolean {
    return bitboards.whitePawns and FILE_H.inv() == 0L &&
            bitboards.whiteBishops and DARK_SQUARES == 0L &&
            bitboards.blackKing and H8H7G8G7 != 0L
}

fun probableDrawWhenBlackIsWinning(bitboards: BitboardData) =
        blackPawnValues(bitboards) == 0 && blackPieceValues(bitboards) - pieceValue(Piece.BISHOP) <= whitePieceValues(bitboards)

fun probablyDrawWhenWhiteIsWinning(bitboards: BitboardData) =
        whitePawnValues(bitboards) == 0 && whitePieceValues(bitboards) - pieceValue(Piece.BISHOP) <= blackPieceValues(bitboards)

fun blackHasInsufficientMaterial(bitboards: BitboardData) =
        blackPawnValues(bitboards) == 0 && (blackPieceValues(bitboards) == pieceValue(Piece.KNIGHT) ||
                blackPieceValues(bitboards) == pieceValue(Piece.BISHOP))

fun whiteHasInsufficientMaterial(bitboards: BitboardData) =
        whitePawnValues(bitboards) == 0 && (whitePieceValues(bitboards) == pieceValue(Piece.KNIGHT) ||
                whitePieceValues(bitboards) == pieceValue(Piece.BISHOP))


fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        bitCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * Evaluation.KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY.value

fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        Bitboards.knightMoves[square] and (enemyPawnAttacks or friendlyPawns)

fun whitePawnsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData): Int {
    return pieceSquareLists.whitePawns.asSequence().map {
        linearScale(
                blackPieceValues(bitboards),
                Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
                Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.pawnEndGame[it],
                PieceSquareTables.pawn[it]
        )
    }.fold(0) { acc, i -> acc + i }
}

fun blackPawnsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData): Int {
    return pieceSquareLists.blackPawns.asSequence().map {
        linearScale(
                whitePieceValues(bitboards),
                Evaluation.PAWN_STAGE_MATERIAL_LOW.value,
                Evaluation.PAWN_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.pawnEndGame[Bitboards.bitFlippedHorizontalAxis[it]],
                PieceSquareTables.pawn[Bitboards.bitFlippedHorizontalAxis[it]]
        )
    }.fold(0) { acc, i -> acc + i }
}

fun blackKnightsEval(
        pieceSquareLists: PieceSquareLists,
        bitboards: BitboardData,
        attacks: Attacks,
        materialValues: MaterialValues)
        : Int {
    return pieceSquareLists.blackKnights.asSequence().map {
        linearScale(
                materialValues.whitePieces + materialValues.whitePawns,
                Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.knightEndGame[Bitboards.bitFlippedHorizontalAxis[it]],
                PieceSquareTables.knight[Bitboards.bitFlippedHorizontalAxis[it]]
        ) -
        blockedKnightPenaltyEval(it, attacks.whitePawns, bitboards.blackPawns)
    }.fold(0) { acc, i -> acc + i }
}

fun whiteKnightsEval(
        pieceSquareLists: PieceSquareLists,
        bitboards: BitboardData,
        attacks: Attacks,
        materialValues: MaterialValues)
        : Int {

    return pieceSquareLists.whiteKnights.asSequence().map {
        linearScale(materialValues.blackPieces + materialValues.blackPawns,
                Evaluation.KNIGHT_STAGE_MATERIAL_LOW.value,
                Evaluation.KNIGHT_STAGE_MATERIAL_HIGH.value,
                PieceSquareTables.knightEndGame[it],
                PieceSquareTables.knight[it]
        ) -
        blockedKnightPenaltyEval(it, attacks.blackPawns, bitboards.whitePawns)
    }.fold(0) { acc, i -> acc + i }
}

fun blackBishopsEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, blackPieces: Long): Int {
    return pieceSquareLists.blackBishops.asSequence().map {
        Evaluation.getBishopMobilityValue(bitCount(bishopAttacks(bitboards, it) and blackPieces.inv())) +
                flippedSquareTableScore(PieceSquareTables.bishop, it)
    }.fold(0) { acc, i -> acc + i }
}

fun whiteBishopEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, whitePieces: Long): Int {
    return pieceSquareLists.whiteBishops.asSequence().map {
        Evaluation.getBishopMobilityValue(bitCount(bishopAttacks(bitboards, it) and whitePieces.inv())) +
                PieceSquareTables.bishop[it]
    }.fold(0) { acc, i -> acc + i }
}

private fun blackQueensEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, blackPieces: Long): Int {
    return pieceSquareLists.blackQueens.asSequence().map {
        Evaluation.getQueenMobilityValue(bitCount(queenAttacks(bitboards, it) and blackPieces.inv())) +
                flippedSquareTableScore(PieceSquareTables.queen, it)
    }.fold(0) { acc, i -> acc + i }
}

private fun whiteQueensEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, whitePieces: Long): Int {
    return pieceSquareLists.whiteQueens.asSequence().map {
        Evaluation.getQueenMobilityValue(bitCount(queenAttacks(bitboards, it) and whitePieces.inv())) +
                PieceSquareTables.queen[it]
    }.fold(0) { acc, i -> acc + i }
}

private fun blackRooksEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, blackPieces: Long): Int {
    return pieceSquareLists.blackRooks.asSequence().map {
        blackRookOpenFilesEval(bitboards, it % 8) +
                Evaluation.getRookMobilityValue(bitCount(rookAttacks(bitboards, it) and blackPieces.inv())) +
                flippedSquareTableScore(PieceSquareTables.rook, it) * rookEnemyPawnMultiplier(whitePawnValues(bitboards)) / 6
    }.fold(0) { acc, i -> acc + i }
}

fun whiteRooksEval(pieceSquareLists: PieceSquareLists, bitboards: BitboardData, whitePieces: Long): Int {
    return pieceSquareLists.whiteRooks.asSequence().map {
        whiteRookOpenFilesEval(bitboards, it % 8) +
                Evaluation.getRookMobilityValue(bitCount(rookAttacks(bitboards, it) and whitePieces.inv())) +
                PieceSquareTables.rook[it] * rookEnemyPawnMultiplier(blackPawnValues(bitboards)) / 6
    }.fold(0) { acc, i -> acc + i }
}

fun pawnScore(whitePawnBitboard: Long,
              blackPawnBitboard: Long,
              attacks: Attacks,
              materialValues: MaterialValues,
              whiteKingSquare: Int,
              blackKingSquare: Int,
              mover: Colour
): Int {

    val whitePawnAttacks = attacks.whitePawns
    val blackPawnAttacks = attacks.blackPawns

    val whitePawnFiles = getPawnFiles(whitePawnBitboard)
    val blackPawnFiles = getPawnFiles(blackPawnBitboard)

    val whitePassedPawnsBitboard = getWhitePassedPawns(whitePawnBitboard, blackPawnBitboard)
    val whiteGuardedPassedPawns = whitePassedPawnsBitboard and whitePawnAttacks
    val blackPassedPawnsBitboard = getBlackPassedPawns(whitePawnBitboard, blackPawnBitboard)
    val blackGuardedPassedPawns = blackPassedPawnsBitboard and blackPawnAttacks

    val whiteIsolatedPawns = whitePawnFiles and (whitePawnFiles shl 1).inv() and (whitePawnFiles ushr 1).inv()
    val blackIsolatedPawns = blackPawnFiles and (blackPawnFiles shl 1).inv() and (blackPawnFiles ushr 1).inv()
    val whiteOccupiedFileMask = southFill(whitePawnBitboard, 8) and RANK_1
    val blackOccupiedFileMask = southFill(blackPawnBitboard, 8) and RANK_1

    val whitePassedPawnScore = bitCount(whiteGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.value +
            squareList(whitePassedPawnsBitboard).asSequence()
                    .map { Evaluation.getPassedPawnBonus(yCoordOfSquare(it)) }.fold(0) { acc, i -> acc + i }

    val blackPassedPawnScore = bitCount(blackGuardedPassedPawns) * Evaluation.VALUE_GUARDED_PASSED_PAWN.value +
            squareList(blackPassedPawnsBitboard).asSequence()
                    .map { Evaluation.getPassedPawnBonus(7 - yCoordOfSquare(it)) }.fold(0) { acc, i -> acc + i }

    return bitCount(blackIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.value -
            bitCount(whiteIsolatedPawns) * Evaluation.VALUE_ISOLATED_PAWN_PENALTY.value -
            (if (whiteIsolatedPawns and FILE_D != 0L) Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.value else 0) +
            (if (blackIsolatedPawns and FILE_D != 0L) Evaluation.VALUE_ISOLATED_DPAWN_PENALTY.value else 0) -
            (bitCount(
                    whitePawnBitboard and
                            (whitePawnBitboard or blackPawnBitboard ushr 8).inv() and
                            (blackPawnAttacks ushr 8) and
                            northFill(whitePawnAttacks, 8).inv() and
                            blackPawnAttacks(whitePawnBitboard) and
                            northFill(blackPawnFiles, 8).inv()
            ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.value) +
            (bitCount(
                    blackPawnBitboard and
                            (blackPawnBitboard or whitePawnBitboard shl 8).inv() and
                            (whitePawnAttacks shl 8) and
                            southFill(blackPawnAttacks, 8).inv() and
                            whitePawnAttacks(blackPawnBitboard) and
                            northFill(whitePawnFiles, 8).inv()
            ) * Evaluation.VALUE_BACKWARD_PAWN_PENALTY.value) -
            ((bitCount(whitePawnBitboard and FILE_A) + bitCount(whitePawnBitboard and FILE_H))
                    * Evaluation.VALUE_SIDE_PAWN_PENALTY.value) +
            ((bitCount(blackPawnBitboard and FILE_A) + bitCount(blackPawnBitboard and FILE_H))
                    * Evaluation.VALUE_SIDE_PAWN_PENALTY.value) -
            Evaluation.VALUE_DOUBLED_PAWN_PENALTY.value * (materialValues.whitePawns / 100 - bitCount(whiteOccupiedFileMask)) -
            bitCount(whiteOccupiedFileMask.inv() ushr 1 and whiteOccupiedFileMask) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.value +
            Evaluation.VALUE_DOUBLED_PAWN_PENALTY.value * (materialValues.blackPawns / 100 - bitCount(blackOccupiedFileMask)) +
            bitCount(blackOccupiedFileMask.inv() ushr 1 and blackOccupiedFileMask) * Evaluation.VALUE_PAWN_ISLAND_PENALTY.value +
            (linearScale(materialValues.blackPieces, 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.value, whitePassedPawnScore * 2, whitePassedPawnScore)) -
            (linearScale(materialValues.whitePieces, 0, Evaluation.PAWN_ADJUST_MAX_MATERIAL.value, blackPassedPawnScore * 2, blackPassedPawnScore)) +
            (if (materialValues.blackPieces < Evaluation.PAWN_ADJUST_MAX_MATERIAL.value)
                calculateLowMaterialPawnBonus(
                        Colour.BLACK,
                        whiteKingSquare,
                        blackKingSquare,
                        materialValues,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        mover)
            else 0) +
            (if (materialValues.whitePieces < Evaluation.PAWN_ADJUST_MAX_MATERIAL.value)
                calculateLowMaterialPawnBonus(
                        Colour.WHITE,
                        whiteKingSquare,
                        blackKingSquare,
                        materialValues,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        mover)
            else 0)
}

fun calculateLowMaterialPawnBonus(
        lowMaterialColour: Colour,
        whiteKingSquare: Int,
        blackKingSquare: Int,
        materialValues: MaterialValues,
        whitePassedPawnsBitboard: Long,
        blackPassedPawnsBitboard: Long,
        mover: Colour
): Int {

    val kingSquare = if (lowMaterialColour == Colour.WHITE) whiteKingSquare else blackKingSquare
    val kingX = xCoordOfSquare(kingSquare)
    val kingY = yCoordOfSquare(kingSquare)
    val lowMaterialSidePieceValues = if (lowMaterialColour == Colour.WHITE) materialValues.whitePieces else materialValues.blackPieces

    return squareList(if (lowMaterialColour == Colour.WHITE) blackPassedPawnsBitboard else whitePassedPawnsBitboard)
            .asSequence().map {
                val pawnDistance = pawnDistanceFromPromotion(lowMaterialColour, it).coerceAtMost(5)
                val kingXDistanceFromPawn = difference(kingX, it)
                val kingYDistanceFromPawn = difference(colourAdjustedYRank(lowMaterialColour, kingY), it)
                val kingDistanceFromPawn = kingXDistanceFromPawn.coerceAtLeast(kingYDistanceFromPawn)

                val moverAdjustment = if (lowMaterialColour == mover) 1 else 0

                val scoreAdjustment = linearScale(
                        lowMaterialSidePieceValues,
                        0,
                        Evaluation.PAWN_ADJUST_MAX_MATERIAL.value,
                        kingDistanceFromPawn * 4,
                        0) +
                        if (pawnDistance < kingDistanceFromPawn - moverAdjustment && lowMaterialSidePieceValues == 0) {
                            Evaluation.VALUE_KING_CANNOT_CATCH_PAWN.value
                        } else 0

                if (lowMaterialColour == Colour.WHITE) -scoreAdjustment else scoreAdjustment
            }.fold(0) { acc, i -> acc + i }

}

private fun colourAdjustedYRank(colour: Colour, yRank: Int) =
        if (colour == Colour.WHITE) yRank else abs(yRank - 7)

private fun difference(kingX: Int, it: Int) = abs(kingX - xCoordOfSquare(it))

private fun xCoordOfSquare(it: Int) = it % 8

private fun pawnDistanceFromPromotion(colour: Colour, square: Int) =
        if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

private fun yCoordOfSquare(kingSquare: Int) = kingSquare / 8

fun evaluate(board: EngineBoard) : Int {

    val bitboards = BitboardData(board)
    val pieceSquareLists = PieceSquareLists(bitboards)
    val attacks = Attacks(bitboards, pieceSquareLists)
    val materialValues = MaterialValues(bitboards)
    val kingSquares = KingSquares(bitboards)

    if (onlyKingsRemain(bitboards)) {
        return 0
    } else {
        val whitePieces = if (board.mover == Colour.WHITE) bitboards.friendly else bitboards.enemy
        val blackPieces = if (board.mover == Colour.WHITE) bitboards.enemy else bitboards.friendly

        val materialDifference = materialDifferenceEval(materialValues)

        val eval =  materialDifference +
                    (twoWhiteRooksTrappingKingEval(bitboards) - twoBlackRooksTrappingKingEval(bitboards)) +
                    (doubledRooksEval(pieceSquareLists.whiteRooks) - doubledRooksEval(pieceSquareLists.blackRooks)) +
                    pawnScore(bitboards.whitePawns,
                            bitboards.blackPawns,
                            attacks, materialValues,
                            board.whiteKingSquare,
                            board.blackKingSquare,
                            board.mover) +
                    (whiteBishopEval(pieceSquareLists, bitboards, whitePieces) -
                            blackBishopsEval(pieceSquareLists, bitboards, blackPieces)) +
                    (whiteKnightsEval(pieceSquareLists, bitboards, attacks, materialValues) -
                            blackKnightsEval(pieceSquareLists, bitboards, attacks, materialValues)) +
                    tradePawnBonusWhenMoreMaterial(bitboards, materialDifference) +
                    tradePieceBonusWhenMoreMaterial(bitboards, materialDifference) +
                    (whiteKingSquareEval(bitboards, kingSquares) - blackKingSquareEval(bitboards, kingSquares)) +
                    (whitePawnsEval(pieceSquareLists, bitboards) - blackPawnsEval(pieceSquareLists, bitboards)) +
                    (whiteRooksEval(pieceSquareLists, bitboards, whitePieces) - blackRooksEval(pieceSquareLists, bitboards, blackPieces)) +
                    (whiteQueensEval(pieceSquareLists, bitboards, whitePieces) - blackQueensEval(pieceSquareLists, bitboards, blackPieces)) +
                    castlingEval(bitboards, board.castlePrivileges) +
                    bishopScore(bitboards, materialDifference, materialValues) +
                    threatEval(bitboards, attacks, board.squareOccupants) +
                    kingSafetyEval(bitboards, attacks, board, kingSquares)

        val endGameAdjustedScore = if (isEndGame(bitboards)) endGameAdjustment(bitboards, eval, kingSquares) else eval

        return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore
    }
}
