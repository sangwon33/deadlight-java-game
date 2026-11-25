package com.example.pixelgame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;

/** One fixed head/body scale through all six airborne slash poses. */
final class AirAttackSprites implements AutoCloseable {
    private final Texture texture=new Texture(Gdx.files.internal("art/hero-air-slash-v1.png"));
    private final TextureRegion[] frames=new TextureRegion[6];
    private static final float SCALE=.155f;
    private static final float[][] ANCHORS={{246,442},{242,425},{240,416},{242,423},{246,433},{246,434}};
    AirAttackSprites(){texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);for(int i=0;i<6;i++)frames[i]=new TextureRegion(texture,i%3*512,i/3*512,512,512);}
    void draw(SpriteBatch b,int frame,float cx,float y,boolean right){
        float ax=ANCHORS[frame][0]*SCALE;
        b.draw(frames[frame],right?cx-ax:cx+ax,y-(512-ANCHORS[frame][1])*SCALE,(right?1:-1)*512*SCALE,512*SCALE);
    }
    Rectangle bounds(int frame,float cx,float y,boolean right,Rectangle out){return out.set(cx-(right?35:47),y,82,68);}
    public void close(){texture.dispose();}
}
