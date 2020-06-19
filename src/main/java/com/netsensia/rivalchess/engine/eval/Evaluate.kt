package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.CASTLEPRIV_BK
import com.netsensia.rivalchess.consts.CASTLEPRIV_BQ
import com.netsensia.rivalchess.consts.CASTLEPRIV_WK
import com.netsensia.rivalchess.consts.CASTLEPRIV_WQ
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square
import java.lang.Long.bitCount
import kotlin.math.abs

fun evaluate(board: EngineBoard): Int {

    val bitboards = BitboardData(board)

    if (onlyKingsRemain(bitboards)) return 0

    val attacks = Attacks(bitboards)
    val materialValues = MaterialValues(bitboards)
    val kingSquares = KingSquares(bitboards)

    val whitePieces = if (board.mover == Colour.WHITE) bitboards.friendly else bitboards.enemy
    val blackPieces = if (board.mover == Colour.WHITE) bitboards.enemy else bitboards.friendly

    val materialDifference = materialDifferenceEval(materialValues)

    val eval =  materialDifference +
            (twoWhiteRooksTrappingKingEval(bitboards) - twoBlackRooksTrappingKingEval(bitboards)) +
            (doubledRooksEval(squareList(bitboards.whiteRooks)) - doubledRooksEval(squareList(bitboards.blackRooks))) +
            (whiteRooksEval(bitboards, whitePieces) - blackRooksEval(bitboards, blackPieces)) +
            pawnScore(bitboards.whitePawns, bitboards.blackPawns, attacks, materialValues, board.whiteKingSquare, board.blackKingSquare, board.mover) +
            tradePawnBonusWhenMoreMaterial(bitboards, materialDifference) +
            (whitePawnsEval(bitboards) - blackPawnsEval(bitboards)) +
            (whiteBishopEval(bitboards, whitePieces) - blackBishopsEval(bitboards, blackPieces)) +
            (whiteKnightsEval(bitboards, attacks, materialValues) - blackKnightsEval(bitboards, attacks, materialValues)) +
            (whiteKingSquareEval(bitboards, kingSquares) - blackKingSquareEval(bitboards, kingSquares)) +
            tradePieceBonusWhenMoreMaterial(bitboards, materialDifference) +
            castlingEval(bitboards, board.castlePrivileges) +
            threatEval(bitboards, attacks, board) +
            kingSafetyEval(bitboards, attacks, board, kingSquares) +
            (whiteQueensEval(bitboards, whitePieces) - blackQueensEval(bitboards, blackPieces)) +
            bishopScore(bitboards, materialDifference, materialValues)

    val endGameAdjustedScore = if (isEndGame(bitboards)) endGameAdjustment(bitboards, eval, kingSquares) else eval

    return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore

}

fun materialDifferenceEval(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces +
                materialValues.whitePawns - materialValues.blackPawns

fun onlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L

fun onlyKingsRemain(bitboards: BitboardData) = onlyOneBitSet(bitboards.enemy) and onlyOneBitSet(bitboards.friendly)

fun whiteKingSquareEval(bitboards: BitboardData, kingSquares: KingSquares) =
        linearScale(
                blackPieceValues(bitboards),
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                PieceSquareTables.kingEndGame[kingSquares.white],
                PieceSquareTables.king[kingSquares.white]
        )

fun blackKingSquareEval(bitboards: BitboardData, kingSquares: KingSquares) =
        linearScale(
                whitePieceValues(bitboards),
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                PieceSquareTables.kingEndGame[bitFlippedHorizontalAxis[kingSquares.black]],
                PieceSquareTables.king[bitFlippedHorizontalAxis[kingSquares.black]]
        )

fun linearScale(x: Int, min: Int, max: Int, a: Int, b: Int) =
        when {
            x < min -> a
            x > max -> b
            else -> a + (x - min) * (b - a) / (max - min)
        }

fun twoWhiteRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.whiteRooks and RANK_7) > 1 && bitboards.blackKing and RANK_8 != 0L)
            VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING else 0

fun twoBlackRooksTrappingKingEval(bitboards: BitboardData) =
        if (bitCount(bitboards.blackRooks and RANK_2) > 1 && bitboards.whiteKing and RANK_1 != 0L)
            VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING else 0

fun whiteRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if (FILES[file] and bitboards.whitePawns == 0L)
            if (FILES[file] and bitboards.blackPawns == 0L) VALUE_ROOK_ON_OPEN_FILE
            else VALUE_ROOK_ON_HALF_OPEN_FILE
        else 0

fun blackRookOpenFilesEval(bitboards: BitboardData, file: Int) =
        if ((FILES[file] and bitboards.blackPawns) == 0L)
            if ((FILES[file] and bitboards.whitePawns) == 0L) VALUE_ROOK_ON_OPEN_FILE
            else VALUE_ROOK_ON_HALF_OPEN_FILE
        else 0

fun rookEnemyPawnMultiplier(enemyPawnValues: Int) = (enemyPawnValues / VALUE_PAWN).coerceAtMost(6)
fun sameFile(square1: Int, square2: Int) = square1 % 8 == square2 % 8

fun doubledRooksEval(squares: List<Int>) =
        if (squares.size > 1 && sameFile(squares[0], squares[1]))
            VALUE_ROOKS_ON_SAME_FILE else
            if (squares.size > 2 && (sameFile(squares[0], squares[2]) || sameFile(squares[1], squares[2])))
                VALUE_ROOKS_ON_SAME_FILE else 0

fun flippedSquareTableScore(table: IntArray, bit: Int) = table[bitFlippedHorizontalAxis[bit]]

fun kingAttackCount(dangerZone: Long, attacks: LongArray): Int {
    var acc = 0
    attacks.forEach {
        if (it == -1L) return@forEach
        acc += bitCount(it and dangerZone)
    }
    return acc
}

fun tradePieceBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int) =
        linearScale(
                if (materialDifference > 0)
                    blackPieceValues(bitboards) + blackPawnValues(bitboards) else
                    whitePieceValues(bitboards) + whitePawnValues(bitboards),
                0,
                TOTAL_PIECE_VALUE_PER_SIDE_AT_START,
                30 * materialDifference / 100,
                0)

fun tradePawnBonusWhenMoreMaterial(bitboards: BitboardData, materialDifference: Int) =
        linearScale(
                if (materialDifference > 0) whitePawnValues(bitboards) else blackPawnValues(bitboards),
                0,
                PAWN_TRADE_BONUS_MAX,
                -30 * materialDifference / 100,
                0)

fun bishopScore(bitboards: BitboardData, materialDifference: Int, materialValues: MaterialValues) =
        bishopPairEval(bitboards, materialValues) +
                oppositeColourBishopsEval(bitboards, materialDifference) + trappedBishopEval(bitboards)

fun whiteLightBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and LIGHT_SQUARES != 0L
fun whiteDarkBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and DARK_SQUARES != 0L
fun blackLightBishopExists(bitboards: BitboardData) = bitboards.blackBishops and LIGHT_SQUARES != 0L
fun blackDarkBishopExists(bitboards: BitboardData) = bitboards.blackBishops and DARK_SQUARES != 0L
fun whiteBishopColourCount(bitboards: BitboardData) = (if (whiteLightBishopExists(bitboards)) 1 else 0) + if (whiteDarkBishopExists(bitboards)) 1 else 0
fun blackBishopColourCount(bitboards: BitboardData) = (if (blackLightBishopExists(bitboards)) 1 else 0) + if (blackDarkBishopExists(bitboards)) 1 else 0

fun oppositeColourBishopsEval(bitboards: BitboardData, materialDifference: Int): Int {

    if (whiteBishopColourCount(bitboards) == 1 && blackBishopColourCount(bitboards) == 1 &&
            whiteLightBishopExists(bitboards) != blackLightBishopExists(bitboards) &&
            whitePieceValues(bitboards) == blackPieceValues(bitboards)) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / WRONG_COLOUR_BISHOP_PENALTY_DIVISOR // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScale(
                whitePieceValues(bitboards) + blackPieceValues(bitboards),
                WRONG_COLOUR_BISHOP_MATERIAL_LOW,
                WRONG_COLOUR_BISHOP_MATERIAL_HIGH,
                maxPenalty,
                0)
    }
    return 0
}

fun bishopPairEval(bitboards: BitboardData, materialValues: MaterialValues) =
        (if (whiteBishopColourCount(bitboards) == 2) VALUE_BISHOP_PAIR +
            (8 - materialValues.whitePawns / VALUE_PAWN) *
            VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS else 0) -
        if (blackBishopColourCount(bitboards) == 2) VALUE_BISHOP_PAIR +
                    (8 - materialValues.blackPawns / VALUE_PAWN) *
                    VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS else 0

fun trappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops or bitboards.blackBishops and A2A7H2H7 != 0L)
            blackA2TrappedBishopEval(bitboards) + blackH2TrappedBishopEval(bitboards) -
                    whiteA7TrappedBishopEval(bitboards) - whiteH7TrappedBishopEval(bitboards)
        else 0

fun blackH2TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.blackBishops and (1L shl Square.H2.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.G3.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.F2.bitRef) != 0L)
            if (bitboards.blackQueens == 0L) VALUE_TRAPPED_BISHOP_PENALTY
            else VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        else 0

fun blackA2TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.blackBishops and (1L shl Square.A2.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.B3.bitRef) != 0L &&
                bitboards.whitePawns and (1L shl Square.C2.bitRef) != 0L)
            VALUE_TRAPPED_BISHOP_PENALTY
        else 0

fun whiteH7TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops and (1L shl Square.H7.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.G6.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.F7.bitRef) != 0L)
            if (bitboards.whiteQueens == 0L) VALUE_TRAPPED_BISHOP_PENALTY
            else VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        else 0

fun whiteA7TrappedBishopEval(bitboards: BitboardData) =
        if (bitboards.whiteBishops and (1L shl Square.A7.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.B6.bitRef) != 0L &&
                bitboards.blackPawns and (1L shl Square.C7.bitRef) != 0L)
            VALUE_TRAPPED_BISHOP_PENALTY
        else 0

fun blackPieceBitboard(bitboards: BitboardData) = (bitboards.blackKnights or bitboards.blackRooks or bitboards.blackQueens or bitboards.blackBishops)

fun whitePieceBitboard(bitboards: BitboardData) = (bitboards.whiteKnights or bitboards.whiteRooks or bitboards.whiteQueens or bitboards.whiteBishops)

fun isEndGame(bitboards: BitboardData) =
        (whitePieceValues(bitboards) +
                whitePawnValues(bitboards) +
                blackPieceValues(bitboards) +
                blackPawnValues(bitboards)) <= EVAL_ENDGAME_TOTAL_PIECES

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

    if (averagePiecesPerSide <= KINGSAFETY_MIN_PIECE_BALANCE) return 0

    val whiteKingSafety: Int = getWhiteKingRightWayScore(board) + KINGSAFETY_SHIELD_BASE + whiteKingShieldEval(bitboards, kingSquares)

    val blackKingSafety: Int = getBlackKingRightWayScore(board) + KINGSAFETY_SHIELD_BASE + blackKingShieldEval(bitboards, kingSquares)

    return linearScale(
            averagePiecesPerSide,
            KINGSAFETY_MIN_PIECE_BALANCE,
            KINGSAFETY_MAX_PIECE_BALANCE,
            0,
            whiteKingSafety - blackKingSafety + (blackKingAttackedCount - whiteKingAttackedCount) * KINGSAFETY_ATTACK_MULTIPLIER)
}

private fun blackKingDangerZone(kingSquares: KingSquares) = kingMoves[kingSquares.black] or (kingMoves[kingSquares.black] ushr 8)

private fun whiteKingDangerZone(kingSquares: KingSquares) = kingMoves[kingSquares.white] or (kingMoves[kingSquares.white] shl 8)

fun uncastledTrappedWhiteRookEval(bitboards: BitboardData) =
        if (bitboards.whiteKing and F1G1 != 0L &&
                bitboards.whiteRooks and G1H1 != 0L &&
                bitboards.whitePawns and FILE_G != 0L &&
                bitboards.whitePawns and FILE_H != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else (if (bitboards.whiteKing and B1C1 != 0L &&
                bitboards.whiteRooks and A1B1 != 0L &&
                bitboards.whitePawns and FILE_A != 0L &&
                bitboards.whitePawns and FILE_B != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else 0)

fun pawnShieldEval(friendlyPawns: Long, enemyPawns: Long, friendlyPawnShield: Long, shifter: Long.(Int) -> Long) =
        (KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * bitCount(friendlyPawns and friendlyPawnShield)
                - KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * bitCount(enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield,8)))
                + KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * bitCount(friendlyPawns and shifter(friendlyPawnShield,8))
                - KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * bitCount(enemyPawns and shifter(friendlyPawnShield,16)))

fun uncastledTrappedBlackRookEval(bitboards: BitboardData) =
        if (bitboards.blackKing and F8G8 != 0L &&
                bitboards.blackRooks and G8H8 != 0L &&
                bitboards.blackPawns and FILE_G != 0L &&
                bitboards.blackPawns and FILE_H != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else (if (bitboards.blackKing and B8C8 != 0L &&
                bitboards.blackRooks and A8B8 != 0L &&
                bitboards.blackPawns and FILE_A != 0L &&
                bitboards.blackPawns and FILE_B != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else 0)

fun openFiles(kingShield: Long, pawnBitboard: Long) = southFill(kingShield) and southFill(pawnBitboard).inv() and RANK_1

fun whiteKingShieldEval(bitboards: BitboardData, kingSquares: KingSquares) =
        if (whiteKingOnFirstTwoRanks(kingSquares)) {
            combineWhiteKingShieldEval(bitboards, whiteKingShield(kingSquares))
        } else 0

fun combineWhiteKingShieldEval(bitboards: BitboardData, kingShield: Long) =
        pawnShieldEval(bitboards.whitePawns, bitboards.blackPawns, kingShield, Long::shl)
                .coerceAtMost(KINGSAFTEY_MAXIMUM_SHIELD_BONUS) -
                uncastledTrappedWhiteRookEval(bitboards) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.whitePawns)) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.blackPawns))

fun openFilesKingShieldEval(openFiles: Long) =
        if (openFiles != 0L) {
            KINGSAFTEY_HALFOPEN_MIDFILE * bitCount(openFiles and MIDDLE_FILES_8_BIT) +
                    KINGSAFTEY_HALFOPEN_NONMIDFILE * bitCount(openFiles and NONMID_FILES_8_BIT)
        } else 0

fun blackKingShieldEval(bitboards: BitboardData, kingSquares: KingSquares) =
        if (blackKingOnFirstTwoRanks(kingSquares)) {
            combineBlackKingShieldEval(bitboards, blackKingShield(kingSquares))
        } else 0

fun combineBlackKingShieldEval(bitboards: BitboardData, kingShield: Long) =
        pawnShieldEval(bitboards.blackPawns, bitboards.whitePawns, kingShield, Long::ushr)
                .coerceAtMost(KINGSAFTEY_MAXIMUM_SHIELD_BONUS) -
                uncastledTrappedBlackRookEval(bitboards) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.whitePawns)) -
                openFilesKingShieldEval(openFiles(kingShield, bitboards.blackPawns))

fun whiteKingOnFirstTwoRanks(kingSquares: KingSquares) = yCoordOfSquare(kingSquares.white) < 2

fun blackKingOnFirstTwoRanks(kingSquares: KingSquares) = yCoordOfSquare(kingSquares.black) >= 6

fun blackKingShield(kingSquares: KingSquares) = whiteKingShieldMask[kingSquares.black % 8] shl 40

fun whiteKingShield(kingSquares: KingSquares): Long = whiteKingShieldMask[kingSquares.white % 8]

fun whiteCastlingEval(bitboards: BitboardData, castlePrivileges: Int) : Int {

    val whiteCastleValue = maxCastleValue(blackPieceValues(bitboards))

    return if (whiteCastleValue > 0) {
        whiteCastleValue / whiteTimeToCastleKingSide(castlePrivileges, bitboards)
                .coerceAtMost(whiteTimeToCastleQueenSide(castlePrivileges, bitboards))
    } else 0
}

fun whiteTimeToCastleQueenSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CASTLEPRIV_WQ != 0) {
            2 +
                    (if (bitboards.all and (1L shl 6) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 5) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 4) != 0L) 1 else 0)
        } else 100


fun whiteTimeToCastleKingSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CASTLEPRIV_WK != 0) {
            2 +
                    (if (bitboards.all and (1L shl 1) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 2) != 0L) 1 else 0)
        } else 100

fun blackTimeToCastleQueenSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CASTLEPRIV_BQ != 0) {
            2 +
                    (if (bitboards.all and (1L shl 60) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 61) != 0L) 1 else 0) +
                    (if (bitboards.all and (1L shl 62) != 0L) 1 else 0)
        } else 100


fun blackTimeToCastleKingSide(castlePrivileges: Int, bitboards: BitboardData) =
        if (castlePrivileges and CASTLEPRIV_BK != 0) {
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
                CASTLE_BONUS_LOW_MATERIAL,
                CASTLE_BONUS_HIGH_MATERIAL,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)

fun isAnyCastleAvailable(castlePrivileges: Int) = castlePrivileges != 0

fun endGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(bitboards)) currentScore / ENDGAME_DRAW_DIVISOR
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(bitboards, currentScore, kingSquares)
            else -> blackWinningEndGameAdjustment(bitboards, currentScore, kingSquares)
        }

fun blackWinningEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (blackHasInsufficientMaterial(bitboards)) currentScore + (blackPieceValues(bitboards) * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probableDrawWhenBlackIsWinning(bitboards)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noBlackRooksQueensOrBishops(bitboards) && (blackBishopDrawOnFileA(bitboards) || blackBishopDrawOnFileH(bitboards))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (whitePawnValues(bitboards) == 0) blackWinningNoWhitePawnsEndGameAdjustment(bitboards, currentScore, kingSquares)
        else currentScore

fun blackWinningNoWhitePawnsEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (blackMoreThanABishopUpInNonPawns(bitboards)) {
            if (blackHasOnlyTwoKnights(bitboards) && whitePieceValues(bitboards) == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (blackHasOnlyAKnightAndBishop(bitboards) && whitePieceValues(bitboards) == 0) {
                blackKnightAndBishopVKingEval(currentScore, bitboards, kingSquares)
            } else currentScore - VALUE_SHOULD_WIN
        } else currentScore

fun blackKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData, kingSquares: KingSquares): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (blackDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(kingSquares.white)
    else enemyKingCloseToLightCornerMateSquareValue(kingSquares.white)
}

fun whiteWinningEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (whiteHasInsufficientMaterial(bitboards)) currentScore - (whitePieceValues(bitboards) * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probablyDrawWhenWhiteIsWinning(bitboards)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noWhiteRooksQueensOrKnights(bitboards) && (whiteBishopDrawOnFileA(bitboards) || whiteBishopDrawOnFileH(bitboards))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (blackPawnValues(bitboards) == 0) whiteWinningNoBlackPawnsEndGameAdjustment(bitboards, currentScore, kingSquares)
        else currentScore

fun whiteWinningNoBlackPawnsEndGameAdjustment(bitboards: BitboardData, currentScore: Int, kingSquares: KingSquares) =
        if (whiteMoreThanABishopUpInNonPawns(bitboards)) {
            if (whiteHasOnlyTwoKnights(bitboards) && blackPieceValues(bitboards) == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (whiteHasOnlyAKnightAndBishop(bitboards) && blackPieceValues(bitboards) == 0) whiteKnightAndBishopVKingEval(currentScore, bitboards, kingSquares)
            else currentScore + VALUE_SHOULD_WIN
        } else currentScore

fun whiteKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData, kingSquares: KingSquares): Int {
    whiteShouldWinWithKnightAndBishopValue(currentScore)
    return +if (whiteDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(kingSquares.black)
    else enemyKingCloseToLightCornerMateSquareValue(kingSquares.black)
}

fun enemyKingCloseToDarkCornerMateSquareValue(kingSquare: Int) =
        enemyKingCloseToLightCornerMateSquareValue(bitFlippedHorizontalAxis[kingSquare])

fun enemyKingCloseToLightCornerMateSquareValue(kingSquare: Int) =
        (7 - distanceToH1OrA8[kingSquare]) * ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE

fun blackShouldWinWithKnightAndBishopValue(eval: Int) =
        -(VALUE_KNIGHT + VALUE_BISHOP + VALUE_SHOULD_WIN) +
                eval / ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR

fun whiteShouldWinWithKnightAndBishopValue(eval: Int) =
        VALUE_KNIGHT + VALUE_BISHOP + VALUE_SHOULD_WIN + eval / ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR

fun whiteHasOnlyAKnightAndBishop(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) == 1 && (whitePieceValues(bitboards) == VALUE_KNIGHT + VALUE_BISHOP)

fun blackHasOnlyAKnightAndBishop(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) == 1 && (blackPieceValues(bitboards) == VALUE_KNIGHT + VALUE_BISHOP)

fun whiteHasOnlyTwoKnights(bitboards: BitboardData) =
        bitCount(bitboards.whiteKnights) == 2 && (whitePieceValues(bitboards) == 2 * VALUE_KNIGHT)

fun blackHasOnlyTwoKnights(bitboards: BitboardData) =
        bitCount(bitboards.blackKnights) == 2 && (blackPieceValues(bitboards) == 2 * VALUE_KNIGHT)

fun blackMoreThanABishopUpInNonPawns(bitboards: BitboardData) =
        blackPieceValues(bitboards) - whitePieceValues(bitboards) > VALUE_BISHOP

fun whiteMoreThanABishopUpInNonPawns(bitboards: BitboardData) =
        whitePieceValues(bitboards) - blackPieceValues(bitboards) > VALUE_BISHOP

fun noBlackRooksQueensOrBishops(bitboards: BitboardData) =
        bitboards.blackRooks or bitboards.blackKnights or bitboards.blackQueens == 0L

fun bothSidesHaveOnlyOneKnightOrBishopEach(bitboards: BitboardData) =
        noPawnsRemain(bitboards) && whitePieceValues(bitboards) < VALUE_ROOK &&
                blackPieceValues(bitboards) < VALUE_ROOK

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
        blackPawnValues(bitboards) == 0 && blackPieceValues(bitboards) - VALUE_BISHOP <= whitePieceValues(bitboards)

fun probablyDrawWhenWhiteIsWinning(bitboards: BitboardData) =
        whitePawnValues(bitboards) == 0 && whitePieceValues(bitboards) - VALUE_BISHOP <= blackPieceValues(bitboards)

fun blackHasInsufficientMaterial(bitboards: BitboardData) =
        blackPawnValues(bitboards) == 0 && (blackPieceValues(bitboards) == VALUE_KNIGHT ||
                blackPieceValues(bitboards) == VALUE_BISHOP)

fun whiteHasInsufficientMaterial(bitboards: BitboardData) =
        whitePawnValues(bitboards) == 0 && (whitePieceValues(bitboards) == VALUE_KNIGHT ||
                whitePieceValues(bitboards) == VALUE_BISHOP)


fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        bitCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY

fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        knightMoves[square] and (enemyPawnAttacks or friendlyPawns)

fun whitePawnsEval(bitboards: BitboardData): Int {
    var acc = 0
    applyToSquares(bitboards.whitePawns) {
        acc += linearScale(
                blackPieceValues(bitboards),
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                PieceSquareTables.pawnEndGame[it],
                PieceSquareTables.pawn[it]
        )
    }
    return acc
}

fun blackPawnsEval(bitboards: BitboardData): Int {
    var acc = 0
    applyToSquares(bitboards.blackPawns) {
        acc += linearScale(
                whitePieceValues(bitboards),
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                PieceSquareTables.pawnEndGame[bitFlippedHorizontalAxis[it]],
                PieceSquareTables.pawn[bitFlippedHorizontalAxis[it]]
        )
    }
    return acc
}

fun blackKnightsEval(
        bitboards: BitboardData,
        attacks: Attacks,
        materialValues: MaterialValues) : Int {

    var acc = 0
    applyToSquares(bitboards.blackKnights) {
        acc += linearScale(
                materialValues.whitePieces + materialValues.whitePawns,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                PieceSquareTables.knightEndGame[bitFlippedHorizontalAxis[it]],
                PieceSquareTables.knight[bitFlippedHorizontalAxis[it]]
        ) -
                blockedKnightPenaltyEval(it, attacks.whitePawns, bitboards.blackPawns)
    }
    return acc
}

fun whiteKnightsEval(
        bitboards: BitboardData,
        attacks: Attacks,
        materialValues: MaterialValues)
        : Int {

    var acc = 0
    applyToSquares(bitboards.whiteKnights) {
        acc += linearScale(materialValues.blackPieces + materialValues.blackPawns,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                PieceSquareTables.knightEndGame[it],
                PieceSquareTables.knight[it]
        ) - blockedKnightPenaltyEval(it, attacks.blackPawns, bitboards.whitePawns)
    }
    return acc
}

fun blackBishopsEval(bitboards: BitboardData, blackPieces: Long): Int {
    var acc = 0
    applyToSquares(bitboards.blackBishops) {
        acc += VALUE_BISHOP_MOBILITY[bitCount(bishopAttacks(bitboards, it) and blackPieces.inv())] +
                flippedSquareTableScore(PieceSquareTables.bishop, it)
    }
    return acc
}

fun whiteBishopEval(bitboards: BitboardData, whitePieces: Long): Int {
    var acc = 0
    applyToSquares(bitboards.whiteBishops) {
        acc += VALUE_BISHOP_MOBILITY[bitCount(bishopAttacks(bitboards, it) and whitePieces.inv())] +
                PieceSquareTables.bishop[it]
    }
    return acc
}

private fun blackQueensEval(bitboards: BitboardData, blackPieces: Long): Int {
    var acc = 0
    applyToSquares(bitboards.blackQueens) {
        acc += VALUE_QUEEN_MOBILITY[bitCount(queenAttacks(bitboards, it) and blackPieces.inv())] +
                flippedSquareTableScore(PieceSquareTables.queen, it)
    }
    return acc
}

private fun whiteQueensEval(bitboards: BitboardData, whitePieces: Long): Int {
    var acc = 0
    applyToSquares(bitboards.whiteQueens) {
        acc += VALUE_QUEEN_MOBILITY[bitCount(queenAttacks(bitboards, it) and whitePieces.inv())] +
                PieceSquareTables.queen[it]
    }
    return acc
}

private fun blackRooksEval(bitboards: BitboardData, blackPieces: Long): Int {
    var acc = 0
    applyToSquares(bitboards.blackRooks) {
        acc += blackRookOpenFilesEval(bitboards, it % 8) +
                VALUE_ROOK_MOBILITY[bitCount(rookAttacks(bitboards, it) and blackPieces.inv())] +
                flippedSquareTableScore(PieceSquareTables.rook, it) * rookEnemyPawnMultiplier(whitePawnValues(bitboards)) / 6
    }
    return acc
}

fun whiteRooksEval(bitboards: BitboardData, whitePieces: Long): Int {
    var acc = 0
    applyToSquares(bitboards.whiteRooks) {
        acc += whiteRookOpenFilesEval(bitboards, it % 8) +
                VALUE_ROOK_MOBILITY[bitCount(rookAttacks(bitboards, it) and whitePieces.inv())] +
                PieceSquareTables.rook[it] * rookEnemyPawnMultiplier(blackPawnValues(bitboards)) / 6
    }
    return acc
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
    val whiteOccupiedFileMask = southFill(whitePawnBitboard) and RANK_1
    val blackOccupiedFileMask = southFill(blackPawnBitboard) and RANK_1

    fun whitePassedPawnScore(): Int {
        var acc = 0
        applyToSquares(whitePassedPawnsBitboard) {acc += VALUE_PASSED_PAWN_BONUS[yCoordOfSquare(it)] }
        return bitCount(whiteGuardedPassedPawns) * VALUE_GUARDED_PASSED_PAWN + acc
    }

    fun blackPassedPawnScore(): Int {
        var acc = 0
        applyToSquares(blackPassedPawnsBitboard) { acc += VALUE_PASSED_PAWN_BONUS[7 - yCoordOfSquare(it)] }
        return bitCount(blackGuardedPassedPawns) * VALUE_GUARDED_PASSED_PAWN + acc
    }

    return bitCount(blackIsolatedPawns) * VALUE_ISOLATED_PAWN_PENALTY -
            bitCount(whiteIsolatedPawns) * VALUE_ISOLATED_PAWN_PENALTY -
            (if (whiteIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) +
            (if (blackIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) -
            (bitCount(whitePawnBitboard and
                            (whitePawnBitboard or blackPawnBitboard ushr 8).inv() and
                            (blackPawnAttacks ushr 8) and
                            northFill(whitePawnAttacks).inv() and
                    blackPawnAttacks(whitePawnBitboard) and
                            northFill(blackPawnFiles).inv()
            ) * VALUE_BACKWARD_PAWN_PENALTY) +
            (bitCount(blackPawnBitboard and
                            (blackPawnBitboard or whitePawnBitboard shl 8).inv() and
                            (whitePawnAttacks shl 8) and
                            southFill(blackPawnAttacks).inv() and
                    whitePawnAttacks(blackPawnBitboard) and
                            northFill(whitePawnFiles).inv()
            ) * VALUE_BACKWARD_PAWN_PENALTY) -
            ((bitCount(whitePawnBitboard and FILE_A) + bitCount(whitePawnBitboard and FILE_H))
                    * VALUE_SIDE_PAWN_PENALTY) +
            ((bitCount(blackPawnBitboard and FILE_A) + bitCount(blackPawnBitboard and FILE_H))
                    * VALUE_SIDE_PAWN_PENALTY) -
            VALUE_DOUBLED_PAWN_PENALTY * (materialValues.whitePawns / 100 - bitCount(whiteOccupiedFileMask)) -
            bitCount(whiteOccupiedFileMask.inv() ushr 1 and whiteOccupiedFileMask) * VALUE_PAWN_ISLAND_PENALTY +
            VALUE_DOUBLED_PAWN_PENALTY * (materialValues.blackPawns / 100 - bitCount(blackOccupiedFileMask)) +
            bitCount(blackOccupiedFileMask.inv() ushr 1 and blackOccupiedFileMask) * VALUE_PAWN_ISLAND_PENALTY +
            (linearScale(materialValues.blackPieces, 0, PAWN_ADJUST_MAX_MATERIAL, whitePassedPawnScore() * 2, whitePassedPawnScore())) -
            (linearScale(materialValues.whitePieces, 0, PAWN_ADJUST_MAX_MATERIAL, blackPassedPawnScore() * 2, blackPassedPawnScore())) +
            (if (materialValues.blackPieces < PAWN_ADJUST_MAX_MATERIAL)
                calculateLowMaterialPawnBonus(
                        Colour.BLACK,
                        whiteKingSquare,
                        blackKingSquare,
                        materialValues,
                        whitePassedPawnsBitboard,
                        blackPassedPawnsBitboard,
                        mover)
            else 0) +
            (if (materialValues.whitePieces < PAWN_ADJUST_MAX_MATERIAL)
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
            .map {
                val pawnDistance = pawnDistanceFromPromotion(lowMaterialColour, it).coerceAtMost(5)
                val kingXDistanceFromPawn = difference(kingX, it)
                val kingYDistanceFromPawn = difference(colourAdjustedYRank(lowMaterialColour, kingY), it)
                val kingDistanceFromPawn = kingXDistanceFromPawn.coerceAtLeast(kingYDistanceFromPawn)

                val moverAdjustment = if (lowMaterialColour == mover) 1 else 0

                val scoreAdjustment = linearScale(
                        lowMaterialSidePieceValues,
                        0,
                        PAWN_ADJUST_MAX_MATERIAL,
                        kingDistanceFromPawn * 4,
                        0) +
                        if (pawnDistance < kingDistanceFromPawn - moverAdjustment && lowMaterialSidePieceValues == 0) {
                            VALUE_KING_CANNOT_CATCH_PAWN
                        } else 0

                if (lowMaterialColour == Colour.WHITE) -scoreAdjustment else scoreAdjustment
            }.fold(0) { acc, i -> acc + i }

}

private fun colourAdjustedYRank(colour: Colour, yRank: Int) = if (colour == Colour.WHITE) yRank else abs(yRank - 7)

private fun difference(kingX: Int, it: Int) = abs(kingX - xCoordOfSquare(it))

private fun pawnDistanceFromPromotion(colour: Colour, square: Int) = if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

private fun xCoordOfSquare(it: Int) = it % 8

private fun yCoordOfSquare(kingSquare: Int) = kingSquare / 8

