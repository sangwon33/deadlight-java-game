package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Exact source rectangles and fixed scale per character; no pose-dependent stretching. */
final class SideBossSprites implements AutoCloseable {
    private final Texture actor,fx;
    private final TextureRegion[] poses,effects;
    private final float[][] feet,cores;
    private final float scale;
    SideBossSprites(SideBossArena arena){
        JsonValue data=new JsonReader().parse(Gdx.files.internal("art/"+arena.layout()));
        JsonValue runtime=data.get("runtime");scale=runtime.getFloat("body_scale");
        feet=points(runtime.get("feet_anchors"));cores=points(runtime.get("effect_anchors"));
        actor=load(data.get("actor").getString("file"));
        Texture effectsTexture;
        try{effectsTexture=load(data.get("effects").getString("file"));}catch(RuntimeException e){actor.dispose();throw e;}
        fx=effectsTexture;poses=regions(actor,data.get("actor").get("frames"));effects=regions(fx,data.get("effects").get("frames"));
    }
    private static Texture load(String file){Texture t=new Texture(Gdx.files.internal("art/"+file));t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return t;}
    private static float[][] points(JsonValue values){float[][] a=new float[8][2];for(int i=0;i<8;i++)a[i]=values.get(i).asFloatArray();return a;}
    private static TextureRegion[] regions(Texture t,JsonValue frames){
        TextureRegion[] out=new TextureRegion[8];for(JsonValue f:frames){JsonValue r=f.get("region");out[f.getInt("index")]=new TextureRegion(t,r.getInt("x"),r.getInt("y"),r.getInt("width"),r.getInt("height"));}return out;
    }
    void actor(SpriteBatch b,SideBoss boss){
        int frame=boss.frame();TextureRegion r=poses[frame];float ax=feet[frame][0]*scale;
        float packed=b.getPackedColor(),alpha=boss.defeated()?Math.max(.25f,1-boss.deathTime()*.3f):1;
        b.setColor(1,boss.flash()>0?.5f:1,boss.flash()>0?.55f:1,alpha);
        b.draw(r,boss.right()?boss.cx()-ax:boss.cx()+ax,boss.body.y-(r.getRegionHeight()-feet[frame][1])*scale,
                (boss.right()?1:-1)*r.getRegionWidth()*scale,r.getRegionHeight()*scale);
        b.setPackedColor(packed);
    }
    void effect(SpriteBatch b,int index,float x,float y,float size,float angle,float alpha){
        TextureRegion r=effects[index];float ox=cores[index][0]*size,oy=(r.getRegionHeight()-cores[index][1])*size;
        float packed=b.getPackedColor();b.setColor(1,1,1,alpha);
        b.draw(r,x-ox,y-oy,ox,oy,r.getRegionWidth()*size,r.getRegionHeight()*size,1,1,angle);b.setPackedColor(packed);
    }
    void effects(SpriteBatch b,SideBoss boss){
        if(boss.arena.roots()){
            for(SideBoss.Root root:boss.roots){
                float age=root.age;
                if(age<.3f)effect(b,0,root.x,boss.arena.floor(),.15f,0,.85f);
                else effect(b,1,root.x,boss.arena.floor(),.23f,0,age>.92f?Math.max(0,(1.15f-age)/.23f):1);
            }
            if(boss.phase()==SideBoss.Phase.RUSH)effect(b,5,boss.cx()-(boss.right()?40:-40),boss.body.y+30,.19f,boss.right()?0:180,.65f);
            if(boss.phase()==SideBoss.Phase.SWEEP)effect(b,4,boss.cx()+(boss.right()?55:-55),boss.body.y+40,.22f,boss.right()?0:180,.9f);
        }else{
            for(SideBoss.Needle n:boss.needles)effect(b,0,n.x,n.y,.17f,-90,1);
            if(boss.phase()==SideBoss.Phase.PLUNGE)effect(b,4,boss.cx(),boss.body.y+35,.22f,boss.right()?0:180,.8f);
            if(boss.phase()==SideBoss.Phase.THRUST)effect(b,7,boss.cx()+(boss.right()?55:-55),boss.body.y+40,.12f,boss.right()?0:180,.7f);
        }
        if(boss.impact()>0)effect(b,boss.arena.roots()?6:5,boss.impactX(),boss.arena.floor(),.22f,0,Math.min(1,boss.impact()/.25f));
    }
    @Override public void close(){actor.dispose();fx.dispose();}
}
