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
import kotlin.math.abs

fun evaluate(board: EngineBoard): Int {

    val bitboards = BitboardData(board)

    if (onlyKingsRemain(bitboards)) return 0

    val attacks = Attacks(bitboards)
    val materialValues = MaterialValues(board)
    val kingSquares = KingSquares(bitboards)

    val whitePieces = if (board.mover == Colour.WHITE) bitboards.friendly else bitboards.enemy
    val blackPieces = if (board.mover == Colour.WHITE) bitboards.enemy else bitboards.friendly

    val materialDifference = materialDifferenceEval(materialValues)

    val eval =  materialDifference +
            (twoWhiteRooksTrappingKingEval(bitboards) - twoBlackRooksTrappingKingEval(bitboards)) +
            (doubledRooksEval(bitboards.whiteRooks) - doubledRooksEval(bitboards.blackRooks)) +
            (whiteRooksEval(bitboards, materialValues, whitePieces.inv()) - blackRooksEval(bitboards, materialValues, blackPieces.inv())) +
            pawnScore(bitboards.whitePawns, bitboards.blackPawns, attacks, materialValues, board.whiteKingSquare, board.blackKingSquare, board.mover) +
            tradePawnBonusWhenMoreMaterial(materialValues, materialDifference) +
            (whitePawnsEval(bitboards, materialValues) - blackPawnsEval(bitboards, materialValues)) +
            (whiteBishopEval(bitboards, whitePieces.inv()) - blackBishopsEval(bitboards, blackPieces.inv())) +
            (whiteKnightsEval(bitboards, attacks, materialValues) - blackKnightsEval(bitboards, attacks, materialValues)) +
            (whiteKingSquareEval(materialValues, kingSquares) - blackKingSquareEval(materialValues, kingSquares)) +
            tradePieceBonusWhenMoreMaterial(materialValues, materialDifference) +
            castlingEval(bitboards, materialValues, board.castlePrivileges) +
            threatEval(bitboards, attacks, board) +
            kingSafetyEval(bitboards, materialValues, attacks, board, kingSquares) +
            (whiteQueensEval(bitboards, whitePieces.inv()) - blackQueensEval(bitboards, blackPieces.inv())) +
            bishopScore(bitboards, materialDifference, materialValues)

    val endGameAdjustedScore = if (isEndGame(materialValues)) endGameAdjustment(bitboards, materialValues, eval, kingSquares) else eval

    return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore

}

fun materialDifferenceEval(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces +
                materialValues.whitePawns - materialValues.blackPawns

fun exactlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L && bitboard != 0L

fun onlyKingsRemain(bitboards: BitboardData) = exactlyOneBitSet(bitboards.enemy) and exactlyOneBitSet(bitboards.friendly)

fun whiteKingSquareEval(materialValues: MaterialValues, kingSquares: KingSquares) =
        linearScale(
                materialValues.blackPieces,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                PieceSquareTables.kingEndGame[kingSquares.white],
                PieceSquareTables.king[kingSquares.white]
        )

fun blackKingSquareEval(materialValues: MaterialValues, kingSquares: KingSquares) =
        linearScale(
                materialValues.whitePieces,
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
        if (popCount(bitboards.whiteRooks and RANK_7) > 1 && bitboards.blackKing and RANK_8 != 0L)
            VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING else 0

fun twoBlackRooksTrappingKingEval(bitboards: BitboardData) =
        if (popCount(bitboards.blackRooks and RANK_2) > 1 && bitboards.whiteKing and RANK_1 != 0L)
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

fun doubledRooksEval(bitboard: Long): Int {
    var files = booleanArrayOf(false,false,false,false,false,false,false,false)
    applyToSquares(bitboard) {
        if (files[it % 8]) return VALUE_ROOKS_ON_SAME_FILE
        files[it % 8] = true
    }
    return 0
}

fun flippedSquareTableScore(table: IntArray, bit: Int) = table[bitFlippedHorizontalAxis[bit]]

fun kingAttackCount(dangerZone: Long, attacks: LongArray): Int {
    var acc = 0
    for (it in attacks) {
        if (it == -1L) return acc
        acc += popCount(it and dangerZone)
    }
    return acc
}

fun tradePieceBonusWhenMoreMaterial(materialValues: MaterialValues, materialDifference: Int) =
        linearScale(
                if (materialDifference > 0)
                    materialValues.blackPieces + materialValues.blackPawns else
                    materialValues.whitePieces + materialValues.whitePawns,
                0,
                TOTAL_PIECE_VALUE_PER_SIDE_AT_START,
                30 * materialDifference / 100,
                0)

fun tradePawnBonusWhenMoreMaterial(materialValues: MaterialValues, materialDifference: Int) =
        linearScale(
                if (materialDifference > 0) materialValues.whitePawns else materialValues.blackPawns,
                0,
                PAWN_TRADE_BONUS_MAX,
                -30 * materialDifference / 100,
                0)

fun bishopScore(bitboards: BitboardData, materialDifference: Int, materialValues: MaterialValues) =
        bishopPairEval(bitboards, materialValues) +
                oppositeColourBishopsEval(bitboards, materialValues, materialDifference) + trappedBishopEval(bitboards)

fun whiteLightBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and LIGHT_SQUARES != 0L
fun whiteDarkBishopExists(bitboards: BitboardData) = bitboards.whiteBishops and DARK_SQUARES != 0L
fun blackLightBishopExists(bitboards: BitboardData) = bitboards.blackBishops and LIGHT_SQUARES != 0L
fun blackDarkBishopExists(bitboards: BitboardData) = bitboards.blackBishops and DARK_SQUARES != 0L
fun whiteBishopColourCount(bitboards: BitboardData) = (if (whiteLightBishopExists(bitboards)) 1 else 0) + if (whiteDarkBishopExists(bitboards)) 1 else 0
fun blackBishopColourCount(bitboards: BitboardData) = (if (blackLightBishopExists(bitboards)) 1 else 0) + if (blackDarkBishopExists(bitboards)) 1 else 0

fun oppositeColourBishopsEval(bitboards: BitboardData, materialValues: MaterialValues, materialDifference: Int): Int {

    if (whiteBishopColourCount(bitboards) == 1 && blackBishopColourCount(bitboards) == 1 &&
            whiteLightBishopExists(bitboards) != blackLightBishopExists(bitboards) &&
            materialValues.whitePieces == materialValues.blackPieces) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / WRONG_COLOUR_BISHOP_PENALTY_DIVISOR // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScale(
                materialValues.whitePieces + materialValues.blackPieces,
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

fun isEndGame(materialValues: MaterialValues) =
        (materialValues.whitePieces +
                materialValues.whitePawns +
                materialValues.blackPieces +
                materialValues.blackPawns) <= EVAL_ENDGAME_TOTAL_PIECES

fun kingSafetyEval(bitboards: BitboardData, materialValues: MaterialValues, attacks: Attacks, board: EngineBoard, kingSquares: KingSquares): Int {

    val whiteKingDangerZone = whiteKingDangerZone(kingSquares)

    val blackKingDangerZone = blackKingDangerZone(kingSquares)

    val blackKingAttackedCount = kingAttackCount(blackKingDangerZone, attacks.whiteRookPair.first) +
            kingAttackCount(blackKingDangerZone, attacks.whiteQueenPair.first) * 2 +
            kingAttackCount(blackKingDangerZone, attacks.whiteBishopPair.first)

    val whiteKingAttackedCount = kingAttackCount(whiteKingDangerZone, attacks.blackRookPair.first) +
            kingAttackCount(whiteKingDangerZone, attacks.blackQueenPair.first) * 2 +
            kingAttackCount(whiteKingDangerZone, attacks.blackBishopPair.first)

    val averagePiecesPerSide = (materialValues.whitePieces + materialValues.blackPieces) / 2

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
        (KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * popCount(friendlyPawns and friendlyPawnShield)
                - KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * popCount(enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield,8)))
                + KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * popCount(friendlyPawns and shifter(friendlyPawnShield,8))
                - KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * popCount(enemyPawns and shifter(friendlyPawnShield,16)))

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
            KINGSAFTEY_HALFOPEN_MIDFILE * popCount(openFiles and MIDDLE_FILES_8_BIT) +
                    KINGSAFTEY_HALFOPEN_NONMIDFILE * popCount(openFiles and NONMID_FILES_8_BIT)
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

fun whiteCastlingEval(bitboards: BitboardData, materialValues: MaterialValues, castlePrivileges: Int) : Int {

    val whiteCastleValue = maxCastleValue(materialValues.blackPieces)

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

fun blackCastlingEval(bitboards: BitboardData, materialValues: MaterialValues, castlePrivileges: Int) : Int {
    // Value of moving King to its queenside castle destination in the middle game
    val blackCastleValue = maxCastleValue(materialValues.whitePieces)

    return if (blackCastleValue > 0) {
        blackCastleValue / blackTimeToCastleKingSide(castlePrivileges, bitboards)
                .coerceAtMost(blackTimeToCastleQueenSide(castlePrivileges, bitboards))
    } else 0
}

fun rookSquareBonus() = PieceSquareTables.rook[3] - PieceSquareTables.rook[0]

fun kingSquareBonusEndGame() = PieceSquareTables.kingEndGame[1] - PieceSquareTables.kingEndGame[3]

fun kingSquareBonusMiddleGame() = PieceSquareTables.king[1] - PieceSquareTables.king[3]

fun castlingEval(bitboards: BitboardData, materialValues: MaterialValues, castlePrivileges: Int) =
        if (isAnyCastleAvailable(castlePrivileges)) {
            whiteCastlingEval(bitboards, materialValues, castlePrivileges) - blackCastlingEval(bitboards, materialValues, castlePrivileges)
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

fun endGameAdjustment(bitboards: BitboardData, materialValues: MaterialValues, currentScore: Int, kingSquares: KingSquares) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(materialValues)) currentScore / ENDGAME_DRAW_DIVISOR
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(bitboards, materialValues, currentScore, kingSquares)
            else -> blackWinningEndGameAdjustment(bitboards, materialValues, currentScore, kingSquares)
        }

fun blackWinningEndGameAdjustment(bitboards: BitboardData, materialValues: MaterialValues, currentScore: Int, kingSquares: KingSquares) =
        if (blackHasInsufficientMaterial(materialValues)) currentScore + (materialValues.blackPieces * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probableDrawWhenBlackIsWinning(materialValues)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noBlackRooksQueensOrBishops(bitboards) && (blackBishopDrawOnFileA(bitboards) || blackBishopDrawOnFileH(bitboards))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (materialValues.whitePawns == 0) blackWinningNoWhitePawnsEndGameAdjustment(bitboards, materialValues, currentScore, kingSquares)
        else currentScore

fun blackWinningNoWhitePawnsEndGameAdjustment(bitboards: BitboardData, materialValues: MaterialValues, currentScore: Int, kingSquares: KingSquares) =
        if (blackMoreThanABishopUpInNonPawns(materialValues)) {
            if (blackHasOnlyTwoKnights(bitboards, materialValues) && materialValues.whitePieces == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (blackHasOnlyAKnightAndBishop(bitboards, materialValues) && materialValues.whitePieces == 0) {
                blackKnightAndBishopVKingEval(currentScore, bitboards, kingSquares)
            } else currentScore - VALUE_SHOULD_WIN
        } else currentScore

fun blackKnightAndBishopVKingEval(currentScore: Int, bitboards: BitboardData, kingSquares: KingSquares): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (blackDarkBishopExists(bitboards)) enemyKingCloseToDarkCornerMateSquareValue(kingSquares.white)
    else enemyKingCloseToLightCornerMateSquareValue(kingSquares.white)
}

fun whiteWinningEndGameAdjustment(bitboards: BitboardData, materialValues: MaterialValues, currentScore: Int, kingSquares: KingSquares) =
        if (whiteHasInsufficientMaterial(materialValues)) currentScore - (materialValues.whitePieces * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probablyDrawWhenWhiteIsWinning(materialValues)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noWhiteRooksQueensOrKnights(bitboards) && (whiteBishopDrawOnFileA(bitboards) || whiteBishopDrawOnFileH(bitboards))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (materialValues.blackPawns == 0) whiteWinningNoBlackPawnsEndGameAdjustment(bitboards, materialValues, currentScore, kingSquares)
        else currentScore

fun whiteWinningNoBlackPawnsEndGameAdjustment(bitboards: BitboardData, materialValues: MaterialValues, currentScore: Int, kingSquares: KingSquares) =
        if (whiteMoreThanABishopUpInNonPawns(materialValues)) {
            if (whiteHasOnlyTwoKnights(bitboards, materialValues) && materialValues.blackPieces == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (whiteHasOnlyAKnightAndBishop(bitboards, materialValues) && materialValues.blackPieces == 0) whiteKnightAndBishopVKingEval(currentScore, bitboards, kingSquares)
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

fun whiteHasOnlyAKnightAndBishop(bitboards: BitboardData, materialValues: MaterialValues) =
        popCount(bitboards.whiteKnights) == 1 && (materialValues.whitePieces == VALUE_KNIGHT + VALUE_BISHOP)

fun blackHasOnlyAKnightAndBishop(bitboards: BitboardData, materialValues: MaterialValues) =
        popCount(bitboards.blackKnights) == 1 && (materialValues.blackPieces == VALUE_KNIGHT + VALUE_BISHOP)

fun whiteHasOnlyTwoKnights(bitboards: BitboardData, materialValues: MaterialValues) =
        popCount(bitboards.whiteKnights) == 2 && (materialValues.whitePieces == 2 * VALUE_KNIGHT)

fun blackHasOnlyTwoKnights(bitboards: BitboardData, materialValues: MaterialValues) =
        popCount(bitboards.blackKnights) == 2 && (materialValues.blackPieces == 2 * VALUE_KNIGHT)

fun blackMoreThanABishopUpInNonPawns(materialValues: MaterialValues) =
        materialValues.blackPieces - materialValues.whitePieces > VALUE_BISHOP

fun whiteMoreThanABishopUpInNonPawns(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces > VALUE_BISHOP

fun noBlackRooksQueensOrBishops(bitboards: BitboardData) =
        bitboards.blackRooks or bitboards.blackKnights or bitboards.blackQueens == 0L

fun bothSidesHaveOnlyOneKnightOrBishopEach(materialValues: MaterialValues) =
        noPawnsRemain(materialValues) && materialValues.whitePieces < VALUE_ROOK &&
                materialValues.blackPieces < VALUE_ROOK

fun noPawnsRemain(materialValues: MaterialValues) = materialValues.whitePawns + materialValues.blackPawns == 0

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

fun probableDrawWhenBlackIsWinning(materialValues: MaterialValues) =
        materialValues.blackPawns == 0 && materialValues.blackPieces - VALUE_BISHOP <= materialValues.whitePieces

fun probablyDrawWhenWhiteIsWinning(materialValues: MaterialValues) =
        materialValues.whitePawns == 0 && materialValues.whitePieces - VALUE_BISHOP <= materialValues.blackPieces

fun blackHasInsufficientMaterial(materialValues: MaterialValues) =
        materialValues.blackPawns == 0 && (materialValues.blackPieces == VALUE_KNIGHT || materialValues.blackPieces == VALUE_BISHOP)

fun whiteHasInsufficientMaterial(materialValues: MaterialValues) =
        materialValues.whitePawns == 0 && (materialValues.whitePieces == VALUE_KNIGHT || materialValues.whitePieces == VALUE_BISHOP)


fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        popCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY

fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        knightMoves[square] and (enemyPawnAttacks or friendlyPawns)

fun whitePawnsEval(bitboards: BitboardData, materialValues: MaterialValues): Int {
    var acc = 0
    val blackPieceValues = materialValues.blackPieces
    applyToSquares(bitboards.whitePawns) {
        acc += linearScale(
                blackPieceValues,
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                PieceSquareTables.pawnEndGame[it],
                PieceSquareTables.pawn[it]
        )
    }
    return acc
}

fun blackPawnsEval(bitboards: BitboardData, materialValues: MaterialValues): Int {
    var acc = 0
    val whitePieceValues = materialValues.whitePieces
    applyToSquares(bitboards.blackPawns) {
        acc += linearScale(
                whitePieceValues,
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
        materialValues: MaterialValues) : Int
{

    var acc = 0
    applyToSquares(bitboards.blackKnights) {
        acc += linearScale(
                materialValues.whitePieces + materialValues.whitePawns,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                PieceSquareTables.knightEndGame[bitFlippedHorizontalAxis[it]],
                PieceSquareTables.knight[bitFlippedHorizontalAxis[it]]
        ) - blockedKnightPenaltyEval(it, attacks.whitePawns, bitboards.blackPawns)
    }
    return acc
}

fun whiteKnightsEval(
        bitboards: BitboardData,
        attacks: Attacks,
        materialValues: MaterialValues) : Int
{

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

fun blackBishopsEval(bitboards: BitboardData, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(bitboards.blackBishops) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(bitboards, it) and blackPiecesInverted)] +
                flippedSquareTableScore(PieceSquareTables.bishop, it)
    }
    return acc
}

fun whiteBishopEval(bitboards: BitboardData, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(bitboards.whiteBishops) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(bitboards, it) and whitePiecesInverted)] + PieceSquareTables.bishop[it]
    }
    return acc
}

private fun blackQueensEval(bitboards: BitboardData, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(bitboards.blackQueens) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(bitboards, it) and blackPiecesInverted)] + flippedSquareTableScore(PieceSquareTables.queen, it)
    }
    return acc
}

private fun whiteQueensEval(bitboards: BitboardData, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(bitboards.whiteQueens) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(bitboards, it) and whitePiecesInverted)] + PieceSquareTables.queen[it]
    }
    return acc
}

private fun blackRooksEval(bitboards: BitboardData, materialValues: MaterialValues, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(bitboards.blackRooks) {
        acc += blackRookOpenFilesEval(bitboards, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(bitboards, it) and blackPiecesInverted)] +
                flippedSquareTableScore(PieceSquareTables.rook, it) * rookEnemyPawnMultiplier(materialValues.whitePawns) / 6
    }
    return acc
}

fun whiteRooksEval(bitboards: BitboardData, materialValues: MaterialValues, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(bitboards.whiteRooks) {
        acc += whiteRookOpenFilesEval(bitboards, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(bitboards, it) and whitePiecesInverted)] +
                PieceSquareTables.rook[it] * rookEnemyPawnMultiplier(materialValues.blackPawns) / 6
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
        return popCount(whiteGuardedPassedPawns) * VALUE_GUARDED_PASSED_PAWN + acc
    }

    fun blackPassedPawnScore(): Int {
        var acc = 0
        applyToSquares(blackPassedPawnsBitboard) { acc += VALUE_PASSED_PAWN_BONUS[7 - yCoordOfSquare(it)] }
        return popCount(blackGuardedPassedPawns) * VALUE_GUARDED_PASSED_PAWN + acc
    }

    return popCount(blackIsolatedPawns) * VALUE_ISOLATED_PAWN_PENALTY -
            popCount(whiteIsolatedPawns) * VALUE_ISOLATED_PAWN_PENALTY -
            (if (whiteIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) +
            (if (blackIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) -
            (popCount(whitePawnBitboard and
                    (whitePawnBitboard or blackPawnBitboard ushr 8).inv() and
                    (blackPawnAttacks ushr 8) and
                    northFill(whitePawnAttacks).inv() and
                    blackPawnAttacks(whitePawnBitboard) and
                    northFill(blackPawnFiles).inv()
            ) * VALUE_BACKWARD_PAWN_PENALTY) +
            (popCount(blackPawnBitboard and
                    (blackPawnBitboard or whitePawnBitboard shl 8).inv() and
                    (whitePawnAttacks shl 8) and
                    southFill(blackPawnAttacks).inv() and
                    whitePawnAttacks(blackPawnBitboard) and
                    northFill(whitePawnFiles).inv()
            ) * VALUE_BACKWARD_PAWN_PENALTY) -
            ((popCount(whitePawnBitboard and FILE_A) + popCount(whitePawnBitboard and FILE_H))
                    * VALUE_SIDE_PAWN_PENALTY) +
            ((popCount(blackPawnBitboard and FILE_A) + popCount(blackPawnBitboard and FILE_H))
                    * VALUE_SIDE_PAWN_PENALTY) -
            VALUE_DOUBLED_PAWN_PENALTY * (materialValues.whitePawns / 100 - popCount(whiteOccupiedFileMask)) -
            popCount(whiteOccupiedFileMask.inv() ushr 1 and whiteOccupiedFileMask) * VALUE_PAWN_ISLAND_PENALTY +
            VALUE_DOUBLED_PAWN_PENALTY * (materialValues.blackPawns / 100 - popCount(blackOccupiedFileMask)) +
            popCount(blackOccupiedFileMask.inv() ushr 1 and blackOccupiedFileMask) * VALUE_PAWN_ISLAND_PENALTY +
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

    var acc = 0
    applyToSquares (if (lowMaterialColour == Colour.WHITE) blackPassedPawnsBitboard else whitePassedPawnsBitboard) {
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

        acc += if (lowMaterialColour == Colour.WHITE) -scoreAdjustment else scoreAdjustment
    }

    return acc

}

private fun colourAdjustedYRank(colour: Colour, yRank: Int) = if (colour == Colour.WHITE) yRank else abs(yRank - 7)

private fun difference(kingX: Int, it: Int) = abs(kingX - xCoordOfSquare(it))

private fun pawnDistanceFromPromotion(colour: Colour, square: Int) = if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

private fun xCoordOfSquare(it: Int) = it % 8

private fun yCoordOfSquare(kingSquare: Int) = kingSquare / 8

