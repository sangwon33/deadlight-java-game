package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

/** Fine silver tracks and type carry HUD information without large opaque cards. */
final class GameHud implements Disposable {
    static Rectangle pauseBounds(float left){return new Rectangle(left+492,292,64,22);}
    static Rectangle inventoryBounds(float left){return new Rectangle(left+564,292,64,22);}
    private final UiTheme ui=new UiTheme();
    private boolean pauseHover,inventoryHover,pointerHeld;
    void pointer(boolean pause,boolean inventory,boolean held){pauseHover=pause;inventoryHover=inventory;pointerHeld=held;}
    private void potion(SpriteBatch b,BitmapFont f,float x,String key,int count,Texture image,float buff){
        float packed=b.getPackedColor();b.setColor(1,1,1,count>0||buff>0?1:.45f);
        if(image!=null)b.draw(image,x,298,14,14);b.setPackedColor(packed);
        ui.text(b,f,"["+key+"] "+count+(buff>0?" · "+(int)Math.ceil(buff)+"초":""),x+18,309,.55f,UiTheme.MUTED,73);
    }
    void draw(SpriteBatch b,BitmapFont f,float left,Player p,float trail,int heals,int powers,int gold,Texture heal,Texture power,Texture coin){
        ui.diamond(b,left+23,327,3,UiTheme.GOLD,.9f);
        ui.text(b,f,"체력",left+30,343,.48f,UiTheme.MUTED,60);
        ui.label(b,f,p.getHealth()+" / "+p.getMaxHealth(),new Rectangle(left+89,331,111,17),.72f,UiTheme.TEXT,Align.right);
        ui.rect(b,left+30,325,167,4,UiTheme.SURFACE,.85f);
        ui.rect(b,left+30,325,167*Math.max(0,Math.min(1,trail)),4,Color.SALMON,.75f);
        ui.rect(b,left+30,325,167*p.getHealthRatio(),4,p.getHealthRatio()<=.3f?Color.SALMON:UiTheme.GOLD,1);
        ui.line(b,left+30,323,left+197,323,.45f,UiTheme.BORDER,.8f);
        ui.diamond(b,left+201,327,1.7f,UiTheme.GOLD,.8f);
        potion(b,f,left+24,KeyBindings.name(KeyBindings.Action.HEAL),heals,heal,0);
        potion(b,f,left+118,KeyBindings.name(KeyBindings.Action.POWER),powers,power,p.getAttackPotionTimer());
        float packed=b.getPackedColor();b.setColor(Color.WHITE);if(coin!=null)b.draw(coin,left+535,330,15,15);b.setPackedColor(packed);
        ui.label(b,f,Integer.toString(gold),new Rectangle(left+552,330,70,18),.8f,UiTheme.TEXT,Align.right);
        ui.line(b,left+535,324,left+622,324,.45f,UiTheme.BORDER,.8f);
        ui.menuChoice(b,f,pauseBounds(left),"Esc · 멈춤",pauseHover,pointerHeld,.49f);
        ui.menuChoice(b,f,inventoryBounds(left),KeyBindings.name(KeyBindings.Action.INVENTORY)+" · 가방",inventoryHover,pointerHeld,.49f);
    }
    void drawBoss(SpriteBatch b,BitmapFont f,float left,FallenGod boss){
        if(boss==null||!boss.isAlive())return;
        ui.centered(b,f,"일식의 처형자"+(boss.isEnraged()?" · 격화":""),new Rectangle(left+174,278,292,18),.65f,UiTheme.TEXT);
        ui.label(b,f,boss.getHealth()+" / "+boss.getMaxHealth(),new Rectangle(left+397,277,73,18),.5f,UiTheme.MUTED,Align.right);
        ui.rect(b,left+174,273,292,3,UiTheme.SURFACE,.9f);
        ui.rect(b,left+174,273,292f*boss.getHealth()/boss.getMaxHealth(),3,UiTheme.GOLD,1);
        ui.diamond(b,left+168,274.5f,2,UiTheme.GOLD,.8f);ui.diamond(b,left+472,274.5f,2,UiTheme.GOLD,.8f);
    }
    void drawActions(SpriteBatch b,BitmapFont f,float left,Player p,boolean controls){drawActions(b,f,left,p,controls,false);}
    void drawActions(SpriteBatch b,BitmapFont f,float left,Player p,boolean controls,boolean interrupted){
        String label;Color color=UiTheme.MUTED;
        if(interrupted){label="집중 취소 · "+(int)Math.ceil(p.getSkillCooldown())+"초 재사용";color=Color.SALMON;}
        else if(p.isChargingSkill()){
            label="정신집중 "+p.getSkillChargeStage()+"/4 · "+String.format(java.util.Locale.ROOT,"%.1f초",Math.max(0,(1-p.getSkillChargeProgress())*GameBalance.SKILL_FULL_CHARGE));
            color=ThrustSprites.chargeColor(p.getSkillChargeStage());
            for(int i=0;i<4;i++)ui.rect(b,left+245+i*57,294,52,1.5f,ThrustSprites.chargeColor(i+1),i<p.getSkillChargeStage()?1:.18f);
        }else if(p.isUsingSkill()){label=p.isSkillActive()?"돌진 찌르기 · 빈틈 반격":"자세 회복";color=UiTheme.GOLD;}
        else if(!controls)label="["+KeyBindings.name(KeyBindings.Action.SKILL)+"] 스킬 "+(p.getSkillCooldown()>0?String.format(java.util.Locale.ROOT,"%.1f초",p.getSkillCooldown()):"준비")
                    +"   ["+KeyBindings.name(KeyBindings.Action.DASH)+"] 대시 "+(p.getDashCooldownTimer()>0?String.format(java.util.Locale.ROOT,"%.1f초",p.getDashCooldownTimer()):"준비");
        else label="["+KeyBindings.name(KeyBindings.Action.SKILL)+"] "+GameBalance.SKILL_CHARGE_LABEL+" 집중 후 돌진";
        ui.centered(b,f,label,new Rectangle(left+236,298,244,16),.52f,color);
    }
    @Override public void dispose(){ui.dispose();}
}
