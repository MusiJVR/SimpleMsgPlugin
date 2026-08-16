package com.mousejava.simplemsgplugin;

import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import com.mousejava.simplemsgplugin.command.api.CommandManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.mousejava.simplemsgplugin.handler.PlayerJoinQuitEventHandlers;
import com.mousejava.simplemsgplugin.handler.PrivateChatHandler;
import com.mousejava.simplemsgplugin.utils.DatabaseCacheManager;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleMsgPlugin extends JavaPlugin implements Listener {
    private static SimpleMsgPlugin instance;
    private DatabaseDriver dbDriver;
    private DatabaseCacheManager cacheManager;
    public Map<UUID, String> offlineReceiver = new ConcurrentHashMap<>();
    public Map<UUID, String> offlineMessages = new ConcurrentHashMap<>();
    public Map<String, String> latestRecipients = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Scheduler.init(this);
        MessageUtils.init(this);

        dbDriver = new DatabaseDriver("jdbc:sqlite:" + getDataFolder() + "/smpdatabase.db");
        dbDriver.createTable("properties", "uuid TEXT NOT NULL PRIMARY KEY", "player_name TEXT", "confirm_sending BOOLEAN NOT NULL DEFAULT 1");
        dbDriver.createTable("sounds", "uuid TEXT NOT NULL PRIMARY KEY", "player_name TEXT", "sound TEXT", "volume INTEGER");
        dbDriver.createTable("offline_msg", "sender TEXT", "receiver TEXT", "message TEXT");
        dbDriver.createTable("blacklist", "uuid TEXT", "blocked_uuid TEXT", "blocked_player TEXT");

        cacheManager = new DatabaseCacheManager(dbDriver);

        CommandManager.init(this, dbDriver, cacheManager);

        getServer().getPluginManager().registerEvents(new PlayerJoinQuitEventHandlers(this, dbDriver, cacheManager), this);
        getServer().getPluginManager().registerEvents(new PrivateChatHandler(), this);
    }

    @Override
    public void onDisable() {
        dbDriver.closeConnection();
    }

    public static SimpleMsgPlugin getInstance() {
        return instance;
    }
}
