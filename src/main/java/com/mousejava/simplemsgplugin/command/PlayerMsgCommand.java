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
import com.mousejava.simplemsgplugin.database.repository.BlacklistRepository;
import com.mousejava.simplemsgplugin.database.repository.OfflineMessagesRepository;
import com.mousejava.simplemsgplugin.database.repository.PlayersRepository;
import com.mousejava.simplemsgplugin.database.repository.PropertiesRepository;
import com.mousejava.simplemsgplugin.service.SkinService;
import com.mousejava.simplemsgplugin.storage.LatestRecipientsStorage;
import com.mousejava.simplemsgplugin.storage.OfflineMessageStorage;
import com.mousejava.simplemsgplugin.utils.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
    private final OfflineMessageStorage offlineMessages;
    private final LatestRecipientsStorage latestRecipients;
    private final SkinService skinService;

    public PlayerMsgCommand(JavaPlugin plugin, PlayersRepository players, PropertiesRepository properties, OfflineMessagesRepository messages, BlacklistRepository blacklist, OfflineMessageStorage offlineMessages, LatestRecipientsStorage latestRecipients, SkinService skinService) {
        this.plugin = (SimpleMsgPlugin) plugin;
        this.players = players;
        this.properties = properties;
        this.messages = messages;
        this.blacklist = blacklist;
        this.offlineMessages = offlineMessages;
        this.latestRecipients = latestRecipients;
        this.skinService = skinService;
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

            List<String> offline = players.findAllNames().stream()
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
        String targetName = target.getName();
        boolean allowSelf = plugin.getConfig().getBoolean("send_msg_yourself");
        Scheduler.runAsync(() -> {
            Optional<String> targetUuid = players.findUuidByName(targetName);
            String error = null;
            if (targetUuid.isEmpty()) {
                error = "messages.invalid_player";
            } else if (senderUuid != null && senderUuid.toString().equals(targetUuid.get()) && !allowSelf) {
                error = "messages.playermsg.not_send_youself";
            } else if (senderUuid != null) {
                UUID resolvedUuid = UUID.fromString(targetUuid.get());
                synchronized (blacklist) {
                    if (blacklist.isBlockedBy(senderUuid, resolvedUuid)) {
                        error = "messages.blacklist.you_cannot_send";
                    } else if (blacklist.isBlocked(senderUuid, resolvedUuid)) {
                        error = "messages.blacklist.you_have_blocked";
                    }
                }
            }

            String errorPath = error;
            Scheduler.run(() -> {
                if (sender instanceof Player player && !player.isOnline()) return;

                if (errorPath != null) {
                    notifySender(sender, errorPath);
                } else if (target.isOnline()) {
                    deliverMessage(sender, target, message);
                }
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    private void deliverMessage(CommandSender sender, Player target, String message) {
        String senderName = sender.getName();
        String targetName = target.getName();

        Component senderHead = sender instanceof Player p
                ? skinService.buildPlayerHeadComponent(skinService.getSkinBase64(p))
                : Component.empty();
        Component targetHead = skinService.buildPlayerHeadComponent(skinService.getSkinBase64(target));

        TagResolver heads = TagResolver.resolver(
                Placeholder.component("sender_head", senderHead),
                Placeholder.component("receiver_head", targetHead)
        );
        if (sender instanceof Player) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.sender_pattern",
                    msg -> msg
                            .replace("<sender>", senderName)
                            .replace("<receiver>", targetName)
                            .replace("<message>", message),
                    heads,
                    component -> component
                            .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.playermsg.click_send_reply")))
                            .clickEvent(ClickEvent.suggestCommand("/msg " + targetName + " "))
            );
        } else {
            MessageUtils.sendMiniMessageTransformed(sender, "messages.playermsg.console_pattern",
                    msg -> msg
                            .replace("<sender>", senderName)
                            .replace("<receiver>", targetName)
                            .replace("<message>", message)
            );
        }

        MessageUtils.sendMiniMessageIfPresent(target, "messages.playermsg.receiver_pattern",
                msg -> msg
                        .replace("<sender>", senderName)
                        .replace("<receiver>", targetName)
                        .replace("<message>", message),
                heads,
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
        Optional<String> resolved = players.findAllNames().stream()
                .filter(n -> n.equalsIgnoreCase(input))
                .findFirst();

        if (resolved.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(sender, "messages.invalid_player");
            return;
        }

        UUID uuid = sender.getUniqueId();
        boolean defaultConfirm = plugin.getConfig().getBoolean("confirm_sending");
        Scheduler.runAsync(() -> {
            boolean confirm = properties.getBoolean(uuid, "confirm_sending", defaultConfirm);
            Scheduler.runForEntity(sender, () -> {
                if (!sender.isOnline()) return;

                prepareOfflineMessage(sender, uuid, resolved.get(), message, confirm);
            });
        });
    }

    private void prepareOfflineMessage(Player sender, UUID uuid, String receiver, String message, boolean confirm) {
        offlineMessages.put(uuid, receiver, message);
        MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.player_missing");
        if (confirm) {
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
                    .filter(pending -> pending.receiver().equals(receiver))
                    .filter(pending -> pending.message().equals(message))
                    .ifPresent(pending -> offlineMessages.remove(uuid, pending));
        }, 1200);
    }

    private void saveOffline(Player sender) {
        UUID uuid = sender.getUniqueId();
        String senderName = sender.getName();
        offlineMessages.find(uuid).ifPresent(pendingMessage -> {
            if (!offlineMessages.beginSending(pendingMessage)) return;

            Scheduler.runAsync(() -> {
                try {
                    synchronized (messages) {
                        messages.save(uuid, senderName, pendingMessage.receiver(), pendingMessage.message());
                        offlineMessages.remove(uuid, pendingMessage);
                    }

                    Scheduler.runForEntity(sender, () -> {
                        if (!sender.isOnline()) return;

                        MessageUtils.sendMiniMessageIfPresent(sender, "messages.playermsg.send_offline_successfully");
                        Utils.msgPlaySound(properties, sender);
                    });
                } finally {
                    offlineMessages.finishSending(pendingMessage);
                }
            });
        });
    }

    private void notifySender(CommandSender sender, String path) {
        if (sender instanceof Player)
            MessageUtils.sendMiniMessageIfPresent(sender, path);
    }
}
