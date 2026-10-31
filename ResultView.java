package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

final class ResultView implements Disposable {
    private final UiTheme ui = new UiTheme();
    static Rectangle buttonBounds(float left, int row) {
        return row == 0 ? new Rectangle(left + 154, 90, 332, 34)
                : new Rectangle(left + (row == 1 ? 154 : 326), 48, 160, 30);
    }
    static Rectangle tabBounds(float left, int page) { return new Rectangle(left + 118 + page * 204, 258, 200, 22); }
    private void metric(SpriteBatch b, BitmapFont f, float x, float y, String label, String value) {
        ui.rect(b, x, y, 126, 40, UiTheme.BORDER, .3f);
        ui.text(b, f, label, x + 10, y + 31, .54f, UiTheme.MUTED, 106);
        ui.text(b, f, value, x + 10, y + 16, .94f, UiTheme.TEXT, 106);
    }
    void draw(SpriteBatch b, BitmapFont f, float left, RunResult r, int hovered, boolean pressed) {
        draw(b, f, left, r, hovered, pressed, 0);
    }
    void draw(SpriteBatch b, BitmapFont f, float left, RunResult r, int hovered, boolean pressed, int page) {
        ui.rect(b, left, 0, 640, 360, Color.BLACK, .65f);
        ui.panel(b, left + 100, 14, 440, 334);
        ui.centered(b, f, r.cleared() ? "THE CHURCH IS SILENT" : "YOUR JOURNEY ENDS HERE",
                new Rectangle(left + 118, 321, 404, 18), .6f, UiTheme.GOLD);
        ui.centered(b, f, r.cleared() ? "VICTORY" : "GAME OVER",
                new Rectangle(left + 118, 294, 404, 28), 1.7f, r.cleared() ? UiTheme.SUCCESS : UiTheme.TEXT);
        String scope = r.startStage() == 1 ? "CAMPAIGN" : "PRACTICE FROM 0" + r.startStage();
        ui.centered(b, f, scope + " / REACHED REGION 0" + r.stage(),
                new Rectangle(left + 118, 281, 404, 12), .6f, UiTheme.MUTED);
        for (int tab = 0; tab < 2; tab++) {
            Rectangle bounds = tabBounds(left, tab);
            ui.button(b, f, bounds, tab == 0 ? "SUMMARY" : "REGIONS & REWARDS", true, tab == page, false, .65f);
            if (tab == page) ui.rect(b, bounds.x, bounds.y, bounds.width, 2, UiTheme.GOLD, 1);
        }
        if (page == 0) summary(b, f, left, r);
        else regions(b, f, left, r);
        String[] labels = r.cleared() ? new String[]{"PLAY AGAIN", "STAGE SELECT", "MAIN MENU"}
                : new String[]{"RETRY STAGE", "NEW RUN", "MAIN MENU"};
        for (int row = 0; row < 3; row++) ui.button(b, f, buttonBounds(left, row), labels[row],
                true, hovered == row, pressed, row == 0 ? 1f : .75f);
        ui.centered(b, f, r.cleared() ? "[ENTER / R] NEW RUN    [TAB] DETAILS    [ESC] MENU"
                        : "[ENTER] RETRY    [R] NEW RUN    [TAB] DETAILS    [ESC] MENU",
                new Rectangle(left + 118, 24, 404, 18), .52f, UiTheme.MUTED);
    }

    private void summary(SpriteBatch b, BitmapFont f, float left, RunResult r) {
        metric(b, f, left + 118, 210, "ACTIVE TIME", JourneyView.time(r.seconds()));
        metric(b, f, left + 257, 210, "DEFEATED", Integer.toString(r.defeated()));
        metric(b, f, left + 396, 210, "GOLD EARNED / LEFT", r.goldEarned() + " / " + r.goldRemaining());
        metric(b, f, left + 118, 164, "DAMAGE TAKEN", r.damageTaken() + " HP");
        metric(b, f, left + 257, 164, "POTIONS USED", Integer.toString(r.potionsUsed()));
        metric(b, f, left + 396, 164, "STAGE RETRIES", Integer.toString(r.retries()));
        ui.text(b, f, "BUILD  ATK +" + r.attackLevels() + " / SHOP HP +" + r.healthLevels() * GameBalance.HEALTH_UPGRADE
                + " / REWARD HP +" + r.bonusHealth(), left + 118, 156, .6f, UiTheme.TEXT, 404);
        ui.text(b, f, "ACTIONS  J " + r.attacks() + " / Q CASTS " + r.skills() + " / DASH " + r.dashes(),
                left + 118, 143, .58f, UiTheme.MUTED, 404);
        String best = r.clearCount() == 0 ? "No clear record yet."
                : (r.newBest() ? "NEW BEST!  " : "BEST  ") + JourneyView.time(r.bestSeconds())
                        + " / CLEARS " + r.clearCount();
        ui.centered(b, f, r.cleared() ? best : "Retry restores region-entry HP, build, gold, items and rewards.",
                new Rectangle(left + 118, 125, 404, 12), .53f, r.cleared() ? UiTheme.GOLD : UiTheme.MUTED);
    }

    private void regions(SpriteBatch b, BitmapFont f, float left, RunResult r) {
        int index = 0;
        for (RegionResult region : r.regions()) {
            float y = 221 - index++ * 29;
            ui.rect(b, left + 118, y, 404, 27, UiTheme.BORDER, .25f);
            ui.text(b, f, "0" + region.stage() + " " + JourneyView.regionName(region.stage()) + (region.cleared() ? " / CLEAR" : " / FAILED"),
                    left + 125, y + 20, .59f, region.cleared() ? UiTheme.SUCCESS : UiTheme.TEXT, 225);
            ui.text(b, f, JourneyView.time(region.seconds()) + " / DMG " + region.damage() + " / R" + region.retries(),
                    left + 365, y + 20, .56f, UiTheme.TEXT, 148);
            String reward = region.stage() == 4 && region.cleared() ? "VICTORY" : "--";
            for (StageReward choice : r.rewards()) if (choice.stage() == region.stage()) reward = choice.detail();
            ui.text(b, f, "REWARD  " + reward, left + 125, y + 8, .52f, UiTheme.GOLD, 225);
            ui.text(b, f, "J " + region.attacks() + " / Q " + region.skills() + " / DASH " + region.dashes() + " / P " + region.potions(),
                    left + 365, y + 8, .5f, UiTheme.MUTED, 148);
        }
        if (r.regions().isEmpty()) ui.text(b, f, "No region details for this run.", left + 130, 225, .7f, UiTheme.MUTED, 380);
        ui.centered(b, f, "R = retries / P = potions. Totals include failed attempts.",
                new Rectangle(left + 118, 125, 404, 12), .51f, UiTheme.MUTED);
    }
    @Override public void dispose() { ui.dispose(); }
}
