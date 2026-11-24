package com.example.pixelgame;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.*;
final class ShopView implements Disposable {
    private static final String[] NAMES={"공격력 강화","체력 강화","회복 포션","공격력 포션","신속 포션"};
    private static final String[] INFO={"이번 모험 동안 공격력 +1","이번 모험 동안 최대 체력 +"+GameBalance.HEALTH_UPGRADE,GameBalance.HEAL_DESCRIPTION,"10초 동안 공격력 +100%","10초 동안 이동속도 +50%"};
    private final UiTheme ui=new UiTheme();
    static Rectangle buyBounds(float left,int row){return new Rectangle(left+452,239-row*43,78,28);}
    static Rectangle closeBounds(float left){return new Rectangle(left+538,298,22,22);}
    void draw(SpriteBatch b,BitmapFont f,float left,Texture coin,Texture[] icons,int gold,int[] prices,int[] owned,int hovered,boolean pressed,boolean closeHover,String message,boolean success,int lastRow,float messageTimer,int... stats){
        ui.rect(b,left,0,640,360,UiTheme.SURFACE,.72f);ui.panel(b,left+76,28,488,302);
        ui.text(b,f,"등불 아래의 상점",left+98,315,.9f,UiTheme.TEXT,300);
        ui.text(b,f,"↑↓ / W S 선택 · Enter / Space 구매 · Esc 닫기",left+98,291,.58f,UiTheme.MUTED,422);
        ui.label(b,f,gold+" 골드",new Rectangle(left+433,299,90,20),.7f,UiTheme.GOLD,Align.right);
        ui.button(b,f,closeBounds(left),"×",true,closeHover,pressed,.8f);
        String[] details=stats.length>=4?preview(stats[0],stats[1],stats[2],stats[3]!=0):INFO;
        for(int row=0;row<5;row++){
            float y=232-row*43;boolean affordable=gold>=prices[row],selected=hovered==row,bought=success&&messageTimer>0&&row==lastRow;
            if(selected){ui.rect(b,left+96,y,446,41,UiTheme.GOLD,.09f);ui.rect(b,left+96,y+4,2,33,UiTheme.GOLD,1);}
            ui.line(b,left+100,y,left+540,y,.5f,UiTheme.BORDER,.5f);ui.diamond(b,left+122,y+21,15,bought?UiTheme.SUCCESS:UiTheme.BORDER,.5f);
            float packed=b.getPackedColor();b.setColor(Color.WHITE);if(icons[row]!=null){Texture icon=icons[row];float scale=Math.min(24f/icon.getWidth(),26f/icon.getHeight());b.draw(icon,left+122-icon.getWidth()*scale/2,y+21-icon.getHeight()*scale/2,icon.getWidth()*scale,icon.getHeight()*scale);}b.setPackedColor(packed);
            ui.text(b,f,NAMES[row],left+151,y+32,.72f,UiTheme.TEXT,189);ui.text(b,f,details[row],left+151,y+14,.52f,UiTheme.MUTED,211);
            ui.text(b,f,"보유 "+owned[row],left+366,y+12,.48f,UiTheme.MUTED,74);ui.centered(b,f,prices[row]+" 골드",new Rectangle(left+366,y+21,72,18),.62f,affordable?UiTheme.GOLD:Color.SALMON);
            ui.button(b,f,buyBounds(left,row),bought?"구매 완료":affordable?"구매":"골드 부족",affordable,selected,pressed,.63f);
        }
        ui.text(b,f,messageTimer>0?message:"필요한 물품을 골라주세요.",left+100,46,.57f,messageTimer>0?(success?UiTheme.SUCCESS:Color.SALMON):UiTheme.MUTED,438);
    }
    static String[] preview(int damage,int maxHealth,int health,boolean active){int base=active?Math.round(damage/GameBalance.POWER_MULTIPLIER):damage;
        return new String[]{"공격력 "+damage+" → "+(damage+(active?2:1)),"최대 체력 "+maxHealth+" → "+(maxHealth+GameBalance.HEALTH_UPGRADE),"체력 "+GameBalance.HEAL_PERCENT+"% 회복 · ["+KeyBindings.name(KeyBindings.Action.HEAL)+"]","공격력 "+base+" → "+base*2+" · 10초","이동속도 +50% · 10초 · ["+KeyBindings.name(KeyBindings.Action.HASTE)+"]"};}
    public void dispose(){ui.dispose();}
}
