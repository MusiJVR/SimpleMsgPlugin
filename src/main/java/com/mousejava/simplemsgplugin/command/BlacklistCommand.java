package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.repository.BlacklistRepository;
import com.mousejava.simplemsgplugin.repository.PlayersRepository;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.*;

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

        if (blacklist.isBlocked(player.getUniqueId(), target.get().getUniqueId())) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.already_block");
        } else {
            blacklist.add(player.getUniqueId(), target.get().getUniqueId(), target.get().getName());
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.success_block");
        }

        return Command.SINGLE_SUCCESS;
    }

    private SuggestionProvider<CommandSourceStack> blacklistSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            if (ctx.getSource().getSender() instanceof Player player)
                blacklist.listNames(player.getUniqueId()).stream()
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(remaining))
                        .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildRemove() {
        return Cmd.playerCommand("remove", "messages.invalid_player").then(Cmd.executesPlayer(
                Cmd.argument("player", StringArgumentType.word(), blacklistSuggestions()), this::executeRemove));
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx, Player player) {
        String name = StringArgumentType.getString(ctx, "player");
        Optional<String> uuid = players.findUuidByName(name);

        if (uuid.isEmpty() || !blacklist.isBlocked(player.getUniqueId(), UUID.fromString(uuid.get()))) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.not_block");
        } else {
            blacklist.remove(player.getUniqueId(), UUID.fromString(uuid.get()));
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.success_unblock");
        }

        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildShow() {
        return Cmd.playerCommand("show", this::executeShow);
    }

    private int executeShow(CommandContext<CommandSourceStack> ctx, Player player) {
        List<String> names = blacklist.listNames(player.getUniqueId());

        if (names.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.blacklist.empty");
        } else {
            MessageUtils.sendMiniMessageTransformed(player, "messages.blacklist.players",
                    msg -> msg
                            .replace("<blacklist>", String.join(", ", names))
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
