package simplemsgplugin.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;

import java.sql.*;
import java.util.*;

public class PlayerMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private Connection con;
    public PlayerMsgCommand(JavaPlugin plugin, Connection con) {
        this.plugin = plugin;
        this.con = con;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String playerName = args[0];
        Player argPlayer = plugin.getServer().getPlayer(playerName);
        if (args.length <= 1) {
            return false;
        }
        if (argPlayer == null || !Objects.equals(argPlayer.getName(), playerName)) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playermissing"));
            return false;
        }

        try {
            Statement stmt = con.createStatement();
            Player blockedSender = (Player) sender;
            if (Objects.equals(blockedSender.getUniqueId().toString(), argPlayer.getUniqueId().toString())) {
                if (!SimpleMsgPlugin.getInstance().getConfig().getBoolean("sendmsgyourself")) {
                    sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.notmsgyouself"));
                    return true;
                }
            }
            ResultSet rsBlock1 = stmt.executeQuery( "SELECT * FROM BLACKLIST WHERE BlockedUUID IS '" + blockedSender.getUniqueId() + "';" );
            if (Objects.equals(rsBlock1.getString("UUID"), argPlayer.getUniqueId().toString())) {
                sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.youinbl"));
                return true;
            }
            ResultSet rsBlock2 = stmt.executeQuery( "SELECT * FROM BLACKLIST WHERE UUID IS '" + blockedSender.getUniqueId() + "';" );
            if (Objects.equals(rsBlock2.getString("BlockedUUID"), argPlayer.getUniqueId().toString())) {
                sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.youbl"));
                return true;
            }

            ResultSet rs = stmt.executeQuery( "SELECT * FROM SOUNDS WHERE UUID IS '" + argPlayer.getUniqueId() + "';" );
            String messagesound = rs.getString("Sound");
            stmt.close();

            StringBuilder message = new StringBuilder();
            for (int i=1; i<args.length; i++) {
                message.append(" " + args[i]);
            }

            String messagepattern = SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgpattern");
            sender.sendMessage(messagepattern.replace("%sender%",sender.getName()).replace("%recipient%",argPlayer.getName()).replace("%message%",message));
            argPlayer.sendMessage(messagepattern.replace("%sender%",sender.getName()).replace("%recipient%",argPlayer.getName()).replace("%message%",message));
            argPlayer.playSound(argPlayer.getLocation(), Sound.valueOf(messagesound), 1.0f, 1.0f);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}