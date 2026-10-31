package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Code-drawn light trails; the character's original sprite pixels are untouched. */
final class ShockSlashView {
    void draw(ShapeRenderer s, Player p) {
        if (!p.isUsingSkill()) return;
        boolean blend = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_BLEND); Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        float elapsed = p.getSkillElapsed(), dir = p.isFacingRight() ? 1 : -1;
        float handX = p.getCenterX() + dir * 18, handY = p.getY() + 38;
        s.begin(ShapeRenderer.ShapeType.Filled);
        if (elapsed < GameBalance.SKILL_WINDUP) {
            float progress = elapsed / GameBalance.SKILL_WINDUP;
            float radius = 16 - 9 * progress;
            s.setColor(1, .75f, .30f, .35f + progress * .5f);
            for (int i = 0; i < 16; i++) {
                float a = i * 22.5f, b = a + 16;
                s.rectLine(handX + MathUtils.cosDeg(a) * radius, handY + MathUtils.sinDeg(a) * radius,
                        handX + MathUtils.cosDeg(b) * radius, handY + MathUtils.sinDeg(b) * radius, 1.2f);
            }
            s.setColor(1, .9f, .58f, .9f);
            s.rect(handX - 1.5f, handY - 1.5f, 3, 3);
        } else if (elapsed < GameBalance.SKILL_STRIKE_END) {
            float fade = 1 - (elapsed - GameBalance.SKILL_WINDUP) / (GameBalance.SKILL_STRIKE_END - GameBalance.SKILL_WINDUP);
            Rectangle r = p.getSkillBounds();
            float start = p.isFacingRight() ? r.x : r.x + r.width;
            s.setColor(1, .67f, .25f, fade * .8f);
            for (int i = 0; i < 18; i++) {
                float a = -75 + i * 150f / 18, b = -75 + (i + 1) * 150f / 18;
                s.rectLine(start + dir * MathUtils.cosDeg(a) * r.width, r.y + 32 + MathUtils.sinDeg(a) * 30,
                        start + dir * MathUtils.cosDeg(b) * r.width, r.y + 32 + MathUtils.sinDeg(b) * 30, 2.8f);
            }
            s.setColor(1, .94f, .71f, fade);
            s.rectLine(start, r.y + 32, start + dir * r.width, r.y + 32, 2);
            float tip = start + dir * r.width;
            s.rectLine(tip - 5, r.y + 26, tip + 5, r.y + 38, 1.7f);
            s.rectLine(tip - 5, r.y + 38, tip + 5, r.y + 26, 1.7f);
        }
        s.end(); if (!blend) Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
