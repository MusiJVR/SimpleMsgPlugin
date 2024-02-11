package simplemsgplugin.handler;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import simplemsgplugin.SimpleMsgPlugin;

import java.sql.*;

public class SimpleEventHandler implements Listener {
    private Connection con;
    public SimpleEventHandler(Connection con) {
        this.con = con;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent playerJoin) {
        UUID uuid = playerJoin.getPlayer().getUniqueId();
        String sound = SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgsound");
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT UUID FROM SOUNDS WHERE UUID IS '" + uuid + "';" );
            if (!rs.next()) {
                String tableSOUNDS = "INSERT INTO SOUNDS (UUID, Sound) " +
                        "VALUES ('" + uuid + "', '" + sound + "');";
                stmt.executeUpdate(tableSOUNDS);
                stmt.close();
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}