package com.example.pixelgame;

import java.util.ArrayList;
import java.util.List;

/** Attempt statistics include failed checkpoint attempts; menus and pauses do not advance time. */
final class RunStats {
    float seconds;
    int damageTaken, potionsUsed, retries, startStage = 1;
    int attacks, skills, dashes;
    private int currentStage = 1;
    private static final class Region {
        float seconds;
        int damage, potions, retries, attacks, skills, dashes;
        boolean cleared;
    }
    private final Region[] regions = {new Region(), new Region(), new Region(), new Region()};
    private Region current() { return regions[currentStage - 1]; }

    void reset(int stage) {
        seconds = 0; damageTaken = potionsUsed = retries = 0; startStage = stage;
        currentStage = stage; attacks = skills = dashes = 0;
        for (int i = 0; i < regions.length; i++) regions[i] = new Region();
    }

    void advance(float delta) {
        if (Float.isFinite(delta) && delta > 0) { seconds += delta; current().seconds += delta; }
    }

    void enterStage(int stage) {
        if (stage != currentStage) current().cleared = true;
        currentStage = stage;
    }
    void clearRegion() { current().cleared = true; }
    void damage(int amount) { damageTaken += amount; current().damage += amount; }
    void potion() { potionsUsed++; current().potions++; }
    void retry() { retries++; current().retries++; }
    void actions(int normal, int skill, int dash) {
        attacks += normal; skills += skill; dashes += dash;
        current().attacks += normal; current().skills += skill; current().dashes += dash;
    }
    List<RegionResult> snapshot(int reachedStage) {
        List<RegionResult> result = new ArrayList<>();
        for (int stage = startStage; stage <= reachedStage; stage++) {
            Region r = regions[stage - 1];
            result.add(new RegionResult(stage, r.seconds, r.damage, r.potions, r.retries,
                    r.attacks, r.skills, r.dashes, r.cleared));
        }
        return List.copyOf(result);
    }

    RunStats copy() {
        RunStats other = new RunStats();
        other.seconds = seconds; other.damageTaken = damageTaken;
        other.potionsUsed = potionsUsed; other.retries = retries; other.startStage = startStage;
        other.currentStage = currentStage; other.attacks = attacks; other.skills = skills; other.dashes = dashes;
        for (int i = 0; i < regions.length; i++) {
            Region from = regions[i], to = other.regions[i];
            to.seconds = from.seconds; to.damage = from.damage; to.potions = from.potions;
            to.retries = from.retries; to.attacks = from.attacks; to.skills = from.skills;
            to.dashes = from.dashes; to.cleared = from.cleared;
        }
        return other;
    }
}
