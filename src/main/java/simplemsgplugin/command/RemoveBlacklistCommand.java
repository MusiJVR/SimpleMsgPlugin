package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RemoveBlacklistCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private SqliteDriver sql;
    public RemoveBlacklistCommand(JavaPlugin plugin, SqliteDriver sql) {
        this.plugin = plugin;
        this.sql = sql;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 1) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing")));
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String unblockPlayerInput = args[0];
        Player unblockPlayer = plugin.getServer().getPlayer(unblockPlayerInput);
        if (unblockPlayer == null || !Objects.equals(unblockPlayer.getName(), unblockPlayerInput)) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing")));
            return false;
        }

        try {
            List<Map<String, Object>> rs = sql.sqlSelectData("BlockedUUID", "BLACKLIST", "UUID = '" + uuid + "' AND BlockedUUID = '" + unblockPlayer.getUniqueId() + "' AND BlockedPlayer = '" + unblockPlayer.getName() + "'");
            if (rs.isEmpty()) {
                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blnotblock")));
                return true;
            }
            sql.sqlDeleteData("BLACKLIST", "UUID = '" + uuid + "' AND BlockedUUID = '" + unblockPlayer.getUniqueId() + "' AND BlockedPlayer = '" + unblockPlayer.getName() + "'");
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blsuccessunblock")));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}