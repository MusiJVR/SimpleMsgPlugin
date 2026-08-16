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

public class AddBlacklistCommand implements ICommand {
    private final DatabaseDriver dbDriver;
    
    public AddBlacklistCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("addblacklist", "simplemsgplugin.addblacklist", "messages.playermsg.missing")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("player", StringArgumentType.word(), Cmd.ONLINE_PLAYERS),
                                this::executeAddBlacklist
                        )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to add players to the blacklist";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("addbl", "ignore");
    }

    private int executeAddBlacklist(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        String blockPlayerInput = StringArgumentType.getString(ctx, "player");
        Optional<Player> resolved = Cmd.resolveOnlinePlayer(blockPlayerInput);
        if (resolved.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.playermsg.missing");
            return Command.SINGLE_SUCCESS;
        }

        Player blockPlayer = resolved.get();
        if (blockPlayer.getUniqueId().equals(uuid)) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.yourself");
            return Command.SINGLE_SUCCESS;
        }

        dbDriver.selectData("uuid", "blacklist", "WHERE uuid = ? AND blocked_uuid = ? AND blocked_player = ?", rs -> {
            if (!rs.isEmpty()) {
                MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.already_block");
                return;
            }
            Map<String, Object> insertMap = new HashMap<>();
            insertMap.put("uuid", uuid);
            insertMap.put("blocked_uuid", blockPlayer.getUniqueId());
            insertMap.put("blocked_player", blockPlayer.getName());
            dbDriver.insertData("blacklist", insertMap);
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.success_block");
        }, uuid, blockPlayer.getUniqueId(), blockPlayer.getName());

        return Command.SINGLE_SUCCESS;
    }
}
