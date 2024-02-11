package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;

import java.sql.*;
import java.util.*;

public class ShowBlacklistCommand implements CommandExecutor {
    private Connection con;
    public ShowBlacklistCommand(Connection con) {
        this.con = con;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT BlockedPlayer FROM BLACKLIST WHERE UUID IS '" + uuid + "';" );
            ArrayList<String> blockedPlayers = new ArrayList<>();
            while (rs.next()) {
                blockedPlayers.add(rs.getString("BlockedPlayer"));
            }
            if (blockedPlayers.isEmpty()) {
                sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.emptybl"));
                return true;
            }
            stmt.close();
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playersbl") + blockedPlayers);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}