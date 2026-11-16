package com.example.pixelgame;
import java.util.*;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

/** Presentation and input preferences only; saved separately from controls and adventure records. */
final class GameSettings {
    static GameSettings current=new GameSettings();
    enum Kind { NUMBER, VOLUME, BOOL, CHOICE }
    enum Option {
        MASTER(0,"masterVolume","전체 음량","배경음과 효과음의 전체 크기",100,0,100,10,Kind.VOLUME),
        MUSIC(0,"musicVolume","배경음","지역 배경 음악의 크기",25,0,100,10,Kind.VOLUME),
        EFFECTS(0,"effectsVolume","효과음","공격과 피격 효과음의 크기",50,0,100,10,Kind.VOLUME),
        MUTE(0,"musicMuted","배경음 음소거","현재 배경음만 잠시 끕니다",false),
        BLUR_MUTE(0,"muteOnBlur","다른 창에서 소리 끄기","게임 창에서 벗어나면 소리를 끕니다",true),
        FULLSCREEN(1,"fullscreen","전체 화면","현재 모니터 크기로 화면을 채웁니다",false),
        WINDOW_SIZE(1,"windowSize","창 크기","창 모드에서 사용할 화면 크기",1,"960 × 540","1280 × 720","1600 × 900"),
        VSYNC(1,"vsync","수직 동기화","화면이 가로로 갈라지는 현상을 줄입니다",true),
        FPS_LIMIT(1,"fpsLimit","프레임 제한","기기 부하와 전력 소모를 조절합니다",0,"60 FPS","120 FPS","제한 없음"),
        BRIGHTNESS(1,"brightness","게임 화면 밝기","어두운 지형과 배경을 더 밝게 봅니다",100,75,130,5,Kind.NUMBER),
        SHOW_FPS(1,"showFps","FPS 표시","화면 아래에 현재 프레임 수를 표시합니다",false),
        MINIMAP(2,"showMinimap","미니맵","오른쪽 위 지도만 숨기거나 표시합니다",true),
        DAMAGE_NUMBERS(2,"damageNumbers","피해 숫자","공격과 피격 때 피해량을 표시합니다",true),
        ENEMY_BARS(2,"enemyBars","적 머리 위 체력바","수문장 포함 · 보스의 상단 체력바는 유지",0,"기본","항상","숨김"),
        ATTACK_CUES(2,"attackCues","공격 예고와 빈틈","적의 공격 범위와 반격 기회를 표시합니다",true),
        DAMAGE_TRAIL(2,"damageTrail","체력 피해 잔상","잃은 체력이 천천히 줄어드는 표시",true),
        REGION_TIPS(2,"regionTips","지역 길 안내","처음 방문한 구역의 안내를 표시합니다",true),
        SCREEN_CONTROLS(3,"showScreenControls","화면 조작 버튼","마우스로 누르는 이동·전투 버튼",false),
        AUTO_PAUSE(3,"autoPause","다른 창에서 자동 멈춤","게임 창에서 벗어나면 일시정지합니다",true),
        LOOK_DELAY(3,"lookDelay","길게 누르기 시간","내려가기와 살펴보기를 구분하는 시간",1,"0.50초","0.65초","0.80초"),
        LOOK_RANGE(3,"lookRange","살펴보기 이동 범위","위·아래로 보여주는 시야의 범위",2,"60%","80%","100%"),
        SMOOTH_CAMERA(3,"smoothCamera","부드러운 카메라","이동과 살펴보기의 시야를 부드럽게 연결",true),
        TEXT_SIZE(4,"textSize","글자 크기","안내·메뉴·피해 숫자를 더 크게 표시합니다",100,100,120,10,Kind.NUMBER),
        REDUCED_EFFECTS(4,"reducedEffects","섬광과 점멸 완화","피격 테두리·전투 섬광·점멸을 줄입니다",false),
        HIT_STOP(4,"hitStop","타격 순간 짧은 멈춤","공격이 맞았을 때의 타격감을 조절합니다",true),
        HIGH_CONTRAST(4,"highContrast","캐릭터 외곽 강조","배경에서 캐릭터와 적을 더 또렷하게 구분",false);
        final int tab,initial,min,max,step;final String key,title,description;final Kind kind;final String[] choices;
        Option(int tab,String key,String title,String desc,int initial,int min,int max,int step,Kind kind){
            this.tab=tab;this.key=key;this.title=title;description=desc;this.initial=initial;this.min=min;this.max=max;this.step=step;this.kind=kind;choices=null;
        }
        Option(int tab,String key,String title,String desc,boolean initial){
            this(tab,key,title,desc,initial?1:0,0,1,1,Kind.BOOL);
        }
        Option(int tab,String key,String title,String desc,int initial,String... choices){
            this.tab=tab;this.key=key;this.title=title;description=desc;this.initial=initial;min=0;max=choices.length-1;step=1;kind=Kind.CHOICE;this.choices=choices;
        }
    }
    private final int[] values=new int[Option.values().length];
    GameSettings(){reset(-1);}
    int value(Option o){return values[o.ordinal()];}
    boolean on(Option o){return value(o)!=0;}
    void set(Option o,int value){values[o.ordinal()]=MathUtils.clamp(value,o.min,o.max);}
    void change(Option o,int direction){
        if(o.kind==Kind.BOOL)set(o,on(o)?0:1);
        else if(o.kind==Kind.CHOICE)set(o,Math.floorMod(value(o)+(direction<0?-1:1),o.max+1));
        else set(o,value(o)+(direction<0?-o.step:o.step));
    }
    void reset(int tab){for(Option o:Option.values())if(tab<0||o.tab==tab)values[o.ordinal()]=o.initial;}
    List<Option> entries(int tab){return Arrays.stream(Option.values()).filter(o->o.tab==tab).toList();}
    String label(Option o){
        return o.kind==Kind.BOOL?(on(o)?"켜짐":"꺼짐"):o.kind==Kind.CHOICE?o.choices[value(o)]:value(o)+"%";
    }
    void load(Preferences prefs){
        reset(-1);
        for(Option o:Option.values()){
            if(o.kind==Kind.VOLUME){float number=prefs.getFloat(o.key,o.initial/100f);set(o,Float.isFinite(number)?Math.round(number*100):o.initial);}
            else if(o.kind==Kind.BOOL)set(o,prefs.getBoolean(o.key,o.initial!=0)?1:0);
            else set(o,prefs.getInteger(o.key,o.initial));
        }
        if(!prefs.getBoolean("compactHud1",false))set(Option.SCREEN_CONTROLS,0);
    }
    void save(Preferences prefs){
        for(Option o:Option.values()){
            if(o.kind==Kind.VOLUME)prefs.putFloat(o.key,value(o)/100f);
            else if(o.kind==Kind.BOOL)prefs.putBoolean(o.key,on(o));
            else prefs.putInteger(o.key,value(o));
        }
        prefs.putBoolean("compactHud1",true).flush();
    }
    float volume(Option option){return value(option)/100f;}
    float lookDelay(){return new float[]{.5f,.65f,.8f}[value(Option.LOOK_DELAY)];}
    float lookRange(){return new float[]{.6f,.8f,1f}[value(Option.LOOK_RANGE)];}
    float textScale(){return value(Option.TEXT_SIZE)/100f;}
    int fpsLimit(){return new int[]{60,120,0}[value(Option.FPS_LIMIT)];}
    int windowWidth(){return new int[]{960,1280,1600}[value(Option.WINDOW_SIZE)];}
    int windowHeight(){return new int[]{540,720,900}[value(Option.WINDOW_SIZE)];}
}
