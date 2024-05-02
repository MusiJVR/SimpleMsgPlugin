package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.UUID;

public class ChangeVolumeCommand implements CommandExecutor {
    private SqliteDriver sql;
    public ChangeVolumeCommand(SqliteDriver sql) {
        this.sql = sql;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 1) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.volumemissing")));
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (!checkDigits(args[0])) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.volumemissing")));
            return true;
        }

        Integer volume = Integer.parseInt(args[0]);
        if (volume < 0 || volume > 100) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.volumemissing")));
            return true;
        }

        try {
            sql.sqlUpdateData("SOUNDS", "Volume = " + volume, "UUID = '" + uuid + "'");
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.volumesuccess")));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }

    private boolean checkDigits(String string) {
        boolean digits = true;
        for(int i = 0; i < string.length() && digits; i++) {
            if(!Character.isDigit(string.charAt(i))) {
                digits = false;
            }
        }
        return digits;
    }
}