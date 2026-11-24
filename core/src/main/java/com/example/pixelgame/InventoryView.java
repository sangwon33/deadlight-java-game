package com.example.pixelgame;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
final class InventoryView {
    private final UiTheme ui;InventoryView(UiTheme ui){this.ui=ui;}
    static Rectangle useBounds(float left,int row){return new Rectangle(left+445,211-row*61,76,28);}
    static Rectangle closeBounds(float left){return new Rectangle(left+503,292,22,22);}
    void draw(SpriteBatch b,BitmapFont f,float left,Player p,int heals,int powers,int speeds,Texture healIcon,Texture powerIcon,Texture hasteIcon,int hovered,boolean pressed,boolean closeHover,String message,boolean success){
        ui.rect(b,left,0,640,360,UiTheme.SURFACE,.72f);ui.panel(b,left+100,53,440,274);
        ui.text(b,f,"여행자의 가방",left+118,313,.96f,UiTheme.TEXT,350);ui.text(b,f,"일시정지 · 같은 포션은 중첩 없이 10초로 갱신",left+118,285,.6f,UiTheme.MUTED,390);
        ui.button(b,f,closeBounds(left),"×",true,closeHover,pressed,.8f);ui.ornament(b,left+320,271,390);
        int[] counts={heals,powers,speeds};String[] names={"회복 포션","공격력 포션","신속 포션"};KeyBindings.Action[] actions={KeyBindings.Action.HEAL,KeyBindings.Action.POWER,KeyBindings.Action.HASTE};
        for(int row=0;row<3;row++){
            float y=194-row*61;int count=counts[row];boolean full=row==0&&p.getHealth()==p.getMaxHealth(),enabled=p.isAlive()&&count>0&&!full;
            float timer=row==1?p.getAttackPotionTimer():row==2?p.getHastePotionTimer():0;
            ui.line(b,left+118,y,left+522,y,.5f,UiTheme.BORDER,.6f);ui.diamond(b,left+142,y+30,18,enabled?UiTheme.GOLD:UiTheme.BORDER,.4f);
            float packed=b.getPackedColor();b.setColor(1,1,1,count>0?1:.4f);Texture icon=row==0?healIcon:row==1?powerIcon:hasteIcon;float scale=Math.min(26f/icon.getWidth(),28f/icon.getHeight());b.draw(icon,left+142-icon.getWidth()*scale/2,y+29-icon.getHeight()*scale/2,icon.getWidth()*scale,icon.getHeight()*scale);b.setPackedColor(packed);
            ui.text(b,f,names[row],left+171,y+45,.78f,UiTheme.TEXT,250);ui.text(b,f,row==0?GameBalance.HEAL_DESCRIPTION:row==1?"공격력 +100% · 10초":"이동속도 +50% · 10초",left+171,y+28,.6f,UiTheme.MUTED,255);
            ui.text(b,f,"보유 "+count+(timer>0?" · 효과 "+(int)Math.ceil(timer)+"초":""),left+171,y+12,.53f,UiTheme.GOLD,255);
            ui.button(b,f,useBounds(left,row),count==0?"없음":full?"체력 최대":(timer>0?"갱신 ":"사용 ")+KeyBindings.name(actions[row]),enabled,hovered==row,pressed,.6f);
        }
        ui.text(b,f,message.isEmpty()?"포션 단축키로 사용 · I / Esc 닫기":message,left+118,70,.59f,message.isEmpty()?UiTheme.MUTED:success?UiTheme.SUCCESS:Color.SALMON,400);
    }
}
