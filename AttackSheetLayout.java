package com.example.pixelgame;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;

/** Drawing dimensions and the common foot anchor for losslessly repacked attack sheets. */
final class AttackSheetLayout {
    private static Map<String, AttackSheetLayout> layouts;
    final float width, height, foot;

    private AttackSheetLayout(float width, float height, float foot) {
        this.width = width;
        this.height = height;
        this.foot = foot;
    }

    static AttackSheetLayout get(String asset) {
        if (layouts == null) {
            Map<String, AttackSheetLayout> loaded = new HashMap<>();
            for (String line : Gdx.files.internal("attack-layouts.tsv").readString("UTF-8").split("\\R")) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split("\\t");
                if (fields.length != 5) throw new IllegalStateException("Invalid attack layout: " + line);
                loaded.put(fields[0], new AttackSheetLayout(Float.parseFloat(fields[2]),
                        Float.parseFloat(fields[3]), Float.parseFloat(fields[4])));
            }
            layouts = loaded;
        }
        AttackSheetLayout layout = layouts.get(asset);
        if (layout == null) throw new IllegalArgumentException("Missing attack layout: " + asset);
        return layout;
    }
}
