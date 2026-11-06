package com.example.pixelgame;

/** Shared combat/item values so the shop, HUD and restored runs describe the same rules. */
final class GameBalance {
    static final float CHARACTER_SCALE = .8f;
    static final float PLAYER_MOVE_SPEED = 340f, PLAYER_DASH_SPEED = 960f;
    static float character(float value) { return value * CHARACTER_SCALE; }
    static final float JUMP_SPEED = 660f, GRAVITY = -2050f, COYOTE_TIME = .10f, INPUT_BUFFER = .12f;
    static final float JUMP_CUT = .48f, DASH_COOLDOWN = .42f;
    static final int BASE_ATTACK_DAMAGE = 6, SLIME_HEALTH = 18, KNIGHT_HEALTH = 36, DARK_HEALTH = 24, BOSS_HEALTH = 240;
    static final float COUNTER_MULTIPLIER = 1.5f;
    static final int GAMEPLAY_VERSION = 5;
    static final int BASE_HEALTH = 100, HEALTH_UPGRADE = 60;
    static final int ENEMY_DAMAGE = 20;
    static final int MOTH_HEALTH = 12, MOTH_DAMAGE = 15, MOTH_GOLD = 15;
    static final int MARKSMAN_HEALTH = 18, MARKSMAN_DAMAGE = 15, MARKSMAN_GOLD = 20;
    static final int POWER_BONUS = 2;
    static final float POWER_DURATION = 8f;
    static final float SKILL_COOLDOWN = 1f, SKILL_FULL_CHARGE = 1.2f;
    static final float SKILL_WINDUP = 0f, SKILL_STRIKE_END = .22f, SKILL_DURATION = .38f;
    static final float SKILL_RUSH_END = .22f, SKILL_RUSH_SPEED = 210f / SKILL_RUSH_END,
            SKILL_REACH = character(68f);
    static final int SKILL_DAMAGE = 20;
    static final String SKILL_CHARGE_LABEL = String.format(java.util.Locale.ROOT, "%.1f초", SKILL_FULL_CHARGE);
    static int chargeStage(float seconds) {
        return Math.min(4, 1 + (int)(Math.max(0, Math.min(1, seconds / SKILL_FULL_CHARGE)) * 4 + .0001f));
    }
    static final int CLEAR_GOLD = 30, CLEAR_HEALTH = 20;
    static int healing(int maxHealth) { return Math.max(1, (maxHealth + 1) / 2); }
}
