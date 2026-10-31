package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

/** Shared charcoal surfaces, warm borders and readable labels. No bitmap art is changed. */
final class UiTheme implements Disposable {
    static final Color SURFACE = new Color(.045f, .052f, .062f, 1);
    static final Color BORDER = new Color(.38f, .34f, .26f, 1);
    static final Color GOLD = new Color(.88f, .74f, .48f, 1);
    static final Color TEXT = new Color(.94f, .94f, .91f, 1);
    static final Color MUTED = new Color(.64f, .69f, .73f, 1);
    static final Color SUCCESS = new Color(.58f, .87f, .72f, 1);
    private static final Color HOVER = new Color(.20f, .18f, .12f, 1);
    private static final Color PRESSED = new Color(.16f, .15f, .12f, 1);
    private final Texture pixel;
    private final GlyphLayout layout = new GlyphLayout();

    UiTheme() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill(); pixel = new Texture(p); p.dispose();
    }

    void rect(SpriteBatch b, float x, float y, float w, float h, Color color, float alpha) {
        float packed = b.getPackedColor();
        b.setColor(color.r, color.g, color.b, alpha); b.draw(pixel, x, y, w, h);
        b.setPackedColor(packed);
    }

    void panel(SpriteBatch b, float x, float y, float w, float h) {
        rect(b, x, y - 2, w, h, Color.BLACK, .3f);
        rect(b, x, y, w, h, BORDER, 1);
        rect(b, x + 1, y + 1, w - 2, h - 2, SURFACE, .98f);
        rect(b, x + 10, y + h - 2, w - 20, 1, GOLD, .55f);
    }

    void text(SpriteBatch b, BitmapFont f, String s, float x, float y,
              float size, Color color, float maxWidth) {
        float sx = f.getData().scaleX, sy = f.getData().scaleY;
        float fr = f.getColor().r, fg = f.getColor().g, fb = f.getColor().b, fa = f.getColor().a;
        float packed = b.getPackedColor();
        f.getData().setScale(size); layout.setText(f, s);
        if (layout.width > maxWidth) f.getData().setScale(size * maxWidth / layout.width);
        b.setColor(Color.WHITE); f.setColor(color); f.draw(b, s, x, y);
        b.setPackedColor(packed); f.setColor(fr, fg, fb, fa); f.getData().setScale(sx, sy);
    }

    void centered(SpriteBatch b, BitmapFont f, String s, Rectangle area,
                  float size, Color color) {
        float sx = f.getData().scaleX, sy = f.getData().scaleY;
        f.getData().setScale(size); layout.setText(f, s);
        if (layout.width > area.width - 12) {
            size *= (area.width - 12) / layout.width;
            f.getData().setScale(size); layout.setText(f, s);
        }
        float x = area.x + (area.width - layout.width) / 2;
        float y = area.y + (area.height + layout.height) / 2;
        f.getData().setScale(sx, sy);
        text(b, f, s, x, y, size, color, area.width - 12);
    }

    void button(SpriteBatch b, BitmapFont f, Rectangle r, String label,
                boolean enabled, boolean hovered, boolean pressed, float size) {
        rect(b, r.x, r.y, r.width, r.height, enabled && hovered ? GOLD : BORDER, enabled ? 1 : .5f);
        Color fill = enabled && hovered ? (pressed ? PRESSED : HOVER) : SURFACE;
        rect(b, r.x + 1, r.y + 1, r.width - 2, r.height - 2, fill, 1);
        if (enabled && hovered) rect(b, r.x + 1, r.y + 1, 2, r.height - 2, GOLD, 1);
        centered(b, f, label, r, size, enabled ? TEXT : MUTED);
    }

    @Override public void dispose() { pixel.dispose(); }
}
