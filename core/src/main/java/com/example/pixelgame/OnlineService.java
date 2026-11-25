package com.example.pixelgame;

import static com.example.pixelgame.online.OnlineContract.*;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/** Non-blocking HTTP and a durable outbox. All state and preferences change on the render thread. */
final class OnlineService implements AutoCloseable {
    private final Preferences prefs;
    private final String baseUrl;
    private PlayerCredentials credentials;
    private final List<Runnable> waitingForIdentity = new ArrayList<>();
    private final List<BiConsumer<Integer,String>> identityFailures = new ArrayList<>();
    private final List<Runnable> recoveryActions = new ArrayList<>();
    private final List<BiConsumer<Integer,String>> recoveryFailures = new ArrayList<>();
    private boolean recovering;
    private final LinkedHashMap<String,RunSubmission> outbox = new LinkedHashMap<>();
    private final Set<Net.HttpRequest> requests = new HashSet<>();
    private boolean registering, sending, closed;
    private float retryDelay, toastSeconds;
    private long boardRequest;
    Board board = new Board(List.of(), null);
    List<Achievement> achievements = lockedAchievements();
    boolean boardLoading, achievementsLoading;
    String boardError = "", achievementError = "", lastError = "", toast = "";
    int errorSequence, boardStage = 1;
    String lastPublishedRunId = "";
    RunSubmission latestClear;

    OnlineService() { this(configuredUrl()); }
    private OnlineService(String url) {
        this(url, Gdx.app.getPreferences("deadlight.online." + UUID.nameUUIDFromBytes(url.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }
    OnlineService(String url, Preferences prefs) {
        baseUrl = url; this.prefs = prefs;
        String token = prefs.getString("token", ""), id = prefs.getString("playerId", "");
        if (!token.isEmpty() && !id.isEmpty()) credentials = new PlayerCredentials(id, token);
        try {
            JsonValue list = new JsonReader().parse(prefs.getString("outbox", "[]"));
            for (JsonValue item : list) { RunSubmission run = parseRun(item); outbox.put(run.runId(), run); }
            String latest = prefs.getString("latestClear", "");
            if (!latest.isEmpty()) latestClear = parseRun(new JsonReader().parse(latest));
            String cache = prefs.getString("achievements", "");
            if (!cache.isEmpty()) achievements = parseAchievements(new JsonReader().parse(cache));
        } catch (RuntimeException e) {
            lastError = "저장된 온라인 기록을 읽지 못했습니다. 설정 파일을 확인해 주세요.";
            // Keep the source preference intact rather than erasing a damaged outbox.
        }
    }

    private static String configuredUrl() {
        String url = System.getProperty("deadlight.serverUrl");
        if (url == null || url.isBlank()) url = System.getenv("DEADLIGHT_SERVER_URL");
        if (url == null || url.isBlank()) url = "http://127.0.0.1:8080";
        url = url.trim().replaceAll("/+$", "");
        URI uri = URI.create(url);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null)
            throw new IllegalArgumentException("DEADLIGHT_SERVER_URL must be an HTTP(S) base URL");
        return url;
    }

    String playerId() { return credentials == null ? "" : credentials.playerId(); }
    String initials() { return prefs.getString("initials", "AAA"); }
    int pendingCount() { return outbox.size(); }
    boolean hasPending(String runId) { return outbox.containsKey(runId); }

    boolean completed(RunResult result) {
        if (!result.cleared() || result.seconds() <= 0 || !Float.isFinite(result.seconds())
                || result.seconds() > 43200 || Boolean.getBoolean("deadlight.debug")) return false;
        latestClear = new RunSubmission(UUID.randomUUID().toString(), initials(), GameBalance.GAMEPLAY_VERSION,
                result.startStage(), result.stage(), true, Math.max(100, Math.round(result.seconds() * 10d) * 100),
                result.damageTaken(), result.potionsUsed(), result.retries(), false, false);
        prefs.putString("latestClear", encode(latestClear));
        enqueue(latestClear); // Achievements sync even when public ranking registration is skipped.
        return true;
    }

    void publish(RunSubmission run, String initials) {
        if (run == null || !initials.matches("[A-Z]{3}")) return;
        prefs.putString("initials", initials);
        enqueue(run.publishAs(initials));
        retryNow();
    }

    private void enqueue(RunSubmission run) {
        RunSubmission existing = outbox.get(run.runId());
        if (existing != null && existing.publish() && !run.publish()) return;
        outbox.put(run.runId(), run); persistOutbox();
    }

    void retryNow() { retryDelay = 0; lastError = ""; }
    void update(float dt, boolean showNotifications) {
        retryDelay = Math.max(0, retryDelay - Math.max(0, dt));
        if (showNotifications) toastSeconds = Math.max(0, toastSeconds - dt);
        if (!closed && !sending && !outbox.isEmpty() && retryDelay == 0) {
            sending = true;
            RunSubmission run = outbox.values().iterator().next();
            ensureIdentity(() -> request("POST", "/api/runs", encode(run), true, json -> {
                List<Achievement> received = parseAchievements(json.get("achievements"));
                List<String> names = new ArrayList<>();
                for (JsonValue value : json.get("newlyUnlocked")) {
                    try { names.add(Goal.valueOf(value.asString()).title); } catch (IllegalArgumentException ignored) { }
                }
                boolean published = json.getBoolean("published");
                if (run.publish() && !published) throw new IllegalStateException("Unconfirmed publication");
                sending = false;
                if (run.equals(outbox.get(run.runId()))) outbox.remove(run.runId());
                persistOutbox(); lastError = "";
                achievements = received;
                prefs.putString("achievements", json.get("achievements").toJson(JsonWriter.OutputType.json)).flush();
                if (!names.isEmpty()) { toast = "도전과제 달성 · " + names.get(0) + (names.size() > 1 ? " 외 " + (names.size()-1) + "개" : ""); toastSeconds = 5; }
                if (run.publish() && published) {
                    if (latestClear != null && latestClear.runId().equals(run.runId())) {
                        latestClear = run; prefs.putString("latestClear", encode(run)).flush();
                    }
                    lastPublishedRunId = run.runId(); loadBoard(run.startStage());
                }
            }, (status, message) -> uploadFailed(run, status, message)),
                    (status, message) -> uploadFailed(run, status, message));
        }
    }

    private void uploadFailed(RunSubmission run, int status, String message) {
        sending = false; retryDelay = 30; lastError = message; errorSequence++;
        // Invalid content cannot become valid by retrying, but a connection/auth failure stays queued.
        if (status == 400 || status == 409) {
            if (run.equals(outbox.get(run.runId()))) outbox.remove(run.runId());
            persistOutbox();
        }
    }

    void loadBoard(int stage) {
        boardStage = stage; boardLoading = true; boardError = "";
        long sequence = ++boardRequest;
        ensureIdentity(() -> request("GET", "/api/leaderboard?rulesVersion=" + GameBalance.GAMEPLAY_VERSION + "&startStage=" + stage + "&limit=10", null, true, json -> {
            if (sequence != boardRequest) return;
            List<Entry> entries = new ArrayList<>();
            for (JsonValue row : json.get("entries")) entries.add(parseEntry(row));
            JsonValue personal = json.get("personalBest");
            board = new Board(List.copyOf(entries), personal == null || personal.isNull() ? null : parseEntry(personal));
            boardLoading = false;
        }, (status, message) -> boardFailed(sequence, message)), (status, message) -> boardFailed(sequence, message));
    }
    private void boardFailed(long sequence, String message) {
        if (sequence == boardRequest) { boardLoading = false; boardError = message; }
    }

    void loadAchievements() {
        achievementsLoading = true; achievementError = "";
        ensureIdentity(() -> request("GET", "/api/achievements", null, true, json -> {
            achievements = parseAchievements(json); achievementsLoading = false;
            prefs.putString("achievements", json.toJson(JsonWriter.OutputType.json)).flush();
        }, (status, message) -> { achievementsLoading = false; achievementError = message; }),
                (status, message) -> { achievementsLoading = false; achievementError = message; });
    }

    private void ensureIdentity(Runnable action, BiConsumer<Integer,String> failure) {
        if (credentials != null) { action.run(); return; }
        waitingForIdentity.add(action); identityFailures.add(failure);
        if (registering) return;
        registering = true;
        request("POST", "/api/players", "{}", false, json -> {
            credentials = new PlayerCredentials(json.getString("playerId"), json.getString("accessToken"));
            prefs.putString("playerId", credentials.playerId()).putString("token", credentials.accessToken()).flush();
            registering = false;
            List<Runnable> actions = List.copyOf(waitingForIdentity);
            waitingForIdentity.clear(); identityFailures.clear();
            for (Runnable next : actions) next.run();
        }, (status, message) -> {
            registering = false;
            List<BiConsumer<Integer,String>> errors = List.copyOf(identityFailures);
            waitingForIdentity.clear(); identityFailures.clear();
            for (BiConsumer<Integer,String> error : errors) error.accept(status, message);
        });
    }

    private void request(String method, String path, String body, boolean authenticate,
            Consumer<JsonValue> success, BiConsumer<Integer,String> failure) {
        request(method,path,body,authenticate,success,failure,true);
    }

    private void recoverIdentity(Runnable retry, BiConsumer<Integer,String> failure) {
        recoveryActions.add(retry); recoveryFailures.add(failure);
        if (recovering) return;
        recovering = true;
        StringWriter out = new StringWriter();
        try {
            JsonWriter writer = new JsonWriter(out); writer.setOutputType(JsonWriter.OutputType.json);
            writer.object().set("playerId",credentials.playerId()).set("accessToken",credentials.accessToken()).pop().close();
        } catch (IOException e) { throw new IllegalStateException(e); }
        request("POST","/api/players/recover",out.toString(),false,json -> {
            if (!credentials.playerId().equals(json.getString("playerId"))
                    || !credentials.accessToken().equals(json.getString("accessToken")))
                throw new IllegalStateException("Recovery identity mismatch");
            recovering=false;
            List<Runnable> actions=List.copyOf(recoveryActions);
            recoveryActions.clear(); recoveryFailures.clear();
            for(Runnable action:actions) action.run();
        },(status,message) -> {
            recovering=false;
            List<BiConsumer<Integer,String>> failures=List.copyOf(recoveryFailures);
            recoveryActions.clear(); recoveryFailures.clear();
            for(var callback:failures) callback.accept(status==400||status==409?401:status,
                    "플레이어 인증을 복구하지 못했습니다. 서버 버전과 기존 DB를 확인해 주세요.");
        },false);
    }

    private void request(String method, String path, String body, boolean authenticate,
            Consumer<JsonValue> success, BiConsumer<Integer,String> failure, boolean allowRecovery) {
        Net.HttpRequest req = new Net.HttpRequest(method);
        req.setUrl(baseUrl + path); req.setTimeOut(5000); req.setHeader("Accept", "application/json");
        if (authenticate) req.setHeader("Authorization", "Bearer " + credentials.accessToken());
        if (body != null) { req.setHeader("Content-Type", "application/json; charset=UTF-8"); req.setContent(body); }
        requests.add(req);
        try {
            Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
                @Override public void handleHttpResponse(Net.HttpResponse response) {
                    int status = response.getStatus().getStatusCode();
                    String text = response.getResultAsString();
                    Gdx.app.postRunnable(() -> {
                        requests.remove(req); if (closed) return;
                        try {
                            JsonValue json = new JsonReader().parse(text);
                            if (status >= 200 && status < 300) success.accept(json);
                            else if(status==401 && authenticate && allowRecovery)
                                recoverIdentity(() -> request(method,path,body,true,success,failure,false),failure);
                            else failure.accept(status, json.getString("message", "기록 요청을 처리하지 못했습니다."));
                        } catch (RuntimeException e) { failure.accept(status, "서버 응답을 읽지 못했습니다. 잠시 후 다시 시도해 주세요."); }
                    });
                }
                @Override public void failed(Throwable error) { fail("기록 서버에 연결하지 못했습니다. 서버 실행과 연결을 확인해 주세요."); }
                @Override public void cancelled() { fail("기록 요청이 취소되었습니다. 다시 시도해 주세요."); }
                private void fail(String message) { Gdx.app.postRunnable(() -> { requests.remove(req); if (!closed) failure.accept(0, message); }); }
            });
        } catch (RuntimeException e) { requests.remove(req); failure.accept(0, "기록 서버에 연결하지 못했습니다."); }
    }

    private void persistOutbox() {
        prefs.putString("outbox", "[" + String.join(",", outbox.values().stream().map(OnlineService::encode).toList()) + "]").flush();
    }
    static String encode(RunSubmission r) {
        try {
            StringWriter out = new StringWriter();
            JsonWriter writer = new JsonWriter(out);
            writer.setOutputType(JsonWriter.OutputType.json);
            writer.object().set("runId",r.runId()).set("initials",r.initials()).set("rulesVersion",r.rulesVersion())
                    .set("startStage",r.startStage()).set("reachedStage",r.reachedStage()).set("cleared",r.cleared())
                    .set("durationMillis",r.durationMillis()).set("damageTaken",r.damageTaken()).set("potionsUsed",r.potionsUsed())
                    .set("retries",r.retries()).set("assisted",r.assisted()).set("publish",r.publish()).pop().close();
            return out.toString();
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    private static RunSubmission parseRun(JsonValue j) {
        return new RunSubmission(j.getString("runId"),j.getString("initials"),j.getInt("rulesVersion"),j.getInt("startStage"),
                j.getInt("reachedStage"),j.getBoolean("cleared"),j.getLong("durationMillis"),j.getInt("damageTaken"),
                j.getInt("potionsUsed"),j.getInt("retries"),j.getBoolean("assisted",false),j.getBoolean("publish"));
    }
    private static Entry parseEntry(JsonValue j) {
        return new Entry(j.getInt("rank"),j.getString("playerId"),j.getString("initials"),j.getLong("durationMillis"),j.getInt("damageTaken"));
    }
    private static List<Achievement> parseAchievements(JsonValue values) {
        List<Achievement> result = new ArrayList<>();
        for (JsonValue j : values) result.add(new Achievement(j.getString("id"),j.getString("title"),j.getString("description"),
                j.getBoolean("unlocked"),j.getString("unlockedAt",null)));
        return List.copyOf(result);
    }
    private static List<Achievement> lockedAchievements() {
        List<Achievement> out = new ArrayList<>();
        for (Goal goal : Goal.values()) out.add(new Achievement(goal.name(),goal.title,goal.description,false,null));
        return List.copyOf(out);
    }
    boolean toastVisible() { return toastSeconds > 0; }
    @Override public void close() {
        closed = true;
        for (Net.HttpRequest request : List.copyOf(requests)) Gdx.net.cancelHttpRequest(request);
        requests.clear(); waitingForIdentity.clear(); identityFailures.clear();
        recoveryActions.clear(); recoveryFailures.clear();
    }
}
