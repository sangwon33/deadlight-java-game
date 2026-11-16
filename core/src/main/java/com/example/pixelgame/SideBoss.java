package com.example.pixelgame;

import java.util.ArrayList;
import com.badlogic.gdx.math.Rectangle;

/** Root rushes and committed aerial needle attacks; no dependency on input or graphics. */
final class SideBoss {
    enum Phase { DORMANT,IDLE,RUSH_PREP,RUSH,ROOT_PREP,ROOTS,SWEEP_PREP,SWEEP,THRUST_PREP,THRUST,LEAP,PLUNGE,NEEDLE_PREP,NEEDLES,RECOVER,DEFEAT }
    static final class Root {final float x;float age;boolean hit;Root(float x,float age){this.x=x;this.age=age;}}
    static final class Needle {final float x;float y,age;Needle(float x,float y,float age){this.x=x;this.y=y;this.age=age;}}
    final SideBossArena arena;
    final Rectangle body;
    final ArrayList<Root> roots=new ArrayList<>();
    final ArrayList<Needle> needles=new ArrayList<>();
    private Phase phase=Phase.DORMANT;
    private float timer,time,invincible,flash,deathTime,lockedX,startX,startY,endX,impact,impactX;
    private int health,pattern,combo;
    private boolean right=true,moving,attackHit,pendingHit,rewardClaimed;
    private float hitOrigin;
    SideBoss(SideBossArena arena){this.arena=arena;health=arena.health();body=new Rectangle(arena.x()+480,arena.floor(),arena.bodyWidth(),arena.bodyHeight());}
    Phase phase(){return phase;}
    int health(){return health;}
    float cx(){return body.x+body.width/2;}
    boolean active(){return phase!=Phase.DORMANT&&phase!=Phase.DEFEAT;}
    boolean defeated(){return phase==Phase.DEFEAT;}
    boolean enraged(){return health>0&&health<=arena.health()/2;}
    boolean right(){return right;}
    float flash(){return flash;}
    float deathTime(){return deathTime;}
    float impact(){return impact;}
    float impactX(){return impactX;}
    int attackDamage(){return arena.roots()?(enraged()?22:18):(enraged()?26:22);}
    boolean recovering(){return phase==Phase.RECOVER;}
    boolean canStart(Rectangle p,boolean grounded){return phase==Phase.DORMANT&&grounded&&Math.abs(p.y-arena.floor())<4&&p.x>=arena.right()-160&&p.x+p.width<=arena.right()-40;}
    boolean start(Rectangle p,boolean grounded){if(!canStart(p,grounded))return false;right=p.x>cx();enter(Phase.IDLE,.85f);return true;}
    private void enter(Phase next,float seconds){phase=next;timer=seconds;attackHit=false;}
    private float speed(){return enraged()?1.16f:1;}
    int frame(){return switch(phase){
        case RUSH_PREP,THRUST_PREP,NEEDLE_PREP,NEEDLES ->2;case RUSH,THRUST ->3;
        case ROOT_PREP,SWEEP_PREP,LEAP ->4;case ROOTS,SWEEP,PLUNGE ->5;
        case RECOVER ->flash>0?6:0;case DEFEAT ->7;case IDLE ->moving?(int)(time/.22f)%2:0;default ->0;
    };}
    int damage(int amount){
        if(!active()||amount<=0||invincible>0)return 0;
        int dealt=Math.min(health,recovering()?Math.round(amount*GameBalance.COUNTER_MULTIPLIER):amount);
        health-=dealt;invincible=.16f;flash=.13f;
        if(health==0){enter(Phase.DEFEAT,0);body.y=arena.floor();roots.clear();needles.clear();pendingHit=false;impact=0;}
        return dealt;
    }
    boolean claimReward(){if(!defeated()||rewardClaimed)return false;rewardClaimed=true;return true;}
    boolean takeHit(){boolean result=pendingHit;pendingHit=false;return result;}
    float hitOrigin(){return hitOrigin;}
    void update(float dt,Rectangle p,boolean alive){
        for(float remaining=Math.max(0,dt);remaining>.000001f;){float step=Math.min(remaining,1f/120);remaining-=step;tick(step,p,alive);}
    }
    private void tick(float dt,Rectangle p,boolean alive){
        time+=dt;invincible=Math.max(0,invincible-dt);flash=Math.max(0,flash-dt);impact=Math.max(0,impact-dt);moving=false;
        if(defeated()){deathTime+=dt;return;}
        if(!active()||!alive){pendingHit=false;roots.clear();needles.clear();return;}
        for(int i=roots.size()-1;i>=0;i--){
            Root r=roots.get(i);r.age+=dt;if(r.age>1.15f){roots.remove(i);continue;}
            if(r.age>=.3f&&r.age<.92f&&!r.hit&&new Rectangle(r.x-15,arena.floor(),30,70).overlaps(p)){r.hit=true;registerHit(r.x);}
        }
        for(int i=needles.size()-1;i>=0;i--){
            Needle n=needles.get(i);n.age+=dt;if(n.age<0)continue;
            float next=n.y-620*dt;
            Rectangle expanded=new Rectangle(p.x-4,p.y-4,p.width+8,p.height+8);
            boolean hit=Float.isFinite(MothFlight.segment(n.x,n.y,n.x,next,expanded));
            if(hit||next<=arena.floor()){
                if(hit)registerHit(n.x);impactX=n.x;impact=.25f;needles.remove(i);
            }else n.y=next;
        }
        timer-=dt;float oldX=body.x,oldY=body.y,dir=right?1:-1;
        if(phase==Phase.IDLE){
            right=p.x+p.width/2>=cx();float gap=p.x+p.width/2-cx();
            if(Math.abs(gap)>(arena.roots()?125:145)){body.x=clamp(body.x+Math.copySign((arena.roots()?125:175)*dt,gap));moving=Math.abs(body.x-oldX)>.001f;}
        }else if(phase==Phase.RUSH||phase==Phase.THRUST){
            float reach=arena.roots()?18:62;body.x=clamp(body.x+dir*(arena.roots()?440:570)*speed()*dt);
            float x=right?Math.min(oldX,body.x)+body.width-7:Math.min(oldX,body.x)-reach;
            strike(new Rectangle(x,body.y+(arena.roots()?2:22),Math.abs(body.x-oldX)+reach+7,arena.roots()?62:28),p,cx());
        }else if(phase==Phase.SWEEP){
            strike(new Rectangle(right?cx():cx()-90,body.y+8,90,46),p,cx());
        }else if(phase==Phase.LEAP){
            float t=clamp01(1-timer/.5f);body.x=clamp(startX+(endX-startX)*t);body.y=arena.floor()+145*(float)Math.sin(t*Math.PI/2);
        }else if(phase==Phase.PLUNGE){
            float t=clamp01(1-timer/.3f);body.x=clamp(startX+(endX-startX)*t);body.y=startY+(arena.floor()-startY)*t;
            Rectangle expanded=new Rectangle(p.x-body.width/2,p.y-body.height/2,p.width+body.width,p.height+body.height);
            if(!attackHit&&Float.isFinite(MothFlight.segment(oldX+body.width/2,oldY+body.height/2,cx(),body.y+body.height/2,expanded))){attackHit=true;registerHit(cx());}
        }
        if(timer>0)return;
        switch(phase){
            case IDLE ->{
                right=p.x+p.width/2>=cx();combo=0;lockedX=Math.max(arena.left()+30,Math.min(arena.right()-30,p.x+p.width/2));
                int next=pattern++%3;
                if(arena.roots())enter(next==0?Phase.RUSH_PREP:next==1?Phase.ROOT_PREP:Phase.SWEEP_PREP,(next==0?.46f:.5f)/speed());
                else if(next==1){startX=body.x;endX=clamp(lockedX-(right?135:-135)-body.width/2);enter(Phase.LEAP,.5f);}
                else enter(next==0?Phase.THRUST_PREP:Phase.NEEDLE_PREP,(next==0?.42f:.55f)/speed());
            }
            case RUSH_PREP ->enter(Phase.RUSH,.3f/speed());
            case THRUST_PREP ->enter(Phase.THRUST,.22f/speed());
            case RUSH,THRUST ->{
                if(enraged()&&combo++==0){right=p.x+p.width/2>=cx();enter(arena.roots()?Phase.RUSH_PREP:Phase.THRUST_PREP,.3f);}
                else enter(Phase.RECOVER,(arena.roots()?1.05f:.8f)/speed());
            }
            case ROOT_PREP ->{
                int count=enraged()?5:3;for(int i=0;i<count;i++){float x=Math.max(arena.left()+22,Math.min(arena.right()-22,lockedX+(i-count/2)*76));roots.add(new Root(x,-i*.13f));}
                enter(Phase.ROOTS,1.7f);
            }
            case ROOTS ->enter(Phase.RECOVER,1.1f/speed());
            case NEEDLE_PREP ->{
                for(int i=0;i<3;i++){float x=Math.max(arena.left()+25,Math.min(arena.right()-25,lockedX+(i-1)*90));needles.add(new Needle(x,arena.roof()-45,-.2f-i*.16f));}
                enter(Phase.NEEDLES,1.35f);
            }
            case NEEDLES ->enter(Phase.RECOVER,1.0f/speed());
            case SWEEP_PREP ->enter(Phase.SWEEP,.22f);
            case SWEEP ->{impactX=cx()+dir*65;impact=.22f;enter(Phase.RECOVER,.9f/speed());}
            case LEAP ->{startX=body.x;startY=body.y;endX=clamp(lockedX-body.width/2);right=endX>=startX;enter(Phase.PLUNGE,.3f);}
            case PLUNGE ->{body.x=endX;body.y=arena.floor();impactX=cx();impact=.3f;enter(Phase.RECOVER,1.05f/speed());}
            case RECOVER ->enter(Phase.IDLE,.5f/speed());
            default ->{ }
        }
    }
    private float clamp(float x){return Math.max(arena.left(),Math.min(arena.right()-body.width,x));}
    private static float clamp01(float x){return Math.max(0,Math.min(1,x));}
    private void strike(Rectangle strike,Rectangle p,float origin){if(!attackHit&&strike.overlaps(p)){attackHit=true;registerHit(origin);}}
    private void registerHit(float origin){pendingHit=true;hitOrigin=origin;}
}
