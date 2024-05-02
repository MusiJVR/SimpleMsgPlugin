package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

            plugin.getServer().getLogger().info("\n[SimpleMsg] ⏴-------SimpleMsgPlugin-------⏵ \n" +
                    "[SimpleMsg] Command: msghelp \n" +
                    "[SimpleMsg] Description: This command allows you to display all possible plugin commands \n" +
                    "[SimpleMsg] Usage: /<command> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: msgrc \n" +
                    "[SimpleMsg] Description: This command allows you to reload the plugin config \n" +
                    "[SimpleMsg] Usage: /<command> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: msg \n" +
                    "[SimpleMsg] Description: This command allows you to send private messages to the player \n" +
                    "[SimpleMsg] Usage: /<command> <player> <message> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: acceptsend \n" +
                    "[SimpleMsg] Description: This command allows you to accept sending an offline message \n" +
                    "[SimpleMsg] Usage: /<command> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: csound \n" +
                    "[SimpleMsg] Description: This command allows you to change the notification sound \n" +
                    "[SimpleMsg] Usage: /<command> <sound> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: cvolume \n" +
                    "[SimpleMsg] Description: This command allows you to change the notification volume \n" +
                    "[SimpleMsg] Usage: /<command> <volume> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: addbl \n" +
                    "[SimpleMsg] Description: This command allows you to add players to the blacklist \n" +
                    "[SimpleMsg] Usage: /<command> <player> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: removebl \n" +
                    "[SimpleMsg] Description: This command allows you to remove players from the blacklist \n" +
                    "[SimpleMsg] Usage: /<command> <player> \n[SimpleMsg] \n" +
                    "[SimpleMsg] Command: showbl \n" +
                    "[SimpleMsg] Description: This command allows you to display your blacklist \n" +
                    "[SimpleMsg] Usage: /<command> \n" +
                    "[SimpleMsg] ⏴----------------------------⏵");
        } else {
            sender.sendMessage("§b§l⏴-------§r§e§lSimpleMsgPlugin§r§b§l-------⏵§r \n" +
                    "§bCommand:§r §emsghelp§r \n" +
                    "§bDescription:§r §eThis command allows you to display all possible plugin commands§r \n" +
                    "§bUsage:§r §e/<command>§r \n \n" +
                    "§bCommand:§r §emsgrc§r \n" +
                    "§bDescription:§r §eThis command allows you to reload the plugin config§r \n" +
                    "§bUsage:§r §e/<command>§r \n \n" +
                    "§bCommand:§r §emsg§r \n" +
                    "§bDescription:§r §eThis command allows you to send private messages to the player§r \n" +
                    "§bUsage:§r §e/<command> <player> <message>§r \n \n" +
                    "§bCommand:§r §eacceptsend§r \n" +
                    "§bDescription:§r §eThis command allows you to accept sending an offline message§r \n" +
                    "§bUsage:§r §e/<command>§r \n \n" +
                    "§bCommand:§r §ecsound§r \n" +
                    "§bDescription:§r §eThis command allows you to change the notification sound§r \n" +
                    "§bUsage:§r §e/<command> <sound>§r \n \n" +
                    "§bCommand:§r §ecvolume§r \n" +
                    "§bDescription:§r §eThis command allows you to change the notification volume§r \n" +
                    "§bUsage:§r §e/<command> <volume>§r \n \n" +
                    "§bCommand:§r §eaddbl§r \n" +
                    "§bDescription:§r §eThis command allows you to add players to the blacklist§r \n" +
                    "§bUsage:§r §e/<command> <player>§r \n \n" +
                    "§bCommand:§r §eremovebl§r \n" +
                    "§bDescription:§r §eThis command allows you to remove players from the blacklist§r \n" +
                    "§bUsage:§r §e/<command> <player>§r \n \n" +
                    "§bCommand:§r §eshowbl§r \n" +
                    "§bDescription:§r §eThis command allows you to display your blacklist§r \n" +
                    "§bUsage:§r §e/<command>§r \n" +
                    "§b§l⏴----------------------------⏵§r");
        }
        return true;
    }
}