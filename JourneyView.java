package com.example.pixelgame;

import java.util.Locale;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

final class JourneyView {
    private static final String[] NAMES = {"VERDANT RUINS", "CASTLE RAMPARTS", "ASHEN BATTLEFIELD", "GODDESS CHURCH"};
    private final UiTheme ui;
    static String regionName(int stage) { return NAMES[stage - 1]; }
    JourneyView(UiTheme ui) { this.ui = ui; }
    static String time(float seconds) {
        long tenths = Math.max(0, Math.round(seconds * 10));
        return String.format(Locale.ROOT, "%02d:%02d.%d", tenths / 600, (tenths / 10) % 60, tenths % 10);
    }
    static Rectangle skipBounds(float left) { return new Rectangle(left + 177, 252, 35, 16); }

    void progress(SpriteBatch b, BitmapFont f, float left, int stage, int defeated, int total,
                  float position, float worldWidth, FallenGod boss, RunStats stats) {
        ui.panel(b, left + 236, 316, 244, 32);
        ui.text(b, f, "0" + stage + "  " + NAMES[stage - 1], left + 246, 339, .72f, UiTheme.TEXT, 174);
        ui.text(b, f, time(stats.seconds), left + 425, 338, .62f, UiTheme.MUTED, 45);
        float progress = stage == 4 && boss != null ? 1f - (float)boss.getHealth() / boss.getMaxHealth()
                : MathUtils.clamp((position - 100) / (worldWidth - 200), 0, 1);
        String goal = stage == 4 ? "DEFEAT THE FALLEN GOD"
                : defeated + "/" + total + " DEFEATED    EXIT > " + Math.round(progress * 100) + "%";
        ui.text(b, f, goal, left + 246, 327, .58f, UiTheme.MUTED, 224);
        ui.rect(b, left + 246, 319, 224, 2, UiTheme.BORDER, 1);
        ui.rect(b, left + 246, 319, 224 * progress, 2, UiTheme.GOLD, 1);
    }

    void tutorial(SpriteBatch b, BitmapFont f, float left, TutorialGuide guide, boolean hovered, boolean pressed) {
        if (!guide.visible()) return;
        boolean detail = !guide.detail().isEmpty();
        ui.panel(b, left + 12, detail ? 216 : 230, 212, detail ? 64 : 50);
        ui.text(b, f, guide.title(), left + 22, 267, .6f, UiTheme.GOLD, 146);
        ui.text(b, f, guide.instruction(), left + 22, 250, .6f, UiTheme.TEXT, 192);
        ui.text(b, f, guide.detail(), left + 22, 237, .58f, UiTheme.MUTED, 192);
        for (int i = 0; i < TutorialGuide.LESSON_COUNT; i++) ui.rect(b, left + 22 + i * 39, detail ? 223 : 238, 33, 2,
                i < guide.step() ? UiTheme.SUCCESS : UiTheme.BORDER, 1);
        if (!guide.finished()) ui.button(b, f, skipBounds(left), "SKIP", true, hovered, pressed, .48f);
    }

    void exit(SpriteBatch b, BitmapFont f, float x, float ground) {
        ui.rect(b, x, ground, 2, 54, UiTheme.GOLD, .8f);
        ui.panel(b, x - 18, ground + 54, 74, 22);
        ui.centered(b, f, "EXIT >", new Rectangle(x - 18, ground + 54, 74, 22), .85f, UiTheme.GOLD);
    }

    void records(SpriteBatch b, BitmapFont f, float left, ClearRecords.Summary record) {
        ui.panel(b, left + 338, 16, 282, 70);
        ui.text(b, f, "CAMPAIGN RECORD", left + 350, 73, .65f, UiTheme.GOLD, 258);
        ui.text(b, f, record.exists() ? "BEST " + time(record.bestSeconds()) + "   CLEARS " + record.clears()
                : "No campaign clears yet.", left + 350, 55, .76f, UiTheme.TEXT, 258);
        ui.text(b, f, record.exists() ? "LEAST DAMAGE  " + record.leastDamage() + " HP"
                : "Clear all four regions to set a record.", left + 350, 37, .6f, UiTheme.MUTED, 258);
    }
}
