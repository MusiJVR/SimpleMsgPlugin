package com.mousejava.simplemsgplugin.repository;

import com.mousejava.simplemsgplugin.database.DatabaseManager;
import com.mousejava.simplemsgplugin.database.SchemaRepository;

import java.util.List;
import java.util.UUID;

public final class BlacklistRepository implements SchemaRepository {
    private final DatabaseManager database;

    public BlacklistRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.execute("""
                CREATE TABLE IF NOT EXISTS blacklist (
                    owner_uuid CHAR(36) NOT NULL,
                    blocked_uuid CHAR(36) NOT NULL,
                    blocked_name VARCHAR(16) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (owner_uuid, blocked_uuid),
                    CONSTRAINT fk_blacklist_owner FOREIGN KEY (owner_uuid) REFERENCES players(uuid) ON DELETE CASCADE,
                    CONSTRAINT fk_blacklist_blocked FOREIGN KEY (blocked_uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
                """);
    }

    public boolean isBlocked(UUID owner, UUID blocked) {
        return database.queryOne("SELECT 1 FROM blacklist WHERE owner_uuid = ? AND blocked_uuid = ?",
                rs -> true, owner.toString(), blocked.toString()
        ).orElse(false);
    }

    public boolean isBlockedBy(UUID blocked, UUID owner) {
        return isBlocked(owner, blocked);
    }

    public void add(UUID owner, UUID blocked, String name) {
        database.execute("""
                        INSERT INTO blacklist (owner_uuid, blocked_uuid, blocked_name) VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE blocked_name = VALUES(blocked_name)
                        """,
                owner.toString(), blocked.toString(), name
        );
    }

    public void remove(UUID owner, UUID blocked) {
        database.execute("DELETE FROM blacklist WHERE owner_uuid = ? AND blocked_uuid = ?",
                owner.toString(), blocked.toString()
        );
    }

    public List<String> listNames(UUID owner) {
        return database.query("SELECT blocked_name FROM blacklist WHERE owner_uuid = ? ORDER BY blocked_name",
                rs -> rs.getString("blocked_name"), owner.toString()
        );
    }
}
