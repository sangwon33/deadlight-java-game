package com.example.pixelgame;

/** A committed exit reward, carried into the next region's checkpoint. */
record StageReward(int stage, Kind kind, String detail) {
    enum Kind { REST, GOLD, FORTIFY }
    StageReward {
        if (stage < 1 || stage > 3 || kind == null || detail == null)
            throw new IllegalArgumentException("Invalid region reward");
    }
}
