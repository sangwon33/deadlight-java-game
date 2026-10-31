package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Fixed screen-space controls with circular targets and pixel icons. */
public class GameControls {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(640, 360, camera);
    // Every target stays below ground level (60), including the attack button.
    private final float[] x = {38, 96, 434, 548, 606, 490};
    private final float[] y = {33, 33, 33, 33, 33, 33};
    private final float[] radius = {21, 21, 20, 22, 20, 20};
    private final String[] keys = {"A", "D", "SPACE", "J", "SHIFT", "Q"};
    private final String[][] icons = {
        {"...#...", "..##...", ".###...", "#####..", ".###...", "..##...", "...#..."},
        {"...#...", "...##..", "...###.", "..#####", "...###.", "...##..", "...#..."},
        {"...#...", "..###..", ".#####.", "...#...", "...#...", ".......", ".#####."},
        {"......##", ".....###", "....###.", "#..###..", ".####...", "..##....", ".#.##...", "#......."},
        {"..#..#..", "...#..#.", "##..#..#", "...#..#.", "..#..#.."},
        {"...#...", ".#.#.#.", "..###..", "#######", "..###..", ".#.#.#.", "...#..."}
    };
    private final boolean[] held = new boolean[6], hover = new boolean[6];
    private final float[] press = new float[6];
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 cursor = new Vector2();
    private boolean attack, dash;

    private void refresh() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        boolean inside = mx >= viewport.getScreenX()
                && mx < viewport.getScreenX() + viewport.getScreenWidth()
                && my >= Gdx.graphics.getHeight() - viewport.getScreenY() - viewport.getScreenHeight()
                && my < Gdx.graphics.getHeight() - viewport.getScreenY();
        viewport.unproject(cursor.set(mx, my));
        for (int i = 0; i < keys.length; i++) {
            hover[i] = inside && cursor.dst2(x[i], y[i]) <= radius[i] * radius[i];
            held[i] = hover[i] && Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        }
    }

    public void update(Player player) {
        refresh();
        boolean clicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        // A quick down/up can arrive in one frame: edge-triggered actions must not
        // require the mouse to still be held when the simulation processes them.
        attack = clicked && hover[3] && !disabled(3, player);
        dash = clicked && hover[4] && !disabled(4, player);
        player.setScreenInput(held[0], held[1], clicked && hover[2] && !disabled(2, player),
                attack, dash, clicked && hover[5] && player.canUseSkill());
    }
    public boolean attackPressed() { return attack; }
    public boolean dashPressed() { return dash; }

    private float cooldown(int index, Player p) { return index == 4 ? p.getDashCooldownTimer() : index == 5 ? p.getSkillCooldown() : 0; }
    private boolean disabled(int index, Player p) {
        if (index < 2) return false;
        if (!p.isAlive() || p.isStunned()) return true;
        if (index == 5) return !p.canUseSkill();
        if (index == 4) return cooldown(index, p) > 0 || p.isAttacking() || p.isUsingSkill();
        return p.isUsingSkill() || (index == 3 && p.isDashing());
    }

    private void ring(ShapeRenderer s, float cx, float cy, float r, float width, float progress) {
        int segments = Math.max(1, (int)Math.ceil(80 * progress));
        for (int n = 0; n < segments; n++) {
            float a = 90 - 360 * progress * n / segments;
            float b = 90 - 360 * progress * (n + 1) / segments;
            s.rectLine(cx + MathUtils.cosDeg(a) * r, cy + MathUtils.sinDeg(a) * r,
                    cx + MathUtils.cosDeg(b) * r, cy + MathUtils.sinDeg(b) * r, width);
        }
    }

    private void icon(ShapeRenderer s, int index, float scale) {
        String[] pixels = icons[index];
        float unit = (index == 3 ? 2.7f : 2.3f) * scale;
        float left = x[index] - pixels[0].length() * unit / 2;
        float top = y[index] + pixels.length * unit / 2;
        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < pixels[row].length(); col++) {
                if (pixels[row].charAt(col) == '#')
                    s.rect(left + col * unit, top - (row + 1) * unit, unit, unit);
            }
        }
    }

    public void draw(Player player, SpriteBatch batch, ShapeRenderer s, BitmapFont font) {
        refresh();
        viewport.apply();
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        boolean blending = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        s.setProjectionMatrix(camera.combined);
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < keys.length; i++) {
            float cooldown = cooldown(i, player);
            boolean disabled = disabled(i, player);
            press[i] = MathUtils.lerp(press[i], held[i] && !disabled ? 1f : 0f, Math.min(1f, dt * 22));
            float scale = 1 - press[i] * 0.08f;
            float r = radius[i] * scale;
            s.setColor(0.01f, 0.02f, 0.04f, 0.24f);
            s.circle(x[i], y[i] - 2, r + 2, 64);
            s.setColor(0.07f + press[i] * 0.1f, 0.10f + press[i] * 0.17f,
                    0.14f + press[i] * 0.2f, disabled ? 0.82f : 0.68f);
            s.circle(x[i], y[i], r, 64);
            if (disabled) s.setColor(0.49f, 0.56f, 0.63f, 0.35f);
            else if (hover[i]) s.setColor(0.92f, 0.78f, 0.50f, 0.95f);
            else s.setColor(0.72f, 0.66f, 0.54f, 0.66f);
            ring(s, x[i], y[i], r - 1, 1.2f, 1);
            if (cooldown > 0) {
                s.setColor(0.88f, 0.74f, 0.48f, 1);
                ring(s, x[i], y[i], r - 1, 2.4f, MathUtils.clamp(1 - cooldown / (i == 5 ? GameBalance.SKILL_COOLDOWN : 5f), 0, 1));
            } else {
                s.setColor(i == 5 ? 1 : .91f, i == 5 ? .81f : .96f, i == 5 ? .43f : 1, disabled ? .3f : 1);
                icon(s, i, scale);
            }
        }
        s.end();
        if (!blending) Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.setProjectionMatrix(camera.combined);
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        float cr = font.getColor().r, cg = font.getColor().g;
        float cb = font.getColor().b, ca = font.getColor().a;
        batch.begin();
        font.getData().setScale(sx * 0.65f, sy * 0.65f);
        font.setColor(0.84f, 0.9f, 0.96f, 0.8f);
        for (int i = 0; i < keys.length; i++) {
            layout.setText(font, keys[i]);
            font.draw(batch, keys[i], x[i] - layout.width / 2, y[i] - radius[i] - 3);
        }
        for (int i : new int[]{4, 5}) if (cooldown(i, player) > 0) {
            font.getData().setScale(sx * 1.4f, sy * 1.4f);
            font.setColor(0.94f, 0.98f, 1, 1);
            String label = Integer.toString((int)Math.ceil(cooldown(i, player)));
            layout.setText(font, label);
            font.draw(batch, label, x[i] - layout.width / 2, y[i] + layout.height / 2);
        }
        batch.end();
        font.getData().setScale(sx, sy);
        font.setColor(cr, cg, cb, ca);
    }
}
