package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.bitboards.util.popCount
import com.netsensia.rivalchess.bitboards.util.southFill
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.model.Colour
import com.netsensia.rivalchess.model.Square

const val PHASE1_CUTOFF = 2500
const val PHASE2_CUTOFF = 255


fun linearScaleKotlin(x: Int, min: Int, max: Int, a: Int, b: Int) =
        when {
            x < min -> a
            x > max -> b
            else -> a + (x - min) * (b - a) / (max - min)
        }

@JvmOverloads
fun evaluate(board: EngineBoard, minScore: Int = -Int.MAX_VALUE): Int {

    if (onlyKingsRemain(board)) return 0

    val materialDifference = materialDifferenceEval(board)
    val isEndGame = isEndGame(board)

    val adjustedMaterialDifference = if (board.mover == Colour.WHITE) materialDifference else -materialDifference

    val viableEvalForPhase1 = isEndGame || (adjustedMaterialDifference + PHASE1_CUTOFF >= minScore)
    if (!viableEvalForPhase1) return adjustedMaterialDifference

    val attacks = Attacks(board)

    val positionalEvalPart1 =
            materialDifference +
            pawnScore(attacks, board) +
            tradePawnBonusWhenMoreMaterial(board, materialDifference) +
            tradePieceBonusWhenMoreMaterial(board, materialDifference) +
            pawnPieceSquareEval(board) +
            kingSafetyEval(board, attacks) +
            kingSquareEval(board) +
            threatEval(attacks, board)

    val adjustedEval = if (board.mover == Colour.WHITE) positionalEvalPart1 else -positionalEvalPart1
    val viableEvalForPhase2 = isEndGame || (adjustedEval + PHASE2_CUTOFF >= minScore)

    if (!viableEvalForPhase2) return adjustedEval

    val whitePieces = if (board.mover == Colour.WHITE) board.getBitboard(BITBOARD_FRIENDLY) else board.getBitboard(BITBOARD_ENEMY)
    val blackPieces = if (board.mover == Colour.WHITE) board.getBitboard(BITBOARD_ENEMY) else board.getBitboard(BITBOARD_FRIENDLY)

    val positionalEval = positionalEvalPart1 + (if (viableEvalForPhase2)
        (       doubledRooksEval(board) +
                rooksEval(board, whitePieces, blackPieces) +
                bishopsEval(board, whitePieces, blackPieces) +
                knightsEval(board, attacks) +
                castlingEval(board, board.castlePrivileges) +
                queensEval(board, whitePieces, blackPieces) +
                bishopScore(board, materialDifference)) else 0)

    val endGameAdjustedScore = if (isEndGame) endGameAdjustment(board, positionalEval) else positionalEval

    return if (board.mover == Colour.WHITE) endGameAdjustedScore else -endGameAdjustedScore
}

private fun queensEval(board: EngineBoard, whitePieces: Long, blackPieces: Long) =
        (whiteQueensEval(board, whitePieces.inv()) - blackQueensEval(board, blackPieces.inv()))

private fun kingSquareEval(board: EngineBoard) =
        (whiteKingSquareEval(board) - blackKingSquareEval(board))

private fun knightsEval(board: EngineBoard, attacks: Attacks) =
        (whiteKnightsEval(board, attacks) - blackKnightsEval(board, attacks))

private fun bishopsEval(board: EngineBoard, whitePieces: Long, blackPieces: Long) =
        (whiteBishopEval(board, whitePieces.inv()) - blackBishopsEval(board, blackPieces.inv()))

private fun pawnPieceSquareEval(board: EngineBoard) = whitePawnsEval(board) - blackPawnsEval(board)

private fun rooksEval(board: EngineBoard, whitePieces: Long, blackPieces: Long) =
        (whiteRooksEval(board, whitePieces.inv()) - blackRooksEval(board, blackPieces.inv()))

private fun doubledRooksEval(board: EngineBoard) =
        (doubledRooksEval(board.getBitboard(BITBOARD_WR)) - doubledRooksEval(board.getBitboard(BITBOARD_BR)))

fun materialDifferenceEval(board: EngineBoard) =
        board.whitePieceValues - board.blackPieceValues +
                board.whitePawnValues - board.blackPawnValues

fun exactlyOneBitSet(bitboard: Long) = (bitboard and (bitboard - 1)) == 0L && bitboard != 0L

fun onlyKingsRemain(board: EngineBoard) = exactlyOneBitSet(board.getBitboard(BITBOARD_ENEMY)) and exactlyOneBitSet(board.getBitboard(BITBOARD_FRIENDLY))

fun whiteKingSquareEval(board: EngineBoard) =
        linearScaleKotlin(
                board.blackPieceValues,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                kingEndGamePieceSquareTable[board.whiteKingSquare],
                kingPieceSquareTable[board.whiteKingSquare]
        )

fun blackKingSquareEval(board: EngineBoard) =
        linearScaleKotlin(
                board.whitePieceValues,
                VALUE_ROOK,
                OPENING_PHASE_MATERIAL,
                kingEndGamePieceSquareTable[bitFlippedHorizontalAxis[board.blackKingSquare]],
                kingPieceSquareTable[bitFlippedHorizontalAxis[board.blackKingSquare]]
        )

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

fun doubledRooksEval(rookBitboard: Long): Int {
    val files = booleanArrayOf(false, false, false, false, false, false, false, false)
    applyToSquares(rookBitboard) { square ->
        if (files[square % 8]) return VALUE_ROOKS_ON_SAME_FILE
        files[square % 8] = true
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

fun tradePieceBonusWhenMoreMaterial(board: EngineBoard, materialDifference: Int) =
        linearScaleKotlin(
                if (materialDifference > 0)
                    board.blackPieceValues + board.blackPawnValues else
                    board.whitePieceValues + board.whitePawnValues,
                0,
                TOTAL_PIECE_VALUE_PER_SIDE_AT_START,
                30 * materialDifference / 100,
                0)

fun tradePawnBonusWhenMoreMaterial(board: EngineBoard, materialDifference: Int) =
        linearScaleKotlin(
                if (materialDifference > 0) board.whitePawnValues else board.blackPawnValues,
                0,
                PAWN_TRADE_BONUS_MAX,
                -30 * materialDifference / 100,
                0)

fun bishopScore(board: EngineBoard, materialDifference: Int) =
        bishopPairEval(board) + oppositeColourBishopsEval(board, materialDifference) + trappedBishopEval(board)

fun whiteLightBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_WB) and LIGHT_SQUARES != 0L
fun whiteDarkBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_WB) and DARK_SQUARES != 0L
fun blackLightBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_BB) and LIGHT_SQUARES != 0L
fun blackDarkBishopExists(board: EngineBoard) = board.getBitboard(BITBOARD_BB) and DARK_SQUARES != 0L
fun whiteBishopColourCount(board: EngineBoard) = (if (whiteLightBishopExists(board)) 1 else 0) + if (whiteDarkBishopExists(board)) 1 else 0
fun blackBishopColourCount(board: EngineBoard) = (if (blackLightBishopExists(board)) 1 else 0) + if (blackDarkBishopExists(board)) 1 else 0

fun oppositeColourBishopsEval(board: EngineBoard, materialDifference: Int): Int {

    if (whiteBishopColourCount(board) == 1 && blackBishopColourCount(board) == 1 &&
            whiteLightBishopExists(board) != blackLightBishopExists(board) &&
            board.whitePieceValues == board.blackPieceValues) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty = materialDifference / WRONG_COLOUR_BISHOP_PENALTY_DIVISOR // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        return -linearScaleKotlin(
                board.whitePieceValues + board.blackPieceValues,
                WRONG_COLOUR_BISHOP_MATERIAL_LOW,
                WRONG_COLOUR_BISHOP_MATERIAL_HIGH,
                maxPenalty,
                0)
    }
    return 0
}

fun bishopPairEval(board: EngineBoard) =
        bishopPairEvalForColour(board, ::whiteBishopColourCount, board.whitePawnValues) -
        bishopPairEvalForColour(board, ::blackBishopColourCount, board.blackPawnValues)

inline fun bishopPairEvalForColour(board: EngineBoard, fn: (EngineBoard) -> Int, pawnValues: Int) =
        (if (fn(board) == 2) VALUE_BISHOP_PAIR + (8 - pawnValues / VALUE_PAWN) * VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS else 0)

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

fun isEndGame(board: EngineBoard) =
        (board.whitePieceValues +
                board.whitePawnValues +
                board.blackPieceValues +
                board.blackPawnValues) <= EVAL_ENDGAME_TOTAL_PIECES

fun kingSafetyEval(board: EngineBoard, attacks: Attacks): Int {

    val whiteKingAttackedCount = whiteKingAttackCount(whiteKingDangerZone[board.whiteKingSquare], attacks)
    val blackKingAttackedCount = blackKingAttackCount(blackKingDangerZone[board.blackKingSquare], attacks)

    val averagePiecesPerSide = (board.whitePieceValues + board.blackPieceValues) / 2

    if (averagePiecesPerSide <= KINGSAFETY_MIN_PIECE_BALANCE) return 0

    val whiteKingSafety: Int = whiteKingShieldEval(board)
    val blackKingSafety: Int = blackKingShieldEval(board)

    return linearScaleKotlin(
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

fun whiteCastlingEval(board: EngineBoard, castlePrivileges: Int) : Int {

    val whiteCastleValue = maxCastleValue(board.blackPieceValues)

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

fun blackCastlingEval(board: EngineBoard, castlePrivileges: Int) : Int {
    // Value of moving King to its queenside castle destination in the middle game
    val blackCastleValue = maxCastleValue(board.whitePieceValues)

    return if (blackCastleValue > 0) {
        blackCastleValue / blackTimeToCastleKingSide(castlePrivileges, board)
                .coerceAtMost(blackTimeToCastleQueenSide(castlePrivileges, board))
    } else 0
}

fun rookSquareBonus() = rookPieceSquareTable[3] - rookPieceSquareTable[0]

fun kingSquareBonusEndGame() = kingEndGamePieceSquareTable[1] - kingEndGamePieceSquareTable[3]

fun kingSquareBonusMiddleGame() = kingPieceSquareTable[1] - kingPieceSquareTable[3]

fun castlingEval(board: EngineBoard, castlePrivileges: Int) =
        if (castlePrivileges != 0) {
            whiteCastlingEval(board, castlePrivileges) - blackCastlingEval(board, castlePrivileges)
        } else 0

// don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
// given by still having castling rights.
fun maxCastleValue(pieceValues: Int) =
        kingSquareBonusScaled(pieceValues, kingSquareBonusEndGame(), kingSquareBonusMiddleGame()) + rookSquareBonus()

fun kingSquareBonusScaled(pieceValues: Int, kingSquareBonusEndGame: Int, kingSquareBonusMiddleGame: Int) =
        linearScaleKotlin(
                pieceValues,
                CASTLE_BONUS_LOW_MATERIAL,
                CASTLE_BONUS_HIGH_MATERIAL,
                kingSquareBonusEndGame,
                kingSquareBonusMiddleGame)

fun endGameAdjustment(board: EngineBoard, currentScore: Int) =
        if (bothSidesHaveOnlyOneKnightOrBishopEach(board)) currentScore / ENDGAME_DRAW_DIVISOR
        else when (currentScore) {
            0 -> 0
            in 0..Int.MAX_VALUE -> whiteWinningEndGameAdjustment(board, currentScore)
            else -> blackWinningEndGameAdjustment(board, currentScore)
        }

fun penaltyForKingNotBeingNearOtherKing(board: EngineBoard) =
        (kotlin.math.abs(xCoordOfSquare(board.whiteKingSquare) - xCoordOfSquare(board.blackKingSquare)) +
                kotlin.math.abs(yCoordOfSquare(board.whiteKingSquare) - yCoordOfSquare(board.blackKingSquare))
                ) * KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING

fun blackWinningEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        (if (blackHasInsufficientMaterial(board)) currentScore + (board.blackPieceValues * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probableDrawWhenBlackIsWinning(board)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noBlackRooksQueensOrBishops(board) && (blackBishopDrawOnFileA(board) || blackBishopDrawOnFileH(board))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (board.whitePawnValues == 0) blackWinningNoWhitePawnsEndGameAdjustment(board, currentScore)
        else currentScore) + penaltyForKingNotBeingNearOtherKing(board) - (kingInCornerPieceSquareTable[board.whiteKingSquare] * KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING)

fun blackWinningNoWhitePawnsEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        if (blackMoreThanABishopUpInNonPawns(board)) {
            if (blackHasOnlyTwoKnights(board) && board.whitePieceValues == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (blackHasOnlyAKnightAndBishop(board) && board.whitePieceValues == 0) {
                blackKnightAndBishopVKingEval(currentScore, board)
            } else currentScore - VALUE_SHOULD_WIN
        } else currentScore

fun blackKnightAndBishopVKingEval(currentScore: Int, board: EngineBoard): Int {
    blackShouldWinWithKnightAndBishopValue(currentScore)
    return -if (blackDarkBishopExists(board)) enemyKingCloseToDarkCornerMateSquareValue(board.whiteKingSquare)
    else enemyKingCloseToLightCornerMateSquareValue(board.whiteKingSquare)
}

fun whiteWinningEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        (if (whiteHasInsufficientMaterial(board)) currentScore - (board.whitePieceValues * endgameSubtractInsufficientMaterialMultiplier).toInt()
        else if (probablyDrawWhenWhiteIsWinning(board)) currentScore / ENDGAME_PROBABLE_DRAW_DIVISOR
        else if (noWhiteRooksQueensOrKnights(board) && (whiteBishopDrawOnFileA(board) || whiteBishopDrawOnFileH(board))) currentScore / ENDGAME_DRAW_DIVISOR
        else if (board.blackPawnValues == 0) whiteWinningNoBlackPawnsEndGameAdjustment(board, currentScore)
        else currentScore) - penaltyForKingNotBeingNearOtherKing(board) + (kingInCornerPieceSquareTable[board.blackKingSquare] * KING_PENALTY_FOR_DISTANCE_PER_SQUARE_WHEN_WINNING)

fun whiteWinningNoBlackPawnsEndGameAdjustment(board: EngineBoard, currentScore: Int) =
        if (whiteMoreThanABishopUpInNonPawns(board)) {
            if (whiteHasOnlyTwoKnights(board) && board.blackPieceValues == 0) currentScore / ENDGAME_DRAW_DIVISOR
            else if (whiteHasOnlyAKnightAndBishop(board) && board.blackPieceValues == 0)
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

fun whiteHasOnlyAKnightAndBishop(board: EngineBoard) =
        exactlyOneBitSet(board.getBitboard(BITBOARD_WN)) && (board.whitePieceValues == VALUE_KNIGHT + VALUE_BISHOP)

fun blackHasOnlyAKnightAndBishop(board: EngineBoard) =
        exactlyOneBitSet(board.getBitboard(BITBOARD_BN)) && (board.blackPieceValues == VALUE_KNIGHT + VALUE_BISHOP)

fun whiteHasOnlyTwoKnights(board: EngineBoard) =
        popCount(board.getBitboard(BITBOARD_WN)) == 2 && (board.whitePieceValues == 2 * VALUE_KNIGHT)

fun blackHasOnlyTwoKnights(board: EngineBoard) =
        popCount(board.getBitboard(BITBOARD_BN)) == 2 && (board.blackPieceValues == 2 * VALUE_KNIGHT)

fun blackMoreThanABishopUpInNonPawns(board: EngineBoard) =
        board.blackPieceValues - board.whitePieceValues > VALUE_BISHOP

fun whiteMoreThanABishopUpInNonPawns(board: EngineBoard) =
        board.whitePieceValues - board.blackPieceValues > VALUE_BISHOP

fun noBlackRooksQueensOrBishops(board: EngineBoard) =
        board.getBitboard(BITBOARD_BR) or board.getBitboard(BITBOARD_BN) or board.getBitboard(BITBOARD_BQ) == 0L

fun bothSidesHaveOnlyOneKnightOrBishopEach(board: EngineBoard) =
        noPawnsRemain(board) && board.whitePieceValues < VALUE_ROOK &&
                board.blackPieceValues < VALUE_ROOK

fun noPawnsRemain(board: EngineBoard) = board.whitePawnValues + board.blackPawnValues == 0

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

fun probableDrawWhenBlackIsWinning(board: EngineBoard) =
        board.blackPawnValues == 0 && board.blackPieceValues - VALUE_BISHOP <= board.whitePieceValues

fun probablyDrawWhenWhiteIsWinning(board: EngineBoard) =
        board.whitePawnValues == 0 && board.whitePieceValues - VALUE_BISHOP <= board.blackPieceValues

fun blackHasInsufficientMaterial(board: EngineBoard) =
        board.blackPawnValues == 0 && (board.blackPieceValues == VALUE_KNIGHT || board.blackPieceValues == VALUE_BISHOP)

fun whiteHasInsufficientMaterial(board: EngineBoard) =
        board.whitePawnValues == 0 && (board.whitePieceValues == VALUE_KNIGHT || board.whitePieceValues == VALUE_BISHOP)

fun blockedKnightPenaltyEval(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        popCount(blockedKnightLandingSquares(square, enemyPawnAttacks, friendlyPawns)) * KNIGHT_LANDING_SQ_PAWN_ATK_PENALTY

fun blockedKnightLandingSquares(square: Int, enemyPawnAttacks: Long, friendlyPawns: Long) =
        knightMoves[square] and (enemyPawnAttacks or friendlyPawns)



fun blackKnightsEval(board: EngineBoard, attacks: Attacks) : Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BN)) {
        acc += linearScaleKotlin(
                board.whitePieceValues + board.whitePawnValues,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                knightEndGamePieceSquareTable[bitFlippedHorizontalAxis[it]],
                knightPieceSquareTable[bitFlippedHorizontalAxis[it]]
        ) - blockedKnightPenaltyEval(it, attacks.whitePawns, board.getBitboard(BITBOARD_BP))
    }
    return acc
}

fun whiteKnightsEval(board: EngineBoard, attacks: Attacks) : Int {

    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WN)) {
        acc += linearScaleKotlin(board.blackPieceValues + board.blackPawnValues,
                KNIGHT_STAGE_MATERIAL_LOW,
                KNIGHT_STAGE_MATERIAL_HIGH,
                knightEndGamePieceSquareTable[it],
                knightPieceSquareTable[it]
        ) - blockedKnightPenaltyEval(it, attacks.blackPawns, board.getBitboard(BITBOARD_WP))
    }
    return acc
}

fun blackBishopsEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BB)) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(board, it) and blackPiecesInverted)] +
                flippedSquareTableScore(bishopPieceSquareTable, it)
    }
    return acc
}

fun whiteBishopEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WB)) {
        acc += VALUE_BISHOP_MOBILITY[popCount(bishopAttacks(board, it) and whitePiecesInverted)] + bishopPieceSquareTable[it]
    }
    return acc
}

private fun blackQueensEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BQ)) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(board, it) and blackPiecesInverted)] + flippedSquareTableScore(queenPieceSquareTable, it)
    }
    return acc
}

private fun whiteQueensEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WQ)) {
        acc += VALUE_QUEEN_MOBILITY[popCount(queenAttacks(board, it) and whitePiecesInverted)] + queenPieceSquareTable[it]
    }
    return acc
}

private fun blackRooksEval(board: EngineBoard, blackPiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_BR)) {
        acc += blackRookOpenFilesEval(board, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(board, it) and blackPiecesInverted)] +
                flippedSquareTableScore(rookPieceSquareTable, it) * rookEnemyPawnMultiplier(board.whitePawnValues) / 6
    }
    return acc
}

fun whiteRooksEval(board: EngineBoard, whitePiecesInverted: Long): Int {
    var acc = 0
    applyToSquares(board.getBitboard(BITBOARD_WR)) {
        acc += whiteRookOpenFilesEval(board, it % 8) +
                VALUE_ROOK_MOBILITY[popCount(rookAttacks(board, it) and whitePiecesInverted)] +
                rookPieceSquareTable[it] * rookEnemyPawnMultiplier(board.blackPawnValues) / 6
    }
    return acc
}

