package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.service.SkinService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.repository.OfflineMessagesRepository;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MailCommand implements ICommand {
    private final OfflineMessagesRepository messages;
    private final SkinService skinService;

    public MailCommand(OfflineMessagesRepository messages, SkinService skinService) {
        this.messages = messages;
        this.skinService = skinService;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("mailmsg", "simplemsgplugin.mailmsg", this::executeMail)
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to view unread messages";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msgmail", "pmmail", "mailpm", "mail");
    }

    private int executeMail(CommandContext<CommandSourceStack> ctx, Player player) {
        String playerName = player.getName();
        List<OfflineMessagesRepository.OfflineMessage> unread = messages.findForReceiver(playerName);
        if (!unread.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.your_unread");
            Component receiverHead = skinService.resolveHeadComponent(player.getUniqueId());
            for (OfflineMessagesRepository.OfflineMessage message : unread) {
                String senderName = message.senderName();
                String messageText = message.message();

                Component senderHead = message.senderUuid() != null
                        ? skinService.resolveHeadComponent(UUID.fromString(message.senderUuid()))
                        : skinService.buildPlayerHeadComponent(null);

                TagResolver heads = TagResolver.resolver(
                        Placeholder.component("sender_head", senderHead),
                        Placeholder.component("receiver_head", receiverHead)
                );

                MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.offline_pattern",
                        msg -> msg
                                .replace("<sender>", senderName)
                                .replace("<receiver>", playerName)
                                .replace("<message>", messageText),
                        heads,
                        component -> component
                                .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                                .clickEvent(ClickEvent.suggestCommand("/msg " + senderName + " "))
                );
            }

            messages.deleteForReceiver(playerName);
        } else {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.no_unread");
        }

        return Command.SINGLE_SUCCESS;
    }
}
