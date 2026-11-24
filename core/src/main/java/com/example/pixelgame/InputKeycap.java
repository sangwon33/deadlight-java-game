package com.example.pixelgame;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** A small matte key with a pale metal rim, shared by HUD and inventory prompts. */
final class InputKeycap {
    private static final Color FACE=new Color(.09f,.14f,.18f,1);
    static void draw(SpriteBatch b,UiTheme ui,BitmapFont f,String key,Rectangle r,boolean enabled){
        float alpha=enabled?1:.55f;
        ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.SURFACE,.96f);
        ui.rect(b,r.x+1,r.y+2,r.width-2,r.height-3,FACE,alpha);
        ui.line(b,r.x+2,r.y+r.height-1,r.x+r.width-2,r.y+r.height-1,.8f,UiTheme.GOLD,alpha);
        ui.line(b,r.x+1,r.y+2,r.x+1,r.y+r.height-2,.7f,UiTheme.BORDER,alpha);
        ui.line(b,r.x+r.width-1,r.y+2,r.x+r.width-1,r.y+r.height-2,.7f,UiTheme.BORDER,alpha);
        ui.line(b,r.x+2,r.y+1,r.x+r.width-2,r.y+1,1.3f,UiTheme.BORDER,alpha);
        // Leave enough width for every digit so narrow '1' is not drawn larger than '2'/'3'.
        float size=key.length()==1&&Character.isDigit(key.charAt(0))?.55f:.61f;
        ui.centered(b,f,key,new Rectangle(r.x,r.y+1,r.width,r.height-2),size,enabled?UiTheme.TEXT:UiTheme.MUTED);
    }
    private InputKeycap(){}
}
