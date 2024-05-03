package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.ColorUtils;
import simplemsgplugin.utils.GeneralUtils;
import simplemsgplugin.utils.SqliteDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AcceptSendCommand implements CommandExecutor {
    private SqliteDriver sql;
    public AcceptSendCommand(SqliteDriver sql) {
        this.sql = sql;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 0) {
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        try {
            if (SimpleMsgPlugin.getInstance().offlineReceiver.containsKey(uuid) && SimpleMsgPlugin.getInstance().offlineMessages.containsKey(uuid)) {
                String playerReceiver = SimpleMsgPlugin.getInstance().offlineReceiver.get(uuid);
                String msgOffline = SimpleMsgPlugin.getInstance().offlineMessages.get(uuid);

                Map<String, Object> insertMap = new HashMap<>();
                insertMap.put("Sender", player.getName());
                insertMap.put("Receiver", playerReceiver);
                insertMap.put("Message", msgOffline);
                sql.sqlInsertData("OFFLINE_MSG", insertMap);

                sender.sendMessage(ColorUtils.translateColorCodes(SimpleMsgPlugin.getInstance().getConfig().getString("messages.msgsendofflinesuccessfully")));
                GeneralUtils.msgPlaySound(sql, player);

                SimpleMsgPlugin.getInstance().offlineReceiver.remove(uuid, playerReceiver);
                SimpleMsgPlugin.getInstance().offlineMessages.remove(uuid, msgOffline);
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
}