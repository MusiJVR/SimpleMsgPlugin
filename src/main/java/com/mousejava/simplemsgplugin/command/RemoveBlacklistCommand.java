package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.*;

public class RemoveBlacklistCommand implements ICommand {
    private final DatabaseDriver dbDriver;

    public RemoveBlacklistCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("removeblacklist", "simplemsgplugin.removeblacklist", "messages.playermsg.missing")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("player", StringArgumentType.word(), Cmd.ONLINE_PLAYERS),
                                this::executeRemoveBlacklist
                        )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to remove players from the blacklist";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("removebl", "unignore");
    }

    private int executeRemoveBlacklist(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        String unblockPlayerInput = StringArgumentType.getString(ctx, "player");
        Optional<Player> resolved = Cmd.resolveOnlinePlayer(unblockPlayerInput);
        if (resolved.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.playermsg.missing");
            return Command.SINGLE_SUCCESS;
        }

        Player unblockPlayer = resolved.get();
        dbDriver.selectData("blocked_uuid", "blacklist", "WHERE uuid = ? AND blocked_uuid = ? AND blocked_player = ?", rs -> {
            if (rs.isEmpty()) {
                MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.not_block");
                return;
            }

            dbDriver.deleteData("blacklist", "uuid = ? AND blocked_uuid = ? AND blocked_player = ?", uuid, unblockPlayer.getUniqueId(), unblockPlayer.getName());
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.success_unblock");
        }, uuid, unblockPlayer.getUniqueId(), unblockPlayer.getName());

        return Command.SINGLE_SUCCESS;
    }
}
