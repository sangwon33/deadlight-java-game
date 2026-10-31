package com.example.pixelgame;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Short, real-time visual responses; this class never changes combat damage. */
final class CombatFeedback {
    static final class Impact {
        final float x, y;
        final int damage;
        final boolean hurt;
        boolean heavy;
        float age, labelY;
        Impact(float x, float y, int damage, boolean hurt) {
            this.x = x; this.y = y; this.damage = damage; this.hurt = hurt;
        }
    }
    final ArrayList<Impact> impacts = new ArrayList<>();
    private final GlyphLayout layout = new GlyphLayout();
    private float stop, trail = 1, target = 1, trailDelay, hurtPulse, interruptedNotice;
    private static final Color HURT = new Color(1, .3f, .26f, 1);
    private boolean impactSoundPlayed;

    void reset(float healthRatio) {
        impacts.clear(); stop = 0; trailDelay = 0; hurtPulse = interruptedNotice = 0;
        trail = target = healthRatio; impactSoundPlayed = false;
    }

    void health(float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        if (ratio < target) trailDelay = 0.3f;
        if (ratio > target) trail = Math.max(trail, ratio);
        target = ratio;
    }

    void update(float realDelta, float healthRatio) {
        health(healthRatio);
        stop = Math.max(0, stop - realDelta);
        hurtPulse = Math.max(0, hurtPulse - realDelta);
        interruptedNotice = Math.max(0, interruptedNotice - realDelta);
        if (trailDelay > 0) trailDelay = Math.max(0, trailDelay - realDelta);
        else trail = Math.max(target, trail - realDelta * 0.8f);
        trail = Math.max(trail, target);
        impactSoundPlayed = false;
        for (int i = impacts.size() - 1; i >= 0; i--) {
            Impact impact = impacts.get(i);
            impact.age += realDelta;
            if (impact.age >= 0.65f) impacts.remove(i);
        }
    }

    void hit(float x, float y, int actualDamage, boolean hurt) {
        hit(x, y, actualDamage, hurt, false);
    }
    void hit(float x, float y, int actualDamage, boolean hurt, boolean heavy) {
        hit(x, y, actualDamage, hurt, heavy, y + (hurt ? 46 : 25));
    }
    void hit(float x, float y, int actualDamage, boolean hurt, boolean heavy, float labelY) {
        if (actualDamage <= 0) return;
        if (impacts.size() >= 32) impacts.remove(0);
        // Stagger close multi-target numbers vertically instead of stacking on one glyph.
        for (Impact previous : impacts)
            if (previous.age < .18f && Math.abs(previous.x - x) < 30 && Math.abs(previous.labelY - labelY) < 13)
                labelY = previous.labelY + 14;
        Impact impact = new Impact(x, y, actualDamage, hurt); impact.heavy = heavy;
        impact.labelY = labelY; impacts.add(impact);
        if (hurt) hurtPulse = .18f;
        stop = Math.max(stop, hurt ? .055f : heavy ? .07f : .035f);
    }

    void skillInterrupted() { interruptedNotice = 1.6f; }
    boolean showsSkillInterrupted() { return interruptedNotice > 0; }

    void drawScreenPulse(SpriteBatch batch, UiTheme ui, float left) {
        if (hurtPulse <= 0) return;
        float alpha = hurtPulse / .18f * .55f;
        ui.rect(batch, left, 0, 640, 3, HURT, alpha);
        ui.rect(batch, left, 357, 640, 3, HURT, alpha);
        ui.rect(batch, left, 3, 3, 354, HURT, alpha);
        ui.rect(batch, left + 637, 3, 3, 354, HURT, alpha);
    }

    boolean stopped() { return stop > 0; }
    float trailRatio() { return trail; }
    boolean allowImpactSound() {
        if (impactSoundPlayed) return false;
        impactSoundPlayed = true;
        return true;
    }

    void drawSparks(ShapeRenderer shapes) {
        for (Impact impact : impacts) {
            if (impact.age > 0.14f) continue;
            float size = (impact.heavy ? 18 : 10) * (1 - impact.age / 0.14f);
            if (impact.hurt) {
                shapes.setColor(1, .3f, .26f, 1);
                for (int dx : new int[]{-1, 1}) for (int dy : new int[]{-1, 1}) {
                    float radius = 18 + impact.age * 22;
                    shapes.rectLine(impact.x + dx * radius, impact.y + dy * radius,
                            impact.x + dx * (radius + size * .35f), impact.y + dy * (radius + size * .35f), 1.4f);
                }
                continue;
            }
            shapes.setColor(1, 0.88f, 0.52f, 1);
            shapes.rectLine(impact.x - size, impact.y - size,
                    impact.x + size, impact.y + size, 1.6f);
            shapes.rectLine(impact.x - size, impact.y + size,
                    impact.x + size, impact.y - size, 1.6f);
        }
    }

    void drawText(SpriteBatch batch, BitmapFont font) {
        drawText(batch, font, null);
    }

    void drawText(SpriteBatch batch, BitmapFont font, UiTheme ui) {
        float r = font.getColor().r, g = font.getColor().g;
        float b = font.getColor().b, a = font.getColor().a;
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        float packed = batch.getPackedColor(); batch.setColor(Color.WHITE);
        for (Impact impact : impacts) {
            String text = impact.hurt ? "HIT -" + impact.damage : impact.damage + (impact.heavy ? "!" : "");
            font.getData().setScale(impact.hurt ? .85f : impact.heavy ? .9f : .75f);
            layout.setText(font, text);
            float x = impact.x - layout.width / 2;
            float y = impact.labelY + impact.age * 28;
            float alpha = Math.min(1, (0.65f - impact.age) / 0.2f);
            if (impact.hurt && ui != null)
                ui.rect(batch, x - 3, y - layout.height - 2, layout.width + 6, layout.height + 4,
                        UiTheme.SURFACE, .92f * alpha);
            font.setColor(0.03f, 0.03f, 0.04f, alpha);
            font.draw(batch, text, x + 1, y - 1);
            font.draw(batch, text, x - 1, y + 1);
            if (impact.hurt) font.setColor(1, 0.4f, 0.35f, alpha);
            else font.setColor(1, 0.92f, 0.65f, alpha);
            font.draw(batch, text, x, y);
        }
        font.setColor(r, g, b, a);
        font.getData().setScale(sx, sy); batch.setPackedColor(packed);
    }
}
