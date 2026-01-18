package simplemsgplugin.adapters;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import simplemsgplugin.SimpleMsgPlugin;

public final class Scheduler {
    private static final boolean isFolia;

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

    public static boolean isFolia() {
        return isFolia;
    }

    public static void run(Runnable runnable) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(SimpleMsgPlugin.getInstance(), runnable);
        } else {
            Bukkit.getScheduler().runTask(SimpleMsgPlugin.getInstance(), runnable);
        }
    }

    public static Task runLater(Runnable runnable, long delay) {
        if (isFolia) {
            return new Task(Bukkit.getGlobalRegionScheduler().runDelayed(SimpleMsgPlugin.getInstance(), t -> runnable.run(), delay));
        } else {
            return new Task(Bukkit.getScheduler().runTaskLater(SimpleMsgPlugin.getInstance(), runnable, delay));
        }
    }

    public static Task runTimer(Runnable runnable, long delay, long period) {
        if (isFolia) {
            return new Task(Bukkit.getGlobalRegionScheduler().runAtFixedRate(SimpleMsgPlugin.getInstance(), t -> runnable.run(), delay < 1 ? 1 : delay, period));
        } else {
            return new Task(Bukkit.getScheduler().runTaskTimer(SimpleMsgPlugin.getInstance(), runnable, delay, period));
        }
    }

    public static void runAsync(Runnable runnable) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(SimpleMsgPlugin.getInstance(), t -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(SimpleMsgPlugin.getInstance(), runnable);
        }
    }

    public static void runForPlayer(Player player, Runnable runnable) {
        if (isFolia) {
            player.getScheduler().run(SimpleMsgPlugin.getInstance(), t -> runnable.run(), null);
        } else {
            player.getScheduler().run(SimpleMsgPlugin.getInstance(), t -> runnable.run(), null);
        }
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
            if (foliaTask != null) {
                ((ScheduledTask) foliaTask).cancel();
            } else {
                bukkitTask.cancel();
            }
        }
    }
}
