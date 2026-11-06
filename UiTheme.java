package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Align;

/** Silver-blue interface surfaces, fine borders and readable Korean labels. */
final class UiTheme implements Disposable {
    static final Color SURFACE = new Color(.025f, .042f, .068f, 1);
    static final Color BORDER = new Color(.27f, .38f, .49f, 1);
    static final Color GOLD = new Color(.68f, .84f, .93f, 1);
    static final Color TEXT = new Color(.91f, .95f, .98f, 1);
    static final Color MUTED = new Color(.64f, .69f, .73f, 1);
    static final Color SUCCESS = new Color(.58f, .87f, .72f, 1);
    private static final Color HOVER = new Color(.10f, .18f, .26f, 1);
    private static final Color PRESSED = new Color(.07f, .12f, .19f, 1);
    private final Texture pixel;
    private final GlyphLayout layout = new GlyphLayout();
    private final float[] triangleVertices=new float[20];

    UiTheme() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill(); pixel = new Texture(p); p.dispose();
    }

    void rect(SpriteBatch b, float x, float y, float w, float h, Color color, float alpha) {
        float packed = b.getPackedColor();
        b.setColor(color.r, color.g, color.b, alpha); b.draw(pixel, x, y, w, h);
        b.setPackedColor(packed);
    }

    void line(SpriteBatch b,float x1,float y1,float x2,float y2,float width,Color color,float alpha) {
        float packed=b.getPackedColor();b.setColor(color.r,color.g,color.b,alpha);
        float dx=x2-x1,dy=y2-y1;
        b.draw(pixel,x1,y1-width/2,0,width/2,(float)Math.sqrt(dx*dx+dy*dy),width,1,1,
                (float)Math.toDegrees(Math.atan2(dy,dx)),0,0,1,1,false,false);
        b.setPackedColor(packed);
    }

    void triangle(SpriteBatch b,float x1,float y1,float x2,float y2,float x3,float y3,Color color,float alpha){
        float tint=Color.toFloatBits(color.r,color.g,color.b,alpha);
        float[] v=triangleVertices;
        float[] xy={x1,y1,x2,y2,x3,y3,x3,y3};
        for(int i=0;i<4;i++){v[i*5]=xy[i*2];v[i*5+1]=xy[i*2+1];v[i*5+2]=tint;v[i*5+3]=0;v[i*5+4]=0;}
        b.draw(pixel,v,0,20);
    }

    void diamond(SpriteBatch b,float x,float y,float radius,Color color,float alpha) {
        line(b,x-radius,y,x,y+radius,.6f,color,alpha);line(b,x,y+radius,x+radius,y,.6f,color,alpha);
        line(b,x+radius,y,x,y-radius,.6f,color,alpha);line(b,x,y-radius,x-radius,y,.6f,color,alpha);
    }

    void ornament(SpriteBatch b,float center,float y,float width) {
        float half=width/2;
        line(b,center-half,y,center-9,y,.55f,BORDER,.8f);
        line(b,center+9,y,center+half,y,.55f,BORDER,.8f);
        diamond(b,center,y,3,GOLD,.85f);
        diamond(b,center-half,y,1.5f,GOLD,.6f);diamond(b,center+half,y,1.5f,GOLD,.6f);
    }

    void panel(SpriteBatch b, float x, float y, float w, float h) {
        rect(b,x,y,w,h,SURFACE,.88f);
        line(b,x+12,y+h,x+w-12,y+h,.6f,BORDER,.8f);
        line(b,x+12,y,x+w-12,y,.6f,BORDER,.65f);
        for(float edge:new float[]{x,x+w}) {
            line(b,edge,y+4,edge,y+12,.6f,BORDER,.8f);
            line(b,edge,y+h-4,edge,y+h-12,.6f,BORDER,.8f);
        }
        diamond(b,x+4,y+h,1.5f,GOLD,.7f);
        diamond(b,x+w-4,y+h,1.5f,GOLD,.7f);
    }

    void menuChoice(SpriteBatch b,BitmapFont f,Rectangle r,String text,boolean active,boolean pressed,float size) {
        if(active)rect(b,r.x+9,r.y+3,r.width-18,r.height-6,GOLD,pressed?.13f:.045f);
        centered(b,f,text,r,size,active?TEXT:MUTED);
        if(active){
            diamond(b,r.x+3,r.y+r.height/2,2.6f,GOLD,1);
            diamond(b,r.x+r.width-3,r.y+r.height/2,2.6f,GOLD,1);
            line(b,r.x+18,r.y+1,r.x+r.width-18,r.y+1,.6f,BORDER,.75f);
        }
    }

    void text(SpriteBatch b, BitmapFont f, String s, float x, float y,
              float size, Color color, float maxWidth) {
        float sx = f.getData().scaleX, sy = f.getData().scaleY;
        float fr = f.getColor().r, fg = f.getColor().g, fb = f.getColor().b, fa = f.getColor().a;
        float packed = b.getPackedColor();
        UiTypography.size(f, size); layout.setText(f, s);
        if (layout.width > maxWidth) UiTypography.size(f, size * maxWidth / layout.width);
        b.setColor(Color.WHITE); f.setColor(color); f.draw(b, s, x, y);
        b.setPackedColor(packed); f.setColor(fr, fg, fb, fa); f.getData().setScale(sx, sy);
    }

    void centered(SpriteBatch b, BitmapFont f, String s, Rectangle area,
                  float size, Color color) {
        label(b, f, s, area, size, color, Align.center);
    }

    /** Fit both dimensions and center actual glyphs, including Korean descenders. */
    void label(SpriteBatch b, BitmapFont f, String s, Rectangle area,
               float size, Color color, int align) {
        float sx = f.getData().scaleX, sy = f.getData().scaleY;
        UiTypography.size(f, size); layout.setText(f, s);
        float top = inkTop(f), bottom = inkBottom(f);
        float fit = Math.min(1f, Math.min(Math.max(1, area.width - 12) / Math.max(1, layout.width),
                Math.max(1, area.height - (area.height < 20 ? 2 : 6)) / Math.max(1, top - bottom)));
        if (fit < 1) {
            size *= fit;
            UiTypography.size(f, size); layout.setText(f, s);
            top = inkTop(f); bottom = inkBottom(f);
        }
        float x = (align & Align.left) != 0 ? area.x + 6
                : (align & Align.right) != 0 ? area.x + area.width - layout.width - 6
                : area.x + (area.width - layout.width) / 2;
        float y = area.y + area.height / 2 - (top + bottom) / 2;
        f.getData().setScale(sx, sy);
        text(b, f, s, x, y, size, color, area.width - 12);
    }

    private float inkTop(BitmapFont f) {
        float top = Float.NEGATIVE_INFINITY;
        for (GlyphLayout.GlyphRun run : layout.runs) for (BitmapFont.Glyph glyph : run.glyphs)
            top = Math.max(top, f.getAscent() + run.y + (glyph.yoffset + glyph.height) * f.getData().scaleY);
        return top == Float.NEGATIVE_INFINITY ? 0 : top;
    }

    private float inkBottom(BitmapFont f) {
        float bottom = Float.POSITIVE_INFINITY;
        for (GlyphLayout.GlyphRun run : layout.runs) for (BitmapFont.Glyph glyph : run.glyphs)
            bottom = Math.min(bottom, f.getAscent() + run.y + glyph.yoffset * f.getData().scaleY);
        return bottom == Float.POSITIVE_INFINITY ? 0 : bottom;
    }

    void button(SpriteBatch b, BitmapFont f, Rectangle r, String label,
                boolean enabled, boolean hovered, boolean pressed, float size) {
        boolean active=enabled&&hovered;
        rect(b,r.x,r.y,r.width,r.height,active?(pressed?PRESSED:HOVER):SURFACE,active?.85f:.55f);
        line(b,r.x+6,r.y,r.x+r.width-6,r.y,.6f,active?GOLD:BORDER,enabled?.8f:.35f);
        if(active){
            line(b,r.x+6,r.y+r.height,r.x+r.width-6,r.y+r.height,.6f,GOLD,.6f);
            diamond(b,r.x+3,r.y+r.height/2,1.7f,GOLD,.9f);
            diamond(b,r.x+r.width-3,r.y+r.height/2,1.7f,GOLD,.9f);
        }
        centered(b,f,label,r,size,enabled?TEXT:MUTED);
    }

    @Override public void dispose() { pixel.dispose(); }
}
