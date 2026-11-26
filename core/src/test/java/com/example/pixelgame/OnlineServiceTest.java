package com.example.pixelgame;

import static com.example.pixelgame.online.OnlineContract.*;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.*;

class OnlineServiceTest {
    record Call(Net.HttpRequest request, Net.HttpResponseListener listener) {}
    final ArrayList<Call> calls=new ArrayList<>();
    Map<String,Object> data;
    Preferences prefs;
    Application oldApp;
    Net oldNet;
    Input oldInput;
    Set<Integer> keys=Set.of();
    long nowNanos;
    static final String ACK="{\"published\":false,\"duplicate\":false,\"achievements\":[],\"newlyUnlocked\":[]}";

    @BeforeEach void setup() {
        oldApp=Gdx.app; oldNet=Gdx.net; oldInput=Gdx.input;
        Gdx.input=(Input)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Input.class},(p,m,a)->switch(m.getName()) {
            case "getX", "getY" -> 0;
            case "isKeyJustPressed" -> keys.contains((Integer)a[0]);
            case "isButtonJustPressed" -> false;
            default -> throw new UnsupportedOperationException(m.getName());
        });
        data=new HashMap<>(); data.put("playerId","test-player"); data.put("token","x".repeat(43));
        prefs=(Preferences)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Preferences.class},(p,m,a)->{
            String name=m.getName();
            if(name.startsWith("put")) { data.put((String)a[0],a[1]); return p; }
            if(name.equals("getString")) return data.getOrDefault(a[0],a.length>1?a[1]:"");
            if(name.equals("flush")) return null;
            if(name.equals("contains")) return data.containsKey(a[0]);
            throw new UnsupportedOperationException(name);
        });
        Gdx.app=(Application)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Application.class},(p,m,a)->{
            if(m.getName().equals("postRunnable")) { ((Runnable)a[0]).run(); return null; }
            throw new UnsupportedOperationException(m.getName());
        });
        Gdx.net=(Net)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Net.class},(p,m,a)->{
            if(m.getName().equals("sendHttpRequest")) { calls.add(new Call((Net.HttpRequest)a[0],(Net.HttpResponseListener)a[1])); return null; }
            if(m.getName().equals("cancelHttpRequest")) return null;
            throw new UnsupportedOperationException(m.getName());
        });
    }
    @AfterEach void cleanup() { Gdx.app=oldApp; Gdx.net=oldNet; Gdx.input=oldInput; }
    RunResult clear() { return new RunResult(true,4,20,300,10,803.28f,0,0,0,1,true,803.28f,1); }
    OnlineService cloud() { return new OnlineService("https://test.invalid",prefs,()->nowNanos); }
    void advance(OnlineService client,float seconds) {
        nowNanos+=(long)(seconds*1_000_000_000d); client.update(seconds,false);
    }
    void reply(int index,String body) {
        reply(index,body,200);
    }
    void reply(int index,String body,int status) {
        Net.HttpResponse response=(Net.HttpResponse)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Net.HttpResponse.class},(p,m,a)->switch(m.getName()){
            case "getStatus" -> new HttpStatus(status);
            case "getResultAsString" -> body;
            default -> throw new UnsupportedOperationException(m.getName());
        });
        calls.get(index).listener().handleHttpResponse(response);
    }

    @Test void failedUploadSurvivesClientRestartAndKeepsOriginalRunId() {
        OnlineService first=new OnlineService("http://localhost:8080",prefs); first.completed(clear());
        String id=first.latestClear.runId(); first.update(.1f,false);
        calls.get(0).listener().failed(new RuntimeException("offline"));
        assertEquals(1,first.pendingCount()); assertFalse(first.lastError.isBlank());
        OnlineService restarted=new OnlineService("http://localhost:8080",prefs); restarted.update(.1f,false);
        assertEquals(id,new JsonReader().parse(calls.get(1).request().getContent()).getString("runId"));
        reply(1,ACK); assertEquals(0,restarted.pendingCount());
        assertEquals(803300,restarted.latestClear.durationMillis());
    }

    @Test void publicationQueuedDuringPrivateUploadIsNotLost() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs); client.completed(clear());
        client.update(.1f,false); client.publish(client.latestClear,"LGH");
        reply(0,ACK); assertEquals(1,client.pendingCount());
        client.update(.1f,false);
        var payload=new JsonReader().parse(calls.get(1).request().getContent());
        assertTrue(payload.getBoolean("publish")); assertEquals("LGH",payload.getString("initials"));
        reply(1,ACK.replace("\"published\":false","\"published\":true"));
        assertEquals(0,client.pendingCount()); assertEquals(client.latestClear.runId(),client.lastPublishedRunId);
        assertTrue(client.latestClear.publish());
    }

    @Test void malformedAcknowledgementNeverDiscardsAnUnconfirmedRecord() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs); client.completed(clear());
        client.update(.1f,false); reply(0,"{}");
        assertEquals(1,client.pendingCount()); assertEquals(1,client.errorSequence);
    }

    @Test void practiceRunCannotUnlockCampaignGoals() {
        var run=new RunSubmission(java.util.UUID.randomUUID().toString(),"AAA",11,2,4,true,10000,0,0,0,false,true);
        for(Goal goal:Goal.values()) assertFalse(goal.earnedBy(run));
        assertEquals("13:22.8",OnlineScreen.time(802800));
    }

    OnlineScreen.Action press(OnlineScreen screen,OnlineService client,int... pressed) {
        keys=java.util.Arrays.stream(pressed).boxed().collect(java.util.stream.Collectors.toSet());
        Viewport viewport=new Viewport() { @Override public Vector2 unproject(Vector2 v) { return v; } };
        OnlineScreen.Action result=screen.update(client,viewport);
        keys=Set.of(); return result;
    }

    @Test void initialsKeyboardSubmissionOpensSharedRankingAndChangesCategory() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs); client.completed(clear());
        OnlineScreen screen=new OnlineScreen(); screen.openEntry(client);
        press(screen,client,Input.Keys.L); press(screen,client,Input.Keys.G); press(screen,client,Input.Keys.H);
        assertEquals(OnlineScreen.Action.STAY,press(screen,client,Input.Keys.ENTER));
        client.update(.1f,false);
        var run=new JsonReader().parse(calls.get(0).request().getContent());
        assertEquals("LGH",run.getString("initials")); assertTrue(run.getBoolean("publish"));
        reply(0,ACK.replace("\"published\":false","\"published\":true"));
        press(screen,client); assertEquals(OnlineScreen.Page.RANKING,screen.page);
        press(screen,client,Input.Keys.RIGHT); assertEquals(2,screen.startStage);
        assertTrue(calls.get(calls.size()-1).request().getUrl().contains("startStage=2"));
        press(screen,client,Input.Keys.TAB); assertEquals(1,screen.startStage);
        assertEquals(OnlineScreen.Action.BACK,press(screen,client,Input.Keys.ESCAPE));
    }

    @Test void failedRegistrationAllowsReturningWhileRecordRemainsQueued() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs); client.completed(clear());
        OnlineScreen screen=new OnlineScreen(); screen.openEntry(client);
        press(screen,client,Input.Keys.ENTER); client.update(.1f,false);
        calls.get(0).listener().failed(new RuntimeException("offline"));
        assertEquals(OnlineScreen.Action.BACK,press(screen,client,Input.Keys.ESCAPE));
        assertEquals(1,client.pendingCount());
    }

    @Test void ineligibleClearCannotReopenAnOlderRegistration() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs);
        assertTrue(client.completed(clear())); String previous=client.latestClear.runId();
        RunResult tooLong=new RunResult(true,4,20,300,10,43201,0,0,0,1,true,43201,1);
        assertFalse(client.completed(tooLong)); assertEquals(previous,client.latestClear.runId());
    }

    @Test void staleIdentityRecoversWithoutDiscardingTokenOrQueuedRun() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs); client.completed(clear());
        String id=client.latestClear.runId(); client.update(.1f,false);
        reply(0,"{\"message\":\"unauthorized\"}",401);
        assertTrue(calls.get(1).request().getUrl().endsWith("/players/recover"));
        reply(1,"{\"playerId\":\"test-player\",\"accessToken\":\""+"x".repeat(43)+"\"}");
        assertEquals(id,new JsonReader().parse(calls.get(2).request().getContent()).getString("runId"));
        reply(2,ACK); assertEquals(0,client.pendingCount());
        assertEquals("test-player",data.get("playerId")); assertEquals("x".repeat(43),data.get("token"));
    }

    @Test void simultaneousFailuresUseOneRecoveryAndRetryOnlyOnce() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs);
        client.loadBoard(1); client.loadAchievements();
        reply(0,"{}",401); reply(1,"{}",401); assertEquals(3,calls.size());
        reply(2,"{\"playerId\":\"test-player\",\"accessToken\":\""+"x".repeat(43)+"\"}");
        assertEquals(5,calls.size()); reply(3,"{}",401); reply(4,"{}",401);
        assertEquals(5,calls.size()); assertFalse(client.boardLoading); assertFalse(client.achievementsLoading);
    }

    @Test void rejectedRecoveryPreservesPendingRecord() {
        OnlineService client=new OnlineService("http://localhost:8080",prefs); client.completed(clear());
        client.update(.1f,false); reply(0,"{}",401); reply(1,"{}",409);
        assertEquals(1,client.pendingCount()); assertEquals("x".repeat(43),data.get("token"));
    }

    @Test void sleepingCloudServerRetriesAndFinishesLoading() {
        OnlineService client=cloud(); client.loadBoard(1);
        reply(0,"<html>starting</html>",503);
        assertTrue(client.boardLoading); assertTrue(client.waitingForServer());
        advance(client,3.1f); assertEquals(2,calls.size());
        assertTrue(client.waitingForServer()); // Stays visible while the next HTTP request is in flight.
        reply(1,"{\"entries\":[],\"personalBest\":null}");
        assertFalse(client.boardLoading); assertEquals("",client.boardError);
        assertFalse(client.waitingForServer());
    }

    @Test void fastWarmupResponsesCanWaitBeyondOneMinuteAndLoadWithoutRefresh() {
        OnlineService client=cloud(); client.loadBoard(1);
        for(int i=0;i<22;i++) { reply(i,"<!doctype html><html>starting</html>",200); advance(client,3); }
        assertTrue(client.boardLoading); assertTrue(client.boardError.isEmpty());
        reply(22,"{\"entries\":[{\"rank\":1,\"playerId\":\"other-player\",\"initials\":\"HSG\",\"durationMillis\":103400,\"damageTaken\":67}],\"personalBest\":null}");
        assertFalse(client.boardLoading); assertFalse(client.waitingForServer());
        assertEquals("HSG",client.board.entries().get(0).initials());
    }

    @Test void cloudRetriesStopAtTwoMinutesEvenWithImmediateFailures() {
        OnlineService client=cloud(); client.loadBoard(1);
        for(int i=0;i<40;i++) { reply(i,"{}",503); if(i<39)advance(client,3); }
        assertEquals(3000,calls.get(39).request().getTimeOut());
        advance(client,3);
        assertEquals(40,calls.size()); assertFalse(client.boardLoading); assertFalse(client.waitingForServer());
        assertTrue(client.boardError.contains("대기 시간이 초과"));
        client.loadBoard(1); reply(40,"{\"entries\":[],\"personalBest\":null}");
        assertFalse(client.boardLoading); assertTrue(client.boardError.isEmpty());
    }

    @Test void cloudTimeoutKeepsOriginalSubmissionUntilAcknowledged() {
        OnlineService client=cloud(); client.completed(clear()); client.update(.1f,false);
        String body=calls.get(0).request().getContent(); calls.get(0).listener().failed(new RuntimeException("timeout"));
        assertEquals(1,client.pendingCount()); advance(client,3.1f);
        assertEquals(body,calls.get(1).request().getContent()); reply(1,ACK); assertEquals(0,client.pendingCount());
    }

    @Test void closingClientCancelsDelayedCloudRequests() {
        OnlineService client=cloud(); client.loadBoard(1);
        reply(0,"{}",503); client.close(); advance(client,10); assertEquals(1,calls.size());
        assertFalse(client.waitingForServer());
    }

    @Test void aSuspendedWindowDoesNotExtendTheDeadlineOrDiscardAClear() {
        OnlineService client=cloud(); client.completed(clear()); client.update(.1f,false);
        String id=client.latestClear.runId(); reply(0,"starting",503);
        advance(client,121);
        assertEquals(1,calls.size()); assertEquals(1,client.pendingCount());
        assertEquals(id,client.latestClear.runId()); assertEquals(1,client.errorSequence);
        assertFalse(client.waitingForServer());
    }

    @Test void malformedSuccessIsReportedWithoutReplayingASuccessfulPost() {
        OnlineService client=cloud(); client.completed(clear()); client.update(.1f,false);
        reply(0,"{}"); advance(client,4);
        assertEquals(1,calls.size()); assertEquals(1,client.pendingCount());
        assertTrue(client.lastError.contains("게임 버전"));
    }

    @Test void nonJsonEdgeRejectionDoesNotDiscardTheQueuedClear() {
        OnlineService client=cloud(); client.completed(clear()); client.update(.1f,false);
        reply(0,"<html>proxy rejected request</html>",400);
        assertEquals(1,client.pendingCount()); assertTrue(client.lastError.contains("HTTP 400"));
    }

    @Test void playerCanLeaveRegistrationWhileUploadIsPending() {
        OnlineService client=new OnlineService("https://test.invalid",prefs); client.completed(clear());
        OnlineScreen screen=new OnlineScreen(); screen.openEntry(client); press(screen,client,Input.Keys.ENTER);
        assertEquals(OnlineScreen.Action.BACK,press(screen,client,Input.Keys.ESCAPE));
        assertEquals(1,client.pendingCount());
    }

    @Test void changingTunnelAddressPreservesIdentityAndPendingClear() {
        String previous=System.getProperty("deadlight.serverUrl");
        ArrayList<String> profiles=new ArrayList<>();
        Gdx.app=(Application)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Application.class},(p,m,a)->{
            if(m.getName().equals("getPreferences")) {profiles.add((String)a[0]);return prefs;}
            if(m.getName().equals("postRunnable")) {((Runnable)a[0]).run();return null;}
            throw new UnsupportedOperationException(m.getName());
        });
        try {
            System.setProperty("deadlight.serverUrl","https://first.trycloudflare.com");
            OnlineService first=new OnlineService();first.completed(clear());
            String runId=first.latestClear.runId();first.close();
            System.setProperty("deadlight.serverUrl","https://second.trycloudflare.com");
            OnlineService reconnected=new OnlineService();
            assertEquals(2,profiles.size());assertEquals(profiles.get(0),profiles.get(1));
            assertEquals("test-player",reconnected.playerId());
            assertEquals(runId,reconnected.latestClear.runId());assertEquals(1,reconnected.pendingCount());
            reconnected.close();
        } finally {
            if(previous==null)System.clearProperty("deadlight.serverUrl");else System.setProperty("deadlight.serverUrl",previous);
        }
    }

    @Test void offlineModePreservesClearsAndMakesNoHttpRequests() {
        String previous=System.getProperty("deadlight.offline");
        try {
            System.setProperty("deadlight.offline","true");
            OnlineService client=new OnlineService("http://localhost:8080",prefs);
            client.completed(clear());client.loadBoard(1);client.update(.1f,false);
            assertTrue(calls.isEmpty());assertEquals(1,client.pendingCount());
            assertFalse(client.boardLoading);assertTrue(client.boardError.contains("오프라인"));
        } finally {
            if(previous==null)System.clearProperty("deadlight.offline");else System.setProperty("deadlight.offline",previous);
        }
    }
}
