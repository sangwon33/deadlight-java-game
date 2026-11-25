package com.example.pixelgame;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Visual-only energy follows the live player's position and actual potion timers. */
final class PotionAura {
    private static final Color[] COLORS={new Color(1,.19f,.24f,1),new Color(.69f,.36f,1,1),new Color(.25f,.96f,.48f,1)};
    private PotionAura(){}
    private static float strength(Player p,int kind){
        float seconds=kind==0?p.getHealingAuraTimer():kind==1?p.getAttackPotionTimer():p.getHastePotionTimer();
        return Math.min(1,seconds/.3f);
    }
    static void drawBehind(SpriteBatch b,UiTheme ui,Player p,float time){
        if(!p.isAlive())return;
        boolean gentle=GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS);
        float height=p.isCrouching()?38:p.isDashing()||p.isUsingSkill()&&!p.isChargingSkill()?48:66;
        float cx=p.getCenterX(),cy=p.getY()+height*.48f;
        for(int kind=0;kind<3;kind++){
            float power=strength(p,kind)*(gentle?.35f:1);if(power<=0)continue;
            float shift=kind==1?-3:kind==2?3:0,pulse=gentle?0:(float)Math.sin(time*3+kind)*1.3f;
            for(int layer=6;layer>=1;layer--)ellipse(b,ui,cx+shift,cy,14+layer*2+pulse,height*.37f+layer*2,.026f*power,COLORS[kind]);
        }
    }
    static void drawFront(SpriteBatch b,UiTheme ui,Player p,float time){
        if(!p.isAlive())return;
        boolean gentle=GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS);
        float cx=p.getCenterX(),floor=p.getY(),height=p.isCrouching()?35:p.isDashing()?45:62;
        for(int kind=0;kind<3;kind++){
            float power=strength(p,kind);if(power<=0)continue;
            Color color=COLORS[kind];
            // Elliptical ground ribbons keep simultaneous violet and green effects distinct.
            for(int i=0;i<24;i++){
                float a=(i*15+kind*52)*(float)Math.PI/180,c=(i*15+15+kind*52)*(float)Math.PI/180;
                float rx=kind==2?20:kind==1?26:30;
                ui.line(b,cx+(float)Math.cos(a)*rx,floor+2+(float)Math.sin(a)*4,cx+(float)Math.cos(c)*rx,floor+2+(float)Math.sin(c)*4,.8f,color,power*(gentle?.18f:.38f));
            }
            if(gentle)continue;
            for(int i=0;i<6;i++){
                float t=(time*(kind==2?1.15f:.72f)+i/6f+kind*.19f)%1;
                float y=floor+5+t*height,x=cx+(float)Math.sin(t*7+i*2.3f+kind)*21;
                float alpha=power*(1-t)*.78f;
                ui.line(b,x,y,x+(float)Math.sin(t*4+kind)*2,y+4.5f,.85f,color,alpha);
                ui.diamond(b,x,y+5,.9f,color,alpha*.8f);
            }
        }
    }
    private static void ellipse(SpriteBatch b,UiTheme ui,float x,float y,float rx,float ry,float alpha,Color color){
        for(int i=0;i<32;i++){
            double a=i*Math.PI*2/32,c=(i+1)*Math.PI*2/32;
            ui.triangle(b,x,y,x+(float)Math.cos(a)*rx,y+(float)Math.sin(a)*ry,x+(float)Math.cos(c)*rx,y+(float)Math.sin(c)*ry,color,alpha);
        }
    }
}
