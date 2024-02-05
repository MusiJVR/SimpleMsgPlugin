package simplemsgplugin;

import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.command.PlayerMsgCommand;
import simplemsgplugin.command.PlayerMsgTabCompleter;

public final class SimpleMsgPlugin extends JavaPlugin {

    private static SimpleMsgPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getServer().getLogger().info("[SimpleMsg] SimpleMsg is enabled");
        getServer().getPluginCommand("playermsg").setExecutor(new PlayerMsgCommand(this));
        getServer().getPluginCommand("playermsg").setTabCompleter(new PlayerMsgTabCompleter());
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[SimpleMsg] SimpleMsg is disabled");
    }

    public static SimpleMsgPlugin getInstance() {return instance;}
}