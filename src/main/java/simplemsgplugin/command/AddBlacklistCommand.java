package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AddBlacklistCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private SqliteDriver sql;
    public AddBlacklistCommand(JavaPlugin plugin, SqliteDriver sql) {
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
        String blockPlayerInput = args[0];
        Player blockPlayer = plugin.getServer().getPlayer(blockPlayerInput);
        if (blockPlayer == null || !Objects.equals(blockPlayer.getName(), blockPlayerInput)) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing")));
            return false;
        }
        if (blockPlayer.getUniqueId() == uuid) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blyourself")));
            return true;
        }

        try {
            List<Map<String, Object>> rs = sql.sqlSelectData("UUID", "BLACKLIST", "UUID = '" + uuid + "', BlockedUUID = '" + blockPlayer.getUniqueId() + "', BlockedPlayer = '" + blockPlayer.getName() + "'");
            if (!rs.isEmpty()) {
                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blalreadyblock")));
                return true;
            }
            Map<String, Object> insertMap = new HashMap<>();
            insertMap.put("UUID", uuid);
            insertMap.put("BlockedUUID", blockPlayer.getUniqueId());
            insertMap.put("BlockedPlayer", blockPlayer.getName());
            sql.sqlInsertData("BLACKLIST", insertMap);
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blsuccessblock")));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}