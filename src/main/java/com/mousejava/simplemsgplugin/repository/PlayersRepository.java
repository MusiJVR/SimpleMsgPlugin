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
