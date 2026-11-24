package com.example.pixelgame;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Death reserves a reward; only the delayed arrival at the player awards it. */
final class GoldDrops {
    static final float MAGNET_DELAY=1.5f,COLLECT_TIME=1.75f;
    static final class Drop {
        final int amount,seed;
        float x,y,vx,vy=92,age,fromX,fromY;
        boolean flying;
        Drop(int amount,float x,float y,int seed){this.amount=amount;this.x=x;this.y=y;this.seed=seed;vx=(seed%3-1)*18;}
    }
    private static final Color GOLD=new Color(.86f,.72f,.43f,1);
    final ArrayList<Drop> drops=new ArrayList<>();
    private int sequence;
    void clear(){drops.clear();sequence=0;}
    int pending(){int amount=0;for(Drop drop:drops)amount+=drop.amount;return amount;}
    void spawn(int amount,float x,float y){if(amount>0)drops.add(new Drop(amount,x,y,sequence++));}
    int update(float dt,Player p,List<Rectangle> solids,List<Rectangle> platforms,float bottom){
        if(!p.isAlive())return 0;
        int collected=0;
        for(int i=drops.size()-1;i>=0;i--){
            Drop d=drops.get(i);d.age+=Math.max(0,dt);
            if(d.age>=COLLECT_TIME){collected+=d.amount;drops.remove(i);continue;}
            if(d.age>=MAGNET_DELAY){
                if(!d.flying){d.flying=true;d.fromX=d.x;d.fromY=d.y;}
                float t=MathUtils.clamp((d.age-MAGNET_DELAY)/(COLLECT_TIME-MAGNET_DELAY),0,1);t=t*t*(3-2*t);
                d.x=MathUtils.lerp(d.fromX,p.getCenterX(),t);d.y=MathUtils.lerp(d.fromY,p.getY()+24,t);
            }else{
                float previous=d.y;d.x+=d.vx*dt;d.vy-=420*dt;d.y+=d.vy*dt;
                float floor=Math.max(landing(d,previous,solids),landing(d,previous,platforms));
                if(d.y<bottom+4)floor=Math.max(floor,bottom+4);
                if(Float.isFinite(floor)){d.y=floor;d.vy=0;d.vx*=Math.max(0,1-8*dt);}
            }
        }
        return collected;
    }
    private float landing(Drop d,float previous,List<Rectangle> surfaces){
        if(d.vy>0)return Float.NEGATIVE_INFINITY;
        float floor=Float.NEGATIVE_INFINITY;
        for(Rectangle r:surfaces){
            if(r.width<=0||d.x<r.x||d.x>r.x+r.width)continue;
            float top=r.y+r.height+4;
            if(previous>=top-.01f&&d.y<=top)floor=Math.max(floor,top);
        }
        return floor;
    }
    void draw(SpriteBatch b,UiTheme ui){
        boolean gentle=GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS);
        for(Drop d:drops){
            float glow=d.flying?.16f:.07f;
            if(!gentle)for(int n=2;n>0;n--)ui.diamond(b,d.x,d.y,9+n*4,GOLD,glow/n);
            int coins=d.amount>=30?3:2;
            for(int c=0;c<coins;c++){
                float x=d.x+(c-(coins-1)/2f)*7,y=d.y+c%2*3;
                float w=gentle?7:4.5f+Math.abs(MathUtils.sin(d.age*8+c))*3;
                for(int n=0;n<16;n++){
                    float a=n*22.5f,e=(n+1)*22.5f;
                    ui.triangle(b,x,y,x+MathUtils.cosDeg(a)*(w+1),y+MathUtils.sinDeg(a)*7f,
                            x+MathUtils.cosDeg(e)*(w+1),y+MathUtils.sinDeg(e)*7f,UiTheme.SURFACE,1);
                    ui.triangle(b,x,y,x+MathUtils.cosDeg(a)*w,y+MathUtils.sinDeg(a)*6f,
                            x+MathUtils.cosDeg(e)*w,y+MathUtils.sinDeg(e)*6f,GOLD,1);
                }
                ui.line(b,x,y-3.5f,x,y+3.5f,.8f,UiTheme.TEXT,.85f);
            }
        }
    }
}
