package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

final class PauseView implements Disposable {
    private final Texture pixel;
    private final GlyphLayout layout=new GlyphLayout();
    PauseView() {
        Pixmap p=new Pixmap(1,1,Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);p.fill();pixel=new Texture(p);p.dispose();
    }
    static Rectangle buttonBounds(float left,int row) {return new Rectangle(left+198,194-row*50,244,36);}
    private void rect(SpriteBatch b,float x,float y,float w,float h,float r,float g,float blue,float a) {
        b.setColor(r,g,blue,a);b.draw(pixel,x,y,w,h);
    }
    private void text(SpriteBatch b,BitmapFont f,String s,float cx,float y,float size,boolean muted) {
        b.setColor(Color.WHITE);f.getData().setScale(size);layout.setText(f,s);
        if(muted)f.setColor(.66f,.73f,.79f,1);else f.setColor(.95f,.96f,.97f,1);
        f.draw(b,s,cx-layout.width/2,y);
    }
    void draw(SpriteBatch b,BitmapFont f,float left,int pending,int hovered,boolean pressed) {
        Color bc=new Color(b.getColor()),fc=new Color(f.getColor());
        float sx=f.getData().scaleX,sy=f.getData().scaleY;
        rect(b,left,0,640,360,.01f,.015f,.025f,.7f);
        rect(b,left+166,62,308,248,.34f,.32f,.26f,.9f);
        rect(b,left+167,63,306,246,.035f,.045f,.06f,.96f);
        text(b,f,pending==0?"PAUSED":pending==1?"RESTART RUN?":"LEAVE RUN?",left+320,283,1.45f,false);
        text(b,f,pending==0?"Your progress is kept while paused.":"Current run progress will be lost.",
                left+320,255,.65f,true);
        String[] labels=pending==0?new String[]{"RESUME","RESTART RUN","MAIN MENU"}
                :new String[]{"BACK","CONFIRM"};
        for(int row=0;row<labels.length;row++) {
            Rectangle r=buttonBounds(left,row);float lift=hovered==row?(pressed?.025f:.07f):0;
            rect(b,r.x,r.y,r.width,r.height,.38f,.34f,.26f,1);
            rect(b,r.x+1,r.y+1,r.width-2,r.height-2,.07f+lift,.08f+lift,.09f+lift,1);
            text(b,f,labels[row],left+320,r.y+23,.9f,false);
        }
        text(b,f,pending==0?"[ESC / ENTER] RESUME":"[ESC] BACK / [ENTER] CONFIRM",left+320,79,.58f,true);
        f.getData().setScale(sx,sy);f.setColor(fc);b.setColor(bc);
    }
    @Override public void dispose(){pixel.dispose();}
}
