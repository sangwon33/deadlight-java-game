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
    static Rectangle pauseBounds(float left){return new Rectangle(left+598,328,32,28);}
    static Rectangle inventoryBounds(float left){return new Rectangle(left+594,14,36,34);}
    static Rectangle actionBounds(float left,int index){return new Rectangle(left+16+index*49,12,43,38);}
    private static final KeyBindings.Action[] ACTIONS={KeyBindings.Action.ATTACK,KeyBindings.Action.SKILL,KeyBindings.Action.DASH};
    private final UiTheme ui=new UiTheme();
    private static final Color METAL = new Color(.43f,.49f,.53f,1);
    private static final Color HUD_DARK = new Color(.095f,.115f,.135f,1);
    private static final Color SLOT_GRAY = new Color(.25f,.275f,.3f,1);
    private static final Color RIM = new Color(.66f,.73f,.76f,1);
    private static final Color HEALTH = new Color(.29f,.49f,.55f,1);
    private static final Color LOW_HEALTH = new Color(.59f,.25f,.28f,1);
    private static final Color LOST_HEALTH = new Color(.76f,.4f,.33f,1);
    private static final Color COIN = new Color(.8f,.69f,.46f,1);
    private static final Color POWER = new Color(.69f,.61f,.82f,1);
    private static final Color HEART_RUBY=new Color(.80f,.22f,.29f,1);
    private final Color fillShade=new Color();
    private CombatFeedback effects;
    void effects(CombatFeedback value){effects=value;}
    private boolean pauseHover,inventoryHover,pointerHeld;
    private int actionHover=-1;
    void pointer(boolean pause,boolean inventory,boolean held,int action){pauseHover=pause;inventoryHover=inventory;pointerHeld=held;actionHover=action;}
    private void item(SpriteBatch b,BitmapFont f,float left,int row,int count,Texture image,float buff,KeyBindings.Action action){
        float x=left+27,y=310-row*24;
        float pulse=effects==null?0:effects.actionFlash(action);
        ui.verticalFade(b,left+14,y-10,90,21,HUD_DARK,.52f,0);
        Color potionColor=action==KeyBindings.Action.HEAL?HEART_RUBY:action==KeyBindings.Action.POWER?POWER:UiTheme.SUCCESS;
        if(pulse>0)ui.diamond(b,x,y,12+(1-pulse)*3,potionColor,pulse*.35f);
        if(image!=null){float packed=b.getPackedColor();b.setColor(1,1,1,count>0||buff>0?1:.4f);float k=Math.min(17f/image.getWidth(),19f/image.getHeight());b.draw(image,x-image.getWidth()*k/2,y-image.getHeight()*k/2,image.getWidth()*k,image.getHeight()*k);b.setPackedColor(packed);}
        ui.label(b,f,String.format(java.util.Locale.ROOT,"%02d",Math.max(0,count)),new Rectangle(left+40,y-9,34,19),.73f,count>0?UiTheme.TEXT:UiTheme.MUTED,Align.left);
        String key=KeyBindings.name(action);float keyWidth=Math.min(56,Math.max(20,12+key.length()*5f));
        InputKeycap.draw(b,ui,f,key,new Rectangle(left+78,y-8,keyWidth,17),count>0);
        if(buff>0){ui.rect(b,left+40,y-11,32,1.5f,UiTheme.BORDER,.8f);ui.rect(b,left+40,y-11,32*Math.min(1,buff/(action==KeyBindings.Action.POWER?GameBalance.POWER_DURATION:GameBalance.HASTE_DURATION)),1.5f,potionColor,1);ui.text(b,f,(int)Math.ceil(buff)+"초",left+166,y+5,.48f,potionColor,28);}
    }
    private void money(SpriteBatch b,BitmapFont f,float left,int gold){
        float flash=effects==null?0:effects.goldFlash();if(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS))flash*=.25f;
        float x=left+27,y=310;ui.verticalFade(b,left+14,y-10,90,21,HUD_DARK,.52f,0);
        if(flash>0)arc(b,x,y,9+(1-flash)*4,0,360,1,COIN,flash);
        disc(b,x,y,7.5f,HUD_DARK,1);disc(b,x,y,6.5f,COIN,1);arc(b,x,y,4.9f,0,360,.7f,HUD_DARK,.8f);ui.diamond(b,x,y,2.3f,HUD_DARK,.9f);
        ui.label(b,f,String.format(java.util.Locale.ROOT,"%02d",gold),new Rectangle(left+40,y-9,63,19),.73f,flash>0?UiTheme.GOLD:UiTheme.TEXT,Align.left);
        if(effects!=null&&effects.goldNotice()>0)ui.text(b,f,"+"+effects.goldNotice(),left+104,y+7,.6f,UiTheme.SUCCESS,55);
    }
    private void pause(SpriteBatch b,float left){
        Rectangle r=pauseBounds(left);float x=r.x+r.width/2,y=r.y+r.height/2;
        disc(b,x,y-1,14.7f,HUD_DARK,.65f);
        disc(b,x,y,14,pauseHover?RIM:METAL,1);
        disc(b,x,y,12.9f,pauseHover?SLOT_GRAY:HUD_DARK,1);
        arc(b,x,y,13.4f,35,145,.6f,RIM,.7f);
        float pressed=pauseHover&&pointerHeld?.6f:0;
        ui.rect(b,x-4.5f,y-4.8f-pressed,2.8f,9.6f,UiTheme.TEXT,1);
        ui.rect(b,x+1.7f,y-4.8f-pressed,2.8f,9.6f,UiTheme.TEXT,1);
    }
    void draw(SpriteBatch b,BitmapFont f,float left,Player p,float trail,int heals,int powers,int speeds,int gold,Texture heal,Texture power,Texture coin,Texture haste){
        ui.verticalFade(b,left,286,640,74,UiTheme.SURFACE,0,.76f);
        healthBar(b,f,left,p,trail);
        item(b,f,left,1,heals,heal,0,KeyBindings.Action.HEAL);
        item(b,f,left,2,powers,power,p.getAttackPotionTimer(),KeyBindings.Action.POWER);
        item(b,f,left,3,speeds,haste,p.getHastePotionTimer(),KeyBindings.Action.HASTE);
        money(b,f,left,gold);
        pause(b,left);
        Rectangle bag=inventoryBounds(left);
        float bagX=bag.x+bag.width/2,bagY=bag.y+bag.height/2;
        ActionIcons.plate(b,ui,bagX,bagY,16,inventoryHover);
        ActionIcons.draw(b,ui,KeyBindings.Action.INVENTORY,bagX,bagY-(inventoryHover&&pointerHeld?.8f:0),.92f,UiTheme.TEXT,1);
        if(inventoryHover)ui.centered(b,f,"가방 · "+KeyBindings.name(KeyBindings.Action.INVENTORY),
                new Rectangle(left+512,59,116,18),.55f,UiTheme.TEXT);
    }

    private void healthBar(SpriteBatch b,BitmapFont f,float left,Player p,float trail){
        float x=left+43,y=334,w=158,h=16;
        float health=Math.max(0,Math.min(1,p.getHealthRatio())),delayed=Math.max(health,Math.min(1,trail));
        float heartX=left+24,heartY=y+h/2;
        disc(b,heartX,heartY,12.5f,HUD_DARK,.96f);disc(b,heartX,heartY,11.3f,METAL,1);disc(b,heartX,heartY,10.4f,HUD_DARK,1);
        heart(b,heartX,heartY+.4f,1.08f,RIM);heart(b,heartX,heartY+.4f,.94f,HEART_RUBY);
        ui.line(b,heartX-3.7f,heartY+2.5f,heartX-2,heartY+3.8f,.75f,UiTheme.TEXT,.7f);
        capsule(b,x-2,y-2,w+4,h+4,HUD_DARK,.95f);
        capsule(b,x-1,y-1,w+2,h+2,METAL,1);capsule(b,x-.3f,y-.3f,w+.6f,h+.6f,RIM,.75f);
        capsule(b,x,y,w,h,HUD_DARK,1);
        healthFill(b,x,y,w,h,delayed,LOST_HEALTH);healthFill(b,x,y,w,h,health,health<=.3f?LOW_HEALTH:HEALTH);
        ui.line(b,x+9,y+h-1,x+w-9,y+h-1,.55f,RIM,.55f);
        ui.centered(b,f,p.getHealth()+" / "+p.getMaxHealth(),new Rectangle(x+10,y,w-20,h),.67f,UiTheme.TEXT);
    }
    private void heart(SpriteBatch b,float x,float y,float s,Color color){
        disc(b,x-2.7f*s,y+1.7f*s,3.3f*s,color,1);disc(b,x+2.7f*s,y+1.7f*s,3.3f*s,color,1);
        ui.triangle(b,x-5.9f*s,y+1.2f*s,x+5.9f*s,y+1.2f*s,x,y-6.5f*s,color,1);
    }

    private void healthFill(SpriteBatch b,float x,float y,float w,float h,float fraction,Color color){
        if(fraction<=0)return;
        float radius=h/2,end=x+w*fraction;
        for(float row=0;row<h;row+=.5f){
            float dy=row+.25f-radius,inset=radius-(float)Math.sqrt(Math.max(0,radius*radius-dy*dy));
            float start=x+inset,right=Math.min(x+w-inset,end);
            if(right<=start)continue;
            float light=.78f+.3f*row/h;
            fillShade.set(color.r*light,color.g*light,color.b*light,1);
            ui.rect(b,start,y+row,right-start,Math.min(.5f,h-row),fillShade,1);
        }
    }
    private void disc(SpriteBatch b,float x,float y,float radius,Color color,float alpha){
        fan(b,x,y,radius,0,360,color,alpha);
    }
    private void capsule(SpriteBatch b,float x,float y,float w,float h,Color color,float alpha){
        float r=h/2;
        ui.rect(b,x+r,y,Math.max(0,w-h),h,color,alpha);
        fan(b,x+r,y+r,r,90,270,color,alpha);
        fan(b,x+w-r,y+r,r,-90,90,color,alpha);
    }
    private void fan(SpriteBatch b,float x,float y,float radius,float from,float to,Color color,float alpha){
        int steps=Math.max(1,(int)Math.ceil((to-from)/10));
        float last=(float)Math.toRadians(from),px=x+(float)Math.cos(last)*radius,py=y+(float)Math.sin(last)*radius;
        for(int i=1;i<=steps;i++){
            float angle=(float)Math.toRadians(from+(to-from)*i/steps),nx=x+(float)Math.cos(angle)*radius,ny=y+(float)Math.sin(angle)*radius;
            ui.triangle(b,x,y,px,py,nx,ny,color,alpha);px=nx;py=ny;
        }
    }
    private void arc(SpriteBatch b,float x,float y,float radius,float from,float to,float width,Color color,float alpha){
        int steps=Math.max(1,(int)Math.ceil((to-from)/10));
        float last=(float)Math.toRadians(from),px=x+(float)Math.cos(last)*radius,py=y+(float)Math.sin(last)*radius;
        for(int i=1;i<=steps;i++){
            float angle=(float)Math.toRadians(from+(to-from)*i/steps),nx=x+(float)Math.cos(angle)*radius,ny=y+(float)Math.sin(angle)*radius;
            ui.line(b,px,py,nx,ny,width,color,alpha);px=nx;py=ny;
        }
    }
    void drawBoss(SpriteBatch b,BitmapFont f,float left,FallenGod boss){
        if(boss==null||!boss.isAlive())return;
        bossBar(b,f,ui,left,"일식의 처형자"+(boss.isEnraged()?" · 격화":""),boss.getHealth(),boss.getMaxHealth(),boss.getHealthTrail());
    }
    static void bossBar(SpriteBatch b,BitmapFont f,UiTheme ui,float left,String name,int health,int max,float trail){
        float x=left+217,y=286,w=264,h=9;
        float ratio=Math.max(0,Math.min(1,(float)health/max));
        ui.centered(b,f,name,new Rectangle(x-2,y+13,w+4,18),.68f,UiTheme.TEXT);
        ui.rect(b,x-2,y-2,w+4,h+4,HUD_DARK,.96f);ui.rect(b,x-1,y-1,w+2,h+2,METAL,1);ui.rect(b,x,y,w,h,HUD_DARK,1);
        ui.rect(b,x,y,w*Math.max(ratio,Math.min(1,trail)),h,LOST_HEALTH,.9f);
        ui.rect(b,x,y,w*ratio,h,new Color(.52f,.19f,.26f,1),1);ui.rect(b,x,y+h-2,w*ratio,1,new Color(.91f,.58f,.57f,1),.8f);
        for(float edge:new float[]{x-6,x+w+6}){ui.diamond(b,edge,y+h/2,5,METAL,1);ui.diamond(b,edge,y+h/2,2.5f,UiTheme.GOLD,1);}
        ui.centered(b,f,health+" / "+max,new Rectangle(x,y-17,w,14),.49f,UiTheme.MUTED);
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
        float pulse=effects==null?0:effects.actionFlash(action);
        if(pulse>0)ui.diamond(b,cx,cy,19+(1-pulse)*6,UiTheme.GOLD,pulse*(GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS)?.35f:.9f));
        ActionIcons.draw(b,ui,action,cx,cy,.98f,color,available||p.isChargingSkill()&&index==1?1:.62f);
        if(index!=0){
            ui.rect(b,r.x+4,10,35,2,UiTheme.BORDER,.65f);
            ui.rect(b,r.x+4,10,35*(1-Math.min(1,remaining/duration)),2,UiTheme.GOLD,1);
            if(remaining>.05f&&actionHover<0&&!p.isChargingSkill())ui.centered(b,f,String.format(java.util.Locale.ROOT,"%.1f",remaining),new Rectangle(r.x,51,43,13),.55f,UiTheme.MUTED);
        }
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
            if(rawFlash>.35f&&actionHover<0)ui.centered(b,f,"스킬 준비",new Rectangle(left+53,51,68,15),.55f,UiTheme.GOLD);
        }
        if(!controls)for(int i=0;i<ACTIONS.length;i++)ability(b,f,left,i,p);
        if(!controls&&actionHover>=0&&!p.isChargingSkill()){
            Rectangle anchor=actionBounds(left,actionHover);
            ActionTooltip.draw(b,ui,f,p,ACTIONS[actionHover],anchor.x+anchor.width/2,anchor.y+anchor.height);
        }
        if(!controls&&p.isChargingSkill()){
            for(int i=0;i<4;i++)ui.rect(b,left+57+i*16,62,12,2,UiTheme.skillChargeColor(i+1),i<p.getSkillChargeStage()?1:.2f);
            ui.centered(b,f,"집중 "+p.getSkillChargeStage()+"/4",new Rectangle(left+42.5f,67,88,16),.6f,UiTheme.TEXT);
        }else if(!controls&&interrupted&&actionHover!=1)ui.centered(b,f,"집중 취소",new Rectangle(left+42.5f,63,88,16),.6f,Color.SALMON);
    }
    @Override public void dispose(){ui.dispose();}
}
