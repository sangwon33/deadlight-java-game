package com.example.pixelgame;

import java.util.List;
import com.badlogic.gdx.math.Rectangle;

/** Region-specific optional rooms. The main route and guardian gate remain independent. */
record SideBossArena(int stage,String name,String room,float x,float floor,float roof,int health,int gold,
                     String layout,float bodyWidth,float bodyHeight) {
    private static final SideBossArena ROOT=new SideBossArena(1,"뿌리에 잠든 순례자","뿌리 예배당",1000,940,1160,108,60,"root-pilgrim-layout-v1.json",58,82);
    private static final SideBossArena ASH=new SideBossArena(3,"재를 꿰매는 자","봉합자의 정원",3700,1300,1660,168,100,"ashen-stitcher-layout-v1.json",34,92);
    static SideBossArena forStage(int stage){return stage==1?ROOT:stage==3?ASH:null;}
    boolean roots(){return stage==1;}
    float left(){return x+20;}
    float right(){return x+850;}
    float entry(){return right()-95;}
    void solids(List<Rectangle> out){
        out.add(new Rectangle(x,floor-18,900,18));out.add(new Rectangle(x,floor,20,roof-floor));out.add(new Rectangle(x,roof,900,20));
    }
    private static void slab(List<Rectangle> p,float x,float top,float width){p.add(new Rectangle(x,top-18,width,18));}
    void platforms(List<Rectangle> p){
        if(roots()){
            slab(p,1610,540,180);slab(p,1390,620,180);slab(p,1610,700,180);
            slab(p,1830,780,180);slab(p,1910,860,200);
        }else{
            slab(p,3700,1060,180);slab(p,3920,1140,180);
            // A continuous outer balcony avoids a jump underneath the room's solid floor.
            slab(p,4100,1220,680);
        }
    }
}
