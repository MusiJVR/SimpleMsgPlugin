package simplemsgplugin.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.SqliteDriver;

public class SimpleEventHandler implements Listener {
    private SqliteDriver sql;
    public SimpleEventHandler(SqliteDriver sql) {
        this.sql = sql;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent playerJoin) {
        UUID uuid = playerJoin.getPlayer().getUniqueId();
        String sound = SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgsound");
        try {
            List<Map<String, Object>> rs = sql.sqlSelectData("UUID", "SOUNDS", "UUID = '" + uuid + "'");
            if (rs.isEmpty()) {
                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("UUID", uuid);
                insertMap.put("Sound", sound);
                sql.sqlInsertData("SOUNDS", insertMap);
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}