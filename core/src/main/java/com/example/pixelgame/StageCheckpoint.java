package com.example.pixelgame;

import java.util.List;

/** Captured only on stage entry. Failed-attempt purchases, rewards and consumed items are rolled back. */
record StageCheckpoint(int stage, int gold, int attackLevels, int healthLevels,
                       int heals, int powers, int health, float buffSeconds, int defeated, int earned,
                       List<StageReward> rewards,int speeds,float hasteSeconds) {
    StageCheckpoint(int stage, int gold, int attackLevels, int healthLevels, int heals, int powers,
                    int health, float buffSeconds, int defeated, int earned) {
        this(stage, gold, attackLevels, healthLevels, heals, powers, health, buffSeconds, defeated, earned, List.of(),0,0);
    }
    StageCheckpoint(int stage,int gold,int attackLevels,int healthLevels,int heals,int powers,int health,float buffSeconds,int defeated,int earned,List<StageReward> rewards){
        this(stage,gold,attackLevels,healthLevels,heals,powers,health,buffSeconds,defeated,earned,rewards,0,0);
    }
    StageCheckpoint { rewards = List.copyOf(rewards); }
}
