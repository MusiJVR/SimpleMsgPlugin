package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.utils.DatabaseDriver;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.*;

public class BlacklistCommand implements ICommand {
    private final DatabaseDriver dbDriver;

    public BlacklistCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.playerCommand("blacklist", "simplemsgplugin.blacklist", "messages.blacklist.usage")
                .then(buildAdd())
                .then(buildRemove())
                .then(buildShow())
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to manage the blacklist";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("bl");
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildAdd() {
        return Cmd.playerCommand("add", "messages.invalid_player")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("player", StringArgumentType.word(), Cmd.ONLINE_PLAYERS),
                                this::executeAdd
                        )
                );
    }

    private int executeAdd(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        String blockPlayerInput = StringArgumentType.getString(ctx, "player");
        Optional<Player> resolved = Cmd.resolveOnlinePlayer(blockPlayerInput);
        if (resolved.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.invalid_player");
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

    private SuggestionProvider<CommandSourceStack> blacklistSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            if (ctx.getSource().getSender() instanceof Player player) {
                dbDriver.selectData("blocked_player", "blacklist", "WHERE uuid = ?", rs -> {
                    if (rs.isEmpty()) return;
                    for (Map<String, Object> i : rs) {
                        String name = (String) i.get("blocked_player");
                        if (name.toLowerCase(Locale.ROOT).startsWith(remaining))
                            builder.suggest(name);
                    }
                }, player.getUniqueId());
            }
            return builder.buildFuture();
        };
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildRemove() {
        return Cmd.playerCommand("remove", "messages.invalid_player")
                .then(
                        Cmd.executesPlayer(
                                Cmd.argument("player", StringArgumentType.word(), blacklistSuggestions()),
                                this::executeRemove
                        )
                );
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        String unblockPlayer = StringArgumentType.getString(ctx, "player");

        dbDriver.selectData("blocked_uuid", "blacklist", "WHERE uuid = ? AND blocked_player = ?", rs -> {
            if (rs.isEmpty()) {
                MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.not_block");
                return;
            }

            dbDriver.deleteData("blacklist", "uuid = ? AND blocked_player = ?", uuid, unblockPlayer);
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.success_unblock");
        }, uuid, unblockPlayer);

        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildShow() {
        return Cmd.playerCommand("show", this::executeShow);
    }

    private int executeShow(CommandContext<CommandSourceStack> ctx, Player player) {
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
    }
}
