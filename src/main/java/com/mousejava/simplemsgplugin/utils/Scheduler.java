package com.mousejava.simplemsgplugin.utils;

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public final class Scheduler {
    @SuppressWarnings("UnstableApiUsage")
    private static final boolean IS_FOLIA = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private static JavaPlugin plugin;

    public static void init(JavaPlugin plugin) {
        if (Scheduler.plugin != null)
            throw new IllegalStateException("Scheduler is already initialized!");

        Scheduler.plugin = plugin;
    }

    private static JavaPlugin getPlugin() {
        if (plugin == null)
            throw new IllegalStateException("Scheduler has not been initialized! Call Scheduler.init(plugin) first.");

        return plugin;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void run(Runnable runnable) {
        if (IS_FOLIA)
            Bukkit.getGlobalRegionScheduler().execute(getPlugin(), runnable);
        else
            Bukkit.getScheduler().runTask(getPlugin(), runnable);
    }

    public static Task runLater(Runnable runnable, long delay) {
        if (IS_FOLIA)
            return new Task(Bukkit.getGlobalRegionScheduler().runDelayed(getPlugin(), t -> runnable.run(), normalizeDelay(delay)));
        else
            return new Task(Bukkit.getScheduler().runTaskLater(getPlugin(), runnable, normalizeDelay(delay)));
    }

    public static Task runTimer(Runnable runnable, long delay, long period) {
        if (IS_FOLIA)
            return new Task(Bukkit.getGlobalRegionScheduler().runAtFixedRate(getPlugin(), t -> runnable.run(), normalizeDelay(delay), normalizePeriod(period)));
        else
            return new Task(Bukkit.getScheduler().runTaskTimer(getPlugin(), runnable, normalizeDelay(delay), normalizePeriod(period)));
    }

    public static void runAsync(Runnable runnable) {
        if (IS_FOLIA)
            Bukkit.getAsyncScheduler().runNow(getPlugin(), t -> runnable.run());
        else
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), runnable);
    }

    public static Task runAsyncLater(Runnable runnable, long delay) {
        if (IS_FOLIA)
            return new Task(Bukkit.getAsyncScheduler().runDelayed(getPlugin(), t -> runnable.run(), normalizeDelay(delay) * 50L, TimeUnit.MILLISECONDS));
        else
            return new Task(Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), runnable, normalizeDelay(delay)));
    }

    public static Task runAsyncTimer(Runnable runnable, long delay, long period) {
        if (IS_FOLIA)
            return new Task(Bukkit.getAsyncScheduler().runAtFixedRate(getPlugin(), t -> runnable.run(), normalizeDelay(delay) * 50L, normalizePeriod(period) * 50L, TimeUnit.MILLISECONDS));
        else
            return new Task(Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), runnable, normalizeDelay(delay), normalizePeriod(period)));
    }

    public static void runForEntity(Entity entity, Runnable runnable) {
        entity.getScheduler().run(getPlugin(), t -> runnable.run(), null);
    }

    public static Task runForEntityLater(Entity entity, Runnable runnable, long delay) {
        return new Task(entity.getScheduler().runDelayed(getPlugin(), t -> runnable.run(), null, normalizeDelay(delay)));
    }

    public static Task runForEntityTimer(Entity entity, Runnable runnable, long delay, long period) {
        return new Task(entity.getScheduler().runAtFixedRate(getPlugin(), t -> runnable.run(), null, normalizeDelay(delay), normalizePeriod(period)));
    }

    public static void runAtLocation(Location location, Runnable runnable) {
        Bukkit.getRegionScheduler().execute(getPlugin(), location, runnable);
    }

    public static void runAtLocation(World world, int chunkX, int chunkZ, Runnable runnable) {
        Bukkit.getRegionScheduler().execute(getPlugin(), world, chunkX, chunkZ, runnable);
    }

    public static Task runAtLocationLater(Location location, Runnable runnable, long delay) {
        return new Task(Bukkit.getRegionScheduler().runDelayed(getPlugin(), location, t -> runnable.run(), normalizeDelay(delay)));
    }

    public static Task runAtLocationLater(World world, int chunkX, int chunkZ, Runnable runnable, long delay) {
        return new Task(Bukkit.getRegionScheduler().runDelayed(getPlugin(), world, chunkX, chunkZ, t -> runnable.run(), normalizeDelay(delay)));
    }

    public static Task runAtLocationTimer(Location location, Runnable runnable, long delay, long period) {
        return new Task(Bukkit.getRegionScheduler().runAtFixedRate(getPlugin(), location, t -> runnable.run(), normalizeDelay(delay), normalizePeriod(period)));
    }

    public static Task runAtLocationTimer(World world, int chunkX, int chunkZ, Runnable runnable, long delay, long period) {
        return new Task(Bukkit.getRegionScheduler().runAtFixedRate(getPlugin(), world, chunkX, chunkZ, t -> runnable.run(), normalizeDelay(delay), normalizePeriod(period)));
    }

    private static long normalizeDelay(long delay) {
        return normalize(0, delay);
    }

    private static long normalizePeriod(long period) {
        return normalize(1, period);
    }

    private static long normalize(long min, long value) {
        return Math.max(min, value);
    }

    public static final class Task {
        private final BukkitTask bukkitTask;
        private final ScheduledTask foliaTask;

        public Task(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
            this.foliaTask = null;
        }

        public Task(ScheduledTask foliaTask) {
            this.bukkitTask = null;
            this.foliaTask = foliaTask;
        }

        public boolean isCancelled() {
            if (bukkitTask != null)
                return bukkitTask.isCancelled();

            if (foliaTask != null)
                return foliaTask.isCancelled();

            return true;
        }

        public void cancel() {
            if (bukkitTask != null)
                bukkitTask.cancel();

            if (foliaTask != null)
                foliaTask.cancel();
        }
    }
}
