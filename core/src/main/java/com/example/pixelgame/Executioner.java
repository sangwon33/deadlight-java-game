package com.example.pixelgame;

import com.badlogic.gdx.math.Rectangle;

/** Grounded greatsword combat with committed direction and continuous fast-motion hits. */
final class Executioner {
    enum Phase { IDLE,WINDUP,STRIKE,RECOVER,DEFEAT }
    enum Attack { SWEEP,SLAM,UPPERCUT,RUSH }
    final Rectangle body;
    private final float width,floor;
    private Phase phase=Phase.IDLE;
    private Attack attack=Attack.SWEEP;
    private float timer=1.05f,time,invincible,flash,deathTime,windupDuration,strikeDuration,impact,impactX;
    private int health=GameBalance.BOSS_HEALTH,cycle;
    private boolean right=false,moving,attackHit,pendingHit;
    private float hitOrigin;
    Executioner(float x,float floor,float width){this.width=width;this.floor=floor;body=new Rectangle(x,floor,58,124);body.x=clamp(x);}
    Phase phase(){return phase;}
    Attack attack(){return attack;}
    int health(){return health;}
    boolean alive(){return health>0;}
    boolean enraged(){return health>0&&health<=GameBalance.BOSS_HEALTH/2;}
    boolean defeatedComplete(){return !alive()&&deathTime>=1.05f;}
    boolean right(){return right;}
    float cx(){return body.x+body.width/2;}
    float floor(){return floor;}
    float flash(){return flash;}
    float deathTime(){return deathTime;}
    float impact(){return impact;}
    float impactX(){return impactX;}
    boolean recovering(){return phase==Phase.RECOVER;}
    boolean preparing(){return phase==Phase.WINDUP;}
    float windupRemaining(){return preparing()?Math.max(0,timer):0;}
    boolean striking(){return phase==Phase.STRIKE;}
    float cueProgress(){return windupDuration>0?Math.max(0,Math.min(1,1-timer/windupDuration)):0;}
    float recoveryProgress(){return recovering()?Math.max(0,Math.min(1,timer/recoveryDuration())):0;}
    int attackDamage(){return enraged()?28:24;}
    float strikeProgress(){return striking()?Math.max(0,Math.min(1,1-timer/strikeDuration)):0;}
    int frame(){
        if(phase==Phase.DEFEAT)return 7;
        if(phase==Phase.IDLE)return moving?(int)(time/.24f)%2:0;
        if(phase==Phase.RECOVER)return 0;
        if(phase==Phase.WINDUP)return attack==Attack.SLAM?2:4;
        return switch(attack){case SLAM->3;case UPPERCUT->6;default->5;};
    }
    private float speed(){return enraged()?1.14f:1;}
    private float recoveryDuration(){return (attack==Attack.SLAM?1.2f:attack==Attack.RUSH?1.05f:.95f)/speed();}
    private void enter(Phase p,float seconds){phase=p;timer=seconds;}
    int damage(int amount){
        if(!alive()||amount<=0||invincible>0)return 0;
        int dealt=Math.min(health,recovering()?Math.round(amount*GameBalance.COUNTER_MULTIPLIER):amount);
        health-=dealt;invincible=.16f;flash=.14f;
        if(health==0){enter(Phase.DEFEAT,0);pendingHit=false;impact=0;}
        return dealt;
    }
    boolean takeHit(){boolean value=pendingHit;pendingHit=false;return value;}
    float hitOrigin(){return hitOrigin;}
    void update(float dt,Rectangle p,boolean alive){
        for(float remaining=Math.max(0,dt);remaining>.000001f;){float step=Math.min(remaining,1f/120);remaining-=step;tick(step,p,alive);}
    }
    private void tick(float dt,Rectangle p,boolean targetAlive){
        time+=dt;flash=Math.max(0,flash-dt);invincible=Math.max(0,invincible-dt);impact=Math.max(0,impact-dt);moving=false;
        if(!alive()){deathTime+=dt;return;}
        if(!targetAlive){pendingHit=false;return;}
        timer-=dt;
        if(phase==Phase.IDLE){
            float dx=p.x+p.width/2-cx();right=dx>=0;
            if(Math.abs(dx)>112){float old=body.x;body.x=clamp(body.x+Math.copySign(145*dt*speed(),dx));moving=Math.abs(body.x-old)>.001f;}
            if(timer<=0&&Math.abs(dx)<=300){
                attack=Math.abs(dx)>210?Attack.RUSH:Attack.values()[cycle++%4];
                // Elevated platforms remain useful, but cannot permanently shelter from all attacks.
                if(p.y>floor+65&&attack==Attack.SWEEP)attack=Attack.UPPERCUT;
                windupDuration=(attack==Attack.SLAM?.62f:attack==Attack.RUSH?.55f:.48f)/speed();
                enter(Phase.WINDUP,windupDuration);attackHit=false;
            }
        }else if(phase==Phase.STRIKE){
            float old=body.x,dir=right?1:-1;
            body.x=clamp(body.x+dir*(attack==Attack.RUSH?650:attack==Attack.SWEEP?100:65)*speed()*dt);
            Rectangle bounds=attackBounds();
            // Include the small interval traversed between substeps, in either facing direction.
            float movement=body.x-old;if(movement>0){bounds.x-=movement;bounds.width+=movement;}else bounds.width-=movement;
            if(!attackHit&&strikeProgress()>=.1f&&bounds.overlaps(p)){attackHit=true;pendingHit=true;hitOrigin=cx();}
        }
        if(timer>0)return;
        if(phase==Phase.WINDUP){strikeDuration=(attack==Attack.RUSH?.32f:attack==Attack.SLAM?.3f:.26f)/speed();enter(Phase.STRIKE,strikeDuration);}
        else if(phase==Phase.STRIKE){
            if(attack==Attack.SLAM){impact=.3f;impactX=cx()+(right?1:-1)*92;}
            enter(Phase.RECOVER,recoveryDuration());
        }else if(phase==Phase.RECOVER)enter(Phase.IDLE,.38f/speed());
    }
    Rectangle attackBounds(){
        if(phase!=Phase.WINDUP&&phase!=Phase.STRIKE)return new Rectangle(cx(),floor,0,0);
        float reach=attack==Attack.SLAM?96:attack==Attack.UPPERCUT?100:attack==Attack.RUSH?82:92;
        float y=attack==Attack.UPPERCUT?floor+48:attack==Attack.SWEEP?floor+12:floor+2;
        float h=attack==Attack.UPPERCUT?125:attack==Attack.SLAM?100:attack==Attack.RUSH?62:52;
        return new Rectangle(right?body.x+body.width-8:body.x-reach,y,reach+8,h);
    }
    private float clamp(float x){float margin=Math.min(105,Math.max(0,(width-body.width)/2));return Math.max(margin,Math.min(width-margin-body.width,x));}
}
