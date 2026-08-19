package com.mousejava.simplemsgplugin.service;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BStatsMetricsService {
    public static void init(JavaPlugin plugin, int serviceId) {
        Metrics metrics = new Metrics(plugin, serviceId);
    }
}
