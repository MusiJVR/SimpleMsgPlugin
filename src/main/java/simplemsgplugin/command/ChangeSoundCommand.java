package simplemsgplugin.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;

import java.sql.*;
import java.util.*;

public class ChangeSoundCommand implements CommandExecutor {
    private Connection con;
    public ChangeSoundCommand(Connection con) {
        this.con = con;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 0 || args.length >= 2) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundmissing"));
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String soundName = args[0];
        boolean valExist = false;
        for (Object valSound:Sound.values()) {
            if (Objects.equals(soundName, valSound.toString())) {
                valExist = true;
            }
        }
        if (!valExist) {
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundmissing"));
            return valExist;
        }

        try {
            Statement stmt = con.createStatement();
            String tableSOUNDS = "UPDATE SOUNDS SET Sound = '" + soundName + "' WHERE UUID IS '" + uuid + "';";
            stmt.executeUpdate(tableSOUNDS);
            stmt.close();
            sender.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundsuccess"));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}