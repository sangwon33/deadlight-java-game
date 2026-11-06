package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.StringReader;
import java.util.Properties;

/** Full original cells, shared textures and stable feet/bolt-core anchors. */
final class LanternSprites implements AutoCloseable {
    private final MoonAtlas actor,fx;
    private final float[][] feet=new float[8][2];
    private static final float[][] CORE={{277,264},{269,263},{260,261},{198,259},{136,256},{192,256},{192,256},{192,256}};
    private final float scale;
    LanternSprites(){this(MoonAtlas.acquire("art/enemy_lantern_marksman_v1.png",4,2),MoonAtlas.acquire("art/lantern_shot_fx_v1.png",4,2),Gdx.files.internal("art/lantern-runtime-v1.properties").readString("UTF-8"));}
    LanternSprites(MoonAtlas actor,MoonAtlas fx,String data){
        this.actor=actor;this.fx=fx;Properties p=new Properties();
        try{p.load(new StringReader(data));}catch(java.io.IOException e){throw new IllegalArgumentException("Lantern layout",e);}
        scale=Float.parseFloat(p.getProperty("scale"));
        for(int i=0;i<8;i++){String[] a=p.getProperty("feet."+i).split(",");feet[i][0]=Float.parseFloat(a[0]);feet[i][1]=Float.parseFloat(a[1]);}
    }
    void draw(SpriteBatch b,int frame,float cx,float y,boolean right,float flash,float alpha){
        float packed=b.getPackedColor();b.setColor(1,flash>0?.4f:1,flash>0?.45f:1,alpha);
        actor.drawPivoted(b,frame,cx,y,feet[frame][0],feet[frame][1],scale,right);b.setPackedColor(packed);
    }
    private void effect(SpriteBatch b,int role,float x,float y,float size,float angle,float alpha){
        b.setColor(1,1,1,alpha);fx.drawRotated(b,role,x,y,CORE[role][0],CORE[role][1],size,angle);
    }
    void drawEffects(SpriteBatch b,LanternMarksman guard){
        float packed=b.getPackedColor();
        if(guard.phase()==LanternMarksman.Phase.AIM){
            float direction=guard.facingRight()?1:-1;
            float offset=guard.progress()<.35f?9.6f:17;
            effect(b,3,guard.cx()+direction*offset,guard.body.y+36,.045f+.025f*guard.progress(),0,.5f+.5f*guard.progress());
        }
        for(LanternMarksman.Shot s:guard.shots){
            int frame=(int)((LanternMarksman.SHOT_LIFE-s.life)/.09f)%3;
            effect(b,frame,s.x,s.y,.085f,(float)Math.toDegrees(Math.atan2(s.vy,s.vx)),1);
        }
        for(LanternMarksman.Flash f:guard.flashes){
            int role=f.muzzle?4:5+Math.min(2,(int)(f.age/.1f));
            effect(b,role,f.x,f.y,f.muzzle?.09f:.12f,f.angle,f.muzzle?1-f.age/.14f:1-f.age/.3f);
        }
        b.setPackedColor(packed);
    }
    @Override public void close(){actor.close();fx.close();}
}
