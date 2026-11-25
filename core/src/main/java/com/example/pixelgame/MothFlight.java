package com.example.pixelgame;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;

/** Bounded flight and committed dives; wings are visual, only the small body can strike. */
final class MothFlight {
    enum Phase { HOVER, WINDUP, DIVE, RECOVER, HURT }
    static final float WINDUP=.55f, DIVE_SPEED=430, MAX_DIVE=.48f, RECOVERY=.85f;
    final Rectangle body;
    private float homeX,homeY,time,remaining,cooldown=1.2f,vx,vy;
    private float aimX,aimY;
    private boolean facingRight=true,hitThisDive,pendingHit;
    private Phase phase=Phase.HOVER;

    MothFlight(float x,float y,float width,float height){body=new Rectangle(x,y,width,height);relocate(x,y);}
    void relocate(float x,float y){body.setPosition(x,y);homeX=cx();homeY=y;}
    Phase phase(){return phase;}
    boolean facingRight(){return facingRight;}
    float cueProgress(){return phase==Phase.WINDUP?1-remaining/WINDUP:1;}
    float recoveryProgress(){return phase==Phase.RECOVER?remaining/RECOVERY:0;}
    boolean takeHit(){boolean result=pendingHit;pendingHit=false;return result;}
    int frame(){return phase==Phase.WINDUP?4:phase==Phase.DIVE?5:phase==Phase.HURT?6:(int)(time/.13f)%4;}
    void hurt(boolean heavy){phase=Phase.HURT;remaining=heavy?.65f:.32f;cooldown=Math.max(cooldown,.7f);pendingHit=false;}
    private float cx(){return body.x+body.width/2;}
    private float cy(){return body.y+body.height/2;}

    void update(float dt,Rectangle target,boolean targetAlive,List<Rectangle> solids,float left,float right){
        dt=Math.max(0,dt);time+=dt;cooldown=Math.max(0,cooldown-dt);
        separate(solids);
        if(!targetAlive){phase=Phase.HOVER;pendingHit=false;cooldown=Math.max(cooldown,.8f);}
        float available=dt;
        for(int transitions=0;available>.000001f&&transitions<5;transitions++){
            if(phase==Phase.WINDUP){
                float used=Math.min(available,remaining);remaining-=used;available-=used;
                if(remaining<=.000001f){
                    float dx=aimX-cx(),dy=aimY-cy(),distance=(float)Math.hypot(dx,dy);
                    vx=distance>.001f?dx/distance*DIVE_SPEED:0;vy=distance>.001f?dy/distance*DIVE_SPEED:0;
                    remaining=Math.min(MAX_DIVE,Math.max(.08f,distance/DIVE_SPEED));phase=Phase.DIVE;hitThisDive=false;
                }
            }else if(phase==Phase.DIVE){
                float used=Math.min(available,remaining),fromX=cx(),fromY=cy();
                boolean blocked=move(vx*used,vy*used,solids,left,right);
                if(!hitThisDive&&Float.isFinite(segment(fromX,fromY,cx(),cy(),expanded(target)))){
                    hitThisDive=true;pendingHit=true;
                }
                remaining-=used;available-=used;
                if(blocked||remaining<=.000001f){phase=Phase.RECOVER;remaining=RECOVERY;cooldown=1.5f;}
            }else if(phase==Phase.RECOVER||phase==Phase.HURT){
                float used=Math.min(available,remaining);remaining-=used;available-=used;
                if(remaining<=.000001f)phase=Phase.HOVER;
            }else{
                float tx=target.x+target.width/2,ty=target.y+target.height/2;
                boolean nearby=targetAlive&&Math.abs(tx-homeX)<420&&Math.abs(ty-(homeY+body.height/2))<230;
                float dx=tx-cx(),dy=ty-cy();
                if(nearby&&cooldown<=0&&Math.hypot(dx,dy)<230&&clearPath(tx,ty,solids)){
                    aimX=tx;aimY=ty;facingRight=dx>=0;phase=Phase.WINDUP;remaining=WINDUP;continue;
                }
                float desiredX=nearby?tx-Math.copySign(115,dx):homeX+(float)Math.sin(time*.8f)*42;
                float desiredY=nearby?Math.max(homeY-125,Math.min(homeY+125,ty+55))-body.height/2
                        :homeY+(float)Math.sin(time*2.1f)*11;
                float mx=desiredX-cx(),my=desiredY-body.y,length=(float)Math.hypot(mx,my);
                float distance=Math.min(length,(nearby?90:40)*available);
                if(length>.01f){move(mx/length*distance,my/length*distance,solids,left,right);if(Math.abs(mx)>3)facingRight=mx>0;}
                available=0;
            }
        }
    }
    void push(float dx,List<Rectangle> solids,float left,float right){move(dx,0,solids,left,right);}
    private Rectangle expanded(Rectangle r){return new Rectangle(r.x-body.width/2,r.y-body.height/2,r.width+body.width,r.height+body.height);}
    private boolean clearPath(float tx,float ty,List<Rectangle> solids){
        for(Rectangle r:solids)if(r.width>0&&segment(cx(),cy(),tx,ty,expanded(r))<1)return false;
        return true;
    }
    private boolean move(float dx,float dy,List<Rectangle> solids,float left,float right){
        float fraction=1;
        if(dx>0)fraction=Math.min(fraction,(right-body.width-body.x)/dx);
        else if(dx<0)fraction=Math.min(fraction,(left-body.x)/dx);
        if(dy>0)fraction=Math.min(fraction,(homeY+150-body.y)/dy);
        else if(dy<0)fraction=Math.min(fraction,(homeY-150-body.y)/dy);
        for(Rectangle r:solids)if(r.width>0)fraction=Math.min(fraction,segment(cx(),cy(),cx()+dx,cy()+dy,expanded(r)));
        fraction=Math.max(0,Math.min(1,fraction));
        if(fraction<1)fraction=Math.max(0,fraction-.001f/Math.max(1,(float)Math.hypot(dx,dy)));
        body.x+=dx*fraction;body.y+=dy*fraction;return fraction<.99999f;
    }
    private void separate(List<Rectangle> solids){
        for(Rectangle r:solids)if(r.width>0&&body.overlaps(r)){
            float a=body.x+body.width-r.x,b=r.x+r.width-body.x,c=body.y+body.height-r.y,d=r.y+r.height-body.y;
            float min=Math.min(Math.min(a,b),Math.min(c,d));
            if(min==a)body.x-=a+.01f;else if(min==b)body.x+=b+.01f;else if(min==c)body.y-=c+.01f;else body.y+=d+.01f;
        }
    }
    /** Slab intersection avoids the empty corners of a diagonal swept bounding box. */
    static float segment(float x,float y,float endX,float endY,Rectangle r){
        float entry=0,exit=1,dx=endX-x,dy=endY-y;
        if(Math.abs(dx)<.000001f){if(x<r.x||x>r.x+r.width)return Float.POSITIVE_INFINITY;}
        else {float a=(r.x-x)/dx,b=(r.x+r.width-x)/dx;entry=Math.max(entry,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));}
        if(Math.abs(dy)<.000001f){if(y<r.y||y>r.y+r.height)return Float.POSITIVE_INFINITY;}
        else {float a=(r.y-y)/dy,b=(r.y+r.height-y)/dy;entry=Math.max(entry,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));}
        return entry>exit||exit<.000001f?Float.POSITIVE_INFINITY:entry;
    }
}
