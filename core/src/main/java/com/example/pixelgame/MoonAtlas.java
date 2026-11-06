package com.example.pixelgame;

import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Generated atlas integration: keep alpha, share textures, anchor each pose to its own feet. */
final class MoonAtlas implements AutoCloseable {
    private static final HashMap<String,MoonAtlas> CACHE = new HashMap<>();
    private final String asset;
    private final Texture texture;
    private final TextureRegion[] regions;
    private final int[][] ink;
    private int owners;
    static MoonAtlas acquire(String asset,int columns,int rows) {
        MoonAtlas atlas=CACHE.get(asset);
        if(atlas==null){atlas=new MoonAtlas(asset,columns,rows);CACHE.put(asset,atlas);}
        atlas.owners++;return atlas;
    }
    private MoonAtlas(String asset,int columns,int rows){
        this.asset=asset;texture=new Texture(Gdx.files.internal(asset));
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        regions=new TextureRegion[columns*rows];ink=new int[regions.length][4];
        Pixmap pixels=new Pixmap(Gdx.files.internal(asset));
        try {
            for(int i=0;i<regions.length;i++){
                int col=i%columns,row=i/columns;
                int x=col*pixels.getWidth()/columns,y=row*pixels.getHeight()/rows;
                int w=(col+1)*pixels.getWidth()/columns-x,h=(row+1)*pixels.getHeight()/rows-y;
                regions[i]=new TextureRegion(texture,x,y,w,h);
                int left=w,top=h,right=-1,bottom=-1;
                for(int py=0;py<h;py++)for(int px=0;px<w;px++)if((pixels.getPixel(x+px,y+py)&255)>96){
                    left=Math.min(left,px);top=Math.min(top,py);right=Math.max(right,px);bottom=Math.max(bottom,py);
                }
                if(right<left)throw new IllegalArgumentException("Empty art cell "+asset+" #"+i);
                ink[i]=new int[]{left,top,right+1,bottom+1};
            }
        }finally{pixels.dispose();}
    }
    float sourceHeight(int index){return ink[index][3]-ink[index][1];}
    void drawRotated(SpriteBatch batch,int index,float cx,float cy,float ax,float ay,float scale,float angle){
        TextureRegion r=regions[index];float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        float ox=ax*scale,oy=(r.getRegionHeight()-ay)*scale;
        batch.draw(r,cx-ox,cy-oy,ox,oy,w,h,1,1,angle);
    }
    void drawPivoted(SpriteBatch batch,int index,float cx,float cy,float ax,float ay,float scale,boolean right){
        TextureRegion r=regions[index];float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        batch.draw(r,right?cx-ax*scale:cx+ax*scale,cy-(r.getRegionHeight()-ay)*scale,right?w:-w,h);
    }
    void draw(SpriteBatch batch,int index,int reference,float center,float feet,float height,boolean right){
        TextureRegion r=regions[index];float scale=height/sourceHeight(reference);
        float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        float x=center-w/2,y=feet-(r.getRegionHeight()-ink[index][3])*scale;
        batch.draw(r,right?x:x+w,y,right?w:-w,h);
    }
    void drawCrouch(SpriteBatch batch,int index,float center,float feet,float standingHeight,float crouchHeight,boolean right){
        TextureRegion r=regions[index];float sx=standingHeight/sourceHeight(0),sy=crouchHeight/sourceHeight(index);
        float w=r.getRegionWidth()*sx,h=r.getRegionHeight()*sy;
        float x=center-w/2,y=feet-(r.getRegionHeight()-ink[index][3])*sy;
        batch.draw(r,right?x:x+w,y,right?w:-w,h);
    }
    Texture croppedTexture(int index){
        TextureRegion r=regions[index];int[] b=ink[index];
        Pixmap source=new Pixmap(Gdx.files.internal(asset));
        Pixmap cut=new Pixmap(b[2]-b[0],b[3]-b[1],Pixmap.Format.RGBA8888);
        try{
            cut.setBlending(Pixmap.Blending.None);
            cut.drawPixmap(source,0,0,r.getRegionX()+b[0],r.getRegionY()+b[1],cut.getWidth(),cut.getHeight());
            Texture image=new Texture(cut);image.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return image;
        }finally{cut.dispose();source.dispose();}
    }
    int surfaceRow(int index){
        TextureRegion r=regions[index];int[] b=ink[index];int[] coverage=new int[b[3]-b[1]];
        Pixmap p=new Pixmap(Gdx.files.internal(asset));
        try{
            int most=0;
            for(int y=0;y<coverage.length;y++){
                for(int x=b[0];x<b[2];x++)if((p.getPixel(r.getRegionX()+x,r.getRegionY()+b[1]+y)&255)>96)coverage[y]++;
                most=Math.max(most,coverage[y]);
            }
            int y=0;while(y<coverage.length-1&&coverage[y]<most*.95f)y++;return y;
        }finally{p.dispose();}
    }
    @Override public void close(){
        if(--owners==0){texture.dispose();CACHE.remove(asset);}
    }
}
