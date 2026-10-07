package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.service.SkinService;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.database.repository.OfflineMessagesRepository;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
        UUID receiverUuid = player.getUniqueId();
        Scheduler.runAsync(() -> {
            synchronized (messages) {
                List<OfflineMessagesRepository.OfflineMessage> unread = messages.findForReceiver(playerName);
                Component receiverHead = unread.isEmpty() ? Component.empty()
                        : skinService.resolveHeadComponent(receiverUuid);

                List<Component> senderHeads = new ArrayList<>();
                Map<UUID, Component> loadedHeads = new HashMap<>();
                for (var message : unread) {
                    senderHeads.add(message.senderUuid() != null
                            ? loadedHeads.computeIfAbsent(UUID.fromString(message.senderUuid()), skinService::resolveHeadComponent)
                            : skinService.buildPlayerHeadComponent(null));
                }

                CompletableFuture<Boolean> delivered = new CompletableFuture<>();
                Scheduler.run(() -> {
                    if (delivered.isDone()) return;

                    try {
                        if (!player.isOnline()) {
                            delivered.complete(false);
                            return;
                        }

                        displayMail(player, playerName, unread, receiverHead, senderHeads);
                        delivered.complete(true);
                    } catch (Exception failure) {
                        delivered.completeExceptionally(failure);
                    }
                });

                if (delivered.orTimeout(30, TimeUnit.SECONDS).join() && !unread.isEmpty())
                    messages.deleteForReceiver(playerName);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private void displayMail(Player player, String playerName, List<OfflineMessagesRepository.OfflineMessage> unread, Component receiverHead, List<Component> senderHeads) {
        if (unread.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.no_unread");
            return;
        }

        MessageUtils.sendMiniMessageIfPresent(player, "messages.mailmsg.your_unread");
        for (int i = 0; i < unread.size(); i++) {
            var message = unread.get(i);
            String senderName = message.senderName();
            String messageText = message.message();
            TagResolver heads = TagResolver.resolver(
                    Placeholder.component("sender_head", senderHeads.get(i)),
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
    }
}
