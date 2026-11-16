package com.example.pixelgame;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.math.Rectangle;

/** Grounded ranged encounters: readable aim, committed shots, cover and bounded retreat. */
final class LanternMarksman {
    enum Phase { IDLE, AIM, FIRE, RECOVER, HURT }
    static final float AIM_TIME=.6f, RECOVER_TIME=.65f, SHOT_SPEED=360, SHOT_LIFE=1.45f, RADIUS=3.5f;
    static final class Shot {
        float x,y,life=SHOT_LIFE;final float vx,vy;
        Shot(float x,float y,float vx,float vy){this.x=x;this.y=y;this.vx=vx;this.vy=vy;}
    }
    static final class Flash {
        final float x,y,angle;final boolean muzzle;float age;
        Flash(float x,float y,float angle,boolean muzzle){this.x=x;this.y=y;this.angle=angle;this.muzzle=muzzle;}
    }
    final Rectangle body;
    final ArrayList<Shot> shots=new ArrayList<>();
    final ArrayList<Flash> flashes=new ArrayList<>();
    private Phase phase=Phase.IDLE;
    private float remaining,cooldown,time,aimX,aimY;
    private boolean facingRight=true,moving,visible=true,pendingHit,fired;
    LanternMarksman(float x,float y,float width,float height){body=new Rectangle(x,y,width,height);cooldown=.55f+(x%173)/173f*.8f;}
    Phase phase(){return phase;}
    boolean facingRight(){return facingRight;}
    float progress(){return phase==Phase.AIM?1-remaining/AIM_TIME:1;}
    float recovery(){return phase==Phase.RECOVER?remaining/RECOVER_TIME:0;}
    float muzzleX(){return cx()+(facingRight?1:-1)*12;}
    float muzzleY(){return body.y+36.7f;}
    float cx(){return body.x+body.width/2;}
    void visible(boolean value){visible=value;}
    boolean takeHit(){boolean hit=pendingHit;pendingHit=false;return hit;}
    boolean takeFired(){boolean value=fired;fired=false;return value;}
    int frame(){return phase==Phase.AIM?(progress()<.35f?3:4):phase==Phase.FIRE?5:phase==Phase.HURT?6:moving?1+(int)(time/.18f)%2:0;}
    void hurt(boolean heavy){phase=Phase.HURT;remaining=heavy?.65f:.28f;cooldown=Math.max(cooldown,.7f);fired=false;}
    void clear(){shots.clear();flashes.clear();pendingHit=fired=false;phase=Phase.IDLE;}
    static boolean safe(Rectangle p,float worldWidth){float x=p.x+p.width/2;return x<260||x>worldWidth-480;}
    private void flash(float x,float y,float angle,boolean muzzle){if(flashes.size()>=12)flashes.remove(0);flashes.add(new Flash(x,y,angle,muzzle));}
    void push(float dx,List<Rectangle> obstacles,float left,float right){move(dx,obstacles,left,right);}
    void update(float dt,Rectangle target,boolean targetAlive,List<Rectangle> obstacles,float left,float right,float worldWidth){
        time+=dt;cooldown=Math.max(0,cooldown-dt);moving=false;
        for(int i=flashes.size()-1;i>=0;i--){Flash f=flashes.get(i);f.age+=dt;if(f.age>=(f.muzzle?.14f:.3f))flashes.remove(i);}
        if(!targetAlive||safe(target,worldWidth)){clear();return;}
        updateShots(dt,target,obstacles);
        float available=dt;
        for(int transitions=0;available>.000001f&&transitions<5;transitions++){
            if(phase!=Phase.IDLE){
                if(phase==Phase.AIM&&!visible){phase=Phase.RECOVER;remaining=RECOVER_TIME;}
                float used=Math.min(available,remaining);remaining-=used;available-=used;
                if(remaining>.000001f)continue;
                if(phase==Phase.AIM){
                    if(visible&&clearLine(muzzleX(),muzzleY(),aimX,aimY,obstacles)){
                        float dx=aimX-muzzleX(),dy=aimY-muzzleY(),length=Math.max(.001f,(float)Math.hypot(dx,dy));
                        if(shots.size()<2){shots.add(new Shot(muzzleX(),muzzleY(),dx/length*SHOT_SPEED,dy/length*SHOT_SPEED));fired=true;
                            flash(muzzleX(),muzzleY(),(float)Math.toDegrees(Math.atan2(dy,dx)),true);}
                        phase=Phase.FIRE;remaining=.16f;
                    }else{phase=Phase.RECOVER;remaining=RECOVER_TIME;}
                }else if(phase==Phase.FIRE){phase=Phase.RECOVER;remaining=RECOVER_TIME;cooldown=1.55f;}
                else phase=Phase.IDLE;
            }else{
                float tx=target.x+target.width/2,ty=target.y+target.height/2,dx=tx-cx();
                boolean nearby=Math.abs(dx)<=380&&Math.abs(ty-muzzleY())<=100;
                if(nearby){
                    facingRight=dx>=0;
                    if(Math.abs(dx)<115){move(-Math.copySign(65*available,dx),obstacles,left,right);}
                    else if(visible&&cooldown<=0){
                        float fromX=muzzleX(),fromY=muzzleY();
                        float angle=(float)Math.atan2(ty-fromY,Math.abs(tx-fromX));
                        if(Math.abs(angle)<Math.toRadians(35)&&clearLine(fromX,fromY,tx,ty,obstacles)){
                            aimX=tx;aimY=ty;phase=Phase.AIM;remaining=AIM_TIME;continue;
                        }
                    }
                }
                available=0;
            }
        }
    }
    private void updateShots(float dt,Rectangle target,List<Rectangle> obstacles){
        Rectangle expandedPlayer=expanded(target);
        for(int i=shots.size()-1;i>=0;i--){
            Shot s=shots.get(i);float travel=Math.min(dt,s.life),endX=s.x+s.vx*travel,endY=s.y+s.vy*travel;
            float wall=Float.POSITIVE_INFINITY;
            for(Rectangle r:obstacles)if(r.width>0)wall=Math.min(wall,MothFlight.segment(s.x,s.y,endX,endY,expanded(r)));
            float player=MothFlight.segment(s.x,s.y,endX,endY,expandedPlayer);
            if(Float.isFinite(wall)||Float.isFinite(player)){
                float t=Math.min(wall,player),x=s.x+(endX-s.x)*t,y=s.y+(endY-s.y)*t;
                if(player<wall)pendingHit=true;
                flash(x,y,0,false);shots.remove(i);
            }else{
                s.x=endX;s.y=endY;s.life-=dt;if(s.life<=0)shots.remove(i);
            }
        }
    }
    private Rectangle expanded(Rectangle r){return new Rectangle(r.x-RADIUS,r.y-RADIUS,r.width+RADIUS*2,r.height+RADIUS*2);}
    private boolean clearLine(float x,float y,float tx,float ty,List<Rectangle> obstacles){
        for(Rectangle r:obstacles)if(r.width>0&&MothFlight.segment(x,y,tx,ty,expanded(r))<=1)return false;return true;
    }
    private void move(float dx,List<Rectangle> obstacles,float left,float right){
        float old=body.x,next=Math.max(left,Math.min(right-body.width,old+dx));
        for(Rectangle r:obstacles){
            if(r.width<=0||body.y>=r.y+r.height-.01f||body.y+body.height<=r.y+.01f)continue;
            if(next>old&&old+body.width<=r.x+.01f&&next+body.width>r.x)next=Math.min(next,r.x-body.width);
            else if(next<old&&old>=r.x+r.width-.01f&&next<r.x+r.width)next=Math.max(next,r.x+r.width);
        }
        body.x=next;moving=Math.abs(next-old)>.001f;
    }
}
