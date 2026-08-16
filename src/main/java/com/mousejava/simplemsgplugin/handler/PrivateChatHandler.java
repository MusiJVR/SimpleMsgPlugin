package com.mousejava.simplemsgplugin.handler;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.mousejava.simplemsgplugin.chatgroups.Group;
import com.mousejava.simplemsgplugin.chatgroups.GroupManager;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

public class PrivateChatHandler implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String prefix = MessageUtils.optionalPlain("privatechat_start_char").orElse("?");

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
