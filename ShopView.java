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
    private static final String[] NAMES = {"Attack upgrade", "Health upgrade", "Heal potion", "Power potion"};
    private static final String[] INFO = {"+1 damage / this run", "+3 max HP / this run",
            "Restore 50% max HP / key [1]", "+2 damage for 8s / key [2]"};
    private final Texture pixel;
    private final GlyphLayout layout = new GlyphLayout();

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
        b.setColor(Color.WHITE); f.getData().setScale(size);
        if (tone == 1) f.setColor(.64f, .69f, .73f, 1);
        else if (tone == 2) f.setColor(.92f, .74f, .4f, 1);
        else if (tone == 3) f.setColor(1, .53f, .43f, 1);
        else if (tone == 4) f.setColor(.58f, .87f, .72f, 1);
        else f.setColor(.94f, .94f, .91f, 1);
        f.draw(b, s, x, y);
    }
    private void align(SpriteBatch b, BitmapFont f, String s, Rectangle area, float size, int tone) {
        f.getData().setScale(size); layout.setText(f, s);
        if (layout.width > area.width - 8) {
            size *= (area.width - 8) / layout.width;
            f.getData().setScale(size); layout.setText(f, s);
        }
        text(b, f, s, area.x + (area.width - layout.width) / 2,
                area.y + (area.height + layout.height) / 2, size, tone);
    }

    void draw(SpriteBatch b, BitmapFont f, float left, Texture frame, Texture goldIcon,
              Texture[] icons, int gold, int[] prices, int[] owned, int hovered, boolean pressed,
              boolean closeHover, String message, boolean success, int lastRow, float messageTimer, int... stats) {
        Color oldBatch = new Color(b.getColor()), oldFont = new Color(f.getColor());
        float sx = f.getData().scaleX, sy = f.getData().scaleY;
        rect(b, left, 0, 640, 360, .015f, .02f, .03f, .76f);
        rect(b, left + 76, 36, 488, 288, .035f, .043f, .05f, .98f);
        b.setColor(Color.WHITE);
        if (frame != null) b.draw(frame, left + 60, 20, 520, 320);

        // Quiet text surfaces keep the ornate frame from competing with labels.
        rect(b, left + 96, 278, 448, 42, .045f, .052f, .062f, .98f);

        text(b, f, "TRAVELING MERCHANT", left + 98, 314, .55f, 2);
        text(b, f, "SUPPLIES", left + 98, 298, 1.1f, 0);
        text(b, f, "Upgrades now / potions on use. This run only.", left + 98, 282, .55f, 1);
        rect(b, left + 410, 284, 116, 34, .07f, .08f, .09f, .96f);
        b.setColor(Color.WHITE);
        if (goldIcon != null) b.draw(goldIcon, left + 420, 292, 17, 17);
        text(b, f, "GOLD", left + 443, 309, .5f, 1);
        align(b, f, Integer.toString(gold), new Rectangle(left + 440, 287, 80, 16), .85f, 2);
        Rectangle close = closeBounds(left);
        rect(b, close.x, close.y, close.width, close.height,
                closeHover ? .31f : .13f, .12f, .13f, 1);
        align(b, f, "X", close, .85f, 0);
        rect(b, left + 96, 274, 448, 1, .48f, .4f, .27f, .8f);

        String[] preview = stats.length >= 4 ? preview(stats[0], stats[1], stats[2], stats[3] != 0) : INFO;
        for (int row = 0; row < 4; row++) {
            float y = 220 - row * 52;
            boolean affordable = gold >= prices[row];
            boolean bought = success && messageTimer > 0 && row == lastRow;
            boolean hover = hovered == row;
            rect(b, left + 96, y, 448, 48, .09f, .105f, .12f, .97f);
            rect(b, left + 96, y, 2, 48,
                    bought ? .43f : .42f, bought ? .73f : .36f, bought ? .56f : .25f, .9f);
            rect(b, left + 104, y + 8, 32, 32, .04f, .05f, .06f, 1);
            b.setColor(Color.WHITE);
            if (icons[row] != null) b.draw(icons[row], left + 108, y + 12, 24, 24);
            text(b, f, NAMES[row], left + 148, y + 35, .83f, 0);
            text(b, f, preview[row], left + 148, y + 19, .6f, 1);
            text(b, f, "OWNED " + owned[row], left + 346, y + 13, .5f, 1);
            align(b, f, prices[row] + " G", new Rectangle(left + 374, y + 21, 66, 23), .8f,
                    affordable ? 2 : 3);
            Rectangle buy = buyBounds(left, row);
            float emphasis = affordable && hover ? (pressed ? .08f : .04f) : 0;
            if (bought) rect(b, buy.x, buy.y, buy.width, buy.height, .16f, .31f, .24f, 1);
            else if (affordable) rect(b, buy.x, buy.y, buy.width, buy.height,
                    .25f + emphasis, .22f + emphasis, .15f + emphasis, 1);
            else rect(b, buy.x, buy.y, buy.width, buy.height, .13f, .14f, .16f, 1);
            String label = bought ? "BOUGHT" : affordable ? "BUY" : "NEED " + (prices[row] - gold) + " G";
            align(b, f, label, buy, affordable || bought ? .78f : .6f,
                    bought ? 4 : affordable ? 0 : 1);
        }
        rect(b, left + 96, 57, 448, 1, .48f, .4f, .27f, .65f);
        rect(b, left + 96, 32, 448, 23, .045f, .052f, .062f, .98f);
        if (messageTimer > 0) {
            align(b, f, (success ? "OK  " : "!  ") + message,
                    new Rectangle(left + 98, 35, 346, 18), .68f, success ? 4 : 3);
        } else text(b, f, "Choose an item to purchase.", left + 100, 48, .6f, 1);
        text(b, f, "[ESC] CLOSE", left + 470, 48, .55f, 1);
        f.getData().setScale(sx, sy); f.setColor(oldFont); b.setColor(oldBatch);
    }
    static String[] preview(int damage, int maxHealth, int health, boolean powerActive) {
        return new String[]{"ATK " + damage + " -> " + (damage + 1) + " / this run",
                "HP " + health + "/" + maxHealth + " -> " + (health + GameBalance.HEALTH_UPGRADE) + "/" + (maxHealth + GameBalance.HEALTH_UPGRADE),
                "USE: HP " + health + " -> " + Math.min(maxHealth, health + GameBalance.healing(maxHealth)) + " / key [1]",
                "USE: ATK " + damage + " -> " + (damage + (powerActive ? 0 : GameBalance.POWER_BONUS))
                        + (powerActive ? " / refresh 8s" : " / 8s")};
    }
    @Override public void dispose() { pixel.dispose(); }
}
