package com.mousejava.simplemsgplugin.database.repository;

import com.mousejava.simplemsgplugin.database.api.DatabaseManager;
import com.mousejava.simplemsgplugin.database.api.SchemaRepository;
import com.mousejava.simplemsgplugin.database.dialect.SqlDialect;

import java.util.Optional;
import java.util.UUID;

public final class SkinsRepository implements SchemaRepository {
    private final DatabaseManager<SqlDialect> database;

    public SkinsRepository(DatabaseManager<SqlDialect> database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.dialect().skinsSchema().forEach(database::execute);
    }

    public void upsert(UUID uuid, String skinBase64) {
        if (skinBase64 == null) return;
        database.execute(database.dialect().skinsUpsert(),
                uuid.toString(), skinBase64
        );
    }

    public Optional<String> findSkin(UUID uuid) {
        return database.queryOne(database.dialect().skinsFindSkin(),
                rs -> rs.getString("skin_base64"), uuid.toString()
        );
    }
}
