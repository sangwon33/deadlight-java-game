package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Align the solid surface, rather than transparent image padding, with collision height. */
final class TerrainTile {
    final TextureRegion region;
    final int surfaceRow;

    TerrainTile(Texture texture, int surfaceRow) {
        region = new TextureRegion(texture);
        this.surfaceRow = surfaceRow;
    }

    TerrainTile(Texture texture, String asset, boolean trimSides) {
        Pixmap pixels = new Pixmap(Gdx.files.internal(asset));
        try {
            int width = pixels.getWidth(), height = pixels.getHeight();
            int[] coverage = new int[height];
            int densest = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) if ((pixels.getPixel(x, y) & 255) > 96) coverage[y]++;
                if (coverage[y] > coverage[densest]) densest = y;
            }
            int surface = 0;
            // Ignore isolated grass tufts above the continuous plank/ground surface.
            while (surface < densest && coverage[surface] < coverage[densest] * .95f) surface++;
            surfaceRow = surface;
            int left = 0, right = width;
            if (trimSides && coverage[densest] > 0) {
                while (left < right && (pixels.getPixel(left, densest) & 255) <= 96) left++;
                while (right > left && (pixels.getPixel(right - 1, densest) & 255) <= 96) right--;
            }
            region = new TextureRegion(texture, left, 0, Math.max(1, right - left), height);
        } finally { pixels.dispose(); }
    }

    float drawY(float surfaceY, float height) {
        return surfaceY - height + height * surfaceRow / region.getRegionHeight();
    }

    void draw(SpriteBatch batch, float x, float surfaceY, float width, float height) {
        batch.draw(region, x, drawY(surfaceY, height), width, height);
    }
}
