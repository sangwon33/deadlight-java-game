package com.example.pixelgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Shared silver silhouettes for the HUD and optional pointer controls. */
final class ActionIcons {
    private static final Color FACE = new Color(.08f, .13f, .18f, 1);
    private ActionIcons() {}

    static void draw(SpriteBatch batch, UiTheme ui, KeyBindings.Action action,
                     float x, float y, float scale, Color color, float alpha) {
        float edge = Math.max(.8f, alpha);
        shape(batch, ui, action, x - .85f, y, scale, UiTheme.SURFACE, edge);
        shape(batch, ui, action, x + .85f, y, scale, UiTheme.SURFACE, edge);
        shape(batch, ui, action, x, y - .85f, scale, UiTheme.SURFACE, edge);
        shape(batch, ui, action, x, y + .85f, scale, UiTheme.SURFACE, edge);
        shape(batch, ui, action, x, y, scale, color, alpha);
    }

    /** A small matte medallion keeps terrain detail from cutting through the silhouette. */
    static void plate(SpriteBatch b, UiTheme ui, float x, float y, float radius, boolean focused) {
        ui.triangle(b,x-radius-1,y,x,y+radius+1,x+radius+1,y,UiTheme.SURFACE,.7f);
        ui.triangle(b,x-radius-1,y,x,y-radius-1,x+radius+1,y,UiTheme.SURFACE,.7f);
        ui.triangle(b,x-radius,y,x,y+radius,x+radius,y,FACE,.9f);
        ui.triangle(b,x-radius,y,x,y-radius,x+radius,y,FACE,.9f);
        ui.triangle(b,x-radius+1,y,x,y+radius-1,x+radius-1,y,UiTheme.BORDER,.15f);
        Color rim=focused?UiTheme.TEXT:UiTheme.GOLD;
        ui.diamond(b,x,y,radius,rim,focused?1:.8f);
        ui.line(b,x-radius+2,y,x,y+radius-2,.65f,rim,.35f);
        ui.line(b,x,y+radius-2,x+radius-2,y,.65f,rim,.35f);
    }

    private static void shape(SpriteBatch b, UiTheme ui, KeyBindings.Action action,
                              float x, float y, float s, Color c, float a) {
        switch (action) {
            case LEFT, RIGHT -> {
                float d = action == KeyBindings.Action.LEFT ? -1 : 1;
                line(b, ui, x, y, s, d * 4, 0, -d * 3, 6, 1.7f, c, a);
                line(b, ui, x, y, s, d * 4, 0, -d * 3, -6, 1.7f, c, a);
            }
            case JUMP -> {
                line(b, ui, x, y, s, 0, -4, 0, 7, 1.7f, c, a);
                line(b, ui, x, y, s, 0, 7, -5, 2, 1.7f, c, a);
                line(b, ui, x, y, s, 0, 7, 5, 2, 1.7f, c, a);
                line(b, ui, x, y, s, -6, -7, 6, -7, 1.1f, c, a);
            }
            case ATTACK -> {
                ui.triangle(b, x - 2 * s, y - s, x + 9 * s, y + 10 * s,
                        x + s, y - 2 * s, c, a);
                line(b, ui, x, y, s, -2, -1, 9, 10, 1.1f, c, a);
                line(b, ui, x, y, s, -5, 0, 1, -6, 1.8f, c, a);
                line(b, ui, x, y, s, -7, -8, -2, -3, 2.1f, c, a);
                line(b, ui, x, y, s, -8, -7, -6, -9, 1.7f, c, a);
            }
            case SKILL -> {
                ui.triangle(b, x - 2 * s, y + 3 * s, x + 12 * s, y + 2 * s,
                        x - 2 * s, y + s, c, a);
                line(b, ui, x, y, s, -3, -3, -3, 7, 1.8f, c, a);
                line(b, ui, x, y, s, -9, 2, -3, 2, 2, c, a);
                line(b, ui, x, y, s, -11, -4, 3, -4, 1.1f, c, a * .8f);
                line(b, ui, x, y, s, -12, 7, -7, 7, 1, c, a * .65f);
            }
            case DASH -> {
                for (int i = 0; i < 2; i++) {
                    float dx = i * 6;
                    line(b, ui, x, y, s, dx - 3, -6, dx + 3, 0, 1.8f, c, a);
                    line(b, ui, x, y, s, dx + 3, 0, dx - 3, 6, 1.8f, c, a);
                }
                line(b, ui, x, y, s, -12, 0, -5, 0, 1.3f, c, a * .8f);
                line(b, ui, x, y, s, -10, 4, -6, 4, 1, c, a * .6f);
                line(b, ui, x, y, s, -10, -4, -6, -4, 1, c, a * .6f);
            }
            case INVENTORY -> {
                line(b, ui, x, y, s, -3, 7, -3, 10, 1.5f, c, a);
                line(b, ui, x, y, s, -3, 10, 3, 10, 1.5f, c, a);
                line(b, ui, x, y, s, 3, 10, 3, 7, 1.5f, c, a);
                line(b, ui, x, y, s, -7, -7, -7, 4, 1.6f, c, a);
                line(b, ui, x, y, s, -7, 4, -5, 7, 1.6f, c, a);
                line(b, ui, x, y, s, -5, 7, 5, 7, 1.6f, c, a);
                line(b, ui, x, y, s, 5, 7, 7, 4, 1.6f, c, a);
                line(b, ui, x, y, s, 7, 4, 7, -7, 1.6f, c, a);
                line(b, ui, x, y, s, 7, -7, -7, -7, 1.6f, c, a);
                line(b, ui, x, y, s, -7, 3, 7, 3, 1.3f, c, a);
                line(b, ui, x, y, s, 0, 3, 0, 0, 2, c, a);
                line(b, ui, x, y, s, -4, -2, 4, -2, 1.2f, c, a);
                line(b, ui, x, y, s, -4, -2, -4, -5, 1.2f, c, a);
                line(b, ui, x, y, s, 4, -2, 4, -5, 1.2f, c, a);
                line(b, ui, x, y, s, -9, -4, -9, 3, 1.2f, c, a * .85f);
                line(b, ui, x, y, s, 9, -4, 9, 3, 1.2f, c, a * .85f);
            }
            default -> { }
        }
    }

    private static void line(SpriteBatch b, UiTheme ui, float x, float y, float s,
                             float ax, float ay, float bx, float by, float width, Color c, float a) {
        ui.line(b, x + ax * s, y + ay * s, x + bx * s, y + by * s, width * s, c, a);
    }

    static void pause(SpriteBatch b, UiTheme ui, float x, float y, Color color) {
        ui.rect(b, x - 5, y - 6, 3.3f, 12, UiTheme.SURFACE, .95f);
        ui.rect(b, x + 2, y - 6, 3.3f, 12, UiTheme.SURFACE, .95f);
        ui.rect(b, x - 5.5f, y - 5.5f, 3.3f, 12, color, 1);
        ui.rect(b, x + 1.5f, y - 5.5f, 3.3f, 12, color, 1);
    }
}
