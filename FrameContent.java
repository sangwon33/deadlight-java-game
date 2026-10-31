package com.example.pixelgame;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Locate the main sprite in a walking cell, excluding fragments from a neighboring cell. */
final class FrameContent {
    static int footFromBottom(Pixmap pixels, TextureRegion frame) {
        for (int row = frame.getRegionHeight() - 1; row >= 0; row--) {
            int solid = 0;
            for (int col = 0; col < frame.getRegionWidth(); col++)
                if ((pixels.getPixel(frame.getRegionX() + col, frame.getRegionY() + row) & 255) > 96) solid++;
            if (solid >= 3) return frame.getRegionHeight() - row - 1;
        }
        return 0;
    }
    static Rectangle mainBody(Pixmap pixels, TextureRegion tile) {
        int w = tile.getRegionWidth(), h = tile.getRegionHeight();
        boolean[] seen = new boolean[w * h];
        int[] queue = new int[w * h];
        int best = 0, left = 0, top = 0, right = w, bottom = h;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int start = y * w + x;
            if (seen[start]) continue;
            seen[start] = true;
            if ((pixels.getPixel(tile.getRegionX() + x, tile.getRegionY() + y) & 255) <= 96) continue;
            int head = 0, tail = 1, minX = x, minY = y, maxX = x, maxY = y;
            queue[0] = start;
            while (head < tail) {
                int pos = queue[head++], px = pos % w, py = pos / w;
                minX = Math.min(minX, px); maxX = Math.max(maxX, px);
                minY = Math.min(minY, py); maxY = Math.max(maxY, py);
                for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                    int nx = px + dx, ny = py + dy;
                    if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                    int index = ny * w + nx;
                    if (seen[index]) continue;
                    seen[index] = true;
                    if ((pixels.getPixel(tile.getRegionX() + nx, tile.getRegionY() + ny) & 255) > 96)
                        queue[tail++] = index;
                }
            }
            if (tail > best) {
                best = tail; left = minX; right = maxX + 1; top = minY; bottom = maxY + 1;
            }
        }
        // Keep antialiased edge pixels around the connected, opaque body.
        left = Math.max(0, left - 2); top = Math.max(0, top - 2);
        right = Math.min(w, right + 2); bottom = Math.min(h, bottom + 2);
        return new Rectangle(tile.getRegionX() + left, tile.getRegionY() + top, right - left, bottom - top);
    }
}
