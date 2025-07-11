package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChangeVolumeCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public ChangeVolumeCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 1) {
            MessageUtils.sendColoredIfPresent(sender, "messages.volumemissing");
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (!Utils.checkDigits(args[0])) {
            MessageUtils.sendColoredIfPresent(sender, "messages.volumemissing");
            return true;
        }

        int volume = Integer.parseInt(args[0]);
        if (volume < 0 || volume > 100) {
            MessageUtils.sendColoredIfPresent(sender, "messages.volumemissing");
            return true;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("volume", volume);
        dbDriver.updateData("sounds", updateMap, "uuid = ?", uuid);
        MessageUtils.sendColoredIfPresent(sender, "messages.volumesuccess");
        Utils.msgPlaySound(dbDriver, player);

        return true;
    }
}
