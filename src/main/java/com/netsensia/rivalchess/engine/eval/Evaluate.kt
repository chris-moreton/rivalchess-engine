package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.*
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square
import kotlin.math.abs

fun evaluate(board: EngineBoard): Int {

    if (onlyKingsRemain(board)) return 0

    val attacks = Attacks(board)
    val materialValues = MaterialValues(board)

    val whitePieces = if (board.mover == Colour.WHITE) board.getBitboard(BITBOARD_FRIENDLY) else board.getBitboard(BITBOARD_ENEMY)
    val blackPieces = if (board.mover == Colour.WHITE) board.getBitboard(BITBOARD_ENEMY) else board.getBitboard(BITBOARD_FRIENDLY)

    val materialDifference = materialDifferenceEval(materialValues)

    val eval =  materialDifference +
            (twoWhiteRooksTrappingKingEval(board) - twoBlackRooksTrappingKingEval(board)) +
            (doubledRooksEval(board.getBitboard(BITBOARD_WR)) - doubledRooksEval(board.getBitboard(BITBOARD_BR))) +
            (whiteRooksEval(board, materialValues, whitePieces.inv()) - blackRooksEval(board, materialValues, blackPieces.inv())) +
            pawnScore(board.getBitboard(BITBOARD_WP),
                    board.getBitboard(BITBOARD_BP),
                    attacks, materialValues, board.whiteKingSquare, board.blackKingSquare, board.mover) +
            tradePawnBonusWhenMoreMaterial(materialValues, materialDifference) +
            (whitePawnsEval(board, materialValues) - blackPawnsEval(board, materialValues)) +
            (whiteBishopEval(board, whitePieces.inv()) - blackBishopsEval(board, blackPieces.inv())) +
            (whiteKnightsEval(board, attacks, materialValues) - blackKnightsEval(board, attacks, materialValues)) +
            (whiteKingSquareEval(materialValues, board) - blackKingSquareEval(materialValues, board)) +
            tradePieceBonusWhenMoreMaterial(materialValues, materialDifference) +
            castlingEval(board, materialValues, board.castlePrivileges) +
            threatEval(attacks, board) +
            kingSafetyEval(board, materialValues, attacks) +
            (whiteQueensEval(board, whitePieces.inv()) - blackQueensEval(board, blackPieces.inv())) +
            bishopScore(board, materialDifference, materialValues)

    val endGameAdjustedScore = if (isEndGame(materialValues)) endGameAdjustment(board, materialValues, eval) else eval

    return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore

}

private fun colourAdjustedYRank(colour: Colour, yRank: Int) = if (colour == Colour.WHITE) yRank else abs(yRank - 7)

private fun difference(kingX: Int, it: Int) = abs(kingX - xCoordOfSquare(it))

private fun pawnDistanceFromPromotion(colour: Colour, square: Int) = if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

private fun xCoordOfSquare(square: Int) = square % 8

private fun yCoordOfSquare(square: Int) = square / 8

fun materialDifferenceEval(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces +
                materialValues.whitePawns - materialValues.blackPawns

fun exactlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L && bitboard != 0L

fun onlyKingsRemain(board: EngineBoard) = exactlyOneBitSet(board.getBitboard(BITBOARD_ENEMY)) and exactlyOneBitSet(board.getBitboard(BITBOARD_FRIENDLY))

fun whiteKingSquareEval(materialValues: MaterialValues, board: EngineBoard) =
        linearScale(
                materialValues.blackPieces,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                PieceSquareTables.kingEndGame[board.whiteKingSquare],
                PieceSquareTables.king[board.whiteKingSquare]
        )

fun blackKingSquareEval(materialValues: MaterialValues, board: EngineBoard) =
        linearScale(
                materialValues.whitePieces,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                PieceSquareTables.kingEndGame[bitFlippedHorizontalAxis[board.blackKingSquare]],
                PieceSquareTables.king[bitFlippedHorizontalAxis[board.blackKingSquare]]
        )

fun linearScale(x: Int, min: Int, max: Int, a: Int, b: Int) =
        when {
            x < min -> a
            x > max -> b
            else -> a + (x - min) * (b - a) / (max - min)
        }

fun twoWhiteRooksTrappingKingEval(board: EngineBoard) =
        if (popCount(board.getBitboard(BITBOARD_WR) and RANK_7) > 1 && board.getBitboard(BITBOARD_BK) and RANK_8 != 0L)
            VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING else 0

fun twoBlackRooksTrappingKingEval(board: EngineBoard) =
        if (popCount(board.getBitboard(BITBOARD_BR) and RANK_2) > 1 && board.getBitboard(BITBOARD_WK) and RANK_1 != 0L)
            VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING else 0

fun whiteRookOpenFilesEval(board: EngineBoard, file: Int) =
        if (FILES[file] and board.getBitboard(BITBOARD_WP) == 0L)
            if (FILES[file] and board.getBitboard(BITBOARD_BP) == 0L) VALUE_ROOK_ON_OPEN_FILE
            else VALUE_ROOK_ON_HALF_OPEN_FILE
        else 0

fun blackRookOpenFilesEval(board: EngineBoard, file: Int) =
        if ((FILES[file] and board.getBitboard(BITBOARD_BP)) == 0L)
            if ((FILES[file] and board.getBitboard(BITBOARD_WP)) == 0L) VALUE_ROOK_ON_OPEN_FILE
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

fun bishopScore(board: EngineBoard, materialDifference: Int, materialValues: MaterialValues) =
        bishopPairEval(board, materialValues) +
                oppositeColourBishopsEval(board, materialValues, materialDifference) + trappedBishopEval(board)

fun whiteLightBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_WB) and LIGHT_SQUARES != 0L
fun whiteDarkBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_WB) and DARK_SQUARES != 0L
fun blackLightBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_BB) and LIGHT_SQUARES != 0L
fun blackDarkBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_BB) and DARK_SQUARES != 0L
fun whiteBishopColourCount(board: EngineBoard) = (if (whiteLightBishopExists(board)) 1 else 0) + if (whiteDarkBishopExists(board)) 1 else 0
fun blackBishopColourCount(board: EngineBoard) = (if (blackLightBishopExists(board)) 1 else 0) + if (blackDarkBishopExists(board)) 1 else 0

fun oppositeColourBishopsEval(board: EngineBoard, materialValues: MaterialValues, materialDifference: Int): Int {

    if (whiteBishopColourCount(board) == 1 && blackBishopColourCount(board) == 1 &&
            whiteLightBishopExists(board) != blackLightBishopExists(board) &&
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

fun bishopPairEval(board: EngineBoard, materialValues: MaterialValues) =
        (if (whiteBishopColourCount(board) == 2) VALUE_BISHOP_PAIR +
                (8 - materialValues.whitePawns / VALUE_PAWN) *
                VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS else 0) -
                if (blackBishopColourCount(board) == 2) VALUE_BISHOP_PAIR +
                        (8 - materialValues.blackPawns / VALUE_PAWN) *
                        VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS else 0

fun trappedBishopEval(board: EngineBoard) =
        if ((board.getBitboard(BITBOARD_WB) or board.getBitboard(BITBOARD_BB)) and A2A7H2H7 != 0L)
            blackA2TrappedBishopEval(board) + blackH2TrappedBishopEval(board) -
                    whiteA7TrappedBishopEval(board) - whiteH7TrappedBishopEval(board)
        else 0

fun blackH2TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_BB) and (1L shl Square.H2.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.G3.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.F2.bitRef) != 0L)
            if (board.getBitboard(BITBOARD_BQ) == 0L) VALUE_TRAPPED_BISHOP_PENALTY
            else VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        else 0

fun blackA2TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_BB) and (1L shl Square.A2.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.B3.bitRef) != 0L &&
                board.getBitboard(BITBOARD_WP) and (1L shl Square.C2.bitRef) != 0L)
            VALUE_TRAPPED_BISHOP_PENALTY
        else 0

fun whiteH7TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_WB) and (1L shl Square.H7.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.G6.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.F7.bitRef) != 0L)
            if (board.getBitboard(BITBOARD_WQ) == 0L) VALUE_TRAPPED_BISHOP_PENALTY
            else VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        else 0

fun whiteA7TrappedBishopEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_WB) and (1L shl Square.A7.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.B6.bitRef) != 0L &&
                board.getBitboard(BITBOARD_BP) and (1L shl Square.C7.bitRef) != 0L)
            VALUE_TRAPPED_BISHOP_PENALTY
        else 0

fun blackPieceBitboard(board: EngineBoard) = (board.getBitboard(BITBOARD_BN) or board.getBitboard(BITBOARD_BR) or board.getBitboard(BITBOARD_BQ) or board.getBitboard(BITBOARD_BB))

fun whitePieceBitboard(board: EngineBoard) = (board.getBitboard(BITBOARD_WN) or board.getBitboard(BITBOARD_WR) or board.getBitboard(BITBOARD_WQ) or board.getBitboard(BITBOARD_WB))

fun isEndGame(materialValues: MaterialValues) =
        (materialValues.whitePieces +
                materialValues.whitePawns +
                materialValues.blackPieces +
                materialValues.blackPawns) <= EVAL_ENDGAME_TOTAL_PIECES

fun kingSafetyEval(board: EngineBoard, materialValues: MaterialValues, attacks: Attacks): Int {

    val whiteKingAttackedCount = whiteKingAttackCount(whiteKingDangerZone[board.whiteKingSquare], attacks)
    val blackKingAttackedCount = blackKingAttackCount(blackKingDangerZone[board.blackKingSquare], attacks)

    val averagePiecesPerSide = (materialValues.whitePieces + materialValues.blackPieces) / 2

    if (averagePiecesPerSide <= KINGSAFETY_MIN_PIECE_BALANCE) return 0

    val whiteKingSafety: Int = whiteKingShieldEval(board)
    val blackKingSafety: Int = blackKingShieldEval(board)

    return linearScale(
            averagePiecesPerSide,
            KINGSAFETY_MIN_PIECE_BALANCE,
            KINGSAFETY_MAX_PIECE_BALANCE,
            0,
            whiteKingSafety - blackKingSafety + (blackKingAttackedCount - whiteKingAttackedCount) * KINGSAFETY_ATTACK_MULTIPLIER)
}

private fun whiteKingAttackCount(whiteKingDangerZone: Long, attacks: Attacks): Int {
    return kingAttackCount(whiteKingDangerZone, attacks.blackRooks) +
            kingAttackCount(whiteKingDangerZone, attacks.blackQueens) * 2 +
            kingAttackCount(whiteKingDangerZone, attacks.blackBishops)
}

private fun blackKingAttackCount(blackKingDangerZone: Long, attacks: Attacks): Int {
    return kingAttackCount(blackKingDangerZone, attacks.whiteRooks) +
            kingAttackCount(blackKingDangerZone, attacks.whiteQueens) * 2 +
            kingAttackCount(blackKingDangerZone, attacks.whiteBishops)
}

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

inline fun pawnShieldEval(friendlyPawns: Long, enemyPawns: Long, friendlyPawnShield: Long, shifter: Long.(Int) -> Long) =
        (KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * popCount(friendlyPawns and friendlyPawnShield)
                - KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * popCount(enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield,8)))
                + KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * popCount(friendlyPawns and shifter(friendlyPawnShield,8))
                - KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * popCount(enemyPawns and shifter(friendlyPawnShield,16)))

fun uncastledTrappedBlackRookEval(board: EngineBoard) =
        if (board.getBitboard(BITBOARD_BK) and F8G8 != 0L && board.getBitboard(BITBOARD_BR) and G8H8 != 0L &&
                board.getBitboard(BITBOARD_BP) and FILE_G != 0L && board.getBitboard(BITBOARD_BP) and FILE_H != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else (if (board.getBitboard(BITBOARD_BK) and B8C8 != 0L && board.getBitboard(BITBOARD_BR) and A8B8 != 0L &&
                board.getBitboard(BITBOARD_BP) and FILE_A != 0L && board.getBitboard(BITBOARD_BP) and FILE_B != 0L)
            KINGSAFETY_UNCASTLED_TRAPPED_ROOK
        else 0)

fun openFiles(kingShield: Long, pawnBitboard: Long) = southFill(kingShield) and southFill(pawnBitboard).inv() and RANK_1

fun whiteKingShieldEval(board: EngineBoard) =
        KINGSAFETY_SHIELD_BASE +
        if (whiteKingOnFirstTwoRanks(board)) {
            combineWhiteKingShieldEval(board, whiteKingShield(board))
        } else 0

fun combineWhiteKingShieldEval(board: EngineBoard, kingShield: Long) =
        pawnShieldEval(board.getBitboard(BITBOARD_WP), board.getBitboard(BITBOARD_BP), kingShield, Long::shl)
                .coerceAtMost(KINGSAFTEY_MAXIMUM_SHIELD_BONUS) -
                uncastledTrappedWhiteRookEval(board) -
                openFilesKingShieldEval(openFiles(kingShield, board.getBitboard(BITBOARD_WP))) -
                openFilesKingShieldEval(openFiles(kingShield, board.getBitboard(BITBOARD_BP)))

fun openFilesKingShieldEval(openFiles: Long) =
        if (openFiles != 0L) {
            KINGSAFTEY_HALFOPEN_MIDFILE * popCount(openFiles and MIDDLE_FILES_8_BIT) +
                    KINGSAFTEY_HALFOPEN_NONMIDFILE * popCount(openFiles and NONMID_FILES_8_BIT)
        } else 0

fun blackKingShieldEval(board: EngineBoard) =
        KINGSAFETY_SHIELD_BASE +
        if (blackKingOnFirstTwoRanks(board)) {
            combineBlackKingShieldEval(board, blackKingShield(board))
        } else 0

fun combineBlackKingShieldEval(board: EngineBoard, kingShield: Long) =
        pawnShieldEval(board.getBitboard(BITBOARD_BP), board.getBitboard(BITBOARD_WP), kingShield, Long::ushr)
                .coerceAtMost(KINGSAFTEY_MAXIMUM_SHIELD_BONUS) -
                uncastledTrappedBlackRookEval(board) -
                openFilesKingShieldEval(openFiles(kingShield, board.getBitboard(BITBOARD_WP))) -
                openFilesKingShieldEval(openFiles(kingShield, board.getBitboard(BITBOARD_BP)))

fun whiteKingOnFirstTwoRanks(board: EngineBoard) = yCoordOfSquare(board.whiteKingSquare) < 2

fun blackKingOnFirstTwoRanks(board: EngineBoard) = yCoordOfSquare(board.blackKingSquare) >= 6

fun blackKingShield(board: EngineBoard) = whiteKingShieldMask[board.blackKingSquare % 8] shl 40

fun whiteKingShield(board: EngineBoard): Long = whiteKingShieldMask[board.whiteKingSquare % 8]

fun whiteCastlingEval(board: EngineBoard, materialValues: MaterialValues, castlePrivileges: Int) : Int {

    val whiteCastleValue = maxCastleValue(materialValues.blackPieces)

    return if (whiteCastleValue > 0) {
        whiteCastleValue / whiteTimeToCastleKingSide(castlePrivileges, board)
                .coerceAtMost(whiteTimeToCastleQueenSide(castlePrivileges, board))
    } else 0
}

fun whiteTimeToCastleQueenSide(castlePrivileges: Int, board: EngineBoard) =
        if (castlePrivileges and CASTLEPRIV_WQ != 0) {
            2 +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 6) != 0L) 1 else 0) +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 5) != 0L) 1 else 0) +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 4) != 0L) 1 else 0)
        } else 100


fun whiteTimeToCastleKingSide(castlePrivileges: Int, board: EngineBoard) =
        if (castlePrivileges and CASTLEPRIV_WK != 0) {
            2 +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 1) != 0L) 1 else 0) +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 2) != 0L) 1 else 0)
        } else 100

fun blackTimeToCastleQueenSide(castlePrivileges: Int, board: EngineBoard) =
        if (castlePrivileges and CASTLEPRIV_BQ != 0) {
            2 +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 60) != 0L) 1 else 0) +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 61) != 0L) 1 else 0) +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 62) != 0L) 1 else 0)
        } else 100

fun blackTimeToCastleKingSide(castlePrivileges: Int, board: EngineBoard) =
        if (castlePrivileges and CASTLEPRIV_BK != 0) {
            2 +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 57) != 0L) 1 else 0) +
                    (if (board.getBitboard(BITBOARD_ALL) and (1L shl 58) != 0L) 1 else 0)
        } else 100

fun blackCastlingEval(board: EngineBoard, materialValues: MaterialValues, castlePrivileges: Int) : Int {
    // Value of moving King to its queenside castle destination in the middle game
    val blackCastleValue = maxCastleValue(materialValues.whitePieces)

    return if (blackCastleValue > 0) {
        blackCastleValue / blackTimeToCastleKingSide(castlePrivileges, board)
                .coerceAtMost(blackTimeToCastleQueenSide(castlePrivileges, board))
    } else 0
}

fun rookSquareBonus() = PieceSquareTables.rook[3] - PieceSquareTables.rook[0]

fun kingSquareBonusEndGame() = PieceSquareTables.kingEndGame[1] - PieceSquareTables.kingEndGame[3]

fun kingSquareBonusMiddleGame() = PieceSquareTables.king[1] - PieceSquareTables.king[3]

fun castlingEval(board: EngineBoard, materialValues: MaterialValues, castlePrivileges: Int) =
        if (castlePrivileges != 0) {
            whiteCastlingEval(board, materialValues, castlePrivileges) - blackCastlingEval(board, materialValues, castlePrivileges)
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

fun endGameAdjustment(board: EngineBoard, materialValues: MaterialValues, currentScore: Int) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(materialValues)) currentScore / ENDGAME_DRAW_DIVISOR
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(board, materialValues, currentScore)
            else -> blackWinningEndGameAdjustment(board, materialValues, currentScore)
        }

fun blackWinningEndGameAdjustment(board: EngineBoard, materialValues: MaterialValues, currentScore: Int) =
        if (blackHasInsufficientMaterial(materialValues)) currentScore + (materialValues.blackPieces * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probableDrawWhenBlackIsWinning(materialValues)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noBlackRooksQueensOrBishops(board) && (blackBishopDrawOnFileA(board) || blackBishopDrawOnFileH(board))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (materialValues.whitePawns == 0) blackWinningNoWhitePawnsEndGameAdjustment(board, materialValues, currentScore)
        else currentScore

fun blackWinningNoWhitePawnsEndGameAdjustment(board: EngineBoard, materialValues: MaterialValues, currentScore: Int) =
        if (blackMoreThanABishopUpInNonPawns(materialValues)) {
            if (blackHasOnlyTwoKnights(board, materialValues) && materialValues.whitePieces == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (blackHasOnlyAKnightAndBishop(board, materialValues) && materialValues.whitePieces == 0) {
                blackKnightAndBishopVKingEval(currentScore, board)
            } else currentScore - VALUE_SHOULD_WIN
        } else currentScore

fun blackKnightAndBishopVKingEval(currentScore: Int, board: EngineBoard): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (blackDarkBishopExists(board)) enemyKingCloseToDarkCornerMateSquareValue(board.whiteKingSquare)
    else enemyKingCloseToLightCornerMateSquareValue(board.whiteKingSquare)
}

fun whiteWinningEndGameAdjustment(board: EngineBoard, materialValues: MaterialValues, currentScore: Int) =
        if (whiteHasInsufficientMaterial(materialValues)) currentScore - (materialValues.whitePieces * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probablyDrawWhenWhiteIsWinning(materialValues)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noWhiteRooksQueensOrKnights(board) && (whiteBishopDrawOnFileA(board) || whiteBishopDrawOnFileH(board))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (materialValues.blackPawns == 0) whiteWinningNoBlackPawnsEndGameAdjustment(board, materialValues, currentScore)
        else currentScore

fun whiteWinningNoBlackPawnsEndGameAdjustment(board: EngineBoard, materialValues: MaterialValues, currentScore: Int) =
        if (whiteMoreThanABishopUpInNonPawns(materialValues)) {
            if (whiteHasOnlyTwoKnights(board, materialValues) && materialValues.blackPieces == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (whiteHasOnlyAKnightAndBishop(board, materialValues) && materialValues.blackPieces == 0)
                whiteKnightAndBishopVKingEval(currentScore, board)
            else currentScore + VALUE_SHOULD_WIN
        } else currentScore

fun whiteKnightAndBishopVKingEval(currentScore: Int, board: EngineBoard): Int {
    whiteShouldWinWithKnightAndBishopValue(currentScore)
    return +if (whiteDarkBishopExists(board)) enemyKingCloseToDarkCornerMateSquareValue(board.blackKingSquare)
    else enemyKingCloseToLightCornerMateSquareValue(board.blackKingSquare)
}

fun enemyKingCloseToDarkCornerMateSquareValue(kingSquare: Int) = enemyKingCloseToLightCornerMateSquareValue(bitFlippedHorizontalAxis[kingSquare])

fun enemyKingCloseToLightCornerMateSquareValue(kingSquare: Int) = (7 - distanceToH1OrA8[kingSquare]) * ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE

fun blackShouldWinWithKnightAndBishopValue(eval: Int) =
        -(VALUE_KNIGHT + VALUE_BISHOP + VALUE_SHOULD_WIN) +
                eval / ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR

fun whiteShouldWinWithKnightAndBishopValue(eval: Int) =
        VALUE_KNIGHT + VALUE_BISHOP + VALUE_SHOULD_WIN + eval / ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR

fun whiteHasOnlyAKnightAndBishop(board: EngineBoard, materialValues: MaterialValues) =
        exactlyOneBitSet(board.getBitboard(BITBOARD_WN)) && (materialValues.whitePieces == VALUE_KNIGHT + VALUE_BISHOP)

fun blackHasOnlyAKnightAndBishop(board: EngineBoard, materialValues: MaterialValues) =
        exactlyOneBitSet(board.getBitboard(BITBOARD_BN)) && (materialValues.blackPieces == VALUE_KNIGHT + VALUE_BISHOP)

fun whiteHasOnlyTwoKnights(board: EngineBoard, materialValues: MaterialValues) =
        popCount(board.getBitboard(BITBOARD_WN)) == 2 && (materialValues.whitePieces == 2 * VALUE_KNIGHT)

fun blackHasOnlyTwoKnights(board: EngineBoard, materialValues: MaterialValues) =
        popCount(board.getBitboard(BITBOARD_BN)) == 2 && (materialValues.blackPieces == 2 * VALUE_KNIGHT)

fun blackMoreThanABishopUpInNonPawns(materialValues: MaterialValues) =
        materialValues.blackPieces - materialValues.whitePieces > VALUE_BISHOP

fun whiteMoreThanABishopUpInNonPawns(materialValues: MaterialValues) =
        materialValues.whitePieces - materialValues.blackPieces > VALUE_BISHOP

fun noBlackRooksQueensOrBishops(board: EngineBoard) =
        board.getBitboard(BITBOARD_BR) or board.getBitboard(BITBOARD_BN) or board.getBitboard(BITBOARD_BQ) == 0L

fun bothSidesHaveOnlyOneKnightOrBishopEach(materialValues: MaterialValues) =
        noPawnsRemain(materialValues) && materialValues.whitePieces < VALUE_ROOK &&
                materialValues.blackPieces < VALUE_ROOK

fun noPawnsRemain(materialValues: MaterialValues) = materialValues.whitePawns + materialValues.blackPawns == 0

fun noWhiteRooksQueensOrKnights(board: EngineBoard) =
        board.getBitboard(BITBOARD_WR) or board.getBitboard(BITBOARD_WN) or board.getBitboard(BITBOARD_WQ) == 0L

fun blackBishopDrawOnFileH(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_BP) and FILE_H.inv() == 0L &&
            board.getBitboard(BITBOARD_BB) and LIGHT_SQUARES == 0L &&
            board.getBitboard(BITBOARD_WK) and H1H2G1G2 != 0L
}

fun blackBishopDrawOnFileA(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_BP) and FILE_A.inv() == 0L &&
            board.getBitboard(BITBOARD_BB) and DARK_SQUARES == 0L &&
            board.getBitboard(BITBOARD_WK) and A1A2B1B2 != 0L
}

fun whiteBishopDrawOnFileA(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_WP) and FILE_A.inv() == 0L &&
            board.getBitboard(BITBOARD_WB) and LIGHT_SQUARES == 0L &&
            board.getBitboard(BITBOARD_BK) and A8A7B8B7 != 0L
}

fun whiteBishopDrawOnFileH(board: EngineBoard): Boolean {
    return board.getBitboard(BITBOARD_WP) and FILE_H.inv() == 0L &&
            board.getBitboard(BITBOARD_WB) and DARK_SQUARES == 0L &&
            board.getBitboard(BITBOARD_BK) and H8H7G8G7 != 0L
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

fun whitePawnsEval(board: EngineBoard, materialValues: MaterialValues): Int {
    var acc = 0
    val blackPieceValues = materialValues.blackPieces
    applyToSquares(board.getBitboard(BITBOARD_WP)) {
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

fun blackPawnsEval(board: EngineBoard, materialValues: MaterialValues): Int {
    var acc = 0
    val whitePieceValues = materialValues.whitePieces
    applyToSquares(board.getBitboard(BITBOARD_BP)) {
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
        board: EngineBoard,
        attacks: Attacks,
        materialValues: MaterialValues) : Int
{

    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BN)) {
        acc += linearScale(
                materialValues.whitePieces + materialValues.whitePawns,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                PieceSquareTables.knightEndGame[bitFlippedHorizontalAxis[it]],
                PieceSquareTables.knight[bitFlippedHorizontalAxis[it]]
        ) - blockedKnightPenaltyEval(it, attacks.whitePawns, board.getBitboard(BITBOARD_BP))
    }
    return acc
}

fun whiteKnightsEval(
        board: EngineBoard,
        attacks: Attacks,
        materialValues: MaterialValues) : Int
{

    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WN)) {
        acc += linearScale(materialValues.blackPieces + materialValues.blackPawns,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                PieceSquareTables.knightEndGame[it],
                PieceSquareTables.knight[it]
        ) - blockedKnightPenaltyEval(it, attacks.blackPawns, board.getBitboard(BITBOARD_WP))
    }
    return acc
}

fun blackBishopsEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BB)) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(board, it) and blackPiecesInverted)] +
                flippedSquareTableScore(PieceSquareTables.bishop, it)
    }
    return acc
}

fun whiteBishopEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WB)) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(board, it) and whitePiecesInverted)] + PieceSquareTables.bishop[it]
    }
    return acc
}

private fun blackQueensEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BQ)) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(board, it) and blackPiecesInverted)] + flippedSquareTableScore(PieceSquareTables.queen, it)
    }
    return acc
}

private fun whiteQueensEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WQ)) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(board, it) and whitePiecesInverted)] + PieceSquareTables.queen[it]
    }
    return acc
}

private fun blackRooksEval(board: EngineBoard, materialValues: MaterialValues, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BR)) {
        acc += blackRookOpenFilesEval(board, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(board, it) and blackPiecesInverted)] +
                flippedSquareTableScore(PieceSquareTables.rook, it) * rookEnemyPawnMultiplier(materialValues.whitePawns) / 6
    }
    return acc
}

fun whiteRooksEval(board: EngineBoard, materialValues: MaterialValues, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WR)) {
        acc += whiteRookOpenFilesEval(board, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(board, it) and whitePiecesInverted)] +
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
            ((popCount(whitePawnBitboard and FILE_A) + popCount(whitePawnBitboard and FILE_H)) * VALUE_SIDE_PAWN_PENALTY) +
            ((popCount(blackPawnBitboard and FILE_A) + popCount(blackPawnBitboard and FILE_H)) * VALUE_SIDE_PAWN_PENALTY) -
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

