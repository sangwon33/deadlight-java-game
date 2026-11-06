package com.example.pixelgame;

import java.nio.IntBuffer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.BufferUtils;

/** Shared enemy preparation/opening cues. Player skill sprites remain effect-free. */
final class EnemyAttackView {
    private static final Color READY=new Color(1,.73f,.34f,1), OPEN=new Color(.38f,.9f,1,1);
    private final IntBuffer value=BufferUtils.newIntBuffer(1);
    private int gl(int name){value.clear();Gdx.gl.glGetIntegerv(name,value);return value.get(0);}
    void draw(ShapeRenderer s,Iterable<Enemy> enemies,FallenGod boss){
        boolean blend=Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        int src=gl(GL20.GL_BLEND_SRC_RGB),dst=gl(GL20.GL_BLEND_DST_RGB),sa=gl(GL20.GL_BLEND_SRC_ALPHA),da=gl(GL20.GL_BLEND_DST_ALPHA);
        Color old=s.getColor();float r=old.r,g=old.g,b=old.b,a=old.a;
        Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        try{
            s.begin(ShapeRenderer.ShapeType.Filled);
            for(Enemy enemy:enemies){
                if(enemy.getHealth()<=0)continue;
                if(enemy.isAttackCueVisible())range(s,enemy.getAttackBounds(),enemy.getBounds(),enemy.getAttackCueProgress());
                else if(enemy.isRecovering())opening(s,enemy.getBounds(),enemy.getRecoveryProgress());
            }
            if(boss!=null&&boss.isAlive()){
                if(boss.isWindingUp())range(s,boss.getAttackBounds(),boss.getBounds(),boss.getAttackCueProgress());
                else if(boss.isRecovering())opening(s,boss.getBounds(),boss.getRecoveryProgress());
            }
            s.end();
        }finally{
            if(s.isDrawing())s.end();Gdx.gl.glBlendFuncSeparate(src,dst,sa,da);
            if(!blend)Gdx.gl.glDisable(GL20.GL_BLEND);s.setColor(r,g,b,a);
        }
    }
    private void range(ShapeRenderer s,Rectangle strike,Rectangle body,float progress){
        float tip=strike.x>=body.x+body.width/2?strike.x+strike.width:strike.x;
        float y=Math.max(strike.y+2,body.y+3);
        s.setColor(.025f,.02f,.02f,.8f);s.rect(strike.x,y-1,strike.width,3);
        s.setColor(READY.r,READY.g,READY.b,.4f+.5f*progress);s.rect(strike.x,y,strike.width,1);
        s.rect(tip,y,1,6);
    }
    private void opening(ShapeRenderer s,Rectangle body,float progress){
        s.setColor(.015f,.03f,.045f,.85f);s.rect(body.x-1,body.y+body.height+16,body.width+2,4);
        s.setColor(OPEN);s.rect(body.x,body.y+body.height+17,body.width*Math.max(0,Math.min(1,progress)),2);
    }
    void labels(SpriteBatch b,BitmapFont f,UiTheme ui,Iterable<Enemy> enemies,FallenGod boss){
        for(Enemy enemy:enemies){
            if(enemy.getHealth()<=0)continue;
            if(enemy.isRecovering())tag(b,f,ui,enemy.getBounds(),"빈틈 · 반격",OPEN);
            else if(enemy.isAttackCueVisible())tag(b,f,ui,enemy.getBounds(),enemy.isRanged()?"! 사격 준비":"! 공격 준비",READY);
            else if(enemy.isGuardian())tag(b,f,ui,enemy.getBounds(),"수문장",UiTheme.GOLD);
        }
        if(boss!=null&&boss.isAlive()){
            if(boss.isRecovering())tag(b,f,ui,boss.getBounds(),"빈틈 · 반격",OPEN);
            else if(boss.isWindingUp())tag(b,f,ui,boss.getBounds(),boss.cueName(),READY);
        }
    }
    private void tag(SpriteBatch b,BitmapFont f,UiTheme ui,Rectangle body,String text,Color color){
        Rectangle box=new Rectangle(body.x+body.width/2-40,body.y+body.height+23,80,16);
        ui.rect(b,box.x,box.y,box.width,box.height,UiTheme.SURFACE,.88f);
        ui.centered(b,f,text,box,.53f,color);
    }
}
