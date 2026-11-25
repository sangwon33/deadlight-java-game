package com.example.pixelgame;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.net.NetJavaImpl;
import com.example.pixelgame.server.RecordServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** Two actual game clients, independent identities, real libGDX HTTP and the same SQL server. */
class GameClientIntegrationTest {
    @Test void twoGameClientsPublishAndSeeEachOther() throws Exception {
        var oldApp=Gdx.app; var oldNet=Gdx.net;
        var callbacks=new ConcurrentLinkedQueue<Runnable>();
        var http=new NetJavaImpl();
        Gdx.app=(Application)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Application.class},(p,m,a)->{
            if(m.getName().equals("postRunnable")) { callbacks.add((Runnable)a[0]); return null; }
            throw new UnsupportedOperationException(m.getName());
        });
        Gdx.net=(Net)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Net.class},(p,m,a)->{
            if(m.getName().equals("sendHttpRequest")) { http.sendHttpRequest((Net.HttpRequest)a[0],(Net.HttpResponseListener)a[1]); return null; }
            if(m.getName().equals("cancelHttpRequest")) { http.cancelHttpRequest((Net.HttpRequest)a[0]); return null; }
            throw new UnsupportedOperationException(m.getName());
        });
        try(var server=new SpringApplicationBuilder(RecordServer.class).run("--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:clienttest;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
                "--spring.main.banner-mode=off","--logging.level.root=WARN")) {
            String url="http://127.0.0.1:"+server.getEnvironment().getProperty("local.server.port");
            Preferences previous=memory();
            String oldId=java.util.UUID.randomUUID().toString();
            previous.putString("playerId",oldId).putString("token","a".repeat(43)).flush();
            try(var a=new OnlineService(url,previous);var b=new OnlineService(url,memory())) {
                a.completed(new RunResult(true,4,20,300,10,802.8f,0,0,0,1,true,802.8f,1));
                a.publish(a.latestClear,"LGH");
                pump(callbacks,a,b,()->!a.lastPublishedRunId.isEmpty()&&!a.boardLoading);
                b.completed(new RunResult(true,4,20,300,10,750f,20,1,1,1,true,750f,1));
                b.publish(b.latestClear,"LGH");
                pump(callbacks,a,b,()->!b.lastPublishedRunId.isEmpty()&&!b.boardLoading);
                a.loadBoard(1); pump(callbacks,a,b,()->!a.boardLoading);
                assertEquals(2,a.board.entries().size());
                assertEquals(b.playerId(),a.board.entries().get(0).playerId());
                assertNotEquals(a.playerId(),b.playerId());
                assertEquals(oldId,a.playerId());
                assertEquals(2,a.board.personalBest().rank());
                assertEquals(4,a.achievements.stream().filter(x->x.unlocked()).count());
                assertEquals(1,b.achievements.stream().filter(x->x.unlocked()).count());
            }
        } finally { Gdx.app=oldApp; Gdx.net=oldNet; }
    }
    private static void pump(ConcurrentLinkedQueue<Runnable> callbacks,OnlineService a,OnlineService b,BooleanSupplier done) throws Exception {
        long deadline=System.nanoTime()+10_000_000_000L;
        do {
            Runnable r; while((r=callbacks.poll())!=null) r.run();
            a.update(.01f,true); b.update(.01f,true);
            if(done.getAsBoolean()) return;
            Thread.sleep(10);
        } while(System.nanoTime()<deadline);
        fail("Client request did not complete: "+a.lastError+" / "+b.lastError);
    }
    private static Preferences memory() {
        var map=new HashMap<String,Object>();
        return (Preferences)Proxy.newProxyInstance(GameClientIntegrationTest.class.getClassLoader(),new Class[]{Preferences.class},(p,m,a)->{
            if(m.getName().startsWith("put")) { map.put((String)a[0],a[1]); return p; }
            if(m.getName().equals("getString")) return map.getOrDefault(a[0],a.length>1?a[1]:"");
            if(m.getName().equals("flush")) return null;
            throw new UnsupportedOperationException(m.getName());
        });
    }
}
