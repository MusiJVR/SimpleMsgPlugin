package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;

import java.sql.*;
import java.util.*;

public class AddBlacklistCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private Connection con;
    public AddBlacklistCommand(JavaPlugin plugin, Connection con) {
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
        String blockPlayerInput = args[0];
        Player blockPlayer = plugin.getServer().getPlayer(blockPlayerInput);
        if (blockPlayer == null || !Objects.equals(blockPlayer.getName(), blockPlayerInput)) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing"));
            return false;
        }
        if (blockPlayer.getUniqueId() == uuid) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blyourself"));
            return true;
        }

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM BLACKLIST WHERE UUID IS '" + uuid + "' AND BlockedUUID IS '" + blockPlayer.getUniqueId() + "' AND BlockedPlayer IS '" + blockPlayer.getName() + "';" );
            if (rs.next()) {
                sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blalreadyblock"));
                return true;
            }
            String tableBLACKLIST = "INSERT INTO BLACKLIST (UUID, BlockedUUID, BlockedPlayer) " +
                    "VALUES ('" + uuid + "', '" + blockPlayer.getUniqueId() + "', '" + blockPlayer.getName() + "');";
            stmt.executeUpdate(tableBLACKLIST);
            stmt.close();
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blsuccessblock"));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}