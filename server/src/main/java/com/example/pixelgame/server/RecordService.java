package com.example.pixelgame.server;

import static com.example.pixelgame.online.OnlineContract.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecordService {
    private final JdbcTemplate db;
    private final TransactionTemplate transaction;
    private final SecureRandom random = new SecureRandom();
    private static final String RANKED = """
        WITH best AS (
            SELECT id, player_id, initials, duration_ms, damage_taken, created_at,
                ROW_NUMBER() OVER (PARTITION BY player_id
                    ORDER BY duration_ms, damage_taken, created_at, id) AS personal_row
            FROM game_runs WHERE rules_version = ? AND start_stage = ? AND published = TRUE
        ), ranked AS (
            SELECT player_id, initials, duration_ms, damage_taken,
                ROW_NUMBER() OVER (ORDER BY duration_ms, damage_taken, created_at, id) AS place
            FROM best WHERE personal_row = 1
        )
        """;

    public RecordService(JdbcTemplate db, PlatformTransactionManager manager) {
        this.db = db; transaction = new TransactionTemplate(manager);
    }

    public PlayerCredentials register() {
        String id = UUID.randomUUID().toString();
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        db.update("INSERT INTO players(id, token_hash, created_at) VALUES (?, ?, ?)", id, hash(token), System.currentTimeMillis());
        return new PlayerCredentials(id, token);
    }

    public String authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw unauthorized();
        String token = authorization.substring(7);
        if (token.length() != 43) throw unauthorized();
        List<String> ids = db.query("SELECT id FROM players WHERE token_hash = ?", (rs, row) -> rs.getString(1), hash(token));
        if (ids.isEmpty()) throw unauthorized();
        return ids.get(0);
    }

    public SubmissionResult submit(String playerId, RunSubmission run) {
        validate(run);
        return transaction.execute(status -> {
            // Serializes retries and simultaneous requests for the same player on both H2 and MySQL.
            db.queryForObject("SELECT id FROM players WHERE id = ? FOR UPDATE", String.class, playerId);
            List<Map<String,Object>> previous = db.queryForList("SELECT * FROM game_runs WHERE id = ?", run.runId());
            boolean duplicate = !previous.isEmpty();
            boolean published = run.publish();
            if (duplicate) {
                Map<String,Object> old = previous.get(0);
                if (!playerId.equals(old.get("player_id"))
                        || number(old,"rules_version") != run.rulesVersion() || number(old,"start_stage") != run.startStage()
                        || number(old,"duration_ms") != run.durationMillis() || number(old,"damage_taken") != run.damageTaken()
                        || number(old,"potions_used") != run.potionsUsed() || number(old,"retries") != run.retries()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 제출한 기록의 내용이 다릅니다.");
                }
                boolean wasPublished = (Boolean) old.get("published");
                if (run.publish() && !wasPublished) db.update("UPDATE game_runs SET published = TRUE, initials = ? WHERE id = ?", run.initials(), run.runId());
                published |= wasPublished;
            } else {
                db.update("INSERT INTO game_runs(id,player_id,initials,rules_version,start_stage,duration_ms,damage_taken,potions_used,retries,published,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        run.runId(), playerId, run.initials(), run.rulesVersion(), run.startStage(), run.durationMillis(),
                        run.damageTaken(), run.potionsUsed(), run.retries(), run.publish(), System.currentTimeMillis());
            }
            List<String> unlocked = new ArrayList<>();
            for (Goal goal : Goal.values()) if (goal.earnedBy(run)) {
                Integer count = db.queryForObject("SELECT COUNT(*) FROM player_achievements WHERE player_id = ? AND achievement_id = ?", Integer.class, playerId, goal.name());
                if (count == 0) {
                    db.update("INSERT INTO player_achievements(player_id,achievement_id,unlocked_at) VALUES (?,?,?)", playerId, goal.name(), System.currentTimeMillis());
                    unlocked.add(goal.name());
                }
            }
            return new SubmissionResult(published, duplicate, achievements(playerId), List.copyOf(unlocked));
        });
    }

    public List<Achievement> achievements(String playerId) {
        Map<String,Long> dates = new java.util.HashMap<>();
        db.query("SELECT achievement_id,unlocked_at FROM player_achievements WHERE player_id = ?", rs -> {
            dates.put(rs.getString(1), rs.getLong(2));
        }, playerId);
        List<Achievement> out = new ArrayList<>();
        for (Goal goal : Goal.values()) {
            Long date = dates.get(goal.name());
            out.add(new Achievement(goal.name(), goal.title, goal.description, date != null,
                    date == null ? null : Instant.ofEpochMilli(date).toString()));
        }
        return List.copyOf(out);
    }

    public Board leaderboard(String playerId, int rulesVersion, int startStage, int limit) {
        if (rulesVersion < 1 || rulesVersion > 1000 || startStage < 1 || startStage > 4 || limit < 1 || limit > 10)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 랭킹 조회 조건입니다.");
        List<Entry> top = db.query(RANKED + "SELECT * FROM ranked ORDER BY place LIMIT ?", (rs, i) ->
                new Entry(rs.getInt("place"), rs.getString("player_id"), rs.getString("initials"), rs.getLong("duration_ms"), rs.getInt("damage_taken")), rulesVersion, startStage, limit);
        List<Entry> own = playerId == null ? List.of() : db.query(RANKED + "SELECT * FROM ranked WHERE player_id = ?", (rs, i) ->
                new Entry(rs.getInt("place"), rs.getString("player_id"), rs.getString("initials"), rs.getLong("duration_ms"), rs.getInt("damage_taken")), rulesVersion, startStage, playerId);
        return new Board(List.copyOf(top), own.isEmpty() ? null : own.get(0));
    }

    private static void validate(RunSubmission r) {
        if (r == null || r.runId() == null || r.initials() == null || !r.initials().matches("[A-Z]{3}")) bad();
        try { if (!UUID.fromString(r.runId()).toString().equals(r.runId())) bad(); }
        catch (IllegalArgumentException e) { bad(); }
        if (!r.cleared() || r.reachedStage() != 4 || r.startStage() < 1 || r.startStage() > 4 || r.assisted()
                || r.rulesVersion() < 1 || r.rulesVersion() > 1000
                || r.durationMillis() <= 0 || r.durationMillis() > 43_200_000L || r.durationMillis() % 100 != 0
                || r.damageTaken() < 0 || r.damageTaken() > 1_000_000 || r.potionsUsed() < 0 || r.potionsUsed() > 100_000
                || r.retries() < 0 || r.retries() > 10_000) bad();
    }
    private static void bad() { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등록할 수 없는 플레이 기록입니다."); }
    private static ResponseStatusException unauthorized() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "플레이어 인증을 확인해 주세요."); }
    private static long number(Map<String,Object> row,String key) { return ((Number) row.get(key)).longValue(); }
    private static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
