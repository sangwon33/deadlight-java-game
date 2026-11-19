package com.example.pixelgame;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Deterministic region mechanisms; reset together with the region-entry checkpoint. */
final class WorldGimmicks {
    private static final Color CREVICE = new Color(.055f, .075f, .09f, 1);
    private static final Color STONE = new Color(.32f, .39f, .43f, 1);
    private static final Color PALE_STONE = new Color(.76f, .79f, .78f, 1);
    private static final Color MIST = new Color(.6f, .76f, .79f, 1);
    private static final Color COPPER = new Color(.84f, .51f, .34f, 1);
    private static final Color HEAT = new Color(.97f, .82f, .63f, 1);
    private static final Color STEEL = new Color(.55f, .64f, .68f, 1);
    private static final Color EDGE = new Color(.91f, .93f, .88f, 1);
    private static final Color STEAM = new Color(.75f, .65f, .55f, 1);
    private final float[] shardVertices = new float[20];
    private static final float BRIDGE_COLLAPSE_DELAY = .85f;
    static final class Lift {
        final Rectangle bounds;final float low,high,speed;float direction=1;
        Lift(float x,float low,float high,float speed){bounds=new Rectangle(x,low-18,150,18);this.low=low;this.high=high;this.speed=speed;}
    }
    static final class Wind {
        final float x,floor,speed;float cooldown;
        Wind(float x,float floor,float speed){this.x=x;this.floor=floor;this.speed=speed;}
    }
    static final class Gate {
        final Rectangle bounds;final float leverX,floor;boolean open;
        Gate(float x,float floor){bounds=new Rectangle(x,floor,28,130);leverX=x-105;this.floor=floor;}
    }
    static final class Bridge {
        final Rectangle bounds;final float width;float stress,respawn;
        Bridge(Rectangle bounds){this.bounds=bounds;this.width=bounds.width;}
    }
    static final class Jet {
        static final float WARNING_START = .55f;
        static final float ACTIVE_START = 1.2f;
        static final float ACTIVE_END = 1.85f;
        static final float WIDTH = 42f;
        static final float HEIGHT = 90f;
        final float x,floor,period;float phase;
        Jet(float x,float floor,float phase,float period){this.x=x;this.floor=floor;this.phase=phase;this.period=period;}
        boolean active(){return phase>=ACTIVE_START&&phase<ACTIVE_END;}
        boolean warning(){return phase>=WARNING_START&&phase<ACTIVE_START;}
        Rectangle bounds(){return new Rectangle(x-WIDTH/2,floor,WIDTH,HEIGHT);}
    }
    static final class Well {final float x,floor;boolean used;Well(float x,float floor){this.x=x;this.floor=floor;}}
    final List<Bridge> bridges=new ArrayList<>();
    final List<Jet> jets=new ArrayList<>();
    final List<Well> wells=new ArrayList<>();
    private boolean healedNotice;
    final List<Lift> lifts=new ArrayList<>();
    final List<Wind> winds=new ArrayList<>();
    final List<Gate> gates=new ArrayList<>();
    final List<Rectangle> spikes=new ArrayList<>();
    private float time;private boolean openedNotice;
    void enter(int stage,ArrayList<Rectangle> platforms,ArrayList<Rectangle> solids){
        lifts.clear();winds.clear();gates.clear();spikes.clear();bridges.clear();jets.clear();wells.clear();time=0;openedNotice=healedNotice=false;
        if(stage<1||stage>3)return;
        lifts.add(new Lift(StageLayout.x(stage,1560),-140,StageLayout.y(stage,510),155));
        lifts.add(new Lift(StageLayout.x(stage,3640),60,StageLayout.y(stage,800),170));
        for(Lift lift:lifts){lift.bounds.width=Math.max(90,StageLayout.x(stage,150));platforms.add(lift.bounds);}
        winds.add(new Wind(StageLayout.x(stage,645),60,870));winds.add(new Wind(StageLayout.x(stage,1610),-160,1050));
        winds.add(new Wind(StageLayout.x(stage,2260),60,1180));
        winds.add(new Wind(StageLayout.x(stage,2970+(stage-1)*25),StageLayout.y(stage,860),930));
        bridges.add(new Bridge(platforms.get(0)));bridges.add(new Bridge(platforms.get(1)));
        jets.add(new Jet(StageLayout.x(stage,3270),60,0,3.4f-(stage-1)*.25f));
        jets.add(new Jet(StageLayout.width(stage)-560,60,1.7f,3.7f-(stage-1)*.25f));
        wells.add(new Well(StageLayout.x(stage,3100),60));
        Gate gate=new Gate(StageLayout.x(stage,2960),60);gates.add(gate);solids.add(gate.bounds);
        spikes.add(new Rectangle(StageLayout.x(stage,1060),-159,46,14));
        spikes.add(new Rectangle(StageLayout.x(stage,2110),61,60,14));
        spikes.add(new Rectangle(StageLayout.x(stage,3500+(stage-1)*80),61,70,14));
    }
    void beforePhysics(float dt,Player p){
        time+=dt;
        for(Bridge bridge:bridges){
            if(bridge.respawn>0){bridge.respawn=Math.max(0,bridge.respawn-dt);if(bridge.respawn==0){bridge.bounds.width=bridge.width;bridge.stress=0;}}
            else if(p.isGrounded()&&p.getSupport()==bridge.bounds){bridge.stress+=dt;if(bridge.stress>=BRIDGE_COLLAPSE_DELAY){bridge.bounds.width=0;bridge.respawn=4;}}
            else bridge.stress=Math.max(0,bridge.stress-dt*.5f);
        }
        for(Jet jet:jets)jet.phase=(jet.phase+dt)%jet.period;
        if(KeyBindings.justPressed(KeyBindings.Action.INTERACT))for(Well well:wells){
            if(!well.used&&Math.abs(p.getCenterX()-well.x)<55&&Math.abs(p.getY()-well.floor)<45&&p.heal(30)){well.used=true;healedNotice=true;break;}
        }
        for(Lift lift:lifts){
            float old=lift.bounds.y,top=old+18;
            top+=lift.direction*lift.speed*dt;
            if(top>lift.high){top=lift.high-(top-lift.high);lift.direction=-1;}
            if(top<lift.low){top=lift.low+(lift.low-top);lift.direction=1;}
            lift.bounds.y=top-18;
            if(p.isGrounded()&&p.getSupport()==lift.bounds&&!p.wantsDropThrough())p.carry(0,lift.bounds.y-old);
        }
        for(Wind wind:winds)wind.cooldown=Math.max(0,wind.cooldown-dt);
        if(KeyBindings.justPressed(KeyBindings.Action.INTERACT))for(Gate gate:gates){
            if(!gate.open&&nearLever(p,gate)){gate.open=true;gate.bounds.width=0;openedNotice=true;break;}
        }
    }
    void afterPhysics(Player p){
        for(Jet jet:jets)if(jet.active()&&p.getBounds().overlaps(jet.bounds()))p.takeDamage(GameBalance.ENEMY_DAMAGE,jet.x);
        for(Wind wind:winds)if(wind.cooldown<=0&&!p.isCrouching()&&p.isGrounded()
                &&Math.abs(p.getY()-wind.floor)<3&&Math.abs(p.getCenterX()-wind.x)<28){
            p.launch(wind.speed);wind.cooldown=.65f;
        }
        for(Rectangle spike:spikes)if(p.isAlive()&&p.getBounds().overlaps(spike))p.takeDamage(GameBalance.ENEMY_DAMAGE,spike.x+spike.width/2);
    }
    private boolean nearLever(Player p,Gate gate){return Math.abs(p.getCenterX()-gate.leverX)<80&&Math.abs(p.getY()-gate.floor)<55;}
    boolean takeHealedNotice(){boolean value=healedNotice;healedNotice=false;return value;}
    boolean takeOpenedNotice(){boolean result=openedNotice;openedNotice=false;return result;}
    void draw(SpriteBatch batch, UiTheme ui, TerrainTile stone, MoonAtlas world) {
        for (Bridge bridge : bridges) drawBridge(batch, ui, bridge);
        for (Jet jet : jets) drawJet(batch, ui, world, jet);
        for (Rectangle spike : spikes) drawSpikes(batch, ui, stone, spike);
        for (Well well : wells) {
            float packed = batch.getPackedColor();
            batch.setColor(1, 1, 1, well.used ? .46f : 1);
            world.draw(batch, 5, 5, well.x, well.floor, 44, true);
            batch.setPackedColor(packed);
        }
        for (Lift lift : lifts) drawLift(batch, ui, lift);
        for (Wind wind : winds) drawWind(batch, ui, stone, wind);
        for (Gate gate : gates) drawGate(batch, ui, stone, gate);
    }

    /** World hints stay above supplies, with the same lettering as the other region hints. */
    void drawLabels(SpriteBatch batch, UiTheme ui, BitmapFont font, Player player) {
        for (Bridge bridge : bridges) {
            if (bridge.respawn <= 0 && player.getSupport() == bridge.bounds) {
                warningLabel(batch, ui, font, "발판에 금이 갑니다", bridge.bounds.x + bridge.width / 2,
                        bridge.bounds.y + bridge.bounds.height + 90, Math.min(150, bridge.width));
            }
        }
        for (Jet jet : jets) {
            if (jet.warning() && Math.abs(player.getCenterX() - jet.x) < 135
                    && Math.abs(player.getY() - jet.floor) < 85) {
                warningLabel(batch, ui, font, "곧 분출합니다", jet.x, jet.floor + Jet.HEIGHT + 39, 116);
            }
        }
        for (Well well : wells) {
            if (!well.used && Math.abs(player.getCenterX() - well.x) < 65
                    && Math.abs(player.getY() - well.floor) < 50) {
                String label = player.getHealth() < player.getMaxHealth()
                        ? "[" + KeyBindings.name(KeyBindings.Action.INTERACT) + "] 회복 +30"
                        : "회복 우물 · 1회 사용";
                ui.centered(batch, font, label, new Rectangle(well.x - 70, well.floor + 92, 140, 20),
                        .65f, UiTheme.SUCCESS);
            }
        }
        for (Gate gate : gates) {
            if (!gate.open && nearLever(player, gate)) {
                Rectangle label = new Rectangle(gate.leverX - 60, gate.floor + 84, 120, 21);
                ui.centered(batch, font, "[" + KeyBindings.name(KeyBindings.Action.INTERACT)
                        + "] 석문 열기", label, .61f, PALE_STONE);
            }
        }
    }

    private void drawBridge(SpriteBatch batch, UiTheme ui, Bridge bridge) {
        float x = bridge.bounds.x, top = bridge.bounds.y + bridge.bounds.height;
        float right = x + bridge.width;
        if (bridge.respawn > 0) {
            // Broken end stones show the gap; do not draw a seemingly walkable line across it.
            ui.triangle(batch, x, top, x + 5, top - 3, x, top - 10, STONE, .95f);
            ui.triangle(batch, right, top, right - 5, top - 3, right, top - 9, STONE, .95f);
            ui.line(batch, x, top, x + 4, top - 2, .8f, PALE_STONE, .85f);
            ui.line(batch, right - 4, top - 2, right, top, .8f, PALE_STONE, .85f);
            return;
        }
        float progress = Math.min(1, bridge.stress / BRIDGE_COLLAPSE_DELAY);
        // Warm, chipped stone edges separate a fragile slab from the ordinary cold stone.
        ui.verticalFade(batch, x, top - 6, bridge.width, 6, COPPER, .02f, .16f + progress * .12f);
        int cracks = Math.max(3, (int) (bridge.width / 23));
        for (int i = 0; i < cracks; i++) {
            float px = x + 8 + i * (bridge.width - 16) / Math.max(1, cracks - 1);
            float bend = (i % 2 == 0 ? -1 : 1) * (3 + i % 3);
            float depth = 7 + i % 4 + progress * 8;
            ui.line(batch, px, top, px + bend, top - depth * .5f, 3.6f, CREVICE, 1);
            ui.line(batch, px + bend, top - depth * .5f, px - 2, top - depth, 2.7f, CREVICE, 1);
            ui.line(batch, px, top, px + bend, top - depth * .5f, 1.1f, COPPER, .7f + progress * .25f);
            ui.line(batch, px + bend, top - depth * .5f, px - 2, top - depth, .8f, COPPER, .65f + progress * .3f);
            ui.line(batch, px + 1, top - 1, px + bend + 1, top - depth * .5f,
                    1.1f, progress > .2f ? HEAT : PALE_STONE, .85f);
            ui.line(batch, px + bend, top - depth * .5f, px + bend + 4,
                    top - depth * .7f, .8f, CREVICE, .9f);
            if (progress > .1f) {
                float fall = gentle() ? 5 : (time * 29 + i * 7) % 23;
                ui.triangle(batch, px - 1.2f, top - 12 - fall, px + 1.2f, top - 13 - fall,
                        px, top - 15.5f - fall, PALE_STONE, .5f + progress * .4f);
            }
        }
        // Irregular worn lips distinguish this slab without replacing its painted stone surface.
        for (int i = 0; i < cracks; i++) {
            float start = x + i * bridge.width / cracks;
            ui.line(batch, start + 3, top - .4f, start + 15 + i % 4, top - .4f,
                    1.1f, HEAT, .85f);
        }
    }

    private void drawJet(SpriteBatch batch, UiTheme ui, MoonAtlas world, Jet jet) {
        Rectangle area = jet.bounds();
        float packed = batch.getPackedColor();
        batch.setColor(.86f, .86f, .88f, 1);
        world.draw(batch, 7, 7, jet.x, jet.floor, 25, true);
        batch.setPackedColor(packed);
        float heat = jet.active() ? 1 : jet.warning()
                ? .7f + .3f * (jet.phase - Jet.WARNING_START) / (Jet.ACTIVE_START - Jet.WARNING_START) : .32f;
        // The original cracked altar is the emitter. Its fissure heats up before the ash erupts.
        ui.line(batch, jet.x - 7, jet.floor + 20, jet.x - 1, jet.floor + 17, 2, CREVICE, 1);
        ui.line(batch, jet.x - 1, jet.floor + 17, jet.x + 2, jet.floor + 8, 2.4f, CREVICE, 1);
        ui.line(batch, jet.x - 6, jet.floor + 20, jet.x, jet.floor + 17, 3.5f, COPPER, heat * .4f);
        ui.line(batch, jet.x, jet.floor + 17, jet.x + 2, jet.floor + 8, 3.5f, COPPER, heat * .4f);
        ui.line(batch, jet.x - 6, jet.floor + 20, jet.x, jet.floor + 17, 1.5f, COPPER, heat);
        ui.line(batch, jet.x, jet.floor + 17, jet.x + 2, jet.floor + 8, 1.5f, COPPER, heat);
        if (!jet.warning() && !jet.active()) return;

        boolean active = jet.active();
        ui.verticalFade(batch, jet.x - 13, jet.floor + 13, 26, active ? 32 : 23,
                COPPER, active ? .46f : .34f, 0);
        ui.line(batch, area.x + 1, jet.floor + 1, area.x + 7, jet.floor + 3, 1.6f, COPPER, heat * .8f);
        ui.line(batch, area.x + area.width - 7, jet.floor + 3, area.x + area.width - 1,
                jet.floor + 1, 1.6f, COPPER, heat * .8f);
        // Soft ash occupies the damaging footprint; there is no rectangular frame or laser grille.
        ui.verticalFade(batch, area.x, area.y, area.width, area.height, STEAM,
                active ? .48f : .19f, active ? .17f : .045f);
        int wisps = active ? 6 : 4;
        for (int i = 0; i < wisps; i++) {
            float center = area.x + 4 + i * (area.width - 8) / (wisps - 1);
            float height = active ? Jet.HEIGHT - (i % 3) * 4 : 48 + i * 12;
            float motion = gentle() ? 0 : time * 3;
            float previousX = center, previousY = jet.floor + 6;
            for (int n = 1; n <= 5; n++) {
                float t = n / 5f;
                float nextY = jet.floor + 6 + (height - 6) * t;
                float nextX = center + (float) Math.sin(t * 5 + i * 1.7f + motion) * 3;
                nextX = Math.max(area.x + 2, Math.min(area.x + area.width - 2, nextX));
                float half = (active ? 4.8f : 3) * (1 - t * .72f);
                float alpha = (active ? .65f : .32f) * (1 - t * .4f);
                ui.triangle(batch, previousX - half, previousY, previousX + half,
                        previousY, nextX + half * .65f, nextY, STEAM, alpha);
                ui.triangle(batch, previousX - half, previousY, nextX + half * .65f,
                        nextY, nextX - half * .65f, nextY, STEAM, alpha);
                ui.line(batch, previousX, previousY, nextX, nextY,
                        active ? 3.1f : 2.1f, CREVICE, active ? .28f : .18f);
                ui.line(batch, previousX, previousY, nextX, nextY,
                        active ? 1.4f - t * .4f : .85f, HEAT, active ? .9f - t * .25f : .48f - t * .16f);
                previousX = nextX;
                previousY = nextY;
            }
        }
        for (int i = 0; i < (active ? 16 : 7); i++) {
            float rise = gentle() ? (i * 13) % (int) Jet.HEIGHT : (time * (active ? 48 : 25) + i * 13) % Jet.HEIGHT;
            float x = area.x + 3 + (i * 17) % (int) (area.width - 6);
            float alpha = (active ? .95f : .8f) * (1 - rise / Jet.HEIGHT * .4f);
            ui.line(batch, x, jet.floor + rise, x + (i % 2 == 0 ? .6f : -.6f),
                    jet.floor + Math.min(Jet.HEIGHT, rise + (active ? 3 : 1.8f)),
                    1.1f, i % 3 == 0 ? COPPER : HEAT, alpha);
        }
    }

    private void drawSpikes(SpriteBatch batch, UiTheme ui, TerrainTile stone, Rectangle spike) {
        // Sample the existing terrain texture so these are weathered shards, not flat icons.
        float packed = batch.getPackedColor();
        batch.setColor(.9f, .92f, .94f, 1);
        stone.draw(batch, spike.x - 1, spike.y + 3, spike.width + 2, 6);
        batch.setPackedColor(packed);
        int teeth = Math.max(1, (int) Math.ceil(spike.width / 9));
        float width = spike.width / teeth;
        for (int i = 0; i < teeth; i++) {
            float left = spike.x + i * width, right = left + width;
            float middle = left + width * (i % 2 == 0 ? .38f : .64f);
            float tip = spike.y + spike.height - (i % 3 == 1 ? 1.5f : 0);
            // A dark silhouette under a broad silver facet reads over bright and dark scenery.
            ui.triangle(batch, left - .8f, spike.y, middle, tip + .3f, right + .8f, spike.y, CREVICE, 1);
            stoneShard(batch, stone, left, spike.y + 1, middle, tip, right, spike.y + 1);
            ui.triangle(batch, left + 1, spike.y + 2, middle, tip - .6f,
                    middle + 1, spike.y + 2, STEEL, .8f);
            ui.line(batch, left, spike.y + 1, middle, tip, 1.9f, CREVICE, 1);
            ui.triangle(batch, middle, tip - 1, right - .6f, spike.y + 1,
                    middle + .7f, spike.y + 2, STONE, .76f);
            ui.line(batch, middle, tip, right - .4f, spike.y + 1, 2.7f, CREVICE, .9f);
            ui.line(batch, middle, tip, right - .4f, spike.y + 1, 1.25f, EDGE, 1);
            ui.line(batch, middle - .6f, tip - 4, middle, tip, 1.2f, EDGE, 1);
            ui.line(batch, left + 1, spike.y + 2, middle, spike.y + 3, 1.1f, COPPER, .85f);
        }
    }

    private void stoneShard(SpriteBatch batch, TerrainTile stone,
                            float ax, float ay, float bx, float by, float cx, float cy) {
        float[] vertices = shardVertices;
        float color = Color.toFloatBits(.94f, .97f, 1, 1);
        float u = stone.region.getU(), v = stone.region.getV();
        float du = stone.region.getU2() - u, dv = stone.region.getV2() - v;
        vertex(vertices, 0, ax, ay, color, u + du * .24f, v + dv * .58f);
        vertex(vertices, 5, bx, by, color, u + du * .48f, v + dv * .13f);
        vertex(vertices, 10, cx, cy, color, u + du * .72f, v + dv * .58f);
        vertex(vertices, 15, cx, cy, color, u + du * .72f, v + dv * .58f);
        batch.draw(stone.region.getTexture(), vertices, 0, 20);
    }

    private void vertex(float[] vertices, int index, float x, float y, float color, float u, float v) {
        vertices[index] = x; vertices[index + 1] = y; vertices[index + 2] = color;
        vertices[index + 3] = u; vertices[index + 4] = v;
    }

    private void drawLift(SpriteBatch batch, UiTheme ui, Lift lift) {
        float center = lift.bounds.x + lift.bounds.width / 2, top = lift.bounds.y + lift.bounds.height;
        // Two hanging chains make the mechanical purpose visible using the ruin's materials.
        for (float x : new float[]{lift.bounds.x + 8, lift.bounds.x + lift.bounds.width - 8}) {
            for (float y = top + 3; y < lift.high + 22; y += 6) {
                ui.line(batch, x - 1, y, x - 1, y + 4, 1.3f, CREVICE, .95f);
                ui.line(batch, x + 1, y, x + 1, y + 4, .7f, STONE, .85f);
                ui.line(batch, x - 1, y + 4, x + 1, y + 4, .8f, PALE_STONE, .65f);
            }
        }
        for (float x : new float[]{lift.bounds.x + 5, lift.bounds.x + lift.bounds.width - 10}) {
            ui.rect(batch, x, top - 12, 5, 10, CREVICE, .8f);
            ui.line(batch, x, top - 2, x + 5, top - 2, .8f, PALE_STONE, .8f);
        }
        ui.line(batch, center - 5, top - 7, center, top - 4, .7f, MIST, .8f);
        ui.line(batch, center, top - 4, center + 5, top - 7, .7f, MIST, .8f);
        ui.line(batch, lift.bounds.x + 3, top - 1, lift.bounds.x + lift.bounds.width - 3,
                top - 1, 2.8f, CREVICE, .8f);
        ui.line(batch, lift.bounds.x + 3, top, lift.bounds.x + lift.bounds.width - 3,
                top, 1.2f, MIST, 1);
    }

    private void drawWind(SpriteBatch batch, UiTheme ui, TerrainTile stone, Wind wind) {
        float packed = batch.getPackedColor();
        batch.setColor(.9f, .96f, 1, 1);
        stone.draw(batch, wind.x - 27, wind.floor + 3, 54, 7);
        batch.setPackedColor(packed);
        ui.verticalFade(batch, wind.x - 25, wind.floor + 3, 50, 22, MIST, .22f, 0);
        ui.diamond(batch, wind.x, wind.floor + 7, 5, CREVICE, 1);
        ui.diamond(batch, wind.x, wind.floor + 7, 4, MIST, 1);
        for (int i = 0; i < 3; i++) {
            float x = wind.x - 16 + i * 16;
            ui.line(batch, x - 3, wind.floor + 2, x, wind.floor + 1, 1.7f, CREVICE, 1);
            ui.line(batch, x, wind.floor + 1, x + 3, wind.floor + 2, .8f, MIST, .8f);
        }
        for (int i = 0; i < 7; i++) {
            float rise = gentle() ? 10 + i * 8 : (time * 31 + i * 11) % 74;
            float x = wind.x + (float) Math.sin(i * 2.4f + (gentle() ? 0 : time * .8f)) * 17;
            float alpha = .95f * (1 - rise / 100);
            ui.line(batch, x, wind.floor + 7 + rise, x + 1.2f,
                    wind.floor + 13 + rise, 1.2f, MIST, alpha);
        }
        for (int i = 0; i < 3; i++) {
            float previousX = wind.x - 16 + i * 16;
            float previousY = wind.floor + 8;
            for (int n = 1; n <= 8; n++) {
                float y = wind.floor + 8 + n * 6;
                float x = wind.x - 16 + i * 16
                        + (float) Math.sin(n * .65f + i * 2 + (gentle() ? 0 : time * 1.5f)) * 3;
                ui.line(batch, previousX, previousY, x, y, 2.8f, CREVICE, .16f * (1 - n / 10f));
                ui.line(batch, previousX, previousY, x, y, 1.15f, MIST, .85f * (1 - n / 11f));
                previousX = x;
                previousY = y;
            }
        }
    }

    private void drawGate(SpriteBatch batch, UiTheme ui, TerrainTile stone, Gate gate) {
        float x = gate.bounds.x, y = gate.floor;
        if (!gate.open) {
            stone.draw(batch, x, y + 65, 28, 65);
            stone.draw(batch, x, y + 130, 28, 65);
            ui.line(batch, x + 4, y + 3, x + 4, y + 128, 1.5f, CREVICE, .75f);
            ui.line(batch, x + 23, y + 3, x + 23, y + 128, .8f, PALE_STONE, .45f);
            for (int i = 0; i < 4; i++) {
                float cy = y + 19 + i * 28;
                ui.line(batch, x + 4, cy, x + 23, cy + 2, 1.7f, CREVICE, .8f);
                ui.line(batch, x + 5, cy - 1, x + 21, cy + 1, .6f, PALE_STONE, .55f);
            }
            // A worn seal carved into stone, sharing the altar's restrained ember color.
            ui.diamond(batch, x + 14, y + 65, 8, CREVICE, 1);
            ui.diamond(batch, x + 14, y + 65, 7, COPPER, .65f);
            ui.diamond(batch, x + 14, y + 65, 6, HEAT, .9f);
            ui.line(batch, x + 14, y + 60, x + 14, y + 70, 1.1f, HEAT, 1);
        } else {
            stone.draw(batch, x, y + 4, 28, 7);
        }
        float packed = batch.getPackedColor();
        batch.setColor(.74f, .79f, .82f, 1);
        stone.draw(batch, gate.leverX - 10, y + 9, 20, 12);
        batch.setPackedColor(packed);
        float handleX = gate.leverX + (gate.open ? 7 : -7);
        ui.line(batch, gate.leverX, y + 7, handleX, y + 25, 3.7f, CREVICE, 1);
        ui.line(batch, gate.leverX, y + 8, handleX, y + 25, 1.7f, STONE, 1);
        ui.line(batch, gate.leverX + 1, y + 9, handleX + 1, y + 25, .6f, PALE_STONE, .85f);
        ui.line(batch, handleX - 3, y + 24, handleX + 3, y + 25, 3,
                gate.open ? MIST : COPPER, 1);
        ui.line(batch, handleX - 2, y + 25, handleX + 2, y + 26, .8f, EDGE, 1);
    }

    private boolean gentle() { return GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS); }

    private void warningLabel(SpriteBatch batch, UiTheme ui, BitmapFont font,
                              String text, float center, float y, float width) {
        ui.centered(batch, font, text, new Rectangle(center - width / 2, y, width, 20), .6f, EDGE);
    }
}
