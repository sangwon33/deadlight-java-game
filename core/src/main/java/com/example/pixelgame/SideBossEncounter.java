package com.example.pixelgame;

import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Shared opt-in/reward lifecycle for the two new region-specific minibosses. */
final class SideBossEncounter implements AutoCloseable {
    final SideBossArena arena;
    final SideBoss boss;
    final Rectangle gate;
    private SideBossSprites sprites;
    SideBossEncounter(SideBossArena arena,List<Rectangle> solids){
        this.arena=arena;boss=new SideBoss(arena);gate=new Rectangle(arena.right(),arena.floor(),0,arena.roof()-arena.floor());solids.add(gate);
    }
    private SideBossSprites art(){if(sprites==null)sprites=new SideBossSprites(arena);return sprites;}
    boolean tryStart(Player p){if(!p.isAlive()||!boss.start(p.getBounds(),p.isGrounded()))return false;gate.width=20;p.clearActionInput();return true;}
    void update(float dt,Player p){boss.update(dt,p.getBounds(),p.isAlive());if(boss.takeHit())p.takeDamage(boss.attackDamage(),boss.hitOrigin());if(boss.defeated())gate.width=0;}
    int hit(Rectangle strike,int amount){int dealt=strike.overlaps(boss.body)?boss.damage(amount):0;if(boss.defeated())gate.width=0;return dealt;}
    void drawRoom(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,TerrainTile stone){
        if(p.getY()<arena.floor()-300||p.getX()<arena.x()-500||p.getX()>arena.x()+1400)return;
        float floor=arena.floor(),height=arena.roof()-floor;
        for(float y=floor;y<arena.roof();y+=60)stone.draw(b,arena.x(),Math.min(y+60,arena.roof()),20,Math.min(60,arena.roof()-y));
        Color color=arena.roots()?UiTheme.GOLD:new Color(.78f,.68f,.88f,1);
        for(float x=arena.x()+65;x<arena.right();x+=170){
            ui.line(b,x,floor+14,x,arena.roof()-12,.6f,UiTheme.BORDER,.3f);
            ui.diamond(b,x,arena.roof()-26,6,color,.45f);
        }
        ui.line(b,arena.left()+10,floor+3,arena.right()-10,floor+3,.7f,color,.65f);
        int prop=arena.roots()?0:3;
        art().effect(b,prop,arena.entry()+35,floor+106,arena.roots()?.12f:.14f,0,.9f);
        if(!boss.active()){
            float y=Math.min(floor+(arena.roots()?135:176),arena.roof()-30);
            ui.centered(b,font,boss.defeated()?arena.room()+" · 시련 완료":arena.name(),new Rectangle(arena.right()-300,y,290,22),.66f,UiTheme.TEXT);
            ui.centered(b,font,boss.defeated()?"봉인이 풀렸습니다":"선택 시련 · 골드 "+arena.gold()+" / 공격력 +1 / "+(arena.roots()?"회복 포션":"공격 포션"),new Rectangle(arena.right()-320,y-21,330,18),.48f,UiTheme.MUTED);
        }
        ui.centered(b,font,"← 선택 시련 · "+arena.room(),new Rectangle(arena.x()+905,floor-31,245,20),.5f,color);
        for(float x=gate.x;x<=gate.x+20;x+=5)ui.line(b,x,floor,x,arena.roof(),gate.width>0?1:.4f,gate.width>0?color:UiTheme.BORDER,gate.width>0?.9f:.35f);
        if(boss.canStart(p.getBounds(),p.isGrounded())){
            Rectangle r=new Rectangle(p.getCenterX()-115,floor+77,230,23);ui.panel(b,r.x,r.y,r.width,r.height);
            ui.centered(b,font,"["+KeyBindings.name(KeyBindings.Action.INTERACT)+"] 시련에 도전",r,.6f,UiTheme.TEXT);
        }
    }
    void drawActor(SpriteBatch b,Player p){if(p.getY()>arena.floor()-300&&Math.abs(p.getCenterX()-boss.cx())<750)art().actor(b,boss);}
    void drawEffects(SpriteBatch b){if(sprites!=null)art().effects(b,boss);}
    void drawHud(SpriteBatch b,UiTheme ui,BitmapFont f,float left){
        if(!boss.active())return;
        ui.centered(b,f,arena.name()+(boss.enraged()?" · 격화":""),new Rectangle(left+236,277,240,18),.6f,UiTheme.TEXT);
        ui.rect(b,left+236,271,240,3,UiTheme.SURFACE,.9f);
        ui.rect(b,left+236,271,240f*boss.health()/arena.health(),3,boss.enraged()?Color.SALMON:UiTheme.GOLD,1);
        ui.diamond(b,left+230,272.5f,2,UiTheme.GOLD,.9f);ui.diamond(b,left+482,272.5f,2,UiTheme.GOLD,.9f);
    }
    @Override public void close(){if(sprites!=null){sprites.close();sprites=null;}}
}
