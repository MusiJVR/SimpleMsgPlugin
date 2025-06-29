package simplemsgplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.utils.MessageUtils;
import simplemsgplugin.utils.Utils;
import simplemsgplugin.utils.DatabaseDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AcceptSendCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public AcceptSendCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;

        if (args.length != 0) {
            return false;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

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

        return true;
    }
}
