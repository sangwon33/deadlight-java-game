package com.example.pixelgame;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.math.Rectangle;

/** Shared horizontal/vertical routes, solid floors, one-way bridges and encounter anchors. */
final class StageLayout {
    static final float GROUND=60f;
    static final float TUTORIAL_LOOK_X=760f,TUTORIAL_LOOK_TOP=270f,TUTORIAL_LOOK_WIDTH=200f;
    static final float TUTORIAL_LANDING_TOP=160f;
    record Spawn(float x,float floor,Enemy.EnemyType type,boolean guardian,float left,float right){
        Enemy create(){Enemy e=new Enemy(x,floor,type);if(right>left)e.setTerritory(left,right);if(guardian)e.markGuardian();return e;}
    }
    record Supply(String id,float x,float floor,int gold,int heals,int powers){Rectangle bounds(){return new Rectangle(x-10,floor+3,20,24);}}
    record Hint(int id,String title,String line1,String line2){}
    private StageLayout(){}
    static String name(int stage){return switch(stage){case 0->"조작 훈련";case 1->"신록의 폐허";case 2->"성벽";case 3->"잿빛 전장";case 4->"여신의 교회";default->throw new IllegalArgumentException("Stage "+stage);};}
    static float width(int stage){return switch(stage){case 0->1000;case 1->3000;case 2->3200;case 3->3400;case 4->1200;default->throw new IllegalArgumentException("Stage "+stage);};}
    static float bottom(int stage){return stage>=1&&stage<=3?-260:0;}
    static float top(int stage){return switch(stage){case 0->620;case 1->1120;case 2->1220;case 3->1460;case 4->600;default->throw new IllegalArgumentException();};}
    private static float sourceWidth(int stage){return switch(stage){case 1->4800;case 2->5200;case 3->5600;default->width(stage);};}
    static float x(int stage,float source){return stage>=1&&stage<=3?source*width(stage)/sourceWidth(stage):source;}
    static float y(int stage,float source){return stage>=1&&stage<=3&&source>GROUND?GROUND+14+(source-GROUND)*.7f:source;}
    private static List<Rectangle> compact(int stage,List<Rectangle> surfaces){
        if(stage>=1&&stage<=3)for(Rectangle r:surfaces){float top=y(stage,r.y+r.height);r.x=x(stage,r.x);r.width=x(stage,r.width);r.y=top-r.height;}
        return surfaces;
    }
    private static void slab(List<Rectangle> out,float x,float top,float width){out.add(new Rectangle(x,top-18,width,18));}
    static boolean tutorialLookPlatform(Rectangle support){
        return support!=null&&Math.abs(support.x-TUTORIAL_LOOK_X)<.1f
                &&Math.abs(support.y+support.height-TUTORIAL_LOOK_TOP)<.1f
                &&Math.abs(support.width-TUTORIAL_LOOK_WIDTH)<.1f;
    }
    static List<Rectangle> floors(int stage){
        var out=new ArrayList<Rectangle>();
        if(stage==0||stage==4){out.add(new Rectangle(0,GROUND-96,sourceWidth(stage),96));return out;}
        out.add(new Rectangle(0,-36,900,96));
        out.add(new Rectangle(1160,-36,340,96));
        out.add(new Rectangle(1780,-36,sourceWidth(stage)-1780,96));
        out.add(new Rectangle(720,-224,1160,64));
        // A sheltered underpass with enough clearance for the standing hero.
        out.add(new Rectangle(1200,-76,170,24));
        compact(stage,out);
        if(stage==2){out.add(new Rectangle(Lamplighter.LEFT-20,Lamplighter.FLOOR-18,900,18));out.add(new Rectangle(Lamplighter.LEFT-20,Lamplighter.FLOOR,20,260));out.add(new Rectangle(Lamplighter.LEFT-20,Lamplighter.FLOOR+260,900,20));}
        SideBossArena arena=SideBossArena.forStage(stage);if(arena!=null)arena.solids(out);
        return out;
    }
    static List<Rectangle> platforms(int stage){
        var p=new ArrayList<Rectangle>();
        if(stage==0){
            slab(p,600,160,150);
            slab(p,TUTORIAL_LOOK_X,TUTORIAL_LANDING_TOP,TUTORIAL_LOOK_WIDTH);
            slab(p,TUTORIAL_LOOK_X,TUTORIAL_LOOK_TOP,TUTORIAL_LOOK_WIDTH);
            return p;
        }
        if(stage==4){slab(p,350,150,170);slab(p,780,150,170);return p;}
        slab(p,900,60,260);slab(p,1500,60,280);
        float shift=(stage-1)*25;
        slab(p,260,140,200);slab(p,530,220,210);slab(p,800,300,220);
        slab(p,1080,380,200);slab(p,1380,460,220);
        slab(p,1900,220,200);slab(p,2140,300,190);slab(p,2380,380,190);
        slab(p,2580,460,190);slab(p,2780,540,200);
        slab(p,2580+shift,620,180);slab(p,2380+shift,700,200);
        slab(p,2580+shift,780,210);slab(p,2860+shift,860,220);
        slab(p,3100+shift,940+(stage-1)*60,220);
        slab(p,3400+shift,860+(stage-1)*60,240);
        slab(p,3720+shift,780,210);slab(p,3990+shift,700,200);
        slab(p,sourceWidth(stage)-1100,180,200);slab(p,sourceWidth(stage)-850,260,190);slab(p,sourceWidth(stage)-610,340,200);
        // Short stepping routes and sheltered combat ledges fill the old empty ground stretches.
        slab(p,120,160,130);slab(p,1800,160,140);slab(p,3250,220,170);
        slab(p,3500,340,160);slab(p,4200,160,170);
        // Interleaved courtyard balconies connect the formerly sparse runs.
        slab(p,400,320,150);slab(p,690,420,160);slab(p,1110,190,150);
        slab(p,1500,300,160);slab(p,1740,410,170);slab(p,2000,500,150);
        slab(p,2240,540,150);slab(p,3090,380,160);slab(p,3340,500,160);
        slab(p,3600,600,150);slab(p,3920,420,170);slab(p,4330,540,160);
        compact(stage,p);
        addCourtyards(stage,p);
        if(stage==2){slab(p,1100,450,180);slab(p,950,530,180);slab(p,1180,610,180);slab(p,1400,690,180);slab(p,1630,770,190);slab(p,1630,850,190);}
        SideBossArena arena=SideBossArena.forStage(stage);if(arena!=null)arena.platforms(p);
        return p;
    }
    /** Additional connected galleries, small step-ups and underpass stepping stones. */
    private static void addCourtyards(int stage,List<Rectangle> p){
        float scale=width(stage)/3000f;
        float[][] ledges={
            {395,-105,110},{580,-90,110},{960,-70,110},
            {770,145,100},{900,150,135},
            {1040,175,130},{1190,250,130},{1340,330,135},
            {1475,410,130},{1680,340,145},{1850,265,135},
            {2060,180,150},{2270,155,130},{2490,150,150},
            {2700,185,135},{2775,280,135},{2640,375,140},
            {2440,455,150},{2165,500,140},{1980,570,140}
        };
        for(float[] ledge:ledges){
            float x=ledge[0]*scale,top=ledge[1],w=ledge[2]*scale;
            // Keep existing encounter entrances, floors and ledges clear; use another gallery level if needed.
            boolean crowded=false;
            for(Rectangle r:p)if(Math.abs(r.y+r.height-top)<48&&x<r.x+r.width+8&&x+w>r.x-8){crowded=true;break;}
            if(!crowded&&galleryClear(stage,x,top,w))slab(p,x,top,w);
            else{
                float alternate=top+88;
                boolean clear=true;
                for(Rectangle r:p)if(Math.abs(r.y+r.height-alternate)<48&&x<r.x+r.width+8&&x+w>r.x-8){clear=false;break;}
                if(clear&&galleryClear(stage,x,alternate,w))slab(p,x,alternate,w);
            }
        }
    }
    private static boolean galleryClear(int stage,float x,float top,float width){
        Rectangle standing=new Rectangle(x,top,width,68);
        for(Rectangle solid:floors(stage))if(standing.overlaps(solid))return false;
        return true;
    }
    private static Spawn ground(float x,Enemy.EnemyType type,boolean guard){return new Spawn(x,GROUND,type,guard,0,0);}
    static List<Spawn> enemies(int stage){
        if(stage<1||stage>3)return List.of();
        var s=Enemy.EnemyType.SLIME;var k=Enemy.EnemyType.KNIGHT;var d=Enemy.EnemyType.DARK_SLIME;
        float width=sourceWidth(stage),high=940+(stage-1)*60,shift=(stage-1)*25;
        var result=new ArrayList<>(List.of(ground(540,stage==3?d:s,false),ground(1260,k,false),
                new Spawn(850,-160,s,false,750,1140),ground(1980,stage==1?s:d,false),
                new Spawn(1450,460,k,false,1380,1600),ground(2700,k,false),
                new Spawn(stage==1?2440:2630+shift,stage==1?700:780,stage==1?s:d,false,stage==1?2380:2580+shift,stage==1?2580:2790+shift),
                new Spawn(3180+shift,high,k,false,3100+shift,3320+shift),
                ground(width-1300,d,false),ground(width-620,stage==3?d:k,true),ground(1850,s,false),ground(3750,stage==1?s:d,false)));
        if(stage==1)result.add(new Spawn(1200,470,Enemy.EnemyType.GLASSWING,false,1000,1570));
        else{
            result.add(new Spawn(2100,390,Enemy.EnemyType.GLASSWING,false,1870,2450));
            result.add(new Spawn(3190+shift,high+95,Enemy.EnemyType.GLASSWING,false,3040+shift,3520+shift));
        }
        var ranged=Enemy.EnemyType.LANTERN_MARKSMAN;
        if(stage==1){
            result.add(new Spawn(2260,60,ranged,false,2060,2430));
            result.add(new Spawn(3520,860,ranged,false,3410,3630));
        }else{
            result.add(new Spawn(2340,60,ranged,false,2160,2520));
            result.add(new Spawn(2520+shift,700,ranged,false,2390+shift,2570+shift));
            result.add(new Spawn(3520+shift,860+(stage-1)*60,ranged,false,3410+shift,3630+shift));
            result.add(new Spawn(width-1050,60,ranged,false,width-1190,width-870));
            if(stage==3)result.add(new Spawn(1640,-160,ranged,false,1450,1850));
        }
        return result.stream().map(e->new Spawn(x(stage,e.x),y(stage,e.floor),e.type,e.guardian,x(stage,e.left),x(stage,e.right))).toList();
    }
    static List<Supply> supplies(int stage){
        if(stage<1||stage>3)return List.of();
        float shift=(stage-1)*25;
        return List.of(new Supply("route-heal",670,220,10,1,0),new Supply("underpass-cache",1450,-160,20,1,0),
                new Supply("tower-cache",2500+shift,700,35,0,1),
                new Supply("summit-cache",3250+shift,940+(stage-1)*60,40,1,1),
                new Supply("exit-heal",sourceWidth(stage)-520,340,15,1,0),
                new Supply("courtyard-cache",3340,220,15,1,0))
                .stream().map(s->new Supply(s.id,x(stage,s.x),y(stage,s.floor),s.gold,s.heals,s.powers)).toList();
    }
    static Hint hint(int stage,float x){return hint(stage,x,GROUND);}
    static Hint hint(int stage,float x,float y){
        if(stage==0)return null;
        if(stage==4)return new Hint(40,"침묵의 성당","높은 발판도 회피에 활용하세요","공격 뒤 빈틈에 집중 후 반격");
        String down=KeyBindings.name(KeyBindings.Action.LOOK_DOWN),interact=KeyBindings.name(KeyBindings.Action.INTERACT);
        SideBossArena arena=SideBossArena.forStage(stage);
        if(arena!=null&&x>arena.x()&&x<arena.x()+1150&&y>=arena.floor()-400)return new Hint(stage*100+8,arena.room(),"옆길의 선택형 중간 보스 · "+interact+"로 도전","도전하지 않아도 다음 지역으로 갈 수 있습니다");
        if(stage==2&&x>Lamplighter.LEFT-20&&x<Lamplighter.RIGHT+200&&y>=450&&y<Lamplighter.FLOOR)return new Hint(207,"등불 감시탑의 계단","상층에 선택형 중간 보스가 잠들어 있습니다","도전하지 않아도 다음 지역으로 갈 수 있습니다");
        if(stage==2&&x>Lamplighter.LEFT-20&&x<Lamplighter.RIGHT+50&&y>=Lamplighter.FLOOR)return new Hint(208,"꺼지지 않는 등불지기","입구의 등불 앞에서 "+interact+" · 도전","공격을 피한 뒤 회복 동작에 반격하세요");
        if(y<0)return new Hint(stage*100+5,"지하의 샛길","통로를 따라 보급품을 찾아보세요","바람을 타고 지상으로 돌아갈 수 있습니다");
        if(y>y(stage,600))return new Hint(stage*100+6,"탑 위의 보급로","공중 대시로 발판 사이를 건너세요","내려갈 때는 "+down+"를 짧게 눌렀다 떼세요");
        if(x<x(stage,850))return new Hint(stage*100,"폐허의 갈림길","발판을 따라 보급품을 찾아보세요",KeyBindings.name(KeyBindings.Action.LOOK_DOWN)+" 길게 · 살펴보기 / 짧게 · 아래로");
        if(x<x(stage,1800))return new Hint(stage*100+1,"발판 아래에도 길이 있습니다","발판 위에서 "+down+"를 짧게 눌렀다 떼세요","아래를 살펴본 뒤 안전하게 착지하세요");
        if(x<x(stage,2600))return new Hint(stage*100+2,"바람을 타고 위로","빛나는 발판은 높이 솟아오릅니다","움직이는 승강 발판도 이용하세요");
        if(x<x(stage,3400))return new Hint(stage*100+3,"높은 탑의 갈림길","지상 길이나 위쪽 발판으로 이동하세요","분출구를 살피고 안전한 길로 건너세요");
        return new Hint(stage*100+4,"다음 지역으로","가시는 회피하거나 점프로 넘으세요","수문장을 쓰러뜨리고 출구로 이동");
    }
}
