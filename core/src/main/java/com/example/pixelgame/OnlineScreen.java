package com.example.pixelgame;

import static com.example.pixelgame.online.OnlineContract.*;
import java.util.Locale;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/** The existing title painting and UiTheme provide the complete visual language. */
final class OnlineScreen {
    enum Page { RANKING, ENTRY, ACHIEVEMENTS }
    enum Action { STAY, BACK, PLAY_AGAIN }
    Page page = Page.RANKING;
    int startStage = 1;
    private int selected = 2, slot, pointerX, pointerY, submittedError;
    private boolean nameFocus = true, waiting;
    private final char[] letters = {'A','A','A'};
    private RunSubmission candidate;
    private final Vector2 cursor = new Vector2();
    private static final Color OWN = new Color(.12f,.24f,.29f,1);

    void openRanking(OnlineService online, int stage) {
        page = Page.RANKING; startStage = stage; selected = 2; waiting = false;
        syncPointer(); online.retryNow(); online.loadBoard(stage);
    }
    void openAchievements(OnlineService online) {
        page = Page.ACHIEVEMENTS; selected = 0; waiting = false;
        syncPointer(); online.retryNow(); online.loadAchievements();
    }
    void openEntry(OnlineService online) {
        candidate = online.latestClear;
        if (candidate == null) { openRanking(online, 1); return; }
        if (candidate.publish()) { openRanking(online, candidate.startStage()); return; }
        page = Page.ENTRY; startStage = candidate.startStage(); slot = selected = 0;
        String name = online.initials();
        if (!name.matches("[A-Z]{3}")) name = "AAA";
        for (int i=0;i<3;i++) letters[i] = name.charAt(i);
        nameFocus = true; waiting = false; syncPointer();
    }
    private void syncPointer() { pointerX=Gdx.input.getX(); pointerY=Gdx.input.getY(); }
    private static Rectangle button(int index, int count) {
        float width = 364f/count;
        return new Rectangle(38+index*width, 38, width-4, 23);
    }
    private static Rectangle tab(int index) { return new Rectangle(101+index*119,265,116,19); }
    private static Rectangle stageTab(int index) { return new Rectangle(67+index*104,244,100,16); }
    private static Rectangle letter(int i) { return new Rectangle(143+i*54,124,42,34); }
    private boolean inside(Rectangle r) { return r.contains(cursor); }

    Action update(OnlineService online, Viewport viewport) {
        viewport.unproject(cursor.set(Gdx.input.getX(), Gdx.input.getY()));
        boolean click=Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean moved=pointerX!=Gdx.input.getX()||pointerY!=Gdx.input.getY();
        syncPointer();
        if (page==Page.ENTRY) {
            if (waiting && online.lastPublishedRunId.equals(candidate.runId())) { openRanking(online,startStage); return Action.STAY; }
            if (waiting && online.errorSequence!=submittedError) waiting=false;
            if (waiting) return Action.STAY;
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) return Action.BACK;
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) nameFocus=!nameFocus;
            if (nameFocus) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) slot=Math.floorMod(slot-1,3);
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) slot=(slot+1)%3;
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) changeLetter(1);
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) changeLetter(-1);
                if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) slot=Math.floorMod(slot-1,3);
                for (int i=0;i<26;i++) if (Gdx.input.isKeyJustPressed(Input.Keys.A+i)) { letters[slot]=(char)('A'+i); slot=(slot+1)%3; }
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)||Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                        ||Gdx.input.isKeyJustPressed(Input.Keys.UP)||Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selected=1-selected;
            }
            if (click) for(int i=0;i<3;i++) {
                Rectangle r=letter(i);
                if (inside(new Rectangle(r.x,r.y-13,r.width,r.height+26))) {
                    slot=i; nameFocus=true;
                    if (cursor.y>r.y+r.height) changeLetter(1);
                    else if(cursor.y<r.y) changeLetter(-1);
                    return Action.STAY;
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) return entryAction(online,nameFocus?0:selected);
            for(int i=0;i<2;i++) {
                if(moved&&inside(button(i,2))) { nameFocus=false; selected=i; }
                if(click&&inside(button(i,2))) return entryAction(online,i);
            }
            return Action.STAY;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) return Action.BACK;
        int count=page==Page.RANKING?4:2;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) selected=Math.floorMod(selected-1,count);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selected=(selected+1)%count;
        if (page==Page.RANKING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) changeStage(online,startStage==1?2:1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) changeStage(online,startStage==1?4:startStage-1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) changeStage(online,startStage==4?1:startStage+1);
            for(int i=0;i<2;i++) if(click&&inside(tab(i))) changeStage(online,i==0?1:2);
            if(startStage>1) for(int i=0;i<3;i++) if(click&&inside(stageTab(i))) changeStage(online,i+2);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) refresh(online);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) return menuAction(online,selected);
        for(int i=0;i<count;i++) {
            if(moved&&inside(button(i,count))) selected=i;
            if(click&&inside(button(i,count))) return menuAction(online,i);
        }
        return Action.STAY;
    }
    private void changeLetter(int delta) { letters[slot]=(char)('A'+Math.floorMod(letters[slot]-'A'+delta,26)); }
    private void changeStage(OnlineService online,int stage) { startStage=stage; online.loadBoard(stage); }
    private void refresh(OnlineService online) {
        online.retryNow(); if(page==Page.RANKING) online.loadBoard(startStage); else online.loadAchievements();
    }
    private Action entryAction(OnlineService online,int action) {
        if(action==1) return Action.BACK;
        online.lastPublishedRunId="";
        online.publish(candidate,new String(letters)); waiting=true; submittedError=online.errorSequence;
        return Action.STAY;
    }
    private Action menuAction(OnlineService online,int index) {
        if(page==Page.ACHIEVEMENTS) { if(index==1) return Action.BACK; refresh(online); return Action.STAY; }
        if(index==0) return Action.PLAY_AGAIN;
        if(index==1) { openEntry(online); return Action.STAY; }
        if(index==2) { refresh(online); return Action.STAY; }
        return Action.BACK;
    }

    void draw(SpriteBatch b,BitmapFont f,UiTheme ui,OnlineService online) {
        // 390/640 leaves the original masked wanderer and cathedral visible on the right.
        ui.rect(b,25,18,392,326,UiTheme.SURFACE,.88f);
        ui.panel(b,25,18,392,326);
        ui.centered(b,f,"D E A D L I G H T",new Rectangle(38,320,364,15),.55f,UiTheme.GOLD);
        ui.centered(b,f,page==Page.ENTRY?"여정을 기록하세요":page==Page.RANKING?"여정의 기록":"여행자의 업적",
                new Rectangle(38,290,364,27),1.15f,UiTheme.TEXT);
        ui.ornament(b,220,285,235);
        if(page==Page.RANKING) drawRanking(b,f,ui,online);
        else if(page==Page.ENTRY) drawEntry(b,f,ui,online);
        else drawAchievements(b,f,ui,online);
        String[] labels=page==Page.RANKING?new String[]{"다시 도전","기록 등록","새로고침","돌아가기"}
                :page==Page.ENTRY?new String[]{waiting?"등록 중…":"기록 등록","건너뛰기"}:new String[]{"새로고침","돌아가기"};
        for(int i=0;i<labels.length;i++) {
            boolean enabled=!(page==Page.ENTRY&&waiting)&&!(page==Page.RANKING&&i==1&&online.latestClear==null);
            ui.menuChoice(b,f,button(i,labels.length),labels[i],enabled&&selected==i&&!(page==Page.ENTRY&&nameFocus),false,.65f);
        }
        String hint=page==Page.ENTRY?"Enter 등록 · Tab 버튼 선택 · Esc 건너뛰기":"↑↓ 선택 · Enter 결정 · R 새로고침 · Esc 돌아가기";
        ui.centered(b,f,hint,new Rectangle(32,21,378,13),.47f,UiTheme.MUTED);
    }

    private void drawRanking(SpriteBatch b,BitmapFont f,UiTheme ui,OnlineService online) {
        for(int i=0;i<2;i++) {
            boolean active=(startStage==1)==(i==0);
            ui.menuChoice(b,f,tab(i),i==0?"전체 모험":"지역 연습",active,false,.72f);
            if(active) { Rectangle r=tab(i); ui.line(b,r.x+18,r.y,r.x+r.width-18,r.y,.8f,UiTheme.GOLD,1); }
        }
        if(startStage>1) for(int i=0;i<3;i++) ui.menuChoice(b,f,stageTab(i),StageLayout.name(i+2),startStage==i+2,false,.52f);
        else ui.centered(b,f,"클리어 시간 순위",new Rectangle(38,244,364,16),.59f,UiTheme.MUTED);
        ui.centered(b,f,"순위",new Rectangle(48,225,42,17),.57f,UiTheme.MUTED);
        ui.centered(b,f,"이름",new Rectangle(132,225,88,17),.57f,UiTheme.MUTED);
        ui.centered(b,f,"클리어 시간",new Rectangle(279,225,105,17),.57f,UiTheme.MUTED);
        ui.line(b,43,224,398,224,.5f,UiTheme.BORDER,.9f);
        if(online.boardLoading) message(b,f,ui,"기록을 불러오고 있습니다.",146,UiTheme.MUTED);
        else if(!online.boardError.isEmpty()) message(b,f,ui,online.boardError+"\n새로고침으로 다시 연결할 수 있습니다.",137,Color.SALMON);
        else if(online.board.entries().isEmpty()) message(b,f,ui,"아직 등록된 여정이 없습니다.\n첫 번째 기록을 남겨보세요.",138,UiTheme.MUTED);
        else {
            int i=0;
            for(Entry entry:online.board.entries()) {
                float y=208-i++*12.4f;
                boolean own=entry.playerId().equals(online.playerId());
                if(own) { ui.rect(b,40,y-1,360,13,OWN,.86f); ui.diamond(b,49,y+5,2,UiTheme.GOLD,1); }
                ui.centered(b,f,String.format(Locale.ROOT,"%02d",entry.rank()),new Rectangle(49,y,40,12),.58f,UiTheme.TEXT);
                ui.centered(b,f,entry.initials()+(own?" · 나":""),new Rectangle(130,y,110,12),.6f,own?UiTheme.GOLD:UiTheme.TEXT);
                ui.centered(b,f,time(entry.durationMillis()),new Rectangle(278,y,112,12),.62f,UiTheme.TEXT);
                ui.line(b,43,y-1,398,y-1,.3f,UiTheme.BORDER,.7f);
            }
        }
        Entry personal=online.boardLoading||!online.boardError.isEmpty()?null:online.board.personalBest();
        ui.line(b,43,82,398,82,.6f,UiTheme.BORDER,.9f);
        ui.centered(b,f,"내 최고 기록",new Rectangle(42,64,100,17),.59f,UiTheme.GOLD);
        String personalText=online.boardLoading?"불러오는 중…":!online.boardError.isEmpty()?"서버 연결 필요"
                :personal==null?"등록된 기록 없음":personal.rank()+"위 · "+personal.initials()+" · "+time(personal.durationMillis());
        ui.centered(b,f,personalText,
                new Rectangle(144,64,253,17),.62f,UiTheme.TEXT);
    }

    private void drawEntry(SpriteBatch b,BitmapFont f,UiTheme ui,OnlineService online) {
        ui.centered(b,f,startStage==1?"전체 모험 완료":StageLayout.name(startStage)+" 연습 완료",new Rectangle(38,260,364,19),.72f,UiTheme.TEXT);
        ui.centered(b,f,"클리어 시간",new Rectangle(38,239,364,16),.61f,UiTheme.MUTED);
        ui.centered(b,f,candidate==null?"--:--.-":time(candidate.durationMillis()),new Rectangle(38,199,364,39),1.85f,UiTheme.TEXT);
        ui.ornament(b,220,189,300);
        ui.centered(b,f,"이름을 남겨주세요",new Rectangle(38,171,364,17),.72f,UiTheme.TEXT);
        for(int i=0;i<3;i++) {
            Rectangle r=letter(i); boolean active=nameFocus&&i==slot;
            if(active) { ui.rect(b,r.x,r.y,r.width,r.height,OWN,.65f); ui.panel(b,r.x,r.y,r.width,r.height); }
            ui.centered(b,f,Character.toString(letters[i]),r,1.65f,active?UiTheme.TEXT:UiTheme.MUTED);
            ui.line(b,r.x+4,r.y,r.x+r.width-4,r.y,.7f,UiTheme.GOLD,active?1:.6f);
            float x=r.x+r.width/2;
            ui.triangle(b,x,r.y+r.height+9,x-3,r.y+r.height+4,x+3,r.y+r.height+4,active?UiTheme.TEXT:UiTheme.BORDER,1);
            ui.triangle(b,x,r.y-10,x-3,r.y-5,x+3,r.y-5,active?UiTheme.TEXT:UiTheme.BORDER,1);
        }
        ui.centered(b,f,"영문 A–Z · 3글자",new Rectangle(38,98,364,16),.58f,UiTheme.MUTED);
        ui.centered(b,f,"←→ 자리 이동 · ↑↓ 글자 변경 · 영문 직접 입력",new Rectangle(38,83,364,15),.51f,UiTheme.MUTED);
        String note=waiting?"기록을 안전하게 등록하고 있습니다.":!online.lastError.isEmpty()?online.lastError:"등록한 기록은 다른 여행자와 함께 표시됩니다.";
        ui.paragraph(b,f,note,new Rectangle(44,62,350,20),.51f,!online.lastError.isEmpty()?Color.SALMON:UiTheme.MUTED);
    }

    private void drawAchievements(SpriteBatch b,BitmapFont f,UiTheme ui,OnlineService online) {
        long count=online.achievements.stream().filter(Achievement::unlocked).count();
        ui.centered(b,f,"전체 모험의 발자취 · "+count+" / 4",new Rectangle(38,260,364,19),.65f,UiTheme.MUTED);
        for(int i=0;i<online.achievements.size();i++) {
            Achievement a=online.achievements.get(i); float y=221-i*42;
            ui.diamond(b,65,y+14,8,a.unlocked()?UiTheme.GOLD:UiTheme.BORDER,a.unlocked()?1:.6f);
            if(a.unlocked()) ui.diamond(b,65,y+14,4,UiTheme.SUCCESS,1);
            ui.label(b,f,a.title(),new Rectangle(80,y+12,248,21),.76f,a.unlocked()?UiTheme.TEXT:UiTheme.MUTED,com.badlogic.gdx.utils.Align.left);
            ui.label(b,f,a.description(),new Rectangle(80,y-7,306,17),.54f,UiTheme.MUTED,com.badlogic.gdx.utils.Align.left);
            String status=a.unlocked()?"달성":"미달성";
            ui.centered(b,f,status,new Rectangle(329,y+19,60,16),.52f,a.unlocked()?UiTheme.SUCCESS:UiTheme.BORDER);
            ui.line(b,45,y-10,397,y-10,.45f,UiTheme.BORDER,.65f);
        }
        String note=online.achievementsLoading?"업적을 불러오고 있습니다.":!online.achievementError.isEmpty()?online.achievementError
                :online.pendingCount()>0?"미전송 기록 "+online.pendingCount()+"개 · 연결되면 자동으로 동기화합니다.":"달성한 업적은 기록 서버에 보관됩니다.";
        ui.paragraph(b,f,note,new Rectangle(44,64,350,18),.5f,!online.achievementError.isEmpty()?Color.SALMON:UiTheme.MUTED);
    }

    private void message(SpriteBatch b,BitmapFont f,UiTheme ui,String text,float y,Color color) {
        ui.paragraph(b,f,text,new Rectangle(72,y,300,42),.68f,color);
    }
    void drawToast(SpriteBatch b,BitmapFont f,UiTheme ui,OnlineService online) {
        if(!online.toastVisible()) return;
        ui.panel(b,177,345,280,15);
        ui.centered(b,f,online.toast,new Rectangle(180,345,274,14),.52f,UiTheme.SUCCESS);
    }
    static String time(long millis) { return String.format(Locale.ROOT,"%02d:%02d.%d",millis/60000,(millis/1000)%60,(millis/100)%10); }
}
