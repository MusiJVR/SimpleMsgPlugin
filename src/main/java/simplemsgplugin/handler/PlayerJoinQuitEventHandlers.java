package simplemsgplugin.handler;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerJoinQuitEventHandlers implements Listener {
    private final DatabaseDriver dbDriver;

    public PlayerJoinQuitEventHandlers(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent playerJoin) {
        Player player = playerJoin.getPlayer();
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        List<Map<String, Object>> rsOldPlayerName = dbDriver.selectData("player_name", "sounds", "WHERE uuid = ?", uuid);
        if (!rsOldPlayerName.isEmpty()) {
            String oldPlayerName = (String) rsOldPlayerName.get(0).get("player_name");
            if (!playerName.equals(oldPlayerName)) {
                Map<String, String> updates = Map.of(
                        "sounds", "player_name",
                        "offline_msg_sender", "sender",
                        "offline_msg_receiver", "receiver",
                        "blacklist", "blocked_player"
                );

                updates.forEach((tableKey, column) -> {
                    String table = tableKey.contains("sender") || tableKey.contains("receiver") ? "offline_msg" : tableKey;
                    String whereColumn = tableKey.contains("sender") ? "sender" :
                            tableKey.contains("receiver") ? "receiver" :
                                    tableKey.equals("blacklist") ? "blocked_uuid" : "uuid";

                    dbDriver.updateData(
                            table,
                            Map.of(column, playerName),
                            whereColumn + " = ?",
                            (tableKey.equals("sounds") || tableKey.equals("blacklist")) ? uuid : oldPlayerName
                    );
                });
            }
        }

        String sound = SimpleMsgPlugin.getInstance().getConfig().getString("msgsound");
        int volume = setDefaultValue(50, "volumesound", 0, 100);

        List<Map<String, Object>> rsRegister = dbDriver.selectData("uuid", "sounds", "WHERE uuid = ?", uuid);
        if (rsRegister.isEmpty()) {
            Map<String, Object> insertMap = new HashMap<>();
            insertMap.put("uuid", uuid);
            insertMap.put("player_name", playerName);
            insertMap.put("sound", sound);
            insertMap.put("volume", volume);
            dbDriver.insertData("sounds", insertMap);
        }

        List<Map<String, Object>> rsOfflineMessage = dbDriver.selectData("sender, message", "offline_msg", "WHERE LOWER(receiver) = LOWER(?)", player.getName());
        if (!rsOfflineMessage.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.haveunreadmsg");
            Utils.msgPlaySound(dbDriver, player);
        }
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent playerQuit) {
        Player player = playerQuit.getPlayer();
        if (SimpleMsgPlugin.getInstance().latestRecipients.containsKey(player.getName()) && SimpleMsgPlugin.getInstance().latestRecipients.get(player.getName()) != null) {
            SimpleMsgPlugin.getInstance().latestRecipients.remove(player.getName());
        }
    }

    private int setDefaultValue(int value, String pathConfig, int minValue, int maxValue) {
        String valueDefaultConfig = SimpleMsgPlugin.getInstance().getConfig().getString(pathConfig);
        if (Utils.checkDigits(valueDefaultConfig)) {
            int valueDefault = Integer.parseInt(valueDefaultConfig);

            if (valueDefault >= minValue && valueDefault <= maxValue) {
                value = valueDefault;
            }
        }

        return value;
    }
}
