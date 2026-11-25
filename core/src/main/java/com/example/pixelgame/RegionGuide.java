package com.example.pixelgame;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Optional supplies and short first-visit guidance; reset with each region checkpoint attempt. */
final class RegionGuide {
    private final HashSet<String> claimed = new HashSet<>();
    private final HashSet<Integer> seen = new HashSet<>();
    private final ArrayDeque<StageLayout.Supply> collected = new ArrayDeque<>();
    private List<StageLayout.Supply> supplies = List.of();
    private StageLayout.Hint active;
    private float seconds;
    private int stage;
    void enter(int stage) {
        this.stage=stage; claimed.clear();seen.clear();collected.clear();active=null;seconds=0;
        supplies=StageLayout.supplies(stage);
    }
    void update(float dt, Player player) {
        seconds=Math.max(0,seconds-dt);
        StageLayout.Hint hint=StageLayout.hint(stage,player.getX(),player.getY());
        if(hint!=null&&seen.add(hint.id())){active=hint;seconds=3.5f;}
        if(!player.isAlive())return;
        Rectangle body=player.getBounds();
        for(StageLayout.Supply supply:supplies)if(!claimed.contains(supply.id())&&body.overlaps(supply.bounds())){
            claimed.add(supply.id());collected.add(supply);
        }
    }
    StageLayout.Supply takeCollected(){return collected.poll();}
    boolean claimed(String id){return claimed.contains(id);}
    void notice(String first,String second){notice("보급품 발견",first,second);}
    void notice(String title,String first,String second){active=new StageLayout.Hint(-1,title,first,second);seconds=4;}
    void drawSupplies(SpriteBatch b,UiTheme ui,MoonAtlas world,Player p,float time){
        float color=b.getPackedColor();b.setColor(Color.WHITE);
        for(StageLayout.Supply supply:supplies)if(!claimed.contains(supply.id())){
            int frame=supply.powers()>0?4:3;
            if(b instanceof ReadabilityBatch r)r.roleOutline(true);
            world.draw(b,frame,frame,supply.x(),supply.floor()+1,frame==4?28:26,true);
            if(b instanceof ReadabilityBatch r)r.normal();
            if(Math.abs(p.getCenterX()-supply.x())<85&&Math.abs(p.getY()-supply.floor())<75){
                ui.diamond(b,supply.x(),supply.floor()+31,2.5f,UiTheme.SUCCESS,.65f+.2f*(float)Math.sin(time*3));
            }
        }
        b.setPackedColor(color);
    }
    void drawHint(SpriteBatch b,BitmapFont font,UiTheme ui,float left){
        if(active==null||seconds<=0||active.id()!=-1&&!GameSettings.current.on(GameSettings.Option.REGION_TIPS))return;
        // Brief contextual text instead of the old permanent three-line card.
        float y=262;
        ui.verticalFade(b,left+182,y-8,312,45,UiTheme.SURFACE,.1f,.58f);
        ui.text(b,font,active.title(),left+194,y+26,.65f,active.id()==-1?UiTheme.SUCCESS:UiTheme.GOLD,284);
        ui.text(b,font,active.line1(),left+194,y+8,.64f,UiTheme.TEXT,284);
    }
}
