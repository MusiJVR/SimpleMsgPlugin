package com.mousejava.simplemsgplugin.repository;

import com.mousejava.simplemsgplugin.database.DatabaseManager;
import com.mousejava.simplemsgplugin.database.SchemaRepository;

import java.util.Optional;
import java.util.UUID;

public final class SkinsRepository implements SchemaRepository {
    private final DatabaseManager database;

    public SkinsRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS smp_player_skins (
                    player_uuid CHAR(36) NOT NULL,
                    skin_base64 TEXT,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (player_uuid),
                    CONSTRAINT fk_smp_player_skins_player FOREIGN KEY (player_uuid) REFERENCES smp_players(uuid) ON DELETE CASCADE
                )
                """);
    }

    public void upsert(UUID uuid, String skinBase64) {
        if (skinBase64 == null) return;
        database.execute("""
                INSERT INTO smp_player_skins (player_uuid, skin_base64) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE skin_base64 = VALUES(skin_base64)
                """,
                uuid.toString(), skinBase64
        );
    }

    public Optional<String> findSkin(UUID uuid) {
        return database.queryOne("SELECT skin_base64 FROM smp_player_skins WHERE player_uuid = ?",
                rs -> rs.getString("skin_base64"), uuid.toString()
        );
    }
}
