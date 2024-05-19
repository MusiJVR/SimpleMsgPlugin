package simplemsgplugin.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.GeneralUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private SqliteDriver sql;
    public PlayerMsgCommand(JavaPlugin plugin, SqliteDriver sql) {
        this.plugin = plugin;
        this.sql = sql;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            return false;
        }

        String playerName = args[0];
        Player argPlayer = plugin.getServer().getPlayer(playerName);

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(" " + args[i]);
        }

        try {
            List<Map<String, Object>> rsArgPlayer = sql.sqlSelectData("UUID", "SOUNDS", "PlayerName = '" + playerName + "'");
            if (rsArgPlayer.isEmpty()) {
                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.blmissing")));
                return true;
            }
            String uuidArgPlayer = (String) rsArgPlayer.get(0).get("UUID");

            Player blockedSender = (Player) sender;
            if (Objects.equals(blockedSender.getUniqueId().toString(), uuidArgPlayer)) {
                if (!SimpleMsgPlugin.getInstance().getConfig().getBoolean("sendmsgyourself")) {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.notmsgyouself")));
                    return true;
                }
            }
            List<Map<String, Object>> rsBlockFirst = sql.sqlSelectData("UUID", "BLACKLIST", "BlockedUUID = '" + blockedSender.getUniqueId() + "'");
            for (Map<String, Object> i : rsBlockFirst) {
                if (Objects.equals(i.get("UUID"), uuidArgPlayer)) {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.youinbl")));
                    return true;
                }
            }

            List<Map<String, Object>> rsBlockSecond = sql.sqlSelectData("BlockedUUID", "BLACKLIST", "UUID = '" + blockedSender.getUniqueId() + "'");
            for (Map<String, Object> i : rsBlockSecond) {
                if (Objects.equals(i.get("BlockedUUID"), uuidArgPlayer)) {
                    sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.youbl")));
                    return true;
                }
            }

            if (argPlayer == null && sender instanceof Player) {
                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();
                sendOfflineMessage(sender, uuid, playerName, message.toString());
                return true;
            }

            String msgSenderPattern = ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgsenderpattern"));
            sender.sendMessage(msgSenderPattern.replace("%sender%",sender.getName()).replace("%receiver%",argPlayer.getName()).replace("%message%",message));
            String msgRecipientPattern = ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgreceiverpattern"));
            argPlayer.sendMessage(msgRecipientPattern.replace("%sender%",sender.getName()).replace("%receiver%",argPlayer.getName()).replace("%message%",message));
            GeneralUtils.msgPlaySound(sql, argPlayer);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }

    private void sendOfflineMessage(CommandSender sender, UUID uuid, String playerName, String message) {
        SimpleMsgPlugin.getInstance().offlineReceiver.put(uuid, playerName);
        SimpleMsgPlugin.getInstance().offlineMessages.put(uuid, message);

        sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.playermissing")));
        sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgsendoffline")));
        TextComponent msgSendOffline = new TextComponent(SimpleMsgPlugin.getInstance().getConfig().getString("messages.acceptsend"));
        msgSendOffline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(SimpleMsgPlugin.getInstance().getConfig().getString("messages.clickmsgsendoffline"))));
        msgSendOffline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptsend"));
        sender.sendMessage(msgSendOffline);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (SimpleMsgPlugin.getInstance().offlineReceiver.containsKey(uuid) && SimpleMsgPlugin.getInstance().offlineMessages.containsKey(uuid)) {
                    SimpleMsgPlugin.getInstance().offlineReceiver.remove(uuid, playerName);
                    SimpleMsgPlugin.getInstance().offlineMessages.remove(uuid, message);
                }
            }
        }, 1200);
    }
}