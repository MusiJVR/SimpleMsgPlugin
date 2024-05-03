package simplemsgplugin.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.GeneralUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimpleEventHandler implements Listener {
    private SqliteDriver sql;
    public SimpleEventHandler(SqliteDriver sql) {
        this.sql = sql;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent playerJoin) {
        Player player = playerJoin.getPlayer();
        UUID uuid = player.getUniqueId();
        String sound = SimpleMsgPlugin.getInstance().getConfig().getString("msgsound");
        Integer volume = setDefaultValue(50, "volumesound", 0, 100);

        try {
            List<Map<String, Object>> rsRegister = sql.sqlSelectData("UUID", "SOUNDS", "UUID = '" + uuid + "'");
            if (rsRegister.isEmpty()) {
                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("UUID", uuid);
                insertMap.put("PlayerName", playerJoin.getPlayer().getName());
                insertMap.put("Sound", sound);
                insertMap.put("Volume", volume);
                sql.sqlInsertData("SOUNDS", insertMap);
            }

            List<Map<String, Object>> rsOfflineMessage = sql.sqlSelectData("Sender, Message", "OFFLINE_MSG", "Receiver = '" + player.getName() + "'");
            if (!rsOfflineMessage.isEmpty()) {
                player.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.haveunreadmsg")));
                String msgOfflienPattern = ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgofflinepattern"));
                for (Map<String, Object> i : rsOfflineMessage) {
                    String sender = (String) i.get("Sender");
                    String messages = (String) i.get("Message");
                    player.sendMessage(msgOfflienPattern.replace("%sender%", sender).replace("%messages%", messages));
                }

                GeneralUtils.msgPlaySound(sql, player);

                sql.sqlDeleteData("OFFLINE_MSG", "Receiver = '" + player.getName() + "'");
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private int setDefaultValue(Integer value, String pathConfig, Integer minValue, Integer maxValue) {
        String valueDefaultConfig = SimpleMsgPlugin.getInstance().getConfig().getString(pathConfig);
        if (checkDigits(valueDefaultConfig)) {
            Integer valueDefault = new Integer(valueDefaultConfig);

            if (valueDefault >= minValue && valueDefault <= maxValue) {
                value = valueDefault;
            }
        }

        return value;
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