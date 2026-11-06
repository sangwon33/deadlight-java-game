package com.example.pixelgame;

import java.io.StringReader;
import java.util.Properties;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Original player costume in focused guard, thrust and recovery poses. */
final class ThrustSprites implements Disposable {
    private static final Color[] COLORS = {
        new Color(.92f,.97f,1,1), new Color(.2f,.82f,1,1),
        new Color(.74f,.38f,1,1), new Color(1,.75f,.2f,1)
    };
    final Texture texture = new Texture(Gdx.files.internal("player/player_thrust_v2.png"));
    private final Texture prepareTexture = new Texture(Gdx.files.internal("player/player_thrust_prepare_v4.png"));
    final TextureRegion[] frames = new TextureRegion[6];
    final float scale;
    private final float prepareScale;
    static final float FOOT_RATIO = 62f / 512f;
    static Color chargeColor(int stage) { return COLORS[Math.max(0,Math.min(3,stage-1))]; }
    ThrustSprites() {
        Properties p = new Properties(), prepare = new Properties();
        try {
            p.load(new StringReader(Gdx.files.internal("player/thrust-layout.properties").readString("UTF-8")));
            prepare.load(new StringReader(Gdx.files.internal("player/thrust-prepare-layout.properties").readString("UTF-8")));
        } catch (Exception e) {
            texture.dispose(); prepareTexture.dispose(); throw new GdxRuntimeException("Thrust sprite layout",e);
        }
        // One physical scale across all poses; crouching and leaning naturally lower the head.
        scale = Float.parseFloat(p.getProperty("renderBodyHeight")) / Float.parseFloat(p.getProperty("bodyHeight"));
        prepareScale = Float.parseFloat(prepare.getProperty("renderBodyHeight")) / Float.parseFloat(prepare.getProperty("bodyHeight"));
        texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        prepareTexture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        for (int i=0;i<6;i++) frames[i]=new TextureRegion(texture,(i%3)*768,(i/3)*512,768,512);
        // Preparation is separate so its hands can be revised without redrawing the rush.
        for (int i=0;i<2;i++) frames[i]=new TextureRegion(prepareTexture,i*768,0,768,1024);
    }
    float width() { return 768*scale; }
    float height() { return 512*scale; }
    float width(TextureRegion frame) { return isPreparation(frame) ? 768*prepareScale : width(); }
    float height(TextureRegion frame) { return isPreparation(frame) ? 1024*prepareScale : height(); }
    private boolean isPreparation(TextureRegion frame) { return frame == frames[0] || frame == frames[1]; }
    @Override public void dispose() { texture.dispose(); prepareTexture.dispose(); }
}
