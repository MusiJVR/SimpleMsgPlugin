package com.mousejava.simplemsgplugin.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Scheduler {
    private static final boolean isFolia;
    private static JavaPlugin plugin;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        isFolia = folia;
    }

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
        return isFolia;
    }

    public static void run(Runnable runnable) {
        if (isFolia)
            Bukkit.getGlobalRegionScheduler().execute(getPlugin(), runnable);
        else
            Bukkit.getScheduler().runTask(getPlugin(), runnable);
    }

    public static Task runLater(Runnable runnable, long delay) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler().runDelayed(getPlugin(), t -> runnable.run(), delay));
        else
            return new Task(Bukkit.getScheduler().runTaskLater(getPlugin(), runnable, delay));
    }

    public static Task runTimer(Runnable runnable, long delay, long period) {
        if (isFolia)
            return new Task(Bukkit.getGlobalRegionScheduler().runAtFixedRate(getPlugin(), t -> runnable.run(), delay < 1 ? 1 : delay, period));
        else
            return new Task(Bukkit.getScheduler().runTaskTimer(getPlugin(), runnable, delay, period));
    }

    public static void runAsync(Runnable runnable) {
        if (isFolia)
            Bukkit.getAsyncScheduler().runNow(getPlugin(), t -> runnable.run());
        else
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), runnable);
    }

    public static void runForEntity(Entity entity, Runnable runnable) {
        if (isFolia)
            entity.getScheduler().run(getPlugin(), t -> runnable.run(), null);
        else
            entity.getScheduler().run(getPlugin(), t -> runnable.run(), null);
    }

    public static Task runForEntityLater(Entity entity, Runnable runnable, long delay) {
        if (isFolia)
            return new Task(entity.getScheduler().runDelayed(getPlugin(), t -> runnable.run(), null, delay));
        else
            return new Task(entity.getScheduler().runDelayed(getPlugin(), t -> runnable.run(), null, delay));
    }

    public static Task runForEntityTimer(Entity entity, Runnable runnable, long delay, long period) {
        if (isFolia)
            return new Task(entity.getScheduler().runAtFixedRate(getPlugin(), t -> runnable.run(), null, delay < 1 ? 1 : delay, period));
        else
            return new Task(entity.getScheduler().runAtFixedRate(getPlugin(), t -> runnable.run(), null, delay < 1 ? 1 : delay, period));
    }

    public static class Task {
        private Object foliaTask;
        private BukkitTask bukkitTask;

        public Task(Object foliaTask) {
            this.foliaTask = foliaTask;
        }

        public Task(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }

        public void cancel() {
            if (foliaTask != null)
                ((ScheduledTask) foliaTask).cancel();
            else
                bukkitTask.cancel();
        }
    }
}
