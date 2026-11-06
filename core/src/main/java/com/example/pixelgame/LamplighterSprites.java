package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Explicit atlas rectangles preserve the generated pixels and one common body scale. */
final class LamplighterSprites implements AutoCloseable {
    private final Texture actor,fx;
    private final TextureRegion[] poses,effects;
    private static final float SCALE=.275f;
    private static final float[][] FEET={{190,406},{175,407},{164,406},{161,405},{163,406},{174,404},{173,406},{192,401}};
    private static final float[][] CORE={{254,255},{254,267},{206,253},{192,259},{261,198},{225,190},{203,182},{197,183}};
    LamplighterSprites(){
        actor=load("art/miniboss_lamplighter_v1.png");fx=load("art/lamplighter_fx_v1.png");
        JsonValue data=new JsonReader().parse(Gdx.files.internal("art/lamplighter-layout-v1.json"));
        poses=regions(actor,data.get("actor").get("frames"));effects=regions(fx,data.get("effects").get("frames"));
    }
    private static Texture load(String path){Texture t=new Texture(Gdx.files.internal(path));t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return t;}
    private static TextureRegion[] regions(Texture t,JsonValue frames){
        TextureRegion[] out=new TextureRegion[8];for(JsonValue f:frames){JsonValue r=f.get("region");out[f.getInt("index")]=new TextureRegion(t,r.getInt("x"),r.getInt("y"),r.getInt("width"),r.getInt("height"));}return out;
    }
    void actor(SpriteBatch b,Lamplighter boss){
        int frame=boss.frame();TextureRegion r=poses[frame];float ax=FEET[frame][0]*SCALE;
        float packed=b.getPackedColor();float alpha=boss.defeated()?Math.max(.25f,1-boss.deathTime()*.3f):1;
        b.setColor(1,boss.flash()>0?.5f:1,boss.flash()>0?.55f:1,alpha);
        // After release, use the guarded body pose; the moving lantern is rendered separately.
        if(boss.lanternOut&&boss.phase()==Lamplighter.Phase.CAST){frame=0;r=poses[0];ax=FEET[0][0]*SCALE;}
        if(boss.lanternOut){
            // Hide only the held lantern/loose chain through draw regions, without editing PNGs.
            int cutX=frame==5?285:55,cutY=frame==5?160:269;
            int cutW=frame==5?r.getRegionWidth()-cutX:63,cutH=frame==5?200:113;
            piece(b,r,frame,boss,0,0,r.getRegionWidth(),cutY);
            piece(b,r,frame,boss,0,cutY+cutH,r.getRegionWidth(),r.getRegionHeight()-cutY-cutH);
            piece(b,r,frame,boss,0,cutY,cutX,cutH);
            piece(b,r,frame,boss,cutX+cutW,cutY,r.getRegionWidth()-cutX-cutW,cutH);
        }else b.draw(r,boss.right()?boss.cx()-ax:boss.cx()+ax,Lamplighter.FLOOR-(r.getRegionHeight()-FEET[frame][1])*SCALE,
                (boss.right()?1:-1)*r.getRegionWidth()*SCALE,r.getRegionHeight()*SCALE);
        b.setPackedColor(packed);
    }
    private void piece(SpriteBatch b,TextureRegion source,int frame,Lamplighter boss,int x,int y,int w,int h){
        if(w<=0||h<=0)return;
        TextureRegion part=new TextureRegion(source,x,y,w,h);
        float local=(x-FEET[frame][0])*SCALE;
        b.draw(part,boss.cx()+(boss.right()?local:-local),Lamplighter.FLOOR+(FEET[frame][1]-y-h)*SCALE,
                (boss.right()?1:-1)*w*SCALE,h*SCALE);
    }
    void effect(SpriteBatch b,int index,float x,float y,float scale,float angle,float alpha){
        TextureRegion r=effects[index];float ox=CORE[index][0]*scale,oy=(r.getRegionHeight()-CORE[index][1])*scale;
        float packed=b.getPackedColor();b.setColor(1,1,1,alpha);
        b.draw(r,x-ox,y-oy,ox,oy,r.getRegionWidth()*scale,r.getRegionHeight()*scale,1,1,angle);b.setPackedColor(packed);
    }
    void effects(SpriteBatch b,Lamplighter boss){
        if(boss.lanternOut){
            boolean pull=boss.phase()==Lamplighter.Phase.PULL;
            float x=boss.cx()+(boss.right()?1:-1)*(pull?26:-23),y=Lamplighter.FLOOR+(pull?45:43),dx=boss.lanternX()-x,dy=boss.lanternY()-y;
            float length=(float)Math.hypot(dx,dy),angle=(float)Math.toDegrees(Math.atan2(dy,dx));
            // Repeat small physical links rather than stretching a chain image across the arena.
            for(float d=8;d<length-8;d+=17)effect(b,3,x+dx*d/length,y+dy*d/length,.09f,angle,.85f);
            effect(b,0,boss.lanternX(),boss.lanternY(),.135f,0,1);
        }
        if(boss.phase()==Lamplighter.Phase.THRUST)effect(b,5,boss.cx()+(boss.right()?73:-73),Lamplighter.FLOOR+40,.18f,boss.right()?0:180,.8f);
        if(boss.phase()==Lamplighter.Phase.SWEEP)effect(b,4,boss.cx()+(boss.right()?55:-55),Lamplighter.FLOOR+35,.26f,boss.right()?0:180,.9f);
        if(boss.impact()>0)effect(b,6,boss.lanternX(),Lamplighter.FLOOR+15,.17f,0,boss.impact()/.25f);
    }
    @Override public void close(){actor.dispose();fx.dispose();}
}
