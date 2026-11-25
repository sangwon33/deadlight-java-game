CREATE TABLE IF NOT EXISTS players (
    id VARCHAR(36) PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at BIGINT NOT NULL
);
CREATE TABLE IF NOT EXISTS game_runs (
    id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(36) NOT NULL,
    initials VARCHAR(3) NOT NULL,
    rules_version INT NOT NULL,
    start_stage INT NOT NULL,
    duration_ms BIGINT NOT NULL,
    damage_taken INT NOT NULL,
    potions_used INT NOT NULL,
    retries INT NOT NULL,
    published BOOLEAN NOT NULL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);
CREATE TABLE IF NOT EXISTS player_achievements (
    player_id VARCHAR(36) NOT NULL,
    achievement_id VARCHAR(40) NOT NULL,
    unlocked_at BIGINT NOT NULL,
    PRIMARY KEY (player_id, achievement_id),
    FOREIGN KEY (player_id) REFERENCES players(id)
);
