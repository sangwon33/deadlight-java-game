package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Explicit padded source rectangles keep steam and falling rubble inside their own frames. */
final class HazardSprites implements AutoCloseable {
    private final Texture texture=new Texture(Gdx.files.internal("art/hazard-states-v1.png"));
    private final TextureRegion[] vent=new TextureRegion[4];
    private final TextureRegion spikes;
    private final TextureRegion[] slab=new TextureRegion[3];
    HazardSprites(){
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        for(int i=0;i<4;i++)vent[i]=new TextureRegion(texture,i*384+38,8,310,476);
        spikes=new TextureRegion(texture,38,512+208,310,146);
        for(int i=0;i<3;i++)slab[i]=new TextureRegion(texture,(i+1)*384+20,512+252,344,166);
    }
    void vent(SpriteBatch b,int frame,float x,float floor){
        float scale=60f/293;
        b.draw(vent[frame],x-154*scale,floor-9*scale,310*scale,476*scale);
    }
    void spikes(SpriteBatch b,float x,float floor,float width,float height){
        // Only the pale tips occupy the damaging height; the mounting rail sits below it.
        b.draw(spikes,x-width*7/294,floor-height*26/112,width*310/294,height*146/112);
    }
    void bridge(SpriteBatch b,int frame,float x,float top,float width){
        // Repeat fixed-aspect masonry; crop the final segment rather than stretching a slab.
        float scale=18f/84,natural=324*scale;
        for(float offset=0;offset<width-.01f;offset+=natural){
            float visible=Math.min(natural,width-offset);
            TextureRegion piece=new TextureRegion(slab[frame],0,0,Math.min(344,Math.round(visible/scale)+20),166);
            b.draw(piece,x+offset-10*scale,top-155*scale,piece.getRegionWidth()*scale,166*scale);
        }
    }
    @Override public void close(){texture.dispose();}
}
