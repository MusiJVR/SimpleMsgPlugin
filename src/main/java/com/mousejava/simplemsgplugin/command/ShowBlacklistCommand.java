package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;

import java.util.*;

public class ShowBlacklistCommand implements ICommand {
    private final DatabaseDriver dbDriver;

    public ShowBlacklistCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("showblacklist", "simplemsgplugin.showblacklist", (ctx, player) -> {
            UUID uuid = player.getUniqueId();

            dbDriver.selectData("blocked_player", "blacklist", "WHERE uuid = ?", rs -> {
                List<String> blockedPlayers = new ArrayList<>();
                for (Map<String, Object> i : rs) {
                    blockedPlayers.add(i.get("blocked_player").toString());
                }

                if (blockedPlayers.isEmpty()) {
                    MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.empty");
                    return;
                }

                MessageUtils.sendMiniMessageTransformed(player, "messages.blacklist.players",
                        msg -> msg.replace("<blacklist>", String.join(", ", blockedPlayers)));
                }, uuid);

            return Command.SINGLE_SUCCESS;
        }).build();
    }

    @Override
    public String description() {
        return "This command allows you to display your blacklist";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("showbl");
    }
}
