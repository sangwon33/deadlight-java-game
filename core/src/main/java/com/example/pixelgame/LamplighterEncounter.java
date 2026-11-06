package com.example.pixelgame;

import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Stage-two-only side room: opt-in, sealed during combat, automatic one-time reward. */
final class LamplighterEncounter implements AutoCloseable {
    final Lamplighter boss=new Lamplighter();
    final Rectangle gate=new Rectangle(Lamplighter.RIGHT,Lamplighter.FLOOR,0,260);
    private LamplighterSprites sprites;
    LamplighterEncounter(List<Rectangle> solids){solids.add(gate);}
    private LamplighterSprites art(){if(sprites==null)sprites=new LamplighterSprites();return sprites;}
    boolean tryStart(Player p){
        if(!p.isAlive()||!boss.start(p.getBounds(),p.isGrounded()))return false;
        gate.width=20;p.clearActionInput();return true;
    }
    void update(float dt,Player p){
        boss.update(dt,p.getBounds(),p.isAlive());
        if(boss.takeHit())p.takeDamage(boss.attackDamage(),boss.hitOrigin());
        if(boss.defeated())gate.width=0;
    }
    int hit(Rectangle strike,int amount){int dealt=strike.overlaps(boss.body)?boss.damage(amount):0;if(boss.defeated())gate.width=0;return dealt;}
    void drawRoom(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,TerrainTile stone){
        if(p.getY()<650||p.getX()<600||p.getX()>2250)return;
        float floor=Lamplighter.FLOOR;
        // Draw the complete collision wall, including its lower portion.
        for(float y=floor;y<1280;y+=65)stone.draw(b,1000,y+65,20,65);
        for(float x=1060;x<1840;x+=180){
            ui.line(b,x,floor+15,x,1265,.6f,UiTheme.BORDER,.3f);
            ui.diamond(b,x,1247,6,UiTheme.GOLD,.35f);
        }
        ui.line(b,1030,floor+3,1840,floor+3,.7f,UiTheme.GOLD,.6f);
        // A permanent wall inscription shows the reward before committing to the duel.
        if(!boss.active()){
            Rectangle title=new Rectangle(1515,floor+168,310,22);
            ui.centered(b,font,boss.defeated()?"등불이 잠든 감시탑":"꺼지지 않는 등불지기",title,.7f,UiTheme.TEXT);
            ui.centered(b,font,boss.defeated()?"시련 완료 · 출구가 열렸습니다":"선택 시련 · 골드 80 / 공격력 +1 / 회복 포션",new Rectangle(1505,floor+145,330,18),.5f,UiTheme.MUTED);
        }
        art().effect(b,boss.defeated()?2:0,1800,floor+115,.1f,0,.95f);
        ui.centered(b,font,"← 선택 시련 · 등불 감시탑",new Rectangle(1905,989,230,20),.5f,UiTheme.GOLD);
        if(gate.width>0){
            ui.rect(b,gate.x,floor,gate.width,gate.height,UiTheme.SURFACE,.8f);
            for(float x=gate.x+3;x<gate.x+gate.width;x+=6)ui.line(b,x,floor,x,1280,1,UiTheme.GOLD,.9f);
            for(float y=floor+15;y<1280;y+=30)ui.diamond(b,gate.x+10,y,4,UiTheme.GOLD,.8f);
        }else{
            ui.line(b,gate.x,floor,gate.x,1280,1,UiTheme.BORDER,.5f);
            ui.line(b,gate.x+20,floor,gate.x+20,1280,1,UiTheme.BORDER,.5f);
        }
        if(boss.canStart(p.getBounds(),p.isGrounded())){
            Rectangle r=new Rectangle(p.getCenterX()-106,floor+83,212,23);ui.panel(b,r.x,r.y,r.width,r.height);
            ui.centered(b,font,"["+KeyBindings.name(KeyBindings.Action.INTERACT)+"] 등불지기에게 도전",r,.59f,UiTheme.TEXT);
        }
    }
    void drawActor(SpriteBatch b,Player p){if(p.getY()>650&&Math.abs(p.getCenterX()-boss.cx())<750)art().actor(b,boss);}
    void drawEffects(SpriteBatch b){if(sprites!=null)art().effects(b,boss);}
    void drawHud(SpriteBatch b,UiTheme ui,BitmapFont f,float left){
        if(!boss.active())return;
        ui.centered(b,f,"꺼지지 않는 등불지기"+(boss.enraged()?" · 격화":""),new Rectangle(left+236,277,240,18),.62f,UiTheme.TEXT);
        ui.rect(b,left+236,271,240,3,UiTheme.SURFACE,.9f);
        ui.rect(b,left+236,271,240f*boss.health()/Lamplighter.MAX_HEALTH,3,boss.enraged()?Color.SALMON:UiTheme.GOLD,1);
        ui.diamond(b,left+230,272.5f,2,UiTheme.GOLD,.9f);
        ui.diamond(b,left+482,272.5f,2,UiTheme.GOLD,.9f);
    }
    @Override public void close(){if(sprites!=null){sprites.close();sprites=null;}}
}
