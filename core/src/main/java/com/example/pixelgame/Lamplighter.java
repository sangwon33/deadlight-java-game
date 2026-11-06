package com.example.pixelgame;

import com.badlogic.gdx.math.Rectangle;

/** Optional tower encounter. Pure combat state, independent of textures and input. */
final class Lamplighter {
    static final float FLOOR=1020, LEFT=1020, RIGHT=1850;
    static final int MAX_HEALTH=144, GOLD=80;
    enum Phase { DORMANT, IDLE, THRUST_PREP, THRUST, CAST_PREP, CAST, PULL, SWEEP_PREP, SWEEP, RECOVER, DEFEAT }
    final Rectangle body=new Rectangle(1480,FLOOR,46,94);
    private Phase phase=Phase.DORMANT;
    private float timer,time,invincible,flash,deathTime;
    private int health=MAX_HEALTH,pattern,combo;
    private boolean right=true,moving,attackHit,pendingHit,rewardClaimed;
    private float hitOrigin,lanternStartX,lanternStartY,lanternEndX,lanternX,lanternY,impact;
    boolean lanternOut;
    Phase phase(){return phase;}
    int health(){return health;}
    boolean active(){return phase!=Phase.DORMANT&&phase!=Phase.DEFEAT;}
    boolean defeated(){return phase==Phase.DEFEAT;}
    boolean enraged(){return health>0&&health<=MAX_HEALTH/2;}
    boolean right(){return right;}
    float cx(){return body.x+body.width/2;}
    float flash(){return flash;}
    float deathTime(){return deathTime;}
    float lanternX(){return lanternX;}
    float lanternY(){return lanternY;}
    float impact(){return impact;}
    boolean recovering(){return phase==Phase.RECOVER;}
    boolean canStart(Rectangle p,boolean grounded){return phase==Phase.DORMANT&&grounded&&Math.abs(p.y-FLOOR)<4&&p.x>=1690&&p.x+p.width<=1810;}
    boolean start(Rectangle p,boolean grounded){if(!canStart(p,grounded))return false;right=p.x>cx();enter(Phase.IDLE,.85f);return true;}
    private void enter(Phase next,float seconds){phase=next;timer=seconds;attackHit=false;}
    private float speed(){return enraged()?1.18f:1;}
    int frame(){return switch(phase){
        case THRUST_PREP ->2;case THRUST ->3;case CAST_PREP,CAST ->4;case PULL ->5;
        case SWEEP_PREP,SWEEP ->5;case DEFEAT ->7;case RECOVER ->flash>0?6:0;
        case IDLE ->moving?(int)(time/.22f)%2:0;default ->0;
    };}
    boolean takeHit(){boolean value=pendingHit;pendingHit=false;return value;}
    float hitOrigin(){return hitOrigin;}
    int attackDamage(){return enraged()?24:20;}
    int damage(int amount){
        if(!active()||amount<=0||invincible>0)return 0;
        int dealt=Math.min(health,recovering()?Math.round(amount*GameBalance.COUNTER_MULTIPLIER):amount);
        health-=dealt;invincible=.16f;flash=.13f;
        if(health==0){enter(Phase.DEFEAT,0);lanternOut=false;pendingHit=false;impact=0;}
        return dealt;
    }
    boolean claimReward(){if(!defeated()||rewardClaimed)return false;rewardClaimed=true;return true;}
    void update(float dt,Rectangle target,boolean targetAlive){
        if(dt<=0)return;
        // Bounded substeps keep fast attacks and throw arcs stable at different frame rates.
        for(float remaining=dt;remaining>.000001f;){float step=Math.min(remaining,1f/120);remaining-=step;tick(step,target,targetAlive);}
    }
    private void tick(float dt,Rectangle target,boolean targetAlive){
        time+=dt;flash=Math.max(0,flash-dt);invincible=Math.max(0,invincible-dt);impact=Math.max(0,impact-dt);
        moving=false;
        if(defeated()){deathTime+=dt;return;}
        if(!active()||!targetAlive){pendingHit=false;return;}
        timer-=dt;
        float dir=right?1:-1,old=body.x;
        if(phase==Phase.IDLE){
            right=target.x+target.width/2>=cx();
            float gap=target.x+target.width/2-cx();
            if(Math.abs(gap)>150){body.x=clamp(body.x+Math.copySign(145*dt,gap),LEFT,RIGHT-body.width);moving=Math.abs(body.x-old)>.001f;}
        }else if(phase==Phase.THRUST){
            body.x=clamp(body.x+dir*470*speed()*dt,LEFT,RIGHT-body.width);
            float a=right?Math.min(old,body.x)+body.width-5:Math.min(old,body.x)-68;
            hit(new Rectangle(a,FLOOR+22,Math.abs(body.x-old)+73,31),target,cx());
        }else if(phase==Phase.CAST){
            float t=clamp(1-timer/.68f,0,1),oldX=lanternX,oldY=lanternY;
            lanternX=lanternStartX+(lanternEndX-lanternStartX)*t;
            lanternY=lanternStartY+(FLOOR+15-lanternStartY)*t+105*4*t*(1-t);
            Rectangle expanded=new Rectangle(target.x-10,target.y-12,target.width+20,target.height+24);
            if(!attackHit&&Float.isFinite(MothFlight.segment(oldX,oldY,lanternX,lanternY,expanded)))registerHit(lanternX);
        }else if(phase==Phase.PULL){
            float gap=lanternX-cx();body.x=clamp(body.x+Math.copySign(Math.min(Math.abs(gap),620*dt*speed()),gap),LEFT,RIGHT-body.width);
            hit(new Rectangle(Math.min(old,body.x)-6,FLOOR,body.width+Math.abs(old-body.x)+12,body.height*.7f),target,cx());
        }else if(phase==Phase.SWEEP){
            hit(new Rectangle(right?cx():cx()-105,FLOOR+8,105,50),target,cx());
        }
        if(timer>0)return;
        switch(phase){
            case IDLE -> {
                right=target.x+target.width/2>=cx();combo=0;
                int next=pattern++%3;
                if(next==0)enter(Phase.THRUST_PREP,.46f/speed());
                else if(next==1){
                    lanternEndX=clamp(target.x+target.width/2,LEFT+35,RIGHT-35);
                    if(Math.abs(lanternEndX-cx())<140)lanternEndX=clamp(cx()+(right?1:-1)*245,LEFT+35,RIGHT-35);
                    enter(Phase.CAST_PREP,.55f/speed());
                }else enter(Phase.SWEEP_PREP,.42f/speed());
            }
            case THRUST_PREP ->enter(Phase.THRUST,.24f/speed());
            case THRUST -> {
                if(enraged()&&combo++==0){right=target.x+target.width/2>=cx();enter(Phase.THRUST_PREP,.34f);}
                else enter(Phase.RECOVER,.95f/speed());
            }
            case CAST_PREP -> {lanternOut=true;lanternStartX=lanternX=cx()+(right?1:-1)*24;lanternStartY=lanternY=FLOOR+60;enter(Phase.CAST,.68f);}
            case CAST -> {lanternX=lanternEndX;lanternY=FLOOR+15;impact=.25f;enter(Phase.PULL,.5f/speed());}
            case PULL -> {lanternOut=false;impact=.2f;enter(Phase.RECOVER,1.1f/speed());}
            case SWEEP_PREP ->enter(Phase.SWEEP,.2f);
            case SWEEP -> {lanternX=cx()+(right?80:-80);impact=.2f;enter(Phase.RECOVER,.85f/speed());}
            case RECOVER ->enter(Phase.IDLE,.5f/speed());
            default -> { }
        }
    }
    private void hit(Rectangle strike,Rectangle target,float origin){if(!attackHit&&strike.overlaps(target))registerHit(origin);}
    private void registerHit(float origin){attackHit=true;pendingHit=true;hitOrigin=origin;}
    private static float clamp(float n,float lo,float hi){return Math.max(lo,Math.min(hi,n));}
}
