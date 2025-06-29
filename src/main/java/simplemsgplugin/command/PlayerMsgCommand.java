package simplemsgplugin.command;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerMsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final DatabaseDriver dbDriver;

    public PlayerMsgCommand(JavaPlugin plugin, DatabaseDriver dbDriver) {
        this.plugin = plugin;
        this.dbDriver = dbDriver;
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

        List<Map<String, Object>> rsArgPlayer = dbDriver.selectData("uuid", "sounds", "WHERE player_name = ?", playerName);
        if (rsArgPlayer.isEmpty()) {
            MessageUtils.sendColoredIfPresent(sender, "messages.blmissing");
            return true;
        }
        String uuidArgPlayer = (String) rsArgPlayer.get(0).get("uuid");

        Player blockedSender = (Player) sender;
        if (Objects.equals(blockedSender.getUniqueId().toString(), uuidArgPlayer)) {
            if (!SimpleMsgPlugin.getInstance().getConfig().getBoolean("sendmsgyourself")) {
                MessageUtils.sendColoredIfPresent(sender, "messages.notmsgyouself");
                return true;
            }
        }
        List<Map<String, Object>> rsBlockFirst = dbDriver.selectData("uuid", "blacklist", "WHERE blocked_uuid = ?", blockedSender.getUniqueId());
        for (Map<String, Object> i : rsBlockFirst) {
            if (Objects.equals(i.get("uuid"), uuidArgPlayer)) {
                MessageUtils.sendColoredIfPresent(sender, "messages.youinbl");
                return true;
            }
        }

        List<Map<String, Object>> rsBlockSecond = dbDriver.selectData("blocked_uuid", "blacklist", "WHERE uuid = ?", blockedSender.getUniqueId());
        for (Map<String, Object> i : rsBlockSecond) {
            if (Objects.equals(i.get("blocked_uuid"), uuidArgPlayer)) {
                MessageUtils.sendColoredIfPresent(sender, "messages.youbl");
                return true;
            }
        }

        if (argPlayer == null && sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            sendOfflineMessage(sender, uuid, playerName, message.toString());
            return true;
        }

        MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgsenderpattern",
                raw -> raw
                        .replace("%sender%", sender.getName())
                        .replace("%receiver%", argPlayer.getName())
                        .replace("%message%", message),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendreply")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + argPlayer.getName() + " "))
        );

        MessageUtils.sendMiniMessageIfPresent(argPlayer, "messages.msgreceiverpattern",
                raw -> raw
                        .replace("%sender%", sender.getName())
                        .replace("%receiver%", argPlayer.getName())
                        .replace("%message%", message),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendreply")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + sender.getName() + " "))
        );

        Utils.msgPlaySound(dbDriver, argPlayer);

        SimpleMsgPlugin.getInstance().latestRecipients.put(sender.getName(), argPlayer.getName());
        SimpleMsgPlugin.getInstance().latestRecipients.put(argPlayer.getName(), sender.getName());

        return true;
    }

    private void sendOfflineMessage(CommandSender sender, UUID uuid, String playerName, String message) {
        SimpleMsgPlugin.getInstance().offlineReceiver.put(uuid, playerName);
        SimpleMsgPlugin.getInstance().offlineMessages.put(uuid, message);

        MessageUtils.sendColoredIfPresent(sender, "messages.playermissing");
        MessageUtils.sendColoredIfPresent(sender, "messages.msgsendoffline");

        MessageUtils.sendMiniMessageComponent(sender, "messages.acceptsend",
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendoffline")))
                        .clickEvent(ClickEvent.runCommand("/acceptsend"))
        );

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
