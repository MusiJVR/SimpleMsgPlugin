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
        database.dialect().blacklistSchema().forEach(database::execute);
    }

    public boolean isBlocked(UUID owner, UUID blocked) {
        return database.queryOne(database.dialect().blacklistIsBlocked(),
                rs -> true, owner.toString(), blocked.toString()
        ).orElse(false);
    }

    public boolean isBlockedBy(UUID blocked, UUID owner) {
        return isBlocked(owner, blocked);
    }

    public void add(UUID owner, UUID blocked, String name) {
        database.execute(database.dialect().blacklistUpsert(),
                owner.toString(), blocked.toString(), name
        );
    }

    public void remove(UUID owner, UUID blocked) {
        database.execute(database.dialect().blacklistRemove(),
                owner.toString(), blocked.toString()
        );
    }

    public List<String> listNames(UUID owner) {
        return database.query(database.dialect().blacklistListNames(),
                rs -> rs.getString("blocked_name"), owner.toString()
        );
    }
}
