package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/** Swap the world once at blackout, then reveal the stage title. Uses real time. */
final class StageTransition implements Disposable {
    private static final float OUT = .18f, BLACK = .08f, REVEAL = .32f, HOLD = .7f, EXIT = .25f;
    private static final float END = OUT + BLACK + REVEAL + HOLD + EXIT;
    private final GlyphLayout layout = new GlyphLayout();
    private Texture pixel;
    private boolean active, swapped;
    private float age;
    private int stage;

    void begin(int destination, boolean fadeOut) {
        if (destination < 1 || destination > 4) throw new IllegalArgumentException("Invalid stage");
        stage = destination; active = true; swapped = !fadeOut;
        age = fadeOut ? 0 : OUT + BLACK;
    }

    boolean update(float delta) {
        if (!active) return false;
        age += Math.max(0, delta);
        boolean swap = !swapped && age >= OUT;
        if (swap) swapped = true;
        if (age >= END) active = false;
        return swap;
    }

    boolean active() { return active; }
    void reset() { active = false; swapped = true; age = 0; }
    float fadeAlpha() {
        if (!active) return 0;
        if (age < OUT) return age / OUT;
        if (age < OUT + BLACK) return 1;
        return Math.max(0, 1 - (age - OUT - BLACK) / REVEAL);
    }
    float titleAlpha() {
        if (!active || age < OUT + BLACK) return 0;
        if (age < OUT + BLACK + REVEAL) return (age - OUT - BLACK) / REVEAL;
        return Math.min(1, Math.max(0, (END - age) / EXIT));
    }

    private void centered(SpriteBatch batch, BitmapFont font, String text,
                          float centerX, float top, float scale, float alpha, boolean gold) {
        UiTypography.size(font, scale); layout.setText(font, text);
        if (gold) font.setColor(.9f, .76f, .49f, alpha);
        else font.setColor(.95f, .96f, .98f, alpha);
        font.draw(batch, text, centerX - layout.width / 2, top);
    }

    void draw(SpriteBatch batch, BitmapFont font, float left) {
        if (!active) return;
        if (pixel == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE); p.fill(); pixel = new Texture(p); p.dispose();
        }
        Color oldBatch = new Color(batch.getColor()), oldFont = new Color(font.getColor());
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        float title = titleAlpha();
        batch.setColor(.015f, .02f, .03f, Math.max(fadeAlpha(), title * .28f));
        batch.draw(pixel, left, 0, 640, 360);
        if (title > 0) {
            batch.setColor(.025f, .035f, .05f, .82f * title);
            batch.draw(pixel, left, 140, 640, 86);
            batch.setColor(.74f, .62f, .4f, .6f * title);
            batch.draw(pixel, left + 214, 218, 212, 1);
            batch.draw(pixel, left + 214, 148, 212, 1);
            batch.setColor(Color.WHITE);
            centered(batch, font, stage == 4 ? "보스와의 조우" : "지역 " + stage,
                    left + 320, 207, .8f, title, true);
            centered(batch, font, StageLayout.name(stage), left + 320, 183, 1.35f, title, false);
        }
        font.getData().setScale(sx, sy); font.setColor(oldFont); batch.setColor(oldBatch);
    }

    @Override public void dispose() { if (pixel != null) pixel.dispose(); }
}
