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
        if(hint!=null&&seen.add(hint.id())){active=hint;seconds=7;}
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
    void drawSupplies(SpriteBatch b,UiTheme ui,Texture heal,Texture power,Texture gold) {
        float color=b.getPackedColor();
        for(StageLayout.Supply supply:supplies)if(!claimed.contains(supply.id())){
            Rectangle r=supply.bounds();Texture image=supply.heals()>0?heal:supply.powers()>0?power:gold;
            ui.rect(b,r.x-2,r.y-2,r.width+4,r.height+4,UiTheme.GOLD,.6f);
            ui.rect(b,r.x-1,r.y-1,r.width+2,r.height+2,UiTheme.SURFACE,.9f);
            b.setColor(Color.WHITE);b.draw(image,r.x,r.y,r.width,r.height);
        }
        b.setPackedColor(color);
    }
    void drawHint(SpriteBatch b,BitmapFont font,UiTheme ui,float left){
        if(active==null||seconds<=0)return;
        float y=stage==4?210:239;
        ui.panel(b,left+12,y,216,49);
        ui.text(b,font,active.title(),left+22,y+38,.53f,active.id()==-1?UiTheme.SUCCESS:UiTheme.GOLD,196);
        ui.text(b,font,active.line1(),left+22,y+25,.49f,UiTheme.TEXT,196);
        ui.text(b,font,active.line2(),left+22,y+12,.48f,UiTheme.MUTED,196);
    }
}
