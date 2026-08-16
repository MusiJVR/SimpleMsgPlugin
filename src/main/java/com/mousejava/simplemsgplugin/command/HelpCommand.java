package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpCommand implements ICommand {
    private final JavaPlugin plugin;

    public HelpCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.senderCommand("helpmsg", "simplemsgplugin.helpmsg", (ctx, sender) -> {
            if (sender instanceof Player) {
                MessageUtils.sendMiniMessageIfPresent(sender, "messages.help_message");
            }
            else {
                String prefix = plugin.getPluginMeta().getLoggerPrefix();
                MessageUtils.sendMiniMessageTransformed(sender, "messages.help_message",
                        msg -> Arrays.stream(msg.split("\n"))
                                .map(line -> "[%s] %s".formatted(prefix, line))
                                .collect(Collectors.joining("\n"))
                );
            }

            return Command.SINGLE_SUCCESS;
        }).build();
    }

    @Override
    public String description() {
        return "This command allows you to display all possible plugin commands";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msghelp", "msgh");
    }
}
