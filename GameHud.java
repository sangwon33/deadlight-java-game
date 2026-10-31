package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

/** Compact HUD, drawn over the world with translucent surfaces and shared spacing. */
final class GameHud implements Disposable {
    static Rectangle pauseBounds(float left) { return new Rectangle(left + 492, 290, 64, 20); }
    static Rectangle inventoryBounds(float left) { return new Rectangle(left + 564, 290, 64, 20); }
    private final Texture pixel;
    private final GlyphLayout layout = new GlyphLayout();

    GameHud() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill();
        pixel = new Texture(p);
        p.dispose();
    }

    private void rect(SpriteBatch batch, float x, float y, float w, float h,
                      float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
        batch.draw(pixel, x, y, w, h);
    }

    private void panel(SpriteBatch batch, float x, float y, float w, float h) {
        rect(batch, x, y - 2, w, h, 0, 0, 0, .18f);
        rect(batch, x, y, w, h, .38f, .34f, .26f, .9f);
        rect(batch, x + 1, y + 1, w - 2, h - 2, .035f, .05f, .07f, .9f);
        rect(batch, x + 1, y + h - 2, w - 2, 1, .88f, .74f, .48f, .3f);
    }

    private void text(SpriteBatch batch, BitmapFont font, String text,
                      float x, float y, float scale, boolean muted) {
        batch.setColor(Color.WHITE);
        font.getData().setScale(scale);
        if (muted) font.setColor(.65f, .72f, .79f, 1);
        else font.setColor(.94f, .96f, .98f, 1);
        font.draw(batch, text, x, y);
    }

    private void right(SpriteBatch batch, BitmapFont font, String text,
                       float edge, float y, float scale, float maxWidth) {
        font.getData().setScale(scale);
        layout.setText(font, text);
        if (layout.width > maxWidth) {
            scale *= maxWidth / layout.width;
            font.getData().setScale(scale);
            layout.setText(font, text);
        }
        text(batch, font, text, edge - layout.width, y, scale, false);
    }

    private void potion(SpriteBatch batch, BitmapFont font, float x, int key,
                        String label, int count, Texture icon, float buff) {
        batch.setColor(1, 1, 1, count > 0 || buff > 0 ? 1 : .38f);
        if (icon != null) batch.draw(icon, x, 298, 14, 14);
        text(batch, font, "[" + key + "] x" + count + (buff > 0 ? "  " + (int)Math.ceil(buff) + "s" : ""),
                x + 20, 309, .62f, count == 0 && buff <= 0);
    }

    void draw(SpriteBatch batch, BitmapFont font, float left, Player player, float trail,
              int heals, int powers, int gold, Texture healIcon, Texture powerIcon, Texture goldIcon) {
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        Color fc = new Color(font.getColor()), bc = new Color(batch.getColor());
        panel(batch, left + 12, 290, 212, 58);
        text(batch, font, "HEALTH", left + 24, 339, .6f, true);
        right(batch, font, player.getHealth() + " / " + player.getMaxHealth(), left + 212, 339, .86f, 120);
        rect(batch, left + 24, 321, 188, 6, .02f, .025f, .035f, 1);
        rect(batch, left + 24, 321, 188 * Math.min(1, Math.max(0, trail)), 6, .84f, .33f, .23f, 1);
        float hp = player.getHealthRatio();
        rect(batch, left + 24, 321, 188 * hp, 6, hp <= .3f ? .94f : .22f,
                hp <= .3f ? .4f : .75f, hp <= .3f ? .32f : .59f, 1);
        potion(batch, font, left + 24, 1, "HEAL", heals, healIcon, 0);
        potion(batch, font, left + 120, 2, "POWER", powers, powerIcon, player.getAttackPotionTimer());

        panel(batch, left + 492, 314, 136, 34);
        batch.setColor(Color.WHITE);
        if (goldIcon != null) batch.draw(goldIcon, left + 503, 322, 18, 18);
        text(batch, font, "GOLD", left + 529, 339, .55f, true);
        right(batch, font, Integer.toString(gold), left + 616, 328, .88f, 85);
        Rectangle inventory = inventoryBounds(left), pause = pauseBounds(left);
        panel(batch, inventory.x, inventory.y, inventory.width, inventory.height);
        text(batch, font, "[I] BAG", inventory.x + 8, 304, .6f, false);
        panel(batch, pause.x, pause.y, pause.width, pause.height);
        text(batch, font, "[ESC] PAUSE", pause.x + 5, 304, .55f, false);
        font.getData().setScale(sx, sy); font.setColor(fc); batch.setColor(bc);
    }

    void drawBoss(SpriteBatch batch, BitmapFont font, float left, FallenGod boss) {
        if (boss == null || !boss.isAlive()) return;
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        Color fc = new Color(font.getColor()), bc = new Color(batch.getColor());
        panel(batch, left + 160, 250, 320, 32);
        text(batch, font, "THE FALLEN GOD", left + 172, 272, .75f, false);
        right(batch, font, boss.getHealth() + " / " + boss.getMaxHealth(), left + 468, 272, .68f, 90);
        rect(batch, left + 172, 258, 296, 5, .08f, .025f, .035f, 1);
        rect(batch, left + 172, 258, 296f * boss.getHealth() / boss.getMaxHealth(), 5, .73f, .13f, .21f, 1);
        font.getData().setScale(sx, sy); font.setColor(fc); batch.setColor(bc);
    }

    void drawActions(SpriteBatch batch, BitmapFont font, float left, Player player, boolean screenControls) {
        drawActions(batch, font, left, player, screenControls, false);
    }

    void drawActions(SpriteBatch batch, BitmapFont font, float left, Player player,
                     boolean screenControls, boolean interrupted) {
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        Color fc = new Color(font.getColor()), bc = new Color(batch.getColor());
        if (interrupted || player.isUsingSkill() || !screenControls || player.getSkillCount() == 0)
            panel(batch, left + 236, 290, 244, 22);
        if (interrupted) {
            batch.setColor(Color.WHITE); font.getData().setScale(.64f); font.setColor(1, .55f, .43f, 1);
            font.draw(batch, "Q INTERRUPTED / " + (int)Math.ceil(player.getSkillCooldown()) + "s COOLDOWN", left + 245, 305);
        } else if (player.isUsingSkill()) {
            text(batch, font, "SHOCK SLASH / " + player.getSkillPhase(), left + 245, 306, .6f, false);
            float e = player.getSkillElapsed();
            float fill = e < GameBalance.SKILL_WINDUP ? e / GameBalance.SKILL_WINDUP
                    : e < GameBalance.SKILL_STRIKE_END ? 1 : (GameBalance.SKILL_DURATION - e)
                            / (GameBalance.SKILL_DURATION - GameBalance.SKILL_STRIKE_END);
            rect(batch, left + 245, 294, 226 * Math.max(0, Math.min(1, fill)), 2, .94f, .72f, .34f, 1);
        } else if (!screenControls) {
            String skill = player.getSkillCooldown() > 0 ? (int)Math.ceil(player.getSkillCooldown()) + "s" : "READY";
            String dash = player.getDashCooldownTimer() > 0 ? (int)Math.ceil(player.getDashCooldownTimer()) + "s" : "READY";
            text(batch, font, "[Q] SHOCK " + skill + "   [SHIFT] DASH " + dash, left + 244, 305, .6f, true);
        } else if (player.getSkillCount() == 0) {
            text(batch, font, "Q  SHOCK SLASH / " + player.getSkillDamage() + " DMG / BREAKS STANCE", left + 244, 305, .58f, true);
        }
        font.getData().setScale(sx, sy); font.setColor(fc); batch.setColor(bc);
    }

    @Override public void dispose() { pixel.dispose(); }
}
