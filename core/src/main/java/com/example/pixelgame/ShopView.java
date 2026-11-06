package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

/** Shop layout and input targets share the same coordinates. */
final class ShopView implements Disposable {
    private static final String[] NAMES = {"공격력 강화", "체력 강화", "회복 포션", "공격력 포션"};
    private static final String[] INFO = {"이번 모험 동안 공격력 +1", "이번 모험 동안 최대 체력 +60",
            "최대 체력의 50% 회복", "8초 동안 공격력 +2"};
    private final Texture pixel;
    private final GlyphLayout layout = new GlyphLayout();
    private final UiTheme ui = new UiTheme();

    ShopView() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill(); pixel = new Texture(p); p.dispose();
    }

    static Rectangle buyBounds(float left, int row) {
        return new Rectangle(left + 450, 229 - row * 52, 82, 30);
    }
    static Rectangle closeBounds(float left) { return new Rectangle(left + 538, 290, 24, 24); }

    private void rect(SpriteBatch b, float x, float y, float w, float h,
                      float r, float g, float blue, float alpha) {
        b.setColor(r, g, blue, alpha); b.draw(pixel, x, y, w, h);
    }
    private void text(SpriteBatch b, BitmapFont f, String s, float x, float y,
                      float size, int tone) {
        b.setColor(Color.WHITE); UiTypography.size(f, size);
        if (tone == 1) f.setColor(.64f, .69f, .73f, 1);
        else if (tone == 2) f.setColor(.92f, .74f, .4f, 1);
        else if (tone == 3) f.setColor(1, .53f, .43f, 1);
        else if (tone == 4) f.setColor(.58f, .87f, .72f, 1);
        else f.setColor(.94f, .94f, .91f, 1);
        f.draw(b, s, x, y);
    }
    private void align(SpriteBatch b, BitmapFont f, String s, Rectangle area, float size, int tone) {
        Color color = switch(tone) {
            case 1 -> UiTheme.MUTED; case 2 -> UiTheme.GOLD;
            case 3 -> Color.SALMON; case 4 -> UiTheme.SUCCESS;
            default -> UiTheme.TEXT;
        };
        ui.centered(b, f, s, area, size, color);
    }

    void draw(SpriteBatch b,BitmapFont f,float left,Texture frame,Texture coin,Texture[] icons,
              int gold,int[] prices,int[] owned,int hovered,boolean pressed,boolean closeHover,
              String message,boolean success,int lastRow,float messageTimer,int... stats) {
        ui.rect(b,left,0,640,360,UiTheme.SURFACE,.64f);
        ui.panel(b,left+76,32,488,292);
        ui.text(b,f,"등불 아래의 상점",left+98,308,.95f,UiTheme.TEXT,300);
        ui.text(b,f,"다음 여정을 준비하세요.",left+98,287,.56f,UiTheme.MUTED,280);
        ui.ornament(b,left+320,274,436);
        float packed=b.getPackedColor();b.setColor(Color.WHITE);
        if(coin!=null)b.draw(coin,left+421,294,16,16);b.setPackedColor(packed);
        ui.label(b,f,gold+" 골드",new Rectangle(left+440,291,84,22),.7f,UiTheme.GOLD,com.badlogic.gdx.utils.Align.right);
        ui.button(b,f,closeBounds(left),"×",true,closeHover,pressed,.85f);
        String[] details=stats.length>=4?preview(stats[0],stats[1],stats[2],stats[3]!=0):INFO;
        for(int row=0;row<4;row++){
            float y=220-row*52;boolean affordable=gold>=prices[row];
            boolean bought=success&&messageTimer>0&&row==lastRow;
            if(hovered==row)ui.rect(b,left+96,y,448,48,UiTheme.GOLD,.045f);
            ui.line(b,left+100,y,left+540,y,.5f,UiTheme.BORDER,.5f);
            ui.diamond(b,left+122,y+26,18,bought?UiTheme.SUCCESS:UiTheme.BORDER,.55f);
            packed=b.getPackedColor();b.setColor(Color.WHITE);
            if(icons[row]!=null)b.draw(icons[row],left+108,y+12,28,28);b.setPackedColor(packed);
            ui.text(b,f,NAMES[row],left+153,y+37,.77f,UiTheme.TEXT,191);
            ui.text(b,f,details[row],left+153,y+17,.54f,UiTheme.MUTED,190);
            ui.text(b,f,"보유 "+owned[row],left+350,y+12,.46f,UiTheme.MUTED,85);
            ui.centered(b,f,prices[row]+" 골드",new Rectangle(left+368,y+23,72,20),.68f,affordable?UiTheme.GOLD:Color.SALMON);
            String label=bought?"구매 완료":affordable?"구매":"부족 "+(prices[row]-gold)+"G";
            ui.button(b,f,buyBounds(left,row),label,affordable,hovered==row,pressed,bought?.66f:.67f);
        }
        ui.text(b,f,messageTimer>0?message:"필요한 물품을 골라주세요.",left+100,48,.57f,
                messageTimer>0?(success?UiTheme.SUCCESS:Color.SALMON):UiTheme.MUTED,345);
        ui.text(b,f,"Esc · 닫기",left+473,48,.51f,UiTheme.MUTED,80);
    }
    static String[] preview(int damage, int maxHealth, int health, boolean powerActive) {
        return new String[]{"공격력 " + damage + " -> " + (damage + 1) + " / 이번 모험",
                "체력 " + health + "/" + maxHealth + " -> " + (health + GameBalance.HEALTH_UPGRADE) + "/" + (maxHealth + GameBalance.HEALTH_UPGRADE),
                "사용 시 체력 " + health + " -> " + Math.min(maxHealth, health + GameBalance.healing(maxHealth))
                        + " / [" + KeyBindings.name(KeyBindings.Action.HEAL) + "]",
                "사용 시 공격력 " + damage + " -> " + (damage + (powerActive ? 0 : GameBalance.POWER_BONUS))
                        + (powerActive ? " / 8초로 갱신" : " / 8초")};
    }
    @Override public void dispose() { pixel.dispose(); ui.dispose(); }
}
