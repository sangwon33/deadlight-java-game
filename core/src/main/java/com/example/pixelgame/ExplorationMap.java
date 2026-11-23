package com.example.pixelgame;
import java.util.BitSet;
import java.util.HashMap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Run-local fog of war; render only visited cell fragments, never an entire undiscovered surface. */
final class ExplorationMap {
    static Rectangle fullBounds(){return new Rectangle(22,42,396,286);}
    static final int CELL=64;
    private static final class Grid {
        final int columns,rows;final float low;final BitSet seen=new BitSet();
        int minCol=Integer.MAX_VALUE,maxCol=-1,minRow=Integer.MAX_VALUE,maxRow=-1;
        Grid(int stage){columns=(int)Math.ceil(StageLayout.width(stage)/CELL);low=StageLayout.bottom(stage);rows=(int)Math.ceil((StageLayout.top(stage)-low)/CELL);}
        void mark(int c,int r){if(c<0||r<0||c>=columns||r>=rows)return;seen.set(r*columns+c);minCol=Math.min(minCol,c);maxCol=Math.max(maxCol,c);minRow=Math.min(minRow,r);maxRow=Math.max(maxRow,r);}
        boolean has(int c,int r){return c>=0&&r>=0&&c<columns&&r<rows&&seen.get(r*columns+c);}
    }
    private final HashMap<Integer,Grid> regions=new HashMap<>();
    void reset(){regions.clear();}
    void visit(int stage,Player p){if(p.isAlive())visit(stage,p.getCenterX(),p.getY()+p.getBounds().height/2);}
    void visit(int stage,float x,float y){
        if(stage<1||stage>3)return;
        Grid g=regions.computeIfAbsent(stage,Grid::new);
        int c=(int)Math.floor(x/CELL),r=(int)Math.floor((y-g.low)/CELL);
        for(int row=r-2;row<=r+2;row++)for(int col=c-2;col<=c+2;col++){
            float dx=((col+.5f)*CELL-x)/112f,dy=(g.low+(row+.5f)*CELL-y)/64f;
            if(dx*dx+dy*dy<=1)g.mark(col,row);
        }
        g.mark(c,r);
    }
    boolean explored(int stage,float x,float y){
        Grid g=regions.get(stage);return g!=null&&g.has((int)Math.floor(x/CELL),(int)Math.floor((y-g.low)/CELL));
    }
    int count(int stage){Grid g=regions.get(stage);return g==null?0:g.seen.cardinality();}
    private Rectangle view(int stage,Player p,boolean mini){
        float ww=StageLayout.width(stage),low=StageLayout.bottom(stage),height=StageLayout.top(stage)-low;
        if(mini)return new Rectangle(MathUtils.clamp(p.getCenterX()-360,0,ww-720),MathUtils.clamp(p.getY()-135,low,StageLayout.top(stage)-320),720,320);
        Grid g=regions.get(stage);
        float minX=g==null? p.getCenterX()-100:g.minCol*CELL-128;
        float maxX=g==null? p.getCenterX()+100:(g.maxCol+1)*CELL+128;
        float minY=g==null? p.getY()-100:low+g.minRow*CELL-96;
        float maxY=g==null? p.getY()+100:low+(g.maxRow+1)*CELL+96;
        float w=Math.min(ww,Math.max(1400,maxX-minX)),h=Math.min(height,Math.max(640,maxY-minY));
        float x=MathUtils.clamp((minX+maxX-w)/2,0,ww-w),y=MathUtils.clamp((minY+maxY-h)/2,low,low+height-h);
        return new Rectangle(x,y,w,h);
    }
    void draw(SpriteBatch b,UiTheme ui,BitmapFont f,int stage,Player p,WorldGimmicks mechanisms,boolean mini){
        draw(b,ui,f,stage,p,mechanisms,null,mini);
    }
    void draw(SpriteBatch b,UiTheme ui,BitmapFont f,int stage,Player p,WorldGimmicks mechanisms,RegionGuide guide,boolean mini){
        draw(b,ui,f,stage,p,mechanisms,guide,null,null,mini);
    }
    void draw(SpriteBatch b,UiTheme ui,BitmapFont f,int stage,Player p,WorldGimmicks mechanisms,RegionGuide guide,LamplighterEncounter lamp,SideBossEncounter side,boolean mini){
        if(stage<1||stage>3)return;
        Rectangle panel=mini?(GameSettings.current.on(GameSettings.Option.PLAYER_STATS)?PlayerStatsView.minimapBounds():new Rectangle(526,229,102,68)):fullBounds();
        if(!mini)ui.rect(b,0,0,640,360,UiTheme.SURFACE,.97f);
        ui.rect(b,panel.x,panel.y,panel.width,panel.height,UiTheme.SURFACE,1);
        ui.panel(b,panel.x,panel.y,panel.width,panel.height);
        if(mini)ui.centered(b,f,"Tab 지도",new Rectangle(panel.x+4,panel.y+50,94,16),.61f,UiTheme.TEXT);
        else{
            ui.diamond(b,panel.x+23,300,6,UiTheme.GOLD,.9f);
            ui.text(b,f,StageLayout.name(stage),panel.x+38,313,1.05f,UiTheme.TEXT,panel.width-125);
            ui.text(b,f,"발걸음이 남긴 지도",panel.x+38,289,.66f,UiTheme.MUTED,panel.width-65);
            ui.line(b,panel.x+16,277,panel.x+panel.width-16,277,.7f,UiTheme.BORDER,.8f);
            ui.text(b,f,"0"+stage+" / 03",panel.x+panel.width-72,307,.72f,UiTheme.GOLD,60);
            for(float x:new float[]{panel.x+10,panel.x+panel.width-10})for(float y:new float[]{52,318})ui.diamond(b,x,y,3,UiTheme.GOLD,.9f);
        }
        Rectangle plot=mini?new Rectangle(panel.x+6,panel.y+6,90,40):new Rectangle(panel.x+22,112,panel.width-44,156);
        Rectangle v=view(stage,p,mini);
        float scale=Math.min(plot.width/v.width,plot.height/v.height);
        float ox=plot.x+(plot.width-v.width*scale)/2,oy=plot.y+(plot.height-v.height*scale)/2;
        ui.rect(b,plot.x,plot.y,plot.width,plot.height,new Color(.025f,.047f,.072f,1),1);
        if(!mini){
            for(float x=plot.x;x<=plot.x+plot.width;x+=30)ui.line(b,x,plot.y,x,plot.y+plot.height,.5f,UiTheme.BORDER,.25f);
            for(float y=plot.y;y<=plot.y+plot.height;y+=20)ui.line(b,plot.x,y,plot.x+plot.width,y,.5f,UiTheme.BORDER,.25f);
            float cx=plot.x+plot.width-22,cy=242;
            for(int i=0;i<8;i++){float a=i*45*(float)Math.PI/180;ui.line(b,cx,cy,cx+(float)Math.cos(a)*14,cy+(float)Math.sin(a)*14,.55f,UiTheme.GOLD,.45f);}
            ui.diamond(b,cx,cy,5,UiTheme.GOLD,.5f);
            ui.triangle(b,cx-2,cy+8,cx,cy+19,cx+2,cy+8,UiTheme.GOLD,.7f);
        }
        Grid g=regions.get(stage);
        if(g!=null)for(int bit=g.seen.nextSetBit(0);bit>=0;bit=g.seen.nextSetBit(bit+1)){
            float wx=(bit%g.columns)*CELL,wy=g.low+(bit/g.columns)*CELL;
            float left=Math.max(wx,v.x),right=Math.min(wx+CELL,v.x+v.width),low=Math.max(wy,v.y),high=Math.min(wy+CELL,v.y+v.height);
            if(right>left&&high>low){
                float x=ox+(left-v.x)*scale,y=oy+(low-v.y)*scale,w=(right-left)*scale,h=(high-low)*scale;
                ui.rect(b,x,y,w,h,UiTheme.BORDER,mini?.13f:.24f);
                int c=bit%g.columns,r=bit/g.columns;
                if(!mini){
                    if(!g.has(c-1,r))ui.line(b,x,y,x,y+h,.6f,UiTheme.GOLD,.38f);
                    if(!g.has(c+1,r))ui.line(b,x+w,y,x+w,y+h,.6f,UiTheme.GOLD,.38f);
                    if(!g.has(c,r-1))ui.line(b,x,y,x+w,y,.6f,UiTheme.GOLD,.38f);
                    if(!g.has(c,r+1))ui.line(b,x,y+h,x+w,y+h,.6f,UiTheme.GOLD,.38f);
                }
            }
        }
        for(Rectangle r:StageLayout.floors(stage))surface(b,ui,stage,r.x,r.x+r.width,r.y+r.height,v,ox,oy,scale,mini?1.3f:3,UiTheme.BORDER);
        for(Rectangle r:StageLayout.platforms(stage)){
            boolean collapsed=false;for(WorldGimmicks.Bridge bridge:mechanisms.bridges)if(bridge.respawn>0&&Math.abs(bridge.bounds.x-r.x)<.1&&Math.abs(bridge.bounds.y-r.y)<.1)collapsed=true;
            if(!collapsed)surface(b,ui,stage,r.x,r.x+r.width,r.y+r.height,v,ox,oy,scale,mini?1:2,UiTheme.GOLD);
        }
        for(WorldGimmicks.Lift lift:mechanisms.lifts){
            float x=lift.bounds.x+lift.bounds.width/2,regionLow=StageLayout.bottom(stage);
            for(int row=(int)Math.floor((lift.low-regionLow)/CELL);regionLow+row*CELL<lift.high;row++){
                float y=Math.max(lift.low,regionLow+row*CELL),last=Math.min(lift.high,regionLow+(row+1)*CELL);
                if(explored(stage,x,(y+last)/2)&&inside(v,x,y)&&inside(v,x,last))ui.line(b,ox+(x-v.x)*scale,oy+(y-v.y)*scale,ox+(x-v.x)*scale,oy+(last-v.y)*scale,.8f,UiTheme.SUCCESS,.7f);
            }
            surface(b,ui,stage,lift.bounds.x,lift.bounds.x+lift.bounds.width,lift.bounds.y+18,v,ox,oy,scale,2,UiTheme.SUCCESS);
        }
        for(WorldGimmicks.Wind wind:mechanisms.winds)marker(b,ui,stage,wind.x,wind.floor+8,v,ox,oy,scale,mini?1.7f:3.3f,UiTheme.SUCCESS);
        for(WorldGimmicks.Jet jet:mechanisms.jets)marker(b,ui,stage,jet.x,jet.floor+8,v,ox,oy,scale,mini?1.7f:3.3f,Color.SALMON);
        for(WorldGimmicks.Well well:mechanisms.wells)marker(b,ui,stage,well.x,well.floor+8,v,ox,oy,scale,mini?1.8f:3.8f,well.used?UiTheme.MUTED:UiTheme.SUCCESS);
        if(guide!=null)for(StageLayout.Supply supply:StageLayout.supplies(stage))if(!guide.claimed(supply.id()))marker(b,ui,stage,supply.x(),supply.floor()+8,v,ox,oy,scale,mini?1.4f:3,UiTheme.GOLD);
        if(lamp!=null)trial(b,ui,f,stage,Lamplighter.RIGHT-95,Lamplighter.FLOOR+20,"등불 감시탑",lamp.boss.defeated(),v,ox,oy,scale,mini);
        if(side!=null)trial(b,ui,f,stage,side.arena.entry(),side.arena.floor()+20,side.arena.room(),side.boss.defeated(),v,ox,oy,scale,mini);
        float exit=StageLayout.width(stage)-100;
        if(explored(stage,exit,80)&&inside(v,exit,80)){
            float sx=ox+(exit-v.x)*scale,sy=oy+(80-v.y)*scale;
            ui.diamond(b,sx,sy,mini?2:4,UiTheme.SUCCESS,1);
            if(!mini)ui.text(b,f,"출구",sx-13,sy+18,.65f,UiTheme.TEXT,35);
        }
        float px=ox+(p.getCenterX()-v.x)*scale,py=oy+(p.getY()+p.getBounds().height/2-v.y)*scale;
        ui.diamond(b,px,py,mini?4:7,UiTheme.GOLD,.25f);
        ui.diamond(b,px,py,mini?2.5f:4,UiTheme.TEXT,1);
        if(!mini){
            float d=p.isFacingRight()?1:-1;ui.triangle(b,px+d*8,py-2,px+d*12,py,px+d*8,py+2,UiTheme.TEXT,1);
            String[] names={"현재 위치","보급품","기믹 · 우물","위험 · 시련"};
            Color[] colors={UiTheme.TEXT,UiTheme.GOLD,UiTheme.SUCCESS,Color.SALMON};
            for(int i=0;i<4;i++){
                float x=panel.x+26+(i%2)*178,y=91-(i/2)*18;
                ui.diamond(b,x,y,2.8f,colors[i],1);ui.text(b,f,names[i],x+10,y+7,.63f,UiTheme.MUTED,145);
            }
            ui.line(b,panel.x+16,60,panel.x+panel.width-16,60,.6f,UiTheme.BORDER,.8f);
            ui.centered(b,f,"미탐색 구역은 숨겨집니다 · Tab을 놓으면 계속",new Rectangle(22,15,596,21),.66f,UiTheme.MUTED);
        }
    }
    private boolean inside(Rectangle v,float x,float y){return x>=v.x&&x<=v.x+v.width&&y>=v.y&&y<=v.y+v.height;}
    private void trial(SpriteBatch b,UiTheme ui,BitmapFont f,int stage,float x,float y,String name,boolean defeated,Rectangle v,float ox,float oy,float scale,boolean mini){
        if(!explored(stage,x,y)||!inside(v,x,y))return;
        float sx=ox+(x-v.x)*scale,sy=oy+(y-v.y)*scale;
        ui.diamond(b,sx,sy,mini?2:4,defeated?UiTheme.SUCCESS:Color.SALMON,1);
        if(!mini)ui.text(b,f,name+(defeated?" · 완료":" · 시련"),sx-35,sy+20,.54f,UiTheme.TEXT,100);
    }
    private void marker(SpriteBatch b,UiTheme ui,int stage,float x,float y,Rectangle v,float ox,float oy,float scale,float size,Color color){
        if(explored(stage,x,y)&&inside(v,x,y))ui.diamond(b,ox+(x-v.x)*scale,oy+(y-v.y)*scale,size,color,1);
    }
    private void surface(SpriteBatch b,UiTheme ui,int stage,float left,float right,float y,Rectangle v,float ox,float oy,float scale,float thickness,Color color){
        if(y<v.y||y>v.y+v.height)return;
        left=Math.max(left,v.x);right=Math.min(right,v.x+v.width);
        for(int c=(int)Math.floor(left/CELL);c*CELL<right;c++){
            float start=Math.max(left,c*CELL),end=Math.min(right,(c+1)*CELL);
            if(end>start&&explored(stage,(start+end)/2,y+8))ui.rect(b,ox+(start-v.x)*scale,oy+(y-v.y)*scale,(end-start)*scale,thickness,color,1);
        }
    }
}
