package com.example.pixelgame.server;

import static com.example.pixelgame.online.OnlineContract.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/** Real HTTP + real file-backed SQL, including restart persistence and concurrent retries. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecordServerTest {
    @TempDir static Path directory;
    static ConfigurableApplicationContext context;
    static RecordService service;
    static JdbcTemplate db;
    static String base;
    static final HttpClient HTTP = HttpClient.newHttpClient();
    static PlayerCredentials persisted;

    @BeforeAll static void start() {
        context = new SpringApplicationBuilder(RecordServer.class).run("--server.port=0",
                "--spring.datasource.url=jdbc:h2:file:"+directory.resolve("records").toString().replace('\\','/')+";MODE=MySQL;DATABASE_TO_LOWER=TRUE",
                "--spring.main.banner-mode=off", "--logging.level.root=WARN");
        service=context.getBean(RecordService.class); db=context.getBean(JdbcTemplate.class);
        base="http://127.0.0.1:"+context.getEnvironment().getProperty("local.server.port");
    }
    @AfterAll static void stop() { if(context!=null) context.close(); }
    static RunSubmission run(int version,int stage,long millis,int damage,int potions,int retries,boolean publish) {
        return new RunSubmission(UUID.randomUUID().toString(),"LGH",version,stage,4,true,millis,damage,potions,retries,false,publish);
    }
    static String json(RunSubmission r) {
        return """
            {"runId":"%s","initials":"%s","rulesVersion":%d,"startStage":%d,"reachedStage":%d,
             "cleared":%s,"durationMillis":%d,"damageTaken":%d,"potionsUsed":%d,"retries":%d,"assisted":%s,"publish":%s}
            """.formatted(r.runId(),r.initials(),r.rulesVersion(),r.startStage(),r.reachedStage(),r.cleared(),r.durationMillis(),r.damageTaken(),r.potionsUsed(),r.retries(),r.assisted(),r.publish());
    }
    static HttpResponse<String> request(String method,String path,PlayerCredentials user,String body) throws Exception {
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/json");
        if(user!=null) b.header("Authorization","Bearer "+user.accessToken());
        b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body));
        return HTTP.send(b.build(),HttpResponse.BodyHandlers.ofString());
    }

    @Test @Order(8) void lostIdentityCanBeRestoredButExistingIdentityCannotBeOverwritten() throws Exception {
        PlayerCredentials identity=new PlayerCredentials(UUID.randomUUID().toString(),"a".repeat(43));
        String body="{\"playerId\":\""+identity.playerId()+"\",\"accessToken\":\""+identity.accessToken()+"\"}";
        assertEquals(200,request("POST","/api/players/recover",null,body).statusCode());
        assertEquals(identity.playerId(),service.authenticate("Bearer "+identity.accessToken()));
        assertEquals(200,request("POST","/api/players/recover",null,body).statusCode());
        assertEquals(401,request("POST","/api/players/recover",null,body.replace("a".repeat(43),"b".repeat(43))).statusCode());
        assertEquals(identity.playerId(),service.authenticate("Bearer "+identity.accessToken()));
        assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM players WHERE id=?",Integer.class,identity.playerId()));
        assertEquals(400,request("POST","/api/players/recover",null,"{}").statusCode());
    }

    @Test @Order(1) void httpRegistrationAndAuthentication() throws Exception {
        var response=request("POST","/api/players",null,"{}");
        assertEquals(201,response.statusCode());
        assertTrue(response.body().contains("accessToken"));
        assertEquals(401,request("POST","/api/runs",null,json(run(11,1,10000,0,0,0,true))).statusCode());
        var health=request("GET","/api/health",null,null);
        assertEquals(200,health.statusCode());
        assertTrue(health.body().contains("\"service\":\"deadlight-records\""));
        PlayerCredentials user=service.register();
        assertEquals(user.playerId(),service.authenticate("Bearer "+user.accessToken()));
        assertNotEquals(user.accessToken(),db.queryForObject("SELECT token_hash FROM players WHERE id = ?",String.class,user.playerId()));
    }

    @Test @Order(2) void twoUsersShareRankingButOnlyTheirBestRunAppears() throws Exception {
        PlayerCredentials a=service.register(),b=service.register();
        service.submit(a.playerId(),run(101,1,30000,0,0,0,true));
        service.submit(b.playerId(),run(101,1,20000,0,0,0,true));
        service.submit(a.playerId(),run(101,1,15000,0,0,0,true));
        Board board=service.leaderboard(a.playerId(),101,1,10);
        assertEquals(2,board.entries().size());
        assertEquals(a.playerId(),board.entries().get(0).playerId());
        assertEquals(b.playerId(),board.entries().get(1).playerId());
        assertEquals(15000,board.personalBest().durationMillis());
        var response=request("GET","/api/leaderboard?rulesVersion=101&startStage=1",b,null);
        assertEquals(200,response.statusCode());
        assertTrue(response.body().contains(a.playerId()));
        assertTrue(response.body().contains(b.playerId()));
    }

    @Test @Order(3) void privateClearAwardsGoalsAndPublishingIsIdempotent() throws Exception {
        persisted=service.register();
        RunSubmission clear=run(102,1,15000,0,0,0,false);
        SubmissionResult first=service.submit(persisted.playerId(),clear);
        assertEquals(4,first.newlyUnlocked().size());
        assertTrue(service.leaderboard(persisted.playerId(),102,1,10).entries().isEmpty());
        var published=request("POST","/api/runs",persisted,json(clear.publishAs("ABC")));
        assertEquals(200,published.statusCode());
        assertTrue(published.body().contains("\"duplicate\":true"));
        assertTrue(published.body().contains("\"newlyUnlocked\":[]"));
        // A late private copy must never undo a public record.
        assertTrue(service.submit(persisted.playerId(),clear).published());
        assertEquals("ABC",service.leaderboard(persisted.playerId(),102,1,10).personalBest().initials());
        assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM game_runs WHERE id = ?",Integer.class,clear.runId()));
        assertEquals(4,db.queryForObject("SELECT COUNT(*) FROM player_achievements WHERE player_id = ?",Integer.class,persisted.playerId()));
        RunSubmission changed=new RunSubmission(clear.runId(),"ABC",102,1,4,true,14000,0,0,0,false,true);
        assertEquals(409,request("POST","/api/runs",persisted,json(changed)).statusCode());
    }

    @Test @Order(4) void practiceAndRuleVersionsAreSeparateAndGoalsRequireCampaign() {
        PlayerCredentials user=service.register();
        service.submit(user.playerId(),run(103,2,10000,0,0,0,true));
        assertTrue(service.achievements(user.playerId()).stream().noneMatch(Achievement::unlocked));
        assertTrue(service.leaderboard(user.playerId(),103,1,10).entries().isEmpty());
        assertTrue(service.leaderboard(user.playerId(),104,2,10).entries().isEmpty());
        assertEquals(1,service.leaderboard(user.playerId(),103,2,10).entries().size());
        var result=service.submit(user.playerId(),run(103,1,20000,20,1,1,true));
        assertEquals(List.of("FIRST_CLEAR"),result.newlyUnlocked());
    }

    @Test @Order(5) void invalidAndAssistedRunsAreRejected() throws Exception {
        PlayerCredentials user=service.register();
        assertEquals(400,request("POST","/api/runs",user,json(run(105,1,-100,0,0,0,true))).statusCode());
        assertEquals(400,request("POST","/api/runs",user,json(run(105,1,12345,0,0,0,true))).statusCode());
        assertEquals(400,request("POST","/api/runs",user,json(run(105,1,10000,0,0,0,true)).replace("\"assisted\":false","\"assisted\":true")).statusCode());
        assertEquals(400,request("POST","/api/runs",user,"{}").statusCode());
        assertEquals(400,request("GET","/api/leaderboard?limit=999",user,null).statusCode());
        assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM game_runs WHERE player_id = ?",Integer.class,user.playerId()));
    }

    @Test @Order(6) void concurrentRetriesNeverDuplicateRunOrAchievements() throws Exception {
        PlayerCredentials user=service.register(); RunSubmission clear=run(106,1,10000,0,0,0,true);
        var pool=Executors.newFixedThreadPool(4);
        try {
            var tasks=java.util.stream.IntStream.range(0,8).<Callable<SubmissionResult>>mapToObj(i -> () -> service.submit(user.playerId(),clear)).toList();
            for(var future:pool.invokeAll(tasks)) assertTrue(future.get().published());
        } finally { pool.shutdownNow(); }
        assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM game_runs WHERE id = ?",Integer.class,clear.runId()));
        assertEquals(4,db.queryForObject("SELECT COUNT(*) FROM player_achievements WHERE player_id = ?",Integer.class,user.playerId()));
    }

    @Test @Order(7) void recordsAndPlayerIdentitySurviveServerRestart() {
        context.close(); start();
        assertEquals(persisted.playerId(),service.authenticate("Bearer "+persisted.accessToken()));
        assertEquals(4,service.achievements(persisted.playerId()).stream().filter(Achievement::unlocked).count());
        assertNotNull(service.leaderboard(persisted.playerId(),102,1,10).personalBest());
    }
}
