package com.example.pixelgame;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

final class PauseView implements Disposable {
    private final UiTheme ui=new UiTheme();
    static Rectangle buttonBounds(float left,int row){return new Rectangle(left+207,200-row*36,226,28);}
    private void icon(SpriteBatch b,float x,float y,int type){
        Color c=UiTheme.GOLD;
        if(type==0){ui.line(b,x-3,y-4,x+4,y,.7f,c,1);ui.line(b,x+4,y,x-3,y+4,.7f,c,1);ui.line(b,x-3,y+4,x-3,y-4,.7f,c,1);}
        else if(type==1||type==2){
            int count=type==1?14:8;float span=type==1?300:360;
            for(int i=0;i<count;i++){double a=Math.toRadians(i*span/count),d=Math.toRadians((i+1)*span/count);ui.line(b,x+(float)Math.cos(a)*4,y+(float)Math.sin(a)*4,x+(float)Math.cos(d)*4,y+(float)Math.sin(d)*4,.65f,c,1);}
            if(type==1)ui.line(b,x+4,y,x+6,y+2,.7f,c,1);else ui.diamond(b,x,y,1.4f,c,1);
        }else if(type==3){ui.line(b,x-5,y,x,y+4,.7f,c,1);ui.line(b,x,y+4,x+5,y,.7f,c,1);ui.line(b,x-3,y-4,x-3,y+1,.7f,c,1);ui.line(b,x+3,y-4,x+3,y+1,.7f,c,1);ui.line(b,x-3,y-4,x+3,y-4,.7f,c,1);}
        else if(type==4){ui.line(b,x-3,y-3,x+3,y+3,.7f,c,1);ui.line(b,x-3,y+3,x+3,y-3,.7f,c,1);}
        else {ui.line(b,x-4,y,x-1,y-3,.7f,c,1);ui.line(b,x-1,y-3,x+4,y+4,.7f,c,1);}
    }
    void draw(SpriteBatch b,BitmapFont f,float left,int pending,int hovered,boolean pressed){
        ui.rect(b,left,0,640,360,UiTheme.SURFACE,.74f);
        ui.panel(b,left+177,48,286,273);
        ui.centered(b,f,"잠시, 숨을 고르세요",new Rectangle(left+185,285,270,18),.5f,UiTheme.MUTED);
        ui.centered(b,f,pending==0?"일시정지":pending==1?"새로 시작할까요?":"로비로 돌아갈까요?",new Rectangle(left+185,251,270,28),1.05f,UiTheme.TEXT);
        ui.ornament(b,left+320,237,170);
        String[] labels=pending==0?new String[]{"계속하기","처음부터 다시","설정","메인 메뉴"}:new String[]{"취소","확인"};
        for(int i=0;i<labels.length;i++){
            Rectangle r=buttonBounds(left,i);ui.menuChoice(b,f,r,"",hovered==i,pressed,.85f);
            icon(b,r.x+21,r.y+r.height/2,pending==0?i:i+4);
            ui.centered(b,f,labels[i],new Rectangle(r.x+36,r.y,r.width-42,r.height),.8f,UiTheme.TEXT);
        }
        ui.centered(b,f,pending==0?"Esc / Enter · 계속하기":"Esc · 취소    Enter · 확인",new Rectangle(left+190,61,260,16),.5f,UiTheme.MUTED);
    }
    @Override public void dispose(){ui.dispose();}
}
