package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

final class InventoryView {
    private final UiTheme ui;
    InventoryView(UiTheme ui) { this.ui = ui; }
    static Rectangle useBounds(float left, int row) {
        return new Rectangle(left + 444, 198 - row * 78, 76, 32);
    }
    static Rectangle closeBounds(float left) { return new Rectangle(left + 500, 280, 24, 24); }

    void draw(SpriteBatch b, BitmapFont f, float left, Player player, int heals, int powers,
              Texture healIcon, Texture powerIcon, int hovered, boolean pressed, boolean closeHover,
              String message, boolean success) {
        ui.rect(b, left, 0, 640, 360, Color.BLACK, .72f);
        ui.panel(b, left + 100, 60, 440, 254);
        ui.text(b, f, "INVENTORY", left + 118, 295, 1.15f, UiTheme.TEXT, 350);
        ui.text(b, f, "Paused | Q SHOCK SLASH: " + player.getSkillDamage() + " damage / 8s cooldown", left + 118, 274, .6f, UiTheme.MUTED, 360);
        ui.button(b, f, closeBounds(left), "X", true, closeHover, pressed, .8f);
        ui.rect(b, left + 118, 260, 404, 1, UiTheme.BORDER, 1);
        for (int row = 0; row < 2; row++) {
            int count = row == 0 ? heals : powers;
            float y = 178 - row * 78;
            boolean full = row == 0 && player.getHealth() >= player.getMaxHealth();
            boolean enabled = player.isAlive() && count > 0 && !full;
            ui.rect(b, left + 118, y, 404, 70, UiTheme.BORDER, .22f);
            ui.rect(b, left + 118, y, 2, 70, enabled ? UiTheme.GOLD : UiTheme.MUTED, .6f);
            Texture icon = row == 0 ? healIcon : powerIcon;
            if (icon != null) {
                float packed = b.getPackedColor();
                b.setColor(1, 1, 1, count > 0 ? 1 : .35f);
                b.draw(icon, left + 128, y + 23, 30, 30); b.setPackedColor(packed);
            }
            ui.text(b, f, row == 0 ? "Heal potion" : "Power potion", left + 170, y + 53, .9f, UiTheme.TEXT, 250);
            ui.text(b, f, row == 0 ? "Restore " + GameBalance.healing(player.getMaxHealth()) + " HP (50% max HP)"
                    : "+" + GameBalance.POWER_BONUS + " damage for " + (int)GameBalance.POWER_DURATION + " seconds",
                    left + 170, y + 35, .62f, UiTheme.MUTED, 260);
            String owned = "OWNED " + count;
            if (row == 1 && player.getAttackPotionTimer() > 0)
                owned += " / ACTIVE " + (int)Math.ceil(player.getAttackPotionTimer()) + "s";
            ui.text(b, f, owned, left + 170, y + 17, .58f, count > 0 ? UiTheme.GOLD : UiTheme.MUTED, 260);
            String label = count == 0 ? "EMPTY" : full ? "FULL HP" : row == 0 ? "USE [1]"
                    : player.getAttackPotionTimer() > 0 ? "REFRESH [2]" : "USE [2]";
            ui.button(b, f, useBounds(left, row), label, enabled, hovered == row, pressed, .7f);
        }
        ui.text(b, f, message.isEmpty() ? "[1 / 2] USE ITEM     [I / ESC] CLOSE" : message,
                left + 118, 82, .64f, message.isEmpty() ? UiTheme.MUTED
                        : success ? UiTheme.SUCCESS : UiTheme.GOLD, 404);
    }
}
