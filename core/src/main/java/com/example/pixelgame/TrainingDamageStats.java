package com.example.pixelgame;

import java.util.ArrayDeque;
import java.util.Locale;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Accepted hit damage, including overkill; DPS is a rolling three-second average. */
final class TrainingDamageStats {
    private record Hit(double time,int damage){}
    private final ArrayDeque<Hit> hits=new ArrayDeque<>();
    private double time;
    private int last,total,recent;
    void reset(){hits.clear();time=0;last=total=recent=0;}
    void update(float dt){
        time+=Math.max(0,dt);
        while(!hits.isEmpty()&&time-hits.peekFirst().time>=3){recent-=hits.removeFirst().damage;}
    }
    void hit(int damage){if(damage<=0)return;last=damage;total+=damage;recent+=damage;hits.addLast(new Hit(time,damage));}
    int last(){return last;}
    int total(){return total;}
    float dps(){return recent/3f;}
    void draw(SpriteBatch b,UiTheme ui,BitmapFont font,Enemy dummy,Player player){
        if(dummy==null||Math.abs(player.getCenterX()-dummy.getCenterX())>340||Math.abs(player.getY()-dummy.getY())>200
                ||player.isLookingDown()||player.isLookingUp())return;
        Rectangle r=new Rectangle(dummy.getCenterX()+40,dummy.getY()+16,166,64);
        ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.SURFACE,.82f);ui.panel(b,r.x,r.y,r.width,r.height);
        ui.text(b,font,"직전 타격 데미지 : "+last,r.x+9,r.y+49,.59f,UiTheme.TEXT,r.width-18);
        ui.text(b,font,String.format(Locale.ROOT,"DPS : %.1f (3초 평균)",dps()),r.x+9,r.y+32,.59f,UiTheme.GOLD,r.width-18);
        ui.text(b,font,"Total : "+total,r.x+9,r.y+15,.59f,UiTheme.TEXT,r.width-18);
    }
}
