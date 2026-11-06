package com.example.pixelgame;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** Layout sizes retain the original 18px units; glyphs are rasterized at 4x resolution. */
final class UiTypography {
    static final int RASTER_SCALE = 4;
    static final int RASTER_PIXELS = 18 * RASTER_SCALE;

    private UiTypography() {}

    static void size(BitmapFont font, float logicalSize) {
        font.getData().setScale(logicalSize / RASTER_SCALE);
    }
}
