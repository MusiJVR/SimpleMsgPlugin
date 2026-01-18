package simplemsgplugin.command;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.scheduler.Scheduler;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.*;

public class PlayerMsgCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public PlayerMsgCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgusage");
            return true;
        }

        String playerName = args[0];
        Player argPlayer = Bukkit.getPlayer(playerName);

        if (argPlayer != null) playerName = argPlayer.getName();

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(" " + args[i]);
        }

        String finalPlayerName = playerName;

        dbDriver.selectData("uuid", "sounds", "WHERE LOWER(player_name) = LOWER(?)", rsArgPlayer -> {
            if (rsArgPlayer.isEmpty()) {
                MessageUtils.sendColoredIfPresent(sender, "messages.blmissing");
                return;
            }
            String uuidArgPlayer = (String) rsArgPlayer.get(0).get("uuid");

            Player blockedSender = (Player) sender;
            if (Objects.equals(blockedSender.getUniqueId().toString(), uuidArgPlayer)) {
                if (!SimpleMsgPlugin.getInstance().getConfig().getBoolean("sendmsgyourself")) {
                    MessageUtils.sendColoredIfPresent(sender, "messages.notmsgyouself");
                    return;
                }
            }

            dbDriver.selectData("uuid", "blacklist", "WHERE blocked_uuid = ?", rsBlockFirst -> {
                for (Map<String, Object> i : rsBlockFirst) {
                    if (Objects.equals(i.get("uuid"), uuidArgPlayer)) {
                        MessageUtils.sendColoredIfPresent(sender, "messages.youinbl");
                        return;
                    }
                }

                dbDriver.selectData("blocked_uuid", "blacklist", "WHERE uuid = ?", rsBlockSecond -> {
                    for (Map<String, Object> i : rsBlockSecond) {
                        if (Objects.equals(i.get("blocked_uuid"), uuidArgPlayer)) {
                            MessageUtils.sendColoredIfPresent(sender, "messages.youbl");
                            return;
                        }
                    }

                    if (argPlayer == null && sender instanceof Player player) {
                        UUID uuid = player.getUniqueId();
                        sendOfflineMessage(sender, uuid, finalPlayerName, message.toString());
                        return;
                    }

                    String argPlayerName = argPlayer.getName();

                    MessageUtils.sendMiniMessageIfPresent(sender, "messages.msgsenderpattern",
                            raw -> raw
                                    .replace("%sender%", sender.getName())
                                    .replace("%receiver%", argPlayerName)
                                    .replace("%message%", message),
                            component -> component
                                    .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendreply")))
                                    .clickEvent(ClickEvent.suggestCommand("/msg " + argPlayerName + " "))
                    );

                    MessageUtils.sendMiniMessageIfPresent(argPlayer, "messages.msgreceiverpattern",
                            raw -> raw
                                    .replace("%sender%", sender.getName())
                                    .replace("%receiver%", argPlayerName)
                                    .replace("%message%", message),
                            component -> component
                                    .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendreply")))
                                    .clickEvent(ClickEvent.suggestCommand("/msg " + sender.getName() + " "))
                    );

                    Utils.msgPlaySound(dbDriver, argPlayer);

                    SimpleMsgPlugin.getInstance().latestRecipients.put(sender.getName(), argPlayer.getName());
                    SimpleMsgPlugin.getInstance().latestRecipients.put(argPlayer.getName(), sender.getName());
                }, blockedSender.getUniqueId());
            }, blockedSender.getUniqueId());
        }, playerName);

        return true;
    }

    private void sendOfflineMessage(CommandSender sender, UUID uuid, String playerName, String message) {
        if (!(sender instanceof Player player)) return;

        SimpleMsgPlugin.getInstance().offlineReceiver.put(uuid, playerName);
        SimpleMsgPlugin.getInstance().offlineMessages.put(uuid, message);

        MessageUtils.sendColoredIfPresent(sender, "messages.playermissing");

        dbDriver.selectData("confirm_sending", "properties", "WHERE uuid = ?", rs -> {
            boolean confirmSending;

            if (!rs.isEmpty()) {
                Object valueObj = rs.get(0).get("confirm_sending");
                if (valueObj instanceof Boolean b) confirmSending = b;
                else if (valueObj instanceof Number n) confirmSending = n.intValue() != 0;
                else if (valueObj instanceof String s) confirmSending = Boolean.parseBoolean(s);
                else confirmSending = SimpleMsgPlugin.getInstance().getConfig().getBoolean("confirm_sending");
            } else {
                confirmSending = SimpleMsgPlugin.getInstance().getConfig().getBoolean("confirm_sending");
            }

            if (confirmSending) {
                MessageUtils.sendColoredIfPresent(sender, "messages.msgsendoffline");

                MessageUtils.sendMiniMessageComponent(sender, "messages.acceptsend",
                        component -> component
                                .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendoffline")))
                                .clickEvent(ClickEvent.runCommand("/acceptsend"))
                );
            } else {
                if (SimpleMsgPlugin.getInstance().offlineReceiver.containsKey(uuid) && SimpleMsgPlugin.getInstance().offlineMessages.containsKey(uuid)) {
                    String playerReceiver = SimpleMsgPlugin.getInstance().offlineReceiver.get(uuid);
                    String msgOffline = SimpleMsgPlugin.getInstance().offlineMessages.get(uuid);

                    Map<String, Object> insertMap = new HashMap<>();
                    insertMap.put("sender", player.getName());
                    insertMap.put("receiver", playerReceiver);
                    insertMap.put("message", msgOffline);
                    dbDriver.insertData("offline_msg", insertMap);

                    MessageUtils.sendColoredIfPresent(sender, "messages.msgsendofflinesuccessfully");
                    Utils.msgPlaySound(dbDriver, player);

                    SimpleMsgPlugin.getInstance().offlineReceiver.remove(uuid, playerReceiver);
                    SimpleMsgPlugin.getInstance().offlineMessages.remove(uuid, msgOffline);
                }
            }

            Scheduler.runLater(() -> {
                if (SimpleMsgPlugin.getInstance().offlineReceiver.containsKey(uuid) && SimpleMsgPlugin.getInstance().offlineMessages.containsKey(uuid)) {
                    SimpleMsgPlugin.getInstance().offlineReceiver.remove(uuid, playerName);
                    SimpleMsgPlugin.getInstance().offlineMessages.remove(uuid, message);
                }
            }, 1200);
        }, uuid);
    }
}
