package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.database.repository.BlacklistRepository;
import com.mousejava.simplemsgplugin.database.repository.PlayersRepository;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import com.mousejava.simplemsgplugin.utils.Scheduler;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BlacklistCommand implements ICommand {
    private final BlacklistRepository blacklist;
    private final PlayersRepository players;

    public BlacklistCommand(BlacklistRepository blacklist, PlayersRepository players) {
        this.blacklist = blacklist;
        this.players = players;
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
        Optional<Player> target = Cmd.resolveOnlinePlayer(StringArgumentType.getString(ctx, "player"));
        if (target.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.invalid_player");
            return Command.SINGLE_SUCCESS;
        }

        if (target.get().getUniqueId().equals(player.getUniqueId())) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.yourself");
            return Command.SINGLE_SUCCESS;
        }

        UUID uuid = player.getUniqueId();
        UUID targetUuid = target.get().getUniqueId();
        String targetName = target.get().getName();
        Scheduler.runAsync(() -> {
            String path;
            synchronized (blacklist) {
                if (blacklist.isBlocked(uuid, targetUuid)) {
                    path = "messages.blacklist.already_block";
                } else {
                    blacklist.add(uuid, targetUuid, targetName);
                    path = "messages.blacklist.success_block";
                }
            }

            Scheduler.runForEntity(player, () -> {
                if (player.isOnline())
                    MessageUtils.sendMiniMessageIfPresent(player, path);
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    private SuggestionProvider<CommandSourceStack> blacklistSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            if (!(ctx.getSource().getSender() instanceof Player player))
                return builder.buildFuture();

            UUID uuid = player.getUniqueId();
            CompletableFuture<Suggestions> result = new CompletableFuture<>();
            Scheduler.runAsync(() -> {
                try {
                    blacklist.listNames(uuid).stream()
                            .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(remaining))
                            .forEach(builder::suggest);
                    result.complete(builder.build());
                } catch (Exception failure) {
                    result.completeExceptionally(failure);
                }
            });

            return result;
        };
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildRemove() {
        return Cmd.playerCommand("remove", "messages.invalid_player").then(Cmd.executesPlayer(
                Cmd.argument("player", StringArgumentType.word(), blacklistSuggestions()), this::executeRemove));
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx, Player player) {
        String name = StringArgumentType.getString(ctx, "player");
        UUID playerUuid = player.getUniqueId();
        Scheduler.runAsync(() -> {
            Optional<String> uuid = players.findUuidByName(name);
            String path;
            synchronized (blacklist) {
                if (uuid.isEmpty() || !blacklist.isBlocked(playerUuid, UUID.fromString(uuid.get()))) {
                    path = "messages.blacklist.not_block";
                } else {
                    blacklist.remove(playerUuid, UUID.fromString(uuid.get()));
                    path = "messages.blacklist.success_unblock";
                }
            }

            Scheduler.runForEntity(player, () -> {
                if (player.isOnline())
                    MessageUtils.sendMiniMessageIfPresent(player, path);
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildShow() {
        return Cmd.playerCommand("show", this::executeShow);
    }

    private int executeShow(CommandContext<CommandSourceStack> ctx, Player player) {
        UUID uuid = player.getUniqueId();
        Scheduler.runAsync(() -> {
            List<String> names = blacklist.listNames(uuid);
            Scheduler.runForEntity(player, () -> {
                if (!player.isOnline()) return;

                if (names.isEmpty()) {
                    MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.empty");
                } else {
                    MessageUtils.sendMiniMessageTransformed(player, "messages.blacklist.players",
                            msg -> msg.replace("<blacklist>", String.join(", ", names)));
                }
            });
        });

        return Command.SINGLE_SUCCESS;
    }
}
