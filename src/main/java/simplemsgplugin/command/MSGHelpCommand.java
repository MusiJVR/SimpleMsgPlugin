package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.utils.MessageUtils;

public class MSGHelpCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public MSGHelpCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length != 0) {
                MessageUtils.sendColoredIfPresent(sender, "messages.incorrectcommand");
                return true;
            }

            MessageUtils.optionalColored("helpmessages.consolehelplist").ifPresent(msg -> plugin.getServer().getLogger().info(msg));
        } else {
            MessageUtils.sendColoredIfPresent(sender, "helpmessages.playerhelplist");
        }
        return true;
    }
}
