package com.example.pixelgame;

import java.util.List;

/** Immutable snapshot, captured before the live run is reset. */
record RunResult(boolean cleared, int stage, int defeated, int goldEarned, int goldRemaining,
                 float seconds, int damageTaken, int potionsUsed, int retries, int startStage,
                 boolean newBest, float bestSeconds, int clearCount,
                 List<RegionResult> regions, List<StageReward> rewards, int attackLevels, int healthLevels,
                 int bonusHealth, int attacks, int skills, int dashes) {
    RunResult(boolean cleared, int stage, int defeated, int goldEarned, int goldRemaining,
              float seconds, int damageTaken, int potionsUsed, int retries, int startStage,
              boolean newBest, float bestSeconds, int clearCount) {
        this(cleared, stage, defeated, goldEarned, goldRemaining, seconds, damageTaken, potionsUsed,
                retries, startStage, newBest, bestSeconds, clearCount, List.of(), List.of(), 0, 0, 0, 0, 0, 0);
    }
    RunResult(boolean cleared, int stage, int defeated, int goldEarned, int goldRemaining) {
        this(cleared, stage, defeated, goldEarned, goldRemaining, 0, 0, 0, 0, 1, false, 0, 0);
    }
    RunResult {
        regions = List.copyOf(regions); rewards = List.copyOf(rewards);
        if (stage < 1 || stage > 4 || defeated < 0 || goldEarned < 0 || goldRemaining < 0
                || !Float.isFinite(seconds) || seconds < 0 || damageTaken < 0 || potionsUsed < 0
                || retries < 0 || startStage < 1 || startStage > 4 || clearCount < 0
                || attackLevels < 0 || healthLevels < 0 || bonusHealth < 0 || attacks < 0 || skills < 0 || dashes < 0) {
            throw new IllegalArgumentException("Invalid run result");
        }
    }
}
