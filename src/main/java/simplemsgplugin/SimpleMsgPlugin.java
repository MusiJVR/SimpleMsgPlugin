package simplemsgplugin;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.command.*;
import simplemsgplugin.handler.SimpleEventHandler;

import java.sql.*;

public final class SimpleMsgPlugin extends JavaPlugin implements Listener {

    private static SimpleMsgPlugin instance;
    private Connection con = null;
    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        try {
            con = DriverManager.getConnection("jdbc:sqlite:plugins/SimpleMsgPlugin/smpdatabase.db");
            Statement stmt = con.createStatement();
            String tableSOUNDS = "CREATE TABLE IF NOT EXISTS SOUNDS (UUID TEXT NOT NULL PRIMARY KEY, Sound TEXT) ";
            stmt.executeUpdate(tableSOUNDS);
            String tableBLACKLIST = "CREATE TABLE IF NOT EXISTS BLACKLIST (UUID TEXT, BlockedUUID TEXT, BlockedPlayer TEXT) ";
            stmt.executeUpdate(tableBLACKLIST);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        getServer().getLogger().info("[SimpleMsg] SimpleMsg is enabled");
        getServer().getPluginManager().registerEvents(new SimpleEventHandler(con), this);
        getServer().getPluginCommand("showblacklist").setExecutor(new ShowBlacklistCommand(con));
        getServer().getPluginCommand("showblacklist").setTabCompleter(new ShowBlacklistTabCompleter());
        getServer().getPluginCommand("addblacklist").setExecutor(new AddBlacklistCommand(this, con));
        getServer().getPluginCommand("addblacklist").setTabCompleter(new AddBlacklistTabCompleter());
        getServer().getPluginCommand("removeblacklist").setExecutor(new RemoveBlacklistCommand(this, con));
        getServer().getPluginCommand("removeblacklist").setTabCompleter(new RemoveBlacklistTabCompleter());
        getServer().getPluginCommand("changesound").setExecutor(new ChangeSoundCommand(con));
        getServer().getPluginCommand("changesound").setTabCompleter(new ChangeSoundTabCompleter());
        getServer().getPluginCommand("playermsg").setExecutor(new PlayerMsgCommand(this, con));
        getServer().getPluginCommand("playermsg").setTabCompleter(new PlayerMsgTabCompleter());
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[SimpleMsg] SimpleMsg is disabled");
        try {
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static SimpleMsgPlugin getInstance() {return instance;}
}