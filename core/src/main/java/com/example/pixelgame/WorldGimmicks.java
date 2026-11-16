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
    static final class Bridge {
        final Rectangle bounds;final float width;float stress,respawn;
        Bridge(Rectangle bounds){this.bounds=bounds;this.width=bounds.width;}
    }
    static final class Jet {
        final float x,floor,period;float phase;
        Jet(float x,float floor,float phase,float period){this.x=x;this.floor=floor;this.phase=phase;this.period=period;}
        boolean active(){return phase>=1.2f&&phase<1.85f;}
        boolean warning(){return phase>=.55f&&phase<1.2f;}
        Rectangle bounds(){return new Rectangle(x-21,floor,42,90);}
    }
    static final class Well {final float x,floor;boolean used;Well(float x,float floor){this.x=x;this.floor=floor;}}
    final List<Bridge> bridges=new ArrayList<>();
    final List<Jet> jets=new ArrayList<>();
    final List<Well> wells=new ArrayList<>();
    private boolean healedNotice;
    final List<Lift> lifts=new ArrayList<>();
    final List<Wind> winds=new ArrayList<>();
    final List<Gate> gates=new ArrayList<>();
    final List<Rectangle> spikes=new ArrayList<>();
    private float time;private boolean openedNotice;
    void enter(int stage,ArrayList<Rectangle> platforms,ArrayList<Rectangle> solids){
        lifts.clear();winds.clear();gates.clear();spikes.clear();bridges.clear();jets.clear();wells.clear();time=0;openedNotice=healedNotice=false;
        if(stage<1||stage>3)return;
        lifts.add(new Lift(StageLayout.x(stage,1560),-140,StageLayout.y(stage,510),155));
        lifts.add(new Lift(StageLayout.x(stage,3640),60,StageLayout.y(stage,800),170));
        for(Lift lift:lifts){lift.bounds.width=Math.max(90,StageLayout.x(stage,150));platforms.add(lift.bounds);}
        winds.add(new Wind(StageLayout.x(stage,645),60,870));winds.add(new Wind(StageLayout.x(stage,1610),-160,1050));
        winds.add(new Wind(StageLayout.x(stage,2260),60,1180));
        winds.add(new Wind(StageLayout.x(stage,2970+(stage-1)*25),StageLayout.y(stage,860),930));
        bridges.add(new Bridge(platforms.get(0)));bridges.add(new Bridge(platforms.get(1)));
        jets.add(new Jet(StageLayout.x(stage,3270),60,0,3.4f-(stage-1)*.25f));
        jets.add(new Jet(StageLayout.width(stage)-560,60,1.7f,3.7f-(stage-1)*.25f));
        wells.add(new Well(StageLayout.x(stage,3100),60));
        Gate gate=new Gate(StageLayout.x(stage,2960),60);gates.add(gate);solids.add(gate.bounds);
        spikes.add(new Rectangle(StageLayout.x(stage,1060),-159,46,14));
        spikes.add(new Rectangle(StageLayout.x(stage,2110),61,60,14));
        spikes.add(new Rectangle(StageLayout.x(stage,3500+(stage-1)*80),61,70,14));
    }
    void beforePhysics(float dt,Player p){
        time+=dt;
        for(Bridge bridge:bridges){
            if(bridge.respawn>0){bridge.respawn=Math.max(0,bridge.respawn-dt);if(bridge.respawn==0){bridge.bounds.width=bridge.width;bridge.stress=0;}}
            else if(p.isGrounded()&&p.getSupport()==bridge.bounds){bridge.stress+=dt;if(bridge.stress>=.85f){bridge.bounds.width=0;bridge.respawn=4;}}
            else bridge.stress=Math.max(0,bridge.stress-dt*.5f);
        }
        for(Jet jet:jets)jet.phase=(jet.phase+dt)%jet.period;
        if(KeyBindings.justPressed(KeyBindings.Action.INTERACT))for(Well well:wells){
            if(!well.used&&Math.abs(p.getCenterX()-well.x)<55&&Math.abs(p.getY()-well.floor)<45&&p.heal(30)){well.used=true;healedNotice=true;break;}
        }
        for(Lift lift:lifts){
            float old=lift.bounds.y,top=old+18;
            top+=lift.direction*lift.speed*dt;
            if(top>lift.high){top=lift.high-(top-lift.high);lift.direction=-1;}
            if(top<lift.low){top=lift.low+(lift.low-top);lift.direction=1;}
            lift.bounds.y=top-18;
            if(p.isGrounded()&&p.getSupport()==lift.bounds&&!p.wantsDropThrough())p.carry(0,lift.bounds.y-old);
        }
        for(Wind wind:winds)wind.cooldown=Math.max(0,wind.cooldown-dt);
        if(KeyBindings.justPressed(KeyBindings.Action.INTERACT))for(Gate gate:gates){
            if(!gate.open&&nearLever(p,gate)){gate.open=true;gate.bounds.width=0;openedNotice=true;break;}
        }
    }
    void afterPhysics(Player p){
        for(Jet jet:jets)if(jet.active()&&p.getBounds().overlaps(jet.bounds()))p.takeDamage(GameBalance.ENEMY_DAMAGE,jet.x);
        for(Wind wind:winds)if(wind.cooldown<=0&&!p.isCrouching()&&p.isGrounded()
                &&Math.abs(p.getY()-wind.floor)<3&&Math.abs(p.getCenterX()-wind.x)<28){
            p.launch(wind.speed);wind.cooldown=.65f;
        }
        for(Rectangle spike:spikes)if(p.isAlive()&&p.getBounds().overlaps(spike))p.takeDamage(GameBalance.ENEMY_DAMAGE,spike.x+spike.width/2);
    }
    private boolean nearLever(Player p,Gate gate){return Math.abs(p.getCenterX()-gate.leverX)<80&&Math.abs(p.getY()-gate.floor)<55;}
    boolean takeHealedNotice(){boolean value=healedNotice;healedNotice=false;return value;}
    boolean takeOpenedNotice(){boolean result=openedNotice;openedNotice=false;return result;}
    void draw(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,TerrainTile stone,MoonAtlas world){
        Color cyan=UiTheme.GOLD;
        for(Bridge bridge:bridges){
            float x=bridge.bounds.x,y=bridge.bounds.y+18;
            if(bridge.respawn>0){for(float px=x;px<x+bridge.width;px+=14)ui.line(b,px,y,Math.min(px+6,x+bridge.width),y,.6f,UiTheme.BORDER,.6f);continue;}
            Color color=bridge.stress>0?Color.SALMON:UiTheme.GOLD;
            ui.line(b,x,y-2,x+bridge.width,y-2,1.5f,color,.8f);
            for(float px=x+15;px<x+bridge.width;px+=24)ui.line(b,px,y-2,px+6,y-10,.9f,color,.6f);
            if(p.getSupport()==bridge.bounds)ui.centered(b,font,"무너지는 발판",new Rectangle(x,y+90,bridge.width,18),.61f,Color.SALMON);
        }
        for(Jet jet:jets){
            world.draw(b,7,7,jet.x,jet.floor,20,true);
            if(jet.warning()){
                float a=(jet.phase-.55f)/.65f;ui.diamond(b,jet.x,jet.floor+30,4+a*3,Color.SALMON,.45f+a*.4f);
                ui.line(b,jet.x-20,jet.floor+2,jet.x+20,jet.floor+2,1.5f,Color.SALMON,.9f);
                if(Math.abs(p.getCenterX()-jet.x)<100&&Math.abs(p.getY()-jet.floor)<90)ui.centered(b,font,"분출 예고",new Rectangle(jet.x-45,jet.floor+102,90,18),.63f,Color.SALMON);
            }
            if(jet.active())for(int i=0;i<7;i++){
                float x=jet.x+(i-3)*5f,h=60+22*(float)Math.sin(time*19+i);
                ui.line(b,x,jet.floor+4,x+(float)Math.sin(time*12+i)*5,jet.floor+h,2.1f,Color.SALMON,.55f);
                ui.line(b,x,jet.floor+4,x,jet.floor+h*.7f,.8f,UiTheme.TEXT,.7f);
            }
        }
        for(Well well:wells){
            float packed=b.getPackedColor();b.setColor(1,1,1,well.used?.46f:1);world.draw(b,5,5,well.x,well.floor,44,true);b.setPackedColor(packed);
            if(!well.used&&Math.abs(p.getCenterX()-well.x)<65&&Math.abs(p.getY()-well.floor)<50){
                String label=p.getHealth()<p.getMaxHealth()?"["+KeyBindings.name(KeyBindings.Action.INTERACT)+"] 회복 +30":"회복 우물 · 1회 사용";
                ui.centered(b,font,label,new Rectangle(well.x-70,well.floor+92,140,20),.65f,UiTheme.SUCCESS);
            }
        }
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
}
