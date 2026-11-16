package com.example.pixelgame;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

/** Small fixed HUD: health, consumables, money and compact ability readiness. */
final class GameHud implements Disposable {
    static Rectangle pauseBounds(float left){return new Rectangle(left+596,57,32,22);}
    static Rectangle inventoryBounds(float left){return new Rectangle(left+526,57,64,22);}
    private final UiTheme ui=new UiTheme();
    private boolean pauseHover,inventoryHover,pointerHeld;
    void pointer(boolean pause,boolean inventory,boolean held){pauseHover=pause;inventoryHover=inventory;pointerHeld=held;}
    private void potion(SpriteBatch b,BitmapFont f,float x,String key,int count,Texture image,float buff){
        float packed=b.getPackedColor();b.setColor(1,1,1,count>0||buff>0?1:.4f);
        if(image!=null)b.draw(image,x,301,13,15);b.setPackedColor(packed);
        ui.text(b,f,"["+key+"] "+count+(buff>0?" · "+(int)Math.ceil(buff)+"초":""),x+17,313,.65f,count>0||buff>0?UiTheme.TEXT:UiTheme.MUTED,78);
    }
    void draw(SpriteBatch b,BitmapFont f,float left,Player p,float trail,int heals,int powers,int gold,Texture heal,Texture power,Texture coin){
        ui.verticalFade(b,left,286,640,74,UiTheme.SURFACE,0,.76f);
        ui.text(b,f,p.getHealth()+" / "+p.getMaxHealth(),left+20,348,.85f,UiTheme.TEXT,170);
        ui.rect(b,left+20,320,148,6,UiTheme.SURFACE,.9f);
        ui.rect(b,left+20,320,148*Math.max(0,Math.min(1,trail)),6,new Color(1,.38f,.28f,1),.94f);
        ui.rect(b,left+20,320,148*p.getHealthRatio(),6,p.getHealthRatio()<=.3f?Color.SALMON:UiTheme.GOLD,1);
        if(trail>p.getHealthRatio()+.005f)ui.rect(b,left+20+148*trail-1,319,1.2f,8,UiTheme.TEXT,.85f);
        potion(b,f,left+20,KeyBindings.name(KeyBindings.Action.HEAL),heals,heal,0);
        potion(b,f,left+111,KeyBindings.name(KeyBindings.Action.POWER),powers,power,p.getAttackPotionTimer());
        float packed=b.getPackedColor();b.setColor(Color.WHITE);if(coin!=null)b.draw(coin,left+548,332,13,13);b.setPackedColor(packed);
        ui.label(b,f,Integer.toString(gold),new Rectangle(left+566,329,60,20),.76f,UiTheme.TEXT,Align.right);
        ui.menuChoice(b,f,pauseBounds(left),"Esc",pauseHover,pointerHeld,.6f);
        ui.menuChoice(b,f,inventoryBounds(left),KeyBindings.name(KeyBindings.Action.INVENTORY)+" 가방",inventoryHover,pointerHeld,.6f);
    }
    void drawBoss(SpriteBatch b,BitmapFont f,float left,FallenGod boss){
        if(boss==null||!boss.isAlive())return;
        bossBar(b,f,ui,left,"일식의 처형자"+(boss.isEnraged()?" · 격화":""),boss.getHealth(),boss.getMaxHealth(),boss.getHealthTrail());
    }
    static void bossBar(SpriteBatch b,BitmapFont f,UiTheme ui,float left,String name,int health,int max,float trail){
        ui.centered(b,f,name,new Rectangle(left+219,306,260,19),.70f,UiTheme.TEXT);
        ui.rect(b,left+224,295,250,3,UiTheme.SURFACE,.9f);
        ui.rect(b,left+224,295,250f*trail,3,Color.SALMON,.9f);
        ui.rect(b,left+224,295,250f*health/max,3,UiTheme.GOLD,1);
        ui.label(b,f,health+" / "+max,new Rectangle(left+405,280,70,13),.48f,UiTheme.MUTED,Align.right);
    }
    private void ability(SpriteBatch b,BitmapFont f,float x,KeyBindings.Action action,float remaining,float duration){
        String key=KeyBindings.name(action);
        ui.centered(b,f,key,new Rectangle(x,15,43,17),.61f,remaining>0?UiTheme.MUTED:UiTheme.TEXT);
        ui.rect(b,x+4,10,35,2,UiTheme.BORDER,.65f);
        ui.rect(b,x+4,10,35*(1-Math.min(1,remaining/duration)),2,UiTheme.GOLD,1);
        if(remaining>.05f)ui.centered(b,f,String.format(java.util.Locale.ROOT,"%.1f",remaining),new Rectangle(x,34,43,13),.58f,UiTheme.MUTED);
    }
    void drawActions(SpriteBatch b,BitmapFont f,float left,Player p,boolean controls){drawActions(b,f,left,p,controls,false);}
    void drawActions(SpriteBatch b,BitmapFont f,float left,Player p,boolean controls,boolean interrupted){
        if(!controls&&p.canUseSkill()){
            float rawFlash=p.getSkillReadyFlash(),flash=rawFlash*(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)?.2f:1),cx=left+37.5f,cy=30;
            for(int i=3;i>0;i--)ui.rect(b,cx-20-i*2,cy-14-i*2,40+i*4,28+i*4,UiTheme.GOLD,.018f+i*.008f+flash*.04f);
            ui.diamond(b,cx,cy,22+flash*5,UiTheme.GOLD,.55f+flash*.4f);
            if(flash>0)for(int i=0;i<8;i++){
                float a=i*45*(float)Math.PI/180,r=22+(1-flash/.9f)*8;
                ui.line(b,cx+(float)Math.cos(a)*r,cy+(float)Math.sin(a)*r,cx+(float)Math.cos(a)*(r+5*flash),cy+(float)Math.sin(a)*(r+5*flash),.8f,UiTheme.TEXT,flash);
            }
            if(rawFlash>.35f)ui.text(b,f,"스킬 준비",left+16,61,.61f,UiTheme.GOLD,80);
        }
        if(!controls){ability(b,f,left+16,KeyBindings.Action.SKILL,p.getSkillCooldown(),GameBalance.SKILL_COOLDOWN);
            ability(b,f,left+65,KeyBindings.Action.DASH,p.getDashCooldownTimer(),GameBalance.DASH_COOLDOWN);}
        if(p.isChargingSkill()){
            for(int i=0;i<4;i++)ui.rect(b,left+265+i*28,55,23,3,UiTheme.skillChargeColor(i+1),i<p.getSkillChargeStage()?1:.2f);
            ui.centered(b,f,"집중 "+p.getSkillChargeStage()+"/4",new Rectangle(left+260,61,120,18),.68f,UiTheme.TEXT);
        }else if(interrupted)ui.centered(b,f,"집중 취소",new Rectangle(left+260,55,120,18),.66f,Color.SALMON);
    }
    @Override public void dispose(){ui.dispose();}
}
