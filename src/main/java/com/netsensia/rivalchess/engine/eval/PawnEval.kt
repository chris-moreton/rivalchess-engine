package com.netsensia.rivalchess.engine.eval

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.bitboards.util.applyToSquares
import com.netsensia.rivalchess.bitboards.util.northFill
import com.netsensia.rivalchess.bitboards.util.southFill
import com.netsensia.rivalchess.config.*
import com.netsensia.rivalchess.consts.BITBOARD_BP
import com.netsensia.rivalchess.consts.BITBOARD_WP
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.pawnValues
import com.netsensia.rivalchess.model.Colour
import kotlin.math.abs

@kotlin.ExperimentalUnsignedTypes
private fun pawnDistanceFromPromotion(colour: Colour, square: Int) = if (colour == Colour.WHITE) yCoordOfSquare(square) else 7 - yCoordOfSquare(square)

@kotlin.ExperimentalUnsignedTypes
inline fun pawnShieldEval(friendlyPawns: Long, enemyPawns: Long, friendlyPawnShield: Long, shifter: Long.(Int) -> Long) =
        (KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * (friendlyPawns and friendlyPawnShield).countOneBits()
                - KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * (enemyPawns and (friendlyPawnShield or shifter(friendlyPawnShield, 8))).countOneBits()
                + KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * (friendlyPawns and shifter(friendlyPawnShield, 8)).countOneBits()
                - KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * (enemyPawns and shifter(friendlyPawnShield, 16)).countOneBits())

@kotlin.ExperimentalUnsignedTypes
fun whitePawnsEval(board: EngineBoard): Int {
    var acc = 0
    val blackPieceValues = board.blackPieceValues
    applyToSquares(board.getBitboard(BITBOARD_WP)) { pawnSquare ->
        acc += linearScale(
                blackPieceValues,
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                pawnEndGamePieceSquareTable[pawnSquare],
                pawnPieceSquareTable[pawnSquare]
        )
    }
    return acc
}

@kotlin.ExperimentalUnsignedTypes
fun blackPawnsEval(board: EngineBoard): Int {
    var acc = 0
    val whitePieceValues = board.whitePieceValues
    applyToSquares(board.getBitboard(BITBOARD_BP)) {
        acc += linearScale(
                whitePieceValues,
                PAWN_STAGE_MATERIAL_LOW,
                PAWN_STAGE_MATERIAL_HIGH,
                pawnEndGamePieceSquareTable[bitFlippedHorizontalAxis[it]],
                pawnPieceSquareTable[bitFlippedHorizontalAxis[it]]
        )
    }
    return acc
}

class PawnHash(val whitePawns: Long, val blackPawns: Long, val whiteKingSquare: Int, val blackKingSquare: Int, val mover: Colour, val score: Int)

const val DEFAULT_PAWN_HASHTABLE_SIZE_MB = 32
const val BYTES_PER_MB = 1024 * 1024
const val INT_BYTES = 4
const val LONG_BYTES = 8
const val PAWN_HASH_INDEX_MAX = DEFAULT_PAWN_HASHTABLE_SIZE_MB * BYTES_PER_MB / (LONG_BYTES * 2 + INT_BYTES * 3)
const val USE_PAWN_HASH = false

val pawnHashMap = HashMap<Int, PawnHash>()

private fun pawnHashIndex(whitePawnBitboard: Long, blackPawnBitboard: Long, whiteKingSquare: Int, blackKingSquare: Int, mover: Colour): Int {
    var hash = 23L
    hash = hash * 31L + whitePawnBitboard
    hash = hash * 31L + blackPawnBitboard
    hash = hash * 31L + whiteKingSquare
    hash = hash * 31L + blackKingSquare
    hash = hash * 31L + if (mover == Colour.WHITE) 1 else 0

    return (Math.abs(hash) % PAWN_HASH_INDEX_MAX).toInt()
}

@kotlin.ExperimentalUnsignedTypes
fun whitePassedPawnScore(whitePassedPawnsBitboard: Long, whiteGuardedPassedPawns: Long): Int {
    var acc = 0
    applyToSquares(whitePassedPawnsBitboard) { acc += VALUE_PASSED_PAWN_BONUS[yCoordOfSquare(it)] }
    return whiteGuardedPassedPawns.countOneBits() * VALUE_GUARDED_PASSED_PAWN + acc
}

@kotlin.ExperimentalUnsignedTypes
fun blackPassedPawnScore(blackPassedPawnsBitboard: Long, blackGuardedPassedPawns: Long): Int {
    var acc = 0
    applyToSquares(blackPassedPawnsBitboard) { acc += VALUE_PASSED_PAWN_BONUS[7 - yCoordOfSquare(it)] }
    return blackGuardedPassedPawns.countOneBits() * VALUE_GUARDED_PASSED_PAWN + acc
}

@kotlin.ExperimentalUnsignedTypes
fun pawnScore(attacks: Attacks, board: EngineBoard): Int {

    val whitePawnBitboard = board.getBitboard(BITBOARD_WP)
    val blackPawnBitboard = board.getBitboard(BITBOARD_BP)

    val whitePassedPawnsBitboard = getWhitePassedPawns(whitePawnBitboard, blackPawnBitboard)
    val blackPassedPawnsBitboard = getBlackPassedPawns(whitePawnBitboard, blackPawnBitboard)
    val whiteGuardedPassedPawns = whitePassedPawnsBitboard and attacks.whitePawnsAttackBitboard
    val blackGuardedPassedPawns = blackPassedPawnsBitboard and attacks.blackPawnsAttackBitboard

    val pawnHashIndex = if (USE_PAWN_HASH) 
        pawnHashIndex(whitePawnBitboard, blackPawnBitboard, board.whiteKingSquareCalculated, board.blackKingSquareCalculated, board.mover) else 0

    val hashedScore = if (USE_PAWN_HASH && pawnHashMap.containsKey(pawnHashIndex) &&
            pawnHashMap[pawnHashIndex]!!.whitePawns == whitePawnBitboard && pawnHashMap[pawnHashIndex]!!.blackPawns == blackPawnBitboard
                && pawnHashMap[pawnHashIndex]!!.whiteKingSquare == board.whiteKingSquareCalculated &&
            pawnHashMap[pawnHashIndex]!!.blackKingSquare == board.blackKingSquareCalculated
                && pawnHashMap[pawnHashIndex]!!.mover == board.mover) {
            pawnHashMap[pawnHashIndex]!!.score
        } else -9999

    val score = if (USE_PAWN_HASH && hashedScore != -9999) hashedScore
    else
    {
        val whitePawnFiles = getPawnFiles(whitePawnBitboard)
        val blackPawnFiles = getPawnFiles(blackPawnBitboard)

        val whiteBackwardPawns = getWhiteBackwardPawns(whitePawnBitboard, blackPawnBitboard, attacks.whitePawnsAttackBitboard, attacks.blackPawnsAttackBitboard, blackPawnFiles)
        val blackBackwardPawns = getBlackBackwardPawns(blackPawnBitboard, whitePawnBitboard, attacks.blackPawnsAttackBitboard, attacks.whitePawnsAttackBitboard, whitePawnFiles)
        val whiteIsolatedPawns = whitePawnFiles and (whitePawnFiles shl 1).inv() and (whitePawnFiles ushr 1).inv()
        val blackIsolatedPawns = blackPawnFiles and (blackPawnFiles shl 1).inv() and (blackPawnFiles ushr 1).inv()

        val whiteOccupiedFileMask = southFill(whitePawnBitboard) and RANK_1
        val blackOccupiedFileMask = southFill(blackPawnBitboard) and RANK_1

        (blackIsolatedPawns).countOneBits() * VALUE_ISOLATED_PAWN_PENALTY -
        (whiteIsolatedPawns).countOneBits() * VALUE_ISOLATED_PAWN_PENALTY -
        (if (whiteIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) +
        (if (blackIsolatedPawns and FILE_D != 0L) VALUE_ISOLATED_DPAWN_PENALTY else 0) -
        (whiteBackwardPawns).countOneBits() * VALUE_BACKWARD_PAWN_PENALTY +
        (blackBackwardPawns).countOneBits() * VALUE_BACKWARD_PAWN_PENALTY -
        (((whitePawnBitboard and FILE_A).countOneBits() + (whitePawnBitboard and FILE_H).countOneBits()) * VALUE_SIDE_PAWN_PENALTY) +
        (((blackPawnBitboard and FILE_A).countOneBits() + (blackPawnBitboard and FILE_H).countOneBits()) * VALUE_SIDE_PAWN_PENALTY) -
        VALUE_DOUBLED_PAWN_PENALTY * (board.pawnValues(BITBOARD_WP) / 100 - (whiteOccupiedFileMask).countOneBits()) -
        (whiteOccupiedFileMask.inv() ushr 1 and whiteOccupiedFileMask).countOneBits() * VALUE_PAWN_ISLAND_PENALTY +
        VALUE_DOUBLED_PAWN_PENALTY * (board.pawnValues(BITBOARD_BP) / 100 - (blackOccupiedFileMask).countOneBits()) +
        (blackOccupiedFileMask.inv() ushr 1 and blackOccupiedFileMask).countOneBits() * VALUE_PAWN_ISLAND_PENALTY
    }

    if (USE_PAWN_HASH) {
        pawnHashMap[pawnHashIndex] = PawnHash(
                whitePawnBitboard,
                blackPawnBitboard,
                board.whiteKingSquareCalculated,
                board.blackKingSquareCalculated,
                board.mover,
                score)
    }

    val whitePassedPawnScore = whitePassedPawnScore(whitePassedPawnsBitboard, whiteGuardedPassedPawns)
    val blackPassedPawnScore = blackPassedPawnScore(blackPassedPawnsBitboard, blackGuardedPassedPawns)

    val impureScore =
        (linearScale(board.blackPieceValues, 0, PAWN_ADJUST_MAX_MATERIAL,whitePassedPawnScore * 2, whitePassedPawnScore)) -
                (linearScale(board.whitePieceValues, 0, PAWN_ADJUST_MAX_MATERIAL,blackPassedPawnScore * 2, blackPassedPawnScore)) +
                (if (board.blackPieceValues < PAWN_ADJUST_MAX_MATERIAL)
                    calculateLowMaterialPawnBonus(
                            Colour.BLACK,
                            board.whiteKingSquareCalculated,
                            board.blackKingSquareCalculated,
                            board,
                            whitePassedPawnsBitboard,
                            blackPassedPawnsBitboard,
                            board.mover)
                else 0) +
                (if (board.whitePieceValues < PAWN_ADJUST_MAX_MATERIAL)
                    calculateLowMaterialPawnBonus(
                            Colour.WHITE,
                            board.whiteKingSquareCalculated,
                            board.blackKingSquareCalculated,
                            board,
                            whitePassedPawnsBitboard,
                            blackPassedPawnsBitboard,
                            board.mover)
                else 0)

    return score + impureScore
}

@kotlin.ExperimentalUnsignedTypes
fun calculateLowMaterialPawnBonus(
        lowMaterialColour: Colour,
        whiteKingSquare: Int,
        blackKingSquare: Int,
        board: EngineBoard,
        whitePassedPawnsBitboard: Long,
        blackPassedPawnsBitboard: Long,
        mover: Colour
): Int {

    val kingSquare = if (lowMaterialColour == Colour.WHITE) whiteKingSquare else blackKingSquare
    val kingX = xCoordOfSquare(kingSquare)
    val kingY = yCoordOfSquare(kingSquare)
    val lowMaterialSidePieceValues = if (lowMaterialColour == Colour.WHITE) board.whitePieceValues else board.blackPieceValues

    var acc = 0
    applyToSquares(if (lowMaterialColour == Colour.WHITE) blackPassedPawnsBitboard else whitePassedPawnsBitboard) {
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

@kotlin.ExperimentalUnsignedTypes
fun colourAdjustedYRank(colour: Colour, yRank: Int) = if (colour == Colour.WHITE) yRank else abs(yRank - 7)

@kotlin.ExperimentalUnsignedTypes
fun difference(kingX: Int, it: Int) = abs(kingX - xCoordOfSquare(it))

@kotlin.ExperimentalUnsignedTypes
fun xCoordOfSquare(square: Int) = square % 8

@kotlin.ExperimentalUnsignedTypes
fun yCoordOfSquare(square: Int) = square / 8

@kotlin.ExperimentalUnsignedTypes
fun getPawnFiles(pawns: Long) = southFill(pawns) and RANK_1

@kotlin.ExperimentalUnsignedTypes
fun getBlackPassedPawns(whitePawns: Long, blackPawns: Long) =
        blackPawns and northFill(whitePawns or whitePawnAttacks(whitePawns) or (blackPawns shl 8)).inv()

@kotlin.ExperimentalUnsignedTypes
fun getWhitePassedPawns(whitePawns: Long, blackPawns: Long) =
        whitePawns and southFill(blackPawns or blackPawnAttacks(blackPawns) or (whitePawns ushr 8)).inv()

@kotlin.ExperimentalUnsignedTypes
fun getWhiteBackwardPawns(whitePawnBitboard: Long, blackPawnBitboard: Long, whitePawnAttacks: Long, blackPawnAttacks: Long, blackPawnFiles: Long) =
        whitePawnBitboard and
                (whitePawnBitboard or blackPawnBitboard ushr 8).inv() and
                (blackPawnAttacks ushr 8) and
                northFill(whitePawnAttacks).inv() and
                blackPawnAttacks(whitePawnBitboard) and
                northFill(blackPawnFiles).inv()

@kotlin.ExperimentalUnsignedTypes
fun getBlackBackwardPawns(blackPawnBitboard: Long, whitePawnBitboard: Long, blackPawnAttacks: Long, whitePawnAttacks: Long, whitePawnFiles: Long) =
        blackPawnBitboard and
                (blackPawnBitboard or whitePawnBitboard shl 8).inv() and
                (whitePawnAttacks shl 8) and
                southFill(blackPawnAttacks).inv() and
                whitePawnAttacks(blackPawnBitboard) and
                northFill(whitePawnFiles).inv()
