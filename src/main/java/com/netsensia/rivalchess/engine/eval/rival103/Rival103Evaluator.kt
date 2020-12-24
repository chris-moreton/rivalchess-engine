package com.netsensia.rivalchess.engine.eval.rival103

import com.netsensia.rivalchess.bitboards.*
import com.netsensia.rivalchess.consts.*
import com.netsensia.rivalchess.engine.board.EngineBoard
import com.netsensia.rivalchess.engine.board.pawnValues
import com.netsensia.rivalchess.engine.eval.linearScale
import com.netsensia.rivalchess.model.Colour

fun evaluateRival103(board: EngineBoard): Int {
    if (board.whitePieceValues + board.blackPieceValues + board.pawnValues(BITBOARD_WP) + board.pawnValues(BITBOARD_BP) === 0) return 0
    var sq: Int
    var bitboard: Long
    var whiteKingAttackedCount = 0
    var blackKingAttackedCount = 0
    var whiteAttacksBitboard: Long = 0
    var blackAttacksBitboard: Long = 0
    val whitePawnAttacks: Long =
        board.getBitboard(BITBOARD_WP) and FILE_A.inv() shl 9 or (board.getBitboard(BITBOARD_WP) and FILE_H.inv() shl 7)
    val blackPawnAttacks: Long = board.getBitboard(BITBOARD_BP) and FILE_A.inv() ushr 7 or (board.getBitboard(BITBOARD_BP) and FILE_H.inv() ushr 9)
    val whitePieces: Long = board.getBitboard(if (board.mover == Colour.WHITE) BITBOARD_FRIENDLY else BITBOARD_ENEMY)
    val blackPieces: Long = board.getBitboard(if (board.mover == Colour.WHITE) BITBOARD_ENEMY else BITBOARD_FRIENDLY)
    val whiteKingDangerZone: Long = kingMoves.get(board.whiteKingSquareCalculated) or (kingMoves.get(board.whiteKingSquareCalculated) shl 8)
    val blackKingDangerZone: Long = kingMoves.get(board.blackKingSquareCalculated) or (kingMoves.get(board.blackKingSquareCalculated) ushr 8)
    val materialDifference: Int = board.whitePieceValues - board.blackPieceValues + board.whitePawnValues - board.blackPawnValues
    var eval = if (RivalConstants.TRACK_PIECE_SQUARE_VALUES) materialDifference
        +linearScale(
            board.blackPieceValues,
            RivalConstants.PAWN_STAGE_MATERIAL_LOW,
            RivalConstants.PAWN_STAGE_MATERIAL_HIGH,
            board.pieceSquareValuesEndGame.get(BITBOARD_WP),
            board.pieceSquareValues.get(BITBOARD_WP)
        )
        -linearScale(
            board.whitePieceValues,
            RivalConstants.PAWN_STAGE_MATERIAL_LOW,
            RivalConstants.PAWN_STAGE_MATERIAL_HIGH,
            board.pieceSquareValuesEndGame.get(BITBOARD_BP),
            board.pieceSquareValues.get(BITBOARD_BP)
        )
    +board.pieceSquareValues.get(BITBOARD_WR) * Math.min(board.blackPawnValues / RivalConstants.VALUE_PAWN, 6) / 6
    -board.pieceSquareValues.get(BITBOARD_BR) * Math.min(board.whitePawnValues / RivalConstants.VALUE_PAWN, 6) / 6
    +board.pieceSquareValues.get(BITBOARD_WB)
    -board.pieceSquareValues.get(BITBOARD_BB)
    +linearScale(
        board.blackPieceValues + board.blackPawnValues,
        RivalConstants.KNIGHT_STAGE_MATERIAL_LOW,
        RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH,
        board.pieceSquareValuesEndGame.get(BITBOARD_WN),
        board.pieceSquareValues.get(BITBOARD_WN)
    )
    -linearScale(
        board.whitePieceValues + board.whitePawnValues,
        RivalConstants.KNIGHT_STAGE_MATERIAL_LOW,
        RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH,
        board.pieceSquareValuesEndGame.get(BITBOARD_BN),
        board.pieceSquareValues.get(BITBOARD_BN)
    )
    +board.pieceSquareValues.get(BITBOARD_WQ)
    -board.pieceSquareValues.get(BITBOARD_BQ)
    +linearScale(
        board.blackPieceValues,
        RivalConstants.VALUE_ROOK,
        RivalConstants.OPENING_PHASE_MATERIAL,
        Bitboards.pieceSquareTableKingEndGame.get(board.whiteKingSquareCalculated),
        Bitboards.pieceSquareTableKing.get(board.whiteKingSquareCalculated)
    )
    -linearScale(
        board.whitePieceValues,
        RivalConstants.VALUE_ROOK,
        RivalConstants.OPENING_PHASE_MATERIAL,
        Bitboards.pieceSquareTableKingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.blackKingSquareCalculated)),
        Bitboards.pieceSquareTableKing.get(Bitboards.bitFlippedHorizontalAxis.get(board.blackKingSquareCalculated))
    ) else materialDifference

    var pieceSquareTemp = 0
    var pieceSquareTempEndGame = 0
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
        bitboard = board.getBitboard(BITBOARD_WP)
        while (bitboard != 0L) {
            bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
            pieceSquareTemp += Bitboards.pieceSquareTablePawn.get(sq)
            pieceSquareTempEndGame += Bitboards.pieceSquareTablePawnEndGame.get(sq)
        }
        eval += linearScale(
            board.blackPieceValues,
            RivalConstants.PAWN_STAGE_MATERIAL_LOW,
            RivalConstants.PAWN_STAGE_MATERIAL_HIGH,
            pieceSquareTempEndGame,
            pieceSquareTemp
        )
        pieceSquareTemp = 0
        pieceSquareTempEndGame = 0
        bitboard = board.getBitboard(BITBOARD_BP)
        while (bitboard != 0L) {
            bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
            pieceSquareTemp += Bitboards.pieceSquareTablePawn.get(Bitboards.bitFlippedHorizontalAxis.get(sq))
            pieceSquareTempEndGame += Bitboards.pieceSquareTablePawnEndGame.get(
                Bitboards.bitFlippedHorizontalAxis.get(
                    sq
                )
            )
        }
        eval -= linearScale(
            board.whitePieceValues,
            RivalConstants.PAWN_STAGE_MATERIAL_LOW,
            RivalConstants.PAWN_STAGE_MATERIAL_HIGH,
            pieceSquareTempEndGame,
            pieceSquareTemp
        )
        eval += (linearScale(
            board.blackPieceValues,
            RivalConstants.VALUE_ROOK,
            RivalConstants.OPENING_PHASE_MATERIAL,
            Bitboards.pieceSquareTableKingEndGame.get(board.whiteKingSquareCalculated),
            Bitboards.pieceSquareTableKing.get(board.whiteKingSquareCalculated)
        )
                - linearScale(
            board.whitePieceValues,
            RivalConstants.VALUE_ROOK,
            RivalConstants.OPENING_PHASE_MATERIAL,
            Bitboards.pieceSquareTableKingEndGame.get(Bitboards.bitFlippedHorizontalAxis.get(board.blackKingSquareCalculated)),
            Bitboards.pieceSquareTableKing.get(Bitboards.bitFlippedHorizontalAxis.get(board.blackKingSquareCalculated))
        ))
    }
    var lastSq = -1
    var file = -1
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp = 0
    bitboard = board.getBitboard(BITBOARD_WR)
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (lastSq != -1 && file == lastSq % 8) eval += RivalConstants.VALUE_ROOKS_ON_SAME_FILE
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp += Bitboards.pieceSquareTableRook.get(sq)
        val allAttacks: Long = Bitboards.magicBitboards.magicMovesRook.get(sq).get(
            ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskRook.get(sq)) * MagicBitboards.magicNumberRook.get(
                sq
            ) ushr MagicBitboards.magicNumberShiftsRook.get(sq)) as Int
        )
        eval += RivalConstants.VALUE_ROOK_MOBILITY.get(java.lang.Long.bitCount(allAttacks and whitePieces.inv()))
        whiteAttacksBitboard = whiteAttacksBitboard or allAttacks
        blackKingAttackedCount += java.lang.Long.bitCount(allAttacks and blackKingDangerZone)
        file = sq % 8
        if (Bitboards.FILES.get(file) and board.getBitboard(BITBOARD_WP) === 0) eval += if (Bitboards.FILES.get(
                file
            ) and board.getBitboard(BITBOARD_BP) === 0
        ) RivalConstants.VALUE_ROOK_ON_OPEN_FILE else RivalConstants.VALUE_ROOK_ON_HALF_OPEN_FILE
        lastSq = sq
    }
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval += pieceSquareTemp * Math.min(
        board.blackPawnValues / RivalConstants.VALUE_PAWN,
        6
    ) / 6
    if (java.lang.Long.bitCount(board.getBitboard(BITBOARD_WR) and Bitboards.RANK_7) > 1 && board.getBitboard(
            BITBOARD_BK
        ) and Bitboards.RANK_8 !== 0
    ) eval += RivalConstants.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING
    bitboard = board.getBitboard(BITBOARD_BR)
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp = 0
    lastSq = -1
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (lastSq != -1 && file == lastSq % 8) eval -= RivalConstants.VALUE_ROOKS_ON_SAME_FILE
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) pieceSquareTemp += Bitboards.pieceSquareTableRook.get(
            Bitboards.bitFlippedHorizontalAxis.get(
                sq
            )
        )
        file = sq % 8
        val allAttacks: Long = Bitboards.magicBitboards.magicMovesRook.get(sq).get(
            ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskRook.get(sq)) * MagicBitboards.magicNumberRook.get(
                sq
            ) ushr MagicBitboards.magicNumberShiftsRook.get(sq)) as Int
        )
        eval -= RivalConstants.VALUE_ROOK_MOBILITY.get(java.lang.Long.bitCount(allAttacks and blackPieces.inv()))
        blackAttacksBitboard = blackAttacksBitboard or allAttacks
        whiteKingAttackedCount += java.lang.Long.bitCount(allAttacks and whiteKingDangerZone)
        if (Bitboards.FILES.get(file) and board.getBitboard(BITBOARD_BP) === 0) eval -= if (Bitboards.FILES.get(
                file
            ) and board.getBitboard(BITBOARD_WP) === 0
        ) RivalConstants.VALUE_ROOK_ON_OPEN_FILE else RivalConstants.VALUE_ROOK_ON_HALF_OPEN_FILE
        lastSq = sq
    }
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval -= pieceSquareTemp * Math.min(
        board.whitePawnValues / RivalConstants.VALUE_PAWN,
        6
    ) / 6
    if (java.lang.Long.bitCount(board.getBitboard(BITBOARD_BR) and Bitboards.RANK_2) > 1 && board.getBitboard(
            BITBOARD_WK
        ) and Bitboards.RANK_1 !== 0
    ) eval -= RivalConstants.VALUE_TWO_ROOKS_ON_SEVENTH_TRAPPING_KING
    bitboard = board.getBitboard(BITBOARD_WN)
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
        pieceSquareTemp = 0
        pieceSquareTempEndGame = 0
    }
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        val knightAttacks: Long = Bitboards.knightMoves.get(sq)
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
            pieceSquareTemp += Bitboards.pieceSquareTableKnight.get(sq)
            pieceSquareTempEndGame += Bitboards.pieceSquareTableKnightEndGame.get(sq)
        }
        whiteAttacksBitboard = whiteAttacksBitboard or knightAttacks
        eval -= java.lang.Long.bitCount(knightAttacks and (blackPawnAttacks or board.getBitboard(BITBOARD_WP))) * RivalConstants.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY
    }
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval += linearScale(
        board.blackPieceValues + board.blackPawnValues,
        RivalConstants.KNIGHT_STAGE_MATERIAL_LOW,
        RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH,
        pieceSquareTempEndGame,
        pieceSquareTemp
    )
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
        pieceSquareTemp = 0
        pieceSquareTempEndGame = 0
    }
    bitboard = board.getBitboard(BITBOARD_BN)
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) {
            pieceSquareTemp += Bitboards.pieceSquareTableKnight.get(Bitboards.bitFlippedHorizontalAxis.get(sq))
            pieceSquareTempEndGame += Bitboards.pieceSquareTableKnightEndGame.get(
                Bitboards.bitFlippedHorizontalAxis.get(
                    sq
                )
            )
        }
        val knightAttacks: Long = Bitboards.knightMoves.get(sq)
        blackAttacksBitboard = blackAttacksBitboard or knightAttacks
        eval += java.lang.Long.bitCount(knightAttacks and (whitePawnAttacks or board.getBitboard(BITBOARD_BP))) * RivalConstants.VALUE_KNIGHT_LANDING_SQUARE_ATTACKED_BY_PAWN_PENALTY
    }
    if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval -= linearScale(
        board.whitePieceValues + board.whitePawnValues,
        RivalConstants.KNIGHT_STAGE_MATERIAL_LOW,
        RivalConstants.KNIGHT_STAGE_MATERIAL_HIGH,
        pieceSquareTempEndGame,
        pieceSquareTemp
    )
    bitboard = board.getBitboard(BITBOARD_WQ)
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval += Bitboards.pieceSquareTableQueen.get(sq)
        val allAttacks: Long = Bitboards.magicBitboards.magicMovesBishop.get(sq).get(
            ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskBishop.get(sq)) * MagicBitboards.magicNumberBishop.get(
                sq
            ) ushr MagicBitboards.magicNumberShiftsBishop.get(sq)) as Int
        ) or
                Bitboards.magicBitboards.magicMovesRook.get(sq).get(
                    ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskRook.get(sq)) * MagicBitboards.magicNumberRook.get(
                        sq
                    ) ushr MagicBitboards.magicNumberShiftsRook.get(sq)) as Int
                )
        whiteAttacksBitboard = whiteAttacksBitboard or allAttacks
        blackKingAttackedCount += java.lang.Long.bitCount(allAttacks and blackKingDangerZone) * 2
        eval += RivalConstants.VALUE_QUEEN_MOBILITY.get(java.lang.Long.bitCount(allAttacks and whitePieces.inv()))
    }
    bitboard = board.getBitboard(BITBOARD_BQ)
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval -= Bitboards.pieceSquareTableQueen.get(
            Bitboards.bitFlippedHorizontalAxis.get(
                sq
            )
        )
        val allAttacks: Long = Bitboards.magicBitboards.magicMovesBishop.get(sq).get(
            ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskBishop.get(sq)) * MagicBitboards.magicNumberBishop.get(
                sq
            ) ushr MagicBitboards.magicNumberShiftsBishop.get(sq)) as Int
        ) or
                Bitboards.magicBitboards.magicMovesRook.get(sq).get(
                    ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskRook.get(sq)) * MagicBitboards.magicNumberRook.get(
                        sq
                    ) ushr MagicBitboards.magicNumberShiftsRook.get(sq)) as Int
                )
        blackAttacksBitboard = blackAttacksBitboard or allAttacks
        whiteKingAttackedCount += java.lang.Long.bitCount(allAttacks and whiteKingDangerZone) * 2
        eval -= RivalConstants.VALUE_QUEEN_MOBILITY.get(java.lang.Long.bitCount(allAttacks and blackPieces.inv()))
    }
    eval += getPawnScore(board, board.whitePieceValues + board.blackPieceValues)
    eval += linearScale(
        if (materialDifference > 0) board.whitePawnValues else board.blackPawnValues,
        0,
        RivalConstants.TRADE_BONUS_UPPER_PAWNS,
        -30 * materialDifference / 100,
        0
    ) +
            linearScale(
                if (materialDifference > 0) board.blackPieceValues + board.blackPawnValues else board.whitePieceValues + board.whitePawnValues,
                0,
                RivalConstants.TOTAL_PIECE_VALUE_PER_SIDE_AT_START,
                30 * materialDifference / 100,
                0
            )
    val castlePrivs: Int = (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_WK) +
            (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_WQ) +
            (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_BK) +
            (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_BQ)
    if (castlePrivs != 0) {
        // Value of moving King to its queenside castle destination in the middle game
        val kingSquareBonusMiddleGame: Int =
            Bitboards.pieceSquareTableKing.get(1) - Bitboards.pieceSquareTableKing.get(3)
        val kingSquareBonusEndGame: Int =
            Bitboards.pieceSquareTableKingEndGame.get(1) - Bitboards.pieceSquareTableKingEndGame.get(3)
        val rookSquareBonus: Int = Bitboards.pieceSquareTableRook.get(3) - Bitboards.pieceSquareTableRook.get(0)
        var kingSquareBonusScaled: Int = linearScale(
            board.blackPieceValues,
            RivalConstants.CASTLE_BONUS_LOW_MATERIAL,
            RivalConstants.CASTLE_BONUS_HIGH_MATERIAL,
            kingSquareBonusEndGame,
            kingSquareBonusMiddleGame
        )

        // don't want to exceed this value because otherwise castling would be discouraged due to the bonuses
        // given by still having castling rights.
        var castleValue = kingSquareBonusScaled + rookSquareBonus
        if (castleValue > 0) {
            var timeToCastleKingSide = 100
            var timeToCastleQueenSide = 100
            if (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_WK !== 0) {
                timeToCastleKingSide = 2
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 1) !== 0) timeToCastleKingSide++
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 2) !== 0) timeToCastleKingSide++
            }
            if (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_WQ !== 0) {
                timeToCastleQueenSide = 2
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 6) !== 0) timeToCastleQueenSide++
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 5) !== 0) timeToCastleQueenSide++
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 4) !== 0) timeToCastleQueenSide++
            }
            eval += castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide)
        }
        kingSquareBonusScaled = linearScale(
            board.whitePieceValues,
            RivalConstants.CASTLE_BONUS_LOW_MATERIAL,
            RivalConstants.CASTLE_BONUS_HIGH_MATERIAL,
            kingSquareBonusEndGame,
            kingSquareBonusMiddleGame
        )
        castleValue = kingSquareBonusScaled + rookSquareBonus
        if (castleValue > 0) {
            var timeToCastleKingSide = 100
            var timeToCastleQueenSide = 100
            if (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_BK !== 0) {
                timeToCastleKingSide = 2
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 57) !== 0) timeToCastleKingSide++
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 58) !== 0) timeToCastleKingSide++
            }
            if (board.m_castlePrivileges and RivalConstants.CASTLEPRIV_BQ !== 0) {
                timeToCastleQueenSide = 2
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 60) !== 0) timeToCastleQueenSide++
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 61) !== 0) timeToCastleQueenSide++
                if (board.getBitboard(BITBOARD_ALL) and (1L shl 62) !== 0) timeToCastleQueenSide++
            }
            eval -= castleValue / Math.min(timeToCastleKingSide, timeToCastleQueenSide)
        }
    }
    val whiteLightBishopExists = board.getBitboard(BITBOARD_WB) and Bitboards.LIGHT_SQUARES !== 0
    val whiteDarkBishopExists = board.getBitboard(BITBOARD_WB) and Bitboards.DARK_SQUARES !== 0
    val blackLightBishopExists = board.getBitboard(BITBOARD_BB) and Bitboards.LIGHT_SQUARES !== 0
    val blackDarkBishopExists = board.getBitboard(BITBOARD_BB) and Bitboards.DARK_SQUARES !== 0
    val whiteBishopColourCount = (if (whiteLightBishopExists) 1 else 0) + if (whiteDarkBishopExists) 1 else 0
    val blackBishopColourCount = (if (blackLightBishopExists) 1 else 0) + if (blackDarkBishopExists) 1 else 0
    var bishopScore = 0
    bitboard = board.getBitboard(BITBOARD_WB)
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval += Bitboards.pieceSquareTableBishop.get(sq)
        val allAttacks: Long = Bitboards.magicBitboards.magicMovesBishop.get(sq).get(
            ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskBishop.get(sq)) * MagicBitboards.magicNumberBishop.get(
                sq
            ) ushr MagicBitboards.magicNumberShiftsBishop.get(sq)) as Int
        )
        whiteAttacksBitboard = whiteAttacksBitboard or allAttacks
        blackKingAttackedCount += java.lang.Long.bitCount(allAttacks and blackKingDangerZone)
        bishopScore += RivalConstants.VALUE_BISHOP_MOBILITY.get(java.lang.Long.bitCount(allAttacks and whitePieces.inv()))
    }
    if (whiteBishopColourCount == 2) bishopScore += RivalConstants.VALUE_BISHOP_PAIR + (8 - board.whitePawnValues / RivalConstants.VALUE_PAWN) * RivalConstants.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS
    bitboard = board.getBitboard(BITBOARD_BB)
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (!RivalConstants.TRACK_PIECE_SQUARE_VALUES) eval -= Bitboards.pieceSquareTableBishop.get(
            Bitboards.bitFlippedHorizontalAxis.get(
                sq
            )
        )
        val allAttacks: Long = Bitboards.magicBitboards.magicMovesBishop.get(sq).get(
            ((board.getBitboard(BITBOARD_ALL) and MagicBitboards.occupancyMaskBishop.get(sq)) * MagicBitboards.magicNumberBishop.get(
                sq
            ) ushr MagicBitboards.magicNumberShiftsBishop.get(sq)) as Int
        )
        blackAttacksBitboard = blackAttacksBitboard or allAttacks
        whiteKingAttackedCount += java.lang.Long.bitCount(allAttacks and whiteKingDangerZone)
        bishopScore -= RivalConstants.VALUE_BISHOP_MOBILITY.get(java.lang.Long.bitCount(allAttacks and blackPieces.inv()))
    }
    if (blackBishopColourCount == 2) bishopScore -= RivalConstants.VALUE_BISHOP_PAIR + (8 - board.blackPawnValues / RivalConstants.VALUE_PAWN) * RivalConstants.VALUE_BISHOP_PAIR_FEWER_PAWNS_BONUS
    if (whiteBishopColourCount == 1 && blackBishopColourCount == 1 && whiteLightBishopExists != blackLightBishopExists && board.whitePieceValues === board.blackPieceValues) {
        // as material becomes less, penalise the winning side for having a single bishop of the opposite colour to the opponent's single bishop
        val maxPenalty: Int =
            (eval + bishopScore) / BITBOARD_WRONG_COLOUR_BISHOP_PENALTY_DIVISOR // mostly pawns as material is identical

        // if score is positive (white winning) then the score will be reduced, if black winning, it will be increased
        bishopScore -= linearScale(
            board.whitePieceValues + board.blackPieceValues,
            BITBOARD_WRONG_COLOUR_BISHOP_MATERIAL_LOW,
            BITBOARD_WRONG_COLOUR_BISHOP_MATERIAL_HIGH,
            maxPenalty,
            0
        )
    }
    if (board.getBitboard(BITBOARD_WB) or board.getBitboard(BITBOARD_BB) and Bitboards.A2A7H2H7 !== 0) {
        if (board.getBitboard(BITBOARD_WB) and (1L shl Bitboards.A7) !== 0 && board.getBitboard(
                BITBOARD_BP
            ) and (1L shl Bitboards.B6) !== 0 && board.getBitboard(BITBOARD_BP) and (1L shl Bitboards.C7) !== 0
        ) bishopScore -= RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY
        if (board.getBitboard(BITBOARD_WB) and (1L shl Bitboards.H7) !== 0 && board.getBitboard(
                BITBOARD_BP
            ) and (1L shl Bitboards.G6) !== 0 && board.getBitboard(BITBOARD_BP) and (1L shl Bitboards.F7) !== 0
        ) bishopScore -= if (board.getBitboard(BITBOARD_WQ) === 0) RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY else RivalConstants.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
        if (board.getBitboard(BITBOARD_BB) and (1L shl Bitboards.A2) !== 0 && board.getBitboard(
                BITBOARD_WP
            ) and (1L shl Bitboards.B3) !== 0 && board.getBitboard(BITBOARD_WP) and (1L shl Bitboards.C2) !== 0
        ) bishopScore += RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY
        if (board.getBitboard(BITBOARD_BB) and (1L shl Bitboards.H2) !== 0 && board.getBitboard(
                BITBOARD_WP
            ) and (1L shl Bitboards.G3) !== 0 && board.getBitboard(BITBOARD_WP) and (1L shl Bitboards.F2) !== 0
        ) bishopScore += if (board.getBitboard(BITBOARD_BQ) === 0) RivalConstants.VALUE_TRAPPED_BISHOP_PENALTY else RivalConstants.VALUE_TRAPPED_BISHOP_KINGSIDE_WITH_QUEEN_PENALTY
    }
    eval += bishopScore

    // Everything white attacks with pieces.  Does not include attacked pawns.
    whiteAttacksBitboard =
        whiteAttacksBitboard and (board.getBitboard(BITBOARD_BN) or board.getBitboard(
            BITBOARD_BR
        ) or board.getBitboard(BITBOARD_BQ) or board.getBitboard(BITBOARD_BB))
    // Plus anything white attacks with pawns.
    whiteAttacksBitboard =
        whiteAttacksBitboard or (board.getBitboard(BITBOARD_WP) and FILE_A.inv() shl 9 or (board.getBitboard(
            BITBOARD_WP
        ) and FILE_H.inv() shl 7))
    var temp = 0
    bitboard = whiteAttacksBitboard and blackPieces and board.getBitboard(BITBOARD_BK).inv()
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (board.squareContents.get(sq) === BITBOARD_BP) temp += RivalConstants.VALUE_PAWN else if (board.squareContents.get(
                sq
            ) === BITBOARD_BN
        ) temp += RivalConstants.VALUE_KNIGHT else if (board.squareContents.get(sq) === BITBOARD_BR) temp += RivalConstants.VALUE_ROOK else if (board.squareContents.get(
                sq
            ) === BITBOARD_BQ
        ) temp += RivalConstants.VALUE_QUEEN else if (board.squareContents.get(sq) === BITBOARD_BB) temp += RivalConstants.VALUE_BISHOP
    }
    var threatScore: Int = temp + temp * (temp / RivalConstants.VALUE_QUEEN)
    blackAttacksBitboard =
        blackAttacksBitboard and (board.getBitboard(BITBOARD_WN) or board.getBitboard(
            BITBOARD_WR
        ) or board.getBitboard(BITBOARD_WQ) or board.getBitboard(BITBOARD_WB))
    blackAttacksBitboard =
        blackAttacksBitboard or (board.getBitboard(BITBOARD_BP) and FILE_A.inv() ushr 7 or (board.getBitboard(
            BITBOARD_BP
        ) and FILE_H.inv() ushr 9))
    temp = 0
    bitboard = blackAttacksBitboard and whitePieces and board.getBitboard(BITBOARD_WK).inv()
    while (bitboard != 0L) {
        bitboard = bitboard xor (1L shl java.lang.Long.numberOfTrailingZeros(bitboard).also { sq = it })
        if (board.squareContents.get(sq) === BITBOARD_WP) temp += RivalConstants.VALUE_PAWN else if (board.squareContents.get(
                sq
            ) === BITBOARD_WN
        ) temp += RivalConstants.VALUE_KNIGHT else if (board.squareContents.get(sq) === BITBOARD_WR) temp += RivalConstants.VALUE_ROOK else if (board.squareContents.get(
                sq
            ) === BITBOARD_WQ
        ) temp += RivalConstants.VALUE_QUEEN else if (board.squareContents.get(sq) === BITBOARD_WB) temp += RivalConstants.VALUE_BISHOP
    }
    threatScore -= temp + temp * (temp / RivalConstants.VALUE_QUEEN)
    threatScore /= RivalConstants.THREAT_SCORE_DIVISOR
    eval += threatScore
    val averagePiecesPerSide: Int = (board.whitePieceValues + board.blackPieceValues) / 2
    var whiteKingSafety = 0
    var blackKingSafety = 0
    var kingSafety = 0
    if (averagePiecesPerSide > RivalConstants.KINGSAFETY_MIN_PIECE_BALANCE) {
        var h1 = 0
        var h2 = 8
        var h3 = 16
        var g1 = 1
        var g2 = 9
        var g3 = 17
        var f1 = 2
        var f2 = 10
        var f3 = 18
        var f4 = 26
        if (board.whiteKingSquareCalculated === 1 || board.whiteKingSquareCalculated === 8) {
            whiteKingSafety =
                scoreRightWayPositions(board, h1, h2, h3, g1, g2, g3, f1, f2, f3, f4, 0, RivalConstants.WHITE)
        }
        if (board.blackKingSquareCalculated === 57 || board.blackKingSquareCalculated === 48) {
            h1 = 56
            h2 = 48
            h3 = 40
            g1 = 57
            g2 = 49
            g3 = 41
            f1 = 58
            f2 = 50
            f3 = 42
            f4 = 34
            blackKingSafety =
                scoreRightWayPositions(board, h1, h2, h3, g1, g2, g3, f1, f2, f3, f4, 6, RivalConstants.BLACK)
        }
        var halfOpenFilePenalty = 0
        var shieldValue = 0
        if (board.whiteKingSquareCalculated / 8 < 2) {
            val kingShield: Long = Bitboards.whiteKingShieldMask.get(board.whiteKingSquareCalculated % 8)
            shieldValue += (RivalConstants.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_WP
                ) and kingShield
            )
                    - RivalConstants.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_BP
                ) and (kingShield or (kingShield shl 8))
            )
                    + RivalConstants.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_WP
                ) and (kingShield shl 8)
            )
                    - RivalConstants.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_BP
                ) and (kingShield shl 16)
            ))
            shieldValue = Math.min(shieldValue, RivalConstants.KINGSAFTEY_MAXIMUM_SHIELD_BONUS)
            if (board.getBitboard(BITBOARD_WK) and Bitboards.F1G1 !== 0 &&
                board.getBitboard(BITBOARD_WR) and Bitboards.G1H1 !== 0 &&
                board.getBitboard(BITBOARD_WP) and FILE_G !== 0 &&
                board.getBitboard(BITBOARD_WP) and FILE_H !== 0
            ) {
                shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK
            } else if (board.getBitboard(BITBOARD_WK) and Bitboards.B1C1 !== 0 &&
                board.getBitboard(BITBOARD_WR) and Bitboards.A1B1 !== 0 &&
                board.getBitboard(BITBOARD_WP) and FILE_A !== 0 &&
                board.getBitboard(BITBOARD_WP) and FILE_B !== 0
            ) {
                shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK
            }
            val whiteOpen: Long =
                Bitboards.southFill(kingShield) and Bitboards.southFill(board.getBitboard(BITBOARD_WP))
                    .inv() and Bitboards.RANK_1
            if (whiteOpen != 0L) {
                halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * java.lang.Long.bitCount(whiteOpen and Bitboards.MIDDLE_FILES_8_BIT)
                halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * java.lang.Long.bitCount(whiteOpen and Bitboards.NONMID_FILES_8_BIT)
            }
            val blackOpen: Long =
                Bitboards.southFill(kingShield) and Bitboards.southFill(board.getBitboard(BITBOARD_BP))
                    .inv() and Bitboards.RANK_1
            if (blackOpen != 0L) {
                halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * java.lang.Long.bitCount(blackOpen and Bitboards.MIDDLE_FILES_8_BIT)
                halfOpenFilePenalty += RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * java.lang.Long.bitCount(blackOpen and Bitboards.NONMID_FILES_8_BIT)
            }
        }
        whiteKingSafety += RivalConstants.KINGSAFETY_SHIELD_BASE + shieldValue - halfOpenFilePenalty
        shieldValue = 0
        halfOpenFilePenalty = 0
        if (board.blackKingSquareCalculated / 8 >= 6) {
            val kingShield: Long = Bitboards.whiteKingShieldMask.get(board.blackKingSquareCalculated % 8) shl 40
            shieldValue += (RivalConstants.KINGSAFTEY_IMMEDIATE_PAWN_SHIELD_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_BP
                ) and kingShield
            )
                    - RivalConstants.KINGSAFTEY_ENEMY_PAWN_IN_VICINITY_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_WP
                ) and (kingShield or (kingShield ushr 8))
            )
                    + RivalConstants.KINGSAFTEY_LESSER_PAWN_SHIELD_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_BP
                ) and (kingShield ushr 8)
            )
                    - RivalConstants.KINGSAFTEY_CLOSING_ENEMY_PAWN_UNIT * java.lang.Long.bitCount(
                board.getBitboard(
                    BITBOARD_WP
                ) and (kingShield ushr 16)
            ))
            shieldValue = Math.min(shieldValue, RivalConstants.KINGSAFTEY_MAXIMUM_SHIELD_BONUS)
            if (board.getBitboard(BITBOARD_BK) and Bitboards.F8G8 !== 0 &&
                board.getBitboard(BITBOARD_BR) and Bitboards.G8H8 !== 0 &&
                board.getBitboard(BITBOARD_BP) and FILE_G !== 0 &&
                board.getBitboard(BITBOARD_BP) and FILE_H !== 0
            ) {
                shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK
            } else if (board.getBitboard(BITBOARD_BK) and Bitboards.B8C8 !== 0 &&
                board.getBitboard(BITBOARD_BR) and Bitboards.A8B8 !== 0 &&
                board.getBitboard(BITBOARD_BP) and FILE_A !== 0 &&
                board.getBitboard(BITBOARD_BP) and FILE_B !== 0
            ) {
                shieldValue -= RivalConstants.KINGSAFETY_UNCASTLED_TRAPPED_ROOK
            }
            val whiteOpen: Long =
                Bitboards.southFill(kingShield) and Bitboards.southFill(board.getBitboard(BITBOARD_WP))
                    .inv() and Bitboards.RANK_1
            if (whiteOpen != 0L) {
                halfOpenFilePenalty += (RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * java.lang.Long.bitCount(whiteOpen and Bitboards.MIDDLE_FILES_8_BIT)
                        + RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * java.lang.Long.bitCount(whiteOpen and Bitboards.NONMID_FILES_8_BIT))
            }
            val blackOpen: Long =
                Bitboards.southFill(kingShield) and Bitboards.southFill(board.getBitboard(BITBOARD_BP))
                    .inv() and Bitboards.RANK_1
            if (blackOpen != 0L) {
                halfOpenFilePenalty += (RivalConstants.KINGSAFTEY_HALFOPEN_MIDFILE * java.lang.Long.bitCount(blackOpen and Bitboards.MIDDLE_FILES_8_BIT)
                        + RivalConstants.KINGSAFTEY_HALFOPEN_NONMIDFILE * java.lang.Long.bitCount(blackOpen and Bitboards.NONMID_FILES_8_BIT))
            }
        }
        blackKingSafety += RivalConstants.KINGSAFETY_SHIELD_BASE + shieldValue - halfOpenFilePenalty
        kingSafety = linearScale(
            averagePiecesPerSide,
            RivalConstants.KINGSAFETY_MIN_PIECE_BALANCE,
            RivalConstants.KINGSAFETY_MAX_PIECE_BALANCE,
            0,
            whiteKingSafety - blackKingSafety + (blackKingAttackedCount - whiteKingAttackedCount) * RivalConstants.KINGSAFETY_ATTACK_MULTIPLIER
        )
    }
    eval += kingSafety
    if (board.whitePieceValues + board.whitePawnValues + board.blackPieceValues + board.blackPawnValues <= RivalConstants.EVAL_ENDGAME_TOTAL_PIECES) {
        eval = endGameAdjustmentRival103(board, eval)
    }
    eval = if (board.mover == Colour.WHITE) eval else -eval
    return eval
}

fun endGameAdjustmentRival103(board: EngineBoard, currentScore: Int): Int {
    var eval = currentScore
    if (com.netsensia.rivalchess.engine.core.RivalSearch.rivalKPKBitbase != null && board.whitePieceValues + board.blackPieceValues === 0 && board.whitePawnValues + board.blackPawnValues === RivalConstants.VALUE_PAWN) {
        return if (board.whitePawnValues === RivalConstants.VALUE_PAWN) {
            kpkLookup(
                board.whiteKingSquareCalculated,
                board.blackKingSquareCalculated,
                java.lang.Long.numberOfTrailingZeros(board.getBitboard(BITBOARD_WP)),
                board.mover == Colour.WHITE
            )
        } else {
            // flip the position so that the black pawn becomes white, and negate the result
            -kpkLookup(
                Bitboards.bitFlippedHorizontalAxis.get(board.blackKingSquareCalculated),
                Bitboards.bitFlippedHorizontalAxis.get(board.whiteKingSquareCalculated),
                Bitboards.bitFlippedHorizontalAxis.get(
                    java.lang.Long.numberOfTrailingZeros(
                        board.getBitboard(
                            BITBOARD_BP
                        )
                    )
                ),
                !board.mover == Colour.WHITE
            )
        }
    }
    if (board.whitePawnValues + board.blackPawnValues === 0 && board.whitePieceValues < RivalConstants.VALUE_ROOK && board.blackPieceValues < RivalConstants.VALUE_ROOK) return eval / RivalConstants.ENDGAME_DRAW_DIVISOR
    if (eval > 0) {
        if (board.whitePawnValues === 0 && (board.whitePieceValues === RivalConstants.VALUE_KNIGHT || board.whitePieceValues === RivalConstants.VALUE_BISHOP)) return eval - (board.whitePieceValues * RivalConstants.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER) as Int else if (board.whitePawnValues === 0 && board.whitePieceValues - RivalConstants.VALUE_BISHOP <= board.blackPieceValues) return eval / RivalConstants.ENDGAME_PROBABLE_DRAW_DIVISOR else if (java.lang.Long.bitCount(
                board.getBitboard(BITBOARD_ALL)
            ) > 3 && board.getBitboard(BITBOARD_WR) or board.getBitboard(BITBOARD_WN) or board.getBitboard(
                BITBOARD_WQ
            ) === 0
        ) {
            // if this is not yet a KPK ending, and if white has only A pawns and has no dark bishop and the black king is on a8/a7/b8/b7 then this is probably a draw
            if (board.getBitboard(BITBOARD_WP) and FILE_A.inv() === 0 &&
                board.getBitboard(BITBOARD_WB) and Bitboards.LIGHT_SQUARES === 0 &&
                board.getBitboard(BITBOARD_BK) and Bitboards.A8A7B8B7 !== 0
            ) return eval / RivalConstants.ENDGAME_DRAW_DIVISOR else if (board.getBitboard(BITBOARD_WP) and FILE_H.inv() === 0 &&
                board.getBitboard(BITBOARD_WB) and Bitboards.DARK_SQUARES === 0 &&
                board.getBitboard(BITBOARD_BK) and Bitboards.H8H7G8G7 !== 0
            ) return eval / RivalConstants.ENDGAME_DRAW_DIVISOR
        }
        if (board.blackPawnValues === 0) {
            if (board.whitePieceValues - board.blackPieceValues > RivalConstants.VALUE_BISHOP) {
                val whiteKnightCount = java.lang.Long.bitCount(board.getBitboard(BITBOARD_WN))
                val whiteBishopCount = java.lang.Long.bitCount(board.getBitboard(BITBOARD_WB))
                return if (whiteKnightCount == 2 && board.whitePieceValues === 2 * RivalConstants.VALUE_KNIGHT && board.blackPieceValues === 0) eval / RivalConstants.ENDGAME_DRAW_DIVISOR else if (whiteKnightCount == 1 && whiteBishopCount == 1 && board.whitePieceValues === RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP && board.blackPieceValues === 0) {
                    eval =
                        RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP + RivalConstants.VALUE_SHOULD_WIN + eval / RivalConstants.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR
                    val kingSquare: Int = board.blackKingSquareCalculated
                    if (board.getBitboard(BITBOARD_WB) and Bitboards.DARK_SQUARES !== 0) eval += (7 - Bitboards.distanceToH1OrA8.get(
                        Bitboards.bitFlippedHorizontalAxis.get(kingSquare)
                    )) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE else eval += (7 - Bitboards.distanceToH1OrA8.get(
                        kingSquare
                    )) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE
                    eval
                } else eval + RivalConstants.VALUE_SHOULD_WIN
            }
        }
    }
    if (eval < 0) {
        if (board.blackPawnValues === 0 && (board.blackPieceValues === RivalConstants.VALUE_KNIGHT || board.blackPieceValues === RivalConstants.VALUE_BISHOP)) return eval + (board.blackPieceValues * RivalConstants.ENDGAME_SUBTRACT_INSUFFICIENT_MATERIAL_MULTIPLIER) as Int else if (board.blackPawnValues === 0 && board.blackPieceValues - RivalConstants.VALUE_BISHOP <= board.whitePieceValues) return eval / RivalConstants.ENDGAME_PROBABLE_DRAW_DIVISOR else if (java.lang.Long.bitCount(
                board.getBitboard(BITBOARD_ALL)
            ) > 3 && board.getBitboard(BITBOARD_BR) or board.getBitboard(BITBOARD_BN) or board.getBitboard(
                BITBOARD_BQ
            ) === 0
        ) {
            if (board.getBitboard(BITBOARD_BP) and FILE_A.inv() === 0 &&
                board.getBitboard(BITBOARD_BB) and Bitboards.DARK_SQUARES === 0 &&
                board.getBitboard(BITBOARD_WK) and Bitboards.A1A2B1B2 !== 0
            ) return eval / RivalConstants.ENDGAME_DRAW_DIVISOR else if (board.getBitboard(BITBOARD_BP) and FILE_H.inv() === 0 &&
                board.getBitboard(BITBOARD_BB) and Bitboards.LIGHT_SQUARES === 0 &&
                board.getBitboard(BITBOARD_WK) and Bitboards.H1H2G1G2 !== 0
            ) return eval / RivalConstants.ENDGAME_DRAW_DIVISOR
        }
        if (board.whitePawnValues === 0) {
            if (board.blackPieceValues - board.whitePieceValues > RivalConstants.VALUE_BISHOP) {
                val blackKnightCount = java.lang.Long.bitCount(board.getBitboard(BITBOARD_BN))
                val blackBishopCount = java.lang.Long.bitCount(board.getBitboard(BITBOARD_BB))
                return if (blackKnightCount == 2 && board.blackPieceValues === 2 * RivalConstants.VALUE_KNIGHT && board.whitePieceValues === 0) eval / RivalConstants.ENDGAME_DRAW_DIVISOR else if (blackKnightCount == 1 && blackBishopCount == 1 && board.blackPieceValues === RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP && board.whitePieceValues === 0) {
                    eval =
                        -(RivalConstants.VALUE_KNIGHT + RivalConstants.VALUE_BISHOP + RivalConstants.VALUE_SHOULD_WIN) + eval / RivalConstants.ENDGAME_KNIGHT_BISHOP_SCORE_DIVISOR
                    val kingSquare: Int = board.whiteKingSquareCalculated
                    if (board.getBitboard(BITBOARD_BB) and Bitboards.DARK_SQUARES !== 0) {
                        eval -= (7 - Bitboards.distanceToH1OrA8.get(Bitboards.bitFlippedHorizontalAxis.get(kingSquare))) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE
                    } else {
                        eval -= (7 - Bitboards.distanceToH1OrA8.get(kingSquare)) * RivalConstants.ENDGAME_DISTANCE_FROM_MATING_BISHOP_CORNER_PER_SQUARE
                    }
                    eval
                } else eval - RivalConstants.VALUE_SHOULD_WIN
            }
        }
    }
    return eval
}