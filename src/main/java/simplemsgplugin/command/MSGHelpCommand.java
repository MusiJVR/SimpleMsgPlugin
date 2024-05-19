package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;

public class MSGHelpCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    public MSGHelpCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length != 0) {
                return true;
            }
            String consoleHelpList = ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("helpmessages.consolehelplist"));
            plugin.getServer().getLogger().info(consoleHelpList);
        } else {
            String playerHelpList = ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("helpmessages.playerhelplist"));
            sender.sendMessage(playerHelpList);
        }
        return true;
    }
}