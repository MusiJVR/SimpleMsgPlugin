package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.utils.DatabaseDriver;
import simplemsgplugin.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AddBlacklistCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final DatabaseDriver dbDriver;
    
    public AddBlacklistCommand(JavaPlugin plugin, DatabaseDriver dbDriver) {
        this.plugin = plugin;
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 1) {
            MessageUtils.sendColoredIfPresent(sender, "messages.blmissing");
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        String blockPlayerInput = args[0];
        Player blockPlayer = plugin.getServer().getPlayer(blockPlayerInput);
        if (blockPlayer == null || !Objects.equals(blockPlayer.getName(), blockPlayerInput)) {
            MessageUtils.sendColoredIfPresent(sender, "messages.blmissing");
            return false;
        }
        if (blockPlayer.getUniqueId() == uuid) {
            MessageUtils.sendColoredIfPresent(sender, "messages.blyourself");
            return true;
        }

        List<Map<String, Object>> rs = dbDriver.selectData("uuid", "blacklist", "WHERE uuid = ? AND blocked_uuid = ? AND blocked_player = ?", uuid, blockPlayer.getUniqueId(), blockPlayer.getName());
        if (!rs.isEmpty()) {
            MessageUtils.sendColoredIfPresent(sender, "messages.blalreadyblock");
            return true;
        }
        Map<String, Object> insertMap = new HashMap<>();
        insertMap.put("uuid", uuid);
        insertMap.put("blocked_uuid", blockPlayer.getUniqueId());
        insertMap.put("blocked_player", blockPlayer.getName());
        dbDriver.insertData("blacklist", insertMap);
        MessageUtils.sendColoredIfPresent(sender, "messages.blsuccessblock");

        return true;
    }
}
