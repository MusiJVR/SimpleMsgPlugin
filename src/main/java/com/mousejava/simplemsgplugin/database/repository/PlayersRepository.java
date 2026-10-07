package com.mousejava.simplemsgplugin.database.repository;

import com.mousejava.simplemsgplugin.database.api.DatabaseManager;
import com.mousejava.simplemsgplugin.database.api.SchemaRepository;
import com.mousejava.simplemsgplugin.database.dialect.SqlDialect;
import com.mousejava.simplemsgplugin.utils.Scheduler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class PlayersRepository implements SchemaRepository {
    private final DatabaseManager<SqlDialect> database;
    private final AtomicReference<List<String>> playerNames = new AtomicReference<>(List.of());
    private Scheduler.Task refreshTask;

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
        return playerNames.get();
    }

    public void refreshPlayerNames() {
        playerNames.set(List.copyOf(database.query(database.dialect().playersFindAllNames(),
                rs -> rs.getString("nickname")
        )));
    }

    public void schedulePlayerNameRefresh(long periodTicks) {
        if (refreshTask != null)
            refreshTask.cancel();

        refreshTask = Scheduler.runAsyncTimer(this::refreshPlayerNames, 1, periodTicks);
    }

    public void close() {
        if (refreshTask != null)
            refreshTask.cancel();

        playerNames.set(List.of());
    }
}
