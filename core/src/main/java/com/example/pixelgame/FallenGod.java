package com.example.pixelgame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/** Final-boss adapter preserves the game's combat/results contract with the new greatsword boss. */
public final class FallenGod {
    final Executioner combat;
    private ExecutionerSprites sprites;
    public FallenGod(float x,float floor,float worldWidth){combat=new Executioner(x,floor,worldWidth);}
    public int getHealth(){return combat.health();}
    public int getMaxHealth(){return GameBalance.BOSS_HEALTH;}
    public float getCenterX(){return combat.cx();}
    public Rectangle getBounds(){return new Rectangle(combat.body);}
    public Rectangle getAttackBounds(){return combat.attackBounds();}
    public boolean isAlive(){return combat.alive();}
    public boolean isEnraged(){return combat.enraged();}
    public boolean isDefeatedComplete(){return combat.defeatedComplete();}
    public boolean isAttacking(){return combat.preparing()||combat.striking();}
    public boolean isWindingUp(){return combat.preparing();}
    public boolean isRecovering(){return combat.recovering();}
    public boolean isVulnerable(){return combat.recovering();}
    public float getAttackCueProgress(){return combat.cueProgress();}
    public float getRecoveryProgress(){return combat.recoveryProgress();}
    public String cueName(){return switch(combat.attack()){case SWEEP->"가로 베기";case SLAM->"내려찍기";case UPPERCUT->"올려베기";case RUSH->"전진 베기";};}
    public void update(float dt,Player player){combat.update(dt,player.getBounds(),player.isAlive());if(combat.takeHit())player.takeDamage(combat.attackDamage(),combat.hitOrigin());}
    public void takeDamage(int damage){combat.damage(damage);}
    private ExecutionerSprites art(){if(sprites==null)sprites=new ExecutionerSprites();return sprites;}
    public void draw(SpriteBatch b){if(!combat.defeatedComplete())art().actor(b,combat);}
    public void drawEffects(SpriteBatch b){if(sprites!=null)art().effects(b,combat);}
    public float getWarningSeconds(){return combat.windupRemaining();}
    public String getWarningLabel(){return combat.preparing()?cueName():"";}
    public void drawTelegraph(ShapeRenderer s){/* The existing EnemyAttackView owns cue rendering. */}
    public void drawHealthBar(ShapeRenderer s,float cameraX,float screenTop){
        if(!isAlive())return;
        s.setColor(.04f,.05f,.08f,1);s.rect(cameraX-180,screenTop-62,360,8);
        s.setColor(.7f,.8f,.9f,1);s.rect(cameraX-180,screenTop-62,360f*getHealth()/getMaxHealth(),8);
    }
    public void dispose(){if(sprites!=null){sprites.close();sprites=null;}}
}
