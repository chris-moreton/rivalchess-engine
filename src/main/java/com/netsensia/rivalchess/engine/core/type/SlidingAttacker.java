package com.netsensia.rivalchess.engine.core.type;

import com.netsensia.rivalchess.constants.BoardDirection;
import com.netsensia.rivalchess.constants.SquareOccupant;

public class SlidingAttacker {
    final BoardDirection direction;
    final SquareOccupant squareOccupant;

    public SlidingAttacker(BoardDirection direction, SquareOccupant squareOccupant) {
        this.direction = direction;
        this.squareOccupant = squareOccupant;
    }

    public BoardDirection getDirection() {
        return direction;
    }

    public SquareOccupant getSquareOccupant() {
        return squareOccupant;
    }
}
