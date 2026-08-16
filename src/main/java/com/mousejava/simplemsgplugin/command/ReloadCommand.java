package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.Set;

public class ReloadCommand implements ICommand {
    private final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.senderCommand("reloadmsg", "simplemsgplugin.reloadmsg", this::executeReload)
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to reload the plugin";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msgr", "msgreload");
    }

    private int executeReload(CommandContext<CommandSourceStack> ctx, CommandSender sender) {
        plugin.reloadConfig();
        MessageUtils.sendMiniMessageIfPresent(sender, "messages.reloadmsg.reload_successfully");
        return Command.SINGLE_SUCCESS;
    }
}
