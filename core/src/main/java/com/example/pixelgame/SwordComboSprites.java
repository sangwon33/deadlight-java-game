package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Stable original player proportions; separate silver-white trails remain in front of the blade. */
final class SwordComboSprites implements AutoCloseable {
    private final Texture actor,fx;
    private final TextureRegion[] poses,effects;
    private final float[][] feet,cores;
    private final float scale;
    SwordComboSprites(){
        JsonValue data=new JsonReader().parse(Gdx.files.internal("art/player-combo-layout-v1.json")),runtime=data.get("runtime");
        scale=runtime.getFloat("body_scale");feet=points(runtime.get("feet_anchors"));cores=points(runtime.get("effect_anchors"));
        actor=load(data.get("actor").getString("file"));Texture other;
        try{other=load(data.get("effects").getString("file"));}catch(RuntimeException e){actor.dispose();throw e;}
        fx=other;poses=regions(actor,data.get("actor").get("frames"));effects=regions(fx,data.get("effects").get("frames"));
    }
    private static Texture load(String file){Texture t=new Texture(Gdx.files.internal("art/"+file));t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return t;}
    private static float[][] points(JsonValue p){float[][] out=new float[p.size][2];for(int i=0;i<p.size;i++)out[i]=p.get(i).asFloatArray();return out;}
    private static TextureRegion[] regions(Texture t,JsonValue frames){TextureRegion[] out=new TextureRegion[frames.size];for(JsonValue f:frames){JsonValue r=f.get("region");out[f.getInt("index")]=new TextureRegion(t,r.getInt("x"),r.getInt("y"),r.getInt("width"),r.getInt("height"));}return out;}
    void actor(SpriteBatch b,SwordCombo combo,float cx,float y,boolean right,float flash,float alpha){drawPose(b,combo.pose(),cx,y,right,flash,alpha);}
    void drawPose(SpriteBatch b,int frame,float cx,float y,boolean right,float flash,float alpha){
        TextureRegion r=poses[frame];float ax=feet[frame][0]*scale,packed=b.getPackedColor();
        b.setColor(1,flash>0?.35f:1,flash>0?.4f:1,alpha);
        b.draw(r,right?cx-ax:cx+ax,y-(r.getRegionHeight()-feet[frame][1])*scale,(right?1:-1)*r.getRegionWidth()*scale,r.getRegionHeight()*scale);b.setPackedColor(packed);
    }
    void effects(SpriteBatch b,SwordCombo combo,float cx,float y,boolean right){
        if(!combo.damaging())return;
        int step=combo.step(),frame=step-1+(combo.strikeProgress()>.55f?3:0);float size=step==3?.17f:step==2?.15f:.13f;
        float alpha=.12f+.8f*(float)Math.sin(Math.PI*combo.strikeProgress());
        float x=cx+(right?1:-1)*(step==3?38:step==2?19:30),cy=y+(step==2?44:step==3?23:28);
        TextureRegion r=effects[frame];float ax=cores[frame][0]*size,packed=b.getPackedColor();b.setColor(1,1,1,Math.max(0,Math.min(1,alpha)));
        b.draw(r,right?x-ax:x+ax,cy-(r.getRegionHeight()-cores[frame][1])*size,(right?1:-1)*r.getRegionWidth()*size,r.getRegionHeight()*size);b.setPackedColor(packed);
    }
    @Override public void close(){actor.dispose();fx.dispose();}
}
