package com.example.pixelgame;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;

/** Five compact keyboard/mouse settings tabs; each option includes its effect in plain language. */
final class SettingsView {
    static final String[] TABS={"소리","화면","표시","조작","접근성"};
    int tab,selected;boolean resetConfirm,confirmYes;
    String message="자동 저장 · ↑↓ 선택 · ←→ 조절 · Tab 분류 이동";
    static Rectangle tabBounds(int tab){return new Rectangle(28+tab*117,298,117,28);}
    static Rectangle rowBounds(int row){return new Rectangle(38,250-row*34,564,32);}
    static Rectangle arrowBounds(int row,int dir){Rectangle r=rowBounds(row);return new Rectangle(dir<0?420:576,r.y+5,24,23);}
    static Rectangle footerBounds(int index){return new Rectangle(38+index*142,17,index==3?138:130,31);}
    static Rectangle confirmBounds(boolean yes){return new Rectangle(yes?153:337,122,150,30);}
    void changeTab(int next){tab=Math.floorMod(next,5);selected=0;message="자동 저장 · ↑↓ 선택 · ←→ 조절 · Tab 분류 이동";}
    void draw(SpriteBatch b,BitmapFont f,UiTheme ui,GameSettings options,int hoverRow,int hoverTab,int hoverFooter,boolean held){
        ui.rect(b,0,0,640,360,UiTheme.SURFACE,.5f);ui.panel(b,22,8,596,341);
        ui.text(b,f,"설정",38,342,1.03f,UiTheme.TEXT,220);
        ui.text(b,f,TABS[tab]+" · "+options.entries(tab).size()+"개 항목",420,337,.62f,UiTheme.MUTED,180);
        for(int i=0;i<5;i++){Rectangle r=tabBounds(i);ui.menuChoice(b,f,r,TABS[i],tab==i||hoverTab==i,held&&hoverTab==i,.75f);if(tab==i)ui.rect(b,r.x+20,296,r.width-40,1.5f,UiTheme.GOLD,1);}
        int row=0;
        for(GameSettings.Option option:options.entries(tab)){
            Rectangle r=rowBounds(row);boolean active=row==selected||row==hoverRow;
            if(active)ui.rect(b,r.x,r.y,r.width,r.height,UiTheme.GOLD,.05f);
            boolean enabled=option!=GameSettings.Option.WINDOW_SIZE||!options.on(GameSettings.Option.FULLSCREEN);
            ui.text(b,f,option.title,r.x+8,r.y+26,.74f,enabled?UiTheme.TEXT:UiTheme.MUTED,363);
            ui.text(b,f,option.description,r.x+8,r.y+10,.59f,UiTheme.MUTED,363);
            for(int direction:new int[]{-1,1})ui.button(b,f,arrowBounds(row,direction),direction<0?"‹":"›",enabled,active,held,.85f);
            ui.centered(b,f,options.label(option),new Rectangle(445,r.y+7,127,23),.73f,enabled?UiTheme.GOLD:UiTheme.MUTED);
            if(option.kind==GameSettings.Kind.VOLUME||option==GameSettings.Option.BRIGHTNESS){
                ui.rect(b,451,r.y+5,114,1.3f,UiTheme.BORDER,.7f);
                ui.rect(b,451,r.y+5,114*(options.value(option)-option.min)/(float)(option.max-option.min),1.3f,UiTheme.GOLD,.9f);
            }
            ui.line(b,r.x+8,r.y-1,r.x+r.width-8,r.y-1,.35f,UiTheme.BORDER,.6f);row++;
        }
        ui.text(b,f,message,38,62,.59f,UiTheme.MUTED,564);
        String[] footer={"이 탭 초기화","설정 초기화","키 설정","돌아가기"};
        for(int i=0;i<4;i++)ui.button(b,f,footerBounds(i),footer[i],true,hoverFooter==i||selected==options.entries(tab).size()+i,held&&hoverFooter==i,.7f);
        if(resetConfirm){
            ui.rect(b,0,0,640,360,UiTheme.SURFACE,.83f);ui.panel(b,122,108,396,132);
            ui.centered(b,f,"설정을 기본값으로 돌릴까요?",new Rectangle(139,195,362,29),.88f,UiTheme.TEXT);
            ui.centered(b,f,"소리·화면·표시·조작·접근성 설정을 초기화합니다.",new Rectangle(133,168,374,22),.62f,UiTheme.MUTED);
            ui.centered(b,f,"지정한 행동 키와 모험 기록은 유지합니다.",new Rectangle(133,150,374,19),.59f,UiTheme.MUTED);
            ui.button(b,f,confirmBounds(true),"초기화",true,confirmYes,false,.76f);
            ui.button(b,f,confirmBounds(false),"취소",true,!confirmYes,false,.76f);
        }
    }
}
