package com.example.pixelgame;

import java.util.Locale;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** One anchored layout for both keyboard HUD icons and optional pointer controls. */
final class ActionTooltip {
    private ActionTooltip(){}
    static Rectangle bounds(float center,float buttonTop,KeyBindings.Action action){
        float width=action==KeyBindings.Action.SKILL?256:action==KeyBindings.Action.ATTACK?210:220;
        float height=action==KeyBindings.Action.SKILL?76:58;
        return new Rectangle(MathUtils.clamp(center-width/2,8,632-width),buttonTop+8,width,height);
    }
    static void draw(SpriteBatch b,UiTheme ui,BitmapFont f,Player p,KeyBindings.Action action,float center,float buttonTop){
        Rectangle r=bounds(center,buttonTop,action);
        ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.SURFACE,.97f);
        ui.panel(b,r.x,r.y,r.width,r.height);
        float pointer=MathUtils.clamp(center,r.x+9,r.x+r.width-9);
        ui.triangle(b,pointer-3,r.y,pointer+3,r.y,pointer,r.y-4,UiTheme.BORDER,1);
        String name=action==KeyBindings.Action.ATTACK?"연속 베기":action==KeyBindings.Action.SKILL?"집중 찌르기":action.label;
        ui.centered(b,f,name+" · "+KeyBindings.name(action),new Rectangle(r.x+8,r.y+r.height-25,r.width-16,20),.72f,UiTheme.TEXT);
        String text;
        if(action==KeyBindings.Action.SKILL){
            text=String.format(Locale.ROOT,"쿨타임 %.1f초 · 피해 %d",GameBalance.SKILL_COOLDOWN,p.getSkillDamage());
            ui.centered(b,f,text,new Rectangle(r.x+8,r.y+34,r.width-16,15),.59f,UiTheme.GOLD);
            ui.paragraph(b,f,GameBalance.SKILL_CHARGE_LABEL+" 집중 후 전방으로 자동 돌진합니다.\n키를 놓으면 취소 · 차징 중 이동 불가",
                    new Rectangle(r.x+12,r.y+7,r.width-24,24),.58f,UiTheme.MUTED);
        }else{
            text=switch(action){
                case ATTACK->"3타 연계 · 피해 "+p.getAttackDamage()+" / "+p.getAttackDamage()+" / "+Math.round(p.getAttackDamage()*1.4f);
                case DASH->String.format(Locale.ROOT,"쿨타임 %.2f초 · 공중 1회 사용",GameBalance.DASH_COOLDOWN);
                case JUMP->"짧게 / 길게 누르면 점프 높이가 달라집니다.";
                default->"누르는 동안 해당 방향으로 이동합니다.";
            };
            ui.paragraph(b,f,text,new Rectangle(r.x+12,r.y+8,r.width-24,22),.61f,UiTheme.MUTED);
        }
    }
}
