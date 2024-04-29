package simplemsgplugin.command;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private SqliteDriver sql;
    public PlayerMsgCommand(JavaPlugin plugin, SqliteDriver sql) {
        this.plugin = plugin;
        this.sql = sql;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String playerName = args[0];
        Player argPlayer = plugin.getServer().getPlayer(playerName);
        if (args.length <= 1) {
            return false;
        }
        if (argPlayer == null || !Objects.equals(argPlayer.getName(), playerName)) {
            sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playermissing")));
            return false;
        }

        try {
            Player blockedSender = (Player) sender;
            if (Objects.equals(blockedSender.getUniqueId().toString(), argPlayer.getUniqueId().toString())) {
                if (!SimpleMsgPlugin.getInstance().getConfig().getBoolean("sendmsgyourself")) {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.notmsgyouself")));
                    return true;
                }
            }
            List<Map<String, Object>> rsBlockFirst = sql.sqlSelectData("UUID", "BLACKLIST", "BlockedUUID = '" + blockedSender.getUniqueId() + "'");
            for (Map<String, Object> i : rsBlockFirst) {
                if (Objects.equals(i.get("UUID"), argPlayer.getUniqueId().toString())) {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.youinbl")));
                    return true;
                }
            }

            List<Map<String, Object>> rsBlockSecond = sql.sqlSelectData("BlockedUUID", "BLACKLIST", "UUID = '" + blockedSender.getUniqueId() + "'");
            for (Map<String, Object> i : rsBlockSecond) {
                if (Objects.equals(i.get("BlockedUUID"), argPlayer.getUniqueId().toString())) {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.youbl")));
                    return true;
                }
            }

            List<Map<String, Object>> rs = sql.sqlSelectData("Sound", "SOUNDS", "UUID = '" + argPlayer.getUniqueId() + "'");
            String messageSound = (String) rs.get(0).get("Sound");

            StringBuilder message = new StringBuilder();
            for (int i=1; i<args.length; i++) {
                message.append(" " + args[i]);
            }

            String messagePattern = ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgpattern"));
            sender.sendMessage(messagePattern.replace("%sender%",sender.getName()).replace("%recipient%",argPlayer.getName()).replace("%message%",message));
            argPlayer.sendMessage(messagePattern.replace("%sender%",sender.getName()).replace("%recipient%",argPlayer.getName()).replace("%message%",message));
            if (!messageSound.equals("false")) {
                for (Sound sound : Sound.values()) {
                    if (messageSound.equals(sound.toString())) {
                        argPlayer.playSound(argPlayer.getLocation(), Sound.valueOf(messageSound), 1.0f, 1.0f);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}