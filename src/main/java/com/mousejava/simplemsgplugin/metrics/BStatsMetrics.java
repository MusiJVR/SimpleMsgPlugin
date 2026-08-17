package com.mousejava.simplemsgplugin.metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BStatsMetrics {
    public static void init(JavaPlugin plugin, int serviceId) {
        Metrics metrics = new Metrics(plugin, serviceId);
    }
}
