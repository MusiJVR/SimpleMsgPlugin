package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.repository.OfflineMessagesRepository;
import com.mousejava.simplemsgplugin.repository.PropertiesRepository;
import com.mousejava.simplemsgplugin.storage.OfflineMessageStorage;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Utils;

import java.util.Set;
import java.util.UUID;

public class AcceptSendCommand implements ICommand {
    private final OfflineMessageStorage offlineMessages;
    private final OfflineMessagesRepository messages;
    private final PropertiesRepository properties;

    public AcceptSendCommand(OfflineMessageStorage offlineMessages, OfflineMessagesRepository messages, PropertiesRepository properties) {
        this.offlineMessages = offlineMessages;
        this.messages = messages;
        this.properties = properties;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("acceptsend", "simplemsgplugin.acceptsend", this::executeAcceptSend)
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to accept sending an offline message";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("accsend");
    }

    private int executeAcceptSend(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();

        offlineMessages.find(uuid).ifPresent(pendingMessage -> {
            String playerReceiver = pendingMessage.receiver();
            String msgOffline = pendingMessage.message();

            messages.save(uuid, player.getName(), playerReceiver, msgOffline);

            MessageUtils.sendMiniMessageIfPresent(player, "messages.acceptsend.send_offline_successfully");
            Utils.msgPlaySound(properties, player);

            offlineMessages.remove(uuid, pendingMessage);
        });

        return Command.SINGLE_SUCCESS;
    }
}
