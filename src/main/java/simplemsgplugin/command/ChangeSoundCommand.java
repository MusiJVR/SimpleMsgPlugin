package simplemsgplugin.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChangeSoundCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public ChangeSoundCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 1) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundmissing")));
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        String soundName = args[0];
        boolean valExist = false;
        for (Object valSound:Sound.values()) {
            if (Objects.equals(soundName, valSound.toString())) {
                valExist = true;
            }
        }

        if (!valExist) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundmissing")));
            return valExist;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("sound", soundName);
        dbDriver.updateData("sounds", updateMap, String.format("uuid = '%s'", uuid));
        sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundsuccess")));
        Utils.msgPlaySound(dbDriver, player);

        return true;
    }
}
