package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShowBlacklistCommand implements CommandExecutor {
    private SqliteDriver sql;
    public ShowBlacklistCommand(SqliteDriver sql) {
        this.sql = sql;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length >= 1) {
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        try {
            List<Map<String, Object>> rs = sql.sqlSelectData("BlockedPlayer", "BLACKLIST", "UUID = '" + uuid + "'");
            ArrayList<String> blockedPlayers = new ArrayList<>();
            for (Map<String, Object> i : rs) {
                blockedPlayers.add(i.get("BlockedPlayer").toString());
            }
            if (blockedPlayers.isEmpty()) {
                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.emptybl")));
                return true;
            }
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playersbl") + blockedPlayers));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}