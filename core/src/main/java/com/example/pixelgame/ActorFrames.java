package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Remove disconnected neighboring-cell debris without moving any source-space anchors. */
final class ActorFrames {
    private ActorFrames(){}
    static void isolate(Texture texture,String asset,TextureRegion[] frames,float[][] feet,float[][] heads){
        Pixmap pixels=new Pixmap(Gdx.files.internal(asset));
        try{
            for(int i=0;i<frames.length;i++){
                TextureRegion r=frames[i];
                int[] ink=MoonAtlas.dominantInk(pixels,r.getRegionX(),r.getRegionY(),r.getRegionWidth(),r.getRegionHeight());
                int left=Math.max(0,ink[0]-8),top=Math.max(0,ink[1]-8);
                int right=Math.min(r.getRegionWidth(),ink[2]+8),bottom=Math.min(r.getRegionHeight(),ink[3]+8);
                frames[i]=new TextureRegion(texture,r.getRegionX()+left,r.getRegionY()+top,right-left,bottom-top);
                feet[i][0]-=left;feet[i][1]-=top;
                if(heads!=null){heads[i][0]-=left;heads[i][1]-=top;}
            }
        }finally{pixels.dispose();}
    }
}
