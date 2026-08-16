package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.Set;

public class ReplyMsgCommand implements ICommand {
    private final SimpleMsgPlugin plugin;

    public ReplyMsgCommand(JavaPlugin plugin) {
        this.plugin = (SimpleMsgPlugin) plugin;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("replymsg", "simplemsgplugin.replymsg", "messages.incorrect_command")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("message", StringArgumentType.greedyString()),
                                this::executeReplyMsg
                        )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to send a private message to the last player who wrote to you";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("reply", "r");
    }

    private int executeReplyMsg(CommandContext<CommandSourceStack> ctx, Player player) {
        String name = player.getName();
        if (!plugin.latestRecipients.containsKey(name) || plugin.latestRecipients.get(name) == null) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.replymsg.no_last_recipient");
            return Command.SINGLE_SUCCESS;
        }

        String message = StringArgumentType.getString(ctx, "message").trim();
        player.performCommand("playermsg %s %s".formatted(plugin.latestRecipients.get(name), message));

        return Command.SINGLE_SUCCESS;
    }
}
