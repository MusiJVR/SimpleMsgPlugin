package simplemsgplugin.command;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.utils.DatabaseDriver;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public NotificationCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1 || args.length > 2) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgnotification.usage");
            return true;
        }

        UUID uuid = player.getUniqueId();
        Map<String, Object> updateMap = new HashMap<>();

        String soundName = getValidSound(args[0]);
        if (soundName == null) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgnotification.soundmissing");
            return true;
        }
        updateMap.put("sound", soundName);

        if (args.length == 2) {
            if (!Utils.checkDigits(args[1])) {
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgnotification.volumemissing");
                return true;
            }

            int volume = Integer.parseInt(args[1]);
            if (volume < 0 || volume > 100) {
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgnotification.volumemissing");
                return true;
            }

            updateMap.put("volume", volume);
        }

        dbDriver.updateData("sounds", updateMap, "uuid = ?", uuid);

        MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgnotification.success");
        Utils.msgPlaySound(dbDriver, player);

        return true;
    }

    private String getValidSound(String name) {
        try {
            Sound.valueOf(name.toUpperCase());
            return name.toLowerCase();
        } catch (Throwable ignored) {}

        try {
            NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
            if (Registry.SOUNDS.get(key) != null) {
                return key.getKey();
            }
        } catch (Throwable ignored) {}

        return null;
    }
}
