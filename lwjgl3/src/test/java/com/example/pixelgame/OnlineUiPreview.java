package com.example.pixelgame;

import static com.example.pixelgame.online.OnlineContract.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Opt-in production UI render check; isolated preferences and no real record-server calls. */
public class OnlineUiPreview extends ApplicationAdapter {
    Texture background;
    SpriteBatch batch;
    UiTheme ui;
    BitmapFont font;
    FreeTypeFontGenerator generator;
    FitViewport viewport;
    OnlineService online;
    OnlineScreen screen;
    Net oldNet;
    int frame, variation;
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();
        config.setTitle("Deadlight UI verification"); config.setWindowedMode(1280,720);
        config.setInitialVisible(false); config.setForegroundFPS(30); config.disableAudio(true);
        new Lwjgl3Application(new OnlineUiPreview(),config);
    }
    @Override public void create() {
        oldNet=Gdx.net;
        Gdx.net=(Net)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Net.class},(p,m,a)->{
            if(m.getName().equals("sendHttpRequest")) ((Net.HttpResponseListener)a[1]).failed(new RuntimeException("preview offline"));
            return null;
        });
        var state=new HashMap<String,Object>(); state.put("playerId","preview-player");state.put("token","x".repeat(43));state.put("initials","LGH");
        Preferences prefs=(Preferences)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Preferences.class},(p,m,a)->{
            if(m.getName().startsWith("put")) { state.put((String)a[0],a[1]);return p; }
            if(m.getName().equals("getString")) return state.getOrDefault(a[0],a.length>1?a[1]:"");
            return null;
        });
        online=new OnlineService("http://127.0.0.1:1",prefs);
        online.latestClear=new RunSubmission("preview","LGH",11,1,4,true,802800,0,0,0,false,false);
        screen=new OnlineScreen(); batch=new SpriteBatch(); ui=new UiTheme();
        background=new Texture("art/title.png");background.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        generator=new FreeTypeFontGenerator(Gdx.files.internal("fonts/NanumGothic-Regular.ttf"));
        var p=new FreeTypeFontGenerator.FreeTypeFontParameter(); p.size=72;p.incremental=true;
        p.shadowOffsetY=2;p.shadowColor.set(0,0,0,.65f);p.borderWidth=1.4f;p.borderColor.set(.95f,.97f,1,1);
        p.minFilter=p.magFilter=Texture.TextureFilter.Linear;font=generator.generateFont(p);font.setUseIntegerPositions(false);
        viewport=new FitViewport(640,360);viewport.update(1280,720,true);
    }
    @Override public void resize(int width,int height) { if(viewport!=null)viewport.update(width,height,true); }
    @Override public void render() {
        if(++frame<3)return;
        int index=(frame-3)%5;
        ScreenUtils.clear(.02f,.03f,.05f,1);viewport.apply();batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();batch.setColor(Color.WHITE);batch.draw(background,0,0,640,360);
        if(index==0) new LobbyView().draw(batch,font,ui,0,2,false,0,new ClearRecords.Summary(802.8f,1,0));
        else {
            if(index==1 || index==4) {
                screen.openRanking(online,1); online.boardLoading=false;online.boardError=index==4?"기록 서버에 연결하지 못했습니다. 서버 실행과 연결을 확인해 주세요.":"";
                String[] names={"RYN","NOX","ASH","LUN","EON","SOL","LGH","IAN","RUE","KAI"};
                List<Entry> entries=new ArrayList<>();for(int i=0;i<10;i++)entries.add(new Entry(i+1,i==6?online.playerId():"other"+i,names[i],600000+i*33800,0));
                online.board=new Board(entries,entries.get(6));
            } else if(index==2) screen.openEntry(online);
            else {
                screen.openAchievements(online); online.achievementsLoading=false; online.achievementError="";
                var achievements=new ArrayList<Achievement>();
                for(Goal goal:Goal.values())achievements.add(new Achievement(goal.name(),goal.title,goal.description,goal.ordinal()<2,"2026-11-25T00:00:00Z"));
                online.achievements=achievements;
            }
            screen.draw(batch,font,ui,online);
        }
        batch.end();
        if(Gdx.gl.glGetError()!=GL20.GL_NO_ERROR)throw new IllegalStateException("OpenGL error");
        var pixels=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        var out=Gdx.files.absolute(System.getProperty("deadlight.previewDir"));out.mkdirs();
        String[] names={"lobby","ranking","registration","achievements","offline"};
        PixmapIO.writePNG(out.child(variation+"-"+names[index]+".png"),pixels,-1,true);pixels.dispose();
        if(index==4) {
            variation++;
            if(variation==1)Gdx.graphics.setWindowedMode(960,540);
            else if(variation==2)GameSettings.current.set(GameSettings.Option.TEXT_SIZE,120);
            else Gdx.app.exit();
        }
    }
    @Override public void dispose() {
        online.close();Gdx.net=oldNet;background.dispose();batch.dispose();font.dispose();generator.dispose();ui.dispose();
    }
}
