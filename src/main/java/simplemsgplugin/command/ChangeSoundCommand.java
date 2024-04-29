package simplemsgplugin.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.Objects;
import java.util.UUID;

public class ChangeSoundCommand implements CommandExecutor {
    private SqliteDriver sql;
    public ChangeSoundCommand(SqliteDriver sql) {
        this.sql = sql;
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

        try {
            sql.sqlUpdateData("SOUNDS", "Sound = '" + soundName + "'", "UUID = '" + uuid + "'");
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.soundsuccess")));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}