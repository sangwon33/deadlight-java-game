package com.example.pixelgame;

/** Shared combat/item values so the shop, HUD and restored runs describe the same rules. */
final class GameBalance {
    static final int BASE_HEALTH = 5, HEALTH_UPGRADE = 3;
    static final int POWER_BONUS = 2;
    static final float POWER_DURATION = 8f;
    static final float SKILL_COOLDOWN = 8f, SKILL_WINDUP = .30f, SKILL_STRIKE_END = .46f, SKILL_DURATION = .74f;
    static final int SKILL_BONUS = 2;
    static final int CLEAR_GOLD = 30, CLEAR_HEALTH = 1;
    static int healing(int maxHealth) { return Math.max(1, (maxHealth + 1) / 2); }
}
