package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.MessageUtils;

public class ReplyMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public ReplyMsgCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            MessageUtils.sendColoredIfPresent(sender, "messages.incorrectcommand");
            return true;
        }

        if (!SimpleMsgPlugin.getInstance().latestRecipients.containsKey(sender.getName()) || SimpleMsgPlugin.getInstance().latestRecipients.get(sender.getName()) == null) {
            MessageUtils.sendColoredIfPresent(sender, "messages.nolastrecipient");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            message.append(" " + args[i]);
        }

        plugin.getServer().dispatchCommand(sender, "msg " + SimpleMsgPlugin.getInstance().latestRecipients.get(sender.getName()) + message);

        return true;
    }
}
