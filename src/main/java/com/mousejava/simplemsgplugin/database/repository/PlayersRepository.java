package com.mousejava.simplemsgplugin.database.repository;

import com.mousejava.simplemsgplugin.database.api.DatabaseManager;
import com.mousejava.simplemsgplugin.database.api.SchemaRepository;
import com.mousejava.simplemsgplugin.database.dialect.SqlDialect;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PlayersRepository implements SchemaRepository {
    private final DatabaseManager<SqlDialect> database;

    public PlayersRepository(DatabaseManager<SqlDialect> database) {
        this.database = database;
    }

    @Override
    public void initializeSchema() {
        database.dialect().playersSchema().forEach(database::execute);
    }

    public void upsert(UUID uuid, String nickname) {
        database.execute(database.dialect().playersUpsert(),
                uuid.toString(), nickname
        );
    }

    public Optional<String> findUuidByName(String nickname) {
        return database.queryOne(database.dialect().playersFindUuidByName(),
                rs -> rs.getString("uuid"), nickname
        );
    }

    public Optional<String> findName(UUID uuid) {
        return database.queryOne(database.dialect().playersFindName(),
                rs -> rs.getString("nickname"), uuid.toString()
        );
    }

    public List<String> findAllNames() {
        return database.query(database.dialect().playersFindAllNames(),
                rs -> rs.getString("nickname")
        );
    }
}
