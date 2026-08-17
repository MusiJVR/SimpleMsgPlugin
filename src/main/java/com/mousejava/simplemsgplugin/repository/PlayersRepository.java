package com.mousejava.simplemsgplugin.repository;

import com.mousejava.simplemsgplugin.database.DatabaseManager;
import com.mousejava.simplemsgplugin.database.SchemaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PlayersRepository implements SchemaRepository {
    private final DatabaseManager database;

    public PlayersRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid CHAR(36) NOT NULL,
                    nickname VARCHAR(16) NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (uuid),
                    UNIQUE KEY uk_players_nickname (nickname)
                )
                """);
    }

    public void upsert(UUID uuid, String nickname) {
        database.execute("""
                        INSERT INTO players (uuid, nickname) VALUES (?, ?)
                        ON DUPLICATE KEY UPDATE nickname = VALUES(nickname)
                        """,
                uuid.toString(), nickname
        );
    }

    public Optional<String> findUuidByName(String nickname) {
        return database.queryOne("SELECT uuid FROM players WHERE LOWER(nickname) = LOWER(?)",
                rs -> rs.getString("uuid"), nickname
        );
    }

    public Optional<String> findName(UUID uuid) {
        return database.queryOne("SELECT nickname FROM players WHERE uuid = ?",
                rs -> rs.getString("nickname"), uuid.toString()
        );
    }

    public List<String> findAllNames() {
        return database.query("SELECT nickname FROM players ORDER BY nickname",
                rs -> rs.getString("nickname")
        );
    }
}
