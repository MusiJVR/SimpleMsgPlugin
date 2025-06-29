package simplemsgplugin.handler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import simplemsgplugin.chatgroups.Group;
import simplemsgplugin.chatgroups.GroupManager;
import simplemsgplugin.utils.MessageUtils;

public class PrivateChatHandler implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String prefix = MessageUtils.getPlain("privatechat_start_char");

        if (message.startsWith(prefix)) {
            String privateMessage = message.substring(prefix.length()).trim();
            Group group = GroupManager.findGroupByPlayer(event.getPlayer().getUniqueId());
            if (group != null) {
                event.setCancelled(true);
                group.sendMessage("messages.privatechat.message_template", event.getPlayer().getName(), privateMessage);
            }
        }
    }
}
