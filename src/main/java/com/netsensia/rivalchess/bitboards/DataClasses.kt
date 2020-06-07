package com.netsensia.rivalchess.bitboards

data class MagicVars(val moves: Array<LongArray>, val mask: LongArray, val number: LongArray, val shift: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MagicVars

        if (!moves.contentEquals(other.moves)) return false
        if (!mask.contentEquals(other.mask)) return false
        if (!number.contentEquals(other.number)) return false
        if (!shift.contentEquals(other.shift)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = moves.contentHashCode()
        result = 31 * result + mask.contentHashCode()
        result = 31 * result + number.contentHashCode()
        result = 31 * result + shift.contentHashCode()
        return result
    }
}