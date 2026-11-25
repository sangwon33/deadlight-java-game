package com.example.pixelgame;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Painted landmarks behind collision surfaces, deliberately softer than the actors and ground. */
final class StageScenery implements AutoCloseable {
    private MoonAtlas props;
    void draw(SpriteBatch b,UiTheme ui,int stage,float cameraX,float time,MoonAtlas world,TerrainTile stone){
        if(stage<0||stage>3)return;
        if(props==null)props=MoonAtlas.acquire("art/ruin-props.png",2,2);
        float packed=b.getPackedColor();int i=Math.max(0,stage-1);
        b.setColor(.63f,.72f,.79f,.68f);
        for(Rectangle floor:StageLayout.floors(stage)){
            float y=floor.y+floor.height;
            if(Math.abs(y-60)>.01f&&Math.abs(y+160)>.01f)continue;
            for(float x=floor.x+95;x<floor.x+floor.width-45;x+=155){
                int frame=i++%4;
                if(Math.abs(x-cameraX)>450)continue;
                float height=frame==0?195:frame==1?122:frame==2?58:105;
                props.draw(b,frame,frame,x,y,height,true);
            }
        }
        // Balconies sit on masonry columns/corbels anchored to actual lower surfaces.
        // These remain background decoration, so one-way traversal stays unchanged.
        b.setColor(.48f,.55f,.61f,.76f);
        java.util.List<Rectangle> floors=StageLayout.floors(stage),ledges=StageLayout.platforms(stage);
        for(Rectangle platform:ledges){
            if(platform.y<80||Math.abs(platform.x+platform.width/2-cameraX)>450)continue;
            for(float px:new float[]{platform.x+12,platform.x+platform.width-26}){
                float base=StageLayout.bottom(stage),top=platform.y;
                for(Rectangle r:floors)if(px>=r.x&&px+14<=r.x+r.width&&r.y+r.height<top)base=Math.max(base,r.y+r.height);
                for(Rectangle r:ledges)if(r!=platform&&px>=r.x&&px+14<=r.x+r.width&&r.y+r.height<top)base=Math.max(base,r.y+r.height);
                // Short repeats preserve stone grain instead of stretching one long texture.
                if(top-base<165){
                    for(float y=top;y>base;y-=30)stone.draw(b,px,y,14,Math.min(30,y-base));
                    ui.line(b,px,base,px,top,.7f,UiTheme.BORDER,.35f);
                }else{
                    float anchor=StageLayout.top(stage);
                    for(Rectangle r:ledges)if(r!=platform&&px+7>=r.x&&px+7<=r.x+r.width&&r.y>top)anchor=Math.min(anchor,r.y);
                    for(Rectangle r:floors)if(px+7>=r.x&&px+7<=r.x+r.width&&r.y>top)anchor=Math.min(anchor,r.y);
                    for(float cy=top+8;cy<anchor;cy+=8){
                        ui.line(b,px+6,cy,px+6,cy+5,1.5f,UiTheme.SURFACE,.7f);
                        ui.line(b,px+7,cy+1,px+7,cy+6,.65f,UiTheme.BORDER,.5f);
                    }
                }
                ui.triangle(b,px-7,top,px+21,top,px+7,top-20,UiTheme.BORDER,.37f);

            }
        }
        i=0;
        b.setColor(.59f,.68f,.76f,.57f);
        for(Rectangle r:StageLayout.platforms(stage)){
            int frame=(i++)%3==0?3:2;
            if(r.y<160||i%2==0||Math.abs(r.x+r.width/2-cameraX)>420)continue;
            props.draw(b,frame,frame,r.x+r.width*.68f,r.y+r.height,frame==3?65:38,true);
        }
        b.setPackedColor(packed);
    }
    @Override public void close(){if(props!=null){props.close();props=null;}}
}
