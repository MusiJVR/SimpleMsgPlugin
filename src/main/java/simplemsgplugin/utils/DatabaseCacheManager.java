package simplemsgplugin.utils;

import simplemsgplugin.scheduler.Scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseCacheManager {
    private final DatabaseDriver dbDriver;
    private final Map<String, List<Map<String, Object>>> caches = new ConcurrentHashMap<>();

    public DatabaseCacheManager(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    public void createCache(String cacheKey, String columns, String table, String condition) {
        refreshCache(cacheKey, columns, table, condition);
    }

    public void removeCache(String cacheKey) {
        caches.remove(cacheKey);
    }

    public void refreshCache(String cacheKey, String columns, String table, String condition) {
        dbDriver.selectData(columns, table, condition, rs -> {
            caches.put(cacheKey, rs);
        });
    }

    public List<Map<String, Object>> getCache(String cacheKey) {
        return caches.getOrDefault(cacheKey, Collections.emptyList());
    }

    public void scheduleAutoRefresh(String cacheKey, String columns, String table, String condition, long periodTicks) {
        Scheduler.runTimer(() -> refreshCache(cacheKey, columns, table, condition), periodTicks, periodTicks);
    }

    public void scheduleCacheRemoval(String cacheKey, long delayTicks) {
        Scheduler.runLater(() -> removeCache(cacheKey), delayTicks);
    }
}
