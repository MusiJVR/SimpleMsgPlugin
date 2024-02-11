package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;

import java.sql.*;
import java.util.*;

public class RemoveBlacklistCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private Connection con;
    public RemoveBlacklistCommand(JavaPlugin plugin, Connection con) {
        this.plugin = plugin;
        this.con = con;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 0 || args.length >= 2) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing"));
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String unblockPlayerInput = args[0];
        Player unblockPlayer = plugin.getServer().getPlayer(unblockPlayerInput);
        if (unblockPlayer == null || !Objects.equals(unblockPlayer.getName(), unblockPlayerInput)) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing"));
            return false;
        }

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLACKLIST WHERE UUID IS '" + uuid + "' AND BlockedUUID IS '" + unblockPlayer.getUniqueId() + "' AND BlockedPlayer IS '" + unblockPlayer.getName() + "';" );
            if (!rs.next()) {
                sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blnotblock"));
                return true;
            }
            String tableBLACKLIST = "DELETE FROM BLACKLIST WHERE UUID IS '" + uuid + "' AND BlockedUUID IS '" + unblockPlayer.getUniqueId() + "' AND BlockedPlayer IS '" + unblockPlayer.getName() + "';";
            stmt.executeUpdate(tableBLACKLIST);
            stmt.close();
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blsuccessunblock"));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}