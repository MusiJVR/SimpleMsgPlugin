package com.mousejava.simplemsgplugin.command.api;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mousejava.simplemsgplugin.utils.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public final class Cmd {
    public static LiteralArgumentBuilder<CommandSourceStack> literal(String name, String permission) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name);
        if (permission != null) {
            node.requires(source -> source.getSender().hasPermission(permission));
        }
        return node;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return literal(name, null);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> playerOnly(String name, String permission) {
        return requirePlayer(literal(name, permission));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> playerOnly(String name) {
        return requirePlayer(literal(name, null));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> requirePlayer(LiteralArgumentBuilder<CommandSourceStack> node) {
        Predicate<CommandSourceStack> existing = node.getRequirement();
        node.requires(existing.and(source -> source.getSender() instanceof Player));
        return node;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> executesPlayer(LiteralArgumentBuilder<CommandSourceStack> node, PlayerCommandExecutor executor) {
        return node.executes(ctx -> executor.run(ctx, (Player) ctx.getSource().getSender()));
    }

    public static <B extends ArgumentBuilder<CommandSourceStack, B>> B executesPlayer(B node, PlayerCommandExecutor executor) {
        node.executes(ctx -> executor.run(ctx, (Player) ctx.getSource().getSender()));
        return node;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> playerCommand(String name, String permission, PlayerCommandExecutor executor) {
        return executesPlayer(playerOnly(name, permission), executor);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> playerCommand(String name, String permission, String message) {
        return executesPlayer(playerOnly(name, permission), ((ctx, player) -> {
            MessageUtils.sendMiniMessageIfPresent(player, message);
            return Command.SINGLE_SUCCESS;
        }));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> playerCommand(String name, PlayerCommandExecutor executor) {
        return playerCommand(name, null, executor);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> playerCommand(String name, String message) {
        return playerCommand(name, null, message);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> executesSender(LiteralArgumentBuilder<CommandSourceStack> node, SenderCommandExecutor executor) {
        return node.executes(ctx -> executor.run(ctx, ctx.getSource().getSender()));
    }

    public static <B extends ArgumentBuilder<CommandSourceStack, B>> B executesSender(B node, SenderCommandExecutor executor) {
        node.executes(ctx -> executor.run(ctx, ctx.getSource().getSender()));
        return node;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> senderCommand(String name, String permission, SenderCommandExecutor executor) {
        return executesSender(literal(name, permission), executor);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> senderCommand(String name, String permission, String message) {
        return executesSender(literal(name, permission), ((ctx, player) -> {
            MessageUtils.sendMiniMessageIfPresent(player, message);
            return Command.SINGLE_SUCCESS;
        }));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> senderCommand(String name, SenderCommandExecutor executor) {
        return executesSender(literal(name, null), executor);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> senderCommand(String name, String message) {
        return senderCommand(name, null, message);
    }

    public static int usage(CommandSender sender, String message) {
        MessageUtils.sendMiniMessageIfPresent(sender, message);
        return Command.SINGLE_SUCCESS;
    }

    public static int usage(CommandContext<CommandSourceStack> ctx, String message) {
        return usage(ctx.getSource().getSender(), message);
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) {
        return Commands.argument(name, type);
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type, SuggestionProvider<CommandSourceStack> suggestions) {
        return argument(name, type).suggests(suggestions);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> unknown(String message) {
        return Commands.argument("unknown", StringArgumentType.greedyString())
                .executes(ctx -> usage(ctx, message));
    }

    @FunctionalInterface
    public interface PlayerCommandExecutor {
        int run(CommandContext<CommandSourceStack> ctx, Player player) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface SenderCommandExecutor {
        int run(CommandContext<CommandSourceStack> ctx, CommandSender sender) throws CommandSyntaxException;
    }

    public static SuggestionProvider<CommandSourceStack> fixedSuggestions(String... values) {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            for (String value : values) {
                if (value.toLowerCase(Locale.ROOT).startsWith(remaining))
                    builder.suggest(value);
            }
            return builder.buildFuture();
        };
    }

    public static SuggestionProvider<CommandSourceStack> fixedSuggestionsUnsorted(String... values) {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());
            List<Suggestion> suggestions = new ArrayList<>();
            for (String value : values) {
                if (value.toLowerCase(Locale.ROOT).startsWith(remaining))
                    suggestions.add(new Suggestion(range, value));
            }
            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        };
    }

    public static Optional<Player> resolveOnlinePlayer(String input) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getName().equalsIgnoreCase(input))
                .map(Player.class::cast)
                .findFirst();
    }

    public static final SuggestionProvider<CommandSourceStack> ONLINE_PLAYERS = (ctx, builder) -> {
        String remaining = builder.getRemainingLowerCase();
        Bukkit.getOnlinePlayers().forEach(player -> {
            String name = player.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(remaining))
                builder.suggest(name);
        });
        return builder.buildFuture();
    };
}
