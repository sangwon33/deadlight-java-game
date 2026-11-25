package com.example.pixelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Supplemental poses share one body scale; bent knees never enlarge the character. */
final class HeroTransitions implements AutoCloseable {
    private final Texture texture = new Texture(Gdx.files.internal("art/hero-transitions-v1.png"));
    private final TextureRegion[] frames = new TextureRegion[8];
    private static final float SCALE = GameBalance.character(78) / 327f;
    // Local source coordinates, measured at the boot soles rather than the cloak or sword.
    private static final float[][] FEET = {{167,426},{184,427},{213,426},{172,427},
            {213.5f,423},{183,423},{153,393},{142,423}};
    private static final int[][] INK = {{27,119,372,426},{60,100,337,427},{45,138,324,426},
            {51,98,311,427},{54,120,303,423},{50,111,297,423},{28,75,310,393},{22,140,347,423}};
    HeroTransitions() {
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        for (int i=0;i<frames.length;i++) {
            // Transparent gutters remain inside each region for the screen-space contour.
            frames[i]=new TextureRegion(texture,i%4*384+2,i/4*512+2,380,508);
        }
    }
    void draw(SpriteBatch b,int frame,float center,float floor,boolean right) {
        TextureRegion r=frames[frame];float ax=(FEET[frame][0]-2)*SCALE;
        b.draw(r,right?center-ax:center+ax,floor-(510-FEET[frame][1])*SCALE,
                (right?1:-1)*380*SCALE,508*SCALE);
    }
    Rectangle bounds(int frame,float center,float floor,boolean right,Rectangle out) {
        int[] ink=INK[frame];float left=right?ink[0]-FEET[frame][0]:FEET[frame][0]-ink[2];
        return out.set(center+left*SCALE,floor,(ink[2]-ink[0])*SCALE,(FEET[frame][1]-ink[1])*SCALE);
    }
    @Override public void close(){texture.dispose();}
}
