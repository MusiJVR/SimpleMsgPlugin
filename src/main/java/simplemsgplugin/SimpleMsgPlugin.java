package simplemsgplugin;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.command.*;
import simplemsgplugin.handler.SimpleEventHandler;
import simplemsgplugin.utils.SqliteDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SimpleMsgPlugin extends JavaPlugin implements Listener {

    private static SimpleMsgPlugin instance;
    private SqliteDriver sql;
    public Map<UUID, String> offlineReceiver = new HashMap<>();
    public Map<UUID, String> offlineMessages = new HashMap<>();

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        try {
            sql = new SqliteDriver(getDataFolder() + "/smpdatabase.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        getServer().getLogger().info("[SimpleMsg] SimpleMsgPlugin is enabled");
        getServer().getPluginManager().registerEvents(new SimpleEventHandler(sql), this);
        getServer().getPluginCommand("msghelp").setExecutor(new MSGHelpCommand(this));
        getServer().getPluginCommand("msghelp").setTabCompleter(new MSGHelpTabCompleter());
        getServer().getPluginCommand("msgreloadconfig").setExecutor(new MSGReloadConfigCommand(this));
        getServer().getPluginCommand("msgreloadconfig").setTabCompleter(new MSGReloadConfigTabCompleter());
        getServer().getPluginCommand("showblacklist").setExecutor(new ShowBlacklistCommand(sql));
        getServer().getPluginCommand("showblacklist").setTabCompleter(new ShowBlacklistTabCompleter());
        getServer().getPluginCommand("addblacklist").setExecutor(new AddBlacklistCommand(this, sql));
        getServer().getPluginCommand("addblacklist").setTabCompleter(new AddBlacklistTabCompleter());
        getServer().getPluginCommand("removeblacklist").setExecutor(new RemoveBlacklistCommand(this, sql));
        getServer().getPluginCommand("removeblacklist").setTabCompleter(new RemoveBlacklistTabCompleter());
        getServer().getPluginCommand("changesound").setExecutor(new ChangeSoundCommand(sql));
        getServer().getPluginCommand("changesound").setTabCompleter(new ChangeSoundTabCompleter());
        getServer().getPluginCommand("changevolume").setExecutor(new ChangeVolumeCommand(sql));
        getServer().getPluginCommand("changevolume").setTabCompleter(new ChangeVolumeTabCompleter());
        getServer().getPluginCommand("playermsg").setExecutor(new PlayerMsgCommand(this, sql));
        getServer().getPluginCommand("playermsg").setTabCompleter(new PlayerMsgTabCompleter(sql));
        getServer().getPluginCommand("acceptsend").setExecutor(new AcceptSendCommand(sql));
        getServer().getPluginCommand("acceptsend").setTabCompleter(new AcceptSendTabCompleter());
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[SimpleMsg] SimpleMsgPlugin is disabled");
        try {
            sql.connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static SimpleMsgPlugin getInstance() {return instance;}
}