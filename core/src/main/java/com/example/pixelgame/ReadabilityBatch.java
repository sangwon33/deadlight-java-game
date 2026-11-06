package com.example.pixelgame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** Render-only treatment: source pixels and collision geometry remain unchanged. */
final class ReadabilityBatch extends SpriteBatch {
    private static final String VERTEX = """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoords;
        void main() {
            v_color = a_color;
            v_color.a *= 255.0 / 254.0;
            v_texCoords = a_texCoord0;
            gl_Position = u_projTrans * a_position;
        }
        """;
    private static final String FRAGMENT = """
        #ifdef GL_ES
        precision mediump float;
        #endif
        varying vec4 v_color;
        varying vec2 v_texCoords;
        uniform sampler2D u_texture;
        uniform int u_mode;
        uniform vec2 u_step;
        uniform vec4 u_region;
        uniform vec4 u_edge;
        float coverage(vec2 uv) {
            if (uv.x < u_region.x || uv.y < u_region.y ||
                uv.x > u_region.z || uv.y > u_region.w) return 0.0;
            return texture2D(u_texture, uv).a;
        }
        void main() {
            vec4 pixel = texture2D(u_texture, v_texCoords) * v_color;
            if (u_mode == 4) {
                float gray = dot(pixel.rgb, vec3(0.299, 0.587, 0.114));
                pixel.rgb = mix(vec3(gray), pixel.rgb, 0.22) * vec3(0.85, 0.79, 0.73);
            } else if (u_mode == 3) {
                float gray = dot(pixel.rgb, vec3(0.299, 0.587, 0.114));
                pixel.rgb = mix(vec3(gray), pixel.rgb, 0.76) * 0.88;
            } else if (u_mode == 2) {
                float gray = dot(pixel.rgb, vec3(0.299, 0.587, 0.114));
                pixel.rgb = mix(pixel.rgb, vec3(gray), 0.22);
                // Cool atmospheric lift reduces background detail contrast.
                pixel.rgb = mix(pixel.rgb, vec3(0.16, 0.18, 0.22), 0.32);
            } else if (u_mode == 1) {
                float nearAlpha = 0.0;
                nearAlpha = max(nearAlpha, coverage(v_texCoords + vec2(u_step.x, 0.0)));
                nearAlpha = max(nearAlpha, coverage(v_texCoords - vec2(u_step.x, 0.0)));
                nearAlpha = max(nearAlpha, coverage(v_texCoords + vec2(0.0, u_step.y)));
                nearAlpha = max(nearAlpha, coverage(v_texCoords - vec2(0.0, u_step.y)));
                nearAlpha = max(nearAlpha, coverage(v_texCoords + u_step));
                nearAlpha = max(nearAlpha, coverage(v_texCoords - u_step));
                nearAlpha = max(nearAlpha, coverage(v_texCoords + vec2(u_step.x, -u_step.y)));
                nearAlpha = max(nearAlpha, coverage(v_texCoords + vec2(-u_step.x, u_step.y)));
                float edgeAlpha = nearAlpha * u_edge.a * (1.0 - pixel.a) * v_color.a;
                float alpha = pixel.a + edgeAlpha;
                pixel = vec4((pixel.rgb * pixel.a + u_edge.rgb * edgeAlpha)
                             / max(alpha, 0.0001), alpha);
            }
            gl_FragColor = pixel;
        }
        """;

    private final ShaderProgram treatment;
    private int mode;

    ReadabilityBatch() {
        this(createTreatment());
    }

    private ReadabilityBatch(ShaderProgram shader) {
        super(1000, shader);
        treatment = shader;
    }

    private static ShaderProgram createTreatment() {
        ShaderProgram shader = new ShaderProgram(VERTEX, FRAGMENT);
        if (!shader.isCompiled()) {
            String log = shader.getLog();
            shader.dispose();
            throw new IllegalStateException("Readability shader: " + log);
        }
        return shader;
    }

    void normal() { setMode(0); }
    void atmosphere() { setMode(2); }
    void mutedBackground() { setMode(3); }
    void weatheredTerrain() { setMode(4); }

    void silhouette(boolean player) {
        setMode(1);
        if (player) treatment.setUniformf("u_edge", 0.58f, 0.74f, 0.86f, 0.52f);
        else treatment.setUniformf("u_edge", 0.48f, 0.57f, 0.72f, 0.35f);
    }

    private void setMode(int value) {
        flush();
        mode = value;
        treatment.setUniformi("u_mode", value);
    }

    @Override
    public void begin() {
        super.begin();
        mode = 0;
        treatment.setUniformi("u_mode", 0);
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (mode == 1) {
            // Flush before changing per-sprite uniforms, including texture atlas bounds.
            flush();
            treatment.setUniformf("u_step", Math.abs(region.getU2() - region.getU())
                    / Math.max(1f, Math.abs(width)), Math.abs(region.getV2() - region.getV())
                    / Math.max(1f, Math.abs(height)));
            treatment.setUniformf("u_region", Math.min(region.getU(), region.getU2()),
                    Math.min(region.getV(), region.getV2()), Math.max(region.getU(), region.getU2()),
                    Math.max(region.getV(), region.getV2()));
        }
        super.draw(region, x, y, width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        treatment.dispose();
    }
}
