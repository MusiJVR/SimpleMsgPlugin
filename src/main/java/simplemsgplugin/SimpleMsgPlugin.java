package simplemsgplugin;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.command.*;
import simplemsgplugin.handler.PlayerJoinQuitEventHandlers;
import simplemsgplugin.handler.PrivateChatHandler;
import simplemsgplugin.utils.DatabaseCacheManager;
import simplemsgplugin.utils.DatabaseDriver;

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

        dbDriver = new DatabaseDriver("jdbc:sqlite:" + getDataFolder() + "/smpdatabase.db");
        dbDriver.createTable("properties", "uuid TEXT NOT NULL PRIMARY KEY", "player_name TEXT", "confirm_sending BOOLEAN NOT NULL DEFAULT 1");
        dbDriver.createTable("sounds", "uuid TEXT NOT NULL PRIMARY KEY", "player_name TEXT", "sound TEXT", "volume INTEGER");
        dbDriver.createTable("offline_msg", "sender TEXT", "receiver TEXT", "message TEXT");
        dbDriver.createTable("blacklist", "uuid TEXT", "blocked_uuid TEXT", "blocked_player TEXT");

        cacheManager = new DatabaseCacheManager(dbDriver);

        getServer().getPluginManager().registerEvents(new PlayerJoinQuitEventHandlers(dbDriver, cacheManager), this);
        getServer().getPluginManager().registerEvents(new PrivateChatHandler(), this);
        getServer().getPluginCommand("msghelp").setExecutor(new MSGHelpCommand(this));
        getServer().getPluginCommand("msghelp").setTabCompleter(new MSGHelpTabCompleter());
        getServer().getPluginCommand("msgreloadconfig").setExecutor(new MSGReloadConfigCommand(this));
        getServer().getPluginCommand("msgreloadconfig").setTabCompleter(new MSGReloadConfigTabCompleter());
        getServer().getPluginCommand("showblacklist").setExecutor(new ShowBlacklistCommand(dbDriver));
        getServer().getPluginCommand("showblacklist").setTabCompleter(new ShowBlacklistTabCompleter());
        getServer().getPluginCommand("addblacklist").setExecutor(new AddBlacklistCommand(this, dbDriver));
        getServer().getPluginCommand("addblacklist").setTabCompleter(new AddBlacklistTabCompleter());
        getServer().getPluginCommand("removeblacklist").setExecutor(new RemoveBlacklistCommand(this, dbDriver));
        getServer().getPluginCommand("removeblacklist").setTabCompleter(new RemoveBlacklistTabCompleter());
        getServer().getPluginCommand("msgnotification").setExecutor(new NotificationCommand(dbDriver));
        getServer().getPluginCommand("msgnotification").setTabCompleter(new NotificationTabCompleter());
        getServer().getPluginCommand("playermsg").setExecutor(new PlayerMsgCommand(dbDriver));
        getServer().getPluginCommand("playermsg").setTabCompleter(new PlayerMsgTabCompleter(cacheManager));
        getServer().getPluginCommand("replymsg").setExecutor(new ReplyMsgCommand(this));
        getServer().getPluginCommand("replymsg").setTabCompleter(new ReplyMsgTabCompleter());
        getServer().getPluginCommand("acceptsend").setExecutor(new AcceptSendCommand(dbDriver));
        getServer().getPluginCommand("acceptsend").setTabCompleter(new AcceptSendTabCompleter());
        getServer().getPluginCommand("msgmail").setExecutor(new MailCommand(dbDriver));
        getServer().getPluginCommand("msgmail").setTabCompleter(new MailTabCompleter());
        getServer().getPluginCommand("privatechat").setExecutor(new PrivateChatCommand());
        getServer().getPluginCommand("privatechat").setTabCompleter(new PrivateChatTabCompleter());
        getServer().getPluginCommand("msgproperties").setExecutor(new PropertiesCommand(dbDriver));
        getServer().getPluginCommand("msgproperties").setTabCompleter(new PropertiesTabCompleter());
    }

    @Override
    public void onDisable() {
        dbDriver.closeConnection();
    }

    public static SimpleMsgPlugin getInstance() {
        return instance;
    }
}
