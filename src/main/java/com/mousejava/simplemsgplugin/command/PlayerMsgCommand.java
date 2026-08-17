package com.mousejava.simplemsgplugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mousejava.simplemsgplugin.SimpleMsgPlugin;
import com.mousejava.simplemsgplugin.command.api.Cmd;
import com.mousejava.simplemsgplugin.command.api.ICommand;
import com.mousejava.simplemsgplugin.database.DatabaseCacheManager;
import com.mousejava.simplemsgplugin.repository.*;
import com.mousejava.simplemsgplugin.storage.LatestRecipientsStorage;
import com.mousejava.simplemsgplugin.storage.OfflineMessageStorage;
import com.mousejava.simplemsgplugin.utils.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerMsgCommand implements ICommand {
    private final SimpleMsgPlugin plugin;
    private final PlayersRepository players;
    private final PropertiesRepository properties;
    private final OfflineMessagesRepository messages;
    private final BlacklistRepository blacklist;
    private final DatabaseCacheManager cache;
    private final OfflineMessageStorage offlineMessages;
    private final LatestRecipientsStorage latestRecipients;

    public PlayerMsgCommand(JavaPlugin plugin, PlayersRepository players, PropertiesRepository properties, OfflineMessagesRepository messages, BlacklistRepository blacklist, DatabaseCacheManager cache, OfflineMessageStorage offlineMessages, LatestRecipientsStorage latestRecipients) {
        this.plugin = (SimpleMsgPlugin) plugin;
        this.players = players;
        this.properties = properties;
        this.messages = messages;
        this.blacklist = blacklist;
        this.cache = cache;
        this.offlineMessages = offlineMessages;
        this.latestRecipients = latestRecipients;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> create() {
        return Cmd.senderCommand("playermsg", "simplemsgplugin.playermsg", "messages.playermsg.usage")
                .then(
                        Cmd.executesSender(
                                Cmd.argument("player", StringArgumentType.word(), playersSuggestions()),
                                        (ctx, sender) -> Cmd.usage(sender, "messages.playermsg.usage")
                                )
                                .then(
                                        Cmd.executesSender(
                                                Cmd.argument("message", StringArgumentType.greedyString()),
                                                this::executePlayerMsg
                                        )
                                )
                )
                .build();
    }

    @Override
    public String description() {
        return "This command allows you to send private messages to the player";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("msg", "pm", "message", "tell", "w");
    }

    private SuggestionProvider<CommandSourceStack> playersSuggestions() {
        return (ctx, builder) -> {
            String remaining = builder.getRemainingLowerCase();
            StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());
            Set<String> online = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

            List<String> offline = cache.getPlayerNames().stream()
                    .filter(n -> !online.contains(n) && n.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();

            List<Suggestion> suggestions = new ArrayList<>();
            online.forEach(n -> suggestions.add(new Suggestion(range, n)));
            offline.forEach(n -> suggestions.add(new Suggestion(range, n)));

            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        };
    }
    private int executePlayerMsg(CommandContext<CommandSourceStack> ctx, CommandSender sender) {
        String input = StringArgumentType.getString(ctx, "player");
        String message = StringArgumentType.getString(ctx, "message").trim();
        Player target = Cmd.resolveOnlinePlayer(input).orElse(null);
        if (target == null) {
            if (sender instanceof Player p) handleOfflineTarget(p, input, message); else MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.not_send_offline_from_console");
            return Command.SINGLE_SUCCESS;
        }

        UUID senderUuid = sender instanceof Player p ? p.getUniqueId() : null;
        Optional<String> targetUuid = players.findUuidByName(target.getName());
        if (targetUuid.isEmpty()) {
            notifySender(sender, "messages.invalid_player");
            return Command.SINGLE_SUCCESS;
        }

        if (senderUuid != null && senderUuid.toString().equals(targetUuid.get()) && !plugin.getConfig().getBoolean("send_msg_yourself")) {
            notifySender(sender, "messages.playermsg.not_send_youself");
            return Command.SINGLE_SUCCESS;
        }

        if (senderUuid != null
                && (blacklist.isBlockedBy(senderUuid, UUID.fromString(targetUuid.get()))
                || blacklist.isBlocked(senderUuid, UUID.fromString(targetUuid.get())))) {
            notifySender(sender, blacklist.isBlockedBy(senderUuid, UUID.fromString(targetUuid.get())) ? "messages.blacklist.you_cannot_send" : "messages.blacklist.you_have_blocked");
            return Command.SINGLE_SUCCESS;
        }

        deliverMessage(sender, target, message); return Command.SINGLE_SUCCESS;
    }

    private void deliverMessage(CommandSender sender, Player target, String message) {
        String senderName = sender.getName();
        String targetName = target.getName();

        MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.sender_pattern",
                msg -> msg
                        .replace("<sender>", senderName)
                        .replace("<receiver>", targetName)
                        .replace("<message>", message),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + targetName + " "))
        );

        MessageUtils.sendMiniMessageIfPresent(target, "messages.playermsg.receiver_pattern",
                msg -> msg
                        .replace("<sender>", senderName)
                        .replace("<receiver>", targetName)
                        .replace("<message>", message),
                component -> component
                        .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + senderName + " "))
        );

        Utils.msgPlaySound(properties, target);
        if (sender instanceof Player) {
            latestRecipients.put(senderName, targetName);
            latestRecipients.put(targetName, senderName);
        }
    }

    private void handleOfflineTarget(Player sender, String input, String message) {
        Optional<String> resolved = cache.getPlayerNames().stream()
                .filter(n -> n.equalsIgnoreCase(input))
                .findFirst();

        if (resolved.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.invalid_player");
            return;
        }

        UUID uuid = sender.getUniqueId();
        offlineMessages.put(uuid, resolved.get(), message);

        MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.player_missing");

        if (properties.getBoolean(uuid, "confirm_sending", plugin.getConfig().getBoolean("confirm_sending"))) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.send_offline");
            MessageUtils.sendMiniMessageComponent(sender, "messages.playermsg.accept_send",
                    component -> component
                            .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_offline")))
                            .clickEvent(ClickEvent.runCommand("acceptsend"))
            );
        } else {
            saveOffline(sender);
        }

        Scheduler.runLater(() -> {
            offlineMessages.find(uuid)
                    .filter(pendingMessage -> pendingMessage.receiver().equals(resolved.get()))
                    .filter(pendingMessage -> pendingMessage.message().equals(message))
                    .ifPresent(pendingMessage -> offlineMessages.remove(uuid, pendingMessage));
        }, 1200);
    }

    private void saveOffline(Player sender) {
        UUID uuid = sender.getUniqueId();
        offlineMessages.find(uuid).ifPresent(pendingMessage -> {
            messages.save(uuid, sender.getName(), pendingMessage.receiver(), pendingMessage.message());

            MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.send_offline_successfully");
            Utils.msgPlaySound(properties, sender);

            offlineMessages.remove(uuid, pendingMessage);
        });
    }

    private void notifySender(CommandSender sender, String path) {
        if (sender instanceof Player)
            MessageUtils.sendMiniMessageIfPresent(sender, path);
    }
}
