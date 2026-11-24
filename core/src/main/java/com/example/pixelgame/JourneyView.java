package com.example.pixelgame;

import java.util.Locale;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

final class JourneyView {
    private final UiTheme ui;
    static String regionName(int stage) { return StageLayout.name(stage); }
    JourneyView(UiTheme ui) { this.ui = ui; }
    static String time(float seconds) {
        long tenths = Math.max(0, Math.round(seconds * 10));
        return String.format(Locale.ROOT, "%02d:%02d.%d", tenths / 600, (tenths / 10) % 60, tenths % 10);
    }
    static Rectangle skipBounds(float left) { return skipBounds(left,0); }
    static Rectangle skipBounds(float left,float offsetY) { return new Rectangle(left+416,330+offsetY,48,18); }
    static Rectangle tutorialBounds(float left) { return new Rectangle(left+210,268,260,84); }

    void progress(SpriteBatch b, BitmapFont f, float left, int stage, int defeated, int total,
                  float position, float worldWidth, FallenGod boss, RunStats stats, int guardians) {
        ui.centered(b,f,StageLayout.name(stage),new Rectangle(left+219,328,260,22),.78f,UiTheme.TEXT);
        if(stage==4)return;
        String goal=guardians>0?"수문장을 찾아 처치하세요":"봉인 해제 · 출구로";
        ui.centered(b,f,goal,new Rectangle(left+219,308,260,18),.63f,guardians>0?UiTheme.MUTED:UiTheme.SUCCESS);
    }

    void tutorial(SpriteBatch b,BitmapFont f,float left,TutorialGuide guide,boolean hovered,boolean pressed){
        tutorial(b,f,left,guide,hovered,pressed,0);
    }
    static Rectangle previousBounds(float left){return new Rectangle(left+216,273,20,20);}
    static Rectangle nextBounds(float left){return new Rectangle(left+444,273,20,20);}
    void tutorial(SpriteBatch b,BitmapFont f,float left,TutorialGuide guide,boolean hovered,boolean pressed,float offsetY){
        if(!guide.visible())return;
        Rectangle r=tutorialBounds(left);ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.SURFACE,.9f);
        ui.line(b,r.x,r.y+r.height,r.x+r.width,r.y+r.height,.7f,guide.satisfied()?UiTheme.SUCCESS:UiTheme.BORDER,.85f);
        ui.label(b,f,guide.title(),new Rectangle(left+217,329,guide.finished()?244:193,20),.65f,guide.satisfied()?UiTheme.SUCCESS:UiTheme.GOLD,Align.left);
        ui.paragraph(b,f,guide.instruction(),new Rectangle(left+220,297,240,30),.65f,UiTheme.TEXT);
        ui.label(b,f,guide.detail(),new Rectangle(left+241,274,199,18),.54f,UiTheme.MUTED,Align.left);
        float width=33;
        for(int i=0;i<TutorialGuide.LESSON_COUNT;i++)ui.rect(b,left+220+i*35,269,width,1.5f,i<guide.step()||i==guide.step()&&guide.satisfied()?UiTheme.SUCCESS:UiTheme.BORDER,1);
        if(guide.satisfied())ui.rect(b,r.x,r.y+r.height-2,r.width*guide.completionProgress(),1.5f,UiTheme.SUCCESS,1);
        arrow(b,previousBounds(left),false,guide.canPrevious());arrow(b,nextBounds(left),true,guide.canNext());
        if(!guide.finished())ui.button(b,f,skipBounds(left),"건너뛰기",true,hovered,pressed,.52f);
    }
    private void arrow(SpriteBatch b,Rectangle r,boolean right,boolean enabled){
        float cx=r.x+r.width/2,cy=r.y+r.height/2,d=right?1:-1;
        ui.triangle(b,cx+d*5,cy,cx-d*3,cy+5,cx-d*3,cy-5,enabled?UiTheme.TEXT:UiTheme.BORDER,enabled?1:.45f);
    }

    void tutorialWorld(SpriteBatch b,BitmapFont f,TutorialGuide guide){
        if(!guide.practicingLook())return;
        float x=StageLayout.TUTORIAL_LOOK_X,top=StageLayout.TUTORIAL_LOOK_TOP;
        ui.line(b,x+6,top,x+StageLayout.TUTORIAL_LOOK_WIDTH-6,top,1.2f,UiTheme.GOLD,.9f);
        if(!guide.onLookPlatform()){
            ui.diamond(b,x+StageLayout.TUTORIAL_LOOK_WIDTH/2,top+17,5,UiTheme.GOLD,.9f);
            ui.centered(b,f,"전망 발판",new Rectangle(x+30,top+72,140,18),.61f,UiTheme.TEXT);
        }
        float landingX=x+80,landingY=StageLayout.TUTORIAL_LANDING_TOP;
        ui.verticalFade(b,landingX-28,landingY,56,11,UiTheme.GOLD,.24f,0);
        ui.line(b,landingX-24,landingY,landingX+24,landingY,1.3f,UiTheme.GOLD,1);
        ui.diamond(b,landingX,landingY+5,4,UiTheme.GOLD,.9f);
    }

    void exit(SpriteBatch b, BitmapFont f, float x, float ground, int guardians) {
        Color color = guardians > 0 ? UiTheme.MUTED : UiTheme.GOLD;
        ui.rect(b,x,ground,2,54,color,.8f);
        ui.panel(b,x-32,ground+54,102,22);
        ui.centered(b,f,guardians > 0 ? "봉인 "+guardians+" / 수문장" : "출구 >",
                new Rectangle(x-32,ground+54,102,22),.65f,color);
    }

    void records(SpriteBatch b, BitmapFont f, float left, ClearRecords.Summary record) {
        ui.panel(b, left + 338, 16, 282, 70);
        ui.text(b, f, "개편판 모험 기록", left + 350, 73, .65f, UiTheme.GOLD, 258);
        ui.text(b, f, record.exists() ? "최고 기록 " + time(record.bestSeconds()) + "   클리어 " + record.clears()
                : "아직 전체 클리어 기록이 없습니다.", left + 350, 55, .76f, UiTheme.TEXT, 258);
        ui.text(b, f, record.exists() ? "최소 받은 피해 " + record.leastDamage() + " 체력"
                : "모든 지역을 클리어하면 기록이 저장됩니다.", left + 350, 37, .6f, UiTheme.MUTED, 258);
    }
}
