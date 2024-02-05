package simplemsgplugin.command;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;

public class PlayerMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    public PlayerMsgCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) return false;
        String playerName = args[0];
        Player argPlayer = plugin.getServer().getPlayer(playerName);
        if (argPlayer == null) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playermissing"));
            return true;
        }
        StringBuilder message = new StringBuilder();
        for (int i=1; i<args.length; i++) {
            message.append(" " + args[i]);
        }

        String messagepattern = SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgpattern");
        sender.sendMessage(messagepattern.replace("%sender%",sender.getName()).replace("%recipient%",argPlayer.getName()).replace("%message%",message));
        argPlayer.sendMessage(messagepattern.replace("%sender%",sender.getName()).replace("%recipient%",argPlayer.getName()).replace("%message%",message));

        String messagesound = SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgsound");
        argPlayer.playSound(argPlayer.getLocation(), Sound.valueOf(messagesound), 1.0f, 1.0f);
        return true;
    }
}