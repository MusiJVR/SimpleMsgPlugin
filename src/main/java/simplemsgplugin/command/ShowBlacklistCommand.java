package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShowBlacklistCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public ShowBlacklistCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length >= 1) {
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        List<Map<String, Object>> rs = dbDriver.selectData("blocked_player", "blacklist", "WHERE uuid = ?", uuid);
        ArrayList<String> blockedPlayers = new ArrayList<>();
        for (Map<String, Object> i : rs) {
            blockedPlayers.add(i.get("blocked_player").toString());
        }
        if (blockedPlayers.isEmpty()) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.emptybl")));
            return true;
        }
        sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playersbl") + blockedPlayers));

        return true;
    }
}
