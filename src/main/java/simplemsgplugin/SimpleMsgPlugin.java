package simplemsgplugin;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.command.*;
import simplemsgplugin.handler.EventHandlers;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SimpleMsgPlugin extends JavaPlugin implements Listener {

    private static SimpleMsgPlugin instance;
    private DatabaseDriver dbDriver;
    public Map<UUID, String> offlineReceiver = new HashMap<>();
    public Map<UUID, String> offlineMessages = new HashMap<>();
    public Map<String, String> latestRecipients = new HashMap<>();

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        dbDriver = new DatabaseDriver("jdbc:sqlite:" + getDataFolder() + "/smpdatabase.db");
        dbDriver.createTable("sounds", "uuid TEXT NOT NULL PRIMARY KEY", "player_name TEXT", "sound TEXT", "volume INTEGER");
        dbDriver.createTable("offline_msg", "sender TEXT", "receiver TEXT", "message TEXT");
        dbDriver.createTable("blacklist", "uuid TEXT", "blocked_uuid TEXT", "blocked_player TEXT");

        getServer().getLogger().info("[SimpleMsg] SimpleMsgPlugin is enabled");
        getServer().getPluginManager().registerEvents(new EventHandlers(dbDriver), this);
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
        getServer().getPluginCommand("changesound").setExecutor(new ChangeSoundCommand(dbDriver));
        getServer().getPluginCommand("changesound").setTabCompleter(new ChangeSoundTabCompleter());
        getServer().getPluginCommand("changevolume").setExecutor(new ChangeVolumeCommand(dbDriver));
        getServer().getPluginCommand("changevolume").setTabCompleter(new ChangeVolumeTabCompleter());
        getServer().getPluginCommand("playermsg").setExecutor(new PlayerMsgCommand(this, dbDriver));
        getServer().getPluginCommand("playermsg").setTabCompleter(new PlayerMsgTabCompleter(dbDriver));
        getServer().getPluginCommand("replymsg").setExecutor(new ReplyMsgCommand(this));
        getServer().getPluginCommand("replymsg").setTabCompleter(new ReplyMsgTabCompleter());
        getServer().getPluginCommand("acceptsend").setExecutor(new AcceptSendCommand(dbDriver));
        getServer().getPluginCommand("acceptsend").setTabCompleter(new AcceptSendTabCompleter());
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[SimpleMsg] SimpleMsgPlugin is disabled");
        dbDriver.closeConnection();
    }

    public static SimpleMsgPlugin getInstance() {
        return instance;
    }
}
