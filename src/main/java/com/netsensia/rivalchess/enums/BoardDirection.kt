package com.netsensia.rivalchess.enums

enum class BoardDirection(val index: Int) {
    E(0), SE(1), S(2), SW(3), W(4), NW(5), N(6), NE(7);

    companion object {
        fun fromIndex(index: Int): BoardDirection {
            for (b in values()) {
                if (b.index == index) {
                    return b
                }
            }
            throw RuntimeException("Attempt was made to get invalid BoardDirection enum")
        }
    }

}