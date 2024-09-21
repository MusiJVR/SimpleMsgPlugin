package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RemoveBlacklistCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final DatabaseDriver dbDriver;

    public RemoveBlacklistCommand(JavaPlugin plugin, DatabaseDriver dbDriver) {
        this.plugin = plugin;
        this.dbDriver = dbDriver;
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

        List<Map<String, Object>> rs = dbDriver.selectData("blocked_uuid", "blacklist", "WHERE uuid = ? AND blocked_uuid = ? AND blocked_player = ?", uuid, unblockPlayer.getUniqueId(), unblockPlayer.getName());
        if (rs.isEmpty()) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blnotblock")));
            return true;
        }
        dbDriver.deleteData("blacklist", "uuid = ? AND blocked_uuid = ? AND blocked_player = ?", uuid, unblockPlayer.getUniqueId(), unblockPlayer.getName());
        sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blsuccessunblock")));

        return true;
    }
}
