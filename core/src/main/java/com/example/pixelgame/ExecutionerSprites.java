package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Original full cells, stable boots, separate head-anchored halo and readable greatsword trails. */
final class ExecutionerSprites implements AutoCloseable {
    private final Texture actor,fx;
    private final TextureRegion[] poses,effects;
    private final float[][] feet,heads,cores;
    private final float scale;
    ExecutionerSprites(){
        JsonValue data=new JsonReader().parse(Gdx.files.internal("art/executioner-layout-v1.json")),runtime=data.get("runtime");
        scale=runtime.getFloat("body_scale");feet=points(runtime.get("feet_anchors"));heads=points(runtime.get("head_anchors"));cores=points(runtime.get("effect_anchors"));
        actor=load(data.get("actor").getString("file"));Texture other;
        try{other=load(data.get("effects").getString("file"));}catch(RuntimeException e){actor.dispose();throw e;}
        fx=other;poses=regions(actor,data.get("actor").get("frames"));effects=regions(fx,data.get("effects").get("frames"));
    }
    private static Texture load(String file){Texture t=new Texture(Gdx.files.internal("art/"+file));t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return t;}
    private static float[][] points(JsonValue p){float[][] out=new float[8][2];for(int i=0;i<8;i++)out[i]=p.get(i).asFloatArray();return out;}
    private static TextureRegion[] regions(Texture t,JsonValue frames){TextureRegion[] out=new TextureRegion[8];for(JsonValue f:frames){JsonValue r=f.get("region");out[f.getInt("index")]=new TextureRegion(t,r.getInt("x"),r.getInt("y"),r.getInt("width"),r.getInt("height"));}return out;}
    void actor(SpriteBatch b,Executioner boss){
        int frame=boss.frame();float direction=boss.right()?1:-1;
        float hx=boss.cx()+direction*(heads[frame][0]-feet[frame][0])*scale,hy=boss.floor()+(feet[frame][1]-heads[frame][1])*scale;
        float alpha=boss.alive()?1:Math.max(0,1-boss.deathTime()/1.05f);
        effect(b,boss.alive()?(boss.enraged()?1:0):7,hx,hy,.21f,0,alpha*.85f);
        TextureRegion r=poses[frame];float ax=feet[frame][0]*scale,packed=b.getPackedColor();
        b.setColor(1,boss.flash()>0?.48f:1,boss.flash()>0?.52f:1,alpha);
        b.draw(r,boss.right()?boss.cx()-ax:boss.cx()+ax,boss.floor()-(r.getRegionHeight()-feet[frame][1])*scale,
                direction*r.getRegionWidth()*scale,r.getRegionHeight()*scale);b.setPackedColor(packed);
    }
    void effect(SpriteBatch b,int index,float x,float y,float size,float angle,float alpha){
        TextureRegion r=effects[index];float ox=cores[index][0]*size,oy=(r.getRegionHeight()-cores[index][1])*size,packed=b.getPackedColor();
        b.setColor(1,1,1,Math.max(0,Math.min(1,alpha)));
        b.draw(r,x-ox,y-oy,ox,oy,r.getRegionWidth()*size,r.getRegionHeight()*size,1,1,angle);b.setPackedColor(packed);
    }
    void effects(SpriteBatch b,Executioner boss){
        float direction=boss.right()?1:-1;
        if(boss.striking()){
            int frame=switch(boss.attack()){case UPPERCUT->3;case SLAM->4;case RUSH->6;default->2;};
            float x=boss.cx()+direction*(boss.attack()==Executioner.Attack.RUSH?-15:75),y=boss.floor()+(boss.attack()==Executioner.Attack.UPPERCUT?98:55);
            float alpha=.8f*(float)Math.sin(Math.PI*boss.strikeProgress());
            // Negative X scale mirrors a down/up arc without reversing its vertical orientation.
            mirroredEffect(b,frame,x,y,boss.attack()==Executioner.Attack.RUSH?.28f:.34f,boss.right(),alpha);
        }
        if(boss.impact()>0)effect(b,5,boss.impactX(),boss.floor(),.25f,0,boss.impact()/.3f);
    }
    private void mirroredEffect(SpriteBatch b,int frame,float x,float y,float size,boolean right,float alpha){
        TextureRegion r=effects[frame];float ax=cores[frame][0]*size,packed=b.getPackedColor();b.setColor(1,1,1,Math.max(0,alpha));
        b.draw(r,right?x-ax:x+ax,y-(r.getRegionHeight()-cores[frame][1])*size,(right?1:-1)*r.getRegionWidth()*size,r.getRegionHeight()*size);b.setPackedColor(packed);
    }
    @Override public void close(){actor.dispose();fx.dispose();}
}
