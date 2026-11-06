package com.example.pixelgame;

import java.io.StringReader;
import java.util.Properties;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Shared atlas, a fixed drawing scale and body anchors instead of moving wing/foot bounds. */
final class MothSprites implements AutoCloseable {
    private final MoonAtlas atlas;
    private final float[][] anchors=new float[8][2];
    private final float scale;
    MothSprites(){this(MoonAtlas.acquire("art/enemy_glasswing_v1.png",4,2),Gdx.files.internal("art/glasswing-layout-v1.properties").readString("UTF-8"));}
    MothSprites(MoonAtlas atlas,String layout){
        this.atlas=atlas;Properties p=new Properties();
        try{p.load(new StringReader(layout));}catch(java.io.IOException e){throw new IllegalArgumentException("Moth layout",e);}
        scale=Float.parseFloat(p.getProperty("scale"));
        for(int i=0;i<8;i++){String[] a=p.getProperty("anchor."+i).split(",");anchors[i][0]=Float.parseFloat(a[0]);anchors[i][1]=Float.parseFloat(a[1]);}
    }
    void draw(SpriteBatch b,int frame,float cx,float cy,boolean right,float flash,float alpha){
        float color=b.getPackedColor();b.setColor(1,flash>0?.4f:1,flash>0?.45f:1,alpha);
        atlas.drawPivoted(b,frame,cx,cy,anchors[frame][0],anchors[frame][1],scale,right);b.setPackedColor(color);
    }
    @Override public void close(){atlas.close();}
}
