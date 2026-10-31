package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Select, review, then commit one free reward before the next region. */
final class RewardView {
    private final UiTheme ui;
    RewardView(UiTheme ui) { this.ui = ui; }
    static Rectangle cardBounds(float left, int index) { return new Rectangle(left + 44 + index * 190, 115, 172, 145); }
    static Rectangle continueBounds(float left) { return new Rectangle(left + 170, 64, 300, 34); }
    static String title(int index) { return new String[]{"REST", "GOLD", "FORTIFY"}[index]; }
    void draw(SpriteBatch b, BitmapFont f, float left, int stage, Player p, int gold,
              int selected, int hovered, boolean continueHover, boolean pressed, Texture[] icons) {
        ui.rect(b, left, 0, 640, 360, Color.BLACK, .76f);
        ui.panel(b, left + 32, 42, 576, 286);
        ui.text(b, f, "REGION 0" + stage + " COMPLETE / " + JourneyView.regionName(stage), left + 52, 309, .85f, UiTheme.GOLD, 536);
        ui.text(b, f, "Choose one reward. Review your choice, then continue to region 0" + (stage + 1) + ".",
                left + 52, 284, .67f, UiTheme.TEXT, 536);
        boolean full = p.getHealth() == p.getMaxHealth();
        String[] effects = {full ? "+1 HEAL POTION" : "FULL RECOVERY", "+" + GameBalance.CLEAR_GOLD + " GOLD", "+1 MAX HP"};
        String[] previews = {full ? "HP FULL / potion saved for later" : "HP " + p.getHealth() + "/" + p.getMaxHealth() + " -> " + p.getMaxHealth() + "/" + p.getMaxHealth(),
                "GOLD " + gold + " -> " + (gold + GameBalance.CLEAR_GOLD),
                "HP " + p.getHealth() + "/" + p.getMaxHealth() + " -> " + (p.getHealth() + 1) + "/" + (p.getMaxHealth() + 1)};
        String[] details = {full ? "Use anytime with [1]." : "Start the next region healed.", "Spend at the next merchant.", "Current HP +1. This run only."};
        for (int i = 0; i < 3; i++) {
            Rectangle r = cardBounds(left, i);ui.panel(b, r.x, r.y, r.width, r.height);
            if (selected == i) {
                ui.rect(b, r.x, r.y, 3, r.height, UiTheme.GOLD, 1);
                ui.rect(b, r.x, r.y + r.height - 2, r.width, 2, UiTheme.GOLD, 1);
            } else if (hovered == i) ui.rect(b, r.x + 1, r.y + 1, r.width - 2, r.height - 2, UiTheme.GOLD, .08f);
            ui.text(b, f, "[" + (i + 1) + "]", r.x + 140, 246, .7f, UiTheme.MUTED, 24);
            if (icons[i] != null) {float color=b.getPackedColor();b.setColor(Color.WHITE);b.draw(icons[i],r.x+14,212,28,28);b.setPackedColor(color);}
            ui.text(b, f, title(i), r.x + 14, 198, .94f, UiTheme.TEXT, 144);
            ui.text(b, f, effects[i], r.x + 14, 178, .8f, UiTheme.GOLD, 144);
            ui.text(b, f, previews[i], r.x + 14, 160, .6f, UiTheme.TEXT, 144);
            ui.text(b, f, details[i], r.x + 14, 144, .56f, UiTheme.MUTED, 144);
            ui.text(b, f, selected == i ? "SELECTED" : "CLICK OR PRESS " + (i + 1), r.x + 14, 128, .56f,
                    selected == i ? UiTheme.GOLD : UiTheme.MUTED, 144);
        }
        ui.button(b, f, continueBounds(left), "TAKE " + title(selected) + " & CONTINUE [ENTER]", true, continueHover, pressed, .78f);
        ui.centered(b, f, "[1 / 2 / 3] CHOOSE     [ENTER] CONFIRM     [ESC] PAUSE",
                new Rectangle(left + 52, 44, 536, 14), .52f, UiTheme.MUTED);
    }
}
