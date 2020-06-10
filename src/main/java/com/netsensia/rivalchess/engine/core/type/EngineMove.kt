package com.netsensia.rivalchess.engine.core.type

import com.netsensia.rivalchess.enums.PromotionPieceMask
import com.netsensia.rivalchess.enums.PromotionPieceMask.Companion.fromPiece
import com.netsensia.rivalchess.model.Move
import com.netsensia.rivalchess.model.SquareOccupant
import com.netsensia.rivalchess.util.getBitRefFromBoardRef

class EngineMove {
    @JvmField
    val compact: Int

    constructor(compact: Int) {
        this.compact = compact
    }

    fun from() = compact shr 16 and 63
    fun to() = compact and 63
    fun promotionPieceMask() = compact and PromotionPieceMask.PROMOTION_PIECE_TOSQUARE_MASK_FULL.value

    constructor(move: Move) {
        val from = getBitRefFromBoardRef(move.srcBoardRef)
        val to = getBitRefFromBoardRef(move.tgtBoardRef)
        val promotionPiece = move.promotedPiece
        val promotionPart = if (promotionPiece == SquareOccupant.NONE) 0 else fromPiece(promotionPiece.piece).value
        compact = to + (from shl 16) + (promotionPart shl 32)
    }

    override fun equals(other: Any?): Boolean {
        other as EngineMove
        return this.compact == other.compact
    }
}