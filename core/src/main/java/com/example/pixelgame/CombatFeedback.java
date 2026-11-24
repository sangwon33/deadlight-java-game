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
        boolean heavy, sword,critical;
        float age, labelY;
        Impact(float x, float y, int damage, boolean hurt) {
            this.x = x; this.y = y; this.damage = damage; this.hurt = hurt;
        }
    }
    final ArrayList<Impact> impacts = new ArrayList<>();
    private final GlyphLayout layout = new GlyphLayout();
    private float stop,hurtPulse,interruptedNotice;
    private final DamageTrail healthTrail=new DamageTrail();
    private static final Color HURT = new Color(1, .3f, .26f, 1);
    private boolean impactSoundPlayed;
    private final float[] actionPulses=new float[KeyBindings.Action.values().length];
    private float goldPulse,goldNotice,pickupBurst,pickupX,pickupY;
    private int collectedGold;
    void action(KeyBindings.Action action){actionPulses[action.ordinal()]=action==KeyBindings.Action.HEAL||action==KeyBindings.Action.POWER||action==KeyBindings.Action.HASTE?.65f:.35f;}
    float actionFlash(KeyBindings.Action action){return Math.min(1,actionPulses[action.ordinal()]/.35f);}
    void goldCollected(int amount,float x,float y){
        if(amount<=0)return;
        collectedGold=goldNotice>0?collectedGold+amount:amount;goldNotice=1.25f;goldPulse=.8f;
        pickupBurst=.35f;pickupX=x;pickupY=y;
    }
    float goldFlash(){return goldPulse/.8f;}
    int goldNotice(){return goldNotice>0?collectedGold:0;}

    void reset(float healthRatio) {
        impacts.clear(); stop = 0; hurtPulse = interruptedNotice = 0;
        healthTrail.reset(healthRatio); impactSoundPlayed = false;
        java.util.Arrays.fill(actionPulses,0);goldPulse=goldNotice=pickupBurst=0;collectedGold=0;
    }

    void health(float ratio) {
        healthTrail.health(ratio);
    }

    void update(float realDelta, float healthRatio) {
        health(healthRatio);
        stop = Math.max(0, stop - realDelta);
        hurtPulse = Math.max(0, hurtPulse - realDelta);
        interruptedNotice = Math.max(0, interruptedNotice - realDelta);
        healthTrail.update(realDelta,healthRatio);
        impactSoundPlayed = false;
        for(int i=0;i<actionPulses.length;i++)actionPulses[i]=Math.max(0,actionPulses[i]-realDelta);
        goldPulse=Math.max(0,goldPulse-realDelta);goldNotice=Math.max(0,goldNotice-realDelta);pickupBurst=Math.max(0,pickupBurst-realDelta);
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
                labelY = previous.labelY + 14*GameSettings.current.textScale();
        Impact impact = new Impact(x, y, actualDamage, hurt); impact.heavy = heavy;
        impact.labelY = labelY; impacts.add(impact);
        if (hurt) hurtPulse = .18f;
        stop = Math.max(stop, hurt ? .055f : heavy ? .07f : .035f);
    }

    void critical(boolean value){if(!impacts.isEmpty())impacts.get(impacts.size()-1).critical=value;}
    void skillInterrupted() { interruptedNotice = 1.6f; }
    void swordHit(float x,float y,int damage,boolean heavy,float labelY){
        if(damage<=0)return;
        hit(x,y,damage,false,heavy,labelY);impacts.get(impacts.size()-1).sword=true;
    }
    boolean showsSkillInterrupted() { return interruptedNotice > 0; }

    float cameraKickX() { return 0; }

    void drawScreenPulse(SpriteBatch batch, UiTheme ui, float left) {
        if (hurtPulse <= 0||GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)) return;
        float alpha = hurtPulse / .18f * .55f;
        ui.rect(batch, left, 0, 640, 3, HURT, alpha);
        ui.rect(batch, left, 357, 640, 3, HURT, alpha);
        ui.rect(batch, left, 3, 3, 354, HURT, alpha);
        ui.rect(batch, left + 637, 3, 3, 354, HURT, alpha);
    }

    boolean stopped() { return GameSettings.current.on(GameSettings.Option.HIT_STOP)&&stop>0; }
    float trailRatio() { return healthTrail.value(); }
    boolean allowImpactSound() {
        if (impactSoundPlayed) return false;
        impactSoundPlayed = true;
        return true;
    }

    void drawSparks(ShapeRenderer shapes) {
        if(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS))return;
        if(pickupBurst>0){
            float t=1-pickupBurst/.35f,radius=7+t*19,length=5*(1-t);
            shapes.setColor(.95f,.81f,.5f,1-t);
            for(int i=0;i<8;i++){
                double a=Math.toRadians(i*45);float dx=(float)Math.cos(a),dy=(float)Math.sin(a);
                shapes.rectLine(pickupX+dx*radius,pickupY+dy*radius,pickupX+dx*(radius+length),pickupY+dy*(radius+length),1);
            }
        }
        for (Impact impact : impacts) {
            if(impact.sword){drawSwordSpark(shapes,impact);continue;}
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

    private void drawSwordSpark(ShapeRenderer s,Impact hit){
        float duration=hit.heavy?.12f:.09f;
        if(hit.age>=duration)return;
        float fade=1-hit.age/duration,size=(hit.heavy?13:7)*fade;
        // A thin cool outline keeps the white core readable on pale enemies too.
        s.setColor(.66f,.76f,.84f,1);
        s.rectLine(hit.x-size,hit.y-size*.65f,hit.x+size,hit.y+size*.65f,hit.heavy?2.8f:2.2f);
        s.rectLine(hit.x-size*.65f,hit.y+size,hit.x+size*.65f,hit.y-size,hit.heavy?2.8f:2.2f);
        s.setColor(1,1,1,1);
        s.rectLine(hit.x-size*.88f,hit.y-size*.57f,hit.x+size*.88f,hit.y+size*.57f,1.1f);
        s.rectLine(hit.x-size*.57f,hit.y+size*.88f,hit.x+size*.57f,hit.y-size*.88f,1.1f);
        if(hit.heavy){
            s.rectLine(hit.x-size*.6f,hit.y,hit.x+size*.6f,hit.y,1.2f);
            s.rectLine(hit.x,hit.y-size*.6f,hit.x,hit.y+size*.6f,1.2f);
            float radius=6+hit.age*45,length=3*fade;
            for(int dx:new int[]{-1,1})for(int dy:new int[]{-1,1})
                s.rectLine(hit.x+dx*radius,hit.y+dy*radius,hit.x+dx*(radius+length),hit.y+dy*(radius+length),.8f);
        }
    }

    void drawText(SpriteBatch batch, BitmapFont font) {
        drawText(batch, font, null);
    }

    void drawText(SpriteBatch batch, BitmapFont font, UiTheme ui) {
        if(!GameSettings.current.on(GameSettings.Option.DAMAGE_NUMBERS))return;
        float r = font.getColor().r, g = font.getColor().g;
        float b = font.getColor().b, a = font.getColor().a;
        float sx = font.getData().scaleX, sy = font.getData().scaleY;
        float packed = batch.getPackedColor(); batch.setColor(Color.WHITE);
        for (Impact impact : impacts) {
            String text = impact.hurt ? "피격 -" + impact.damage : impact.damage + (impact.heavy ? "!" : "");
            UiTypography.size(font, impact.hurt ? .85f : impact.heavy ? .9f : .75f);
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
            else if(impact.critical)font.setColor(1,.19f,.22f,alpha);
            else font.setColor(1, 0.92f, 0.65f, alpha);
            font.draw(batch, text, x, y);
        }
        font.setColor(r, g, b, a);
        font.getData().setScale(sx, sy); batch.setPackedColor(packed);
    }
}
