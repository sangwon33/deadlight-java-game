package com.example.pixelgame;

import java.util.Locale;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

/** Live combat values, never a cached copy of a player's upgrades or temporary buffs. */
final class PlayerStatsView {
    private PlayerStatsView(){}
    static Rectangle mapBounds(){return new Rectangle(432,42,186,286);}
    private static final String[] NAMES={"체력","공격력","이동속도","공격속도","크리티컬 확률","크리티컬 데미지","스킬 공격력","스킬 쿨타임","대시 쿨타임"};
    static Rectangle hudBounds(){return new Rectangle(490,242,138,78);}
    static Rectangle minimapBounds(){return new Rectangle(526,170,102,68);}
    static Rectangle hudCell(int index){
        Rectangle r=hudBounds();return new Rectangle(r.x+(index%2)*69,r.y+r.height-(index/2+1)*26,69,26);
    }
    static String[] values(Player p){
        return new String[]{p.getHealth()+" / "+p.getMaxHealth(),Integer.toString(p.getAttackDamage()),
                String.format(Locale.ROOT,"%.0f",p.getMoveSpeed()),String.format(Locale.ROOT,"%.2f회/초",p.getAttackSpeed()),
                String.format(Locale.ROOT,"%.0f%%",p.getCriticalChance()*100),
                String.format(Locale.ROOT,"%.0f%%",p.getCriticalDamageMultiplier()*100),
                Integer.toString(p.getSkillDamage()),String.format(Locale.ROOT,"%.2f초",GameBalance.SKILL_COOLDOWN),
                String.format(Locale.ROOT,"%.2f초",GameBalance.DASH_COOLDOWN)};
    }
    static void draw(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,Rectangle r,boolean compact){
        draw(b,ui,font,p,r,compact,-1);
    }
    static void draw(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,Rectangle r,boolean compact,int hovered){
        if(compact){compact(b,ui,font,p,r,hovered);return;}
        ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.SURFACE,1);
        ui.panel(b,r.x,r.y,r.width,r.height);
        float top=r.y+r.height;
        ui.label(b,font,"캐릭터 정보",new Rectangle(r.x+6,top-26,r.width-12,21),.83f,UiTheme.GOLD,Align.left);
        ui.line(b,r.x+12,top-36,r.x+r.width-12,top-36,.6f,UiTheme.BORDER,.8f);
        String[] data=values(p);int rows=NAMES.length;
        float step=22,first=top-62,labelWidth=100;
        for(int i=0;i<rows;i++){
            float y=first-i*step;
            ui.label(b,font,NAMES[i],new Rectangle(r.x+5,y,labelWidth,19),.61f,UiTheme.MUTED,Align.left);
            ui.label(b,font,data[i],new Rectangle(r.x+labelWidth,y,r.width-labelWidth-5,19),.65f,
                    i==1&&p.getAttackPotionTimer()>0?UiTheme.SUCCESS:UiTheme.TEXT,Align.right);
        }
        ui.line(b,r.x+12,r.y+41,r.x+r.width-12,r.y+41,.6f,UiTheme.BORDER,.8f);
        ui.text(b,font,"공격속도: 3타 연계 기준",r.x+12,r.y+24,.52f,UiTheme.MUTED,r.width-24);
    }
    private static void compact(SpriteBatch b,UiTheme ui,BitmapFont font,Player p,Rectangle r,int hovered){
        ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.SURFACE,.78f);
        ui.line(b,r.x+5,r.y+r.height,r.x+r.width-5,r.y+r.height,.6f,UiTheme.GOLD,.65f);
        ui.line(b,r.x+5,r.y,r.x+r.width-5,r.y,.6f,UiTheme.BORDER,.75f);
        ui.line(b,r.x+69,r.y+6,r.x+69,r.y+r.height-6,.5f,UiTheme.BORDER,.4f);
        String[] data=values(p);data[0]=p.getHealth()+"/"+p.getMaxHealth();
        data[3]=String.format(Locale.ROOT,"%.2f/s",p.getAttackSpeed());
        for(int i=0;i<6;i++){
            Rectangle cell=hudCell(i);float cy=cell.y+13;
            if(i==hovered)ui.rect(b,cell.x+2,cell.y+2,cell.width-4,cell.height-4,UiTheme.GOLD,.08f);
            StatIcons.draw(b,ui,i,cell.x+11,cy);
            ui.label(b,font,data[i],new Rectangle(cell.x+20,cell.y+4,48,18),.57f,
                    i==1&&p.getAttackPotionTimer()>0?UiTheme.SUCCESS:UiTheme.TEXT,Align.right);
            if(i<4)ui.line(b,cell.x+6,cell.y,cell.x+cell.width-6,cell.y,.45f,UiTheme.BORDER,.35f);
        }
        if(hovered>=0&&hovered<6){
            Rectangle cell=hudCell(hovered),hint=new Rectangle(r.x-130,cell.y+2,122,22);
            ui.rect(b,hint.x,hint.y,hint.width,hint.height,UiTheme.SURFACE,.96f);
            ui.centered(b,font,NAMES[hovered],hint,.65f,UiTheme.TEXT);
        }
    }
}
