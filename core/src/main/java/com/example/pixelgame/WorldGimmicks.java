package com.example.pixelgame;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Deterministic region mechanisms; reset together with the region-entry checkpoint. */
final class WorldGimmicks implements AutoCloseable {
    private HazardSprites sprites;
    private HazardSprites art(){if(sprites==null)sprites=new HazardSprites();return sprites;}
    @Override public void close(){if(sprites!=null){sprites.close();sprites=null;}}
    private static final Color CREVICE = new Color(.055f, .075f, .09f, 1);
    private static final Color STONE = new Color(.32f, .39f, .43f, 1);
    private static final Color PALE_STONE = new Color(.76f, .79f, .78f, 1);
    private static final Color MIST = new Color(.36f,.90f,.61f,1);
    private static final Color HEAT = new Color(.97f, .82f, .63f, 1);
    private static final Color EDGE = new Color(.91f, .93f, .88f, 1);
    private static final Color STEAM = new Color(.75f, .65f, .55f, 1);
    private static final float BRIDGE_COLLAPSE_DELAY = .85f;
    static final class Lift {
        final Rectangle bounds;final float low,high,speed;float direction=1;
        Lift(float x,float low,float high,float speed){bounds=new Rectangle(x,low-18,150,18);this.low=low;this.high=high;this.speed=speed;}
    }
    static final class Wind {
        final float x,floor,speed;float cooldown;
        Wind(float x,float floor,float speed){this.x=x;this.floor=floor;this.speed=speed;}
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
    final List<Rectangle> spikes=new ArrayList<>();
    private float time;
    void enter(int stage,ArrayList<Rectangle> platforms){
        lifts.clear();winds.clear();spikes.clear();bridges.clear();jets.clear();wells.clear();time=0;healedNotice=false;
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
    }
    void afterPhysics(Player p){
        for(Jet jet:jets)if(jet.active()&&p.getBounds().overlaps(jet.bounds()))p.takeDamage(GameBalance.ENEMY_DAMAGE,jet.x);
        for(Wind wind:winds)if(wind.cooldown<=0&&!p.isCrouching()&&p.isGrounded()
                &&Math.abs(p.getY()-wind.floor)<3&&Math.abs(p.getCenterX()-wind.x)<28){
            p.launch(wind.speed);wind.cooldown=.65f;
        }
        for(Rectangle spike:spikes)if(p.isAlive()&&p.getBounds().overlaps(spike))p.takeDamage(GameBalance.ENEMY_DAMAGE,spike.x+spike.width/2);
    }
    boolean takeHealedNotice(){boolean value=healedNotice;healedNotice=false;return value;}
    void draw(SpriteBatch batch, UiTheme ui, TerrainTile stone, MoonAtlas world) {
        for (Bridge bridge : bridges){role(batch,false);drawBridge(batch, ui, bridge);normal(batch);}
        for (Jet jet : jets){role(batch,false);drawJet(batch, ui, world, jet);normal(batch);}
        for (Rectangle spike : spikes){role(batch,false);drawSpikes(batch, ui, stone, spike);normal(batch);}
        for (Well well : wells) {
            float packed = batch.getPackedColor();
            batch.setColor(1, 1, 1, well.used ? .46f : 1);
            if(!well.used)role(batch,true);world.draw(batch, 5, 5, well.x, well.floor, 44, true);normal(batch);
            batch.setPackedColor(packed);
        }
        for (Lift lift : lifts) drawLift(batch, ui, lift);
        for (Wind wind : winds) drawWind(batch, ui, stone, wind);
    }

    private void role(SpriteBatch b,boolean good){if(b instanceof ReadabilityBatch r)r.roleOutline(good);}
    private void normal(SpriteBatch b){if(b instanceof ReadabilityBatch r)r.normal();}

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
    }

    private void drawBridge(SpriteBatch batch, UiTheme ui, Bridge bridge) {
        float x=bridge.bounds.x,top=bridge.bounds.y+bridge.bounds.height;
        if(bridge.respawn>0){
            if(bridge.respawn>3.55f){
                float packed=batch.getPackedColor();
                batch.setColor(1,1,1,(bridge.respawn-3.55f)/.45f);
                art().bridge(batch,2,x,top,bridge.width);batch.setPackedColor(packed);
            }
            // A persistent gap must never resemble a usable platform while it respawns.
            ui.triangle(batch,x,top,x+5,top-3,x,top-10,STONE,1);
            ui.triangle(batch,x+bridge.width,top,x+bridge.width-5,top-3,x+bridge.width,top-9,STONE,1);
            return;
        }
        float progress=Math.min(1,bridge.stress/BRIDGE_COLLAPSE_DELAY);
        art().bridge(batch,progress>.12f?1:0,x,top,bridge.width);
        ui.line(batch,x+2,top,x+bridge.width-2,top,1.2f,Color.SALMON,.9f);
        if(progress>.1f)for(int i=0;i<5;i++){
            float px=x+12+i*(bridge.width-24)/4;
            float fall=gentle()?7:(time*35+i*11)%27;
            ui.triangle(batch,px-1,top-18-fall,px+1.5f,top-19-fall,px,top-22-fall,PALE_STONE,.65f+progress*.3f);
        }
    }

    private void drawJet(SpriteBatch batch, UiTheme ui, MoonAtlas world, Jet jet) {
        int state=jet.active()?2:jet.warning()?1:jet.phase>=Jet.ACTIVE_END&&jet.phase<Jet.ACTIVE_END+.4f?3:0;
        art().vent(batch,state,jet.x,jet.floor);
        ui.line(batch,jet.x-23,jet.floor+1,jet.x+23,jet.floor+1,1.5f,Color.SALMON,.95f);
        if(!jet.warning()&&!jet.active())return;
        Rectangle area=jet.bounds();boolean active=jet.active();
        // Quiet full-width ash marks the real hit footprint without outlining a rectangle.
        ui.verticalFade(batch,area.x,area.y,area.width,active?area.height:22,STEAM,active?.22f:.1f,0);
        if(gentle())return;
        for(int i=0;i<(active?7:3);i++){
            float rise=(time*(active?48:22)+i*13)%(active?Jet.HEIGHT:28);
            float x=area.x+4+(i*17)%(int)(area.width-8);
            ui.line(batch,x,jet.floor+rise,x+.5f,jet.floor+rise+2,1,HEAT,.65f);
        }
    }

    private void drawSpikes(SpriteBatch batch, UiTheme ui, TerrainTile stone, Rectangle spike) {
        art().spikes(batch,spike.x,spike.y,spike.width,spike.height);
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

    private boolean gentle() { return GameSettings.current.on(GameSettings.Option.REDUCED_EFFECTS); }

    private void warningLabel(SpriteBatch batch, UiTheme ui, BitmapFont font,
                              String text, float center, float y, float width) {
        ui.centered(batch, font, text, new Rectangle(center - width / 2, y, width, 20), .6f, EDGE);
    }
}
