package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.utils.MessageUtils;

public class MSGReloadConfigCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    public MSGReloadConfigCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0) {
            MessageUtils.sendColoredIfPresent(sender, "messages.error");
            return false;
        }
        plugin.reloadConfig();
        MessageUtils.sendColoredIfPresent(sender, "messages.configreloadsuccessfully");
        return true;
    }
}
