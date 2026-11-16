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
        b.setColor(.36f,.43f,.51f,.62f);
        for(Rectangle platform:StageLayout.platforms(stage)){
            if(platform.y<80||Math.abs(platform.x+platform.width/2-cameraX)>450)continue;
            float depth=Math.min(190,Math.max(65,platform.y-20));
            if((int)platform.x%3==0){float chain=Math.min(depth,platform.width*.85f);world.draw(b,6,6,platform.x+platform.width/2,platform.y+18-chain,chain,true);}
            else for(float px:new float[]{platform.x+7,platform.x+platform.width-23})stone.draw(b,px,platform.y,15,depth);
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
