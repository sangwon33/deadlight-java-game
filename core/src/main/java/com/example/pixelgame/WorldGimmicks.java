package com.example.pixelgame;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Deterministic region mechanisms; reset together with the region-entry checkpoint. */
final class WorldGimmicks {
    static final class Lift {
        final Rectangle bounds;final float low,high,speed;float direction=1;
        Lift(float x,float low,float high,float speed){bounds=new Rectangle(x,low-18,150,18);this.low=low;this.high=high;this.speed=speed;}
    }
    static final class Wind {
        final float x,floor,speed;float cooldown;
        Wind(float x,float floor,float speed){this.x=x;this.floor=floor;this.speed=speed;}
    }
    static final class Gate {
        final Rectangle bounds;final float leverX,floor;boolean open;
        Gate(float x,float floor){bounds=new Rectangle(x,floor,28,130);leverX=x-105;this.floor=floor;}
    }
    final List<Lift> lifts=new ArrayList<>();
    final List<Wind> winds=new ArrayList<>();
    final List<Gate> gates=new ArrayList<>();
    final List<Rectangle> spikes=new ArrayList<>();
    private float time;private boolean openedNotice;
    void enter(int stage,ArrayList<Rectangle> platforms,ArrayList<Rectangle> solids){
        lifts.clear();winds.clear();gates.clear();spikes.clear();time=0;openedNotice=false;
        if(stage<1||stage>3)return;
        lifts.add(new Lift(1680,-140,510,180));lifts.add(new Lift(3640,60,800,200));
        for(Lift lift:lifts)platforms.add(lift.bounds);
        winds.add(new Wind(645,60,1020));winds.add(new Wind(1610,-160,1120));
        winds.add(new Wind(2260,60,1450));winds.add(new Wind(2970+(stage-1)*25,860,1200));
        Gate gate=new Gate(2960,60);gates.add(gate);solids.add(gate.bounds);
        spikes.add(new Rectangle(1060,-159,65,14));
        spikes.add(new Rectangle(2110,61,85,14));
        spikes.add(new Rectangle(3500+(stage-1)*80,61,110,14));
    }
    void beforePhysics(float dt,Player p){
        time+=dt;
        for(Lift lift:lifts){
            float old=lift.bounds.y,top=old+18;
            top+=lift.direction*lift.speed*dt;
            if(top>lift.high){top=lift.high-(top-lift.high);lift.direction=-1;}
            if(top<lift.low){top=lift.low+(lift.low-top);lift.direction=1;}
            lift.bounds.y=top-18;
            if(p.isGrounded()&&p.getSupport()==lift.bounds&&!p.wantsDown())p.carry(0,lift.bounds.y-old);
        }
        for(Wind wind:winds)wind.cooldown=Math.max(0,wind.cooldown-dt);
        if(KeyBindings.justPressed(KeyBindings.Action.INTERACT))for(Gate gate:gates){
            if(!gate.open&&nearLever(p,gate)){gate.open=true;gate.bounds.width=0;openedNotice=true;break;}
        }
    }
    void afterPhysics(Player p){
        for(Wind wind:winds)if(wind.cooldown<=0&&!p.isCrouching()&&p.isGrounded()
                &&Math.abs(p.getY()-wind.floor)<3&&Math.abs(p.getCenterX()-wind.x)<28){
            p.launch(wind.speed);wind.cooldown=.65f;
        }
        for(Rectangle spike:spikes)if(p.isAlive()&&p.getBounds().overlaps(spike))p.takeDamage(GameBalance.ENEMY_DAMAGE,spike.x+spike.width/2);
    }
    private boolean nearLever(Player p,Gate gate){return Math.abs(p.getCenterX()-gate.leverX)<80&&Math.abs(p.getY()-gate.floor)<55;}
    boolean takeOpenedNotice(){boolean result=openedNotice;openedNotice=false;return result;}
    void draw(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,TerrainTile stone){
        Color cyan=UiTheme.GOLD;
        for(Lift lift:lifts){
            float cx=lift.bounds.x+lift.bounds.width/2,top=lift.bounds.y+18;
            ui.line(b,cx,lift.low-18,cx,lift.high+22,.6f,UiTheme.BORDER,.45f);
            ui.diamond(b,cx,top-8,3,cyan,.9f);
        }
        for(Wind wind:winds){
            float pulse=.55f+.2f*(float)Math.sin(time*3);
            ui.line(b,wind.x-26,wind.floor+1,wind.x+26,wind.floor+1,2,cyan,.9f);
            ui.diamond(b,wind.x,wind.floor+7,4,cyan,1);
            for(int i=0;i<5;i++){
                float y=wind.floor+12+(time*48+i*15)%75;
                float x=wind.x+(float)Math.sin(time+i*2)*15;
                ui.line(b,x,y,x,y+8,.65f,cyan,pulse*(1-(y-wind.floor)/105));
            }
        }
        for(Rectangle spike:spikes)for(float x=spike.x;x<spike.x+spike.width-3;x+=10){
            float w=Math.min(9,spike.x+spike.width-x),tip=spike.y+spike.height;
            ui.triangle(b,x,spike.y,x+w/2,tip,x+w,spike.y,UiTheme.SURFACE,1);
            ui.triangle(b,x,spike.y,x+w/2,tip,x+w/2,spike.y,UiTheme.BORDER,1);
            ui.line(b,x,spike.y,x+w/2,tip,.9f,Color.SALMON,.85f);
            ui.line(b,x+w/2,tip,x+w,spike.y,.9f,Color.SALMON,.85f);
        }
        for(Gate gate:gates){
            float x=gate.bounds.x,y=gate.floor;
            if(!gate.open){
                stone.draw(b,x,y+65,28,65);stone.draw(b,x,y+130,28,65);
                ui.rect(b,x,y,28,130,UiTheme.SURFACE,.35f);
                ui.line(b,x,y,x,y+130,1,cyan,.8f);ui.line(b,x+28,y,x+28,y+130,1,cyan,.8f);
                for(int i=0;i<5;i++)ui.diamond(b,x+14,y+14+i*24,6,cyan,.75f);
            }else{
                ui.line(b,x,y,x,y+16,1,cyan,.5f);ui.line(b,x+28,y,x+28,y+16,1,cyan,.5f);
            }
            ui.rect(b,gate.leverX-7,y,14,10,UiTheme.SURFACE,1);
            ui.line(b,gate.leverX,y+10,gate.leverX+(gate.open?7:-7),y+24,2,cyan,1);
            ui.diamond(b,gate.leverX+(gate.open?7:-7),y+24,2,cyan,1);
            if(!gate.open&&nearLever(p,gate)){
                Rectangle label=new Rectangle(gate.leverX-53,y+84,106,20);
                ui.panel(b,label.x,label.y,label.width,label.height);
                ui.centered(b,font,"["+KeyBindings.name(KeyBindings.Action.INTERACT)+"] 석문 열기",label,.58f,UiTheme.TEXT);
            }
        }
    }
    void drawMap(SpriteBatch b,UiTheme ui,BitmapFont font,int stage,Player p){
        if(stage<1||stage>3)return;
        float x=70,y=58,w=500,h=244,worldW=StageLayout.width(stage),low=StageLayout.bottom(stage),range=StageLayout.top(stage)-low;
        ui.rect(b,0,0,640,360,UiTheme.SURFACE,.86f);ui.panel(b,x,y,w,h);
        ui.text(b,font,StageLayout.name(stage)+" · 탐색 지도",x+15,y+h-20,.82f,UiTheme.TEXT,460);
        float bx=x+15,by=y+28,bw=w-30,bh=h-72;
        for(Rectangle r:StageLayout.floors(stage))ui.rect(b,bx+r.x/worldW*bw,by+(r.y+r.height-low)/range*bh,Math.max(1,r.width/worldW*bw),2,UiTheme.BORDER,1);
        for(Rectangle r:StageLayout.platforms(stage))ui.rect(b,bx+r.x/worldW*bw,by+(r.y+r.height-low)/range*bh,Math.max(1,r.width/worldW*bw),1,UiTheme.GOLD,.65f);
        for(Lift lift:lifts)ui.line(b,bx+(lift.bounds.x+75)/worldW*bw,by+(lift.low-low)/range*bh,bx+(lift.bounds.x+75)/worldW*bw,by+(lift.high-low)/range*bh,.7f,UiTheme.SUCCESS,.8f);
        for(Wind wind:winds)ui.diamond(b,bx+wind.x/worldW*bw,by+(wind.floor-low)/range*bh,2,UiTheme.SUCCESS,1);
        for(Gate gate:gates)ui.diamond(b,bx+gate.bounds.x/worldW*bw,by+(gate.floor-low)/range*bh,3,gate.open?UiTheme.SUCCESS:Color.SALMON,1);
        if(stage==2){
            float tx=bx+1750/worldW*bw,ty=by+(Lamplighter.FLOOR-low)/range*bh;
            ui.diamond(b,tx,ty+4,4,UiTheme.GOLD,1);
            ui.text(b,font,"선택 · 등불지기",tx-43,ty+21,.43f,UiTheme.TEXT,110);
        }
        SideBossArena arena=SideBossArena.forStage(stage);
        if(arena!=null){
            float tx=bx+arena.entry()/worldW*bw,ty=by+(arena.floor()-low)/range*bh;
            ui.diamond(b,tx,ty+4,4,UiTheme.GOLD,1);
            ui.text(b,font,arena.roots()?"선택 · 순례자":"선택 · 봉합자",tx-42,ty+20,.43f,UiTheme.TEXT,110);
        }
        ui.diamond(b,bx+p.getCenterX()/worldW*bw,by+(p.getY()-low)/range*bh,4,UiTheme.TEXT,1);
        ui.text(b,font,"출구",bx+bw-23,by+(60-low)/range*bh+12,.45f,UiTheme.GOLD,30);
        ui.text(b,font,"◇ 현재 위치    초록: 바람 / 승강기    붉은색: 닫힌 문",x+15,y+13,.52f,UiTheme.MUTED,470);
        ui.centered(b,font,"Tab을 놓으면 닫힙니다 · 지도를 보는 동안 일시정지",new Rectangle(x,y-25,w,18),.52f,UiTheme.MUTED);
    }
}
