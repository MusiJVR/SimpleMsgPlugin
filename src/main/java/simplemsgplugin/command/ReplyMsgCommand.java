package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;

public class ReplyMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public ReplyMsgCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        if (!SimpleMsgPlugin.getInstance().latestRecipients.containsKey(sender.getName()) || SimpleMsgPlugin.getInstance().latestRecipients.get(sender.getName()) == null) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.nolastrecipient")));
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
