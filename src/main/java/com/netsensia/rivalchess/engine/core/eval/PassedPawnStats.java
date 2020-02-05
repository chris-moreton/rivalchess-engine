package com.netsensia.rivalchess.engine.core.eval;

import com.netsensia.rivalchess.engine.core.RivalConstants;

public class PassedPawnStats {
    int whitePassedPawnScore = RivalConstants.PAWNHASH_DEFAULT_SCORE;
    int blackPassedPawnScore = RivalConstants.PAWNHASH_DEFAULT_SCORE;
    long whitePassedPawnsBitboard = 0;
    long blackPassedPawnsBitboard = 0;

    public int getWhitePassedPawnScore() {
        return whitePassedPawnScore;
    }

    public void setWhitePassedPawnScore(int whitePassedPawnScore) {
        this.whitePassedPawnScore = whitePassedPawnScore;
    }

    public int getBlackPassedPawnScore() {
        return blackPassedPawnScore;
    }

    public void addBlackPassedPawnScore(int score) {
        blackPassedPawnScore += score;
    }

    public void addWhitePassedPawnScore(int score) {
        whitePassedPawnScore += score;
    }

    public void setBlackPassedPawnScore(int blackPassedPawnScore) {
        this.blackPassedPawnScore = blackPassedPawnScore;
    }

    public long getWhitePassedPawnsBitboard() {
        return whitePassedPawnsBitboard;
    }

    public void setWhitePassedPawnsBitboard(long whitePassedPawnsBitboard) {
        this.whitePassedPawnsBitboard = whitePassedPawnsBitboard;
    }

    public long getBlackPassedPawnsBitboard() {
        return blackPassedPawnsBitboard;
    }

    public void setBlackPassedPawnsBitboard(long blackPassedPawnsBitboard) {
        this.blackPassedPawnsBitboard = blackPassedPawnsBitboard;
    }
}
