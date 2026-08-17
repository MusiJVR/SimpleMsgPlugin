package com.mousejava.simplemsgplugin.database;

import com.mousejava.simplemsgplugin.repository.PlayersRepository;
import com.mousejava.simplemsgplugin.utils.Scheduler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class DatabaseCacheManager {
    private final PlayersRepository players;
    private final AtomicReference<List<String>> playerNames = new AtomicReference<>(List.of());
    private Scheduler.Task refreshTask;

    public DatabaseCacheManager(PlayersRepository players) {
        this.players = players;
    }

    public void refreshPlayerNames() {
        playerNames.set(List.copyOf(players.findAllNames()));
    }

    public List<String> getPlayerNames() {
        return playerNames.get();
    }

    public void schedulePlayerNameRefresh(long periodTicks) {
        if (refreshTask != null)
            refreshTask.cancel();

        refreshTask = Scheduler.runTimer(this::refreshPlayerNames, 1, periodTicks);
    }

    public void close() {
        if (refreshTask != null)
            refreshTask.cancel();

        playerNames.set(List.of());
    }
}
