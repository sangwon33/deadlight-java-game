package com.example.pixelgame;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Deterministic region mechanisms; reset together with the region-entry checkpoint. */
final class WorldGimmicks {
    private static final Color WARNING = new Color(.96f, .73f, .36f, 1f);
    private static final Color DANGER = new Color(1f, .39f, .29f, 1f);
    private static final Color HAZARD_CORE = new Color(1f, .94f, .76f, 1f);
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
        for (Rectangle spike : spikes) drawSpikes(batch, ui, spike);
        for (Well well : wells) {
            float packed = batch.getPackedColor();
            batch.setColor(1, 1, 1, well.used ? .46f : 1);
            world.draw(batch, 5, 5, well.x, well.floor, 44, true);
            batch.setPackedColor(packed);
        }
        for (Lift lift : lifts) drawLift(batch, ui, lift);
        for (Wind wind : winds) drawWind(batch, ui, wind);
        for (Gate gate : gates) drawGate(batch, ui, stone, gate);
    }

    /** Labels belong above supplies and actors so nearby objects cannot hide their text. */
    void drawLabels(SpriteBatch batch, UiTheme ui, BitmapFont font, Player player) {
        for (Bridge bridge : bridges) {
            if (bridge.respawn <= 0 && player.getSupport() == bridge.bounds) {
                warningLabel(batch, ui, font, "발판이 무너집니다", bridge.bounds.x + bridge.width / 2,
                        bridge.bounds.y + bridge.bounds.height + 90, Math.min(150, bridge.width));
            }
        }
        for (Jet jet : jets) {
            if ((jet.warning() || jet.active()) && Math.abs(player.getCenterX() - jet.x) < 135
                    && Math.abs(player.getY() - jet.floor) < 85) {
                warningLabel(batch, ui, font, jet.active() ? "분출 중" : "곧 분출합니다",
                        jet.x, jet.floor + Jet.HEIGHT + 39, 116);
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
                ui.panel(batch, label.x, label.y, label.width, label.height);
                ui.centered(batch, font, "[" + KeyBindings.name(KeyBindings.Action.INTERACT)
                        + "] 석문 열기", label, .61f, HAZARD_CORE);
            }
        }
    }

    private void drawBridge(SpriteBatch batch, UiTheme ui, Bridge bridge) {
        float x = bridge.bounds.x;
        float top = bridge.bounds.y + bridge.bounds.height;
        float right = x + bridge.width;
        if (bridge.respawn > 0) {
            for (float px = x; px < right; px += 14) {
                ui.line(batch, px, top, Math.min(px + 7, right), top, 1.2f, WARNING, .6f);
            }
            ui.line(batch, x, top - 9, x, top + 3, 1.5f, WARNING, .9f);
            ui.line(batch, right, top - 9, right, top + 3, 1.5f, WARNING, .9f);
            return;
        }

        float progress = Math.min(1, bridge.stress / BRIDGE_COLLAPSE_DELAY);
        Color edge = bridge.stress > 0 ? DANGER : WARNING;
        ui.rect(batch, x, top - 7, bridge.width, 7, UiTheme.SURFACE, .92f);
        for (float px = x; px < right; px += 12) {
            ui.rect(batch, px, top - 5, Math.min(7, right - px), 3, WARNING, 1);
        }
        ui.line(batch, x, top, right, top, 1.3f, HAZARD_CORE, 1);
        for (float px = x + 12; px < right - 5; px += 25) {
            float depth = 8 + progress * 8;
            ui.line(batch, px, top - 1, px - 4, top - depth / 2, 2.2f, UiTheme.SURFACE, 1);
            ui.line(batch, px - 4, top - depth / 2, px + 3, top - depth, 2.2f, UiTheme.SURFACE, 1);
            ui.line(batch, px + 1, top - 1, px - 3, top - depth / 2, .8f, edge, .9f);
        }
        if (bridge.stress > 0) {
            ui.rect(batch, x, top - 9, bridge.width * progress, 2, DANGER, 1);
        }
    }

    private void drawJet(SpriteBatch batch, UiTheme ui, MoonAtlas world, Jet jet) {
        Rectangle area = jet.bounds();
        float top = area.y + area.height;
        world.draw(batch, 7, 7, jet.x, jet.floor, 25, true);
        ui.rect(batch, area.x, jet.floor, area.width, 5, UiTheme.SURFACE, .96f);
        for (float x = area.x + 1; x < area.x + area.width - 1; x += 8) {
            ui.line(batch, x, jet.floor + 1, Math.min(x + 4, area.x + area.width - 1),
                    jet.floor + 4, 1.3f, WARNING, .95f);
        }

        if (jet.warning()) {
            float progress = (jet.phase - Jet.WARNING_START) / (Jet.ACTIVE_START - Jet.WARNING_START);
            ui.verticalFade(batch, area.x, jet.floor + 5, area.width, area.height - 5,
                    WARNING, .15f + progress * .12f, .05f);
            for (float y = jet.floor + 7; y < top; y += 12) {
                ui.line(batch, area.x, y, area.x, Math.min(y + 5, top), 1, WARNING, .85f);
                ui.line(batch, area.x + area.width, y, area.x + area.width,
                        Math.min(y + 5, top), 1, WARNING, .85f);
            }
            ui.line(batch, area.x, top, area.x + area.width, top, 1, WARNING, .9f);
            ui.rect(batch, area.x, jet.floor + 5, area.width * progress, 2, HAZARD_CORE, 1);
            warningMark(batch, ui, jet.x, jet.floor + 33, WARNING);
        } else if (jet.active()) {
            // Draw the same top and width as the damaging rectangle.
            ui.verticalFade(batch, area.x, area.y, area.width, area.height, DANGER, .5f, .2f);
            ui.line(batch, area.x, jet.floor, area.x, top, 1.4f, DANGER, .95f);
            ui.line(batch, area.x + area.width, jet.floor, area.x + area.width, top, 1.4f, DANGER, .95f);
            ui.line(batch, area.x, top, area.x + area.width, top, 1, HAZARD_CORE, .9f);
            boolean gentle = GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS);
            for (int i = 0; i < 7; i++) {
                float x = area.x + 3 + i * 6;
                float sway = gentle ? 0 : (float) Math.sin(time * 9 + i) * 2;
                ui.line(batch, x, jet.floor + 4, x + sway, top - 2,
                        2.3f, DANGER, .85f);
                ui.line(batch, x, jet.floor + 4, x + sway, top - 3,
                        .8f, HAZARD_CORE, .85f);
            }
        }
    }

    private void drawSpikes(SpriteBatch batch, UiTheme ui, Rectangle spike) {
        ui.rect(batch, spike.x - 2, spike.y - 3, spike.width + 4,
                spike.height + 5, UiTheme.SURFACE, .88f);
        ui.rect(batch, spike.x, spike.y, spike.width, 3, DANGER, 1);
        int teeth = Math.max(1, (int) Math.ceil(spike.width / 10));
        float toothWidth = spike.width / teeth;
        for (int i = 0; i < teeth; i++) {
            float left = spike.x + i * toothWidth;
            float right = left + toothWidth;
            float middle = (left + right) / 2;
            float tip = spike.y + spike.height;
            ui.triangle(batch, left, spike.y + 2, middle, tip, right, spike.y + 2, WARNING, 1);
            ui.triangle(batch, middle, spike.y + 3, middle, tip,
                    right - 1, spike.y + 2, HAZARD_CORE, 1);
            ui.line(batch, left, spike.y + 2, middle, tip, 1, DANGER, 1);
        }
    }

    private void drawLift(SpriteBatch batch, UiTheme ui, Lift lift) {
        float center = lift.bounds.x + lift.bounds.width / 2;
        float top = lift.bounds.y + lift.bounds.height;
        ui.line(batch, center, lift.low - 18, center, lift.high + 22, 1, UiTheme.BORDER, .8f);
        ui.diamond(batch, center, lift.low - 12, 3, UiTheme.GOLD, .9f);
        ui.diamond(batch, center, lift.high + 16, 3, UiTheme.GOLD, .9f);
        ui.rect(batch, lift.bounds.x, top - 5, lift.bounds.width, 5, UiTheme.SURFACE, .85f);
        ui.line(batch, lift.bounds.x, top, lift.bounds.x + lift.bounds.width,
                top, 1.5f, UiTheme.GOLD, 1);
        float arrowY = top - 10;
        float tip = arrowY + lift.direction * 4;
        float tail = arrowY - lift.direction * 3;
        ui.line(batch, center - 4, tail, center, tip, 1.3f, UiTheme.TEXT, 1);
        ui.line(batch, center, tip, center + 4, tail, 1.3f, UiTheme.TEXT, 1);
    }

    private void drawWind(SpriteBatch batch, UiTheme ui, Wind wind) {
        ui.rect(batch, wind.x - 29, wind.floor, 58, 6, UiTheme.SURFACE, .9f);
        ui.line(batch, wind.x - 27, wind.floor + 2, wind.x + 27,
                wind.floor + 2, 2, UiTheme.GOLD, 1);
        boolean gentle = GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS);
        for (int i = 0; i < 3; i++) {
            float travel = gentle ? 0 : (time * 16) % 20;
            float y = wind.floor + 13 + i * 20 + travel;
            ui.line(batch, wind.x - 7, y, wind.x, y + 6, 1.2f, UiTheme.GOLD, .85f);
            ui.line(batch, wind.x, y + 6, wind.x + 7, y, 1.2f, UiTheme.GOLD, .85f);
        }
    }

    private void drawGate(SpriteBatch batch, UiTheme ui, TerrainTile stone, Gate gate) {
        float x = gate.bounds.x;
        float y = gate.floor;
        Color color = gate.open ? UiTheme.SUCCESS : WARNING;
        if (!gate.open) {
            stone.draw(batch, x, y + 65, 28, 65);
            stone.draw(batch, x, y + 130, 28, 65);
            ui.rect(batch, x, y, 28, 130, UiTheme.SURFACE, .55f);
            ui.line(batch, x, y, x, y + 130, 1.5f, color, 1);
            ui.line(batch, x + 28, y, x + 28, y + 130, 1.5f, color, 1);
            for (int i = 0; i < 4; i++) ui.diamond(batch, x + 14, y + 18 + i * 29, 6, color, .95f);
            ui.rect(batch, x + 8, y + 58, 12, 10, HAZARD_CORE, 1);
            ui.line(batch, x + 10, y + 68, x + 10, y + 73, 1.4f, color, 1);
            ui.line(batch, x + 10, y + 73, x + 18, y + 73, 1.4f, color, 1);
            ui.line(batch, x + 18, y + 73, x + 18, y + 68, 1.4f, color, 1);
        } else {
            ui.line(batch, x, y, x, y + 16, 1.4f, color, 1);
            ui.line(batch, x + 28, y, x + 28, y + 16, 1.4f, color, 1);
        }
        ui.rect(batch, gate.leverX - 10, y - 1, 20, 14, UiTheme.SURFACE, .96f);
        ui.line(batch, gate.leverX - 9, y, gate.leverX + 9, y, 1.4f, color, 1);
        float handleX = gate.leverX + (gate.open ? 7 : -7);
        ui.line(batch, gate.leverX, y + 10, handleX, y + 25, 2.5f, color, 1);
        ui.diamond(batch, handleX, y + 25, 3, HAZARD_CORE, 1);
    }

    private void warningMark(SpriteBatch batch, UiTheme ui, float x, float y, Color color) {
        ui.triangle(batch, x - 9, y, x, y + 9, x + 9, y, UiTheme.SURFACE, 1);
        ui.triangle(batch, x - 9, y, x, y - 9, x + 9, y, UiTheme.SURFACE, 1);
        ui.diamond(batch, x, y, 9, color, 1);
        ui.rect(batch, x - 1, y - 1, 2, 6, HAZARD_CORE, 1);
        ui.rect(batch, x - 1, y - 5, 2, 2, HAZARD_CORE, 1);
    }

    private void warningLabel(SpriteBatch batch, UiTheme ui, BitmapFont font,
                              String text, float center, float y, float width) {
        Rectangle label = new Rectangle(center - width / 2, y, width, 20);
        ui.rect(batch, label.x, label.y, label.width, label.height, UiTheme.SURFACE, .92f);
        ui.line(batch, label.x, label.y, label.x + label.width, label.y, .8f, WARNING, .95f);
        ui.centered(batch, font, text, label, .62f, HAZARD_CORE);
    }
}
