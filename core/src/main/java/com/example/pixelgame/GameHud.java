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
    static Rectangle pauseBounds(float left){return new Rectangle(left+600,330,28,24);}
    static Rectangle inventoryBounds(float left){return new Rectangle(left+594,14,36,34);}
    static Rectangle actionBounds(float left,int index){return new Rectangle(left+16+index*49,12,43,38);}
    private static final KeyBindings.Action[] ACTIONS={KeyBindings.Action.ATTACK,KeyBindings.Action.SKILL,KeyBindings.Action.DASH};
    private final UiTheme ui=new UiTheme();
    private static final Color METAL = new Color(.29f,.39f,.43f,1);
    private static final Color HEALTH = new Color(.23f,.4f,.47f,1);
    private static final Color LOW_HEALTH = new Color(.59f,.25f,.28f,1);
    private static final Color LOST_HEALTH = new Color(.76f,.4f,.33f,1);
    private boolean pauseHover,inventoryHover,pointerHeld;
    private int actionHover=-1;
    void pointer(boolean pause,boolean inventory,boolean held,int action){pauseHover=pause;inventoryHover=inventory;pointerHeld=held;actionHover=action;}
    private void potion(SpriteBatch b,BitmapFont f,float x,String key,int count,Texture image,float buff){
        float packed=b.getPackedColor();b.setColor(1,1,1,count>0||buff>0?1:.4f);
        if(image!=null)b.draw(image,x,301,13,15);b.setPackedColor(packed);
        ui.text(b,f,"["+key+"] "+count+(buff>0?" · "+(int)Math.ceil(buff)+"초":""),x+17,313,.65f,count>0||buff>0?UiTheme.TEXT:UiTheme.MUTED,78);
    }
    void draw(SpriteBatch b,BitmapFont f,float left,Player p,float trail,int heals,int powers,int gold,Texture heal,Texture power,Texture coin){
        ui.verticalFade(b,left,286,640,74,UiTheme.SURFACE,0,.76f);
        healthBar(b,f,left,p,trail);
        potion(b,f,left+20,KeyBindings.name(KeyBindings.Action.HEAL),heals,heal,0);
        potion(b,f,left+111,KeyBindings.name(KeyBindings.Action.POWER),powers,power,p.getAttackPotionTimer());
        float packed=b.getPackedColor();b.setColor(Color.WHITE);if(coin!=null)b.draw(coin,left+548,312,13,13);b.setPackedColor(packed);
        ui.label(b,f,Integer.toString(gold),new Rectangle(left+566,309,60,20),.76f,UiTheme.TEXT,Align.right);
        Rectangle pause=pauseBounds(left);
        if(pauseHover)ui.rect(b,pause.x,pause.y,pause.width,pause.height,UiTheme.GOLD,pointerHeld?.18f:.09f);
        ActionIcons.pause(b,ui,pause.x+pause.width/2,pause.y+pause.height/2,pauseHover?UiTheme.TEXT:UiTheme.GOLD);
        Rectangle bag=inventoryBounds(left);
        float bagX=bag.x+bag.width/2,bagY=bag.y+bag.height/2;
        ActionIcons.plate(b,ui,bagX,bagY,16,inventoryHover);
        ActionIcons.draw(b,ui,KeyBindings.Action.INVENTORY,bagX,bagY-(inventoryHover&&pointerHeld?.8f:0),.92f,UiTheme.TEXT,1);
        if(inventoryHover)ui.centered(b,f,"가방 · "+KeyBindings.name(KeyBindings.Action.INVENTORY),
                new Rectangle(left+512,59,116,18),.55f,UiTheme.TEXT);
    }

    private void healthBar(SpriteBatch b,BitmapFont f,float left,Player p,float trail){
        float x=left+20,y=328,w=170,h=18;
        float health=Math.max(0,Math.min(1,p.getHealthRatio()));
        float delayed=Math.max(health,Math.max(0,Math.min(1,trail)));
        // Dull silver rails and restrained end seals echo the ruin's carved metalwork.
        ui.rect(b,x-2,y-2,w+4,h+4,UiTheme.SURFACE,.97f);
        ui.rect(b,x-1,y-1,w+2,h+2,METAL,.9f);
        ui.rect(b,x,y,w,h,UiTheme.SURFACE,1);
        ui.rect(b,x,y,w*delayed,h,LOST_HEALTH,.9f);
        ui.rect(b,x,y,w*health,h,health<=.3f?LOW_HEALTH:HEALTH,1);
        ui.verticalFade(b,x,y,w*health,h,UiTheme.GOLD,0,.2f);
        ui.line(b,x,y+h,x+w,y+h,.7f,UiTheme.GOLD,.65f);
        ui.line(b,x+4,y-1,x+w-4,y-1,.7f,UiTheme.BORDER,.85f);
        for(float seal:new float[]{x-3,x+w+3}){
            ui.diamond(b,seal,y+h/2,3,METAL,1);
            ui.line(b,seal,y+h/2-1.5f,seal,y+h/2+1.5f,.65f,UiTheme.GOLD,.7f);
        }
        for(int i=0;i<5;i++){
            float scratch=x+10+i*33;
            ui.line(b,scratch,y+h+1,scratch+4,y+h+1,.6f,UiTheme.SURFACE,.6f);
        }
        if(delayed>health+.005f)ui.line(b,x+w*delayed,y+1,x+w*delayed,y+h-1,.8f,UiTheme.TEXT,.7f);
        ui.centered(b,f,p.getHealth()+" / "+p.getMaxHealth(),new Rectangle(x,y,w,h),.82f,UiTheme.TEXT);
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
    private void ability(SpriteBatch b,BitmapFont f,float left,int index,Player p){
        Rectangle r=actionBounds(left,index);
        float cx=r.x+r.width/2,cy=31;
        KeyBindings.Action action=ACTIONS[index];
        float remaining=index==1?p.getSkillCooldown():index==2?p.getDashCooldownTimer():0;
        float duration=index==1?GameBalance.SKILL_COOLDOWN:GameBalance.DASH_COOLDOWN;
        boolean available=index==0?!p.isStunned()&&!p.isUsingSkill()&&!p.isDashing():index==1?p.canUseSkill():p.canDash();
        Color color=available?UiTheme.TEXT:UiTheme.MUTED;
        if(index==1&&p.isChargingSkill())color=UiTheme.skillChargeColor(p.getSkillChargeStage());
        if(index==0&&p.isAttacking())color=UiTheme.GOLD;
        ActionIcons.plate(b,ui,cx,cy,18,actionHover==index);
        ActionIcons.draw(b,ui,action,cx,cy,.98f,color,available||p.isChargingSkill()&&index==1?1:.62f);
        if(index!=0){
            ui.rect(b,r.x+4,10,35,2,UiTheme.BORDER,.65f);
            ui.rect(b,r.x+4,10,35*(1-Math.min(1,remaining/duration)),2,UiTheme.GOLD,1);
            if(remaining>.05f)ui.centered(b,f,String.format(java.util.Locale.ROOT,"%.1f",remaining),new Rectangle(r.x,51,43,13),.55f,UiTheme.MUTED);
        }
        if(actionHover==index)ui.centered(b,f,action.label+" · "+KeyBindings.name(action),
                new Rectangle(Math.max(left+8,r.x-24),69,125,18),.55f,UiTheme.TEXT);
    }
    void drawActions(SpriteBatch b,BitmapFont f,float left,Player p,boolean controls){drawActions(b,f,left,p,controls,false);}
    void drawActions(SpriteBatch b,BitmapFont f,float left,Player p,boolean controls,boolean interrupted){
        if(!controls&&p.canUseSkill()){
            float rawFlash=p.getSkillReadyFlash(),flash=rawFlash*(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)?.2f:1),cx=left+86.5f,cy=31;
            for(int i=3;i>0;i--)ui.rect(b,cx-20-i*2,cy-14-i*2,40+i*4,28+i*4,UiTheme.GOLD,.018f+i*.008f+flash*.04f);
            ui.diamond(b,cx,cy,22+flash*5,UiTheme.GOLD,.55f+flash*.4f);
            if(flash>0)for(int i=0;i<8;i++){
                float a=i*45*(float)Math.PI/180,r=22+(1-flash/.9f)*8;
                ui.line(b,cx+(float)Math.cos(a)*r,cy+(float)Math.sin(a)*r,cx+(float)Math.cos(a)*(r+5*flash),cy+(float)Math.sin(a)*(r+5*flash),.8f,UiTheme.TEXT,flash);
            }
            if(rawFlash>.35f)ui.centered(b,f,"스킬 준비",new Rectangle(left+53,51,68,15),.55f,UiTheme.GOLD);
        }
        if(!controls)for(int i=0;i<ACTIONS.length;i++)ability(b,f,left,i,p);
        if(p.isChargingSkill()){
            for(int i=0;i<4;i++)ui.rect(b,left+265+i*28,55,23,3,UiTheme.skillChargeColor(i+1),i<p.getSkillChargeStage()?1:.2f);
            ui.centered(b,f,"집중 "+p.getSkillChargeStage()+"/4",new Rectangle(left+260,61,120,18),.68f,UiTheme.TEXT);
        }else if(interrupted)ui.centered(b,f,"집중 취소",new Rectangle(left+260,55,120,18),.66f,Color.SALMON);
    }
    @Override public void dispose(){ui.dispose();}
}
