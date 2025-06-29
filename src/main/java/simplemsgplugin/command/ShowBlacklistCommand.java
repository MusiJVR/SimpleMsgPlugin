package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.utils.DatabaseDriver;
import simplemsgplugin.utils.MessageUtils;

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
            MessageUtils.sendColoredIfPresent(sender, "messages.emptybl");
            return true;
        }

        MessageUtils.sendColoredTransformed(sender, "messages.playersbl",
                raw -> raw.replace("%blacklist%", String.join(", ", blockedPlayers)));

        return true;
    }
}
