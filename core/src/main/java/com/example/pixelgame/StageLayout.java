package com.example.pixelgame;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.math.Rectangle;

/** Shared horizontal/vertical routes, solid floors, one-way bridges and encounter anchors. */
final class StageLayout {
    static final float GROUND=60f;
    record Spawn(float x,float floor,Enemy.EnemyType type,boolean guardian,float left,float right){
        Enemy create(){Enemy e=new Enemy(x,floor,type);if(right>left)e.setTerritory(left,right);if(guardian)e.markGuardian();return e;}
    }
    record Supply(String id,float x,float floor,int gold,int heals,int powers){Rectangle bounds(){return new Rectangle(x-10,floor+3,20,24);}}
    record Hint(int id,String title,String line1,String line2){}
    private StageLayout(){}
    static String name(int stage){return switch(stage){case 0->"조작 훈련";case 1->"신록의 폐허";case 2->"성벽";case 3->"잿빛 전장";case 4->"여신의 교회";default->throw new IllegalArgumentException("Stage "+stage);};}
    static float width(int stage){return switch(stage){case 0->1400;case 1->4800;case 2->5200;case 3->5600;case 4->1200;default->throw new IllegalArgumentException("Stage "+stage);};}
    static float bottom(int stage){return stage>=1&&stage<=3?-260:0;}
    static float top(int stage){return switch(stage){case 0->620;case 1->1180;case 2->1300;case 3->1680;case 4->600;default->throw new IllegalArgumentException();};}
    private static void slab(List<Rectangle> out,float x,float top,float width){out.add(new Rectangle(x,top-18,width,18));}
    static List<Rectangle> floors(int stage){
        var out=new ArrayList<Rectangle>();
        if(stage==0||stage==4){out.add(new Rectangle(0,GROUND-96,width(stage),96));return out;}
        out.add(new Rectangle(0,-36,900,96));
        out.add(new Rectangle(1160,-36,340,96));
        out.add(new Rectangle(1780,-36,width(stage)-1780,96));
        out.add(new Rectangle(720,-224,1160,64));
        // A real low ceiling: standing height cannot fit, crouching can.
        out.add(new Rectangle(1200,-124,170,24));
        if(stage==2){
            // Optional upper watchtower, reached from the existing 460-height branch.
            out.add(new Rectangle(1000,1002,900,18));
            out.add(new Rectangle(1000,1020,20,260));
            out.add(new Rectangle(1000,1280,900,20));
        }
        SideBossArena arena=SideBossArena.forStage(stage);if(arena!=null)arena.solids(out);
        return out;
    }
    static List<Rectangle> platforms(int stage){
        var p=new ArrayList<Rectangle>();
        if(stage==0){slab(p,680,140,180);slab(p,920,220,200);return p;}
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
        slab(p,width(stage)-1100,180,200);slab(p,width(stage)-850,260,190);slab(p,width(stage)-610,340,200);
        if(stage==2){
            slab(p,1610,540,180);slab(p,1390,620,180);slab(p,1610,700,180);
            slab(p,1830,780,180);slab(p,1910,860,180);slab(p,1910,940,200);
        }
        SideBossArena arena=SideBossArena.forStage(stage);if(arena!=null)arena.platforms(p);
        return p;
    }
    private static Spawn ground(float x,Enemy.EnemyType type,boolean guard){return new Spawn(x,GROUND,type,guard,0,0);}
    static List<Spawn> enemies(int stage){
        if(stage<1||stage>3)return List.of();
        var s=Enemy.EnemyType.SLIME;var k=Enemy.EnemyType.KNIGHT;var d=Enemy.EnemyType.DARK_SLIME;
        float width=width(stage),high=940+(stage-1)*60,shift=(stage-1)*25;
        var result=new ArrayList<>(List.of(ground(540,stage==3?d:s,false),ground(1260,k,false),
                new Spawn(850,-160,s,false,750,1140),ground(1980,stage==1?s:d,false),
                new Spawn(1450,460,k,false,1380,1600),ground(2700,k,false),
                new Spawn(stage==1?2440:2630+shift,stage==1?700:780,stage==1?s:d,false,stage==1?2380:2580+shift,stage==1?2580:2790+shift),
                new Spawn(3180+shift,high,k,false,3100+shift,3320+shift),
                ground(width-1300,d,false),ground(width-620,stage==3?d:k,true)));
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
        return List.copyOf(result);
    }
    static List<Supply> supplies(int stage){
        if(stage<1||stage>3)return List.of();
        float shift=(stage-1)*25;
        return List.of(new Supply("route-heal",670,220,10,1,0),new Supply("underpass-cache",1450,-160,20,1,0),
                new Supply("tower-cache",2500+shift,700,35,0,1),
                new Supply("summit-cache",3250+shift,940+(stage-1)*60,40,1,1),
                new Supply("exit-heal",width(stage)-520,340,15,1,0));
    }
    static Hint hint(int stage,float x){return hint(stage,x,GROUND);}
    static Hint hint(int stage,float x,float y){
        if(stage==0)return null;
        if(stage==4)return new Hint(40,"침묵의 성당","높은 발판도 회피에 활용하세요","공격 뒤 빈틈에 집중 후 반격");
        String down=KeyBindings.name(KeyBindings.Action.DOWN),interact=KeyBindings.name(KeyBindings.Action.INTERACT);
        SideBossArena arena=SideBossArena.forStage(stage);
        if(arena!=null&&x>arena.x()&&x<arena.x()+1150&&y>=arena.floor()-400)return new Hint(stage*100+8,arena.room(),"옆길의 선택형 중간 보스 · "+interact+"로 도전","도전하지 않아도 다음 지역으로 갈 수 있습니다");
        if(stage==2&&x>1000&&x<2050&&y>=460&&y<1020)return new Hint(207,"등불 감시탑의 계단","상층에 선택형 중간 보스가 잠들어 있습니다","도전하지 않아도 다음 지역으로 갈 수 있습니다");
        if(stage==2&&x>1000&&x<1900&&y>=1020)return new Hint(208,"꺼지지 않는 등불지기","입구의 등불 앞에서 "+interact+" · 도전","공격을 피한 뒤 회복 동작에 반격하세요");
        if(y<0)return new Hint(stage*100+5,"지하의 샛길",down+"를 눌러 낮은 통로를 지나세요","바람을 타고 지상으로 돌아갈 수 있습니다");
        if(y>600)return new Hint(stage*100+6,"탑 위의 보급로","공중 대시로 발판 사이를 건너세요","내려갈 때는 "+down+"로 한 칸씩");
        if(x<850)return new Hint(stage*100,"넓어진 폐허의 길","위쪽 길과 지하 샛길을 탐색하세요",down+" · 웅크리기 / 발판 아래로");
        if(x<1800)return new Hint(stage*100+1,"발판 아래에도 길이 있습니다","얇은 다리 위에서 "+down+"로 내려가세요","낮은 천장에서는 웅크려 이동하세요");
        if(x<2600)return new Hint(stage*100+2,"바람을 타고 위로","빛나는 발판은 높이 솟아오릅니다","움직이는 승강 발판도 이용하세요");
        if(x<3400)return new Hint(stage*100+3,"봉인된 길과 높은 탑","레버 근처에서 "+interact+" · 문 열기","위를 탐색하거나 문으로 지름길을 여세요");
        return new Hint(stage*100+4,"다음 지역으로","가시는 회피하거나 점프로 넘으세요","수문장을 쓰러뜨리고 출구로 이동");
    }
}
