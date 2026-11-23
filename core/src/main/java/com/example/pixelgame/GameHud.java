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
    private final Color fillShade=new Color();
    private boolean pauseHover,inventoryHover,pointerHeld;
    private int actionHover=-1;
    void pointer(boolean pause,boolean inventory,boolean held,int action){pauseHover=pause;inventoryHover=inventory;pointerHeld=held;actionHover=action;}
    private void potion(SpriteBatch b,BitmapFont f,float x,float y,int count,Texture image,float buff,KeyBindings.Action action){
        disc(b,x,y-1,16.8f,HUD_DARK,.65f);
        disc(b,x,y,16,METAL,1);
        disc(b,x,y,14.8f,SLOT_GRAY,1);
        arc(b,x,y,15.3f,35,145,.65f,RIM,.8f);
        float packed=b.getPackedColor();b.setColor(1,1,1,count>0||buff>0?1:.36f);
        if(image!=null){
            float scale=Math.min(18f/image.getWidth(),22f/image.getHeight());
            float w=image.getWidth()*scale,h=image.getHeight()*scale;
            b.draw(image,x-w/2,y-h/2+1,w,h);
        }
        b.setPackedColor(packed);
        if(buff>0)arc(b,x,y,17.8f,90,90+360*Math.min(1,buff/GameBalance.POWER_DURATION),1.5f,POWER,1);
        String key=KeyBindings.name(action);
        float keyWidth=Math.max(24,Math.min(84,16+key.length()*6.2f));
        capsule(b,x+24,y-8,keyWidth,16,HUD_DARK,.94f);
        ui.centered(b,f,key,new Rectangle(x+24,y-8,keyWidth,16),.61f,UiTheme.TEXT);
        // Quantity stays separate from the current, remappable use key.
        String quantity=Integer.toString(Math.max(0,count));
        float badgeWidth=Math.max(15,9+quantity.length()*5.5f),badgeX=x-11-badgeWidth/2;
        capsule(b,badgeX-1,y-19,badgeWidth+2,15,METAL,1);
        capsule(b,badgeX,y-18,badgeWidth,13,HUD_DARK,1);
        ui.centered(b,f,quantity,new Rectangle(badgeX-3,y-18,badgeWidth+6,13),.57f,count>0?UiTheme.TEXT:UiTheme.MUTED);
    }
    private void money(SpriteBatch b,BitmapFont f,float left,int gold){
        capsule(b,left+490,328,100,28,METAL,.7f);
        capsule(b,left+491,329,98,26,HUD_DARK,.96f);
        float cx=left+505,cy=342;
        disc(b,cx,cy,7.5f,HUD_DARK,1);disc(b,cx,cy,6.5f,COIN,1);
        arc(b,cx,cy,5.1f,0,360,.65f,HUD_DARK,.65f);
        ui.diamond(b,cx,cy,2.5f,HUD_DARK,.9f);
        ui.line(b,cx-3,cy+4,cx,cy+5,.7f,UiTheme.TEXT,.7f);
        ui.label(b,f,Integer.toString(gold),new Rectangle(left+514,331,71,22),.73f,UiTheme.TEXT,Align.right);
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
    void draw(SpriteBatch b,BitmapFont f,float left,Player p,float trail,int heals,int powers,int gold,Texture heal,Texture power,Texture coin){
        ui.verticalFade(b,left,286,640,74,UiTheme.SURFACE,0,.76f);
        healthBar(b,f,left,p,trail);
        potion(b,f,left+32,204,heals,heal,0,KeyBindings.Action.HEAL);
        potion(b,f,left+32,156,powers,power,p.getAttackPotionTimer(),KeyBindings.Action.POWER);
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
        float x=left+20,y=333,w=180,h=18;
        float health=Math.max(0,Math.min(1,p.getHealthRatio()));
        float delayed=Math.max(health,Math.max(0,Math.min(1,trail)));
        capsule(b,x-2,y-3,w+4,h+4,HUD_DARK,.7f);
        capsule(b,x-1.5f,y-1.5f,w+3,h+3,METAL,1);
        capsule(b,x-.5f,y-.5f,w+1,h+1,RIM,.6f);
        capsule(b,x,y,w,h,HUD_DARK,1);
        healthFill(b,x,y,w,h,delayed,LOST_HEALTH);
        healthFill(b,x,y,w,h,health,health<=.3f?LOW_HEALTH:HEALTH);
        ui.line(b,x+10,y+h+.65f,x+w-10,y+h+.65f,.65f,RIM,.65f);
        if(delayed>health+.005f)ui.line(b,x+w*delayed,y+1,x+w*delayed,y+h-1,.8f,UiTheme.TEXT,.7f);
        ui.centered(b,f,p.getHealth()+" / "+p.getMaxHealth(),new Rectangle(x,y,w,h),.76f,UiTheme.TEXT);
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
