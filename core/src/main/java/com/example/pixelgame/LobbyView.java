package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Floating title choices leave the cathedral and wanderer visible. */
final class LobbyView {
    static Rectangle bounds(float left,int row){return new Rectangle(left+46,199-row*37,232,30);}
    private static final String[] CHOICES={"여정 시작","지역 선택","설정","게임 종료"};
    void draw(SpriteBatch b,BitmapFont f,UiTheme ui,float left,int selected,boolean pressed,float time,ClearRecords.Summary record){
        ui.text(b,f,"D E A D L I G H T",left+35,321,1.65f,UiTheme.TEXT,295);
        ui.centered(b,f,"빛을 잃은 왕국",new Rectangle(left+46,272,232,22),.65f,UiTheme.GOLD);
        ui.ornament(b,left+162,261,220);
        ui.centered(b,f,"잊힌 길 끝에서, 마지막 빛을 찾아.",new Rectangle(left+36,234,252,18),.52f,UiTheme.MUTED);
        for(int i=0;i<4;i++)ui.menuChoice(b,f,bounds(left,i),CHOICES[i],i==selected,pressed,i==0?.98f:.83f);
        ui.centered(b,f,"↑ ↓ 선택     Enter 결정     마우스 클릭",new Rectangle(left+25,57,274,17),.47f,UiTheme.MUTED);
        ui.ornament(b,left+162,50,150);
        String value=record.exists()?"최고 "+JourneyView.time(record.bestSeconds())+"   ·   "+record.clears()+"회 클리어":"아직 기록되지 않은 여정";
        ui.centered(b,f,value,new Rectangle(left+26,22,272,18),.5f,UiTheme.MUTED);
        for(int i=0;i<18;i++){
            float x=left+330+(i*61.7f+time*(1+i%2))%300;
            float y=80+(i*43.1f)%260+(float)Math.sin(time*.45f+i)*8;
            ui.rect(b,x,y,.7f,.7f,UiTheme.TEXT,.14f+.1f*(float)Math.sin(time+i));
        }
    }
}
