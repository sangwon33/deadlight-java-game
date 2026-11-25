package com.example.pixelgame.online;

import java.util.List;

/** Versioned HTTP payloads shared by the game and the standalone record server. */
public final class OnlineContract {
    private OnlineContract() {}

    public record PlayerCredentials(String playerId, String accessToken) {}
    public record RunSubmission(String runId, String initials, int rulesVersion, int startStage,
            int reachedStage, boolean cleared, long durationMillis, int damageTaken,
            int potionsUsed, int retries, boolean assisted, boolean publish) {
        public RunSubmission publishAs(String name) {
            return new RunSubmission(runId, name, rulesVersion, startStage, reachedStage, cleared,
                    durationMillis, damageTaken, potionsUsed, retries, assisted, true);
        }
    }
    public record Entry(int rank, String playerId, String initials, long durationMillis, int damageTaken) {}
    public record Board(List<Entry> entries, Entry personalBest) {}
    public record Achievement(String id, String title, String description, boolean unlocked, String unlockedAt) {}
    public record SubmissionResult(boolean published, boolean duplicate,
            List<Achievement> achievements, List<String> newlyUnlocked) {}
    public record ApiError(String message) {}

    public enum Goal {
        FIRST_CLEAR("첫 여정의 끝", "전체 모험을 처음으로 완료하세요."),
        NO_RETRY("꺾이지 않는 여정", "재도전 없이 전체 모험을 완료하세요."),
        NO_POTION("물약 없는 순례", "포션을 사용하지 않고 전체 모험을 완료하세요."),
        NO_DAMAGE("닿지 않는 그림자", "받은 피해 0으로 전체 모험을 완료하세요.");

        public final String title, description;
        Goal(String title, String description) { this.title = title; this.description = description; }
        public boolean earnedBy(RunSubmission run) {
            if (!run.cleared() || run.startStage() != 1 || run.reachedStage() != 4 || run.assisted()) return false;
            return switch (this) {
                case FIRST_CLEAR -> true;
                case NO_RETRY -> run.retries() == 0;
                case NO_POTION -> run.potionsUsed() == 0;
                case NO_DAMAGE -> run.damageTaken() == 0;
            };
        }
    }
}
