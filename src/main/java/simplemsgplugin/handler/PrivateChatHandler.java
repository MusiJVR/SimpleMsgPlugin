package simplemsgplugin.handler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import simplemsgplugin.SimpleMsgPlugin;
import simplemsgplugin.chatgroups.Group;
import simplemsgplugin.chatgroups.GroupManager;

public class PrivateChatHandler implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String prefix = SimpleMsgPlugin.getInstance().getConfig().getString("privatechat_start_char");

        if (message.startsWith(prefix)) {
            String privateMessage = message.substring(prefix.length()).trim();
            Group group = GroupManager.findGroupByPlayer(event.getPlayer().getUniqueId());
            if (group != null) {
                event.setCancelled(true);
                group.sendMessage(SimpleMsgPlugin.getInstance().getConfig().getString("messages.privatechat.message_template"), event.getPlayer().getName(), privateMessage);
            }
        }
    }
}
