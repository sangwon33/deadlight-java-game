package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Fixed screen-space controls with circular targets and pixel icons. */
public class GameControls {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(640, 360, camera);
    // Every target stays below ground level (60), including the attack button.
    private final float[] x = {38, 96, 434, 548, 606, 490,154};
    private final float[] y = {33, 33, 33, 33, 33, 33,33};
    private final float[] radius = {21, 21, 20, 22, 20, 20,20};
    private final String[] keys = {"A", "D", "SPACE", "J", "SHIFT", "Q","S"};
    private final String[][] icons = {
        {"...#...", "..##...", ".###...", "#####..", ".###...", "..##...", "...#..."},
        {"...#...", "...##..", "...###.", "..#####", "...###.", "...##..", "...#..."},
        {"...#...", "..###..", ".#####.", "...#...", "...#...", ".......", ".#####."},
        {"......##", ".....###", "....###.", "#..###..", ".####...", "..##....", ".#.##...", "#......."},
        {"..#..#..", "...#..#.", "##..#..#", "...#..#.", "..#..#.."},
        {".....#.", "....###", "...###.", "..###..", ".###...", "###....", ".#....."}
    };
    private final boolean[] held = new boolean[7], hover = new boolean[7];
    private final float[] press = new float[7];
    private final Vector2 cursor = new Vector2();
    private boolean attack, dash;
    private boolean skillPointerHeld, jumpPointerHeld;

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
        boolean jumpPressed = clicked && hover[2] && !disabled(2, player);
        if (jumpPressed) jumpPointerHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) jumpPointerHeld = false;
        boolean skillPressed = clicked && hover[5] && player.canUseSkill();
        if (skillPressed) skillPointerHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) skillPointerHeld = false;
        player.setScreenInput(held[0], held[1], jumpPressed,
                attack, dash, skillPressed, skillPointerHeld);
        player.setScreenJumpHeld(jumpPointerHeld);
        player.setScreenDown(held[6]&&!disabled(6,player),clicked&&hover[6]&&!disabled(6,player));
    }
    public boolean attackPressed() { return attack; }
    public boolean dashPressed() { return dash; }

    private float cooldown(int index, Player p) { return index == 4 ? p.getDashCooldownTimer() : index == 5 ? p.getSkillCooldown() : 0; }
    private boolean disabled(int index, Player p) {
        if (index < 2) return false;
        if (!p.isAlive() || p.isStunned()) return true;
        if (index == 5) return !p.isChargingSkill() && !p.canUseSkill();
        if (index == 4) return !p.canDash();
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

    private void icon(ShapeRenderer s,int index,float scale) {
        float cx=x[index],cy=y[index],w=1.15f*scale;
        if(index<2){
            float d=index==0?-1:1;
            s.rectLine(cx+d*5,cy,cx-d*2,cy+5,w);s.rectLine(cx+d*5,cy,cx-d*2,cy-5,w);
        }else if(index==2){
            s.rectLine(cx,cy-5,cx,cy+5,w);s.rectLine(cx,cy+5,cx-4,cy+1,w);s.rectLine(cx,cy+5,cx+4,cy+1,w);
            s.rectLine(cx-5,cy-7,cx+5,cy-7,w*.7f);
        }else if(index==3){
            s.rectLine(cx-4,cy-5,cx+6,cy+6,w);s.rectLine(cx-4,cy,cx,cy-4,w);s.rectLine(cx-6,cy-7,cx-4,cy-5,w*1.7f);
        }else if(index==4){
            for(int i=0;i<2;i++){float dx=i*6-3;s.rectLine(cx+dx-2,cy-5,cx+dx+3,cy,w);s.rectLine(cx+dx+3,cy,cx+dx-2,cy+5,w);}
        }else if(index==6){
            s.rectLine(cx-5,cy+3,cx,cy-3,w);s.rectLine(cx,cy-3,cx+5,cy+3,w);
            s.rectLine(cx-5,cy-6,cx+5,cy-6,w*.7f);
        }else{
            s.rectLine(cx-7,cy,cx+7,cy,w);s.rectLine(cx+7,cy,cx+3,cy+3,w);s.rectLine(cx+7,cy,cx+3,cy-3,w);
            s.rectLine(cx-3,cy-4,cx-3,cy+4,w);
        }
    }

    public void draw(Player player, SpriteBatch batch, ShapeRenderer s, BitmapFont font, UiTheme ui) {
        KeyBindings.Action[] actions = {KeyBindings.Action.LEFT, KeyBindings.Action.RIGHT, KeyBindings.Action.JUMP,
                KeyBindings.Action.ATTACK, KeyBindings.Action.DASH, KeyBindings.Action.SKILL,KeyBindings.Action.DOWN};
        for (int i = 0; i < keys.length; i++) keys[i] = KeyBindings.name(actions[i]);
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
            float r = radius[i] * scale * .82f;
            s.setColor(0.01f, 0.02f, 0.04f, 0.24f);
            s.circle(x[i], y[i] - 1, r + 1, 64);
            s.setColor(0.07f + press[i] * 0.1f, 0.10f + press[i] * 0.17f,
                    0.14f + press[i] * 0.2f, disabled ? 0.28f : 0.18f);
            s.circle(x[i], y[i], r, 64);
            if (disabled) s.setColor(0.49f, 0.56f, 0.63f, 0.35f);
            else if (hover[i]) s.setColor(0.72f, 0.88f, 1f, 0.95f);
            else s.setColor(0.54f, 0.68f, 0.79f, 0.55f);
            ring(s, x[i], y[i], r - 1, .65f, 1);
            if (i == 5 && player.isChargingSkill()) {
                Color charge = ThrustSprites.chargeColor(player.getSkillChargeStage());
                s.setColor(charge.r, charge.g, charge.b, 1);
                icon(s, i, scale);
                ring(s, x[i], y[i], r - 1, 1.4f, player.getSkillChargeProgress());
            } else if (cooldown > 0) {
                s.setColor(0.68f, 0.84f, 0.93f, 1);
                ring(s, x[i], y[i], r - 1, 2.4f, MathUtils.clamp(1 - cooldown / (i == 5 ? GameBalance.SKILL_COOLDOWN : GameBalance.DASH_COOLDOWN), 0, 1));
            } else {
                s.setColor(i == 5 ? .68f : .91f, i == 5 ? .88f : .96f, 1, disabled ? .3f : 1);
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
        for (int i = 0; i < keys.length; i++) {
            ui.centered(batch, font, keys[i], new Rectangle(x[i] - 28, 0, 56, 12),
                    .52f, UiTheme.MUTED);
        }
        for (int i : new int[]{4, 5}) if (cooldown(i, player) > 0) {
            String label = cooldown(i, player) < 1 ? String.format(java.util.Locale.ROOT, "%.1f", cooldown(i, player))
                    : Integer.toString((int)Math.ceil(cooldown(i, player)));
            ui.centered(batch, font, label, new Rectangle(x[i] - 18, y[i] - 18, 36, 36),
                    .9f, Color.WHITE);
        }
        batch.end();
        font.getData().setScale(sx, sy);
        font.setColor(cr, cg, cb, ca);
    }
}
