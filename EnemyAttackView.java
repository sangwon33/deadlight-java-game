package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/** Small range brackets and a countdown marker; no sprite tint or bitmap edits. */
final class EnemyAttackView {
    void draw(ShapeRenderer s, Iterable<Enemy> enemies) {
        boolean blend = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            if (!e.isAttackCueVisible()) continue;
            Rectangle r = e.getAttackBounds(), body = e.getBounds();
            float progress = e.getAttackCueProgress();
            boolean strike = progress >= 1;
            boolean right = r.x >= body.x + body.width / 2;
            float tip = right ? r.x + r.width : r.x;
            float tickX = right ? tip - 7 : tip;
            float markerX = (r.x + r.width / 2 + tip) / 2;
            float markerY = body.y + body.height + 23;
            // Dark underlines remain legible against bright grass and castle stone.
            s.setColor(.02f, .025f, .04f, .82f);
            s.rect(r.x - 1, r.y - 1, r.width + 2, 3);
            s.rect(tip - 1, r.y - 1, 3, r.height + 2);
            s.rect(tickX - 1, r.y + r.height - 1, 10, 3);
            s.rect(markerX - 7, markerY - 8, 14, 17);
            if (strike) s.setColor(1, .35f, .25f, .95f);
            else s.setColor(1, .77f, .36f, .58f + progress * .35f);
            s.rect(r.x, r.y, r.width, 1);
            s.rect(tip, r.y, 1, r.height);
            s.rect(tickX, r.y + r.height, 8, 1);
            // The exclamation mark and draining line communicate time without color alone.
            s.rect(markerX - 1, markerY, 2, 6);
            s.rect(markerX - 1, markerY - 4, 2, 2);
            s.rect(markerX - 5, markerY - 7, 10 * (1 - progress), 1);
        }
        s.end();
        if (!blend) Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
