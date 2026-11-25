package com.example.pixelgame;

import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Generated atlas integration: keep alpha, share textures, anchor each pose to its own feet. */
final class MoonAtlas implements AutoCloseable {
    private static final HashMap<String,MoonAtlas> CACHE = new HashMap<>();
    private final String asset;
    private final String cacheKey;
    private final Texture texture;
    private final TextureRegion[] regions;
    private final TextureRegion[] renderRegions;
    private final TextureRegion[] sampleRegions;
    private final int[][] ink;
    private int owners;
    static MoonAtlas acquire(String asset,int columns,int rows) {
        return acquire(asset,columns,rows,.5f);
    }
    static MoonAtlas acquire(String asset,int columns,int rows,float rowSplit) {
        MoonAtlas atlas=CACHE.get(asset);
        if(atlas==null){atlas=new MoonAtlas(asset,columns,rows,rowSplit);CACHE.put(asset,atlas);}
        atlas.owners++;return atlas;
    }
    /** Isolate one contaminated actor frame without rewriting or scaling its source artwork. */
    static MoonAtlas acquireIsolated(String asset,int columns,int rows,int frame) {
        if(frame<0||frame>=columns*rows)throw new IllegalArgumentException("Invalid actor frame: "+frame);
        String key=asset+"#isolated:"+frame;
        MoonAtlas atlas=CACHE.get(key);
        if(atlas==null){atlas=new MoonAtlas(asset,columns,rows,.5f,frame,key);CACHE.put(key,atlas);}
        atlas.owners++;return atlas;
    }
    private MoonAtlas(String asset,int columns,int rows,float rowSplit){
        this(asset,columns,rows,rowSplit,-1,asset);
    }
    private MoonAtlas(String asset,int columns,int rows,float rowSplit,int isolatedFrame,String cacheKey){
        this.asset=asset;this.cacheKey=cacheKey;
        texture=new Texture(Gdx.files.internal(asset));
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        regions=new TextureRegion[columns*rows];ink=new int[regions.length][4];
        renderRegions=new TextureRegion[regions.length];
        sampleRegions=new TextureRegion[regions.length];
        Pixmap pixels=new Pixmap(Gdx.files.internal(asset));
        try {
            for(int i=0;i<regions.length;i++){
                int col=i%columns,row=i/columns;
                int x=col*pixels.getWidth()/columns,y=row*pixels.getHeight()/rows;
                int cellEnd=(row+1)*pixels.getHeight()/rows;
                if(rows==2&&rowSplit!=.5f){int split=Math.round(pixels.getHeight()*rowSplit);y=row==0?0:split;cellEnd=row==0?split:pixels.getHeight();}
                int w=(col+1)*pixels.getWidth()/columns-x,h=cellEnd-y;
                regions[i]=new TextureRegion(texture,x,y,w,h);
                int left=w,top=h,right=-1,bottom=-1;
                for(int py=0;py<h;py++)for(int px=0;px<w;px++)if((pixels.getPixel(x+px,y+py)&255)>96){
                    left=Math.min(left,px);top=Math.min(top,py);right=Math.max(right,px);bottom=Math.max(bottom,py);
                }
                if(right<left)throw new IllegalArgumentException("Empty art cell "+asset+" #"+i);
                ink[i]=new int[]{left,top,right+1,bottom+1};
                if(i==isolatedFrame){
                    int[] body=dominantInk(pixels,x,y,w,h);ink[i]=body;
                    // The 0.8-scale actor needs six source pixels for its screen-space outline.
                    int clipX=Math.max(0,body[0]-6),clipY=Math.max(0,body[1]-6);
                    int clipRight=Math.min(w,body[2]+6),clipBottom=Math.min(h,body[3]+6);
                    renderRegions[i]=new TextureRegion(texture,x+clipX,y+clipY,clipRight-clipX,clipBottom-clipY);
                }else renderRegions[i]=regions[i];
                sampleRegions[i]=renderRegions[i];
                if(isolatedFrame>=0){
                    TextureRegion r=renderRegions[i];
                    // Keep geometry separate from UVs; inset UVs must not shrink the actor.
                    sampleRegions[i]=new TextureRegion(texture,r.getU()+.5f/texture.getWidth(),
                            r.getV()+.5f/texture.getHeight(),r.getU2()-.5f/texture.getWidth(),r.getV2()-.5f/texture.getHeight());
                }
            }
        }finally{pixels.dispose();}
    }

    static int[] dominantInk(Pixmap pixels,int sx,int sy,int w,int h){
        boolean[] seen=new boolean[w*h];int[] queue=new int[w*h];
        int largest=0;int[] result=null;
        for(int start=0;start<seen.length;start++){
            if(seen[start]||(pixels.getPixel(sx+start%w,sy+start/w)&255)<16)continue;
            int head=0,tail=1,left=w,top=h,right=-1,bottom=-1;queue[0]=start;seen[start]=true;
            while(head<tail){
                int p=queue[head++],x=p%w,y=p/w;
                left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);
                for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){
                    int nx=x+dx,ny=y+dy;
                    if(nx<0||ny<0||nx>=w||ny>=h)continue;
                    int next=ny*w+nx;
                    if(!seen[next]&&(pixels.getPixel(sx+nx,sy+ny)&255)>=16){seen[next]=true;queue[tail++]=next;}
                }
            }
            if(tail>largest){largest=tail;result=new int[]{left,top,right+1,bottom+1};}
        }
        if(result==null)throw new IllegalArgumentException("Actor frame has no visible body");
        return result;
    }
    float sourceHeight(int index){return ink[index][3]-ink[index][1];}
    Rectangle inkBounds(int index,int reference,float center,float feet,float height,boolean right,Rectangle out){
        float scale=height/sourceHeight(reference);
        TextureRegion r=regions[index];int[] bounds=ink[index];
        float left=right?bounds[0]-r.getRegionWidth()/2f:r.getRegionWidth()/2f-bounds[2];
        return out.set(center+left*scale,feet,(bounds[2]-bounds[0])*scale,sourceHeight(index)*scale);
    }
    void drawRotated(SpriteBatch batch,int index,float cx,float cy,float ax,float ay,float scale,float angle){
        TextureRegion r=renderRegions[index],original=regions[index];float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        float ox=(ax-r.getRegionX()+original.getRegionX())*scale;
        float oy=(r.getRegionHeight()+r.getRegionY()-original.getRegionY()-ay)*scale;
        batch.draw(sampleRegions[index],cx-ox,cy-oy,ox,oy,w,h,1,1,angle);
    }
    void drawPivoted(SpriteBatch batch,int index,float cx,float cy,float ax,float ay,float scale,boolean right){
        TextureRegion r=renderRegions[index],original=regions[index];float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        float pivotX=(ax-r.getRegionX()+original.getRegionX())*scale;
        float pivotY=(r.getRegionHeight()+r.getRegionY()-original.getRegionY()-ay)*scale;
        batch.draw(sampleRegions[index],right?cx-pivotX:cx+pivotX,cy-pivotY,right?w:-w,h);
    }
    void draw(SpriteBatch batch,int index,int reference,float center,float feet,float height,boolean right){
        TextureRegion r=renderRegions[index],original=regions[index];float scale=height/sourceHeight(reference);
        float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        float x=center+(r.getRegionX()-original.getRegionX()-original.getRegionWidth()/2f)*scale;
        float y=feet+(ink[index][3]-(r.getRegionY()-original.getRegionY())-r.getRegionHeight())*scale;
        batch.draw(sampleRegions[index],right?x:2*center-x,y,right?w:-w,h);
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
        if(--owners==0){texture.dispose();CACHE.remove(cacheKey);}
    }
}
