package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** World-anchored strata and separate collision lips; peeking never exposes repeated root rows. */
final class GroundArt implements AutoCloseable {
    private final Texture texture=new Texture(Gdx.files.internal("art/terrain-depth-v1.png"));
    GroundArt(){
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
    }
    void draw(SpriteBatch b,TerrainTile lip,Rectangle floor,float bottom,float left,float right,int stage,UiTheme ui){
        float top=floor.y+floor.height,packed=b.getPackedColor();
        float r=b.getColor().r,g=b.getColor().g,blue=b.getColor().b,a=b.getColor().a;
        float tileW=texture.getWidth()/2f,tileH=texture.getHeight()/2f,offset=stage*137;
        int start=(int)Math.floor((Math.max(left,floor.x)+offset)/tileW);
        int end=(int)Math.floor((Math.min(right,floor.x+floor.width)+offset)/tileW);
        // A wide continuous painting replaces the old small independent root rectangles.
        // Reflecting alternate repeats joins identical edges, even when looking far below ground.
        for(float depth=0;top-7-depth>bottom;depth+=16)for(int col=start;col<=end;col++){
            float x=col*tileW-offset,l=Math.max(x,floor.x),rr=Math.min(x+tileW,floor.x+floor.width);
            if(rr<=l)continue;
            float y=top-7-depth,h=Math.min(16,y-bottom);
            int row=(int)(depth/tileH);
            int sx=Math.round((l-x)*2),sw=Math.max(1,Math.round((rr-l)*2));
            int sy=Math.round((depth-row*tileH)*2),sh=Math.max(1,Math.round(h*2));
            boolean flipX=(col&1)==0,flipY=(row&1)!=0;
            if(flipX)sx=texture.getWidth()-sx-sw;
            if(flipY)sy=texture.getHeight()-sy-sh;
            TextureRegion cut=new TextureRegion(texture,sx,sy,sw,sh);cut.flip(flipX,flipY);
            float shade=Math.max(.48f,.82f-depth/1400);
            b.setColor(r*shade*(stage==3?1:.94f),g*shade*(stage==1?1:.96f),blue*shade,a);
            b.draw(cut,l,y-h,rr-l,h);
        }
        b.setColor(r,g,blue,a);
        // Keep the established walkable top, but not its repeatedly tiled roots and arches.
        int sourceHeight=Math.min(lip.region.getRegionHeight(),lip.surfaceRow+Math.max(1,Math.round(18f/96*lip.region.getRegionHeight())));
        TextureRegion cap=new TextureRegion(lip.region,0,0,lip.region.getRegionWidth(),sourceHeight);
        float capHeight=96f*sourceHeight/lip.region.getRegionHeight();
        float capY=top-(sourceHeight-lip.surfaceRow)*96f/lip.region.getRegionHeight();
        for(float x=floor.x;x<floor.x+floor.width;x+=64){
            if(x+64<left||x>right)continue;
            float w=Math.min(64,floor.x+floor.width-x);
            TextureRegion cut=new TextureRegion(cap,0,0,Math.max(1,Math.round(cap.getRegionWidth()*w/64)),cap.getRegionHeight());
            b.draw(cut,x,capY,w,capHeight);
        }
        // Dark side planes and a broken pale rim distinguish real ledges from background rock.
        float visibleBottom=Math.max(bottom,top-180);
        for(float edge:new float[]{floor.x,floor.x+floor.width})if(edge>=left&&edge<=right){
            ui.rect(b,edge==floor.x?edge:edge-3,visibleBottom,3,top-visibleBottom,UiTheme.SURFACE,.8f);
            ui.line(b,edge,top-2,edge,Math.max(visibleBottom,top-25),.8f,UiTheme.BORDER,.8f);
        }
        b.setPackedColor(packed);
    }
    @Override public void close(){texture.dispose();}
}
