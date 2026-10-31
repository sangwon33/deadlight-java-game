package com.example.pixelgame;

import com.badlogic.gdx.Preferences;

/** Local records are separated by starting region so practice cannot overwrite campaign records. */
final class ClearRecords {
    record Summary(float bestSeconds, int clears, int leastDamage) {
        boolean exists() { return clears > 0; }
    }
    private final Preferences prefs;
    ClearRecords(Preferences prefs) { this.prefs = prefs; }
    private String key(int startStage) { return startStage == 1 ? "campaign" : "practice." + startStage; }
    Summary read(int startStage) {
        String key = key(startStage);
        return new Summary(prefs.getFloat(key + ".bestSeconds", 0),
                prefs.getInteger(key + ".clears", 0), prefs.getInteger(key + ".leastDamage", 0));
    }
    boolean saveClear(int startStage, float seconds, int damage) {
        if (startStage < 1 || startStage > 4 || !Float.isFinite(seconds) || seconds < 0 || damage < 0)
            throw new IllegalArgumentException("Invalid clear record");
        Summary old = read(startStage);
        boolean best = !old.exists() || seconds < old.bestSeconds();
        String key = key(startStage);
        prefs.putFloat(key + ".bestSeconds", best ? seconds : old.bestSeconds());
        prefs.putInteger(key + ".clears", old.clears() + 1);
        prefs.putInteger(key + ".leastDamage", old.exists() ? Math.min(old.leastDamage(), damage) : damage);
        prefs.flush();
        return best;
    }
}
