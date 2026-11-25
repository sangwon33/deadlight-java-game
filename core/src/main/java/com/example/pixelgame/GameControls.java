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

/** Fixed screen-space controls with circular targets and shared action silhouettes. */
public class GameControls {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(640, 360, camera);
    // Every target stays below ground level (60), including the attack button.
    private final float[] x = {38, 96, 380, 494, 552, 436};
    private final float[] y = {31, 31, 31, 31, 31, 31};
    private final float[] radius = {21, 21, 20, 22, 20, 20};
    private final String[] keys = {"A", "D", "SPACE", "J", "SHIFT", "Q"};
    private final boolean[] held = new boolean[6], hover = new boolean[6];
    private final float[] press = new float[6];
    private final Vector2 cursor = new Vector2();
    private boolean attack, dash;
    private boolean skillPointerHeld, jumpPointerHeld;
    private CombatFeedback effects;
    void effects(CombatFeedback value){effects=value;}

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

    public void draw(Player player, SpriteBatch batch, ShapeRenderer s, BitmapFont font, UiTheme ui) {
        KeyBindings.Action[] actions = {KeyBindings.Action.LEFT, KeyBindings.Action.RIGHT, KeyBindings.Action.JUMP,
                KeyBindings.Action.ATTACK, KeyBindings.Action.DASH, KeyBindings.Action.SKILL};
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
            s.setColor(0.01f, 0.02f, 0.04f, 0.65f);
            s.circle(x[i], y[i] - 1, r + 1, 64);
            s.setColor(0.07f + press[i] * 0.1f, 0.10f + press[i] * 0.17f,
                    0.14f + press[i] * 0.2f, disabled ? 0.58f : 0.7f);
            s.circle(x[i], y[i], r, 64);
            if (disabled) s.setColor(0.49f, 0.56f, 0.63f, 0.65f);
            else if (hover[i]) s.setColor(0.72f, 0.88f, 1f, 0.95f);
            else s.setColor(0.68f, 0.84f, 0.93f, 0.9f);
            ring(s, x[i], y[i], r - 1, .9f, 1);
            float pulse=effects==null?0:effects.actionFlash(actions[i]);
            if(pulse>0){
                s.setColor(.76f,.9f,1,pulse*(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)?.3f:.85f));
                ring(s,x[i],y[i],r+2+(1-pulse)*5,1.1f,1);
            }
            if(i==5&&player.canUseSkill()){
                float flash=player.getSkillReadyFlash()*(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)?.2f:1);
                s.setColor(.68f,.88f,1,.45f+flash*.5f);ring(s,x[i],y[i],r+3+flash*5,.8f+flash,1);
            }
            if (i == 5 && player.isChargingSkill()) {
                Color charge = UiTheme.skillChargeColor(player.getSkillChargeStage());
                s.setColor(charge.r, charge.g, charge.b, 1);
                ring(s, x[i], y[i], r - 1, 1.4f, player.getSkillChargeProgress());
            } else if (cooldown > 0) {
                s.setColor(0.68f, 0.84f, 0.93f, 1);
                ring(s, x[i], y[i], r - 1, 2.4f, MathUtils.clamp(1 - cooldown / (i == 5 ? GameBalance.SKILL_COOLDOWN : GameBalance.DASH_COOLDOWN), 0, 1));
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
            boolean charging=i==5&&player.isChargingSkill();
            Color color=charging?UiTheme.skillChargeColor(player.getSkillChargeStage()):i==5?UiTheme.GOLD:UiTheme.TEXT;
            ActionIcons.draw(batch,ui,actions[i],x[i],y[i],.95f*(1-press[i]*.08f),color,disabled(i,player)&&!charging?.62f:1);
            if(hover[i]&&!player.isChargingSkill())ActionTooltip.draw(batch,ui,font,player,actions[i],x[i],y[i]+radius[i]);
        }
        for (int i : new int[]{4, 5}) if (cooldown(i, player) > 0) {
            String label = cooldown(i, player) < 1 ? String.format(java.util.Locale.ROOT, "%.1f", cooldown(i, player))
                    : Integer.toString((int)Math.ceil(cooldown(i, player)));
            ui.centered(batch, font, label, new Rectangle(x[i] - 18, 0, 36, 14),
                    .62f, Color.WHITE);
        }
        if(player.isChargingSkill()){
            ui.centered(batch,font,"집중 "+player.getSkillChargeStage()+"/4",new Rectangle(x[5]-44,66,88,16),.6f,UiTheme.TEXT);
        }else if(!hover[5]&&player.getSkillReadyFlash()>.35f){
            ui.centered(batch,font,"스킬 준비",new Rectangle(x[5]-44,63,88,16),.55f,UiTheme.GOLD);
        }else if(!hover[5]&&effects!=null&&effects.showsSkillInterrupted()){
            ui.centered(batch,font,"집중 취소",new Rectangle(x[5]-44,63,88,16),.6f,Color.SALMON);
        }
        batch.end();
        font.getData().setScale(sx, sy);
        font.setColor(cr, cg, cb, ca);
    }
}
