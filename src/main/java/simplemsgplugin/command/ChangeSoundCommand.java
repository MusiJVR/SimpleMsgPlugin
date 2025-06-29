package simplemsgplugin.command;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChangeSoundCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public ChangeSoundCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        if (args.length != 1) {
            MessageUtils.sendColoredIfPresent(sender, "messages.soundmissing");
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        String soundName = args[0];
        if (!isSoundValid(soundName)) {
            MessageUtils.sendColoredIfPresent(sender, "messages.soundmissing");
            return false;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("sound", soundName);
        dbDriver.updateData("sounds", updateMap, "uuid = ?", uuid);
        MessageUtils.sendColoredIfPresent(sender, "messages.soundsuccess");
        Utils.msgPlaySound(dbDriver, player);

        return true;
    }

    private static boolean isSoundValid(String name) {
        try {
            Sound.valueOf(name.toUpperCase());
            return true;
        } catch (Throwable ignored) {
            try {
                NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase());
                return Registry.SOUNDS.get(key) != null;
            } catch (Throwable ignoredAgain) {
                return false;
            }
        }
    }
}
